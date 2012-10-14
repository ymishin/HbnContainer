package com.vaadin.demo.workoutlog;

import java.util.Calendar;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil
{
	private static final SessionFactory sessionFactory;
	private static Type defaultType;

	static
	{
		try
		{
			Configuration cfg = new Configuration();
			cfg.setProperty(Environment.DRIVER, "org.hsqldb.jdbcDriver");
			cfg.setProperty(Environment.URL, "jdbc:hsqldb:mem:Workout");
			cfg.setProperty(Environment.USER, "sa");
			cfg.setProperty(Environment.DIALECT, HSQLDialect.class.getName());
			cfg.setProperty(Environment.SHOW_SQL, "true");
			cfg.setProperty(Environment.HBM2DDL_AUTO, "create-drop");
			cfg.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
			cfg.addAnnotatedClass(Workout.class);
			cfg.addAnnotatedClass(Type.class);

			ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();

			sessionFactory = cfg.buildSessionFactory(serviceRegistry);

			insertExampleTypes();

		}
		catch (Throwable ex)
		{
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

	public static void insertExampleTypes()
	{
		Session sess = getSessionFactory().getCurrentSession();

		if (!sess.getTransaction().isActive())
			sess.beginTransaction();

		Type type;

		type = new Type();
		type.setTitle("Running");
		sess.save(type);

		defaultType = type;
		System.err.println("Default type id : " + defaultType.getId());

		type = new Type();
		type.setTitle("MTB");
		sess.save(type);

		type = new Type();
		type.setTitle("Trecking");
		sess.save(type);

		type = new Type();
		type.setTitle("Swimming");
		sess.save(type);

		type = new Type();
		type.setTitle("Orienteering");
		sess.save(type);

		type = new Type();
		type.setTitle("Football");
		sess.save(type);

	}

	public static void insertExampleData(int trainingsToLoad)
	{
		Session sess = getSessionFactory().getCurrentSession();

		if (!sess.getTransaction().isActive())
			sess.beginTransaction();

		// insert some sample data
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);

		String[] titles = new String[] { "A short easy one", "intervals", "very long", "just shaking legs after work", "long one with Paul", "test run" };

		c.add(Calendar.DATE, -trainingsToLoad);

		Random rnd = new Random();

		Workout r;

		for (int i = 0; i < trainingsToLoad; i++)
		{
			r = new Workout();
			c.set(Calendar.HOUR_OF_DAY, 12 + (rnd.nextInt(11) - rnd.nextInt(11)));
			r.setDate(c.getTime());
			r.setTitle(titles[rnd.nextInt(titles.length)]);
			r.setKilometers(Math.round(rnd.nextFloat() * 30));
			r.setTrainingType(defaultType);
			sess.save(r);
			c.add(Calendar.DATE, 1);
		}

	}
}
