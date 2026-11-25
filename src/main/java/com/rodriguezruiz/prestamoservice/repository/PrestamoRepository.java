package com.rodriguezruiz.prestamoservice.repository;

import com.rodriguezruiz.prestamoservice.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuarioEmailOrderByFechaVencimientoAsc(String email);

    Optional<Prestamo> findByIdAndUsuarioEmail(Long id, String email);

    @Query("SELECT p FROM Prestamo p WHERE p.usuario.email = :email AND " +
           "(LOWER(p.cliente) LIKE LOWER(CONCAT('%', :q, '%')) OR p.dni LIKE CONCAT(:q, '%')) " +
           "ORDER BY p.fechaVencimiento ASC")
    List<Prestamo> buscarPorUsuario(@Param("email") String email, @Param("q") String q);
}
