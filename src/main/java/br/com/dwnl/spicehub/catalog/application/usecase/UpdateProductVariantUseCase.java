package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantAlreadyExistsException;
import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.MeasurementUnit;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;


@Service
public class UpdateProductVariantUseCase {

    private final ProductVariantRepository productVariantRepository;

    public UpdateProductVariantUseCase(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    public ProductVariant execute(UUID variantId, UUID productId,int quantity, MeasurementUnit measurementUnit, BigDecimal price){
        ProductVariant variant = productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product variant not found"));

        if (productVariantRepository.existsByProductIdAndQuantityAndMeasurementUnitAndIdNot(
                variant.getProductId(),
                quantity,
                measurementUnit,
                variantId
        )){
            throw new ProductVariantAlreadyExistsException("Product variant already exists");
        }

        variant.changeMeasurement(quantity, measurementUnit);
        variant.changePrice(price);

        return productVariantRepository.save(variant);
    }
}
