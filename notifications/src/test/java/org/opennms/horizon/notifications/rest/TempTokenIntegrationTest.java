package org.opennms.horizon.notifications.rest;

import org.junit.jupiter.api.Test;
import org.opennms.horizon.notifications.NotificationsApplication;
import org.opennms.horizon.notifications.api.PagerDutyAPI;
import org.opennms.horizon.shared.dto.notifications.PagerDutyConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = NotificationsApplication.class)
@TestPropertySource(locations = "classpath:application.yml")
class TempTokenIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @SpyBean
    private PagerDutyAPI pagerDutyAPI;

    @LocalServerPort
    private Integer port;

    @Test
    void callSaveConfig() throws Exception {
        String integrationKey = "not_verified";

        saveConfig(integrationKey);
    }

    private void saveConfig(String integrationKey) {
        PagerDutyConfigDTO config = new PagerDutyConfigDTO(integrationKey);
        HttpHeaders headers = new HttpHeaders();
        String accessToken = "";
        accessToken = "";
        accessToken = "";
        accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJXMVA1TEx0cDJNNGN4NWt6dGhvV0lyaWdwakZXWEdCOGxtemxoQ3RnQV9NIn0.eyJleHAiOjE2NjQ5NzY3MTQsImlhdCI6MTY2NDk3MDcxNCwianRpIjoiZDMzOWFiYTgtYjg1OS00NmVkLTlkM2ItNDcyMjZkNDRjNjc2IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDoyODA4MC9yZWFsbXMvb3Blbm5tcyIsInN1YiI6IjhkZTNkNTkyLTNmMzEtNGVmZC04MjJiLTAyYTViNGM4ZDMxMyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFkbWluLWNsaSIsInNlc3Npb25fc3RhdGUiOiI5NzEzOTViZi1lYmI4LTRkZmUtOTJmMS1kZDU0YWY0NjQ4ZDEiLCJhY3IiOiIxIiwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSIsInNpZCI6Ijk3MTM5NWJmLWViYjgtNGRmZS05MmYxLWRkNTRhZjQ2NDhkMSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IlVzZXIwMDEiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMDAxIiwiZ2l2ZW5fbmFtZSI6IlVzZXIwMDEiLCJmYW1pbHlfbmFtZSI6IiIsImVtYWlsIjoidXNlcjAwMUB0ZXN0Lm9wZW5ubXMub3JnIn0.O9Kjyp8eXDHVlciGwjeOdtX4wPu-yZHl-OFjDDVTSAnKL5cdm40Z-zWSCDHbzduFsxNzup8kuLZPfsuhWXgzf3lvSQekrauS4tmZ-T7UPyjJcsPUVQGFCbljDPpdWZnILZtrzw2OkcHYTOVXvPJh7alDkeMhsn4O2VHseh0jyYQvhtt5Ll7zmaV1df78LWBdXtSq1TteZmgoF1aIGRzRQhyTOZ_KjcPtqCD3RkfUyPu5Gf-iromFaYrdOUdbZ4h8BokszAD-K3zOAJvfhX9dF1ot4JWtaF_LcOHkQx9ygOpmpqBoz6Rfr36SaNTeW0jhHzcqYM9ZJOgkLxSNGcVS7Q";
        headers.set("Authorization", "Bearer "+accessToken);
        HttpEntity<PagerDutyConfigDTO> request = new HttpEntity<>(config, headers);

        ResponseEntity<String> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/notifications/config", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());

        verifyConfigTable(integrationKey);
    }

    private void verifyConfigTable(String integrationKey) {
        String sql = "SELECT integrationKey FROM pager_duty_config";
        List<PagerDutyConfigDTO> configList = null;
        configList = jdbcTemplate.query(
            sql,
            (rs, rowNum) ->
                new PagerDutyConfigDTO(
                    rs.getString("integrationKey")
                )
        );

        PagerDutyConfigDTO config = configList.get(0);

        assertEquals(integrationKey, config.getIntegrationkey());
    }
}
