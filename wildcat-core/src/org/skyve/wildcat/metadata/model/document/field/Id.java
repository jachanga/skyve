package org.skyve.wildcat.metadata.model.document.field;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.skyve.metadata.model.document.DomainType;
import org.skyve.wildcat.util.XMLUtil;

@XmlType(namespace = XMLUtil.DOCUMENT_NAMESPACE)
@XmlRootElement(namespace = XMLUtil.DOCUMENT_NAMESPACE)
public class Id extends Field {
	private static final long serialVersionUID = -4193089805642262555L;

	public Id() {
		setAttributeType(AttributeType.id);
	}
	
	/**
	 * Not used
	 */
	@Override
	@XmlTransient
	public DomainType getDomainType() {
		return null;
	}
}