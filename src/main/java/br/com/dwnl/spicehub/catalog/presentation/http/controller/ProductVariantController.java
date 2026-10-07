package br.com.dwnl.spicehub.catalog.presentation.http.controller;

import br.com.dwnl.spicehub.catalog.application.usecase.*;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.presentation.http.dto.CreateVariantRequest;
import br.com.dwnl.spicehub.catalog.presentation.http.dto.ProductVariantResponse;
import br.com.dwnl.spicehub.catalog.presentation.http.dto.UpdateProductVariantRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/variants")
public class ProductVariantController {

    private final CreateProductVariantUseCase createProductVariantUseCase;
    private final GetProductVariantUseCase getProductVariantUseCase;
    private final ListProductVariantUseCase listProductVariantUseCase;
    private final UpdateProductVariantUseCase updateProductVariantUseCase;
    private final MarkProductVariantAvailableUseCase markProductVariantAvailableUseCase;
    private final MarkProductVariantSoldOutUseCase markProductVariantSoldOutUseCase;

    public ProductVariantController(CreateProductVariantUseCase createProductVariantUseCase, GetProductVariantUseCase getProductVariantUseCase, ListProductVariantUseCase listProductVariantUseCase, UpdateProductVariantUseCase updateProductVariantUseCase, MarkProductVariantAvailableUseCase markProductVariantAvailableUseCase, MarkProductVariantSoldOutUseCase markProductVariantSoldOutUseCase) {
        this.createProductVariantUseCase = createProductVariantUseCase;
        this.getProductVariantUseCase = getProductVariantUseCase;
        this.listProductVariantUseCase = listProductVariantUseCase;
        this.updateProductVariantUseCase = updateProductVariantUseCase;
        this.markProductVariantAvailableUseCase = markProductVariantAvailableUseCase;
        this.markProductVariantSoldOutUseCase = markProductVariantSoldOutUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductVariantResponse create(@PathVariable UUID productId, @Valid @RequestBody CreateVariantRequest request){

        ProductVariant productVariant = createProductVariantUseCase.execute(
                productId,
                request.quantity(),
                request.measurementUnit(),
                request.price()
        );

        return ProductVariantResponse.from(productVariant);
    }

    @GetMapping("/{variantId}")
    public ProductVariantResponse get(
            @PathVariable UUID productId,
            @PathVariable UUID variantId
    ){
        ProductVariant productVariant = getProductVariantUseCase.execute(variantId, productId);

        return ProductVariantResponse.from(productVariant);
    }

    @GetMapping
    public List<ProductVariantResponse> list(@PathVariable UUID productId)
    {
        return listProductVariantUseCase.execute(productId)
                .stream()
                .map(ProductVariantResponse::from)
                .toList();
    }

    @PutMapping("/{variantId}")
    public ProductVariantResponse update(@PathVariable UUID productId, @PathVariable UUID variantId, @Valid @RequestBody UpdateProductVariantRequest request){
        ProductVariant productVariant = updateProductVariantUseCase.execute(
                variantId,
                productId,
                request.quantity(),
                request.measurementUnit(),
                request.price()
        );

        return ProductVariantResponse.from(productVariant);
    }

    @PatchMapping("/{variantId}/available")
    public ProductVariantResponse markAvailable(
            @PathVariable UUID productId,
            @PathVariable UUID variantId
    ){
        ProductVariant productVariant = markProductVariantAvailableUseCase.execute(variantId, productId);

        return ProductVariantResponse.from(productVariant);
    }

    @PatchMapping("/{variantId}/sold-out")
    public ProductVariantResponse markSoldOut(
            @PathVariable UUID productId,
            @PathVariable UUID variantId
    ){
        ProductVariant productVariant = markProductVariantSoldOutUseCase.execute(variantId, productId);

        return ProductVariantResponse.from(productVariant);
    }

}
