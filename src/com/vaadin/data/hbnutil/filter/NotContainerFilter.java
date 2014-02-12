package com.vaadin.data.hbnutil.filter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Not;

public class NotContainerFilter extends ContainerFilter
{

	final Filter filter;

	public NotContainerFilter(Not filter)
	{
		super(null);
		this.filter = filter.getFilter();
	}

	@Override
	public Criterion getFieldCriterion(String fullPropertyName)
	{
		final ContainerFilter containerFilter = FilterFactory
				.getContainerFilter(filter);
		final Criterion criterion = containerFilter
				.getFieldCriterion(fullPropertyName);

		return Restrictions.not(criterion);
	}

}
