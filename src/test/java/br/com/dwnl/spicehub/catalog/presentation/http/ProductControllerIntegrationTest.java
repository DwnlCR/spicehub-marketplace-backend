
package br.com.dwnl.spicehub.catalog.presentation.http;

import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductStatus;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.storage.ImageStorage;
import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
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
    private ProductRepository productRepository;

    @MockitoBean
    private ImageStorage imageStorage;

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
                                "categoryId": "%s"
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
                                "categoryId": "%s"
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
                                "categoryId": "%s"
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
                .andExpect(jsonPath("$.hasImage").value(false))
                .andExpect(jsonPath("$.imageKey").doesNotExist())
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
                                "categoryId": "%s"
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

    private UUID createProduct(UUID categoryId, String productName)
            throws Exception {

        MvcResult result = mockMvc.perform(post("/products")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "%s",
                                "description": "Integration product",
                                "categoryId": "%s"
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

    @Test
    void shouldFilterProductsByCategory() throws Exception {
        UUID firstCategoryId = createCategory();
        UUID secondCategoryId = createCategory();

        String firstProductName =
                "Category Filter Product A " + UUID.randomUUID();

        String secondProductName =
                "Category Filter Product B " + UUID.randomUUID();

        UUID firstProductId = createProduct(
                firstCategoryId,
                firstProductName
        );

        UUID secondProductId = createProduct(
                secondCategoryId,
                secondProductName
        );

        mockMvc.perform(get("/products")
                        .param("categoryId", firstCategoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath(
                        "$.content[?(@.id == '%s')]".formatted(firstProductId)
                ).exists())
                .andExpect(jsonPath(
                        "$.content[?(@.id == '%s')]".formatted(secondProductId)
                ).doesNotExist());
    }

    @Test
    void shouldReturnNotFoundWhenProductHasNoImage() throws Exception {
        UUID categoryId = createCategory();

        UUID productId = createProduct(
                categoryId,
                "Product Without Image " + UUID.randomUUID()
        );

        mockMvc.perform(get("/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasImage").value(false))
                .andExpect(jsonPath("$.imageKey").doesNotExist());

        mockMvc.perform(get("/products/{productId}/image", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        UUID nonexistentProductId = UUID.randomUUID();

        mockMvc.perform(get("/products/{productId}/image", nonexistentProductId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnProductImage() throws Exception {
        UUID categoryId = createCategory();

        UUID productId = createProduct(
                categoryId,
                "Product With Image " + UUID.randomUUID()
        );

        String imageKey = "products/" + productId + "/test.webp";
        byte[] imageBytes = {1, 2, 3, 4, 5};

        var product = productRepository.findById(productId)
                .orElseThrow();

        product.changeImage(imageKey);
        productRepository.save(product);

        mockMvc.perform(get("/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasImage").value(true))
                .andExpect(jsonPath("$.imageKey").doesNotExist());

        when(imageStorage.download(imageKey))
                .thenReturn(imageBytes);

        MvcResult result = mockMvc.perform(
                        get("/products/{productId}/image", productId)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andReturn();

        assertArrayEquals(
                imageBytes,
                result.getResponse().getContentAsByteArray()
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


    @Test
    void shouldArchiveProductForAdmin() throws Exception {
        UUID categoryId = createCategory();

        UUID productId = createProduct(
                categoryId,
                "Product To Archive " + UUID.randomUUID()
        );

        mockMvc.perform(patch("/products/{productId}/archive", productId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        Product product = productRepository.findById(productId)
                .orElseThrow();

        assertEquals(ProductStatus.ARCHIVED, product.getStatus());
    }


    @Test
    void shouldHideArchivedProductFromPublicEndpoints() throws Exception {
        UUID categoryId = createCategory();

        String productName = "Archived Product " + UUID.randomUUID();

        UUID productId = createProduct(categoryId, productName);

        mockMvc.perform(patch("/products/{productId}/archive", productId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(get("/products")
                        .param("search", productName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/products/{productId}", productId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/products/{productId}/image", productId))
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldUnarchiveProductForAdmin() throws Exception {
        UUID categoryId = createCategory();

        String productName = "Product To Unarchive " + UUID.randomUUID();

        UUID productId = createProduct(categoryId, productName);

        mockMvc.perform(patch("/products/{productId}/archive", productId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(patch("/products/{productId}/unarchive", productId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        Product product = productRepository.findById(productId)
                .orElseThrow();

        assertEquals(ProductStatus.INACTIVE, product.getStatus());

        mockMvc.perform(get("/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(get("/products")
                        .param("search", productName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value(productId.toString()))
                .andExpect(jsonPath("$.content[0].status")
                        .value("INACTIVE"));
    }


    @Test
    void shouldRejectStatusChangesForArchivedProduct() throws Exception {
        UUID categoryId = createCategory();

        UUID productId = createProduct(
                categoryId,
                "Archived Status Product " + UUID.randomUUID()
        );

        mockMvc.perform(patch("/products/{productId}/archive", productId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(patch("/products/{productId}/activate", productId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title")
                        .value("Invalid product status transition"));

        mockMvc.perform(patch("/products/{productId}/deactivate", productId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title")
                        .value("Invalid product status transition"));

        Product product = productRepository.findById(productId)
                .orElseThrow();

        assertEquals(ProductStatus.ARCHIVED, product.getStatus());
    }


    @Test
    void shouldRejectArchivingAndUnarchivingWithoutAdminRole() throws Exception {
        UUID categoryId = createCategory();

        UUID productId = createProduct(
                categoryId,
                "Product Authorization " + UUID.randomUUID()
        );

        mockMvc.perform(patch("/products/{productId}/archive", productId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/products/{productId}/archive", productId)
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/products/{productId}/unarchive", productId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/products/{productId}/unarchive", productId)
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        Product product = productRepository.findById(productId)
                .orElseThrow();

        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }
}
