package com.vaadin.data.hbnutil.tests;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

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
	private static HbnContainer<SampleNode> nodeContainer = null;
	private static int recordsToLoad = 10;

	
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
		HibernateUtil.insertExampleData(recordsToLoad);
		//HibernateUtil.insertExampleNodes(recordsToLoad);
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
		nodeContainer = new HbnContainer<SampleNode>(SampleNode.class, sessionFactory);
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
		nodeContainer = null;
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
		
		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for GetItemIds
	 * {@link com.vaadin.data.hbnutil.HbnContainer#getItemIds()}.
	 */
	@Test
	public final void testGetItemIds()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);
		
		Collection<?> entityIds = typeContainer.getItemIds();
		assertTrue(entityIds.contains(entityId));

		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for GetItemIdsIntInt
	 * {@link com.vaadin.data.hbnutil.HbnContainer#getItemIds(int, int)}.
	 */
	@Test
	public final void testGetItemIdsIntInt()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);
		
		List<?> entityIds = typeContainer.getItemIds(0, 2);
		assertTrue(entityIds.size() == 2);

		entityIds = typeContainer.getItemIds(0, typeContainer.size());
		assertTrue(entityIds.contains(entityId));

		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getType(java.lang.Object)}.
	 */
	@Test
	public final void testGetType()
	{
		Class<?> propertyType = typeContainer.getType("kilometers");
		assertTrue(propertyType.equals(Float.class));

		propertyType = typeContainer.getType("title");
		assertTrue(propertyType.equals(String.class));

		propertyType = typeContainer.getType("date");
		assertTrue(propertyType.equals(Date.class));
	}

	/**
	 * Test method for RemoveAllItems
	 * {@link com.vaadin.data.hbnutil.HbnContainer#removeAllItems()}.
	 */
	@Test
	public final void testRemoveAllItems()
	{
		workoutContainer.removeAllItems();
		assertTrue(workoutContainer.size() == 0);
	
		typeContainer.removeAllItems();
		assertTrue(typeContainer.size() == 0);

		HibernateUtil.insertExampleTypes();
		HibernateUtil.insertExampleData(recordsToLoad);
	}

	/**
	 * Test method for RemoveContainerProperty
	 * {@link com.vaadin.data.hbnutil.HbnContainer#removeContainerProperty(java.lang.Object)}.
	 */
	@Test
	public final void testRemoveContainerProperty()
	{
		typeContainer.addContainerProperty("qq", String.class, "");
		assertTrue(typeContainer.getContainerPropertyIds().contains("qq"));

		typeContainer.removeContainerProperty("qq");
		assertTrue(!typeContainer.getContainerPropertyIds().contains("qq"));

		typeContainer.addContainerProperty("ww", Integer.class, 1);
		assertTrue(typeContainer.getContainerPropertyIds().contains("ww"));

		typeContainer.removeContainerProperty("ww");
		assertTrue(!typeContainer.getContainerPropertyIds().contains("ww"));

		typeContainer.addContainerProperty("ee", Long.class, 1);
		assertTrue(typeContainer.getContainerPropertyIds().contains("ee"));

		typeContainer.removeContainerProperty("ee");
		assertTrue(!typeContainer.getContainerPropertyIds().contains("ee"));
		
		typeContainer.addContainerProperty("rr", Float.class, 1.0);
		assertTrue(typeContainer.getContainerPropertyIds().contains("rr"));

		typeContainer.removeContainerProperty("rr");
		assertTrue(!typeContainer.getContainerPropertyIds().contains("rr"));
		
		typeContainer.addContainerProperty("tt", Double.class, 1.0);
		assertTrue(typeContainer.getContainerPropertyIds().contains("tt"));

		typeContainer.removeContainerProperty("tt");
		assertTrue(!typeContainer.getContainerPropertyIds().contains("tt"));
		
		typeContainer.addContainerProperty("yy", Boolean.class, true);
		assertTrue(typeContainer.getContainerPropertyIds().contains("yy"));

		typeContainer.removeContainerProperty("yy");
		assertTrue(!typeContainer.getContainerPropertyIds().contains("yy"));
	}

	/**
	 * Test method for RemoveItem
	 * {@link com.vaadin.data.hbnutil.HbnContainer#removeItem(java.lang.Object)}.
	 */
	@Test
	public final void testRemoveItem()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);
		
		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 

		Collection<?> entityIds = typeContainer.getItemIds();
		assertTrue(!entityIds.contains(entityId));
	}

	/**
	 * Test method for Size
	 * {@link com.vaadin.data.hbnutil.HbnContainer#size()}.
	 */
	@Test
	public final void testSize()
	{
		workoutContainer.removeAllItems();
		assertTrue(workoutContainer.size() == 0);
	
		typeContainer.removeAllItems();
		assertTrue(typeContainer.size() == 0);

		HibernateUtil.insertExampleTypes();
		HibernateUtil.insertExampleData(recordsToLoad);
		assertTrue(workoutContainer.size() == recordsToLoad);
	}

	/**
	 * Test method for AddItemAfterObject
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addItemAfter(java.lang.Object)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemAfterObject()
	{
		final Object entityId = typeContainer.firstItemId();
		typeContainer.addItemAfter(entityId); // should be unsupported
	}

	/**
	 * Test method for AddItemAfterObjectObject
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addItemAfter(java.lang.Object, java.lang.Object)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemAfterObjectObject()
	{
		final Object entityId = typeContainer.firstItemId();
		typeContainer.addItemAfter(entityId, 12345); // should be unsupported
	}

	/**
	 * Test method for FirstItemId
	 * {@link com.vaadin.data.hbnutil.HbnContainer#firstItemId()}.
	 */
	@Test
	public final void testFirstItemId()
	{
		workoutContainer.removeAllItems();
		typeContainer.removeAllItems();

		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);
		
		final Object entityId2 = typeContainer.addItem();
		assertNotNull(entityId2);
		
		assertTrue(typeContainer.firstItemId().equals(entityId));
		
		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 

		assertTrue(typeContainer.firstItemId().equals(entityId2));

		workoutContainer.removeAllItems();
		assertTrue(workoutContainer.size() == 0);

		typeContainer.removeAllItems();
		assertTrue(workoutContainer.size() == 0);

		HibernateUtil.insertExampleTypes();
		HibernateUtil.insertExampleData(recordsToLoad);
		assertTrue(workoutContainer.size() == recordsToLoad);
	}

	/**
	 * Test method for IsFirstId
	 * {@link com.vaadin.data.hbnutil.HbnContainer#isFirstId(java.lang.Object)}.
	 */
	@Test
	public final void testIsFirstId()
	{
		workoutContainer.removeAllItems();
		typeContainer.removeAllItems();

		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);
		
		final Object entityId2 = typeContainer.addItem();
		assertNotNull(entityId2);
		
		assertTrue(typeContainer.isFirstId(entityId));
		
		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 

		assertTrue(typeContainer.isFirstId(entityId2));

		workoutContainer.removeAllItems();
		assertTrue(workoutContainer.size() == 0);

		typeContainer.removeAllItems();
		assertTrue(workoutContainer.size() == 0);

		HibernateUtil.insertExampleTypes();
		HibernateUtil.insertExampleData(recordsToLoad);
		assertTrue(workoutContainer.size() == recordsToLoad);
	}

	/**
	 * Test method for IsLastId
	 * {@link com.vaadin.data.hbnutil.HbnContainer#isLastId(java.lang.Object)}.
	 */
	@Test
	public final void testIsLastId()
	{
		workoutContainer.removeAllItems();
		typeContainer.removeAllItems();

		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);
		
		final Object entityId2 = typeContainer.addItem();
		assertNotNull(entityId2);
		
		assertTrue(typeContainer.isLastId(entityId2));
		
		final boolean removed = typeContainer.removeItem(entityId2);
		assertTrue(removed); 

		assertTrue(typeContainer.isLastId(entityId));

		workoutContainer.removeAllItems();
		assertTrue(workoutContainer.size() == 0);

		typeContainer.removeAllItems();
		assertTrue(workoutContainer.size() == 0);

		HibernateUtil.insertExampleTypes();
		HibernateUtil.insertExampleData(recordsToLoad);
		assertTrue(workoutContainer.size() == recordsToLoad);
	}

	/**
	 * Test method for LastItemId
	 * {@link com.vaadin.data.hbnutil.HbnContainer#lastItemId()}.
	 */
	@Test
	public final void testLastItemId()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);
		
		final Object lastId = typeContainer.lastItemId();
		assertTrue(entityId.equals(lastId));
		assertTrue(typeContainer.isLastId(lastId));
		assertTrue(typeContainer.isLastId(entityId));

		final boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 
	}

	/**
	 * Test method for NextItemId
	 * {@link com.vaadin.data.hbnutil.HbnContainer#nextItemId(java.lang.Object)}.
	 */
	@Test
	public final void testNextItemId()
	{
		final Object entityId = typeContainer.addItem();
		assertNotNull(entityId);
		
		final Object entityId2 = typeContainer.addItem();
		assertNotNull(entityId2);
		
		assertTrue(typeContainer.nextItemId(entityId).equals(entityId2));
		
		boolean removed = typeContainer.removeItem(entityId);
		assertTrue(removed); 

		removed = typeContainer.removeItem(entityId2);
		assertTrue(removed); 
	}

	/**
	 * Test method for PrevItemId
	 * {@link com.vaadin.data.hbnutil.HbnContainer#prevItemId(java.lang.Object)}.
	 */
	@Test
	public final void testPrevItemId()
	{
		final Object firstId = typeContainer.firstItemId();
		assertNotNull(firstId);
		
		final Object secondId = typeContainer.nextItemId(firstId);
		assertNotNull(secondId);
	
		assertTrue(typeContainer.prevItemId(secondId).equals(firstId));
	}

	/**
	 * Test method for AddItemAtInt
	 * {@link com.vaadin.data.hbnutil.HbnContainer#addItemAt(int)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemAtInt()
	{
		typeContainer.addItemAt(0);
		// expecting an UnsupportedOperationException
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#addItemAt(int, java.lang.Object)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemAtIntObject()
	{
		typeContainer.addItemAt(0, 1234);
		// expecting an UnsupportedOperationException
	}

	/**
	 * Test method for {@link com.vaadin.data.hbnutil.HbnContainer#getIdByIndex(int)}.
	 */
	@Test
	public final void testGetIdByIndex()
	{
		final Object entityId = typeContainer.getIdByIndex(0);
		assertNotNull(entityId);
		
		final Object firstId = typeContainer.firstItemId();
		assertNotNull(firstId);
	
		assertTrue(entityId.equals(firstId));
	}

	/**
	 * Test method for IndexOfId
	 * {@link com.vaadin.data.hbnutil.HbnContainer#indexOfId(java.lang.Object)}.
	 */
	@Test
	public final void testIndexOfId()
	{
		final Object entityId = typeContainer.firstItemId();
		assertNotNull(entityId);
		
		assertTrue(typeContainer.indexOfId(entityId) == 0);
	}

	/**
	 * Test method for GetSortableContainerPropertyIds
	 * {@link com.vaadin.data.hbnutil.HbnContainer#getSortableContainerPropertyIds()}.
	 */
	@Test
	public final void testGetSortableContainerPropertyIds()
	{
		Collection<?> propertyIds = typeContainer.getSortableContainerPropertyIds();
		assertTrue(propertyIds.contains("title"));
		assertTrue(propertyIds.contains("date"));
		assertTrue(propertyIds.contains("kilometers"));
	}

	/**
	 * Test method for GetChildren
	 * {@link com.vaadin.data.hbnutil.HbnContainer#getChildren(java.lang.Object)}.
	 */
	@Test
	public final void testGetChildren()
	{
		HibernateUtil.insertExampleNodes(10);
		
		Collection<?> rootEntityIds = nodeContainer.rootItemIds();
		assertTrue(rootEntityIds.size() == 1);
		
		for (Object rootId : rootEntityIds)
		{
			Collection<?> entityIds = nodeContainer.getChildren(rootId);
			assertTrue(entityIds.size() == 9);

			for (Object entityId : entityIds)
			{
				Object parentId = nodeContainer.getParent(entityId);
				assertEquals(parentId, rootId);
			}
		}
		
		nodeContainer.removeAllItems();
	}

	/**
	 * Test method for GetParent
	 * {@link com.vaadin.data.hbnutil.HbnContainer#getParent(java.lang.Object)}.
	 */
	@Test
	public final void testGetParent()
	{
		final Session session = sessionFactory.getCurrentSession();

		final SampleNode rootNode = new SampleNode();
		rootNode.setParent(null);
		session.save(rootNode);

		final SampleNode sampleNode = new SampleNode();
		sampleNode.setParent(rootNode);
		session.save(sampleNode);
		
		final Object parentId = nodeContainer.getParent(sampleNode.getId());
		assertEquals(parentId, rootNode.getId());
		
		nodeContainer.removeAllItems();
	}

	/**
	 * Test method for RootItemIds
	 * {@link com.vaadin.data.hbnutil.HbnContainer#rootItemIds()}.
	 */
	@Test
	public final void testRootItemIds()
	{
		final Session session = sessionFactory.getCurrentSession();

		final SampleNode rootNode = new SampleNode();
		rootNode.setParent(null);
		session.save(rootNode);

		final SampleNode sampleNode = new SampleNode();
		sampleNode.setParent(rootNode);
		session.save(sampleNode);
		
		Collection<?> rootEntityIds = nodeContainer.rootItemIds();
		assertTrue(rootEntityIds.size() == 1);
		assertTrue(rootEntityIds.contains(rootNode.getId()));
		
		nodeContainer.removeAllItems();
	}

	/**
	 * Test method for SetParent
	 * {@link com.vaadin.data.hbnutil.HbnContainer#setParent(java.lang.Object, java.lang.Object)}.
	 */
	@Test
	public final void testSetParent()
	{
		final Session session = sessionFactory.getCurrentSession();

		final SampleNode rootNode = new SampleNode();
		rootNode.setParent(null);
		session.save(rootNode);

		final SampleNode sampleNode = new SampleNode();
		sampleNode.setParent(null);
		session.save(sampleNode);
		
		Collection<?> rootEntityIds = nodeContainer.rootItemIds();
		assertTrue(rootEntityIds.size() == 2);
		assertTrue(rootEntityIds.contains(rootNode.getId()));
		assertTrue(rootEntityIds.contains(sampleNode.getId()));
		
		nodeContainer.setParent(sampleNode.getId(), rootNode.getId());
		rootEntityIds = nodeContainer.rootItemIds();
		assertTrue(rootEntityIds.size() == 1);
		assertTrue(rootEntityIds.contains(rootNode.getId()));
		assertTrue(!rootEntityIds.contains(sampleNode.getId()));
		
		nodeContainer.removeAllItems();
	}

	/**
	 * Test method for AreChildrenAllowed
	 * {@link com.vaadin.data.hbnutil.HbnContainer#areChildrenAllowed(java.lang.Object)}.
	 */
	@Test
	public final void testAreChildrenAllowed()
	{
		final Session session = sessionFactory.getCurrentSession();

		final SampleNode rootNode = new SampleNode();
		rootNode.setParent(null);
		session.save(rootNode);

		boolean childrenAllowed = nodeContainer.areChildrenAllowed(rootNode.getId());
		assertTrue(childrenAllowed);

		nodeContainer.removeAllItems();
	}

	/**
	 * Test method for SetChildrenAllowed
	 * {@link com.vaadin.data.hbnutil.HbnContainer#setChildrenAllowed(java.lang.Object, boolean)}.
	 */
	@Test
	public final void testSetChildrenAllowed()
	{
		final Session session = sessionFactory.getCurrentSession();

		final SampleNode rootNode = new SampleNode();
		rootNode.setParent(null);
		session.save(rootNode);

		boolean childrenAllowed = nodeContainer.setChildrenAllowed(rootNode.getId(), true);
		assertTrue(!childrenAllowed);

		childrenAllowed = nodeContainer.setChildrenAllowed(rootNode.getId(), false);
		assertTrue(!childrenAllowed);

		nodeContainer.removeAllItems();
	}

	/**
	 * Test method for IsRoot
	 * {@link com.vaadin.data.hbnutil.HbnContainer#isRoot(java.lang.Object)}.
	 */
	@Test
	public final void testIsRoot()
	{
		final Session session = sessionFactory.getCurrentSession();

		final SampleNode rootNode = new SampleNode();
		rootNode.setParent(null);
		session.save(rootNode);

		final SampleNode sampleNode = new SampleNode();
		sampleNode.setParent(rootNode);
		session.save(sampleNode);

		boolean isRootNode = nodeContainer.isRoot(rootNode.getId());
		assertTrue(isRootNode);

		isRootNode = nodeContainer.isRoot(sampleNode.getId());
		assertTrue(!isRootNode);

		nodeContainer.removeAllItems();
	}

	/**
	 * Test method for HasChildren
	 * {@link com.vaadin.data.hbnutil.HbnContainer#hasChildren(java.lang.Object)}.
	 */
	@Test
	public final void testHasChildren()
	{
		final Session session = sessionFactory.getCurrentSession();

		final SampleNode rootNode = new SampleNode();
		rootNode.setParent(null);
		session.save(rootNode);

		final SampleNode sampleNode = new SampleNode();
		sampleNode.setParent(rootNode);
		session.save(sampleNode);

		boolean hasChildren = nodeContainer.hasChildren(rootNode.getId());
		assertTrue(hasChildren);

		hasChildren = nodeContainer.hasChildren(sampleNode.getId());
		assertTrue(!hasChildren);

		nodeContainer.removeAllItems();
	}
}
