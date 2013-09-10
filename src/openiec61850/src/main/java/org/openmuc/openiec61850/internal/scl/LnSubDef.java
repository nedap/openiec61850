package org.openmuc.openiec61850.internal.scl;

import org.openmuc.openiec61850.LogicalNode;
import org.w3c.dom.Node;

public class LnSubDef {

	public Node defXmlNode;
	public LogicalNode logicalNode;

	public LnSubDef(Node dataSetDefXmlNode, LogicalNode logicalNode) {
		defXmlNode = dataSetDefXmlNode;
		this.logicalNode = logicalNode;
	}

}
