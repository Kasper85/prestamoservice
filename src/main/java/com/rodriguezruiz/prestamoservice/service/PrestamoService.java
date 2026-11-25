package com.rodriguezruiz.prestamoservice.service;

import com.rodriguezruiz.prestamoservice.config.GlobalExceptionHandler.RecursoNoEncontradoException;
import com.rodriguezruiz.prestamoservice.config.GlobalExceptionHandler.ValidacionNegocioException;
import com.rodriguezruiz.prestamoservice.dto.PrestamoRequest;
import com.rodriguezruiz.prestamoservice.dto.PrestamoResponse;
import com.rodriguezruiz.prestamoservice.dto.ResumenResponse;
import com.rodriguezruiz.prestamoservice.entity.Prestamo;
import com.rodriguezruiz.prestamoservice.entity.Usuario;
import com.rodriguezruiz.prestamoservice.repository.PrestamoRepository;
import com.rodriguezruiz.prestamoservice.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;

    public PrestamoService(PrestamoRepository prestamoRepository, UsuarioRepository usuarioRepository) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listar(String q) {
        String email = currentUserEmail();
        List<Prestamo> lista = (q != null && !q.isBlank())
                ? prestamoRepository.buscarPorUsuario(email, q.trim())
                : prestamoRepository.findByUsuarioEmailOrderByFechaVencimientoAsc(email);
        return lista.stream().map(PrestamoResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public PrestamoResponse obtener(Long id) {
        Prestamo prestamo = prestamoRepository.findByIdAndUsuarioEmail(id, currentUserEmail())
                .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado"));
        return PrestamoResponse.fromEntity(prestamo);
    }

    @Transactional
    public PrestamoResponse crear(PrestamoRequest request) {
        if (request.fechaVencimiento().isBefore(LocalDate.now())) {
            throw new ValidacionNegocioException(
                    "fechaVencimiento",
                    "La fecha de vencimiento debe ser hoy o posterior"
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(currentUserEmail())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(request.cliente().trim());
        prestamo.setDni(request.dni().trim());
        prestamo.setMonto(request.monto());
        prestamo.setInteres(request.interes());
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaVencimiento(request.fechaVencimiento());
        prestamo.setPagado(false);
        prestamo.setUsuario(usuario);

        return PrestamoResponse.fromEntity(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse actualizar(Long id, PrestamoRequest request) {
        Prestamo prestamo = prestamoRepository.findByIdAndUsuarioEmail(id, currentUserEmail())
                .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado"));

        prestamo.setCliente(request.cliente().trim());
        prestamo.setDni(request.dni().trim());
        prestamo.setMonto(request.monto());
        prestamo.setInteres(request.interes());
        prestamo.setFechaVencimiento(request.fechaVencimiento());
        if (request.pagado() != null) {
            prestamo.setPagado(request.pagado());
        }

        return PrestamoResponse.fromEntity(prestamoRepository.save(prestamo));
    }

    @Transactional
    public void eliminar(Long id) {
        Prestamo prestamo = prestamoRepository.findByIdAndUsuarioEmail(id, currentUserEmail())
                .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado"));
        prestamoRepository.delete(prestamo);
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen() {
        List<Prestamo> lista = prestamoRepository.findByUsuarioEmailOrderByFechaVencimientoAsc(currentUserEmail());
        LocalDate hoy = LocalDate.now();

        BigDecimal capitalPrestado = BigDecimal.ZERO;
        BigDecimal totalPorCobrar = BigDecimal.ZERO;
        BigDecimal gananciaEsperada = BigDecimal.ZERO;
        long cantidadVencidos = 0;

        for (Prestamo p : lista) {
            BigDecimal total = PrestamoResponse.calcularTotalACobrar(p.getMonto(), p.getInteres());
            capitalPrestado = capitalPrestado.add(p.getMonto());
            gananciaEsperada = gananciaEsperada.add(total.subtract(p.getMonto()));

            if (!Boolean.TRUE.equals(p.getPagado())) {
                totalPorCobrar = totalPorCobrar.add(total);
                if (p.getFechaVencimiento().isBefore(hoy)) {
                    cantidadVencidos++;
                }
            }
        }

        return new ResumenResponse(
                capitalPrestado,
                totalPorCobrar,
                gananciaEsperada,
                lista.size(),
                cantidadVencidos
        );
    }
}
