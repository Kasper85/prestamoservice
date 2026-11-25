package com.rodriguezruiz.prestamoservice.dto;

import com.rodriguezruiz.prestamoservice.entity.Prestamo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record PrestamoResponse(
        Long id,
        String cliente,
        String dni,
        BigDecimal monto,
        BigDecimal interes,
        BigDecimal totalACobrar,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        Boolean pagado
) {
    public static BigDecimal calcularTotalACobrar(BigDecimal monto, BigDecimal interes) {
        BigDecimal factor = interes.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return monto.add(monto.multiply(factor)).setScale(2, RoundingMode.HALF_UP);
    }

    public static PrestamoResponse fromEntity(Prestamo p) {
        return new PrestamoResponse(
                p.getId(),
                p.getCliente(),
                p.getDni(),
                p.getMonto(),
                p.getInteres(),
                calcularTotalACobrar(p.getMonto(), p.getInteres()),
                p.getFechaPrestamo(),
                p.getFechaVencimiento(),
                p.getPagado()
        );
    }
}
