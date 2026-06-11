package com.srb.backend.config;

import com.srb.backend.service.RememberLoginTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.*;

class RememberLoginAuthenticationFilterTest {

    @Test
    void shouldTryRestoreBeforeApiRequestWhenSessionMissing() throws Exception {
        RememberLoginTokenService service = mock(RememberLoginTokenService.class);
        RememberLoginAuthenticationFilter filter = new RememberLoginAuthenticationFilter(service);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/current");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(service).restoreSessionIfPossible(request, response);
    }
}
