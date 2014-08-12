package org.skyve.wildcat.metadata.repository.view.actions;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.skyve.metadata.controller.ImplicitActionName;
import org.skyve.wildcat.util.XMLUtil;

@XmlType(namespace = XMLUtil.VIEW_NAMESPACE)
@XmlRootElement(namespace = XMLUtil.VIEW_NAMESPACE, name = "remove")
public class RemoveAction extends PositionableAction {
	public RemoveAction() {
		implicitName = ImplicitActionName.Remove;
	}
}