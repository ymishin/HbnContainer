package com.vaadin.data.hbnutil;

import org.hibernate.criterion.Criterion;

public abstract class ContainerFilter
{
	private final Object propertyId;

	public ContainerFilter(Object propertyId)
	{
		this.propertyId = propertyId;
	}

	public Object getPropertyId()
	{
		return propertyId;
	}

	public abstract Criterion getFieldCriterion(String fullPropertyName);

	public Criterion getCriterion(String idName)
	{
		return (idName == null)
			? getFieldCriterion(getPropertyId().toString())
			: getFieldCriterion(idName + "." + getPropertyId());
	}

}
