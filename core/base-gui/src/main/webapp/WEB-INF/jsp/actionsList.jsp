<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script type="text/javascript">
//<![CDATA[	

	function disableButtons()
	{
		$('#actions-list').find('button').prop('disabled', true);
	}
	
	function enableButtons()
	{
		$('#actions-list').find('button').prop('disabled', false);
	}
	
	function saveAction(taskId)
	{
		clearAlerts();
		
		var errors = [];
		<!-- Validate html widgets -->
		$.each(widgets, function() 
		{
			var errorMessages = this.validate();
			$.each(errorMessages, function() {
				errors.push(this);
				addAlert(this);
			});
	    });
		
		if(errors.length > 0)
		{
			enableButtons();
			return;
		}
		
		var widgetData = [];
		
		$.each(widgets, function() 
		{
			var widgetDataBean = new WidgetDataBean(this.widgetId, this.name, this.getData());
			widgetData.push(widgetDataBean);
	    });
		
		var JsonWidgetData = JSON.stringify(widgetData, null, 2);
		
		var state = 'OK';
		var newBpmTask = $.getJSON('<spring:url value="/processes/saveAction.json"/>', 
		{
			"taskId": taskId,
			"widgetData": JsonWidgetData
		})
		.done(function(data) 
		{ 
			if(data.errors != null)
			{
				addAlerts(data.errors);
			}
		})
		.always(function() 
		{ 
			enableButtons();
		})
		.fail(function(data) 
		{ 
			addAlerts(data.errors);
		});
		
		return state;
	}
	
	function addAlerts(alertsMessages)
	{
		$('#alerts-list').empty();
		$.each( alertsMessages, function( ) 
		{
			console.log( "alert: "+this.message );
			addAlert(this.message);
		});
	}
	
	function addAlert(alertMessage)
	{
		$('#alerts-list').append('<div class="alert"><button type="button" class="close" data-dismiss="alert">&times;</button>'+alertMessage+'</div>')
	}
	
	function clearAlerts()
	{
		$('#alerts-list').empty();
	}
	
	function performAction(button, actionName, skipSaving, taskId)
	{
		var JsonWidgetData = "[{}]";
		if(skipSaving != true)
		{
			clearAlerts();
			
			var errors = [];
			<!-- Validate html widgets -->
			$.each(widgets, function() 
			{
				var errorMessages = this.validate();
				$.each(errorMessages, function() {
					errors.push(this);
					addAlert(this);
				});
			});
			
			if(errors.length > 0)
			{
				enableButtons();
				return;
			}
			
			var widgetData = [];
			
			$.each(widgets, function() 
			{
				var widgetDataBean = new WidgetDataBean(this.widgetId, this.name, this.getData());
				widgetData.push(widgetDataBean);
			});
			
			JsonWidgetData = JSON.stringify(widgetData, null, 2);
		}
		
		var newBpmTask = $.getJSON('<spring:url value="/processes/performAction.json"/>', 
		{
			"taskId": taskId,
			"actionName": actionName,
			"skipSaving": skipSaving,
			"widgetData": JsonWidgetData
		})
		.done(function(data) 
		{ 
			console.log( "DONE: "+data); 
			reloadQueues();
			
			if(data == null)
			{
			    closeProcessView();
				reloadCurrentQueue();
				windowManager.showProcessList();
				
				return;
			}
			else if(data.errors.length > 0)
			{
				addAlerts(data.errors);
				return;
			}

			var taskId = data.taskId;
			var processStateConfigurationId = data.processStateConfigurationId;
			
			if(taskId != null)
			{
				loadProcessView(processStateConfigurationId, taskId);
			}
			else
			{
				closeProcessView();
				reloadCurrentQueue();
				windowManager.showProcessList();
			}
		})
		.fail(function() { addAlerts(data.errors); })
		.always(function(data) 
		{ 
			if(data != null)
			{
				enableButtons();
			}
		});
	}
	
	function onSaveButton(taskId)
	{
		disableButtons();
		saveAction(taskId);
	}
	
	function onCancelButton()
	{
		reloadQueues();
		disableButtons(); 
		windowManager.previousView();
	}
	
	function closeProcessView()
	{
		$('#vaadin-widgets').empty();
		$('#actions-list').empty();
		
		windowManager.showProcessList();
	}


//]]>
</script>