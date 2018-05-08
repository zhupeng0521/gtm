package dev.local.gtm.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.local.gtm.api.config.AppProperties;
import dev.local.gtm.api.domain.Authority;
import dev.local.gtm.api.domain.User;
import dev.local.gtm.api.repository.mongo.AuthorityRepository;
import dev.local.gtm.api.repository.mongo.UserRepository;
import dev.local.gtm.api.security.AuthoritiesConstants;
import dev.local.gtm.api.service.AuthService;
import dev.local.gtm.api.web.exception.ExceptionTranslator;
import dev.local.gtm.api.web.rest.AuthResource;
import dev.local.gtm.api.web.rest.vm.UserVM;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthResourceTest {

        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private HttpMessageConverter[] httpMessageConverters;

        @Autowired
        private ExceptionTranslator exceptionTranslator;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AuthorityRepository authorityRepository;

        @Autowired
        private AuthService authService;

        @Autowired
        private AppProperties appProperties;

        @Before
        public void setup() {
                userRepository.deleteAll();
                authorityRepository.deleteAll();
                val authResource = new AuthResource(authService, appProperties);
                mockMvc = MockMvcBuilders.standaloneSetup(authResource).setMessageConverters(httpMessageConverters)
                                .setControllerAdvice(exceptionTranslator).build();
                authorityRepository.save(new Authority(AuthoritiesConstants.USER));
                authorityRepository.save(new Authority(AuthoritiesConstants.ADMIN));
        }

        @Test
        public void testRegisterSuccess() throws Exception {
                val user = UserVM.builder().login("test1").mobile("13000000000").email("test1@local.dev").name("test 1")
                                .password("12345").build();

                mockMvc.perform(post("/api/auth/register").content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isOk())
                                .andExpect(jsonPath("$.id_token").isNotEmpty());
                assertThat(userRepository.findOneByLoginIgnoreCase("test1").isPresent()).isTrue();
        }

        @Test
        public void testRegisterFailDup() throws Exception {
                val user = User.builder().login("test1").mobile("13000000000").email("test1@local.dev").name("test 1")
                                .password("123456").build();
                userRepository.save(user);
                mockMvc.perform(post("/api/auth/register").content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isBadRequest());
        }

        @Test
        public void testVerifyMobileSuccess() throws Exception {
                val user = User.builder().login("test1").mobile("13898810892").email("test1@local.dev").name("test 1")
                                .password("123456").build();
                userRepository.save(user);
                val verification = new AuthResource.MobileVerification("13898810892", "525798");
                mockMvc.perform(post("/api/auth/mobile").content(objectMapper.writeValueAsString(verification))
                                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isOk())
                                .andExpect(jsonPath("$.reset_key").isNotEmpty());
        }

        @Test
        public void testVerifyMobileFail() throws Exception {
                val user = User.builder().login("test1").mobile("13898810892").email("test1@local.dev").name("test 1")
                                .password("123456").build();
                userRepository.save(user);
                val verification = new AuthResource.MobileVerification("13898810892", "123456");
                mockMvc.perform(post("/api/auth/mobile").content(objectMapper.writeValueAsString(verification))
                                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isBadRequest());
        }

        @Test
        public void testSendSmsSuccess() throws Exception {
                val user = User.builder().login("test1").mobile("13898810892").email("test1@local.dev").name("test 1")
                                .password("123456").build();
                userRepository.save(user);
                val params = new LinkedMultiValueMap<String, String>();
                params.add("mobile", "13898810892");
                params.add("token", "1234");
                mockMvc.perform(get("/api/auth/mobile").params(params).contentType(MediaType.APPLICATION_JSON_UTF8))
                                .andExpect(status().isOk());
        }
}