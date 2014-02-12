package com.vaadin.data.hbnutil.filter;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.UnsupportedFilterException;

public class FilterFactory
{

	public static ContainerFilter getContainerFilter(SimpleStringFilter filter)
	{
		final String filterString = filter.getFilterString();
		final Object propertyId = filter.getPropertyId();
		final boolean ignoreCase = filter.isIgnoreCase();
		final boolean onlyMatchPrefix = filter.isOnlyMatchPrefix();

		return new StringContainerFilter(propertyId, filterString, ignoreCase,
				onlyMatchPrefix);
	}

	public static ContainerFilter getContainerFilter(Compare filter)
	{
		return new CompareContainerFilter(filter);
	}

	public static ContainerFilter getContainerFilter(Between filter)
	{
		return new BetweenContainerFilter(filter);
	}

	public static ContainerFilter getContainerFilter(IsNull filter)
	{
		return new IsNullContainerFilter(filter);
	}

	public static ContainerFilter getContainerFilter(And filter)
	{
		return new AndContainerFilter(filter);
	}

	public static ContainerFilter getContainerFilter(Or filter)
	{
		return new OrContainerFilter((Or) filter);
	}

	public static ContainerFilter getContainerFilter(Filter filter)
	{
		if (filter instanceof SimpleStringFilter)
		{
			return getContainerFilter((SimpleStringFilter) filter);
		} else if (filter instanceof Compare)
		{
			return getContainerFilter((Compare) filter);
		} else if (filter instanceof Between)
		{
			return getContainerFilter((Between) filter);
		} else if (filter instanceof IsNull)
		{
			return getContainerFilter((IsNull) filter);
		} else if (filter instanceof Like)
		{
			return getContainerFilter((Like) filter);
		} else if (filter instanceof And)
		{
			return getContainerFilter((And) filter);
		} else if (filter instanceof Or)
		{
			return getContainerFilter((Or) filter);
		} else
		{
			final String message = "HbnContainer does not support filtering using "
					+ filter.getClass().getName();
			throw new UnsupportedFilterException(message);
		}
	}
}
