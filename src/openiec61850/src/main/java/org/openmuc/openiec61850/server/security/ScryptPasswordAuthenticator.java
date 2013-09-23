/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850.server.security;

import com.lambdaworks.crypto.SCryptUtil;
import org.openmuc.openiec61850.ServerSap;

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
public class ScryptPasswordAuthenticator extends PasswordAuthenticator {

    private String passwordHash;

    public ScryptPasswordAuthenticator(ServerSap sap, String passwordHash) {
        super(sap);
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean checkPassword(String authenticationValue) {
        return SCryptUtil.check(authenticationValue, passwordHash);
    }

}
