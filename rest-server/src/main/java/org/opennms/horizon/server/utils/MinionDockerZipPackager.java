package org.opennms.horizon.server.utils;

import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MinionDockerZipPackager {
    private static final Logger LOG = LoggerFactory.getLogger(MinionDockerZipPackager.class);

    public static byte[] generateZip(ByteString certificate, String locationName, String password) throws IOException {
        var bytesOut = new ByteArrayOutputStream();
        var zipOutStream = new ZipOutputStream(bytesOut);
        var minionName = "minion1-" + locationName;
        ZipEntry entry = new ZipEntry("storage/" + minionName + ".p12");

        zipOutStream.putNextEntry(entry);
        zipOutStream.write(certificate.toByteArray());
        zipOutStream.closeEntry();

        byte[] dockerBytes = loadDockerCompose(minionName, password);
        entry = new ZipEntry("docker-compose.yaml");
        zipOutStream.putNextEntry(entry);
        zipOutStream.write(dockerBytes);
        zipOutStream.closeEntry();

        zipOutStream.close();
        bytesOut.close();

        return bytesOut.toByteArray();
    }

    private static byte[] loadDockerCompose(String minionName, String password) throws IOException {
        String dockerCompose = System.getenv("PACKAGED_MINION_FILE");
        if (dockerCompose == null || dockerCompose.isBlank()) {
            dockerCompose = "run-minion-docker-compose.yaml";
        }
        InputStream dockerStream = MinionDockerZipPackager.class.getClassLoader()
            .getResourceAsStream(dockerCompose);
        if (dockerStream == null) {
            throw new IOException("Unable to load docker compose file from resources");
        }

        String minionEndpoint = System.getenv("MINION_ENDPOINT");
        if (minionEndpoint == null) {
            minionEndpoint = "";
        }

        String dockerTxt = new BufferedReader(new InputStreamReader(dockerStream)).lines()
            .parallel().collect(Collectors.joining("\n"));
        dockerTxt = dockerTxt.replace("[KEYSTORE_PASSWORD]", password);
        dockerTxt = dockerTxt.replace("[MINION_NAME]", minionName);
        dockerTxt = dockerTxt.replace("[MINION_ENDPOINT]", minionEndpoint);
        dockerTxt = dockerTxt.replace("[CERT_FILE]", minionName + ".p12");
        return dockerTxt.getBytes();
    }
}
