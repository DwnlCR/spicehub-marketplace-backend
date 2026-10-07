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
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

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
class ProductControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    ProductControllerIntegrationTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void shouldAllowPublicAccessToProductList() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldRejectProductCreationWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Chá Verde",
                                "description": "Chá verde",
                                "categoryId": "%s",
                                "imageKey": "cha-verde.jpg"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProductCreationForUser() throws Exception {
        mockMvc.perform(post("/products")
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Chá Verde",
                                "description": "Chá verde",
                                "categoryId": "%s",
                                "imageKey": "cha-verde.jpg"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateProductForAdmin() throws Exception {
        UUID categoryId = createCategory();

        String productName =
                "Integration Product " + UUID.randomUUID();

        mockMvc.perform(post("/products")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "%s",
                                "description": "Integration product",
                                "categoryId": "%s",
                                "imageKey": "integration-product.jpg"
                            }
                            """.formatted(productName, categoryId)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(productName))
                .andExpect(jsonPath("$.description")
                        .value("Integration product"))
                .andExpect(jsonPath("$.categoryId")
                        .value(categoryId.toString()))
                .andExpect(jsonPath("$.imageKey")
                        .value("integration-product.jpg"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenCreatingProductWithNonexistentCategory()
            throws Exception {

        UUID nonexistentCategoryId = UUID.randomUUID();

        mockMvc.perform(post("/products")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Invalid Category Product",
                                "description": "Integration product",
                                "categoryId": "%s",
                                "imageKey": "product.jpg"
                            }
                            """.formatted(nonexistentCategoryId)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title")
                        .value("Category not found"));
    }

    private UUID createCategory() throws Exception {
        String categoryName =
                "Integration Category " + UUID.randomUUID();

        MvcResult result = mockMvc.perform(post("/categories")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "%s"
                            }
                            """.formatted(categoryName)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .get("id")
                        .asText()
        );
    }
}