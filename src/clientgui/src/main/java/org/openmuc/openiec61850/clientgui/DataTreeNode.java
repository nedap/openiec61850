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

import javax.swing.tree.TreeNode;

import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.ServiceError;

public interface DataTreeNode {

	public abstract BasicDataBind<?> getData();

	public abstract void reset(ClientAssociation association) throws ServiceError, IOException;

	public abstract void writeValues(ClientAssociation association) throws ServiceError, IOException;

	public abstract int getChildCount();

	public abstract TreeNode getChildAt(int index);

	public abstract boolean writable();

	public abstract boolean readable();
}
