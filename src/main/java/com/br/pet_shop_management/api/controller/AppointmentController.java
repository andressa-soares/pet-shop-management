package com.br.pet_shop_management.api.controller;

import com.br.pet_shop_management.api.dto.request.AppointmentActionForm;
import com.br.pet_shop_management.api.dto.request.AppointmentForm;
import com.br.pet_shop_management.api.dto.request.AppointmentItemForm;
import com.br.pet_shop_management.api.dto.response.AppointmentDTO;
import com.br.pet_shop_management.application.service.AppointmentService;
import com.br.pet_shop_management.domain.enums.AppointmentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/future")
    public Page<AppointmentDTO> listFutureAppointments(@RequestParam(required = false) AppointmentStatus status, Pageable pageable) {
        return appointmentService.listFutureAppointments(status, pageable);
    }

    @GetMapping("/history")
    public Page<AppointmentDTO> listHistory(@RequestParam(required = false) AppointmentStatus status, Pageable pageable) {
        return appointmentService.listHistory(status, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDTO createAppointment(@Valid @RequestBody AppointmentForm form) {
        return appointmentService.createAppointment(form);
    }

    @PostMapping("/{id:\\d+}/items")
    public AppointmentDTO addAppointmentItems(@PathVariable Long id, @Valid @RequestBody List<@Valid AppointmentItemForm> items) {
        return appointmentService.addAppointmentItems(id, items);
    }

    @PostMapping("/{id:\\d+}/actions")
    public AppointmentDTO applyAction(@PathVariable Long id, @Valid @RequestBody AppointmentActionForm form) {
        return appointmentService.applyAction(id, form.action());
    }
}