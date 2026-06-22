package com.certak.ghcpmgmt.config;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loads custom certificates from the {@code certs/} folder (sibling to {@code .ghcp-mgmt.properties})
 * and installs them into the default JVM SSL context so all HTTPS connections trust them.
 *
 * <p>Supports {@code .cer} and {@code .crt} files (PEM or DER encoded X.509).
 */
public final class CertLoader {

    private CertLoader() {}

    public static void installFromHome() {
        String home = System.getProperty("ghcp-mgmt.home");
        Path certsDir = (home != null && !home.isBlank())
                ? Path.of(home, "certs")
                : Path.of("certs");

        if (!Files.isDirectory(certsDir)) return;

        List<Path> certFiles;
        try (Stream<Path> stream = Files.list(certsDir)) {
            certFiles = stream
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".cer") || name.endsWith(".crt");
                    })
                    .sorted()
                    .toList();
        } catch (Exception e) {
            System.err.println("  Certs: failed to list certs dir: " + e.getMessage());
            return;
        }

        if (certFiles.isEmpty()) return;

        System.err.println("  Certs: loading " + certFiles.size() + " certificate(s) from " + certsDir);

        try {
            KeyStore keyStore = loadDefaultTrustStore();

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (Path certFile : certFiles) {
                try (InputStream is = Files.newInputStream(certFile)) {
                    Certificate cert = cf.generateCertificate(is);
                    keyStore.setCertificateEntry(certFile.getFileName().toString(), cert);
                    System.err.println("    Trusted: " + certFile.getFileName());
                } catch (Exception e) {
                    System.err.println("    Skipped: " + certFile.getFileName() + " (" + e.getMessage() + ")");
                }
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);

        } catch (Exception e) {
            System.err.println("  Certs: failed to install certificates: " + e.getMessage());
        }
    }

    private static KeyStore loadDefaultTrustStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        String customPath = System.getProperty("javax.net.ssl.trustStore");
        if (customPath != null) {
            char[] password = System.getProperty("javax.net.ssl.trustStorePassword", "changeit").toCharArray();
            try (InputStream is = Files.newInputStream(Path.of(customPath))) {
                keyStore.load(is, password);
            }
            return keyStore;
        }

        Path cacerts = Path.of(System.getProperty("java.home"), "lib", "security", "cacerts");
        try (InputStream is = Files.newInputStream(cacerts)) {
            keyStore.load(is, "changeit".toCharArray());
        }
        return keyStore;
    }
}
