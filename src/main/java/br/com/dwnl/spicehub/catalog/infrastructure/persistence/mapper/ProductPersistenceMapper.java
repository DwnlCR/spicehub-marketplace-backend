package br.com.dwnl.spicehub.catalog.infrastructure.persistence.mapper;

import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity.ProductEntity;

public final class ProductPersistenceMapper {

    private ProductPersistenceMapper(){}

    public static ProductEntity toEntity(Product product){
        return new ProductEntity(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategoryId(),
                product.getImageKey(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static Product toDomain(ProductEntity entity){
        return Product.restore(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategoryId(),
                entity.getImageKey(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
