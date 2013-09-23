/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850.server.security;

import java.nio.ByteBuffer;
import org.openmuc.openiec61850.ServerSap;
import org.openmuc.openiec61850.internal.acse.AcseAssociation;

/**
 *
 * @author pieter.bos
 */
public interface Authenticator {
    /**
     * Gets called whenever a new connection has been initiated.
     * Authentication can be processed here. Return the desired ServerSap on succesfull authentication.
     * Return null if authentication failed.
     *
     * It is possible with this method to return different ServerSaps based on authenticationvalue
     * @param acseAssociation
     * @param psdu
     * @return
     */
    ServerSap acceptConnection(AcseAssociation acseAssociation, ByteBuffer psdu);
}
