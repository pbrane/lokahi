package org.opennms.horizon.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.grpc.stub.MetadataUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.common.VerificationException;
import org.opennms.horizon.email.proto.Email;
import org.opennms.horizon.email.proto.EmailAddresses;
import org.opennms.horizon.email.proto.EmailMessage;
import org.opennms.horizon.email.proto.EmailServiceGrpc;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CucumberContextConfiguration
@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application.yml")
@ContextConfiguration(initializers = SpringContextTestInitializer.class)
public class EmailCucumberTestSteps extends GrpcTestBase {

    private HttpClient client = HttpClientBuilder.create().build();
    private EmailServiceGrpc.EmailServiceBlockingStub serviceStub;
    private Email email;

    @Given("gRPC setup")
    public void cleanGrpc() throws VerificationException {
        prepareServer();
        serviceStub = EmailServiceGrpc.newBlockingStub(channel);
    }

    @When("an email addressed to {string} with subject {string} and body {string} is received")
    public void newEmail(String to, String subject, String body) {
        EmailAddresses addresses = EmailAddresses.newBuilder().addTo(to).build();
        EmailMessage message = EmailMessage.newBuilder().setSubject(subject).setBody(body).build();

        email = Email.newBuilder().setAddresses(addresses).setMessage(message).build();
        serviceStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(createAuthHeader(authHeader))).sendEmail(email);
    }

    @Then("the email should be sent to the SMTP server")
    public void verifyEmail() {
        // Mailhog exposes an API with all emails recieved
        HttpGet get = new HttpGet(String.format("http://%s:%d/api/v2/messages", SpringContextTestInitializer.mailhog.getHost(), SpringContextTestInitializer.getWebPort()));

        try {
            JsonNode nodes = new ObjectMapper().readTree(client.execute(get).getEntity().getContent());
            assertEquals(1, nodes.get("total").asInt());
            JsonNode content = nodes.get("items").get(0).get("Content");
            assertEquals(email.getAddresses().getToList(), StreamSupport.stream(content.get("Headers").get("To").spliterator(), false).map(JsonNode::asText).toList());
            assertEquals(email.getMessage().getSubject(), content.get("Headers").get("Subject").get(0).asText());
            assertEquals(email.getMessage().getBody(), content.get("Body").asText());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
