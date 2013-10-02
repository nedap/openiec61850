package org.openmuc.openiec61850.security.tls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class AllowedCiphers {

    public static final String[] ENABLED_PROTOCOLS = new String[] {
        "TLSv1", //only tls 1.0 and higher shall be allowable, according to 62351-3
        "TLSv1.1",
        "TLSv1.2",
    };

    public static final String[] ENABLED_CIPHER_SUITES = new String[] {
        "TLS_DH_DSS_WITH_AES_256_SHA", /** This is listed in the standard, BUT no one else uses this notation. Instead:*/
        "TLS_DH_DSS_WITH_AES_256_CBC_SHA256", //this one is used
        "TLS_RSA_WITH_AES_256_CBC_SHA",
        "TLS_RSA_WITH_AES_128_CBC_SHA", //mandatory according to TLS 1.2
        "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
        "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
        "TLS_RSA_WITH_AES_256_SHA"
    };


    public static String[] supportedProtocols(String[] availableProtocols) {
        return intersection(ENABLED_PROTOCOLS, availableProtocols);
    }

    public static String[] supportedCipherSuites(String[] availableCiphers) {
        return intersection(ENABLED_CIPHER_SUITES, availableCiphers);
    }

    private static String[] intersection(String[] a1, String[] a2) {
        Set<String> s1 = new HashSet<String>(Arrays.asList(a1));
        s1.retainAll(Arrays.asList(a2));
        return s1.toArray(new String[0]);
    }

}