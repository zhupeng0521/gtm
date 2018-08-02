package dev.local.gtm.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.local.gtm.api.Application;
import dev.local.gtm.api.config.AppProperties;
import dev.local.gtm.api.repository.mongo.TaskRepository;
import dev.local.gtm.api.repository.mongo.UserRepository;
import dev.local.gtm.api.web.exception.ExceptionTranslator;
import dev.local.gtm.api.web.rest.TaskResource;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableSpringDataWebSupport
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TaskResourceTest {

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpMessageConverter[] httpMessageConverters;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private AppProperties appProperties;

    @Before
    public void setup() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        val taskResource = new TaskResource(taskRepository, userRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(taskResource).setMessageConverters(httpMessageConverters)
                .setControllerAdvice(exceptionTranslator).setViewResolvers((ViewResolver) (viewName, locale) -> {

                    return new MappingJackson2JsonView();
                }).build();
    }

    @Test
    @WithMockUser
    public void testGetTasks() throws Exception {

        mockMvc.perform(get("/api/tasks").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

}
