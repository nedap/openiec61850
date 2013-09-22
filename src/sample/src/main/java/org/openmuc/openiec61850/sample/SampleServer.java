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
package org.openmuc.openiec61850.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.openiec61850.BasicDataAttribute;
import org.openmuc.openiec61850.BdaFloat32;
import org.openmuc.openiec61850.BdaQuality;
import org.openmuc.openiec61850.BdaTimestamp;
import org.openmuc.openiec61850.Fc;
import org.openmuc.openiec61850.SclParseException;
import org.openmuc.openiec61850.ServerModel;
import org.openmuc.openiec61850.ServerSap;
import org.openmuc.openiec61850.ServerSapSelector;
import org.openmuc.openiec61850.ServerStopListener;
import org.openmuc.openiec61850.ServiceError;
import org.openmuc.openiec61850.WriteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleServer implements ServerStopListener, WriteListener {

	private final static Logger logger = LoggerFactory.getLogger(SampleServer.class);

	private static ServerSap serverSap = null;

	public static void main(String[] args) throws IOException {

		if (args.length < 1 || args.length > 2) {
			System.out.println("usage: org.openmuc.openiec61850.sample.Server <scl-file> [<port>]");
			return;
		}

		int port = 102;

		if (args.length == 2) {
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.println("usage: org.openmuc.openiec61850.sample.Server <scl-file> [<port>]");
				return;
			}
		}

		List<ServerSap> serverSaps = null;
		try {
			serverSaps = ServerSap.getSapsFromSclFile(args[0]);
		} catch (SclParseException e) {
			logger.warn("Error parsing SCL/ICD file: " + e.getMessage());
			return;
		}

		serverSap = serverSaps.get(0);
		serverSap.setPort(port);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (serverSap != null) {
					serverSap.stop();
				}
				logger.error("Server was stopped.");
			}
		});

		ServerModel serverModel = serverSap.getModelCopy();

		// create a SampleServer instance that can be passed as a callback object to startListening() and
		// setDefaultWriteListener()
		SampleServer sampleServer = new SampleServer();

		serverSap.startListening(sampleServer, sampleServer);

		BdaFloat32 totWMag = (BdaFloat32) serverModel.findModelNode("ied1lDevice1/MMXU1.TotW.mag.f", Fc.MX);
		BdaQuality q = (BdaQuality) serverModel.findModelNode("ied1lDevice1/MMXU1.TotW.q", Fc.MX);
		BdaTimestamp t = (BdaTimestamp) serverModel.findModelNode("ied1lDevice1/MMXU1.TotW.t", Fc.MX);

		List<BasicDataAttribute> totWBdas = new ArrayList<BasicDataAttribute>(3);
		totWBdas.add(totWMag);
		totWBdas.add(q);
		totWBdas.add(t);

		float totWMagVal = 0.0f;
		q.setValidity(BdaQuality.Validity.GOOD);

		while (true) {
			// if (stopped == true) {
			// break;
			// }

			totWMagVal += 1.0;

			logger.info("setting totWmag to: " + totWMagVal);
			totWMag.setFloat(totWMagVal);
			t.setCurrentTime();
			serverSap.setValues(totWBdas);

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}

	}

	@Override
	public void write(BasicDataAttribute bda) throws ServiceError {
		// calling of this method still has to be implemented
		logger.info("got a write request: " + bda);

	}

    @Override
    public void serverStoppedListening(ServerSapSelector serverSAP) {
        logger.error("The SAP stopped listening");
    }

}
