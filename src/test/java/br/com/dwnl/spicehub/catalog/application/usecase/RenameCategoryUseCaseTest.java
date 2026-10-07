package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.CategoryAlreadyExistsException;
import br.com.dwnl.spicehub.catalog.application.exception.CategoryNotFoundException;
import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenameCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private RenameCategoryUseCase renameCategoryUseCase;

    @Test
    void shouldRenameCategory() {
        Category category = Category.create("Chás");
        UUID categoryId = category.getId();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNameIgnoreCase("Ervas"))
                .thenReturn(false);

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result = renameCategoryUseCase.execute(
                categoryId,
                "Ervas"
        );

        assertEquals("Ervas", result.getName());

        verify(categoryRepository).existsByNameIgnoreCase("Ervas");
        verify(categoryRepository).save(category);
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> renameCategoryUseCase.execute(categoryId, "Ervas")
        );

        verify(categoryRepository, never())
                .existsByNameIgnoreCase(anyString());

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAnotherCategoryHasSameName() {
        Category category = Category.create("Chás");
        UUID categoryId = category.getId();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNameIgnoreCase("Ervas"))
                .thenReturn(true);

        assertThrows(
                CategoryAlreadyExistsException.class,
                () -> renameCategoryUseCase.execute(
                        categoryId,
                        "Ervas"
                )
        );

        assertEquals("Chás", category.getName());

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldAllowChangingOnlyNameCase() {
        Category category = Category.create("Chás");
        UUID categoryId = category.getId();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result = renameCategoryUseCase.execute(
                categoryId,
                "CHÁS"
        );

        assertEquals("CHÁS", result.getName());

        verify(categoryRepository, never())
                .existsByNameIgnoreCase(anyString());

        verify(categoryRepository).save(category);
    }
}