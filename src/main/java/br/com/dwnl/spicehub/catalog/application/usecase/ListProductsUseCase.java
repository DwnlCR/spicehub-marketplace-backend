package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.domain.model.PageResult;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductSort;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ListProductsUseCase {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final ProductRepository productRepository;

    public ListProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public PageResult<Product> execute(String search, UUID categoryId, ProductSort sort, Integer page, Integer size){
        int resolvedPage = page == null || page < 0 ? DEFAULT_PAGE : page;

        int resolvedSize = size == null || size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        ProductSort resolvedSort = sort == null ? ProductSort.NAME_ASC : sort;

        return productRepository.search(search, categoryId, resolvedSort, resolvedPage, resolvedSize);
    }
}
