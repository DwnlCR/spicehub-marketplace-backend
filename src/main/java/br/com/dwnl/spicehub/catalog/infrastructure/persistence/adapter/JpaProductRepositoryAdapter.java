package br.com.dwnl.spicehub.catalog.infrastructure.persistence.adapter;

import br.com.dwnl.spicehub.catalog.domain.model.PageResult;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductSort;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.entity.ProductEntity;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.mapper.ProductPersistenceMapper;
import br.com.dwnl.spicehub.catalog.infrastructure.persistence.repository.SpringDataProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository productRepository;

    public JpaProductRepositoryAdapter(SpringDataProductRepository productRepository)
    {
        this.productRepository = productRepository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductPersistenceMapper.toEntity(product);

        ProductEntity saved = productRepository.save(entity);

        return ProductPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id)
                .map(ProductPersistenceMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByCategoryId(UUID id) {
        return productRepository.findByCategoryId(id)
                .stream()
                .map(ProductPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return productRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public PageResult<Product> search(String name, ProductSort sort, int page, int size) {
        Sort springSort = switch (sort){
            case NAME_ASC -> Sort.by("name").ascending();
            case NAME_DESC -> Sort.by("name").descending();
        };

        PageRequest pageable = PageRequest.of(page, size, springSort);

        String searchName = name == null ? "" : name.trim();

        Page<ProductEntity> result = productRepository.findByNameContainingIgnoreCase(searchName, pageable);

        List<Product> products = result
                .getContent()
                .stream()
                .map(ProductPersistenceMapper::toDomain)
                .toList();

        return new PageResult<>(
                products,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }
}
