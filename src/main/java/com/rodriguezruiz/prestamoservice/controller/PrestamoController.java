package com.rodriguezruiz.prestamoservice.controller;

import com.rodriguezruiz.prestamoservice.dto.PrestamoRequest;
import com.rodriguezruiz.prestamoservice.dto.PrestamoResponse;
import com.rodriguezruiz.prestamoservice.dto.ResumenResponse;
import com.rodriguezruiz.prestamoservice.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public ResponseEntity<List<PrestamoResponse>> listar(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(prestamoService.listar(q));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenResponse> resumen() {
        return ResponseEntity.ok(prestamoService.resumen());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<PrestamoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<PrestamoResponse> crear(@Valid @RequestBody PrestamoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoService.crear(request));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<PrestamoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PrestamoRequest request) {
        return ResponseEntity.ok(prestamoService.actualizar(id, request));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        prestamoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
