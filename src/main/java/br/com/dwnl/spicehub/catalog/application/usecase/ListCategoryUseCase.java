package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListCategoryUseCase {
    private final CategoryRepository categoryRepository;

    public ListCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> execute(){
        return categoryRepository.findAll();
    }
}
