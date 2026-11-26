package com.lingualink.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"openrouter.api-key=test-key", "openrouter.model=deepseek/test"})
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@ActiveProfiles("test")
class LanguageToolsIntegrationTest {
    private static final String COMPLETIONS = "https://openrouter.ai/api/v1/chat/completions";
    private static final String TRANSLATION = """
            {"targetLanguage":"GERMANY","text":"Good morning!"}
            """;

    @Autowired MockMvc mvc;
    @Autowired MockRestServiceServer provider;

    @Test
    void practiceSendsHistoryAndReturnsOnlyAssistantContent() throws Exception {
        provider.expect(requestTo(COMPLETIONS)).andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("deepseek/test"))
                .andExpect(jsonPath("$.messages[0].content", containsString("FRENCH")))
                .andExpect(jsonPath("$.messages[1].content").value("Bonjour"))
                .andExpect(jsonPath("$.messages[2].role").value("assistant"))
                .andExpect(jsonPath("$.messages[3].content").value("Et toi ?"))
                .andRespond(withSuccess("""
                        {"choices":[{"finish_reason":"stop","message":{"content":"Très bien !","reasoning":"private"}}]}
                        """, MediaType.APPLICATION_JSON));
        mvc.perform(post("/api/ai/chat").with(user("student")).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"FRENCH","message":"Et toi ?","history":[{"user":"Bonjour","assistant":"Salut !"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value("Très bien !"));
        provider.verify();
    }

    @Test
    void translatorSendsTargetAndSourceText() throws Exception {
        provider.expect(requestTo(COMPLETIONS)).andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.messages[0].content", containsString("German")))
                .andExpect(jsonPath("$.messages[1].content").value("Good morning!"))
                .andRespond(withSuccess("""
                        {"choices":[{"finish_reason":"stop","message":{"content":"Guten Morgen!"}}]}
                        """, MediaType.APPLICATION_JSON));
        mvc.perform(post("/api/ai/translate").with(user("student"))
                        .contentType(MediaType.APPLICATION_JSON).content(TRANSLATION))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value("Guten Morgen!"));
        provider.verify();
    }

    @Test
    void upstreamFailureDoesNotInvalidateLogin() throws Exception {
        provider.expect(requestTo(COMPLETIONS)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        mvc.perform(post("/api/ai/translate").with(user("student"))
                        .contentType(MediaType.APPLICATION_JSON).content(TRANSLATION))
                .andExpect(status().isBadGateway());
        provider.verify();
    }

    @Test
    void anonymousOrInvalidRequestsDoNotReachProvider() throws Exception {
        mvc.perform(post("/api/ai/translate").contentType(MediaType.APPLICATION_JSON).content(TRANSLATION))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/ai/chat").with(user("student")).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"ENGLISH","message":"  ","history":[]}
                                """))
                .andExpect(status().isBadRequest());
        provider.verify();
    }

    @Test
    void emptyKeyIsReportedBeforeSendingAnything() {
        var client = new OpenRouterClient(RestClient.builder(), "https://openrouter.invalid", "", "deepseek/test");
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> client.complete(List.of(), 100))
                .satisfies(ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }
}
