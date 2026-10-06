package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.CategoryAlreadyExistsException;
import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(String name){
        if (categoryRepository.existsByNameIgnoreCase(name)){
            throw new CategoryAlreadyExistsException("Category already exists");
        }

        Category category = Category.create(name);

        return categoryRepository.save(category);
    }
}
