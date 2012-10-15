/*
 * Copyright 2012, Gary Piercey, All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.vaadin.data.hbnutil;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.hbnutil.HbnContainer.EntityItem.EntityItemProperty;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.UnsupportedFilterException;

/**
 * Lazy, almost full-featured, general purpose Hibernate entity container. Makes lots of queries, but shouldn't consume
 * too much memory.
 * <p>
 * HbnContainer is developed and tested with session-per-request pattern, but should work with other session handling
 * mechanisms too. To abstract away session handling, user must provide an implementation of SessionManager interface,
 * via HbnContainer fetches reference to Session instance whenever it needs it. Session returned by HbnContainer is
 * expected to be open.
 * 
 * <p>
 * In in its constructor it will only need entity class type (Pojo) and a SessionManager.
 * <p>
 * HbnContainer also expects that identifiers are auto generated. This matters only if HbnContainer is used to create
 * new entities.
 * <p>
 * Note, container caches size, firstId, lastId to be much faster with large datasets. TODO make this caching optional,
 * actually should trust on Hibernates and DB engines query caches.
 * 
 * <p>
 * See http://vaadin.com/wiki/-/wiki/Main/Using%20Hibernate%20with%20Vaadin?
 * p_r_p_185834411_title=Using%20Hibernate%20with%20Vaadin for a working example application.
 */
