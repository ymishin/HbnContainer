/*
 * Copyright 2012, Gary Piercey, All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.vaadin.data.hbnutil.filter;

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
		return (ignoreCase) ? Restrictions.ilike(fullPropertyName, filterString, onlyMatchPrefix ? MatchMode.START
				: MatchMode.ANYWHERE) : Restrictions.like(fullPropertyName, filterString,
				onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((filterString == null) ? 0 : filterString.hashCode());
		result = prime * result + (ignoreCase ? 1231 : 1237);
		result = prime * result + (onlyMatchPrefix ? 1231 : 1237);
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
		StringContainerFilter other = (StringContainerFilter) obj;
		if (filterString == null)
		{
			if (other.filterString != null)
				return false;
		} else if (!filterString.equals(other.filterString))
			return false;
		if (ignoreCase != other.ignoreCase)
			return false;
		if (onlyMatchPrefix != other.onlyMatchPrefix)
			return false;
		return true;
	}

}
