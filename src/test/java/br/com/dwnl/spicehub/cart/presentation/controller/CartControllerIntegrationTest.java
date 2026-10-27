
package br.com.dwnl.spicehub.cart.presentation.controller;

import br.com.dwnl.spicehub.cart.infrastructure.persistence.repository.SpringDataCartRepository;
import br.com.dwnl.spicehub.catalog.domain.storage.ImageStorage;
import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.entity.UserEntity;
import br.com.dwnl.spicehub.identity.infrastructure.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({
        PostgresTestContainerConfig.class,
        RedisTestContainerConfig.class
})
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private SpringDataCartRepository cartRepository;

    @MockitoBean
    private ImageStorage imageStorage;

    @Test
    void shouldRejectCartAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnEmptyCartForAuthenticatedUser() throws Exception {
        UUID userId = createUser();

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        assertTrue(cartRepository.findByUserId(userId).isEmpty());
    }

    @Test
    void shouldRejectAddingItemWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemBody(UUID.randomUUID(), 2)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAddItemAndPersistCart() throws Exception {
        UUID userId = createUser();
        UUID variantId = createAvailableVariant();

        addItem(userId, variantId, 2);

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].variantId").value(variantId.toString()))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(12.50))
                .andExpect(jsonPath("$.items[0].subtotal").value(25.00))
                .andExpect(jsonPath("$.items[0].available").value(true))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPrice").value(25.00));

        assertTrue(cartRepository.findByUserId(userId).isPresent());
    }

    @Test
    void shouldIncreaseQuantityWhenAddingSameVariant() throws Exception {
        UUID userId = createUser();
        UUID variantId = createAvailableVariant();

        addItem(userId, variantId, 2);
        addItem(userId, variantId, 3);

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.totalItems").value(5))
                .andExpect(jsonPath("$.totalPrice").value(62.50));
    }

    @Test
    void shouldUpdateItemQuantity() throws Exception {
        UUID userId = createUser();
        UUID variantId = createAvailableVariant();

        addItem(userId, variantId, 2);

        mockMvc.perform(patch("/cart/items/{variantId}", variantId)
                        .with(authenticatedUser(userId))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "quantity": 5
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.totalItems").value(5))
                .andExpect(jsonPath("$.totalPrice").value(62.50));
    }

    @Test
    void shouldRemoveItem() throws Exception {
        UUID userId = createUser();
        UUID variantId = createAvailableVariant();

        addItem(userId, variantId, 2);

        mockMvc.perform(delete("/cart/items/{variantId}", variantId)
                        .with(authenticatedUser(userId))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        assertTrue(cartRepository.findByUserId(userId).isPresent());
    }

    @Test
    void shouldClearCart() throws Exception {
        UUID userId = createUser();
        UUID firstVariantId = createAvailableVariant();
        UUID secondVariantId = createAvailableVariant();

        addItem(userId, firstVariantId, 2);
        addItem(userId, secondVariantId, 3);

        mockMvc.perform(delete("/cart/items")
                        .with(authenticatedUser(userId))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        assertTrue(cartRepository.findByUserId(userId).isPresent());
    }

    @Test
    void shouldAllowClearingNonexistentCart() throws Exception {
        UUID userId = createUser();

        mockMvc.perform(delete("/cart/items")
                        .with(authenticatedUser(userId))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertTrue(cartRepository.findByUserId(userId).isEmpty());
    }

    @Test
    void shouldRejectAddingItemWithZeroQuantity() throws Exception {
        UUID userId = createUser();

        mockMvc.perform(post("/cart/items")
                        .with(authenticatedUser(userId))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemBody(UUID.randomUUID(), 0)))
                .andExpect(status().isBadRequest());

        assertTrue(cartRepository.findByUserId(userId).isEmpty());
    }

    @Test
    void shouldRejectAddingItemWithMissingVariantId() throws Exception {
        UUID userId = createUser();

        mockMvc.perform(post("/cart/items")
                        .with(authenticatedUser(userId))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "quantity": 2
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertTrue(cartRepository.findByUserId(userId).isEmpty());
    }

    @Test
    void shouldRejectUpdatingItemWithZeroQuantity() throws Exception {
        UUID userId = createUser();
        UUID variantId = createAvailableVariant();

        addItem(userId, variantId, 2);

        mockMvc.perform(patch("/cart/items/{variantId}", variantId)
                        .with(authenticatedUser(userId))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void shouldKeepCartsIsolatedBetweenUsers() throws Exception {
        UUID firstUserId = createUser();
        UUID secondUserId = createUser();
        UUID variantId = createAvailableVariant();

        addItem(firstUserId, variantId, 2);

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(secondUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(firstUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        assertTrue(cartRepository.findByUserId(secondUserId).isEmpty());
    }

    @Test
    void shouldNotAllowUserToModifyAnotherUsersCart() throws Exception {
        UUID firstUserId = createUser();
        UUID secondUserId = createUser();
        UUID variantId = createAvailableVariant();

        addItem(firstUserId, variantId, 2);

        mockMvc.perform(delete("/cart/items")
                        .with(authenticatedUser(secondUserId))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(firstUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void shouldRejectAddingSoldOutVariant() throws Exception {
        UUID userId = createUser();
        UUID productId = createProduct();
        UUID variantId = createVariant(productId);

        mockMvc.perform(patch(
                        "/products/{productId}/variants/{variantId}/sold-out",
                        productId,
                        variantId
                )
                        .with(adminUser())
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/items")
                        .with(authenticatedUser(userId))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemBody(variantId, 2)))
                .andExpect(status().isConflict());

        assertTrue(cartRepository.findByUserId(userId).isEmpty());
    }

    @Test
    void shouldKeepSoldOutItemVisibleButUnavailable() throws Exception {
        UUID userId = createUser();
        UUID productId = createProduct();
        UUID variantId = createVariant(productId);

        addItem(userId, variantId, 2);

        mockMvc.perform(patch(
                        "/products/{productId}/variants/{variantId}/sold-out",
                        productId,
                        variantId
                )
                        .with(adminUser())
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].available").value(false))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    private UUID createUser() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        UserEntity user = new UserEntity(
                userId,
                "Cart Integration User",
                "cart-" + userId + "@example.com",
                "test-password-hash",
                true,
                true,
                Set.of(),
                now,
                now
        );

        userRepository.saveAndFlush(user);

        return userId;
    }

    private RequestPostProcessor authenticatedUser(UUID userId) {
        return jwt().jwt(token -> token
                .subject(userId.toString())
                .claim("roles", List.of("USER"))
                .claim("email", "cart@example.com")
        ).authorities(() -> "ROLE_USER");
    }

    private RequestPostProcessor adminUser() {
        return jwt().jwt(token -> token
                .subject(UUID.randomUUID().toString())
                .claim("roles", List.of("ADMIN"))
                .claim("email", "admin@example.com")
        ).authorities(() -> "ROLE_ADMIN");
    }

    private UUID createCategory() throws Exception {
        MvcResult result = mockMvc.perform(post("/categories")
                        .with(adminUser())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Cart Category %s"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn();

        return readId(result);
    }

    private UUID createProduct() throws Exception {
        UUID categoryId = createCategory();

        MvcResult result = mockMvc.perform(post("/products")
                        .with(adminUser())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Cart Product %s",
                                    "description": "Integration test product",
                                    "categoryId": "%s"
                                }
                                """.formatted(UUID.randomUUID(), categoryId)))
                .andExpect(status().isCreated())
                .andReturn();

        return readId(result);
    }

    private UUID createVariant(UUID productId) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/products/{productId}/variants", productId)
                                .with(adminUser())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "quantity": 100,
                                            "measurementUnit": "GRAM",
                                            "price": 12.50
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andReturn();

        return readId(result);
    }

    private UUID createAvailableVariant() throws Exception {
        UUID productId = createProduct();
        return createVariant(productId);
    }

    private void addItem(UUID userId, UUID variantId, int quantity)
            throws Exception {

        mockMvc.perform(post("/cart/items")
                        .with(authenticatedUser(userId))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemBody(variantId, quantity)))
                .andExpect(status().isNoContent());
    }

    private String addItemBody(UUID variantId, int quantity) {
        return """
                {
                    "variantId": "%s",
                    "quantity": %d
                }
                """.formatted(variantId, quantity);
    }

    private UUID readId(MvcResult result) throws Exception {
        return UUID.fromString(
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .get("id")
                        .asText()
        );
    }

    @Test
    void shouldAllowAuthenticatedJwtRequestWithoutCsrf() throws Exception {
        UUID userId = createUser();

        mockMvc.perform(delete("/cart/items")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isNoContent());
    }


    @Test
    void shouldRejectAddingNonexistentVariant() throws Exception {
        UUID userId = createUser();
        UUID variantId = UUID.randomUUID();

        mockMvc.perform(post("/cart/items")
                        .with(authenticatedUser(userId))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemBody(variantId, 2)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Cart variant not found"));

        assertTrue(cartRepository.findByUserId(userId).isEmpty());
    }

    @Test
    void shouldRejectRemovingNonexistentItem() throws Exception {
        UUID userId = createUser();
        UUID variantId = createAvailableVariant();

        addItem(userId, variantId, 2);

        mockMvc.perform(delete("/cart/items/{variantId}", UUID.randomUUID())
                        .with(authenticatedUser(userId))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Cart item not found"));

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void shouldRejectUpdatingNonexistentItem() throws Exception {
        UUID userId = createUser();
        UUID variantId = createAvailableVariant();

        addItem(userId, variantId, 2);

        mockMvc.perform(patch("/cart/items/{variantId}", UUID.randomUUID())
                        .with(authenticatedUser(userId))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "quantity": 3
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Cart item not found"));

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void shouldRejectUpdatingSoldOutVariant() throws Exception {
        UUID userId = createUser();
        UUID productId = createProduct();
        UUID variantId = createVariant(productId);

        addItem(userId, variantId, 2);

        mockMvc.perform(patch(
                        "/products/{productId}/variants/{variantId}/sold-out",
                        productId,
                        variantId
                )
                        .with(adminUser())
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/cart/items/{variantId}", variantId)
                        .with(authenticatedUser(userId))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "quantity": 5
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Cart variant unavailable"));

        mockMvc.perform(get("/cart")
                        .with(authenticatedUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].available").value(false));
    }

    @Test
    void shouldRejectRemovingItemFromNonexistentCart() throws Exception {
        UUID userId = createUser();

        mockMvc.perform(delete("/cart/items/{variantId}", UUID.randomUUID())
                        .with(authenticatedUser(userId))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Cart not found"));

        assertTrue(cartRepository.findByUserId(userId).isEmpty());
    }

}
