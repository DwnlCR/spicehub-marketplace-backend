package br.com.dwnl.spicehub.catalog.presentation.http.controller;

import br.com.dwnl.spicehub.catalog.application.usecase.*;
import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.presentation.http.dto.CategoryResponse;
import br.com.dwnl.spicehub.catalog.presentation.http.dto.CreateCategoryRequest;
import br.com.dwnl.spicehub.catalog.presentation.http.dto.RenameCategoryRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final RenameCategoryUseCase renameCategoryUseCase;
    private final ActivateCategoryUseCase activateCategoryUseCase;
    private final DeactivateCategoryUseCase deactivateCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final ListCategoryUseCase listCategoryUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase, RenameCategoryUseCase renameCategoryUseCase, ActivateCategoryUseCase activateCategoryUseCase, DeactivateCategoryUseCase deactivateCategoryUseCase, GetCategoryUseCase getCategoryUseCase, ListCategoryUseCase listCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.renameCategoryUseCase = renameCategoryUseCase;
        this.activateCategoryUseCase = activateCategoryUseCase;
        this.deactivateCategoryUseCase = deactivateCategoryUseCase;
        this.getCategoryUseCase = getCategoryUseCase;
        this.listCategoryUseCase = listCategoryUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request){
        Category category = createCategoryUseCase.execute(request.name());

        return CategoryResponse.from(category);
    }

    @GetMapping("/{categoryId}")
    public CategoryResponse get(@PathVariable UUID categoryId){
        Category category = getCategoryUseCase.execute(categoryId);

        return CategoryResponse.from(category);
    }

    @GetMapping
    public List<CategoryResponse> list(){
        return listCategoryUseCase.execute()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse rename(@PathVariable UUID categoryId, @Valid @RequestBody RenameCategoryRequest request){
        Category category = renameCategoryUseCase.execute(categoryId, request.name());

        return CategoryResponse.from(category);
    }

    @PatchMapping("/{categoryId}/activate")
    public CategoryResponse activate(@PathVariable UUID categoryId){
        Category category = activateCategoryUseCase.execute(categoryId);

        return CategoryResponse.from(category);
    }

    @PatchMapping("/{categoryId}/deactivate")
    public CategoryResponse deactivate(@PathVariable UUID categoryId){
        Category category = deactivateCategoryUseCase.execute(categoryId);

        return CategoryResponse.from(category);
    }
}
