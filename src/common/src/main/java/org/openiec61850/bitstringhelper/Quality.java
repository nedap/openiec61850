/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openiec61850.bitstringhelper;

import java.util.BitSet;

/**
 *
 * @author pieter.bos
 */
public class Quality {

    byte[] bitString;

    public static final int OVERFLOW = 2;
    public static final int OUT_OF_RANGE = 3;
    public static final int BAD_REFERENCE = 4;
    public static final int OSCILLATORY = 5;
    public static final int FAILURE = 6;
    public static final int OLD_DATA = 7;
    public static final int INCONSISTENT = 8;
    public static final int INACCURATE = 9;
    public static final int SOURCE = 10;
    public static final int TEST = 11;
    public static final int OPERATOR_BLOCKED = 12;

    /**
     * Construct a default Quality bitstring
     */
    public Quality() {
        bitString = new byte[2];
        //default is 0 for everything
        bitString[0] = 0;
        bitString[1] = 0;
    }

    public Quality(byte[] bitString) {
        this.bitString = bitString;
    }

    public byte[] getValue() {
        return bitString;
    }

    /**
     * Validity is first two bits:
     * 0 0 GOOD
     * 0 1 INVALID
     * 1 0 QUESTIONABLE
     * 1 1 RESERVED
     * @return
     */
    public Validity getValidity() {
        boolean bit0 = getBit(0);
        boolean bit1 = getBit(1);
        if(bit0) {
            if(bit1) {
                return Validity.QUESTIONABLE;
            } else {
                return Validity.RESERVED;
            }
        } else {
            if(bit1) {
                return Validity.INVALID;
            } else {
                return Validity.GOOD;
            }
        }
    }

    /**
     * Validity is first two bits:
     * 0 0 GOOD
     * 0 1 INVALID
     * 1 1 QUESTIONABLE
     * 1 0 RESERVED
     * @return
     */
    public void setValidity(Validity validity) {
        switch(validity) {
            case GOOD:
                setBit(0, false);
                setBit(1, false);
                break;
            case INVALID:
                setBit(0, false);
                setBit(1, true);
                break;
            case RESERVED:
                setBit(0, true);
                setBit(1, false);
                break;
            case QUESTIONABLE:
                setBit(0, true);
                setBit(1, true);
                break;

        }
    }

    public boolean getOverflow() {
        return getBit(OVERFLOW);
    }

    public void setOverFlow(boolean value) {
        setBit(OVERFLOW, value);
    }

    public boolean getOutOfRange() {
        return getBit(OUT_OF_RANGE);
    }

    public void setOutOfRange(boolean value) {
        setBit(OUT_OF_RANGE, value);
    }

    public boolean getBadReference() {
        return getBit(BAD_REFERENCE);
    }

    public void setBadReference(boolean value) {
        setBit(BAD_REFERENCE, value);
    }

    public boolean getFailure() {
        return getBit(FAILURE);
    }

    public void setFailure(boolean value) {
        setBit(FAILURE, value);
    }

    public boolean getOldData() {
        return getBit(OLD_DATA);
    }

    public void setOldData(boolean value) {
        setBit(OLD_DATA, value);
    }

    public void setBit(int pos, boolean value) {
      int byteIndex = pos / 8;
      int bitIndex = 7 - (pos % 8);

      byte mask = (byte) (1 << bitIndex);
      byte valueBit = value ? mask : 0;

      bitString[byteIndex] = (byte) ((bitString[byteIndex] & ~mask) | valueBit);
   }

    private  boolean getBit( int pos) {
      int posByte = pos/8;
      int posBit = pos%8;
      byte valByte = bitString[posByte];
      int valInt = valByte>>(8-(posBit+1)) & 0x0001;
      return  valInt > 0;
   }

    public String toString() {
        return getValidity().name() + ":" + bitString;
    }
}
