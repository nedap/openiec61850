/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850.security.tls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AlgorithmConstraints;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author pieter.bos
 */
public class TlsSocketFactory extends SSLSocketFactory {

    private static final Logger logger = LoggerFactory.getLogger(TlsSocketFactory.class);

    private SSLParameters parameters;
    private SSLContext sslContext;

    private String[] supportedProtocols;

    private SSLSocketFactory defaultFactory;

    private TrustManagerFactory tmf;

    public TlsSocketFactory(String trustStorePath, String trustStorePassword) {
        try {
            init(new FileInputStream(new File(trustStorePath)), trustStorePassword);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public TlsSocketFactory(InputStream trustStore, String trustStorePassword) {
           init(trustStore, trustStorePassword);
    }

    private void init(InputStream trustStore, String trustStorePassword) {
         try {
            try {
                sslContext = SSLContext.getInstance("TLS");
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }

            KeyStore trust = KeyStore.getInstance(KeyStore.getDefaultType());

            trust.load(trustStore, trustStorePassword.toCharArray());

            // Set up key manager factory to use our key store
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            tmf.init(trust);

            try {
                sslContext.init(null, tmf.getTrustManagers(), null);//TODO: implement a trustmanager which accepts ALL client certificates
            } catch (KeyManagementException ex) {
                throw new RuntimeException(ex);
            }

            defaultFactory = sslContext.getSocketFactory();

            parameters = sslContext.getDefaultSSLParameters();
            parameters.setNeedClientAuth(false);

            SSLEngine engine = sslContext.createSSLEngine();
            supportedProtocols = AllowedCiphers.supportedProtocols(AllowedCiphers.supportedProtocols(engine.getSupportedProtocols()));
            parameters.setCipherSuites(AllowedCiphers.supportedCipherSuites(engine.getSupportedCipherSuites()));
            parameters.setProtocols(supportedProtocols);
            //then according to IEC 62351 we can allow or disallow the connection after the ACSE AARQ.
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (CertificateException ex) {
            throw new RuntimeException(ex);
        }

    }

    public void enableClientAuth(String keyStore, String keyStorePassword) {
        parameters.setNeedClientAuth(true);
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return defaultFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return AllowedCiphers.supportedCipherSuites(defaultFactory.getSupportedCipherSuites());
    }

    public String[] getSupportedProtocols() {
        return supportedProtocols;
    }

    @Override
    public Socket createSocket(Socket socket, String string, int i, boolean bln) throws IOException {
        Socket s = defaultFactory.createSocket(socket, string, i, bln);
        return fixSocket(s);
    }

    @Override
    public Socket createSocket(String string, int i) throws IOException, UnknownHostException {
        Socket s = defaultFactory.createSocket(string, i);
        return fixSocket(s);
    }

    @Override
    public Socket createSocket(String string, int i, InetAddress ia, int i1) throws IOException, UnknownHostException {
        Socket s = defaultFactory.createSocket(string, i, ia, i1);
        return fixSocket(s);
    }

    @Override
    public Socket createSocket(InetAddress ia, int i) throws IOException {
        Socket s = defaultFactory.createSocket(ia, i);
        return fixSocket(s);
    }

    @Override
    public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException {
        Socket s = defaultFactory.createSocket(ia, i, ia1, i1);
        return fixSocket(s);
    }

   private Socket fixSocket(Socket s) {
       SSLSocket socket = (SSLSocket) s;

        //socket.setEnableSessionCreation(true); //i think this means we support renegotiation. This shall be supported!

         socket.setSSLParameters(parameters);


        return socket;
    }

}
