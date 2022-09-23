package org.opennms.horizon.notifications.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opennms.horizon.notifications.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class NotificationRestSecurityTest {

    @InjectMocks
    NotificationRestController notificationRestController;

    @Mock
    NotificationService notificationsService;

    @Test
    @WithMockUser(username = "admin", authorities = { "admin", "user" })
    public void testSecurity() throws Exception {
        ResponseEntity<String> ret = notificationRestController.postPagerDutyConfig(null);

        assertEquals(HttpStatus.OK, ret.getStatusCode());
    }

    @Test
    //@WithMockUser(username = "john", authorities = { "low", "useless" })
    @WithAnonymousUser
    public void testSecurityNotAuth() throws Exception {
        ResponseEntity<String> ret = notificationRestController.postPagerDutyConfig(null);

        assertEquals(HttpStatus.FORBIDDEN, ret.getStatusCode());
    }
}
