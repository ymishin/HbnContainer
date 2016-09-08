package com.vaadin.data.hbnutil;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class NumberContainerFilter extends ContainerFilter
{
	private final String cmp;
	private final Number num;

	public NumberContainerFilter(Object propertyId, String cmp, Number num)
	{
		super(propertyId);
		this.cmp = cmp;
		this.num = num;
	}

	public Criterion getFieldCriterion(String fullPropertyName)
	{
		Criterion criterion = null;

		switch (cmp)
		{
		case "GT":
		case "gt":
			criterion = Restrictions.gt(fullPropertyName, num);
			break;
		case "GE":
		case "ge":
			criterion = Restrictions.ge(fullPropertyName, num);
			break;
		case "LT":
		case "lt":
			criterion = Restrictions.lt(fullPropertyName, num);
			break;
		case "LE":
		case "le":
			criterion = Restrictions.le(fullPropertyName, num);
			break;
		case "EQ":
		case "eq":
			criterion = Restrictions.eq(fullPropertyName, num);
			break;
		default:
			break;
		}

		return criterion;
	}
}