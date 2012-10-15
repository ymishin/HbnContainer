package com.vaadin.data.hbnutil.tests;

import static org.junit.Assert.*;

import java.util.Collection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vaadin.data.Property;
import com.vaadin.data.hbnutil.HbnContainer;

public class HbnContainerTests
{
	private static SessionFactory sessionFactory = null;
	private static Session session = null;
	private static HbnContainer<Type> typeContainer = null;
	private static HbnContainer<Workout> workoutContainer = null;

	
	/**
	 * Will execute the method once, before the start of all tests. This can be used to perform time intensive
	 * activities, for example to connect to a database.
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		sessionFactory = HibernateUtil.getSessionFactory();
		session = sessionFactory.openSession();
		
		HibernateUtil.insertExampleTypes();
		HibernateUtil.insertExampleData(10);
	}

	/**
	 * Will execute the method once, after all tests have finished. This can be used to perform clean-up activities, for
	 * example to disconnect from a database.
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		final Transaction transaction = session.getTransaction();
		
		if (transaction.isActive())
			transaction.commit();

		if (session != null)
			session.close();

		if (sessionFactory != null)
			sessionFactory.close();
	}

	/**
	 * Will execute the method before each test. This method can prepare the test environment (e.g. read input data,
	 * initialize the class).
	 */
	@Before
	public void setUp() throws Exception
	{
		typeContainer = new HbnContainer<Type>(Type.class, sessionFactory);
		workoutContainer = new HbnContainer<Workout>(Workout.class, sessionFactory);
	}

	/**
	 * Will execute the method after each test. This method can cleanup the test environment (e.g. delete temporary
	 * data, restore defaults).
	 */
	@After
	public void tearDown() throws Exception
	{
		typeContainer = null;
		workoutContainer = null;
	}

	//
	// ************************************************************************************************************
	//
	
	/**
	 * @Test
	 * @Ignore
	 * @Test(timeout=100)
	 * @Test (expected = Exception.class)
	 * 
	 * fail(String)
	 * assertTrue(boolean)
	 * assertTrue([message], boolean condition)
	 * assertsEquals([String message], expected, actual)
	 * assertsEquals([String message], expected, actual, tolerance)
	 * assertNull([message], object)
	 * assertNotNull([message], object)
	 * assertSame([String], expected, actual)
	 * assertNotSame([String], expected, actual)
	 */

	/**
	 * Test method for HbnContainer constructor
	 * {@link com.vaadin.data.hbnutil.HbnContainer#HbnContainer(java.lang.Class, org.hibernate.SessionFactory)}.
	 */
	@Test
	public final void testHbnContainer()
	{
		assertNotNull(typeContainer); // not null... pass!
		assertNotNull(workoutContainer); // not null... pass!
	}

	/**
	 * Test method for AddContainerProperty
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addContainerProperty(java.lang.Object, java.lang.Class, java.lang.Object)}
	 */
	@Test
	public final void testAddContainerProperty()
	{
		typeContainer.addContainerProperty("qq", String.class, "");
		assertTrue(typeContainer.getContainerPropertyIds().contains("qq"));

		typeContainer.addContainerProperty("ww", Integer.class, 1);
		assertTrue(typeContainer.getContainerPropertyIds().contains("ww"));

		typeContainer.addContainerProperty("ee", Long.class, 1);
		assertTrue(typeContainer.getContainerPropertyIds().contains("ee"));

		typeContainer.addContainerProperty("rr", Float.class, 1.0);
		assertTrue(typeContainer.getContainerPropertyIds().contains("rr"));

		typeContainer.addContainerProperty("tt", Double.class, 1.0);
		assertTrue(typeContainer.getContainerPropertyIds().contains("tt"));

		typeContainer.addContainerProperty("yy", Boolean.class, true);
		assertTrue(typeContainer.getContainerPropertyIds().contains("yy"));
	}

