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
package org.openmuc.openiec61850;

import org.openmuc.openiec61850.server.security.NoSecurityAuthenticator;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Timer;

import javax.net.ServerSocketFactory;

/**
 * The <code>ServerSap</code> class represents the IEC 61850 service access point for server applications. It
 * corresponds to the AccessPoint defined in the ICD/SCL file. A server application that is to listen for client
 * connections should first get an instance of <code>ServerSap</code> using the static function
 * ServerSap.getSapsFromSclFile(). Next all the necessary configuration parameters can be set. Finally the
 * <code>startListening</code> function is called to listen for client associations. Changing properties of a ServerSap
 * after starting to listen is not recommended and has unknown effects.
 */
public final class ServerSap {

	static final int MINIMUM_MMS_PDU_SIZE = 64;
	private static final int MAXIMUM_MMS_PDU_SIZE = 65000;

	private int proposedMaxMmsPduSize = 65000;
	private int proposedMaxServOutstandingCalling = 5;
	private int proposedMaxServOutstandingCalled = 5;
	private int proposedDataStructureNestingLevel = 10;
	private byte[] servicesSupportedCalled = new byte[] { (byte) 0xee, 0x1c, 0, 0, 0x04, 0x08, 0, 0, 0x79, (byte) 0xef, 0x18 };
	private byte[] cbbBitString = { (byte) (0xfb), 0x00 };

	private final String name;

	private Timer timer;

	private final ServerModel serverModel;
	private WriteListener writeListener;

    private ServerSapConnectionHandler connectionHandler;

	public static List<ServerSap> getSapsFromSclFile(String sclFilePath) throws SclParseException {
		SclParser sclParserObject = new SclParser();
		sclParserObject.parse(sclFilePath);
		return sclParserObject.getServerSaps();
	}

    public static List<ServerSap> getSapsFromSclFile(InputStream inputStream) throws SclParseException {
		SclParser sclParserObject = new SclParser();
		sclParserObject.parse(inputStream);
		return sclParserObject.getServerSaps();
	}

	/**
	 * Creates a ServerSap.
	 *
	 * @param port
	 *            local port to listen on for new connections
	 * @param backlog
	 *            The maximum queue length for incoming connection indications (a request to connect) is set to the
	 *            backlog parameter. If a connection indication arrives when the queue is full, the connection is
	 *            refused. Set to 0 or less for the default value.
	 * @param bindAddr
	 *            local IP address to bind to, pass null to bind to all
	 * @param serverSocketFactory
	 *            the factory class to generate the ServerSocket. Could be used to create SSLServerSockets. null =
	 *            default
	 *
	 */
	public ServerSap(int port, int backlog, InetAddress bindAddr, ServerModel serverModel, String name,
			ServerSocketFactory serverSocketFactory) {
        connectionHandler = new ServerSapConnectionHandler(serverSocketFactory, backlog, bindAddr, port, this,
                new NoSecurityAuthenticator());
		this.name = name;
		this.serverModel = serverModel;
	}

    /**
     * Creates a ServerSap, without network information.
     * To use this as an actual server, manage the ServerSapConnectionHandler yourself.
     * This is not required for most implementations.
     */
    public ServerSap(ServerModel serverModel, String name) {
        this.name = name;
        this.serverModel = serverModel;
    }

	/**
	 * Returns the name of the ServerSap / AccessPoint as specified in the SCL file.
	 *
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the maximum MMS PDU size in bytes that the server will support. If the client requires the use of a smaller
	 * maximum MMS PDU size, then the smaller size will be accepted by the server. The default size is 65000.
	 *
	 * @param size
	 *            cannot be less than 64. The upper limit is 65000 so that segmentation at the lower transport layer is
	 *            avoided. The Transport Layer's maximum PDU size is 65531.
	 */
	public void setMaxMmsPduSize(int size) {
		if (size >= MINIMUM_MMS_PDU_SIZE && size <= MAXIMUM_MMS_PDU_SIZE) {
			proposedMaxMmsPduSize = size;
		}
		else {
			throw new IllegalArgumentException("maximum size is out of bound");
		}
	}

	/**
	 * Gets the maximum MMS PDU size.
	 *
	 * @return the maximum MMS PDU size.
	 */
	public int getMaxMmsPduSize() {
		return proposedMaxMmsPduSize;
	}


	/**
	 * Sets the ProposedMaxServOutstandingCalling parameter. The given parameter has no affect on the functionality of
	 * this server.
	 *
	 * @param maxCalling
	 *            the ProposedMaxServOutstandingCalling parameter. The default is 5.
	 */
	public void setProposedMaxServOutstandingCalling(int maxCalling) {
		proposedMaxServOutstandingCalling = maxCalling;
	}

	/**
	 * Gets the ProposedMaxServOutstandingCalling parameter.
	 *
	 * @return the ProposedMaxServOutstandingCalling parameter.
	 */
	public int getProposedMaxServOutstandingCalling() {
		return proposedMaxServOutstandingCalling;
	}

