package com.rodriguezruiz.prestamoservice.dto;

import java.math.BigDecimal;

public record ResumenResponse(
        BigDecimal capitalPrestado,
        BigDecimal totalPorCobrar,
        BigDecimal gananciaEsperada,
        long cantidadPrestamos,
        long cantidadVencidos
) {}
