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

/**
 *
 * @author pieter.bos
 */
public class ClientServerTest {

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

        serverSap = serverSapList.get(0);
        serverSap.setPort(PORT_NUMBER);//todo: find a free port
        serverSap.setBindAddress(InetAddress.getLocalHost());
        serverSap.startListening(new TestStopListener(), new TestWriteListener());

        clientSap = new ClientSap();
		clientAssociation = clientSap.associate(InetAddress.getLocalHost(), PORT_NUMBER, null);
        writtenAttributes = new ArrayList<BasicDataAttribute>();
    }

    @After
    public void teardown() throws Exception {
        serverSap.stop();
        clientAssociation.close();
    }

    @Test
    public void testClientServerGetModel() throws Exception {


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

    class TestStopListener implements ServerStopListener {

        @Override
        public void serverStoppedListening(ServerSapConnectionHandler serverSAP) {
            //nothing to do in this test
        }

    }

    class TestWriteListener implements WriteListener {

        @Override
        public void write(BasicDataAttribute bda) throws ServiceError {
            System.out.println("WRITE REQUEST!" + bda);
            writtenAttributes.add(bda);

        }

    }
}
