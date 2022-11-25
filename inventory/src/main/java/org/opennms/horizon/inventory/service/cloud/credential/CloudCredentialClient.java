package org.opennms.horizon.inventory.service.cloud.credential;

import com.google.protobuf.Any;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.inventory.dto.CloudCredentialsDTO;
import org.opennms.horizon.inventory.dto.CloudType;
import org.opennms.horizon.inventory.service.cloud.CloudFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CloudCredentialClient {
    private final CloudFactory cloudFactory;

    public CloudCredentialsDTO create(CloudType cloudType,
                                      String tenantId, Any request) {
        String beanName = getBeanName(cloudType);

        CloudCredentialService credentialService =
            cloudFactory.getCredentialService(beanName);
        return credentialService.create(tenantId, request);
    }

    private String getBeanName(CloudType cloudType) {
        String azureCloudType = cloudType.name().toLowerCase();
        return azureCloudType + CloudCredentialService.class.getSimpleName();
    }
}
