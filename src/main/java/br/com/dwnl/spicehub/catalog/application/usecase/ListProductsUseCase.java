package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.domain.model.PageResult;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductSort;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Service;

@Service
public class ListProductsUseCase {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final ProductRepository productRepository;

    public ListProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public PageResult<Product> execute(String search, ProductSort sort, Integer page, Integer size){
        int resolvedPag = page == null || page < 0 ? DEFAULT_PAGE : page;

        int resolvedSize = size == null || size <= 0 ? DEFAULT_SIZE : size;

        ProductSort resolvedSort = sort == null ? ProductSort.NAME_ASC : sort;

        return productRepository.search(search, resolvedSort, resolvedPag, resolvedSize);
    }
}
