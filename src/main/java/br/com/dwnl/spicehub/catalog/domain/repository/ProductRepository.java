package br.com.dwnl.spicehub.catalog.domain.repository;

import br.com.dwnl.spicehub.catalog.domain.model.PageResult;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductSort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    List<Product> findAll();

    List<Product> findByCategoryId(UUID id);

    boolean existsByNameIgnoreCase(String name);

    PageResult<Product> search(String name, ProductSort sort, int page, int size);
}
