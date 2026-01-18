package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.request.PaymentForm;
import com.br.pet_shop_management.api.dto.response.PaymentDTO;
import com.br.pet_shop_management.application.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments/{appointmentId:\\d+}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDTO register(@PathVariable Long appointmentId, @Valid @RequestBody PaymentForm form) {
        return paymentService.registerPayment(appointmentId, form);
    }
}
