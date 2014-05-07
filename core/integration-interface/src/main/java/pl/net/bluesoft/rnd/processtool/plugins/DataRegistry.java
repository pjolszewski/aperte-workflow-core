package pl.net.bluesoft.rnd.processtool.plugins;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.dao.*;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 21:46
 */
public interface DataRegistry {
	void addClassLoader(String name, ClassLoader loader);
	void removeClassLoader(String name);
	ClassLoader getModelAwareClassLoader(ClassLoader parent);

	boolean registerModelExtension(Class<?>... cls);
	boolean unregisterModelExtension(Class<?>... cls);
	void commitModelExtensions();

	void addHibernateResource(String name, byte[] resource);
	void removeHibernateResource(String name);

    Dialect getHibernateDialect();

	boolean isJta();

	SessionFactory getSessionFactory();

	ProcessToolContextFactory getProcessToolContextFactory();

	ProcessDictionaryDAO getProcessDictionaryDAO(Session hibernateSession);
	ProcessInstanceDAO getProcessInstanceDAO(Session hibernateSession);
	ProcessInstanceFilterDAO getProcessInstanceFilterDAO(Session hibernateSession);
	UserSubstitutionDAO getUserSubstitutionDAO(Session hibernateSession);
	ProcessInstanceSimpleAttributeDAO getProcessInstanceSimpleAttributeDAO(Session hibernateSession);
	ProcessDefinitionDAO getProcessDefinitionDAO(Session hibernateSession);

    OperationLockDAO getOperationLockDAO(Session hibernateSession);

    void registerAttributesCopier(Class<? extends IAttributesCopier> copierClass);
    void unregisterAttributesCopier(Class<? extends IAttributesCopier> copierClass);
    List<Class<? extends IAttributesCopier>> getAttributesCopiers();
    // List<IAttributesCopier> getAttributesCopiersFor(Class<? extends IAttribute> clazz);

}
