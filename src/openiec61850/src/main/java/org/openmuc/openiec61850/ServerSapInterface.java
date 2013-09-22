/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import javax.net.ServerSocketFactory;
import org.openmuc.openiec61850.internal.acse.ServerAcseSap;

/**
 *
 * @author pieter.bos
 */
public interface ServerSapInterface {


    byte[] getCbbBitString();

    /**
     * Gets the maximum MMS PDU size.
     *
     * @return the maximum MMS PDU size.
     */
    int getMaxMmsPduSize();


    /**
     * Gets the ProposedDataStructureNestingLevel parameter.
     *
     * @return the ProposedDataStructureNestingLevel parameter.
     */
    int getProposedDataStructureNestingLevel();

    int getProposedMaxMmsPduSize();

    int getProposedMaxServOutstandingCalling();

    int getProposedMaxServOutstandingCalled();

    /**
     * Gets the ServicesSupportedCalled parameter.
     *
     * @return the ServicesSupportedCalled parameter.
     */
    byte[] getServicesSupportedCalled();

    Timer getTimer();

    WriteListener getWriteListener();


    void addNonPersistentDataSet(DataSet dataSet, ServerAssociation connectionHandler);

    ServerModel getServerModel();


}
