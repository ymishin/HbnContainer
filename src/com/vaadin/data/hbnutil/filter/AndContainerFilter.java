package com.vaadin.data.hbnutil.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;

public class AndContainerFilter extends ContainerFilter
{

	final Collection<Filter> filters;

	public AndContainerFilter(And filter)
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

		return Restrictions.and(criteria.toArray(new Criterion[0]));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((filters == null) ? 0 : filters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AndContainerFilter other = (AndContainerFilter) obj;
		if (filters == null)
		{
			if (other.filters != null)
				return false;
		} else if (!filters.equals(other.filters))
			return false;
		return true;
	}

}
