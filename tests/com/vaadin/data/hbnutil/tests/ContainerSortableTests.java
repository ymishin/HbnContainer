package com.vaadin.data.hbnutil.tests;

import static org.junit.Assert.assertTrue;
import java.util.Collection;

import org.hibernate.*;
import org.junit.*;
import com.vaadin.data.hbnutil.*;

public class ContainerSortableTests
{
	private static SessionFactory sessionFactory = null;
	private static Session session = null;
	private static HbnContainer<SampleNode> container = null;
	private static int recordsToLoad = 10;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		sessionFactory = HibernateUtil.getSessionFactory();
		session = sessionFactory.openSession();
		container = new HbnContainer<SampleNode>(SampleNode.class, sessionFactory);
		HibernateUtil.insertExampleNodes(recordsToLoad);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		final Transaction transaction = session.getTransaction();
		
		if (transaction.isActive())
			transaction.commit();

		if (session != null)
			session.close();
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	//
	// ************************************************************************************************************
	//

	@Test
	public final void testSort()
	{
		Object[] propertyIds = new Object[] { "id" };
		boolean[] sortAscending = new boolean[] { true };
		container.sort(propertyIds, sortAscending);
		
		Collection<?> entityIds = container.getItemIds();
		assertTrue(entityIds.size() == recordsToLoad);
		
		long lastId = -1;
		long currId = -1;

		for (Object entityId : container.getItemIds())
		{
			SampleNode entity = container.getItem(entityId).getPojo();
			currId = entity.getId();
			assertTrue(currId > lastId);
			lastId = currId;
		}
	
		sortAscending = new boolean[] { false };
		container.sort(propertyIds, sortAscending);
		
		entityIds = container.getItemIds();
		assertTrue(entityIds.size() == recordsToLoad);
		
		lastId = -1;
		currId = -1;

		for (Object entityId : container.getItemIds())
		{
			SampleNode entity = container.getItem(entityId).getPojo();
			currId = entity.getId();
			assertTrue(currId < lastId);
			lastId = currId;
		}
	}

	@Test
	public final void testGetSortableContainerPropertyIds()
	{
		Collection<?> propertyIds = container.getSortableContainerPropertyIds();
		assertTrue(propertyIds.contains("title"));
		assertTrue(propertyIds.contains("created"));
		assertTrue(propertyIds.contains("parent"));
	}
}
