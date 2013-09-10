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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import org.openmuc.openiec61850.BdaTimestamp;
import org.openmuc.openiec61850.BdaType;
import org.openmuc.openiec61850.clientgui.BasicDataBind;

import com.toedter.calendar.JDateChooser;

public class TimeStampDataBind extends BasicDataBind<BdaTimestamp> {

	private static final Dimension DATECHOOSERDIMENSION = new Dimension(120, 20);

	private JDateChooser dateChooser;
	private JSpinner timeSpinner;

	public TimeStampDataBind(BdaTimestamp data) {
		super(data, BdaType.TIMESTAMP);
	}

	@Override
	protected JComponent init() {
		dateChooser = new JDateChooser();
		dateChooser.setDateFormatString("dd-MM-yyyy");
		dateChooser.setPreferredSize(DATECHOOSERDIMENSION);
		timeSpinner = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
		timeSpinner.setEditor(timeEditor);

		Date d = data.getDate();
		if (d == null) {
			d = new Date(0);
		}
		dateChooser.setDate(d);
		timeSpinner.setValue(d);

		JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		dateTimePanel.add(dateChooser);
		dateTimePanel.add(Box.createRigidArea(new Dimension(5, 0)));
		dateTimePanel.add(timeSpinner);
		return dateTimePanel;
	}

	@Override
	protected void resetImpl() {
		Date d = data.getDate();
		if (d == null) {
			d = new Date(0);
		}
		dateChooser.setDate(d);
		timeSpinner.setValue(d);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void writeImpl() {
		Date newDate = dateChooser.getDate();
		Date timeValues = (Date) timeSpinner.getValue();
		newDate.setHours(timeValues.getHours());
		newDate.setMinutes(timeValues.getMinutes());
		newDate.setSeconds(timeValues.getSeconds());
		data.setDate(newDate);
	}
}
