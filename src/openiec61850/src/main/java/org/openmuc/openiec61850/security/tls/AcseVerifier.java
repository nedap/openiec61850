/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmuc.openiec61850.security.tls;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.SimpleTimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import org.openmuc.jasn1.ber.types.BerGeneralizedTime;
import org.openmuc.jasn1.ber.types.BerOctetString;
import org.openmuc.openiec61850.internal.acse.asn1.MMS_TLS_Authentication_value;

/**
 *
 * @author pieter.bos
 */
public class AcseVerifier {


    public static boolean verify(KeyStore trustStore, MMS_TLS_Authentication_value value) {
        try {
            BerGeneralizedTime time = value.certificate_based.time;

            Date timestamp = parseTime(time.octetString);
            if(Math.abs(System.currentTimeMillis() - timestamp.getTime()) > 10*60*1000) {
                //time out of range. TODO: log, add reason
                return false;
            }

            byte[] signature = value.certificate_based.signature.octetString;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(value.certificate_based.authentication_Certificate.octetString));
            Signature s = Signature.getInstance("SHA1withRSA");
            s.initVerify(certificate);
            s.update(time.octetString);
            if(!s.verify(signature)) {
                return false;
            }
            String alias = trustStore.getCertificateAlias(certificate);
            return alias != null;

        } catch (ParseException ex) {
            //TODO: log. time format not current, or we do not have the correct generalized time yet
            Logger.getLogger(AcseVerifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AcseVerifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.security.cert.CertificateException ex) {
            Logger.getLogger(AcseVerifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AcseVerifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(AcseVerifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(AcseVerifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static KeyStore.PrivateKeyEntry getRSAPrivateKeyEntry(KeyStore keyStore, String keystorePassword) throws KeyStoreException {

        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keystorePassword.toCharArray());

        Enumeration<String> aliases = keyStore.aliases();
        while(aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if(keyStore.isKeyEntry(alias)) {
                try {
                    KeyStore.Entry entry = keyStore.getEntry(alias, protParam);
                    if(entry instanceof KeyStore.PrivateKeyEntry) {
                        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
                        PrivateKey privateKey = privateKeyEntry.getPrivateKey();
                        if(privateKey.getAlgorithm().equalsIgnoreCase("RSA") && privateKeyEntry.getCertificate() instanceof java.security.cert.X509Certificate) {
                            return privateKeyEntry;
                        }
                    }
                } catch (NoSuchAlgorithmException ex) {
                   //TODO: log. not important, just means we have keys we cannot use
                } catch (UnrecoverableEntryException ex) {
                   //TODO: log. not important, just means we have an entry we cannot use
                }
            }
        }
        return null;
    }

    public static MMS_TLS_Authentication_value sign(KeyStore keystore, String keystorePassword) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, CertificateEncodingException, javax.security.cert.CertificateEncodingException {
            KeyStore.PrivateKeyEntry privateKeyEntry = null;

            try{
                 privateKeyEntry = getRSAPrivateKeyEntry(keystore, keystorePassword);
            } catch (KeyStoreException ex) {
                 throw new RuntimeException("error accessing keystore: ", ex);
            }

            if(privateKeyEntry == null) {
                throw new IllegalArgumentException("keystore contains no private RSA key to use, or no X509Certificate. We need both.");
            }
            return sign(privateKeyEntry.getCertificate(), privateKeyEntry.getPrivateKey());
    }

    public static MMS_TLS_Authentication_value sign(Certificate cert, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, CertificateEncodingException, javax.security.cert.CertificateEncodingException {

        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);

        SimpleDateFormat generalizedTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
        byte[] generalizedTime = generalizedTimeFormat.format(new Date()).getBytes();
        BerGeneralizedTime time = new BerGeneralizedTime(generalizedTime);
        signature.update(generalizedTime);
        //byte[] octets = {1, 2, 3, 4, 5, 6, 7, 8, 9};
       // signature.sign();

        BerOctetString berSignature = new BerOctetString(signature.sign());
        BerOctetString certificate = new BerOctetString(cert.getEncoded());

        MMS_TLS_Authentication_value.SubSeq_certificate_based certBased =
                new MMS_TLS_Authentication_value.SubSeq_certificate_based(certificate, time, berSignature);
        return new MMS_TLS_Authentication_value(certBased);
    }

    private static Date parseTime(byte[] timeOctets) throws ParseException {
        String timeString = new String(timeOctets);
        SimpleDateFormat dateF;

        if (timeString.indexOf('.') == 14)
        {
            dateF = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
        }
        else
        {
            dateF = new SimpleDateFormat("yyyyMMddHHmmssZ");
        }
        dateF.setTimeZone(new SimpleTimeZone(0, "Z"));//Z means no timezone
        return dateF.parse(timeString);
    }
}
