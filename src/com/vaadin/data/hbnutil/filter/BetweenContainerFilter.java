package com.vaadin.data.hbnutil.filter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.vaadin.data.util.filter.Between;

public class BetweenContainerFilter extends ContainerFilter
{

	final Object startValue;
	final Object endValue;

	public BetweenContainerFilter(Between filter)
	{
		super(filter.getPropertyId());
		this.startValue = filter.getStartValue();
		this.endValue = filter.getEndValue();
	}

	@Override
	public Criterion getFieldCriterion(String fullPropertyName)
	{
		return Restrictions.between(fullPropertyName, startValue, endValue);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((endValue == null) ? 0 : endValue.hashCode());
		result = prime * result
				+ ((startValue == null) ? 0 : startValue.hashCode());
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
		BetweenContainerFilter other = (BetweenContainerFilter) obj;
		if (endValue == null)
		{
			if (other.endValue != null)
				return false;
		} else if (!endValue.equals(other.endValue))
			return false;
		if (startValue == null)
		{
			if (other.startValue != null)
				return false;
		} else if (!startValue.equals(other.startValue))
			return false;
		return true;
	}

}
