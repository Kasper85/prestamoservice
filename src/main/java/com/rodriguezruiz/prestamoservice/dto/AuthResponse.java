package com.rodriguezruiz.prestamoservice.dto;

public record AuthResponse(
        String token,
        String nombre,
        String email
) {}
