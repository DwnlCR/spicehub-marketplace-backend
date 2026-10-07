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
class ProductVariantControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    ProductVariantControllerIntegrationTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void shouldAllowPublicAccessToVariantList() throws Exception {
        UUID productId = createProduct();

        mockMvc.perform(get("/products/{productId}/variants", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldRejectVariantCreationWithoutAuthentication() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(post("/products/{productId}/variants", productId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "quantity": 100,
                                "measurementUnit": "GRAM",
                                "price": 12.90
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectVariantCreationForUser() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(post("/products/{productId}/variants", productId)
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "quantity": 100,
                                "measurementUnit": "GRAM",
                                "price": 12.90
                            }
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateVariantForAdmin() throws Exception {
        UUID productId = createProduct();

        mockMvc.perform(post("/products/{productId}/variants", productId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "quantity": 100,
                                "measurementUnit": "GRAM",
                                "price": 12.90
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.productId")
                        .value(productId.toString()))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.measurementUnit").value("GRAM"))
                .andExpect(jsonPath("$.price").value(12.90))
                .andExpect(jsonPath("$.availability")
                        .value("AVAILABLE"));
    }

    @Test
    void shouldReturnNotFoundWhenCreatingVariantForNonexistentProduct()
            throws Exception {

        UUID productId = UUID.randomUUID();

        mockMvc.perform(post("/products/{productId}/variants", productId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "quantity": 100,
                                "measurementUnit": "GRAM",
                                "price": 12.90
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title")
                        .value("Product not found"));
    }

    private UUID createProduct() throws Exception {
        UUID categoryId = createCategory();

        String productName =
                "Integration Product " + UUID.randomUUID();

        MvcResult result = mockMvc.perform(post("/products")
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
                .andReturn();

        return UUID.fromString(
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .get("id")
                        .asText()
        );
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