package org.openiec61850.sample;

/*
 * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2011
 *
 * This file is part of openIEC61850.
 * For more information visit http://www.openmuc.org
 *
 * openIEC61850 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * openIEC61850 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with openIEC61850.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openiec61850.AccessPoint;
import org.openiec61850.BasicDataAttribute;
import org.openiec61850.ModelNode;
import org.openiec61850.ObjectReference;
import org.openiec61850.ServiceError;
import org.openiec61850.server.data.DataSource;
import org.openiec61850.ConnectionHandler;
import org.openiec61850.jmms.iso.acse.AcseAssociation;

/**
 * This {@link DummyDataSource} stores written values in a simple HashMap and
 * returns them when they are read. Default values are returned for
 * BasicDataAttributes that have never been written.
 *
 */
public class DummyDataSource implements DataSource {

	protected Map<ObjectReference, Object> valueStore = new LinkedHashMap<ObjectReference, Object>();

    @Override
	public void readValues(List<BasicDataAttribute> basicDataAttributes, AcseAssociation association) throws ServiceError {
        System.out.println(association.getAuthenticationValue());
		for (BasicDataAttribute basicDataAttribute : basicDataAttributes) {
			Object value = valueStore.get(basicDataAttribute.getReference());
			if (value != null) {
				basicDataAttribute.setValue(value);
			}
			// will not be null if a Default value was configured in the SCL
			// file
			else if (basicDataAttribute.getValue() == null) {
				basicDataAttribute.setDefault();
			}
		}
	}

    @Override
	public void writeValues(List<BasicDataAttribute> basicDataAttributes, AcseAssociation association) throws ServiceError {
		for (BasicDataAttribute bda : basicDataAttributes) {
			valueStore.put(bda.getReference(), bda.getValue());
		}
	}

	public void run() {

	}

    @Override
	public void initialize(AccessPoint ac, Set<String> triggerDchg, Set<String> triggerQchg, Set<String> triggerDupd) {
	}
}
