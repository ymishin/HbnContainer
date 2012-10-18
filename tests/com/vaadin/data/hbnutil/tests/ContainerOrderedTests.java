package com.vaadin.data.hbnutil.tests;

import static org.junit.Assert.*;

import org.hibernate.*;
import org.junit.*;
import com.vaadin.data.hbnutil.*;

public class ContainerOrderedTests
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
		container.removeAllItems();
	}

	//
	// ************************************************************************************************************
	//
	
	@Test
	public final void testNextItemId()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		final Object entityId2 = container.addItem();
		assertNotNull(entityId2);
		
		assertTrue(container.nextItemId(entityId).equals(entityId2));
	}
	
	@Test
	public final void testPrevItemId()
	{
		container.addItem();
		container.addItem();
		container.addItem();
		
		@SuppressWarnings("unused")
		Object[] itemIds = container.getItemIds().toArray();

		Object firstId = container.firstItemId();
		assertNotNull(firstId);
		
		Object secondId = container.nextItemId(firstId);
		assertNotNull(secondId);
		
		Object thirdId = container.nextItemId(secondId);
		assertNotNull(thirdId);
		
		Object prevId = container.prevItemId(firstId);
		assertTrue(prevId == null);
		
		prevId = container.prevItemId(secondId);
		assertTrue(prevId.equals(firstId));
		
		prevId = container.prevItemId(thirdId);
		assertTrue(prevId.equals(secondId));
	}
	
	@Test
	public final void testFirstItemId()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		final Object entityId2 = container.addItem();
		assertNotNull(entityId2);
		assertTrue(container.firstItemId().equals(entityId));
		
		final boolean removed = container.removeItem(entityId);
		assertTrue(removed); 
		assertTrue(container.firstItemId().equals(entityId2));
	}
	
	@Test
	public final void testLastItemId()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		final Object lastId = container.lastItemId();
		assertTrue(entityId.equals(lastId));
		assertTrue(container.isLastId(lastId));
		assertTrue(container.isLastId(entityId));
	}
	
	@Test
	public final void testIsFirstId()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		final Object entityId2 = container.addItem();
		assertNotNull(entityId2);
		assertTrue(container.isFirstId(entityId));
		
		final boolean removed = container.removeItem(entityId);
		assertTrue(removed); 
		assertTrue(container.isFirstId(entityId2));
	}
	
	@Test
	public final void testIsLastId()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		final Object entityId2 = container.addItem();
		assertNotNull(entityId2);
		assertTrue(container.isLastId(entityId2));
		
		final boolean removed = container.removeItem(entityId2);
		assertTrue(removed); 
		assertTrue(container.isLastId(entityId));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemAfterObject()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		final Object newEntityId = container.firstItemId();
		container.addItemAfter(newEntityId); // should be unsupported
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemAfterObjectObject()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		final Object newEntityId = container.firstItemId();
		container.addItemAfter(newEntityId, 12345); // should be unsupported
	} 
}
