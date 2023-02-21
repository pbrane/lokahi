package org.opennms.horizon.minion.snmp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;
import org.opennms.horizon.shared.snmp.conf.xml.SnmpConfig;

import junit.framework.TestCase;


public class SnmpPeerFactoryTest extends TestCase {

    @Test
    public void testSnmpPeerFactory() throws IOException {
        // Create a temporary file with the JSON content
        String jsonContent = "{\n" +
            "  \"version\":\"v2c\",\n" +
            "  \"read-community\":\"public\",\n" +
            "  \"timeout\":1800,\n" +
            "  \"retry\":1\n" +
            "}";
        File tempFile = File.createTempFile("mytest", ".json");
        Files.writeString(tempFile.toPath(), jsonContent);

        // Load the SnmpConfig object from the file
        SnmpPeerFactory test = new SnmpPeerFactory(tempFile);
        SnmpConfig snmpConfig = test.getSnmpConfig();

        // Check that the properties are set correctly
        assertEquals("v2c", snmpConfig.getVersion());
        assertEquals("public", snmpConfig.getReadCommunity());
        assertEquals(Integer.valueOf(1800), snmpConfig.getTimeout());
        assertEquals(Integer.valueOf(1), snmpConfig.getRetry());

        // Clean up the temporary file
        tempFile.delete();
    }
}
