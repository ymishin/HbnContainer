package com.vaadin.data.hbnutil.filter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Compare.Operation;
import com.vaadin.data.util.filter.UnsupportedFilterException;

public class CompareContainerFilter extends ContainerFilter
{

	private final Object value;
	private final Operation operation;

	public CompareContainerFilter(Compare filter)
	{
		super(filter.getPropertyId());
		this.value = filter.getValue();
		this.operation = filter.getOperation();
	}

	@Override
	public Criterion getFieldCriterion(String fullPropertyName)
	{
		switch (operation)
		{
		case EQUAL:
			return Restrictions.eq(fullPropertyName, value);
		case GREATER:
			return Restrictions.gt(fullPropertyName, value);
		case GREATER_OR_EQUAL:
			return Restrictions.ge(fullPropertyName, value);
		case LESS:
			return Restrictions.lt(fullPropertyName, value);
		case LESS_OR_EQUAL:
			return Restrictions.le(fullPropertyName, value);
		default:
			throw new UnsupportedFilterException(
					"Unknown Compare filter operation " + operation);
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((operation == null) ? 0 : operation.hashCode());
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
		CompareContainerFilter other = (CompareContainerFilter) obj;
		if (operation != other.operation)
			return false;
		if (value == null)
		{
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
