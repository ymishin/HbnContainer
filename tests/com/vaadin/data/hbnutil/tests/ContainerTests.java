package com.vaadin.data.hbnutil.tests;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Date;

import org.hibernate.*;
import org.junit.*;

import com.vaadin.data.Property;
import com.vaadin.data.hbnutil.*;

public class ContainerTests
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
	public final void testGetItem()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);

		SampleNode entity = (SampleNode) container.getItem(entityId).getPojo();
		entity.setTitle("zzz");

		container.updateEntity(entity);
		entity = null;

		entity = (SampleNode) container.getItem(entityId).getPojo();
		assertTrue(entity.getTitle() == "zzz");
	}

	@Test
	public final void testGetContainerPropertyIds()
	{
		final Collection<String> propertyIds = container.getContainerPropertyIds();
		assertTrue(propertyIds.contains("title"));
		assertTrue(propertyIds.contains("created"));
	}

	@Test
	public final void testGetItemIds()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		Collection<?> entityIds = container.getItemIds();
		assertTrue(entityIds.contains(entityId));
	}

	@Test
	public final void testGetContainerProperty()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);

		SampleNode entity = (SampleNode) container.getItem(entityId).getPojo();
		entity.setTitle("zzz");

		container.updateEntity(entity);
		
		Property property = container.getContainerProperty(entityId, "title");
		final String propertyValue = (String) property.getValue();
		assertTrue(propertyValue == "zzz");
	}

	@Test
	public final void testGetType()
	{
		Class<?> propertyType = container.getType("title");
		assertTrue(propertyType.equals(String.class));

		propertyType = container.getType("created");
		assertTrue(propertyType.equals(Date.class));
	}

	@Test
	public final void testSize()
	{
		container.removeAllItems();
		assertTrue(container.size() == 0);
	
		HibernateUtil.insertExampleNodes(recordsToLoad);
		assertTrue(container.size() == recordsToLoad);
	}

	@Test
	public final void testContainsId()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		final boolean containsId = container.containsId(entityId);
		assertTrue(containsId); 
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void testAddItemObject()
	{
		final Object entityId = 12345;
		container.addItem(entityId);
	}

	@Test
	public final void testAddItem()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);

		final boolean containsId = container.containsId(entityId);
		assertTrue(containsId); 
	}

	@Test
	public final void testRemoveItem()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);
		
		final boolean removed = container.removeItem(entityId);
		assertTrue(removed); 

		Collection<?> entityIds = container.getItemIds();
		assertTrue(!entityIds.contains(entityId));
	}

	@Test
	public final void testAddContainerProperty()
	{
		container.addContainerProperty("qq", String.class, "");
		assertTrue(container.getContainerPropertyIds().contains("qq"));

		container.addContainerProperty("ww", Integer.class, 1);
		assertTrue(container.getContainerPropertyIds().contains("ww"));

		container.addContainerProperty("ee", Long.class, 1);
		assertTrue(container.getContainerPropertyIds().contains("ee"));

		container.addContainerProperty("rr", Float.class, 1.0);
		assertTrue(container.getContainerPropertyIds().contains("rr"));

		container.addContainerProperty("tt", Double.class, 1.0);
		assertTrue(container.getContainerPropertyIds().contains("tt"));

		container.addContainerProperty("yy", Boolean.class, true);
		assertTrue(container.getContainerPropertyIds().contains("yy"));
	}

	@Test
	public final void testRemoveContainerProperty()
	{
		container.addContainerProperty("qq", String.class, "");
		assertTrue(container.getContainerPropertyIds().contains("qq"));

		container.removeContainerProperty("qq");
		assertTrue(!container.getContainerPropertyIds().contains("qq"));

		container.addContainerProperty("ww", Integer.class, 1);
		assertTrue(container.getContainerPropertyIds().contains("ww"));

		container.removeContainerProperty("ww");
		assertTrue(!container.getContainerPropertyIds().contains("ww"));

		container.addContainerProperty("ee", Long.class, 1);
		assertTrue(container.getContainerPropertyIds().contains("ee"));

		container.removeContainerProperty("ee");
		assertTrue(!container.getContainerPropertyIds().contains("ee"));
		
		container.addContainerProperty("rr", Float.class, 1.0);
		assertTrue(container.getContainerPropertyIds().contains("rr"));

		container.removeContainerProperty("rr");
		assertTrue(!container.getContainerPropertyIds().contains("rr"));
		
		container.addContainerProperty("tt", Double.class, 1.0);
		assertTrue(container.getContainerPropertyIds().contains("tt"));

		container.removeContainerProperty("tt");
		assertTrue(!container.getContainerPropertyIds().contains("tt"));
		
		container.addContainerProperty("yy", Boolean.class, true);
		assertTrue(container.getContainerPropertyIds().contains("yy"));

		container.removeContainerProperty("yy");
		assertTrue(!container.getContainerPropertyIds().contains("yy"));
	}

	@Test
	public final void testRemoveAllItems()
	{
		container.removeAllItems();
		assertTrue(container.size() == 0);
	
		HibernateUtil.insertExampleNodes(recordsToLoad);
		assertTrue(container.size() == recordsToLoad);

		container.removeAllItems();
		assertTrue(container.size() == 0);
	}
}
