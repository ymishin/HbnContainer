package com.vaadin.data.hbnutil.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Or;

public class OrContainerFilter extends ContainerFilter
{

	final Collection<Filter> filters;

	public OrContainerFilter(Or filter)
	{
		super(null);
		this.filters = filter.getFilters();
	}

	@Override
	public Criterion getFieldCriterion(String fullPropertyName)
	{
		final List<Criterion> criteria = new ArrayList<Criterion>();
		for (Filter filter : filters)
		{
			final ContainerFilter f = FilterFactory.getContainerFilter(filter);
			final Criterion c = f.getFieldCriterion(fullPropertyName);
			criteria.add(c);
		}

		return Restrictions.or(criteria.toArray(new Criterion[0]));
	}

}
