package org.skyve.impl.web.faces.actions;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.skyve.CORE;
import org.skyve.domain.Bean;
import org.skyve.impl.metadata.customer.CustomerImpl;
import org.skyve.impl.metadata.model.document.AssociationImpl;
import org.skyve.impl.metadata.model.document.DocumentImpl;
import org.skyve.impl.metadata.repository.AbstractRepository;
import org.skyve.impl.util.UtilImpl;
import org.skyve.impl.web.faces.FacesAction;
import org.skyve.metadata.customer.Customer;
import org.skyve.metadata.model.Attribute;
import org.skyve.metadata.model.document.Bizlet.DomainValue;
import org.skyve.metadata.model.document.Document;
import org.skyve.metadata.model.document.DomainType;
import org.skyve.metadata.module.Module;
import org.skyve.util.Binder;
import org.skyve.util.Binder.TargetMetaData;
import org.skyve.util.Util;

public class GetSelectItemsAction extends FacesAction<List<SelectItem>> {
	private Bean bean;
	private String binding;
	private boolean includeEmptyItem;
	private String moduleName;
	private String documentName;
	
	/**
	 * Constructor used for input components in forms and grids.
	 * @param bean
	 * @param binding
	 * @param includeEmptyItem
	 */
	public GetSelectItemsAction(Bean bean, String binding, boolean includeEmptyItem) {
		this.bean = bean;
		this.binding = binding;
		this.includeEmptyItem = includeEmptyItem;
		this.moduleName = bean.getBizModule();
		this.documentName = bean.getBizDocument();
	}

	/**
	 * Constructor used for filter components.
	 * @param moduleName
	 * @param documentName
	 * @param binding
	 * @param includeEmptyItem
	 */
	public GetSelectItemsAction(String moduleName, String documentName, String binding, boolean includeEmptyItem) {
		this.binding = binding;
		this.includeEmptyItem = includeEmptyItem;
		this.moduleName = moduleName;
		this.documentName = documentName;
	}

	@Override
	public List<SelectItem> callback() throws Exception {
		if (UtilImpl.FACES_TRACE) Util.LOGGER.info("GetSelectItemsAction - binding=" + binding + " : includeEmptyItem=" + includeEmptyItem);

    	Customer customer = CORE.getUser().getCustomer();
        Module module = customer.getModule(moduleName);
        Document document = module.getDocument(customer, documentName);
        TargetMetaData target = Binder.getMetaDataForBinding(customer, module, document, binding);
        Attribute targetAttribute = target.getAttribute();
        Document targetDocument = target.getDocument();

        List<SelectItem> result = null;
        
        if ((targetDocument != null) && (targetAttribute != null)) {
            DomainType domainType = targetAttribute.getDomainType();
            Bean owningBean = bean;
            if (bean != null) {
	            int lastDotIndex = binding.lastIndexOf('.');
	            if (lastDotIndex > 0) {
	            	owningBean = (Bean) Binder.get(bean, binding.substring(0, lastDotIndex));
	            }
            }
            
            List<DomainValue> domainValues = ((DocumentImpl) targetDocument).getDomainValues((CustomerImpl) customer,
																	                            domainType,
																	                            targetAttribute,
																	                            owningBean,
																	                            true);
            if (includeEmptyItem) {
	            result = new ArrayList<>(domainValues.size() + 1);
	        	// add an empty select item so that a null value 
	        	// in the bean can be represented, even if mandatory
	            // Notice filter components should always have a selectable empty value
	        	if ((bean != null) && targetAttribute.isRequired()) {
	        		result.add(new SelectItem(null, "", "", true, false, true)); // mandatory gets an unselectable item
	        	}
	        	else {
	        		result.add(new SelectItem(null, "")); // optional gets a selectable item
	        	}
            }
            else {
	            result = new ArrayList<>(domainValues.size());
            }

            Class<?> type = null;
        	for (DomainValue domainValue : domainValues) {
            	String code = domainValue.getCode();
            	Object value = code;
            	if (code != null) {
	            	if (targetAttribute instanceof org.skyve.impl.metadata.model.document.field.Enumeration) {
	            		if (type == null) {
	            			type = AbstractRepository.get().getEnum((org.skyve.impl.metadata.model.document.field.Enumeration) targetAttribute); 
	            		}
            			value = Binder.convert(type, code);
            		}
	            	else if (targetAttribute instanceof AssociationImpl) {
                   		AssociationImpl targetAssociation = (AssociationImpl) targetAttribute;
                   	 	value = CORE.getPersistence().retrieve(targetDocument.getOwningModuleName(), 
	                											targetAssociation.getDocumentName(),
	                											code,
	                											false);
	            	}
	            	else {
	            		if (type == null) {
	            			type = targetAttribute.getAttributeType().getImplementingType();
	            		}
	            		if (! type.equals(String.class)) {
	            			value = Binder.fromString(customer, null, type, code, true);
	            		}
	            	}
            	}
            	result.add(new SelectItem(value, domainValue.getDescription()));
            }
        }
        else {
        	result = new ArrayList<>(0);
        }

        return result;
	}
}
