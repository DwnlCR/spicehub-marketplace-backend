package br.com.dwnl.spicehub.catalog.infrastructure.persistence.mapper;

import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity.ProductVariantEntity;

public final class ProductVariantPersistenceMapper {

    private ProductVariantPersistenceMapper(){}

    public static ProductVariantEntity toEntity(ProductVariant productVariant){
        return new ProductVariantEntity(
                productVariant.getId(),
                productVariant.getProductId(),
                productVariant.getQuantity(),
                productVariant.getMeasurementUnit(),
                productVariant.getPrice(),
                productVariant.getAvailability(),
                productVariant.getCreatedAt(),
                productVariant.getUpdatedAt()
        );
    }

    public static ProductVariant toDomain(ProductVariantEntity productVariantEntity){
        return ProductVariant.restore(
                productVariantEntity.getId(),
                productVariantEntity.getProductId(),
                productVariantEntity.getQuantity(),
                productVariantEntity.getMeasurementUnit(),
                productVariantEntity.getPrice(),
                productVariantEntity.getAvailability(),
                productVariantEntity.getCreatedAt(),
                productVariantEntity.getUpdatedAt()
        );
    }
}
