package com.vaadin.data.hbnutil.tests;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil
{
	private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
	private static SessionFactory sessionFactory;

	static
	{
		try
		{
			logger.debug("Initializing HibernateUtil");
			
			final Configuration configuration = new Configuration();
			configuration.configure();
			
			final ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder();

			final ServiceRegistry serviceRegistry = serviceRegistryBuilder
					.applySettings(configuration.getProperties())
					.buildServiceRegistry();

			sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		}
		catch (Throwable e)
		{
			logger.error(StackToString(e));
			throw new ExceptionInInitializerError(e);
		}
	}

	private static String StackToString(Throwable exception)
	{
		try
		{
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			exception.printStackTrace(printWriter);
			return stringWriter.toString();
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			return "Stack Trace Unavailable";
		}
	}
	
	public static SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}
	
	public static void insertExampleNodes(int nodesToLoad)
	{
		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		if (!session.getTransaction().isActive())
			session.beginTransaction();

		SampleNode rootNode = new SampleNode();
		rootNode.setParent(null);
		session.save(rootNode);

		for (int i = 1; i < nodesToLoad; i++)
		{
			SampleNode sampleNode = new SampleNode();
			sampleNode.setParent(rootNode);
			session.save(sampleNode);
		}
	}
}
