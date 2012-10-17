package com.vaadin.data.hbnutil.tests;

import static org.junit.Assert.*;

import java.util.Collection;

import org.hibernate.*;
import org.junit.*;

import com.vaadin.data.hbnutil.*;

public class ContainerHierarchicalTests
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
	public final void testGetChildren()
	{
		HibernateUtil.insertExampleNodes(recordsToLoad);
		
		Collection<?> rootEntityIds = container.rootItemIds();
		assertTrue(rootEntityIds.size() == 1);
		
		for (Object rootId : rootEntityIds)
		{
			Collection<?> entityIds = container.getChildren(rootId);
			assertTrue(entityIds.size() == recordsToLoad - 1);

			for (Object entityId : entityIds)
			{
				Object parentId = container.getParent(entityId);
				assertEquals(parentId, rootId);
			}
		}
	}

	@Test
	public final void testGetParent()
	{
		final Object rootNodeId = container.addItem();
		final SampleNode rootNode = container.getItem(rootNodeId).getPojo();
		rootNode.setParent(null);
		session.update(rootNode);

		final Object sampleNodeId = container.addItem();
		final SampleNode sampleNode = container.getItem(sampleNodeId).getPojo();
		sampleNode.setParent(rootNode);
		session.update(sampleNode);
		
		final Object parentId = container.getParent(sampleNode.getId());
		assertEquals(parentId, rootNode.getId());
	}

	@Test
	public final void testRootItemIds()
	{
		final Object rootNodeId = container.addItem();
		final SampleNode rootNode = container.getItem(rootNodeId).getPojo();
		rootNode.setParent(null);
		session.update(rootNode);

		final Object sampleNodeId = container.addItem();
		final SampleNode sampleNode = container.getItem(sampleNodeId).getPojo();
		sampleNode.setParent(rootNode);
		session.update(sampleNode);
		
		Collection<?> rootEntityIds = container.rootItemIds();
		assertTrue(rootEntityIds.size() == 1);
		assertTrue(rootEntityIds.contains(rootNode.getId()));
	}

	@Test
	public final void testSetParent()
	{
		final Object rootNodeId = container.addItem();
		final SampleNode rootNode = container.getItem(rootNodeId).getPojo();
		rootNode.setParent(null);
		session.update(rootNode);

		final Object sampleNodeId = container.addItem();
		final SampleNode sampleNode = container.getItem(sampleNodeId).getPojo();
		sampleNode.setParent(null);
		session.update(sampleNode);
		
		Collection<?> rootEntityIds = container.rootItemIds();
		assertTrue(rootEntityIds.size() == 2);
		assertTrue(rootEntityIds.contains(rootNode.getId()));
		assertTrue(rootEntityIds.contains(sampleNode.getId()));
		
		container.setParent(sampleNode.getId(), rootNode.getId());
		rootEntityIds = container.rootItemIds();
		assertTrue(rootEntityIds.size() == 1);
		assertTrue(rootEntityIds.contains(rootNode.getId()));
		assertTrue(!rootEntityIds.contains(sampleNode.getId()));
	}

	@Test
	public final void testAreChildrenAllowed()
	{
		final Object rootNodeId = container.addItem();
		final SampleNode rootNode = container.getItem(rootNodeId).getPojo();
		rootNode.setParent(null);
		session.update(rootNode);

		boolean childrenAllowed = container.areChildrenAllowed(rootNode.getId());
		assertTrue(childrenAllowed);
	}

	@Test
	public final void testSetChildrenAllowed()
	{
		final Object rootNodeId = container.addItem();
		final SampleNode rootNode = container.getItem(rootNodeId).getPojo();
		rootNode.setParent(null);
		session.update(rootNode);

		boolean childrenAllowed = container.setChildrenAllowed(rootNode.getId(), true);
		assertTrue(!childrenAllowed);

		childrenAllowed = container.setChildrenAllowed(rootNode.getId(), false);
		assertTrue(!childrenAllowed);
	}

	@Test
	public final void testIsRoot()
	{
		final Object rootNodeId = container.addItem();
		final SampleNode rootNode = container.getItem(rootNodeId).getPojo();
		rootNode.setParent(null);
		session.update(rootNode);

		final Object sampleNodeId = container.addItem();
		final SampleNode sampleNode = container.getItem(sampleNodeId).getPojo();
		sampleNode.setParent(rootNode);
		session.update(sampleNode);

		boolean isRootNode = container.isRoot(rootNode.getId());
		assertTrue(isRootNode);

		isRootNode = container.isRoot(sampleNode.getId());
		assertTrue(!isRootNode);
	}

	@Test
	public final void testHasChildren()
	{
		final Object rootNodeId = container.addItem();
		final SampleNode rootNode = container.getItem(rootNodeId).getPojo();
		rootNode.setParent(null);
		session.update(rootNode);

		final Object sampleNodeId = container.addItem();
		final SampleNode sampleNode = container.getItem(sampleNodeId).getPojo();
		sampleNode.setParent(rootNode);
		session.update(sampleNode);

		boolean hasChildren = container.hasChildren(rootNode.getId());
		assertTrue(hasChildren);

		hasChildren = container.hasChildren(sampleNode.getId());
		assertTrue(!hasChildren);
	}
}