	/**
	 * Sets the ProposedMaxServOutstandingCalled parameter.The given parameter has no affect on the functionality of
	 * this server.
	 *
	 * @param maxCalled
	 *            the ProposedMaxServOutstandingCalled parameter. The default is 5.
	 */
	public void setProposedMaxServOutstandingCalled(int maxCalled) {
		proposedMaxServOutstandingCalled = maxCalled;
	}

	/**
	 * Gets the ProposedMaxServOutstandingCalled parameter.
	 *
	 * @return the ProposedMaxServOutstandingCalled parameter.
	 */
	public int getProposedMaxServOutstandingCalled() {
		return proposedMaxServOutstandingCalled;
	}

	/**
	 * Sets the ProposedDataStructureNestingLevel parameter. The given parameter has no affect on the functionality of
	 * this server.
	 *
	 * @param nestingLevel
	 *            the ProposedDataStructureNestingLevel parameter. The default is 10.
	 */
	public void setProposedDataStructureNestingLevel(int nestingLevel) {
		proposedDataStructureNestingLevel = nestingLevel;
	}

	/**
	 * Gets the ProposedDataStructureNestingLevel parameter.
	 *
	 * @return the ProposedDataStructureNestingLevel parameter.
	 */
	public int getProposedDataStructureNestingLevel() {
		return proposedDataStructureNestingLevel;
	}

	/**
	 * Sets the SevicesSupportedCalled parameter. The given parameter has no affect on the functionality of this server.
	 *
	 * @param services
	 *            the ServicesSupportedCalled parameter
	 */
	public void setServicesSupportedCalled(byte[] services) {
		if (services.length != 11) {
			throw new IllegalArgumentException("The services parameter needs to be of lenth 11");
		}
		servicesSupportedCalled = services;
	}

	/**
	 * Gets the ServicesSupportedCalled parameter.
	 *
	 * @return the ServicesSupportedCalled parameter.
	 */
	public byte[] getServicesSupportedCalled() {
		return servicesSupportedCalled;
	}

	/**
	 * Creates a server socket waiting on the configured port for incoming association requests.
	 *
	 * @param sapStopListener
	 *            the listener that is notified when the server stopped listening for some reason.
	 * @param defaultWriteListener
	 *            the default listener that is notified of incoming write (setDataValues/control) requests.
	 * @throws IOException
	 *             if an error occurs binding to the port.
	 */
	public void startListening(ServerStopListener sapStopListener, WriteListener defaultWriteListener)
			throws IOException {
		timer = new Timer();
        if(this.connectionHandler != null) {
            connectionHandler.startListening(sapStopListener);
        }
		writeListener = defaultWriteListener;
	}

	/**
	 * Stops listening for new connections and closes all existing connections/associations.
	 */
	public void stop() {
		if(connectionHandler != null) {
            connectionHandler.stop();
        }
	}

	protected void addNonPersistentDataSet(DataSet dataSet, ServerAssociation connectionHandler) {
		// TODO Auto-generated method stub
	}

	public ServerModel getModelCopy() {
		return serverModel.copy();
	}

	public void setValues(List<BasicDataAttribute> bdas) {
		synchronized (serverModel) {
			for (BasicDataAttribute bda : bdas) {
				// if (bda.getFunctionalConstraint() != FunctionalConstraint.ST) {
				// logger.debug("fc:" + bda.getFunctionalConstraint());
				// throw new IllegalArgumentException(
				// "One can only set values of BDAs with Functional Constraint ST(status)");
				// }
				bda.getMirror().setValueFrom(bda);
			}
		}
	}

     public static int getMINIMUM_MMS_PDU_SIZE() {
        return MINIMUM_MMS_PDU_SIZE;
    }

    public static int getMAXIMUM_MMS_PDU_SIZE() {
        return MAXIMUM_MMS_PDU_SIZE;
    }


    public int getProposedMaxMmsPduSize() {
        return proposedMaxMmsPduSize;
    }

    public byte[] getCbbBitString() {
        return cbbBitString;
    }

    protected Timer getTimer() {
        return timer;
    }

    public boolean isListening() {
        return connectionHandler != null && connectionHandler.isListening();
    }

    protected ServerModel getServerModel() {
        return serverModel;
    }

    public WriteListener getWriteListener() {
        return writeListener;
    }

    public void setPort(int port) {
        if(connectionHandler != null) {
            connectionHandler.setPort(port);
        } else {
            throw new IllegalStateException("Cannot set a port for a ServerSap which has no connectionHandler");
        }
    }

    public int getPort() {
        if(connectionHandler != null) {
            return connectionHandler.getPort();
        }
        throw new IllegalStateException("Cannot return a port for a ServerSap which has no connectionHandler");
    }

    public InetAddress getBindAddress() {
        if(connectionHandler != null) {
            return connectionHandler.getBindAddress();
        }
        throw new IllegalStateException("Cannot return a bind address for a ServerSap which has no connectionHandler");
    }

    public void setBindAddress(InetAddress bindAddress) {
        if(connectionHandler != null) {
            connectionHandler.setBindAddress(bindAddress);
        } else {
            throw new IllegalStateException("Cannot set a bind address for a ServerSap which has no connectionHandler");
        }
    }
}
