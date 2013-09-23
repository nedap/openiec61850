/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850;

import org.openmuc.openiec61850.server.security.Authenticator;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import javax.net.ServerSocketFactory;
import org.openmuc.openiec61850.internal.acse.AcseAssociation;
import org.openmuc.openiec61850.internal.acse.AcseAssociationListener;
import org.openmuc.openiec61850.internal.acse.ServerAcseSap;

/**
 *
 * Handles lower levels of the connection stack: TCP, (possibly) TLS and Acse.
 *
 * Sets up a socket, handles Acse, then passes the connection through authentication.
 * If succesful, the connection gets handed over to the ServerSap.
 * If not succesful, the connection is closed.
 *
 * @author pieter.bos
 */
public abstract class ServerSapSelector implements AcseAssociationListener {

    private ServerSocketFactory serverSocketFactory;
    private InetAddress bindAddress;
    private int port;
    private int backlog;

    private ServerAcseSap acseSap;

    private final List<ServerAssociation> associations;
    private ServerStopListener sapStopListener;

    private Authenticator authenticator;

    /**
     * For use in ServerSap only
     * @param factory
     * @param backlog
     * @param bindAddress
     * @param port
     */
    protected ServerSapSelector(ServerSocketFactory factory, int backlog, InetAddress bindAddress, int port) {
        this(factory, backlog, bindAddress, port, null);
    }

    public ServerSapSelector(ServerSocketFactory factory, int backlog, InetAddress bindAddress, int port, Authenticator authenticator) {
        this.serverSocketFactory = factory;
        this.bindAddress = bindAddress;
        this.port = port;
        this.backlog = backlog;
        associations = new ArrayList<ServerAssociation>();
        this.authenticator = authenticator;
    }

    /**
     * Set the authenticator.
     * @param authenticator
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
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
	public void startListening(ServerStopListener sapStopListener)
			throws IOException {
		if (serverSocketFactory == null) {
			serverSocketFactory = ServerSocketFactory.getDefault();
		}
		this.acseSap = new ServerAcseSap(port, backlog, bindAddress, this, serverSocketFactory);
		this.sapStopListener = sapStopListener;
		acseSap.startListening();
	}

    @Override
    public void connectionIndication(AcseAssociation acseAssociation, ByteBuffer psdu) {
        ServerSap serverSap = authenticator.acceptConnection(acseAssociation, psdu);
        if(serverSap != null) {
            ServerAssociation association = new ServerAssociation(serverSap);
            synchronized(associations) {
                associations.add(association);
            }
            try {
                association.handleNewAssociation(acseAssociation, psdu);
            } finally {
                synchronized (associations) {
                    associations.remove(association);
                }
            }
        } else {
            acseAssociation.close();
        }
    }

    @Override
    public void serverStoppedListeningIndication(IOException e) {
        sapStopListener.serverStoppedListening(this);
    }

    /**
	 * Sets the message fragment timeout. This is the timeout that the socket timeout is set to after the first byte of
	 * a message has been received. If such a timeout is thrown, the association/socket is closed.
	 *
	 * @param timeout
	 *            the message fragment timeout in milliseconds. The default is 60000.
	 */
	public void setMessageFragmentTimeout(int timeout) {
		acseSap.serverTSap.setMessageFragmentTimeout(timeout);
	}

    /**
	 * Set the maximum number of associations that are allowed in parallel by the server.
	 *
	 * @param maxAssociations
	 *            the number of associations allowed (default is 100)
	 */
	public void setMaxAssociations(int maxAssociations) {
		acseSap.serverTSap.setMaxConnections(maxAssociations);
	}

    /**
	 * Sets local port to listen on for new connections.
	 *
	 * @param port
	 *            local port to listen on for new connections
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}



	/**
	 * Sets the maximum queue length for incoming connection indications (a request to connect) is set to the backlog
	 * parameter. If a connection indication arrives when the queue is full, the connection is refused. Set to 0 or less
	 * for the default value.
	 *
	 * @param backlog
	 *            the maximum queue length for incoming connections.
	 */
	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getBacklog() {
		return backlog;
	}

	/**
	 * Sets the local IP address to bind to, pass null to bind to all
	 *
	 * @param bindAddr
	 *            the local IP address to bind to
	 */
	public void setBindAddress(InetAddress bindAddr) {
		this.bindAddress = bindAddr;
	}

	public InetAddress getBindAddress() {
		return bindAddress;
	}

    /**
	 * Sets the factory class to generate the ServerSocket. The ServerSocketFactory could be used to create
	 * SSLServerSockets. Set to <code>null</code> to use <code>ServerSocketFactory.getDefault()</code>.
	 *
	 * @param serverSocketFactory
	 *            the factory class to generate the ServerSocket.
	 */
	public void setServerSocketFactory(ServerSocketFactory serverSocketFactory) {
		this.serverSocketFactory = serverSocketFactory;
	}

    public ServerAcseSap getAcseSap() {
        return acseSap;
    }

    public void setAcseSap(ServerAcseSap acseSap) {
        this.acseSap = acseSap;
    }

    public ServerStopListener getSapStopListener() {
        return sapStopListener;
    }

    public void setSapStopListener(ServerStopListener sapStopListener) {
        this.sapStopListener = sapStopListener;
    }

    void stop() {
        acseSap.stopListening();
        synchronized(associations) {
            for (ServerAssociation association : associations) {
                    association.stop();
            }
            associations.clear();
        }
    }

    public List<ServerAssociation> getAssociations() {
        return associations;
    }




}
