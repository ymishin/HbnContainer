package com.vaadin.data.hbnutil.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.hibernate.*;
import org.junit.*;
import com.vaadin.data.hbnutil.*;

public class ContainerIndexedTests
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
	public final void testIndexOfId()
	{
		final Object entityId = container.firstItemId();
		assertNotNull(entityId);
		assertTrue(container.indexOfId(entityId) == 0);
	}

	@Test
	public final void testGetIdByIndex()
	{
		final Object entityId = container.getIdByIndex(0);
		assertNotNull(entityId);
		
		final Object firstId = container.firstItemId();
		assertNotNull(firstId);
		assertTrue(entityId.equals(firstId));
	}

	@Test
	public final void testGetItemIds()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		List<?> entityIds = container.getItemIds(0, 2);
		assertTrue(entityIds.size() == 2);

		entityIds = container.getItemIds(0, container.size());
		assertTrue(entityIds.contains(entityId));

		final boolean removed = container.removeItem(entityId);
		assertTrue(removed); 
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemAtInt()
	{
		container.addItemAt(0);
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemAtIntObject()
	{
		container.addItemAt(0, 1234);
	}
}
