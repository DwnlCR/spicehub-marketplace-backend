package br.com.dwnl.spicehub.catalog.infrastructure.persistence.adapter;

import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity.ProductVariantEntity;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.mapper.ProductVariantPersistenceMapper;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.repository.SpringDataProductVariantRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaProductVariantRepositoryAdapter implements ProductVariantRepository {
    private final SpringDataProductVariantRepository productVariantRepository;

    public JpaProductVariantRepositoryAdapter(SpringDataProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public ProductVariant save(ProductVariant productVariant) {
        ProductVariantEntity entity = ProductVariantPersistenceMapper.toEntity(productVariant);

        ProductVariantEntity saved = productVariantRepository.save(entity);

        return ProductVariantPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<ProductVariant> findById(UUID id) {
        return productVariantRepository.findById(id)
                .map(ProductVariantPersistenceMapper::toDomain);
    }

    @Override
    public List<ProductVariant> findByProductId(UUID productId) {
        return productVariantRepository.findByProductId(productId)
                .stream()
                .map(ProductVariantPersistenceMapper::toDomain)
                .toList();
    }
}