public class HbnContainer<T> implements Container, Container.Indexed, Container.Sortable,
		Container.Filterable, Container.Hierarchical, Container.ItemSetChangeNotifier, Container.Ordered
{
	private static final int REFERENCE_CLEANUP_INTERVAL = 2000;
	private static final long serialVersionUID = -6410337120924382057L;
	private Logger logger = LoggerFactory.getLogger(HbnContainer.class);

	/**
	 * Item wrappping a Hibernate mapped entity object. EntityItems are generally instantiated automatically by
	 * HbnContainer.
	 */
	@SuppressWarnings("hiding")
	public class EntityItem<T> implements Item
	{

		private static final long serialVersionUID = -2847179724504965599L;

		/**
		 * Reference to hibernate mapped entity that this Item wraps.
		 */
		protected T pojo;

		/**
		 * Instantiated properties of this EntityItem. May be either EntityItemProperty (hibernate field) or manually
		 * added container property (MethodProperty).
		 */
		protected Map<Object, Property<?>> properties = new HashMap<Object, Property<?>>();

		@SuppressWarnings("unchecked")
		public EntityItem(Serializable id)
		{
			pojo = (T) sessionFactory.getCurrentSession().get(entityType, id);
			// add non-hibernate mapped container properties
			for (String propertyId : addedProperties.keySet())
			{
				addItemProperty(propertyId, new MethodProperty<Object>(pojo, propertyId));
			}
		}

		/**
		 * @return the wrapped entity object.
		 */
		public T getPojo()
		{
			return pojo;
		}

		@SuppressWarnings("rawtypes")
		public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException
		{
			properties.put(id, property);
			return true;
		}

		public Property<?> getItemProperty(Object id)
		{
			Property<?> p = properties.get(id);
			if (p == null)
			{
				p = new EntityItemProperty(id.toString());
				properties.put(id, p);
			}
			return p;
		}

		public Collection<?> getItemPropertyIds()
		{
			return getContainerPropertyIds();
		}

		public boolean removeItemProperty(Object id) throws UnsupportedOperationException
		{
			Property<?> removed = properties.remove(id);
			return removed != null;
		}

		/**
		 * EntityItemProperty wraps one Hibernate controlled field of the pojo used by EntityItem. For common fields the
		 * field value is the same as Property value. For relation fields it is the identifier of related object or a
		 * collection of identifiers.
		 */
		@SuppressWarnings("rawtypes")
		public class EntityItemProperty implements Property, Property.ValueChangeNotifier
		{

			private static final long serialVersionUID = -4086774943938055297L;
			private final String propertyName;

			public EntityItemProperty(String propertyName)
			{
				this.propertyName = propertyName;
			}

			public EntityItem<T> getEntityItem()
			{
				return EntityItem.this;
			}

			public T getPojo()
			{
				return pojo;
			}

			/**
			 * A helper method to get raw type of this (Hibernate) property.
			 * 
			 * @return the raw type of field
			 */
			private Type getPropertyType()
			{
				return classMetadata.getPropertyType(propertyName);
			}

			private boolean propertyInEmbeddedKey()
			{
				// TODO a place for optimization, this is not needed to be done
				// for each separate property
				Type idType = classMetadata.getIdentifierType();
				if (idType.isComponentType())
				{
					ComponentType idComponent = (ComponentType) idType;
					String[] idPropertyNames = idComponent.getPropertyNames();
					List<String> idPropertyNameList = Arrays.asList(idPropertyNames);
					if (idPropertyNameList.contains(propertyName))
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				else
				{
					return false;
				}
			}

			public Class<?> getType()
			{
				// TODO clean, optimize, review

				if (propertyInEmbeddedKey())
				{
					ComponentType idType = (ComponentType) classMetadata.getIdentifierType();
					String[] propertyNames = idType.getPropertyNames();
					for (int i = 0; i < propertyNames.length; i++)
					{
						String name = propertyNames[i];
						if (name.equals(propertyName))
						{
							try
							{
								String idName = classMetadata.getIdentifierPropertyName();
								Field idField = entityType.getDeclaredField(idName);
								Field propertyField = idField.getType().getDeclaredField(propertyName);
								return propertyField.getType();
							}
							catch (NoSuchFieldException ex)
							{
								throw new RuntimeException("Could not find the type of specified container property.",
										ex);
							}
						}
					}
				}

				Type propertyType = getPropertyType();
				if (propertyType.isCollectionType())
				{
					/*
					 * For collection types the Property value is the same type of collection, but containing
					 * identifiers instead of the actual referenced objects.
					 */
					Class<?> returnedClass = propertyType.getReturnedClass();
					return returnedClass;
				}
				else if (propertyType.isAssociationType())
				{
					/*
					 * For association the the property value type is the type of referenced types identifier. Use
					 * Hibernates ClassMetadata for referenced type and get the type of its identifier.
					 */
					// TODO clean, optimize, review, this could be optimized
					// among similar properties
					ClassMetadata classMetadata2 = sessionFactory.getClassMetadata(classMetadata.getPropertyType(
							propertyName).getReturnedClass());
					return classMetadata2.getIdentifierType().getReturnedClass();

				}
				else
				{
					/*
					 * For basic fields the Property type is the same as the type in entity class.
					 */
					return classMetadata.getPropertyType(propertyName).getReturnedClass();
				}
			}

			@SuppressWarnings("unchecked")
			public Object getValue()
			{

				// TODO clean, optimize, review

				// Ensure we have an attached pojo
				if (!sessionFactory.getCurrentSession().contains(pojo))
				{
					pojo = (T) sessionFactory.getCurrentSession().get(entityType, (Serializable) getIdForPojo(pojo));
				}

				if (propertyInEmbeddedKey())
				{
					ComponentType idType = (ComponentType) classMetadata.getIdentifierType();
					String[] propertyNames = idType.getPropertyNames();
					for (int i = 0; i < propertyNames.length; i++)
					{
						String name = propertyNames[i];
						if (name.equals(propertyName))
						{
							Object id = classMetadata.getIdentifier(pojo,
									(SessionImplementor) sessionFactory.getCurrentSession());
							return idType.getPropertyValue(id, i, EntityMode.POJO);
						}
					}
				}

				Type propertyType = getPropertyType();
				Object propertyValue = classMetadata.getPropertyValue(pojo, propertyName);

				if (!propertyType.isAssociationType())
				{
					return propertyValue;
				}
				else if (propertyType.isCollectionType())
				{
					if (propertyValue == null)
					{
						return null;
					}

					/*
					 * Build a HashSet of identifiers of entities stored in collection.
					 */
					HashSet<Serializable> identifiers = new HashSet<Serializable>();
					Collection<?> pojos = (Collection<?>) propertyValue;

					for (Object object : pojos)
					{
						// here, object must be of an association type
						if (!sessionFactory.getCurrentSession().contains(object))
						{
							// ensure a fresh object if session contains the
							// object
							object = sessionFactory.getCurrentSession().merge(object);
						}
						identifiers.add(sessionFactory.getCurrentSession().getIdentifier(object));
					}
					return identifiers;
				}
				else
				{
					/*
					 * the return value will be the identifier of referenced object
					 */
					if (propertyValue == null)
					{
						return null;
					}
					Class<?> propertyTypeClass = propertyType.getReturnedClass();

					ClassMetadata classMetadata2 = sessionFactory.getClassMetadata(propertyTypeClass);

					Serializable identifier = classMetadata2.getIdentifier(propertyValue,
							(SessionImplementor) sessionFactory.getCurrentSession());
					return identifier;
				}
			}

			public boolean isReadOnly()
			{
				// TODO
				return false;
			}

			public void setReadOnly(boolean newStatus)
			{
				throw new UnsupportedOperationException();
			}

			public void setValue(Object newValue) throws ReadOnlyException, ConversionException
			{

				try
				{
					Object value;
					try
					{
						if (newValue == null || getType().isAssignableFrom(newValue.getClass()))
						{
							value = newValue;
						}
						else
						{
							// Gets the string constructor
							final Constructor<?> constr = getType().getConstructor(new Class[] { String.class });

							value = constr.newInstance(new Object[] { newValue.toString() });
						}

						// TODO same optimizations (caching introspection of
						// types) as in getType and getValue
						// could be done here.

						if (propertyInEmbeddedKey())
						{
							ComponentType idType = (ComponentType) classMetadata.getIdentifierType();
							String[] propertyNames = idType.getPropertyNames();
							for (int i = 0; i < propertyNames.length; i++)
							{
								String name = propertyNames[i];
								if (name.equals(propertyName))
								{
									Object id = classMetadata.getIdentifier(pojo,
											(SessionImplementor) sessionFactory.getCurrentSession());
									Object[] values = idType.getPropertyValues(id, EntityMode.POJO);
									values[i] = value;
									idType.setPropertyValues(id, values, EntityMode.POJO);
								}
							}
						}
						else
						{
							Type propertyType = classMetadata.getPropertyType(propertyName);
							if (propertyType.isCollectionType())
							{
								/*
								 * Value is a collection of identifiers of referenced objects.
								 */
								// TODO figure out how to fetch mapped type
								// properly
								Field declaredField = entityType.getDeclaredField(propertyName);
								java.lang.reflect.Type genericType = declaredField.getGenericType();
								java.lang.reflect.Type[] actualTypeArguments = ((ParameterizedType) genericType)
										.getActualTypeArguments();

								java.lang.reflect.Type assosiatedType = actualTypeArguments[0];
								String typestring = assosiatedType.toString().substring(6);

								/*
								 * Reuse existing persistent collection if possible so Hibernate may optimize queries
								 * properly.
								 */
								@SuppressWarnings("unchecked")
								Collection<Object> pojoCollection = (Collection<Object>) classMetadata
										.getPropertyValue(pojo, propertyName);
								if (pojoCollection == null)
								{
									pojoCollection = new HashSet<Object>();
									classMetadata.setPropertyValue(pojo, propertyName, pojoCollection);
								}
								// copy existing set, so we can track which are
								// to be removed
								Collection<Object> orphans = new HashSet<Object>(pojoCollection);

								Collection<?> identifiers = (Collection<?>) value;
								Session session = sessionFactory.getCurrentSession();
								// add missing objects
								for (Object id : identifiers)
								{
									Object object = session.get(typestring, (Serializable) id);
									if (!pojoCollection.contains(object))
									{
										pojoCollection.add(object);
									}
									else
									{
										orphans.remove(object);
									}
								}
								// remove the ones that are no more supposed to
								// be in collection
								pojoCollection.removeAll(orphans);

							}
							else if (propertyType.isAssociationType())
							{
								/*
								 * Property value is identifier, convert to the referenced type
								 */
								Class<?> referencedType = classMetadata.getPropertyType(propertyName)
										.getReturnedClass();
								Object object = sessionFactory.getCurrentSession().get(referencedType,
										(Serializable) value);
								classMetadata.setPropertyValue(pojo, propertyName, object);
								// TODO check if these are needed
								sessionFactory.getCurrentSession().merge(object);
								sessionFactory.getCurrentSession().saveOrUpdate(pojo);

							}
							else
							{
								classMetadata.setPropertyValue(pojo, propertyName, value);
							}
						}
						// Persist (possibly) detached pojo
						@SuppressWarnings("unchecked")
						T newPojo = (T) sessionFactory.getCurrentSession().merge(pojo);
						pojo = newPojo;

						fireValueChange();

					}
					catch (final java.lang.Exception e)
					{
						e.printStackTrace();
						throw new ConversionException(e);
					}

				}
				catch (HibernateException e)
				{
					e.printStackTrace();
				}
			}

			@Override
			public String toString()
			{
				Object v = getValue();
				if (v != null)
				{
					return v.toString();
				}
				else
				{
					return null;
				}
			}

			private class HbnPropertyValueChangeEvent implements Property.ValueChangeEvent
			{
				private static final long serialVersionUID = 166764621324404579L;

				public Property<?> getProperty()
				{
					return EntityItemProperty.this;
				}
			}

			private List<ValueChangeListener> valueChangeListeners;

			private void fireValueChange()
			{
				if (valueChangeListeners != null)
				{
					HbnPropertyValueChangeEvent event = new HbnPropertyValueChangeEvent();
					Object[] array = valueChangeListeners.toArray();
					for (int i = 0; i < array.length; i++)
					{
						((ValueChangeListener) array[i]).valueChange(event);
					}
				}
			}

			public void addListener(ValueChangeListener listener)
			{
				if (valueChangeListeners == null)
				{
					valueChangeListeners = new LinkedList<ValueChangeListener>();
				}
				if (!valueChangeListeners.contains(listener))
				{
					valueChangeListeners.add(listener);
				}
			}

			public void removeListener(ValueChangeListener listener)
			{
				if (valueChangeListeners != null)
				{
					valueChangeListeners.remove(listener);
				}
			}

			@Override
			public void addValueChangeListener(ValueChangeListener listener)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void removeValueChangeListener(ValueChangeListener listener)
			{
				// TODO Auto-generated method stub

			}

		}
	}

	private static final int ROW_BUF_SIZE = 100;
	private static final int ID_TO_INDEX_MAX_SIZE = 300;

	/** Entity class that will be listed in container */
	protected Class<T> entityType;
	private ClassMetadata classMetadata;

	/** internal flag used to temporarily invert order of listing */
	private boolean normalOrder = true;

	/** Row buffer of pojos, used to optimize query count when iterating forward */
	private List<T> ascRowBuffer;
	/**
	 * Row buffer of pojos, used to optimize query count when iterating backward
	 */

	private List<T> descRowBuffer;
	/** cached last item identifier */
	private Object lastId;
	/** cached first item identifier */
	private Object firstId;

	/**
	 * Row buffer of pojos, used to optimize query count when container is accessed with indexes
	 */
	private List<T> indexRowBuffer;

	/** Container wide index of the first entity in indexRowBuffer */
	private int indexRowBufferFirstIndex;

	/**
	 * Map from entity/item identifiers to index. Maps does not contain mapping for all identifiers in container, but
	 * only those that are recently loaded. Map gets cleanded during usage, to free memory.
	 */
	private final Map<Object, Integer> idToIndex = new LinkedHashMap<Object, Integer>();

	/**
	 * whether sorts are made ascending or descending, see {@link #orderPropertyIds}
	 */
	private boolean[] orderAscendings;
	/**
	 * Properties among container is sorted by. Used to implement Container.Sortable
	 */
	private Object[] orderPropertyIds;

	/** Cached size of container. Used to optimize query count. */
	private Integer size;

	private LinkedList<ItemSetChangeListener> itemSetChangeListeners;

	/**
	 * Contains current filters which has been applied to this container. Used to implement Container.Filterable.
	 */
	private HashSet<ContainerFilter> filters;

	/** Caches weak references to items, in order to conserve memory. */
	private final HashMap<Object, WeakReference<EntityItem<T>>> entityCache = new HashMap<Object, WeakReference<EntityItem<T>>>();

	/** A map of added javabean property names to their respective types */
	private final Map<String, Class<?>> addedProperties = new HashMap<String, Class<?>>();

	/**
	 * counter for items loaded by container, used to implement cleanup of weakreferences
	 */
	private int loadCount;

	private SessionFactory sessionFactory;

	
	
	
	/**
	 * Constructor creates a new instance of HbnContainer.
	 */
	public HbnContainer(Class<T> entityType, SessionFactory sessionFactory)
	{
		this.entityType = entityType;
		this.sessionFactory = sessionFactory;
		this.classMetadata = sessionFactory.getClassMetadata(entityType);
	}

	/**
	 * This is an internal utility method that takes an exception, extracts the stack trace and creates a string from
	 * the stack trace.
	 */
	private String StackToString(Throwable exception)
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

	/**
	 * Adds a new Property to all Items in the Container. The Property ID, data type and default value of the new
	 * Property are given as parameters. This functionality is optional.
	 * 
	 * HbnContainer automatically adds all fields that are mapped by Hibernate to the database. With this method
	 * we can add a bean property to the container that is contained in the pojo but not hibernate-mapped.
	 */
	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> classType, Object defaultValue)
			throws UnsupportedOperationException
	{
		boolean propertyExists = true;

		try
		{
			// TODO: this is a little hackish, try to find a cleaner way.
			// Determine if the property exists and if not, determine if it can be created.
			new MethodProperty<Object>(this.entityType.newInstance(), propertyId.toString());
		}
		catch (Exception e)
		{
			logger.debug("Note: this is not an error: " + StackToString(e));
			propertyExists = false;
		}

		addedProperties.put(propertyId.toString(), classType);
		return propertyExists;
	}

	/**
	 * This is an HbnContainer specific method to persist a newly created entity. This method will trigger a cache clear
	 * and will also fire an item set change event.
	 */
	public Serializable saveEntity(T entity)
	{
		final Session session = sessionFactory.getCurrentSession();
		final Serializable entityId = session.save(entity);

		clearInternalCache();
		fireItemSetChange();

		return entityId;
	}

	/**
	 * This is an HbnContainer specific method to update an entity. This method takes care of updating the cache
	 * appropriately as well as firing value change events when necessary.
	 */
	public Serializable updateEntity(T entity)
	{
		final Session session = sessionFactory.getCurrentSession();
		session.update(entity);

		EntityItem<T> cachedEntity = null;
		final Serializable entityId = (Serializable) getIdForPojo(entity);

		if (entityCache != null) // Refresh the entity cache
		{
			final WeakReference<EntityItem<T>> weakReference = entityCache.get(entityId);

			if (weakReference != null)
			{
				cachedEntity = weakReference.get();

				if (cachedEntity != null) // May be already collected but not cleaned
					cachedEntity.pojo = entity;
			}
		}

		if (cachedEntity != null) // If it was in cache it might be rendered
		{
			for (Object propertyId : cachedEntity.getItemPropertyIds())
			{
				// fire change events on this item, properties might have changed
				final Property<?> property = cachedEntity.getItemProperty(propertyId);

				if (property instanceof EntityItem.EntityItemProperty)
				{
					@SuppressWarnings("rawtypes")
					final EntityItemProperty entityProperty = (EntityItemProperty) property;
					entityProperty.fireValueChange();
				}
			}
		}

		return entityId;
	}

	/**
	 * Creates a new Item into the Container and assigns it an automatic ID. The new ID is returned, or null if the
	 * operation fails. After a successful call you can use the getItemmethod to fetch the Item. This functionality is
	 * optional.
	 */
	@Override
	public Object addItem() throws UnsupportedOperationException
	{
		try
		{
			final Object entity = entityType.newInstance();
			final Session session = sessionFactory.getCurrentSession();
			final Object entityId = session.save(entity);
			
			clearInternalCache();
			fireItemSetChange();

			return entityId;
		}
		catch (Exception e)
		{
			logger.error(StackToString(e));
			return null;
		}
	}

	/**
	 * Creates a new Item with the given ID in the Container. The new Item is returned, and it is ready to have its
	 * Properties modified. Returns null if the operation fails or the Container already contains a Item with the given
	 * ID. This functionality is optional.
	 * 
	 * Note that in this implementation we are expecting auto-generated identifiers so this method is not implemented.
	 */
	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests if the Container contains the specified Item. Filtering can hide items so that they will not be visible
	 * through the container API, and this method should respect visibility of items (i.e. only indicate visible items
	 * as being in the container) if feasible for the container.
	 */
	@Override
	public boolean containsId(Object entityId)
	{
		try
		{
			final Session session = sessionFactory.getCurrentSession();
			final Object entity = session.get(entityType, (Serializable) entityId);
			return (entity != null);
		}
		catch (Exception e)
		{
			logger.debug(StackToString(e)); // not an error
			return false;
		}
	}

	/**
	 * Gets the Property identified by the given itemId and propertyId from the Container. If the Container does not
	 * contain the item or it is filtered out, or the Container does not have the Property, null is returned.
	 */
	@Override
	public Property<?> getContainerProperty(Object entityId, Object propertyId)
	{
		try
		{
			EntityItem<?> entity = getItem(entityId);
			Property<?> property = entity.getItemProperty(propertyId);
			return property;
		}
		catch (Exception e)
		{
			logger.debug(StackToString(e)); // not an error
			return null;
		}
	}

	/**
	 * Gets the ID's of all Properties stored in the Container. The ID's cannot be modified through the returned
	 * collection.
	 */
	@Override
	public Collection<String> getContainerPropertyIds()
	{
		Collection<String> propertyIds = getSortableContainerPropertyIds();
		propertyIds.addAll(addedProperties.keySet());
		return propertyIds;
	}

	/**
	 * This is an HbnContainer specific utility method that is used to retrieve the list of embedded property key
	 * identifiers.
	 */
	private Collection<String> getEmbeddedKeyPropertyIds()
	{
		final ArrayList<String> embeddedKeyPropertyIds = new ArrayList<String>();
		final Type identifierType = classMetadata.getIdentifierType();

		if (identifierType.isComponentType())
		{
			final ComponentType idComponent = (ComponentType) identifierType;
			final String[] propertyNameArray = idComponent.getPropertyNames();

			if (propertyNameArray != null)
			{
				final List<String> propertyNames = Arrays.asList(propertyNameArray);
				embeddedKeyPropertyIds.addAll(propertyNames);
			}
		}
		
		return embeddedKeyPropertyIds;
	}

	/**
	 * Gets the Item with the given Item ID from the Container. If the Container does not contain the requested Item,
	 * null is returned. Containers should not return Items that are filtered out.
	 */
	@Override
	public EntityItem<T> getItem(Object entityId)
	{
		return loadEntity((Serializable)entityId);
	}

	/**
	 * This is an HbnContainer specific utility method and it is used to fetch Items by id. Override this if you need to
	 * customize your query for EntityItems.
	 */
	protected EntityItem<T> loadEntity(Serializable entityId)
	{
		if (entityId == null)
			return null;
		
		cleanCache();

		EntityItem<T> entity;
		final WeakReference<EntityItem<T>> weakReference = entityCache.get(entityId);

		if (weakReference != null)
		{
			entity = weakReference.get();

			if (entity != null)
				return entity;
		}

		entity = new EntityItem<T>(entityId);
		entityCache.put(entityId, new WeakReference<EntityItem<T>>(entity));

		return entity;
	}

	/**
	 * Cleans the itemCache of collected item references. This method run every now and then by
	 * {@link #loadEntity(Serializable)}, but may be run manually too.
	 * 
	 * <p>
	 * TODO figure out if this is the best possible way to free the memory consumed by (empty) weak references and open
	 * this mechanism for extension
	 * 
	 */
	private void cleanCache()
	{
		if (++loadCount % REFERENCE_CLEANUP_INTERVAL == 0)
		{
			Set<Entry<Object, WeakReference<EntityItem<T>>>> entries = entityCache.entrySet();
			for (Iterator<Entry<Object, WeakReference<EntityItem<T>>>> iterator = entries.iterator(); iterator
					.hasNext();)
			{
				Entry<Object, WeakReference<EntityItem<T>>> entry = iterator.next();
				if (entry.getValue().get() == null)
				{
					// if the referenced entityitem is carbage collected, remove
					// the weak reference itself
					iterator.remove();
				}
			}
		}
	}

	public Collection<?> getItemIds()
	{
		/*
		 * Create an optimized query to return only identifiers. Note that this method does not scale well for large
		 * database. At least Table is optimized so that it does not call this method.
		 */
		Criteria crit = getCriteria();
		crit.setProjection(Projections.id());
		return crit.list();
	}

	@Override
	public List<?> getItemIds(int startIndex, int numberOfItems)
	{
		List<?> itemIds = (List<?>) this.getItemIds();
		return itemIds.subList(startIndex, startIndex + numberOfItems);
	}

	public Class<?> getType(Object propertyId)
	{
		/*
		 * This method does pretty much the same thing as EntityItemProperty#getType()
		 * 
		 * TODO refactor to use same code, will also fix incomplete implementation of this method (for assosiation
		 * types). Not critical as componets don't really rely on this methods.
		 */
		if (addedProperties.keySet().contains(propertyId))
		{
			return addedProperties.get(propertyId);
		}
		else if (propertyInEmbeddedKey(propertyId))
		{
			ComponentType idType = (ComponentType) classMetadata.getIdentifierType();
			String[] propertyNames = idType.getPropertyNames();
			for (int i = 0; i < propertyNames.length; i++)
			{
				String name = propertyNames[i];
				if (name.equals(propertyId))
				{
					String idName = classMetadata.getIdentifierPropertyName();
					try
					{
						Field idField = entityType.getDeclaredField(idName);
						Field propertyField = idField.getType().getDeclaredField((String) propertyId);
						return propertyField.getType();
					}
					catch (NoSuchFieldException ex)
					{
						throw new RuntimeException("Could not find the type of specified container property.", ex);
					}
				}
			}
		}
		Type propertyType = classMetadata.getPropertyType(propertyId.toString());
		return propertyType.getReturnedClass();
	}

	/**
	 * TODO combine with the very same method from EntityItemProperty
	 * 
	 * @param propertyId
	 * @return true if property is part of embedded key of entity
	 */
	private boolean propertyInEmbeddedKey(Object propertyId)
	{
		Type idType = classMetadata.getIdentifierType();
		if (idType.isComponentType())
		{
			ComponentType idComponent = (ComponentType) idType;
			String[] idPropertyNames = idComponent.getPropertyNames();
			List<String> idPropertyNameList = Arrays.asList(idPropertyNames);
			if (idPropertyNameList.contains(propertyId))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	public boolean removeAllItems() throws UnsupportedOperationException
	{
		// TODO
		return false;
	}

	/* Remove a container property added with addContainerProperty() */
	public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException
	{
		boolean propertyExisted = false;
		Class<?> removed = addedProperties.remove(propertyId);
		if (removed != null)
		{
			propertyExisted = true;
		}
		return propertyExisted;
	}

	public boolean removeItem(Object itemId) throws UnsupportedOperationException
	{
		for (Object id : getChildren(itemId))
			removeItem(id);

		Session session = this.sessionFactory.getCurrentSession();
		Object id = session.load(entityType, (Serializable) itemId);

		this.sessionFactory.getCurrentSession().delete(id);

		clearInternalCache();
		fireItemSetChange();

		return true;
	}

	public void addListener(ItemSetChangeListener listener)
	{
		if (itemSetChangeListeners == null)
		{
			itemSetChangeListeners = new LinkedList<ItemSetChangeListener>();
		}
		itemSetChangeListeners.add(listener);
	}

	public void removeListener(ItemSetChangeListener listener)
	{
		if (itemSetChangeListeners != null)
		{
			itemSetChangeListeners.remove(listener);
		}

	}

	private void fireItemSetChange()
	{
		if (itemSetChangeListeners != null)
		{
			final Object[] l = itemSetChangeListeners.toArray();
			final Container.ItemSetChangeEvent event = new Container.ItemSetChangeEvent()
			{
				private static final long serialVersionUID = -3002746333251784195L;

				public Container getContainer()
				{
					return HbnContainer.this;
				}
			};
			for (int i = 0; i < l.length; i++)
			{
				((ItemSetChangeListener) l[i]).containerItemSetChange(event);
			}
		}
	}

	public int size()
	{
		if (size == null)
		{
			/*
			 * If cached size does not exist, query from database
			 */
			size = ((Number) getBaseCriteria().setProjection(Projections.rowCount()).uniqueResult()).intValue();
		}
		return size.intValue();
	}

	public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException
	{
		// can't implement properly for database backed container like this
		throw new UnsupportedOperationException();
	}

	public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException
	{
		// can't implement properly for database backed container like this
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets a base listing using current orders etc.
	 * 
	 * @return criteria with current Order criterias added
	 */
	private Criteria getCriteria()
	{
		Criteria criteria = getBaseCriteria();
		List<Order> orders = getOrder(!normalOrder);
		for (Order order : orders)
		{
			criteria.addOrder(order);
		}
		return criteria;
	}

	/**
	 * Return the ordering criteria in the order in which they should be applied. The composed order must be stable and
	 * must include {@link #getNaturalOrder(boolean)} at the end.
	 * 
	 * @param flipOrder reverse the order if true
	 * @return List<Order> orders to apply, first item has the highest priority
	 */
	protected final List<Order> getOrder(boolean flipOrder)
	{
		List<Order> orders = new ArrayList<Order>();

		// standard sort order set by the user by property
		orders.addAll(getDefaultOrder(flipOrder));

		// natural order
		orders.add(getNaturalOrder(flipOrder));

		return orders;
	}

	/**
	 * Returns the ordering to use for the container contents. The default implementation provides the
	 * {@link Container.Sortable} functionality.
	 * 
	 * Can be overridden to customize item sort order.
	 * 
	 * @param flipOrder reverse the order if true
	 * @return List<Order> orders to apply, first item has the highest priority
	 */
	protected List<Order> getDefaultOrder(boolean flipOrder)
	{
		List<Order> orders = new ArrayList<Order>();
		if (orderPropertyIds != null)
		{
			for (int i = 0; i < orderPropertyIds.length; i++)
			{
				String orderPropertyId = orderPropertyIds[i].toString();
				if (propertyInEmbeddedKey(orderPropertyId))
				{
					String idName = classMetadata.getIdentifierPropertyName();
					orderPropertyId = idName + "." + orderPropertyId;
				}
				boolean a = flipOrder ? !orderAscendings[i] : orderAscendings[i];
				if (a)
				{
					orders.add(Order.asc(orderPropertyId));
				}
				else
				{
					orders.add(Order.desc(orderPropertyId));
				}
			}
		}
		return orders;
	}

	/**
	 * This method is meant to be called by HbnContainer itself. It will create the base criteria for entity class and
	 * add possible restrictions to query. This method is protected so developers can add their own custom criterias.
	 * 
	 * @return
	 */
	protected Criteria getBaseCriteria()
	{
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(entityType);
		// if container is filtered via Container.Filterable API
		if (filters != null)
		{
			for (ContainerFilter filter : filters)
			{
				// convert ContainerFilters to hibernate Restriction Criterias
				String idName = null;
				if (propertyInEmbeddedKey(filter.getPropertyId()))
				{
					idName = classMetadata.getIdentifierPropertyName();
				}
				criteria = criteria.add(filter.getCriterion(idName));
			}
		}
		return criteria;
	}

	/**
	 * Natural order is the order in which the database is sorted if container has no other ordering set. Natural order
	 * is always added as least significant order to queries. This is needed to keep items stable order across queries.
	 * <p>
	 * The default implementation sorts entities by identifier column.
	 * 
	 * @param flipOrder
	 * @return
	 */
	protected Order getNaturalOrder(boolean flipOrder)
	{
		if (flipOrder)
		{
			return Order.desc(getIdPropertyName());
		}
		else
		{
			return Order.asc(getIdPropertyName());
		}
	}

	public Object firstItemId()
	{
		if (firstId == null)
		{
			// firstId was not cached, query the first item from db
			firstId = firstItemId(true);
		}
		return firstId;
	}

	/**
	 * Internanal helper method to implement {@link #firstItemId()} and {@link #lastItemId()}.
	 */
	protected Object firstItemId(boolean byPassCache)
	{
		if (byPassCache)
		{
			@SuppressWarnings("unchecked")
			T first = (T) getCriteria().setMaxResults(1).setCacheable(true).uniqueResult();
			Object id = getIdForPojo(first);
			idToIndex.put(id, normalOrder ? 0 : size() - 1);
			return id;
		}
		else
		{
			return firstItemId();
		}
	}

	/**
	 * Helper method to detect identifier of given entity object.
	 * 
	 * @param pojo the entity object which identifier is to be resolved
	 * @return the identifier if the given Hibernate entity object
	 */
	private Object getIdForPojo(Object pojo)
	{
		// TODO: is this really necessary???
		return classMetadata.getIdentifier(pojo, (SessionImplementor) this.sessionFactory.getCurrentSession());
	}

	public boolean isFirstId(Object itemId)
	{
		return itemId.equals(firstItemId());
	}

	public boolean isLastId(Object itemId)
	{
		return itemId.equals(lastItemId());
	}

	public Object lastItemId()
	{
		if (lastId == null)
		{
			normalOrder = !normalOrder;
			lastId = firstItemId(true);
			normalOrder = !normalOrder;
		}
		return lastId;
	}

	/*
	 * Simple method, but lot's of code :-)
	 * 
	 * Rather complicated logic is needed to avoid:
	 * 
	 * 1. large number of db queries
	 * 
	 * 2. scrolling through whole query result
	 * 
	 * This way this container can be used with large data sets.
	 */
	public Object nextItemId(Object itemId)
	{
		// TODO should not call if know that next exists based on cache, would
		// optimize one query in some situations
		if (isLastId(itemId))
		{
			return null;
		}

		EntityItem<T> item = new EntityItem<T>((Serializable) itemId);

		// check if next itemId is in current buffer
		List<T> buffer = getRowBuffer();
		try
		{
			int curBufIndex = buffer.indexOf(item.getPojo());
			if (curBufIndex != -1)
			{
				T object = buffer.get(curBufIndex + 1);
				return getIdForPojo(object);
			}
		}
		catch (Exception e)
		{
			// not in buffer
		}

		// itemId was not in buffer
		// build query with current order and limiting result set with the
		// reference row. Then first result is next item.

		int currentIndex = indexOfId(itemId);
		int firstIndex = normalOrder ? currentIndex + 1 : size() - currentIndex - 1;

		Criteria crit = getCriteria();
		crit = crit.setFirstResult(firstIndex).setMaxResults(ROW_BUF_SIZE);
		@SuppressWarnings("unchecked")
		List<T> newBuffer = crit.list();
		if (newBuffer.size() > 0)
		{
			// save buffer to optimize query count
			setRowBuffer(newBuffer, firstIndex);
			T nextPojo = newBuffer.get(0);
			return getIdForPojo(nextPojo);
		}
		else
		{
			return null;
		}
	}

	/**
	 * RowBuffer stores a list of entity items to avoid excessive number of DB queries.
	 * 
	 * @return
	 */
	private List<T> getRowBuffer()
	{
		if (normalOrder)
		{
			return ascRowBuffer;
		}
		else
		{
			return descRowBuffer;
		}
	}

	/**
	 * RowBuffer stores some pojos to avoid excessive number of DB queries.
	 * 
	 * Also updates the idToIndex map.
	 */
	private void setRowBuffer(List<T> list, int firstIndex)
	{
		if (normalOrder)
		{
			ascRowBuffer = list;
			for (int i = 0; i < list.size(); ++i)
			{
				idToIndex.put(getIdForPojo(list.get(i)), firstIndex + i);
			}
		}
		else
		{
			descRowBuffer = list;
			int lastIndex = size() - 1;
			for (int i = 0; i < list.size(); ++i)
			{
				idToIndex.put(getIdForPojo(list.get(i)), lastIndex - firstIndex - i);
			}
		}
	}

	/**
	 * @return column name of identifier property
	 */
	private String getIdPropertyName()
	{
		return classMetadata.getIdentifierPropertyName();
	}

	public Object prevItemId(Object itemId)
	{
		// temp flip order and use nextItemId implementation
		normalOrder = !normalOrder;
		Object prev = nextItemId(itemId);
		normalOrder = !normalOrder;
		return prev;
	}

	// Container.Indexed

	/**
	 * Not supported in HbnContainer. Indexing/order is controlled by underlaying database.
	 */
	public Object addItemAt(int index) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported in HbnContainer. Indexing/order is controlled by underlaying database.
	 */
	public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	public Object getIdByIndex(int index)
	{
		if (indexRowBuffer == null)
		{
			resetIndexRowBuffer(index);
		}
		int indexInCache = index - indexRowBufferFirstIndex;
		if (!(indexInCache >= 0 && indexInCache < indexRowBuffer.size()))
		{
			/*
			 * If requested index is not currently in cache, reset it starting from queried index.
			 */
			resetIndexRowBuffer(index);
			indexInCache = 0;
		}
		T pojo = indexRowBuffer.get(indexInCache);
		Object id = getIdForPojo(pojo);
		idToIndex.put(id, new Integer(index));
		if (idToIndex.size() > ID_TO_INDEX_MAX_SIZE)
		{
			// clear one from beginning, if ID_TO_INDEX_MAX_SIZE is total of all
			// caches, only detached indexes should get removed
			idToIndex.remove(idToIndex.keySet().iterator().next());
		}
		return id;
	}

	/**
	 * Helper method to query new set of entity items to cache from given index.
	 * 
	 * @param index the index of first entity object ot be included in query
	 */
	@SuppressWarnings("unchecked")
	private void resetIndexRowBuffer(int index)
	{
		indexRowBufferFirstIndex = index;
		indexRowBuffer = getCriteria().setFirstResult(index).setMaxResults(ROW_BUF_SIZE).list();
	}

	/*
	 * Note! Expects that getIdByIndex is called for this itemId. Otherwise it will be potentially rather slow operation
	 * with large tables. When used with Table, this shouldn't be a problem.
	 */
	public int indexOfId(Object itemId)
	{
		Integer index = idToIndex.get(itemId);
		if (index == null)
		{
			return slowIndexOfId(itemId);
		}
		return index;
	}

	private int slowIndexOfId(Object itemId)
	{
		Criteria crit = getCriteria();
		crit.setProjection(Projections.id());
		List<?> list = crit.list();
		return list.indexOf(itemId);
	}

	// Container.Sortable methods

	public Collection<String> getSortableContainerPropertyIds()
	{
		// use Hibernates metadata helper to determine property names
		String[] propertyNames = classMetadata.getPropertyNames();
		LinkedList<String> propertyIds = new LinkedList<String>();
		propertyIds.addAll(Arrays.asList(propertyNames));
		propertyIds.addAll(getEmbeddedKeyPropertyIds());
		return propertyIds;
	}

	public void sort(Object[] propertyId, boolean[] ascending)
	{
		// we do not actually sort anything here, just clearing cache will do
		// the thing lazily.
		clearInternalCache();
		orderPropertyIds = propertyId;
		orderAscendings = ascending;
	}

	/**
	 * Helper method to clear all cache fields.
	 */
	protected void clearInternalCache()
	{
		idToIndex.clear();
		indexRowBuffer = null;
		ascRowBuffer = null;
		descRowBuffer = null;
		firstId = null;
		lastId = null;
		size = null;
	}

	/**
	 * Adds container filter for hibernate mapped property. For property not mapped by Hibernate,
	 * {@link UnsupportedOperationException} is thrown.
	 * 
	 * @see Container.Filterable#addContainerFilter(Object, String, boolean, boolean)
	 */
	public void addContainerFilter(Object propertyId, String filterString, boolean ignoreCase, boolean onlyMatchPrefix)
	{
		addContainerFilter(new StringContainerFilter(propertyId, filterString, ignoreCase, onlyMatchPrefix));
	}

	public void addContainerFilter(ContainerFilter containerFilter)
	{
		if (addedProperties.containsKey(containerFilter.getPropertyId()))
		{
			throw new UnsupportedOperationException(
					"HbnContainer does not support filterig properties not mapped by Hibernate");
		}
		else
		{
			if (filters == null)
			{
				filters = new HashSet<ContainerFilter>();
			}
			filters.add(containerFilter);
			clearInternalCache();
			fireItemSetChange();
		}
	}

	public void removeAllContainerFilters()
	{
		if (filters != null)
		{
			filters = null;
			clearInternalCache();
			fireItemSetChange();
		}
	}

	public void removeContainerFilters(Object propertyId)
	{
		if (filters != null)
		{
			for (Iterator<ContainerFilter> iterator = filters.iterator(); iterator.hasNext();)
			{
				ContainerFilter f = iterator.next();
				if (f.getPropertyId().equals(propertyId))
				{
					iterator.remove();
				}
			}
			clearInternalCache();
			fireItemSetChange();
		}
	}

	/**
	 * HbnContainer only supports old style addContainerFilter(Object, String, boolean booblean) API and
	 * {@link SimpleStringFilter}. Support for this newer API maybe in upcoming versions.
	 * <p>
	 * Also note that for complex filtering it is possible to override {@link #getBaseCriteria()} method and add filter
	 * so the query directly.
	 * 
	 * @see #addContainerFilter(Object, String, boolean, boolean)
	 * @see com.vaadin.data.Container.Filterable#addContainerFilter(com.vaadin.data.Container.Filter)
	 */
	public void addContainerFilter(Filter filter) throws UnsupportedFilterException
	{
		// TODO support new Filter api propertly
		if (filter instanceof SimpleStringFilter)
		{
			SimpleStringFilter sf = (SimpleStringFilter) filter;
			Object propertyId = sf.getPropertyId();
			boolean onlyMatchPrefix = sf.isOnlyMatchPrefix();
			boolean ignoreCase = sf.isIgnoreCase();
			String filterString = sf.getFilterString();
			addContainerFilter(propertyId, filterString, ignoreCase, onlyMatchPrefix);
		}
		else
		{
			throw new UnsupportedFilterException(
					"HbnContainer only supports old style addContainerFilter(Object, String, boolean booblean) API. Support for this newer API maybe in upcoming versions.");
		}
	}

	public void removeContainerFilter(Filter filter)
	{
		// TODO support new Filter api propertly. Even the workaround for
		// SimpleStringFilter works wrong, but hopefully will work good enough
		// for e.g. ComboBox
		if (filter instanceof SimpleStringFilter)
		{
			SimpleStringFilter sf = (SimpleStringFilter) filter;
			Object propertyId = sf.getPropertyId();
			removeContainerFilters(propertyId);
		}
	}

	/*
	 * Implementation of the Container.Hierarchical interface
	 */

	private String parentPropertyName = null;

	/**
	 * Finds the identifiers for the children of the given item. The returned collection is unmodifiable.
	 * 
	 * @param itemId - ID of the Item whose children the caller is interested in
	 * @return Returns An unmodifiable collection containing the IDs of all other Items that are children in the
	 *         container hierarchy
	 */
	public Collection<?> getChildren(Object itemId)
	{
		ArrayList<Object> children = new ArrayList<Object>();
		String parentPropertyName = getParentPropertyName();

		if (parentPropertyName == null)
		{
			return children;
		}

		for (Object id : getItemIds())
		{
			EntityItem<T> item = getItem(id);
			Property<?> property = item.getItemProperty(parentPropertyName);
			Object value = property.getValue();

			if (itemId.equals(value))
			{
				children.add(id);
			}
		}

		return children;
	}

	/**
	 * Gets the identifier of the given item's parent. If there is no parent or we are unable to infer the name of the
	 * parent property this method will return null.
	 * 
	 * @param itemId - The identifier of the item.
	 * @return Returns the identifier of the parent, or null if a parent cannot be found.
	 */
	public Object getParent(Object itemId)
	{
		String parentPropertyName = getParentPropertyName();

		if (parentPropertyName == null)
		{
			logger.warn("failed to find a parent property name; hierarchy may be incomplete.");
			return null;
		}

		EntityItem<T> item = getItem(itemId);
		Property<?> property = item.getItemProperty(parentPropertyName);
		Object value = property.getValue();

		return value;
	}

	/**
	 * Gets the IDs of all Items in the container that don't have a parent. Such items are called root Items. The
	 * returned collection is unmodifiable.
	 * 
	 * @return Returns a collection all root element item identifiers.
	 */
	public Collection<?> rootItemIds()
	{
		ArrayList<Object> rootItems = new ArrayList<Object>();
		String parentPropertyName = getParentPropertyName();

		if (parentPropertyName == null)
		{
			logger.warn("failed to find a parent property name; hierarchy may be incomplete.");
			return rootItems;
		}

		for (Object id : getItemIds())
		{
			EntityItem<T> item = getItem(id);
			Property<?> property = item.getItemProperty(parentPropertyName);
			Object value = property.getValue();

			if (value == null)
			{
				rootItems.add(id);
			}
		}

		return rootItems;
	}

	/**
	 * Sets the parent of an Item. The new parent item must exist and be able to have children. (
	 * areChildrenAllowed(Object) == true ). It is also possible to detach a node from the hierarchy (and thus make it
	 * root) by setting the parent null.
	 * 
	 * This operation is optional.
	 * 
	 * @param itemId - ID of the item to be set as the child of the Item identified with newParentId
	 * @param newParentId - ID of the Item that's to be the new parent of the Item identified with itemId
	 * @return Returns true if the operation succeeded, false if not
	 */
	public boolean setParent(Object itemId, Object newParentId)
	{
		String parentPropertyName = getParentPropertyName();

		if (parentPropertyName == null)
		{
			logger.warn("failed to find a parent property name; unable to set the parent.");
			return false;
		}

		EntityItem<T> item = getItem(itemId);
		Property<?> property = item.getItemProperty(parentPropertyName);
		property.setValue(newParentId);

		Object value = property.getValue();
		return (value.equals(newParentId));
	}

	/**
	 * Tests if the Item with given ID can have children.
	 * 
	 * @param itemId - ID of the Item in the container whose child capability is to be tested
	 * @return Returns true if the specified Item exists in the Container and it can have children, false if it's not
	 *         found from the container or it can't have children.
	 */
	public boolean areChildrenAllowed(Object itemId)
	{
		return (parentPropertyName != null && containsId(itemId));
	}

	/**
	 * Sets the given Item's capability to have children. If the Item identified with itemId already has children and
	 * areChildrenAllowed(Object) is false this method fails and false is returned.
	 * 
	 * The children must be first explicitly removed with setParent(Object itemId, Object newParentId)or
	 * com.vaadin.data.Container.removeItem(Object itemId).
	 * 
	 * This operation is optional. If it is not implemented, the method always returns false.
	 * 
	 * @param itemId - ID of the Item in the container whose child capability is to be set.
	 * @param areChildrenAllowed - boolean value specifying if the Item can have children or not.
	 * @return Returns true if the operation succeeded, false if not.
	 */
	public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed)
	{
		return false;
	}

	/**
	 * Tests if the Item specified with itemId is a root Item. The hierarchical container can have more than one root
	 * and must have at least one unless it is empty. The getParent(Object itemId) method always returns null for root
	 * Items.
	 * 
	 * @param itemId - ID of the Item whose root status is to be tested
	 * @return Returns true if the specified Item is a root, false if not
	 */
	public boolean isRoot(Object itemId)
	{
		String parentPropertyName = getParentPropertyName();

		if (parentPropertyName == null)
		{
			logger.warn("failed to find a parent property name; hierarchy may be incomplete.");
			return false;
		}

		EntityItem<T> item = getItem(itemId);
		Property<?> property = item.getItemProperty(parentPropertyName);
		Object value = property.getValue();

		return (value == null);
	}

	/**
	 * Tests if the Item specified with itemId has child Items or if it is a leaf. The getChildren(Object itemId) method
	 * always returns null for leaf Items.
	 * 
	 * Note that being a leaf does not imply whether or not an Item is allowed to have children.
	 * 
	 * @param itemId - ID of the Item to be tested.
	 * @return Returns true if the specified Item has children, false if not (is a leaf).
	 */
	public boolean hasChildren(Object itemId)
	{
		String parentPropertyName = getParentPropertyName();

		if (parentPropertyName == null)
		{
			logger.warn("failed to find a parent property name; hierarchy may be incomplete.");
			return false;
		}

		for (Object id : getItemIds())
		{
			EntityItem<T> item = getItem(id);
			Property<?> property = item.getItemProperty(parentPropertyName);
			Object value = property.getValue();

			if (itemId.equals(value))
			{
				return true;
			}
		}

		return false;
	}

	/*
	 * Private Helper Method
	 * 
	 * Infers the name of the parent field belonging to the current propoerty based on type.
	 */
	private String getParentPropertyName()
	{
		// TODO: make this a little more robust, there are a number of cases
		// where this will fail.

		if (parentPropertyName == null)
		{
			String[] propertyNames = classMetadata.getPropertyNames();

			for (int i = 0; i < propertyNames.length; ++i)
			{
				String entityTypeName = entityType.getName();
				String propertyTypeName = classMetadata.getPropertyType(propertyNames[i]).getName();

				if (entityTypeName.equals(propertyTypeName))
				{
					parentPropertyName = propertyNames[i];
					break;
				}
			}
		}

		return parentPropertyName;
	}

	@Override
	public void addItemSetChangeListener(ItemSetChangeListener listener)
	{
		this.addListener(listener);
	}

	@Override
	public void removeItemSetChangeListener(ItemSetChangeListener listener)
	{
		this.removeListener(listener);
	}
}
