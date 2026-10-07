package br.com.dwnl.spicehub.catalog.presentation.http;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({
        PostgresTestContainerConfig.class,
        RedisTestContainerConfig.class
})
class CategoryControllerIntegrationTest {

    private final MockMvc mockMvc;

    @Autowired
    CategoryControllerIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldAllowPublicAccessToCategoryList() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));
    }

    @Test
    void shouldRejectCategoryCreationWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Chás"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectCategoryCreationForUser() throws Exception {
        mockMvc.perform(post("/categories")
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Chás"
                            }
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateCategoryForAdmin() throws Exception {
        String categoryName =
                "Integration Category " + UUID.randomUUID();

        mockMvc.perform(post("/categories")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "%s"
                            }
                            """.formatted(categoryName)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(categoryName))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRejectInvalidCategoryNameForAdmin() throws Exception {
        mockMvc.perform(post("/categories")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": ""
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(400));
    }
}