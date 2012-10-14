package com.vaadin.data.hbnutil;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class StringContainerFilter extends ContainerFilter
{
	private final String filterString;
	private final boolean onlyMatchPrefix;
	private final boolean ignoreCase;

	public StringContainerFilter(Object propertyId, String filterString, boolean ignoreCase, boolean onlyMatchPrefix)
	{
		super(propertyId);
		this.ignoreCase = ignoreCase;
		this.filterString = ignoreCase ? filterString.toLowerCase() : filterString;
		this.onlyMatchPrefix = onlyMatchPrefix;
	}

	public Criterion getFieldCriterion(String fullPropertyName)
	{
		return (ignoreCase) ? Restrictions.ilike(fullPropertyName, filterString, onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE) : Restrictions.like(fullPropertyName, filterString,
				onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE);
	}
}
