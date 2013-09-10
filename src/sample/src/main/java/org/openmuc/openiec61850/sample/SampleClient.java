package org.openmuc.openiec61850.sample;

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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openmuc.openiec61850.BdaFloat32;
import org.openmuc.openiec61850.BdaQuality;
import org.openmuc.openiec61850.BdaTimestamp;
import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.ClientSap;
import org.openmuc.openiec61850.Fc;
import org.openmuc.openiec61850.FcModelNode;
import org.openmuc.openiec61850.ServerModel;
import org.openmuc.openiec61850.ServiceError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class SampleClient {

	private final static Logger logger = LoggerFactory.getLogger(SampleClient.class);

	public static void main(String[] args) throws ServiceError, IOException {

		String usageString = "usage: org.openmuc.openiec61850.sample.SampleClient <host> <port>";

		if (args.length != 2) {
			System.out.println(usageString);
			return;
		}

		String remoteHost = args[0];
		InetAddress address;
		try {
			address = InetAddress.getByName(remoteHost);
		} catch (UnknownHostException e) {
			logger.error("Unknown host: " + remoteHost);
			return;
		}

		int remotePort;
		try {
			remotePort = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println(usageString);
			return;
		}

		ClientSap clientSap = new ClientSap();
		// alternatively you could use ClientSap(SocketFactory factory) to e.g. connect using SSL

		// optionally you can set some association parameters (but usually the default should work):
		// clientSap.setTSelRemote(new byte[] { 0, 1 });
		// clientSap.setTSelLocal(new byte[] { 0, 0 });

		ClientAssociation association;

		logger.info("Attempting to connect to server " + remoteHost + " on port " + remotePort);
		try {
			association = clientSap.associate(address, remotePort, null);
		} catch (IOException e) {
			// an IOException will always indicate a fatal exception. It indicates that the association was closed and
			// cannot be recovered. You will need to create a new association using ClientSap.associate() in order to
			// reconnect.
			logger.error("Error connecting to server: " + e.getMessage());
			return;
		}

		ServerModel serverModel;
		try {
			// requestModel() will call all GetDirectory and GetDefinition ACSI services needed to get the complete
			// server model
			serverModel = association.retrieveModel();
		} catch (ServiceError e) {
			logger.error("Service Error requesting model.", e);
			association.close();
			return;
		} catch (IOException e) {
			logger.error("Fatal IOException requesting model.", e);
			return;
		}

		// instead of calling retrieveModel you could read the model directly from an SCL file:
		// try {
		// serverNode = association.getModelFromSclFile("../sampleServer/sampleModel.icd");
		// } catch (SclParseException e1) {
		// logger.error("Error parsing SCL file.", e1);
		// return;
		// }

		// get the values of all data attributes in the model:
		association.getAllDataValues();

		// example for writing a variable:
		FcModelNode modCtlModel = (FcModelNode) serverModel.findModelNode("ied1lDevice1/CSWI1.Mod.ctlModel", Fc.CF);
		association.setDataValues(modCtlModel);

		// example for reading a variable:
		FcModelNode totW = (FcModelNode) serverModel.findModelNode("ied1lDevice1/MMXU1.TotW", Fc.MX);
		BdaFloat32 totWmag = (BdaFloat32) totW.getChild("mag").getChild("f");
		BdaTimestamp totWt = (BdaTimestamp) totW.getChild("t");
		BdaQuality totWq = (BdaQuality) totW.getChild("q");

		while (true) {
			association.getDataValues(totW);
			logger.info("got totW: mag " + totWmag.getFloat() + ", time " + totWt.getDate() + ", quality "
					+ totWq.getValidity());

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}

		}

	}

}
