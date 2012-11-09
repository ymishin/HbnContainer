package com.vaadin.data.hbnutil.tests;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.hibernate.*;
import org.junit.*;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.hbnutil.*;
import com.vaadin.data.util.filter.SimpleStringFilter;

public class HbnContainerTests
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
	public final void testSaveEntity()
	{
		SampleNode entity = new SampleNode();
		entity.setTitle("zzz");
		
		final Object entityId = container.saveEntity(entity);
		assertNotNull(entityId);
		entity = null;

		entity = container.getItem(entityId).getPojo();
		assertTrue(entity.getTitle() == "zzz");
		
		final boolean removed = container.removeItem(entityId);
		assertTrue(removed); 
	}

	@Test
	public final void testUpdateEntity()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);

		SampleNode entity = (SampleNode) container.getItem(entityId).getPojo();
		entity.setTitle("zzz");

		container.updateEntity(entity);
		entity = null;

		entity = container.getItem(entityId).getPojo();
		assertTrue(entity.getTitle() == "zzz");
		
		container.sort(new Object[] { "id" }, new boolean[] { true });
		
		entity = container.getItem(entityId).getPojo();
		assertTrue(entity.getTitle() == "zzz");
		
		final boolean removed = container.removeItem(entityId);
		assertTrue(removed); 
	}

	@Test
	public final void testLoadItem()
	{
		final Object entityId = container.addItem();
		assertNotNull(entityId);

		SampleNode entity = container.getItem(entityId).getPojo();
		entity.setTitle("zzz");

		container.updateEntity(entity);
		entity = null;

		entity = container.getItem(entityId).getPojo();
		assertTrue(entity.getTitle() == "zzz");
		
		final boolean removed = container.removeItem(entityId);
		assertTrue(removed); 
	}
	
	@Test
	public final void testAddItemSetChangeListener()
	{
		@SuppressWarnings("serial")
		final ItemSetChangeListener listener = new ItemSetChangeListener()
		{
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event)
			{
			}
		};
		
		container.addItemSetChangeListener(listener);
		
		List<ItemSetChangeListener> listeners = container.getItemSetChangeListeners();
		assertTrue(listeners.contains(listener));
		
		container.removeItemSetChangeListener(listener);
	}

	@Test
	public final void testRemoveItemSetChangeListener()
	{
		@SuppressWarnings("serial")
		final ItemSetChangeListener listener = new ItemSetChangeListener()
		{
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event)
			{
			}
		};

		container.addItemSetChangeListener(listener);
		container.removeItemSetChangeListener(listener);

		List<ItemSetChangeListener> listeners = container.getItemSetChangeListeners();
		assertTrue(!listeners.contains(listener));
	}

	@Test
	public final void testAddListener()
	{
		@SuppressWarnings("serial")
		final ItemSetChangeListener listener = new ItemSetChangeListener()
		{
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event)
			{
			}
		};
		
		container.addListener(listener);
		
		List<ItemSetChangeListener> listeners = container.getItemSetChangeListeners();
		assertTrue(listeners.contains(listener));
		
		container.removeListener(listener);
	}

	@Test
	public final void testRemoveListener()
	{
		@SuppressWarnings("serial")
		final ItemSetChangeListener listener = new ItemSetChangeListener()
		{
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event)
			{
			}
		};
		
		container.addListener(listener);
		container.removeListener(listener);

		List<ItemSetChangeListener> listeners = container.getItemSetChangeListeners();
		assertTrue(!listeners.contains(listener));
	}

	@Test
	public final void testAddContainerFilter()
	{
		final ContainerFilter filter = new StringContainerFilter("title", "abc", true, false);
		container.addContainerFilter(filter);
		
		Set<ContainerFilter> filters = container.getContainerFilters();
		assertTrue(filters.contains(filter));
		
		container.removeAllContainerFilters();
	}

	@Test
	public final void testRemoveContainerFilters()
	{
		final ContainerFilter filter = new StringContainerFilter("title", "abc", true, false);
		container.addContainerFilter(filter);
		
		Set<ContainerFilter> filters = container.getContainerFilters();
		assertTrue(filters.contains(filter));
		
		container.removeContainerFilters("title");
		
		filters = container.getContainerFilters();
		assertTrue(!filters.contains(filter));
	}

	@Test
	public final void testAddFilter()
	{
		final Filter filter = new SimpleStringFilter("title", "abc", true, false);
		container.addContainerFilter(filter);
		
		Set<ContainerFilter> filters = container.getContainerFilters();
		assertTrue(filters.size() == 1); // lame
		
		container.removeAllContainerFilters();
		filters = container.getContainerFilters();
		assertTrue(filters == null);
	}
}
