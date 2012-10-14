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
		return (ignoreCase) ? Restrictions.ilike(fullPropertyName, filterString, onlyMatchPrefix ? MatchMode.START
				: MatchMode.ANYWHERE) : Restrictions.like(fullPropertyName, filterString,
				onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE);
	}
}
