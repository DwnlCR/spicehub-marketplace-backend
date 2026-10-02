package br.com.dwnl.spicehub.catalog.infrastructure.persistence.adapter;

import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity.CategoryEntity;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.repository.SpringDataCategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCategoryRepositoryAdapter implements CategoryRepository {

    private final SpringDataCategoryRepository categoryRepository;

    public JpaCategoryRepositoryAdapter(SpringDataCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = CategoryPersistenceMapper.toEntity(category);

        CategoryEntity saved = categoryRepository.save(entity);

        return CategoryPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryRepository.findById(id)
                .map(CategoryPersistenceMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }
}
