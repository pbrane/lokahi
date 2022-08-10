package org.opennms.horizon.shared.opentelemetry.itests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.debugConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import javax.inject.Inject;
import org.apache.karaf.features.BootFinished;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionBaseConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@RunWith(PaxExam.class)
public class MetricsTest {

    public static final String METRIC_NAME = "a_sample_meter";
    public static final String ATTRIBUTE_NAME = "process";
    public static final String ATTRIBUTE_VALUE = "process-value";
    public static final String COUNTER_NAME = "foo";

    @Inject
    private BootFinished bootFinished;

    @Inject
    private FeaturesService featuresService;

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] configuration() {
        KarafDistributionBaseConfigurationOption distribution = new KarafDistributionConfigurationOption().unpackDirectory(new File("target/exam"))
            .frameworkUrl(maven("org.apache.karaf", "apache-karaf").type("zip").versionAsInProject());
        KarafFeaturesOption bootFeatures = new KarafFeaturesOption(
            maven("org.opennms.horizon.shared.opentelemetry", "features").classifier("features").type("xml").versionAsInProject(),
            "opentelemetry-prometheus"
        );
        return new Option[] {
            distribution,
            keepRuntimeFolder(),
            bootFeatures,
            // logLevel(LogLevel.DEBUG),
            // if you ever need to figure out what your test is doing, this line will cause it waiting for debugger on port 5005
            //debugConfiguration()
        };
    }

    @Test
    public void verifyMeterProviderAndReporterBehavior() throws Exception {
        Feature feature = featuresService.getFeature("opentelemetry-core");
        if (feature == null) {
            fail("Required feature not found");
        }

        if (!featuresService.isInstalled(feature)) {
            featuresService.installFeature(feature.getName());
        }

        ServiceReference<?> reference = bundleContext.getServiceReference(MeterProvider.class);
        if (reference == null) {
            fail("Meter provider not found");
        }

        MeterProvider meterProvider = (MeterProvider) bundleContext.getService(reference);
        LongCounter counter = meterProvider.meterBuilder(METRIC_NAME).build()
            .counterBuilder(COUNTER_NAME).build();
        Attributes attributes = Attributes.builder().put(ATTRIBUTE_NAME, ATTRIBUTE_VALUE).build();
        counter.add(10, attributes);

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:9464/metrics")).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        String responseBody = response.body();

        assertEquals("Expected HTTP 200 status was not received", 200, response.statusCode());
        //assertTrue("Prometheus response should contain metric name", responseBody.contains(METRIC_NAME));
        assertTrue("Prometheus response should contain counter name", responseBody.contains(COUNTER_NAME));
        assertTrue("A sample attribute name is missing", responseBody.contains(ATTRIBUTE_NAME));
        assertTrue("A sample attribute value is missing", responseBody.contains(ATTRIBUTE_VALUE));
    }

}
