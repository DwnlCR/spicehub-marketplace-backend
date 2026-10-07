package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductNotFoundException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantAlreadyExistsException;
import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductRepository;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CreateProductVariantUseCase {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public CreateProductVariantUseCase(ProductRepository productRepository, ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public ProductVariant execute(UUID productId, int quantity, MeasurementUnit measurementUnit, BigDecimal price){
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (productVariantRepository.existsByProductIdAndQuantityAndMeasurementUnit(productId, quantity, measurementUnit)){
            throw new ProductVariantAlreadyExistsException("Product variant already exists");
        }

        ProductVariant variant = ProductVariant.create(productId, quantity, measurementUnit, price);

        return productVariantRepository.save(variant);
    }
}
