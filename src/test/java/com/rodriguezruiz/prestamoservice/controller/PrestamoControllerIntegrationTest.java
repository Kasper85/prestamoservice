package com.rodriguezruiz.prestamoservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrestamoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndGetToken(String nombre, String email, String password) throws Exception {
        String body = """
                {"nombre":"%s","email":"%s","password":"%s"}
                """.formatted(nombre, email, password);

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void sinTokenYTokenAlterado_responden401Json() throws Exception {
        mockMvc.perform(get("/prestamos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").isNotEmpty());

        mockMvc.perform(get("/prestamos")
                        .header("Authorization", "Bearer token.alterado.invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void registroDuplicadoYLoginInvalido_responden409Y401() throws Exception {
        registerAndGetToken("Ana", "ana@test.com", "secret123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana2\",\"email\":\"ana@test.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isNotEmpty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@test.com\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    void crudAislamientoEntrePrestamistasYCalculoTotal() throws Exception {
        String tokenA = registerAndGetToken("Prestamista A", "a@test.com", "pass1234");
        String tokenB = registerAndGetToken("Prestamista B", "b@test.com", "pass1234");

        String vencimiento = LocalDate.now().plusDays(15).toString();
        String createBody = """
                {
                  "cliente": "Carlos Perez",
                  "dni": "12345678",
                  "monto": 1000.00,
                  "interes": 15.50,
                  "fechaVencimiento": "%s"
                }
                """.formatted(vencimiento);

        MvcResult createResult = mockMvc.perform(post("/prestamos")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalACobrar").value(1155.00))
                .andExpect(jsonPath("$.pagado").value(false))
                .andExpect(jsonPath("$.fechaPrestamo").value(LocalDate.now().toString()))
                .andReturn();

        long prestamoId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // A ve su prestamo y resumen
        mockMvc.perform(get("/prestamos")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cliente").value("Carlos Perez"));

        mockMvc.perform(get("/prestamos?q=1234")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/prestamos/resumen")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capitalPrestado").value(1000.00))
                .andExpect(jsonPath("$.totalPorCobrar").value(1155.00))
                .andExpect(jsonPath("$.gananciaEsperada").value(155.00))
                .andExpect(jsonPath("$.cantidadPrestamos").value(1));

        // B no ve ni puede modificar ni eliminar el prestamo de A (404)
        mockMvc.perform(get("/prestamos")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/prestamos/" + prestamoId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/prestamos/" + prestamoId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/prestamos/" + prestamoId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // A marca como pagado y luego elimina
        String updateBody = """
                {
                  "cliente": "Carlos Perez",
                  "dni": "12345678",
                  "monto": 1000.00,
                  "interes": 15.50,
                  "fechaVencimiento": "%s",
                  "pagado": true
                }
                """.formatted(vencimiento);

        mockMvc.perform(put("/prestamos/" + prestamoId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagado").value(true));

        mockMvc.perform(delete("/prestamos/" + prestamoId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }
}
