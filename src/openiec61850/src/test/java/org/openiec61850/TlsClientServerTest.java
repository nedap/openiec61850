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
import java.io.EOFException;
import javax.net.ServerSocketFactory;
import org.openmuc.openiec61850.server.security.ScryptPasswordAuthenticator;
import org.openmuc.openiec61850.security.tls.TlsServerSocketFactory;
import org.openmuc.openiec61850.security.tls.TlsSocketFactory;

/**
 *
 * @author pieter.bos
 */
public class TlsClientServerTest {

    private static final String SERVER_KEYSTORE = "serverKeyStore.jks";
    private static final String SERVER_KEYSTOREPASSWORD = "keystorePassword";

    private static final String CLIENT_TRUSTSTORE = "clientTrustStore.jks";
    private static final String CLIENT_TRUSTSTOREPASSWORD = "truststorepassword";

    private static final int PORT_NUMBER = 56472;

    ServerSap serverSap;
    ClientSap clientSap;
    ClientAssociation clientAssociation;

    List<BasicDataAttribute> writtenAttributes;

    @Before
    public void setup() throws Exception {
        InputStream stream= getClass().getResourceAsStream("/testServer.icd");
        List<ServerSap> serverSapList = ServerSap.getSapsFromSclFile(stream);
        stream.close();

        TlsServerSocketFactory serverSocketFactory = new TlsServerSocketFactory(getClass().getResourceAsStream("/" + SERVER_KEYSTORE), SERVER_KEYSTOREPASSWORD);

        serverSap = serverSapList.get(0);
        //construct with serversocketfactory
        serverSap = new ServerSap(PORT_NUMBER, 10, InetAddress.getLocalHost(), serverSap.getModelCopy(), "TestSap",
			serverSocketFactory);

        serverSap.setPort(PORT_NUMBER);//todo: find a free port
        serverSap.setBindAddress(InetAddress.getLocalHost());
        serverSap.startListening(new TlsClientServerTest.TlsTestStopListener(), new TlsClientServerTest.TlsTestWriteListener());

        TlsSocketFactory socketFactory = new TlsSocketFactory(getClass().getResourceAsStream("/" + CLIENT_TRUSTSTORE), CLIENT_TRUSTSTOREPASSWORD);
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

    @Test
    public void testClientServerGetModel() throws Exception {
        associateClient();

        Date now = new Date();
        {
            ServerModel serverModelFromServer = serverSap.getModelCopy();

            BdaFloat32 totWMag = (BdaFloat32) serverModelFromServer.findModelNode("ied1lDevice1/MMXU1.TotW.mag.f", Fc.MX);
            BdaQuality q = (BdaQuality) serverModelFromServer.findModelNode("ied1lDevice1/MMXU1.TotW.q", Fc.MX);
            BdaTimestamp t = (BdaTimestamp) serverModelFromServer.findModelNode("ied1lDevice1/MMXU1.TotW.t", Fc.MX);
            totWMag.setFloat(10.5f);
            q.setTest(true);

            t.setDate(now);

            List<BasicDataAttribute> totWBdas = new ArrayList<BasicDataAttribute>(3);
            totWBdas.add(totWMag);
            totWBdas.add(q);
            totWBdas.add(t);

            serverSap.setValues(totWBdas);
        }


        {
            ServerModel serverModelFromClient = clientAssociation.retrieveModel();
            clientAssociation.getAllDataValues();

            BdaFloat32 totWMag = (BdaFloat32) serverModelFromClient.findModelNode("ied1lDevice1/MMXU1.TotW.mag.f", Fc.MX);
            BdaQuality q = (BdaQuality) serverModelFromClient.findModelNode("ied1lDevice1/MMXU1.TotW.q", Fc.MX);
            BdaTimestamp t = (BdaTimestamp) serverModelFromClient.findModelNode("ied1lDevice1/MMXU1.TotW.t", Fc.MX);
            assertEquals(10.5f, totWMag.getFloat().floatValue(), 0.05f);
            assertEquals(true, q.isTest());
            assertEquals(now, t.getDate());
        }
    }

    @Test
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

    private void associateClient() throws Exception {
        clientAssociation = clientSap.associate(InetAddress.getLocalHost(), PORT_NUMBER, null);
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
