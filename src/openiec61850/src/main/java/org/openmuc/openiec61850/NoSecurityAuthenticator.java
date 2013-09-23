/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850;

import org.openmuc.openiec61850.server.security.Authenticator;
import java.nio.ByteBuffer;
import org.openmuc.openiec61850.internal.acse.AcseAssociation;

/**
 * Authenticator that does not have any security:
 * every connection is allowed.
 * Always returns the same ServerSap regardless of the authentication values.
 *
 *
 * @author pieter.bos
 */
public class NoSecurityAuthenticator implements Authenticator {

    public ServerSap serverSap;

    public NoSecurityAuthenticator(ServerSap serverSap) {
        this.serverSap = serverSap;
    }

    @Override
    public ServerSap acceptConnection(AcseAssociation acseAssociation, ByteBuffer psdu) {
        return serverSap;
    }


}
