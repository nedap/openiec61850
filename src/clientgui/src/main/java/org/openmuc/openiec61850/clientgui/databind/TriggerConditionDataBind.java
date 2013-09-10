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
package org.openmuc.openiec61850.clientgui.databind;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.openmuc.openiec61850.BdaTriggerConditions;
import org.openmuc.openiec61850.BdaType;
import org.openmuc.openiec61850.clientgui.BasicDataBind;

public class TriggerConditionDataBind extends BasicDataBind<BdaTriggerConditions> {

	private final JCheckBox dataChange = new JCheckBox("DataChange");
	private final JCheckBox dataUpdate = new JCheckBox("DataUpdate");
	private final JCheckBox generalInterrogation = new JCheckBox("GeneralInterrogation");
	private final JCheckBox integrity = new JCheckBox("Integrity");
	private final JCheckBox qualityChange = new JCheckBox("QualityChange");

	public TriggerConditionDataBind(BdaTriggerConditions data) {
		super(data, BdaType.TRIGGER_CONDITIONS);
	}

	@Override
	protected JComponent init() {
		dataChange.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel valuePanel = new JPanel();
		valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.PAGE_AXIS));
		valuePanel.add(dataChange);
		valuePanel.add(dataUpdate);
		valuePanel.add(generalInterrogation);
		valuePanel.add(integrity);
		valuePanel.add(qualityChange);
		return valuePanel;
	}

	@Override
	protected void resetImpl() {
		dataChange.setSelected(data.isDataChange());
		dataUpdate.setSelected(data.isDataUpdate());
		generalInterrogation.setSelected(data.isGeneralInterrogation());
		integrity.setSelected(data.isIntegrity());
		qualityChange.setSelected(data.isQualityChange());
	}

	@Override
	protected void writeImpl() {
		data.setDataChange(dataChange.isSelected());
		data.setDataUpdate(dataUpdate.isSelected());
		data.setGeneralInterrogation(generalInterrogation.isSelected());
		data.setIntegrity(integrity.isSelected());
		data.setQualityChange(qualityChange.isSelected());
	}
}
