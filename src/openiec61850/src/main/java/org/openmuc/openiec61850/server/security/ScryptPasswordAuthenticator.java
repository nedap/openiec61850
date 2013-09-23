/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850.server.security;

import com.lambdaworks.crypto.SCryptUtil;
import java.nio.ByteBuffer;
import org.openmuc.openiec61850.internal.acse.AcseAssociation;

/**
 * Checks a password based on an scrypt hash.
 *
 * Accepts a password hash encrypted with scrypt (maven dependency: group: 'com.lambdaworks', name: 'scrypt', version:'1.4.0')
 *
 * To generate such a hash do:
 *
 * SCryptUtil.scrypt(passwd, N, r, p)
 *
 * you could use: N=16384, r=8, p=1, but check https://github.com/wg/scrypt for documentation
 *
 * @author pieter.bos
 */
public class ScryptPasswordAuthenticator implements Authenticator {

    private String passwordHash;

    public ScryptPasswordAuthenticator(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean acceptConnection(AcseAssociation acseAssociation, ByteBuffer psdu) {
        String authenticationValue =
                new String(acseAssociation.getAarq().calling_authentication_value.charstring.octetString);
        return SCryptUtil.check(authenticationValue, passwordHash);
    }

}
