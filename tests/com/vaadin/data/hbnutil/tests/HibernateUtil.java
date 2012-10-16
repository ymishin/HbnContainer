package com.vaadin.data.hbnutil.tests;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Random;

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
	

	private static Type defaultType;

	public static void insertExampleTypes()
	{
		final Session session = sessionFactory.getCurrentSession();

		if (!session.getTransaction().isActive())
			session.beginTransaction();

		Type type;

		type = new Type();
		type.setTitle("Running");
		session.save(type);

		defaultType = type;
		logger.debug("Default type id : " + defaultType.getId());

		type = new Type();
		type.setTitle("MTB");
		session.save(type);

		type = new Type();
		type.setTitle("Trecking");
		session.save(type);

		type = new Type();
		type.setTitle("Swimming");
		session.save(type);

		type = new Type();
		type.setTitle("Orienteering");
		session.save(type);

		type = new Type();
		type.setTitle("Football");
		session.save(type);

	}

	public static void insertExampleData(int trainingsToLoad)
	{
		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		final String[] titles = new String[] { "A short easy one", "intervals", "very long", "just shaking legs after work", "long one with Paul", "test run" };
		final Random random = new Random();

		if (!session.getTransaction().isActive())
			session.beginTransaction();

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.add(Calendar.DATE, -trainingsToLoad);

		Workout workout;
		for (int i = 0; i < trainingsToLoad; i++)
		{
			workout = new Workout();
			calendar.set(Calendar.HOUR_OF_DAY, 12 + (random.nextInt(11) - random.nextInt(11)));
			workout.setDate(calendar.getTime());
			workout.setTitle(titles[random.nextInt(titles.length)]);
			workout.setKilometers(Math.round(random.nextFloat() * 30));
			workout.setTrainingType(defaultType);
			session.save(workout);
			calendar.add(Calendar.DATE, 1);
		}
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
