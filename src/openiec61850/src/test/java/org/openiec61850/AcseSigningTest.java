/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openiec61850;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmuc.openiec61850.BasicDataAttribute;
import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.ClientSap;
import org.openmuc.openiec61850.ServerModel;

import org.openmuc.openiec61850.ServerSap;
import org.openmuc.openiec61850.ServerSapConnectionHandler;
import org.openmuc.openiec61850.ServerStopListener;
import org.openmuc.openiec61850.ServiceError;
import org.openmuc.openiec61850.WriteListener;

import static org.junit.Assert.*;
import org.openmuc.openiec61850.BdaBoolean;
import org.openmuc.openiec61850.BdaFloat32;
import org.openmuc.openiec61850.BdaQuality;
import org.openmuc.openiec61850.BdaTimestamp;
import org.openmuc.openiec61850.BdaVisibleString;
import org.openmuc.openiec61850.Fc;
import org.openmuc.openiec61850.FcModelNode;
import org.openmuc.openiec61850.ModelNode;

import com.lambdaworks.crypto.SCryptUtil;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import javax.net.ServerSocketFactory;
import org.openmuc.jasn1.ber.BerIdentifier;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.types.BerGeneralizedTime;
import org.openmuc.openiec61850.internal.acse.AcseAssociation;
import org.openmuc.openiec61850.internal.acse.asn1.Authentication_value;
import org.openmuc.openiec61850.internal.acse.asn1.MMS_TLS_Authentication_value;
import org.openmuc.openiec61850.security.tls.AcseVerifier;
import org.openmuc.openiec61850.server.security.ScryptPasswordAuthenticator;
import org.openmuc.openiec61850.security.tls.TlsServerSocketFactory;
import org.openmuc.openiec61850.security.tls.TlsSocketFactory;
import org.openmuc.openiec61850.server.security.Authenticator;

/**
 *
 * @author pieter.bos
 */
public class AcseSigningTest {

    private static final String SERVER_KEYSTORE = "serverKeyStore.jks";
    private static final String SERVER_KEYSTOREPASSWORD = "keystorePassword";

    private static final String CLIENT_TRUSTSTORE = "clientTrustStore.jks";
    private static final String CLIENT_TRUSTSTOREPASSWORD = "truststorepassword";

    //for client certificate support
    private static final String CLIENT_KEYSTORE = "clientKeyStore.jks";
    private static final String CLIENT_KEYSTOREPASSWORD = "keystorePassword";
    //for client certificate support
    private static final String SERVER_TRUSTSTORE = "serverTrustStore.jks";
    private static final String SERVER_TRUSTSTOREPASSWORD = "truststorepassword";

    private static final int PORT_NUMBER = 56472;

    KeyStore clientKeystore;
    KeyStore serverTrustStore;
    KeyStore clientTrustStore;

    ServerSap serverSap;
    ClientSap clientSap;
    ClientAssociation clientAssociation;

    List<BasicDataAttribute> writtenAttributes;

    @Before
    public void setup() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/testServer.icd");
        List<ServerSap> serverSapList = ServerSap.getSapsFromSclFile(stream);
        stream.close();

        TlsServerSocketFactory serverSocketFactory = new TlsServerSocketFactory(getClass().getResourceAsStream("/" + SERVER_KEYSTORE), SERVER_KEYSTOREPASSWORD);

        serverSap = serverSapList.get(0);
        //construct with serversocketfactory
        serverSap = new ServerSap(PORT_NUMBER, 10, InetAddress.getLocalHost(), serverSap.getModelCopy(), "TestSap",
			serverSocketFactory);
        serverSap.setAuthenticator(new AcseSigningAuthenticator());

        serverSap.setPort(PORT_NUMBER);//todo: find a free port
        serverSap.setBindAddress(InetAddress.getLocalHost());
        serverSap.startListening(new TlsTestStopListener(), new TlsTestWriteListener());

        TlsSocketFactory socketFactory = new TlsSocketFactory(getClass().getResourceAsStream("/" + CLIENT_TRUSTSTORE), CLIENT_TRUSTSTOREPASSWORD);

        clientKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
        clientKeystore.load(getClass().getResourceAsStream("/" + CLIENT_KEYSTORE), CLIENT_KEYSTOREPASSWORD.toCharArray());
        serverTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        serverTrustStore.load(getClass().getResourceAsStream("/" + SERVER_TRUSTSTORE), CLIENT_TRUSTSTOREPASSWORD.toCharArray());
        clientTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        clientTrustStore.load(getClass().getResourceAsStream("/" + CLIENT_TRUSTSTORE), CLIENT_TRUSTSTOREPASSWORD.toCharArray());
        clientSap = new ClientSap(socketFactory);

        writtenAttributes = new ArrayList<BasicDataAttribute>();
    }

    @After
    public void teardown() throws Exception {
        serverSap.stop();
        if(clientAssociation != null) {
            clientAssociation.close();
        }
    }

    private Authentication_value authValue;

    @Test
    public void testVerifySignature() throws Exception {
        associateClient();
        assertNotNull(authValue);
        byte[] content = authValue.external.encoding.single_ASN1_type.getContent();
        MMS_TLS_Authentication_value signedValue = new MMS_TLS_Authentication_value();
        ByteArrayInputStream stream = new ByteArrayInputStream(content);
        BerIdentifier id = new BerIdentifier();
        id.decode(stream);
        new BerLength().decode(stream);

        signedValue.decode(stream, null);
        BerGeneralizedTime time = signedValue.certificate_based.time;
        String timeString = new String(time.octetString);
        SimpleDateFormat generalizedTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
        Date now = generalizedTimeFormat.parse(timeString);
//        assertTrue(System.currentTimeMillis() - now.getTime() < 1000);
        assertTrue(AcseVerifier.verify(serverTrustStore, signedValue));

    }

    @Test
    public void testNotInTrustStore() throws Exception {
        associateClient();
        assertNotNull(authValue);
        byte[] content = authValue.external.encoding.single_ASN1_type.getContent();
        MMS_TLS_Authentication_value signedValue = new MMS_TLS_Authentication_value();
        ByteArrayInputStream stream = new ByteArrayInputStream(content);
        BerIdentifier id = new BerIdentifier();
        id.decode(stream);
        new BerLength().decode(stream);

        signedValue.decode(stream, null);
        BerGeneralizedTime time = signedValue.certificate_based.time;
        String timeString = new String(time.octetString);
        SimpleDateFormat generalizedTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
        Date now = generalizedTimeFormat.parse(timeString);
        assertTrue(System.currentTimeMillis() - now.getTime() < 1000);
        assertFalse(AcseVerifier.verify(clientTrustStore, signedValue));

    }

 /*   @Test
    public void testWrite() throws Exception {
        associateClient();
        ServerModel serverModelFromClient = clientAssociation.retrieveModel();
        clientAssociation.getAllDataValues();
        FcModelNode node = (FcModelNode) serverModelFromClient.findModelNode("ied1lDevice1/CSWI1.Pos.Oper", Fc.CO);
        //clientAssociation.select(node);
        BdaBoolean oper = (BdaBoolean) serverModelFromClient.findModelNode("ied1lDevice1/CSWI1.Pos.Oper.ctlVal", Fc.CO);
        oper.setValue(true);
        clientAssociation.setDataValues(node);

        assertEquals(1, writtenAttributes.size());
        BdaBoolean serverSBO = (BdaBoolean) writtenAttributes.get(0);
        assertEquals(true, serverSBO.getValue());
    }
*/
    private void associateClient() throws Exception {
        clientAssociation = clientSap.associate(InetAddress.getLocalHost(), PORT_NUMBER, clientKeystore, this.CLIENT_KEYSTOREPASSWORD);
    }

    class AcseSigningAuthenticator implements Authenticator {

        @Override
        public boolean acceptConnection(AcseAssociation acseAssociation, ByteBuffer psdu) {
            //store the authentication value to test later
            authValue = acseAssociation.getAarq().calling_authentication_value;
            return true;
        }

    }

    class TlsTestStopListener implements ServerStopListener {

        @Override
        public void serverStoppedListening(ServerSapConnectionHandler serverSAP) {
            //nothing to do in this test
        }

    }

    class TlsTestWriteListener implements WriteListener {

        @Override
        public void write(BasicDataAttribute bda) throws ServiceError {
            System.out.println("WRITE REQUEST!" + bda);
            writtenAttributes.add(bda);

        }

    }
}
