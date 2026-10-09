
package br.com.dwnl.spicehub.catalog.infrastructure.image;

import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.adapter.JpaProductRepositoryAdapter;
import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import({
        PostgresTestContainerConfig.class,
        JpaProductRepositoryAdapter.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProductOptimisticLockingIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRejectUpdateWithOutdatedVersion() {
        UUID categoryId = UUID.randomUUID();
        Instant now = Instant.now();

        jdbcTemplate.update(
                """
                INSERT INTO categories
                    (id, name, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                categoryId,
                "Concurrency Category " + UUID.randomUUID(),
                true,
                Timestamp.from(now),
                Timestamp.from(now)
        );

        Product product = Product.create(
                "Concurrency Product " + UUID.randomUUID(),
                "Original description",
                categoryId,
                null
        );

        Product savedProduct = productRepository.save(product);

        assertNotNull(savedProduct.getVersion());

        UUID productId = savedProduct.getId();

        Product firstCopy = productRepository.findById(productId)
                .orElseThrow();

        Product secondCopy = productRepository.findById(productId)
                .orElseThrow();

        Long originalVersion = firstCopy.getVersion();

        assertEquals(
                originalVersion,
                secondCopy.getVersion()
        );

        firstCopy.changeDescription(
                "Updated by administrator A"
        );

        Product updatedProduct = productRepository.save(firstCopy);

        assertEquals(
                originalVersion + 1,
                updatedProduct.getVersion()
        );

        secondCopy.changeDescription(
                "Updated by administrator B"
        );

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> productRepository.save(secondCopy)
        );

        Product persistedProduct = productRepository.findById(productId)
                .orElseThrow();

        assertEquals(
                "Updated by administrator A",
                persistedProduct.getDescription()
        );

        assertEquals(
                updatedProduct.getVersion(),
                persistedProduct.getVersion()
        );
    }
}
