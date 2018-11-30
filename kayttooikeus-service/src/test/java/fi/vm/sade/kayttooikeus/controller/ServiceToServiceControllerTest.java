package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckDto;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
public class ServiceToServiceControllerTest extends AbstractControllerTest {
    @MockBean
    PermissionCheckerService permissionCheckerService;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void checkUserPermissionToUser() throws Exception {
        String postContent = "{\"callingUserOid\": \"1.2.3.4.5\"," +
                "\"userOid\": \"1.2.3.1.1\"," +
                "\"allowedRoles\": [\"READ_WRITE\"]," +
                "\"externalPermissionService\": \"HAKU_APP\"," +
                "\"callingUserRoles\": [\"ROLE_APP_HENKILONHALLINTA_OPHREKISTERI\"]}";
        given(this.permissionCheckerService.isAllowedToAccessPerson(any(PermissionCheckDto.class))).willReturn(true);
        this.mvc.perform(post("/s2s/canUserAccessUser").content(postContent).contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        ArgumentCaptor<PermissionCheckDto> captor = ArgumentCaptor.forClass(PermissionCheckDto.class);
        verify(permissionCheckerService).isAllowedToAccessPerson(captor.capture());
        PermissionCheckDto dto = captor.getValue();
        assertThat(dto).isNotNull();
        assertThat(dto.getCallingUserOid()).isEqualTo("1.2.3.4.5");
        assertThat(dto.getUserOid()).isEqualTo("1.2.3.1.1");
        assertThat(dto.getAllowedRoles()).containsExactly("READ_WRITE");
        assertThat(dto.getExternalPermissionService()).isEqualByComparingTo(ExternalPermissionService.HAKU_APP);
        assertThat(dto.getCallingUserRoles()).containsExactly("ROLE_APP_HENKILONHALLINTA_OPHREKISTERI");
    }
}
