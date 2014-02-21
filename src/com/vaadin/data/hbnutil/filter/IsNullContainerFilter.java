package com.vaadin.data.hbnutil.filter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.vaadin.data.util.filter.IsNull;

public class IsNullContainerFilter extends ContainerFilter
{

	public IsNullContainerFilter(IsNull filter)
	{
		super(filter.getPropertyId());
	}

	@Override
	public Criterion getFieldCriterion(String fullPropertyName)
	{
		return Restrictions.isNull(fullPropertyName);
	}

}
