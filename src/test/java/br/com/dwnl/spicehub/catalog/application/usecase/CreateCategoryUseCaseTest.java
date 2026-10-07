package br.com.dwnl.spicehub.catalog.application.usecase;

import br.com.dwnl.spicehub.catalog.application.exception.CategoryAlreadyExistsException;
import br.com.dwnl.spicehub.catalog.domain.model.Category;
import br.com.dwnl.spicehub.catalog.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CreateCategoryUseCase createCategoryUseCase;

    @Test
    void shouldCreateCategory() {
        when(categoryRepository.existsByNameIgnoreCase("Chás"))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category result = createCategoryUseCase.execute("Chás");

        assertEquals("Chás", result.getName());
        assertTrue(result.isActive());

        verify(categoryRepository).existsByNameIgnoreCase("Chás");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowWhenCategoryAlreadyExists() {
        when(categoryRepository.existsByNameIgnoreCase("Chás"))
                .thenReturn(true);

        assertThrows(
                CategoryAlreadyExistsException.class,
                () -> createCategoryUseCase.execute("Chás")
        );

        verify(categoryRepository, never()).save(any());
    }
}