package br.com.dwnl.spicehub.cart.presentation.controller;

import br.com.dwnl.spicehub.cart.application.usecase.*;
import br.com.dwnl.spicehub.cart.presentation.dto.AddCartItemRequest;
import br.com.dwnl.spicehub.cart.presentation.dto.CartResponse;
import br.com.dwnl.spicehub.cart.presentation.dto.UpdateCartItemQuantityRequest;
import br.com.dwnl.spicehub.cart.presentation.mapper.CartResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartResponseMapper cartResponseMapper;
    private final GetCartUseCase getCartUseCase;
    private final AddCartItemUseCase addCartItemUseCase;
    private final UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final ClearCartUseCase clearCartUseCase;


    @GetMapping
    public ResponseEntity<CartResponse> getCart(){
        CartResponse response = cartResponseMapper.toResponse(getCartUseCase.execute());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<Void> addItem(@Valid @RequestBody AddCartItemRequest request){
        addCartItemUseCase.execute(request.variantId(), request.quantity());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{variantId}")
    public ResponseEntity<Void> updateItemQuantity(@PathVariable UUID variantId, @Valid @RequestBody UpdateCartItemQuantityRequest request){
        updateCartItemQuantityUseCase.execute(variantId, request.quantity());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> clearCart(){
        clearCartUseCase.execute();

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{variantId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID variantId){
        removeCartItemUseCase.execute(variantId);

        return ResponseEntity.noContent().build();
    }
}
