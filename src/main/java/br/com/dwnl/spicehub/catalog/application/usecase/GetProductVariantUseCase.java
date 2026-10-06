package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.ProductVariantNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.ProductVariant;
import br.com.dwnl.spicehub.catalog.domain.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetProductVariantUseCase {

    private final ProductVariantRepository variantRepository;

    public GetProductVariantUseCase(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    public ProductVariant execute(UUID variantId){
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product variant not found"));
    }
}
