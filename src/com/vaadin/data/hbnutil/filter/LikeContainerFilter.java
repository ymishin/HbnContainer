package com.vaadin.data.hbnutil.filter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.vaadin.data.util.filter.Like;

public class LikeContainerFilter extends ContainerFilter
{

	final Object value;

	public LikeContainerFilter(Like filter)
	{
		super(filter.getPropertyId());
		this.value = filter.getValue();
	}

	@Override
	public Criterion getFieldCriterion(String fullPropertyName)
	{
		return Restrictions.like(fullPropertyName, value);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		LikeContainerFilter other = (LikeContainerFilter) obj;
		if (value == null)
		{
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
