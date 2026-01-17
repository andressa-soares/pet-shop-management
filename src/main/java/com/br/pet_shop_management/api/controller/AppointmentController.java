package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.request.AppointmentForm;
import com.br.pet_shop_management.api.dto.request.AppointmentItemForm;
import com.br.pet_shop_management.api.dto.response.AppointmentDTO;
import com.br.pet_shop_management.application.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/{id:\\d+}")
    public AppointmentDTO findById(@PathVariable Long id) {
        return appointmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDTO createAppointment(@Valid @RequestBody AppointmentForm form) {
        return appointmentService.createAppointment(form);
    }

    @PostMapping("/{id:\\d+}/items")
    public AppointmentDTO addAppointmentItems(@PathVariable Long id, @Valid @RequestBody List<AppointmentItemForm> items) {
        return appointmentService.addAppointmentItems(id, items);
    }

    @PatchMapping("/{id:\\d+}/close")
    public AppointmentDTO closeForPayment(@PathVariable Long id) {
        return appointmentService.closeForPayment(id);
    }

    @PatchMapping("/{id:\\d+}/cancel")
    public AppointmentDTO cancel(@PathVariable Long id) {
        return appointmentService.cancelAppointment(id);
    }
}