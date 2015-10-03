package org.skyve.metadata.module.query;

import java.util.List;

import org.skyve.metadata.MetaDataException;
import org.skyve.metadata.customer.Customer;
import org.skyve.metadata.module.Module;
import org.skyve.persistence.DocumentQuery;
import org.skyve.persistence.DocumentQuery.AggregateFunction;
import org.skyve.web.WebContext;

/**
 * 
 */
public interface DocumentQueryDefinition extends QueryDefinition {
	/**
	 * 
	 * @return
	 */
	public String getDocumentName();
	
	/**
	 * 
	 * @param customer
	 * @return
	 * @throws MetaDataException
	 */
	public Module getDocumentModule(Customer customer)
	throws MetaDataException;
	
	/**
	 * 
	 * @return
	 */
	public String getFromClause();
	
	/**
	 * 
	 * @return
	 */
	public String getFilterClause();
	
	/**
	 * 
	 * @return
	 */
	public List<QueryColumn> getColumns();
	
	/**
	 * 
	 * @return
	 */
	public <T extends WebContext> Querylet<T> getQuerylet();
	
	/**
	 * 
	 * @param summaryType
	 * @param tagId
	 * @return
	 * @throws MetaDataException
	 */
	public DocumentQuery constructDocumentQuery(AggregateFunction summaryType, String tagId)
	throws MetaDataException;
}