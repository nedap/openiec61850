/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850.server.security;

import java.nio.ByteBuffer;
import org.openmuc.openiec61850.ServerSap;
import org.openmuc.openiec61850.internal.acse.AcseAssociation;

/**
 * Simple authentication support. Return true or false, based on the ACSE AARQ.
 *
 * This works fine for servers which have the same servermodel for every connection, eg. probably most devices.
 *
 * @author pieter.bos
 */
public interface Authenticator extends BaseAuthenticator {

    /**
     * Gets called whenever a new connection has been initiated.
     * return true if connection is allowed, false otherwise.
     * If false, the connection will be closed. If true, the connection will be handed over to the ServerSap
     *
     * @param acseAssociation
     * @param psdu
     * @return
     */
    boolean acceptConnection(AcseAssociation acseAssociation, ByteBuffer psdu);
}
