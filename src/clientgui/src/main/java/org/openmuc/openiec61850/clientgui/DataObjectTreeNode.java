/*
 * Copyright 2011-13 Fraunhofer ISE, energy & meteo Systems GmbH and other contributors
 *
 * This file is part of OpenIEC61850.
 * For more information visit http://www.openmuc.org
 *
 * OpenIEC61850 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * OpenIEC61850 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenIEC61850.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.openiec61850.clientgui;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.openmuc.openiec61850.BasicDataAttribute;
import org.openmuc.openiec61850.BdaBoolean;
import org.openmuc.openiec61850.BdaCheck;
import org.openmuc.openiec61850.BdaDoubleBitPos;
import org.openmuc.openiec61850.BdaEntryTime;
import org.openmuc.openiec61850.BdaFloat32;
import org.openmuc.openiec61850.BdaFloat64;
import org.openmuc.openiec61850.BdaInt16;
import org.openmuc.openiec61850.BdaInt16U;
import org.openmuc.openiec61850.BdaInt32;
import org.openmuc.openiec61850.BdaInt32U;
import org.openmuc.openiec61850.BdaInt64;
import org.openmuc.openiec61850.BdaInt8;
import org.openmuc.openiec61850.BdaInt8U;
import org.openmuc.openiec61850.BdaOctetString;
import org.openmuc.openiec61850.BdaOptFlds;
import org.openmuc.openiec61850.BdaQuality;
import org.openmuc.openiec61850.BdaReasonForInclusion;
import org.openmuc.openiec61850.BdaTapCommand;
import org.openmuc.openiec61850.BdaTimestamp;
import org.openmuc.openiec61850.BdaTriggerConditions;
import org.openmuc.openiec61850.BdaUnicodeString;
import org.openmuc.openiec61850.BdaVisibleString;
import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.Fc;
import org.openmuc.openiec61850.FcModelNode;
import org.openmuc.openiec61850.ModelNode;
import org.openmuc.openiec61850.ServiceError;
import org.openmuc.openiec61850.clientgui.databind.BooleanDataBind;
import org.openmuc.openiec61850.clientgui.databind.CheckDataBind;
import org.openmuc.openiec61850.clientgui.databind.DoubleBitPosDataBind;
import org.openmuc.openiec61850.clientgui.databind.EntryTimeDataBind;
import org.openmuc.openiec61850.clientgui.databind.Float32DataBind;
import org.openmuc.openiec61850.clientgui.databind.Float64DataBind;
import org.openmuc.openiec61850.clientgui.databind.Int16DataBind;
import org.openmuc.openiec61850.clientgui.databind.Int16UDataBind;
import org.openmuc.openiec61850.clientgui.databind.Int32DataBind;
import org.openmuc.openiec61850.clientgui.databind.Int32UDataBind;
import org.openmuc.openiec61850.clientgui.databind.Int64DataBind;
import org.openmuc.openiec61850.clientgui.databind.Int8DataBind;
import org.openmuc.openiec61850.clientgui.databind.Int8UDataBind;
import org.openmuc.openiec61850.clientgui.databind.OctetStringDataBind;
import org.openmuc.openiec61850.clientgui.databind.OptfldsDataBind;
import org.openmuc.openiec61850.clientgui.databind.QualityDataBind;
import org.openmuc.openiec61850.clientgui.databind.ReasonForInclusionDataBind;
import org.openmuc.openiec61850.clientgui.databind.TapCommandDataBind;
import org.openmuc.openiec61850.clientgui.databind.TimeStampDataBind;
import org.openmuc.openiec61850.clientgui.databind.TriggerConditionDataBind;
import org.openmuc.openiec61850.clientgui.databind.UnicodeStringDataBind;
import org.openmuc.openiec61850.clientgui.databind.VisibleStringDataBind;

public class DataObjectTreeNode extends DefaultMutableTreeNode implements DataTreeNode {

	private static final long serialVersionUID = -3596243932937737877L;

	private final ModelNode node;
	private final BasicDataBind<?> data;

	public DataObjectTreeNode(String name, ModelNode node) {
		super(name);
		this.node = node;
		if (node != null && node.getChildren() == null) {
			// for (ModelNode child : node.getChildren()) {
			// if (child instanceof BasicDataAttribute) {
			// data.add(createDataBind((BasicDataAttribute) child));
			// }
			// }
			data = createDataBind((BasicDataAttribute) node);
		}
		else {
			data = null;
		}
	}

	public ModelNode getNode() {
		return node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmuc.openiec61850.clientgui.DataTreeNode#getData()
	 */
	@Override
	public BasicDataBind<?> getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmuc.openiec61850.clientgui.DataTreeNode#reset()
	 */
	@Override
	public void reset(ClientAssociation association) throws ServiceError, IOException {
		if (association != null) {
			association.getDataValues((FcModelNode) node);
		}
		if (data != null) {
			data.reset();
		}
		else {
			for (int i = 0; i < getChildCount(); i++) {
				if (getChildAt(i) instanceof DataObjectTreeNode) {
					DataTreeNode child = (DataTreeNode) getChildAt(i);
					child.reset(null);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmuc.openiec61850.clientgui.DataTreeNode#writeValues()
	 */
	@Override
	public void writeValues(ClientAssociation association) throws ServiceError, IOException {
		if (data != null) {
			data.write();
		}
		else {
			for (int i = 0; i < getChildCount(); i++) {
				if (getChildAt(i) instanceof DataObjectTreeNode) {
					DataTreeNode child = (DataTreeNode) getChildAt(i);
					child.writeValues(null);
				}
			}
		}
		if (association != null) {
			association.setDataValues((FcModelNode) node);
		}
	}

	@Override
	public boolean writable() {
		if (node instanceof FcModelNode) {
			FcModelNode modelNode = (FcModelNode) node;
			Fc constraint = modelNode.getFc();
			return constraint != Fc.ST && constraint != Fc.MX;
		}
		return false;
	}

	@Override
	public boolean readable() {
		return node instanceof FcModelNode;
	}

	private static BasicDataBind<?> createDataBind(BasicDataAttribute bda) {
		switch (bda.getBasicType()) {
		case BOOLEAN:
			return new BooleanDataBind((BdaBoolean) bda);
		case ENTRY_TIME:
			return new EntryTimeDataBind((BdaEntryTime) bda);
		case FLOAT32:
			return new Float32DataBind((BdaFloat32) bda);
		case FLOAT64:
			return new Float64DataBind((BdaFloat64) bda);
		case INT16:
			return new Int16DataBind((BdaInt16) bda);
		case INT16U:
			return new Int16UDataBind((BdaInt16U) bda);
		case INT32:
			return new Int32DataBind((BdaInt32) bda);
		case INT32U:
			return new Int32UDataBind((BdaInt32U) bda);
		case INT64:
			return new Int64DataBind((BdaInt64) bda);
		case INT8:
			return new Int8DataBind((BdaInt8) bda);
		case INT8U:
			return new Int8UDataBind((BdaInt8U) bda);
		case OCTET_STRING:
			return new OctetStringDataBind((BdaOctetString) bda);
		case TIMESTAMP:
			return new TimeStampDataBind((BdaTimestamp) bda);
		case UNICODE_STRING:
			return new UnicodeStringDataBind((BdaUnicodeString) bda);
		case VISIBLE_STRING:
			return new VisibleStringDataBind((BdaVisibleString) bda);
		case CHECK:
			return new CheckDataBind((BdaCheck) bda);
		case DOUBLE_BIT_POS:
			return new DoubleBitPosDataBind((BdaDoubleBitPos) bda);
		case OPTFLDS:
			return new OptfldsDataBind((BdaOptFlds) bda);
		case QUALITY:
			return new QualityDataBind((BdaQuality) bda);
		case REASON_FOR_INCLUSION:
			return new ReasonForInclusionDataBind((BdaReasonForInclusion) bda);
		case TAP_COMMAND:
			return new TapCommandDataBind((BdaTapCommand) bda);
		case TRIGGER_CONDITIONS:
			return new TriggerConditionDataBind((BdaTriggerConditions) bda);
		default:
			throw new IllegalArgumentException("BasicType " + bda.getBasicType() + " unknown");
		}
	}
}
