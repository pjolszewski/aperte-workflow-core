package pl.net.bluesoft.rnd.processtool.model;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

import java.util.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;

import org.hibernate.annotations.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;

/**
 * Entity representing process instance data. It should be persisted in appropriate database.
 * Custom processes can reference this class.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_instance")
public class ProcessInstance extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_INST")
			}
	)
	@Column(name = "id")
	protected Long id;

	private String externalKey;
	private String internalId;
	private String definitionName;
	private String description;
	private String keyword;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @Transient
    private String[] assignees;
    @Transient
    private String[] taskQueues;
    @Transient
    private BpmTask[] activeTasks;

	private Date createDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="creator_id")
	private UserData creator;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="definition_id")
	private ProcessDefinitionConfig definition;

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessInstanceAttribute> processAttributes = new HashSet<ProcessInstanceAttribute>();

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessInstanceLog> processLogs = new HashSet<ProcessInstanceLog>();

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="parent_id")
	private Set<ProcessInstance> children = new HashSet<ProcessInstance>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="parent_id")
	private ProcessInstance parent;
	
	/** Owners of the process. Owner is diffrent then process creator. Process can have many owners */
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "pt_process_instance_owners", joinColumns = @JoinColumn(name = "process_id"))
	private Set<String> owners = new HashSet<String>();

	@Transient
	private Set<ProcessInstanceAttribute> toDelete;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public ProcessInstance getRootProcessInstance() {
    	ProcessInstance parentProcess = this;
    	while (parentProcess.getParent() != null){
    		parentProcess = parentProcess.getParent();
    	}
    	return parentProcess;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public ProcessInstance(String externalKey, UserData creator, String definionName) {
		this.externalKey = externalKey;
		this.creator = creator;
		this.definitionName = definionName;
		this.createDate = new Date();
	}

	public ProcessInstance() {
	}

	public String getExternalKey() {
		if (externalKey == null && parent != null){
			return parent.getExternalKey();
		}
		return externalKey;
	}

	public void setExternalKey(String externalKey) {	
		this.externalKey = externalKey;
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public UserData getCreator() {
		return creator;
	}

	public void setCreator(UserData creator) {
		this.creator = creator;
	}

	public String getDefinitionName() {
		return definitionName;
	}

	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}

	public Set<String> getOwners() {
		return owners;
	}

	public void setOwners(Set<String> ownersLogins) {
		this.owners = ownersLogins;
	}

	public void addOwner(String ownerLogin) {
		this.owners.add(ownerLogin);
	}

	public void addOwners(Collection<String> ownerLogins) {
		this.owners.addAll(ownerLogins);
	}

	public void removeOwner(String ownerLogin) {
		this.owners.remove(ownerLogin);
	}

	public Set<ProcessInstanceAttribute> getProcessAttributes() {
		if (processAttributes == null) {
			processAttributes = new HashSet<ProcessInstanceAttribute>();
		}
		return processAttributes;
	}

	public void setProcessAttributes(Set<ProcessInstanceAttribute> processAttributes) {
		this.processAttributes = processAttributes;
	}

	public ProcessDefinitionConfig getDefinition() {
		return definition;
	}

	public void setDefinition(ProcessDefinitionConfig definition) {
		this.definition = definition;
	}

	public void removeAttribute(ProcessInstanceAttribute attr) {
		attr.setProcessInstance(null);
		processAttributes.remove(attr);
		if (attr.getId() > 0) {
            if (toDelete == null) {
                toDelete = new HashSet<ProcessInstanceAttribute>();
            }
            toDelete.add(attr);
        }
	}

	public Set<ProcessInstanceAttribute> getToDelete() {
		return toDelete;
	}

	public void addAttribute(ProcessInstanceAttribute attr) {
		attr.setProcessInstance(this);
		processAttributes.add(attr);
	}

	public Set<ProcessInstanceLog> getProcessLogs() {
		return processLogs;
	}

	public void setProcessLogs(Set<ProcessInstanceLog> processLogs) {
		this.processLogs = processLogs;
	}

	public void addProcessLog(ProcessInstanceLog log) {
		log.setProcessInstance(this);
		processLogs.add(log);
	}

	public ProcessInstanceAttribute findAttributeByKey(String key) {
		Set<ProcessInstanceAttribute> attrs = getProcessAttributes();
		for (ProcessInstanceAttribute pia : attrs) {
			if (pia.getKey() != null && pia.getKey().equals(key)) {
				return pia;
			}
		}
		return null;
	}

    public <T extends ProcessInstanceAttribute> T findAttributeByClass(Class<T> clazz) {
        Set<ProcessInstanceAttribute> attrs = getProcessAttributes();
        for (ProcessInstanceAttribute pia : attrs) {
            if (clazz.isAssignableFrom(pia.getClass())) {
                return (T) pia;
            }
        }
        return null;
    }

	public <T extends ProcessInstanceAttribute> T findOrCreateAttribute(Class<T> attrClass) {
		T attribute = findAttributeByClass(attrClass);
		if(attribute == null) {
			try {
				attribute = attrClass.newInstance();
				addAttribute(attribute);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return attribute;	
	}
    
    public <T extends ProcessInstanceAttribute> Set<T> findAttributesByClass(Class<T> clazz) {
        Set<T> result = new HashSet<T>();
        Set<ProcessInstanceAttribute> attrs = getProcessAttributes();
        for (ProcessInstanceAttribute pia : attrs) {
            if (clazz.isAssignableFrom(pia.getClass())) {
                result.add((T) pia);
            }
        }
        return result;
    }

    public String getSimpleAttributeValue(String key) {
        ProcessInstanceAttribute attr = findAttributeByKey(key);
        return attr != null ?  ((ProcessInstanceSimpleAttribute)attr).getValue() : null;
    }

    public String getSimpleAttributeValue(String key, String default_) {
        ProcessInstanceAttribute attr = findAttributeByKey(key);
        return attr != null ? ((ProcessInstanceSimpleAttribute)attr).getValue() : default_;
    }

	public Map<String, String> getSimpleAttributeValues() {
		Map<String, String> result = new HashMap<String, String>();

		for (ProcessInstanceAttribute attribute : getProcessAttributes()) {
			if (attribute instanceof ProcessInstanceSimpleAttribute) {
				ProcessInstanceSimpleAttribute simpleAttribute = (ProcessInstanceSimpleAttribute)attribute;

				result.put(simpleAttribute.getKey(), simpleAttribute.getValue());
			}
		}
		return result;
	}


	public String getInheritedSimpleAttributeValue(String key) {
		return getInheritedSimpleAttributeValue(key, null);
	}

	public String getInheritedSimpleAttributeValue(String key, String default_) {
		for (ProcessInstance pi = this; pi != null; pi = pi.getParent()) {
			ProcessInstanceAttribute attr = findAttributeByKey(key);
			if (attr instanceof ProcessInstanceSimpleAttribute) {
				return ((ProcessInstanceSimpleAttribute)attr).getValue();
			}
		}
		return default_;
	}

    public void setSimpleAttribute(String key, String value) {
        ProcessInstanceSimpleAttribute attr = (ProcessInstanceSimpleAttribute)findAttributeByKey(key);
        if (attr != null) {
            attr.setValue(value);
        }
        else {
            addAttribute(new ProcessInstanceSimpleAttribute(key, value));
        }
    }

    public void addDictionaryAttributeItem(String dictionary, String key, String value){
        ProcessInstanceDictionaryAttribute attr = (ProcessInstanceDictionaryAttribute)findAttributeByKey(dictionary);
        if (attr == null) {
            addAttribute(attr = new ProcessInstanceDictionaryAttribute(dictionary));
        }
        attr.put(key, value);

    }

    public String[] getAssignees() {
        return nvl(assignees, new String[] { });
    }

    public void setAssignees(String... assignees) {
        this.assignees = assignees;
    }

    public String[] getTaskQueues() {
        return nvl(taskQueues, new String[] { });
    }

    public void setTaskQueues(String... taskQueues) {
        this.taskQueues = taskQueues;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public BpmTask[] getActiveTasks() {
        return activeTasks;
    }

    public void setActiveTasks(BpmTask[] activeTasks) {
        this.activeTasks = Lang2.noCopy(activeTasks);
    }

	public Set<ProcessInstance> getChildren() {
		return children;
	}

	public void setChildren(Set<ProcessInstance> children) {
		this.children = children;
	}

	public ProcessInstance getParent() {
		return parent;
	}

	public void setParent(ProcessInstance parent) {
		this.parent = parent;
	}
	
	/** Method checks if the process is in running or new state */
	public boolean isProcessRunning()
	{
		return status == ProcessStatus.NEW || status == ProcessStatus.RUNNING;
	}

	@Override
	public String toString() {
		return "ProcessInstance [externalKey=" + getExternalKey() + ", internalId=" + internalId + "]";
	}

	/** Check if process is subprocess (has parent process) */
	public boolean isSubprocess() {
		return getParent() != null;
	}
}
