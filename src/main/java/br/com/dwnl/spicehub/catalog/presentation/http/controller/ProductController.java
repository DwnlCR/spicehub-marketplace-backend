package br.com.dwnl.spicehub.catalog.presentation.http.controller;

import br.com.dwnl.spicehub.catalog.application.usecase.*;
import br.com.dwnl.spicehub.catalog.domain.model.PageResult;
import br.com.dwnl.spicehub.catalog.domain.model.Product;
import br.com.dwnl.spicehub.catalog.domain.model.ProductSort;
import br.com.dwnl.spicehub.catalog.presentation.http.dto.CreateProductRequest;
import br.com.dwnl.spicehub.catalog.presentation.http.dto.ProductResponse;
import br.com.dwnl.spicehub.catalog.presentation.http.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ActivateProductUseCase activateProductUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;

    public ProductController(CreateProductUseCase createProductUseCase, GetProductUseCase getProductUseCase, ListProductsUseCase listProductsUseCase, UpdateProductUseCase updateProductUseCase, ActivateProductUseCase activateProductUseCase, DeactivateProductUseCase deactivateProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.activateProductUseCase = activateProductUseCase;
        this.deactivateProductUseCase = deactivateProductUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request){
        Product product = createProductUseCase.execute(request.name(), request.description(), request.categoryId(), request.imageKey());

        return ProductResponse.from(product);
    }

    @GetMapping("/{productId}")
    public ProductResponse get(@PathVariable UUID productId){
        Product product = getProductUseCase.execute(productId);

        return ProductResponse.from(product);
    }

    @GetMapping
    public PageResult<ProductResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProductSort sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
            ){

        PageResult<Product> result = listProductsUseCase.execute(search, sort, page, size);

        return new PageResult<>(
                result.content().stream().map(ProductResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last()
        );
    }

    @PutMapping("/{productId}")
    public ProductResponse update(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request
            ){
        Product product = updateProductUseCase.execute(
                productId,
                request.name(),
                request.description(),
                request.categoryId(),
                request.imageKey()
        );

        return ProductResponse.from(product);
    }

    @PatchMapping("{productId}/activate")
    public ProductResponse activate(@PathVariable UUID productId){
        Product product = activateProductUseCase.execute(productId);

        return ProductResponse.from(product);
    }

    @PatchMapping("{productId}/deactivate")
    public ProductResponse deactivate(@PathVariable UUID productId){
        Product product = deactivateProductUseCase.execute(productId);

        return ProductResponse.from(product);
    }
}
