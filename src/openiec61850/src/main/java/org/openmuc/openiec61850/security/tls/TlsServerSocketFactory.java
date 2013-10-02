
package org.openmuc.openiec61850.security.tls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author pieter.bos
 */
public class TlsServerSocketFactory extends SSLServerSocketFactory {

    private static final Logger logger = LoggerFactory.getLogger(TlsServerSocketFactory.class);

    private SSLParameters parameters;
    private SSLContext sslContext;

    private String[] supportedProtocols;

    private SSLServerSocketFactory defaultFactory;

    private KeyManagerFactory kmf;
    private TrustManagerFactory tmf;


    private SSLServerSocketFactory getOriginalDefault() {
        return defaultFactory;
    }

    /**
     * Create a socket factory that does TLS with the given keystore file and password.
     *
     * No client certificate wanted or needed.
     * @param keystore the keystore path
     * @param keystorePassword the keystore password
     */
    public TlsServerSocketFactory(String keystore, String keystorePassword) {
        try {
            init(new FileInputStream(new File(keystore)), keystorePassword, null, null);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Create a socket factory that does TLS with the given keystore file and password.
     *
     * No client certificate wanted or needed.
     * @param keystore the keystore inputstream
     * @param keystorePassword the keystore password
     */
    public TlsServerSocketFactory(InputStream inputStream, String keystorePassword) {
        init(inputStream, keystorePassword, null, null);
    }

    /**
     * Create a socket factory that does TLS with client certificates with the given keystore file and password.
     *
     * Client certificates needed
     * @param keystore the keystore inputstream
     * @param keystorePassword the keystore password
     */
    public TlsServerSocketFactory(InputStream keystore, String keystorePassword,
            InputStream trustStore, String trustStorePassword) {
        init(keystore, keystorePassword, trustStore, trustStorePassword);
    }

    /**
     * Create a socket factory that does TLS with client certificates with the given keystore file and password.
     *
     * Client certificates needed
     * @param keystore the keystore inputstream
     * @param keystorePassword the keystore password
     */
    public TlsServerSocketFactory(String keystore, String keystorePassword,
            String trustStore, String trustStorePassword) {
        try {
            init(new FileInputStream(new File(keystore)), keystorePassword,
                    new FileInputStream(new File(trustStore)), trustStorePassword);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void init(InputStream inputStream, String keyStorePassword,
            InputStream trustStore, String trustStorePassword) {
        try {

            try {
                sslContext = SSLContext.getInstance("TLS");
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }

            KeyStore key = KeyStore.getInstance(KeyStore.getDefaultType());
            key.load(inputStream, keyStorePassword.toCharArray());
            // Set up key manager factory to use our key store
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(key, keyStorePassword.toCharArray());

            TrustManager[] trustManagers = null;
            if(trustStore != null) {
                KeyStore trust = KeyStore.getInstance(KeyStore.getDefaultType());
                trust.load(trustStore, trustStorePassword.toCharArray());
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trust);
                trustManagers = tmf.getTrustManagers();
            }
            try {
                sslContext.init(kmf.getKeyManagers(), trustManagers, null);//TODO: implement a trustmanager which accepts ALL client certificates
            } catch (KeyManagementException ex) {
                throw new RuntimeException(ex);
            }
            parameters = sslContext.getDefaultSSLParameters();

            SSLEngine engine = sslContext.createSSLEngine();

            supportedProtocols = AllowedCiphers.supportedProtocols(AllowedCiphers.supportedProtocols(engine.getSupportedProtocols()));

            //then according to IEC 62351 we can allow or disallow the connection after the ACSE AARQ.
            defaultFactory = sslContext.getServerSocketFactory();

            if(trustStore != null) {
                parameters.setNeedClientAuth(true);
            }

            parameters.setCipherSuites(AllowedCiphers.supportedCipherSuites(engine.getSupportedCipherSuites()));
            parameters.setProtocols(supportedProtocols);
            //parameters.setNeedClientAuth(false);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (CertificateException ex) {
            throw new RuntimeException(ex);
        } catch (UnrecoverableKeyException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void enableClientAuth(String trustStore, String trustStorePassword) {
        parameters.setNeedClientAuth(true);
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        parameters.setNeedClientAuth(needClientAuth);
    }

    public void setWantClientAuth(boolean wantClientAuth) {
        parameters.setWantClientAuth(wantClientAuth);
    }

    public void setSSLParameter(SSLParameters parameters) {
        this.parameters = parameters;
    }


    @Override
    public String[] getDefaultCipherSuites() {
        return getOriginalDefault().getDefaultCipherSuites();
    }

    public String[] getSupportedProtocols() {
        return supportedProtocols;
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return AllowedCiphers.supportedCipherSuites(getOriginalDefault().getSupportedCipherSuites());
    }

    @Override
    public ServerSocket createServerSocket(int i) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) getOriginalDefault().createServerSocket(i);
        fixSocket(socket);
        return socket;
    }

    @Override
    public ServerSocket createServerSocket(int i, int i1) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) getOriginalDefault().createServerSocket(i, i1);

        fixSocket(socket);
        return socket;
    }

    @Override
    public ServerSocket createServerSocket(int i, int i1, InetAddress ia) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) getOriginalDefault().createServerSocket(i, i1, ia);
        fixSocket(socket);
        return socket;
    }

    private void fixSocket(SSLServerSocket socket) {

        //the next line is th easiest, but does not work in JDK 6
        //socket.setSSLParameters(parameters);

        //this effectively does the same thing
        String[] s;
        s = parameters.getCipherSuites();
        if (s != null) {
            socket.setEnabledCipherSuites(s);
        }

        s = parameters.getProtocols();
        if (s != null) {
            socket.setEnabledProtocols(s);
        }

        if (parameters.getNeedClientAuth()) {
            socket.setNeedClientAuth(true);
        } else if (parameters.getWantClientAuth()) {
            socket.setWantClientAuth(true);
        } else {
            socket.setWantClientAuth(false);
        }

        socket.setUseClientMode(false);//we're not a client!
    }

}
