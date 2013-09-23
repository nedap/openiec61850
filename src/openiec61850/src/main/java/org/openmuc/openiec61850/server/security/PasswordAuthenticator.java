/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850.server.security;

import org.openmuc.openiec61850.server.security.Authenticator;
import java.nio.ByteBuffer;
import org.openmuc.openiec61850.internal.acse.AcseAssociation;

import org.openmuc.openiec61850.ServerSap;

/**
 * Authenticator that only allows a connection if the specified authenticationValue is passed in the AARQ.
 *
 *  implement a subclass to use, with the checkPassword callback
 *
 * @author pieter.bos
 */
public abstract class PasswordAuthenticator implements Authenticator {

    private ServerSap serverSap;

    /**
     * Construct the password authenticator with an scrypt hash of the password.
     *
     * @param serverSap
     * @param bcryptHash
     */
    public PasswordAuthenticator(ServerSap serverSap) {
        this.serverSap = serverSap;
    }

    @Override
    public ServerSap acceptConnection(AcseAssociation acseAssociation, ByteBuffer psdu) {
        String authenticationValue = new String(acseAssociation.getAarq().calling_authentication_value.charstring.octetString);

        if(checkPassword(authenticationValue)) {
            return serverSap;
        } else {
            return null;
        }
    }

    /**
     * The checkpassword callback. return true if password correct, false otherwise
     *
     * @param authenticationValue
     * @return
     */
    protected abstract boolean checkPassword(String authenticationValue);

}
