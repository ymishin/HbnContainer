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

public abstract class ContainerFilter
{
	private final Object propertyId;

	public ContainerFilter(Object propertyId)
	{
		this.propertyId = propertyId;
	}

	public Object getPropertyId()
	{
		return propertyId;
	}

	public abstract Criterion getFieldCriterion(String fullPropertyName);

	public Criterion getCriterion(String idName)
	{
		return (idName == null) ? getFieldCriterion(getPropertyId().toString())
				: getFieldCriterion(idName + "." + getPropertyId());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((propertyId == null) ? 0 : propertyId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContainerFilter other = (ContainerFilter) obj;
		if (propertyId == null)
		{
			if (other.propertyId != null)
				return false;
		} else if (!propertyId.equals(other.propertyId))
			return false;
		return true;
	}

}
