package org.skyve.wildcat.metadata.module.query;

import org.skyve.metadata.FilterOperator;
import org.skyve.metadata.SortDirection;

public class QueryColumn implements org.skyve.metadata.module.query.QueryColumn {
	/**
	 * For Serialization
	 */
	private static final long serialVersionUID = 5165779649604451833L;

	private String name;

	private String binding;

	private String expression;

	private FilterOperator filterOperator;

	private String filterExpression;

	private SortDirection sortOrder;

	private String displayName;

	private boolean projected = true;

	private boolean hidden = false;

	private boolean sortable = true;

	private boolean filterable = true;

	private boolean editable = true;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getBinding() {
		return binding;
	}

	public void setBinding(String binding) {
		this.binding = binding;
	}

	@Override
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public FilterOperator getFilterOperator() {
		return filterOperator;
	}

	public void setFilterOperator(FilterOperator filterOperator) {
		this.filterOperator = filterOperator;
	}

	@Override
	public String getFilterExpression() {
		return filterExpression;
	}

	public void setFilterExpression(String filterExpression) {
		this.filterExpression = filterExpression;
	}

	@Override
	public SortDirection getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortDirection sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public boolean isProjected() {
		return projected;
	}

	public void setSelected(boolean projected) {
		this.projected = projected;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public boolean isFilterable() {
		return filterable;
	}

	public void setFilterable(boolean filterable) {
		this.filterable = filterable;
	}

	@Override
	public boolean isSortable() {
		return sortable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}
