package org.opennms.miniongateway.ratelimiting;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Configuration
@PropertySource(value = "classpath:rate-limiting-policy.yaml", factory = YamlPropertySourceFactory.class)
public class PolicyProperties {

    private List<Tenant> tenants;

    public void setTenants(List<Tenant> tenants) {
        this.tenants = tenants;
    }

    public List<Tenant> getTenants() {
        return this.tenants;
    }

    public static class Tenant implements Serializable {
        @Serial
        private static final long serialVersionUID = 6690691502298635887L;
        private String tenantID;
        private int traps;
//        private int flows;
//        private int metrics;

        // Getters and setters
        public void setTenantID(String tenantID) {
            this.tenantID = tenantID;
        }
        public String getTenantID() {
            return this.tenantID;
        }

        public void setTraps(int traps) {
            this.traps = traps;
        }
        public int getTraps() {
            return this.traps;
        }

        @Override
        public String toString() {
//            return "Tenant [tenantID=" + tenantID + ", traps=" + traps + ", flows=" + flows + ", metrics=" + metrics + "]";
            return "Tenant [tenantID=" + tenantID + ", traps=" + traps + "]";
        }
    }
}
