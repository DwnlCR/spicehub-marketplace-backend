package br.com.dwnl.spicehub.catalog.infrastructure.persistence.mapper;

import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity.CategoryEntity;

public final class CategoryPersistenceMapper {

    private CategoryPersistenceMapper (){}

    public static CategoryEntity toEntity(Category category){
        return new CategoryEntity(
                category.getId(),
                category.getName(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public static Category toDomain(CategoryEntity entity){
        return Category.restore(
                entity.getId(),
                entity.getName(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
