package com.rodriguezruiz.prestamoservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PrestamoRequest(
        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 100, message = "El cliente no puede superar 100 caracteres")
        String cliente,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos")
        String dni,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El monto tiene formato inválido")
        BigDecimal monto,

        @NotNull(message = "El interés es obligatorio")
        @DecimalMin(value = "0.0", message = "El interés no puede ser negativo")
        @DecimalMax(value = "999.99", message = "El interés no puede superar 999.99")
        BigDecimal interes,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        LocalDate fechaVencimiento,

        Boolean pagado
) {}