	/**
	 * Test method for SaveEntity
	 * {@link com.vaadin.data.hbnutil.HbnContainer#saveEntity(java.lang.Object)}.
	 */
	@Test
	public final void testSaveEntity()
	{
		Type entity = new Type();
		entity.setTitle("zzz");
		
		final Object entityId = typeContainer.saveEntity(entity);
		assertNotNull(entityId);

		entity = null;
		entity = typeContainer.getItem(entityId).getPojo();
		assertTrue(entity.getTitle() == "zzz");
		
		// Not really necessary but we want to cleanup so we may as well check the result.
		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for UpdateEntity
	 * {@link com.vaadin.data.hbnutil.HbnContainer#updateEntity(java.lang.Object)}.
	 */
	@Test
	public final void testUpdateEntity()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);

		Type entity = (Type) typeContainer.getItem(entityId).getPojo();
		entity.setTitle("zzz");

		typeContainer.updateEntity(entity);

		entity = null;
		entity = (Type) typeContainer.getItem(entityId).getPojo();
		assertTrue(entity.getTitle() == "zzz");
		
		// Not really necessary but we want to cleanup so we may as well check the result.
		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for AddItem
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addItem()}.
	 */
	@Test
	public final void testAddItem()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);

		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for AddItemObject
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addItem(java.lang.Object)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemObject()
	{
		final Object entityId = 12345;
		typeContainer.addItem(entityId);
	}

	/**
	 * Test method for ContainsId
	 * {@link com.vaadin.data.hbnutil.HbnContainer#containsId(java.lang.Object)}.
	 */
	@Test
	public final void testContainsId()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);
		
		final boolean containsId = typeContainer.containsId(entityId);
		assertTrue(containsId); 

		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for GetContainerProperty
	 * {@link com.vaadin.data.hbnutil.HbnContainer#getContainerProperty(java.lang.Object, java.lang.Object)}.
	 */
	@Test
	public final void testGetContainerProperty()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);

		Type entity = (Type) typeContainer.getItem(entityId).getPojo();
		entity.setTitle("zzz");

		typeContainer.updateEntity(entity);
		
		Property<?> property = typeContainer.getContainerProperty(entityId, "title");
		final String propertyValue = (String) property.getValue();
		assertTrue(propertyValue == "zzz");
		
		// Not really necessary but we want to cleanup so we may as well check the result.
		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for GetContainerPropertyIds
	 * {@link com.vaadin.data.hbnutil.HbnContainer#getContainerPropertyIds()}.
	 */
	@Test
	public final void testGetContainerPropertyIds()
	{
		final Collection<String> propertyIds = typeContainer.getContainerPropertyIds();
		assertTrue(propertyIds.contains("title"));
		assertTrue(propertyIds.contains("date"));
		assertTrue(propertyIds.contains("kilometers"));
	}

	/**
	 * Test method for GetItem
	 * {@link com.vaadin.data.hbnutil.HbnContainer#getItem(java.lang.Object)}.
	 */
	@Test
	public final void testGetItem()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);

		Type entity = (Type) typeContainer.getItem(entityId).getPojo();
		entity.setTitle("zzz");

		typeContainer.updateEntity(entity);

		entity = null;
		entity = (Type) typeContainer.getItem(entityId).getPojo();
		assertTrue(entity.getTitle() == "zzz");
		
		// Not really necessary but we want to cleanup so we may as well check the result.
		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for LoadItem. This is identical to GetItem because GetItem uses loadItem.
	 * {@link com.vaadin.data.hbnutil.HbnContainer#loadEntity(java.io.Serializable)}.
	 */
	@Test
	public final void testLoadItem()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);

		Type entity = (Type) typeContainer.getItem(entityId).getPojo();
		entity.setTitle("zzz");

		typeContainer.updateEntity(entity);

		entity = null;
		entity = (Type) typeContainer.getItem(entityId).getPojo();
		assertTrue(entity.getTitle() == "zzz");
		
		// Not really necessary but we want to cleanup so we may as well check the result.
		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getItemIds()}.
	 */
	@Test
	public final void testGetItemIds()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getItemIds(int, int)}.
	 */
	@Test
	public final void testGetItemIdsIntInt()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getType(java.lang.Object)}.
	 */
	@Test
	public final void testGetType()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#removeAllItems()}.
	 */
	@Test
	public final void testRemoveAllItems()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#removeContainerProperty(java.lang.Object)}.
	 */
	@Test
	public final void testRemoveContainerProperty()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#removeItem(java.lang.Object)}.
	 */
	@Test
	public final void testRemoveItem()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addListener(com.vaadin.data.Container.ItemSetChangeListener)}.
	 */
	@Test
	public final void testAddListener()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link com.vaadin.data.hbnutil.HbnContainer#removeListener(com.vaadin.data.Container.ItemSetChangeListener)}.
	 */
	@Test
	public final void testRemoveListener()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#size()}.
	 */
	@Test
	public final void testSize()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#addItemAfter(java.lang.Object)}.
	 */
	@Test
	public final void testAddItemAfterObject()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#addItemAfter(java.lang.Object, java.lang.Object)}.
	 */
	@Test
	public final void testAddItemAfterObjectObject()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getOrder(boolean)}.
	 */
	@Test
	public final void testGetOrder()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getDefaultOrder(boolean)}.
	 */
	@Test
	public final void testGetDefaultOrder()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getBaseCriteria()}.
	 */
	@Test
	public final void testGetBaseCriteria()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getNaturalOrder(boolean)}.
	 */
	@Test
	public final void testGetNaturalOrder()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#firstItemId()}.
	 */
	@Test
	public final void testFirstItemId()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#firstItemId(boolean)}.
	 */
	@Test
	public final void testFirstItemIdBoolean()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#isFirstId(java.lang.Object)}.
	 */
	@Test
	public final void testIsFirstId()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#isLastId(java.lang.Object)}.
	 */
	@Test
	public final void testIsLastId()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#lastItemId()}.
	 */
	@Test
	public final void testLastItemId()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#nextItemId(java.lang.Object)}.
	 */
	@Test
	public final void testNextItemId()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#prevItemId(java.lang.Object)}.
	 */
	@Test
	public final void testPrevItemId()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#addItemAt(int)}.
	 */
	@Test
	public final void testAddItemAtInt()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#addItemAt(int, java.lang.Object)}.
	 */
	@Test
	public final void testAddItemAtIntObject()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getIdByIndex(int)}.
	 */
	@Test
	public final void testGetIdByIndex()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#indexOfId(java.lang.Object)}.
	 */
	@Test
	public final void testIndexOfId()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getSortableContainerPropertyIds()}.
	 */
	@Test
	public final void testGetSortableContainerPropertyIds()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#sort(java.lang.Object[], boolean[])}.
	 */
	@Test
	public final void testSort()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#clearInternalCache()}.
	 */
	@Test
	public final void testClearInternalCache()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addContainerFilter(java.lang.Object, java.lang.String, boolean, boolean)}
	 * .
	 */
	@Test
	public final void testAddContainerFilterObjectStringBooleanBoolean()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addContainerFilter(com.vaadin.data.hbnutil.ContainerFilter)}.
	 */
	@Test
	public final void testAddContainerFilterContainerFilter()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#removeAllContainerFilters()}.
	 */
	@Test
	public final void testRemoveAllContainerFilters()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#removeContainerFilters(java.lang.Object)}.
	 */
	@Test
	public final void testRemoveContainerFilters()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#addContainerFilter(com.vaadin.data.Container.Filter)}
	 * .
	 */
	@Test
	public final void testAddContainerFilterFilter()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link com.vaadin.data.hbnutil.HbnContainer#removeContainerFilter(com.vaadin.data.Container.Filter)}.
	 */
	@Test
	public final void testRemoveContainerFilter()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getChildren(java.lang.Object)}.
	 */
	@Test
	public final void testGetChildren()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getParent(java.lang.Object)}.
	 */
	@Test
	public final void testGetParent()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#rootItemIds()}.
	 */
	@Test
	public final void testRootItemIds()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#setParent(java.lang.Object, java.lang.Object)}.
	 */
	@Test
	public final void testSetParent()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#areChildrenAllowed(java.lang.Object)}.
	 */
	@Test
	public final void testAreChildrenAllowed()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#setChildrenAllowed(java.lang.Object, boolean)}.
	 */
	@Test
	public final void testSetChildrenAllowed()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#isRoot(java.lang.Object)}.
	 */
	@Test
	public final void testIsRoot()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#hasChildren(java.lang.Object)}.
	 */
	@Test
	public final void testHasChildren()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addItemSetChangeListener(com.vaadin.data.Container.ItemSetChangeListener)}
	 * .
	 */
	@Test
	public final void testAddItemSetChangeListener()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link com.vaadin.data.hbnutil.HbnContainer#removeItemSetChangeListener(com.vaadin.data.Container.ItemSetChangeListener)}
	 * .
	 */
	@Test
	public final void testRemoveItemSetChangeListener()
	{
		fail("Not yet implemented"); // TODO
	}

}
