package com.vaadin.data.hbnutil;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class IdContainerFilter extends ContainerFilter
{
	private final Integer id;

	public IdContainerFilter(Object propertyId, Integer id)
	{
		super(propertyId);
		this.id = id;
	}

	@Override
	public Criterion getFieldCriterion(String fullPropertyName)
	{
		return Restrictions.eq(fullPropertyName, id);
	}
}
