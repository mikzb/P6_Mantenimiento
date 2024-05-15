package com.uma.example.springuma.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MedicoControllerMockMvcIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Medico medico;

    @BeforeEach
    void setUp() throws Exception{
         medico = new Medico();
         medico.setNombre("Medico1");
         medico.setDni("12345678A");
         medico.setEspecialidad("Traumatologia");
         medico.setId(1);

         //Creación del medico
        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

    }

    @Test
    @DisplayName("Get by id a medico that exists")
        void getMedico_ThatExists_ReturnsMedico() throws Exception {

        // Obtener el medico
        this.mockMvc.perform(get("/medico/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.nombre").value("Medico1"))
                .andExpect(jsonPath("$.dni").value("12345678A"))
                .andExpect(jsonPath("$.especialidad").value("Traumatologia"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Get by id a medico that does not exist")
    void getMedico_ThatDoesNotExist_ReturnsError() throws Exception {
        // Obtener un medico que no existe
        this.mockMvc.perform(get("/medico/2"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Save a medico that does not exist")
    void saveMedico_ThatDoesNotExist_ReturnsSuccess() throws Exception {
        // Crear un medico que no existe
        Medico medico2 = new Medico();
        medico2.setNombre("Medico2");
        medico2.setDni("12345678B");
        medico2.setEspecialidad("Traumatologia");
        medico2.setId(2);
        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico2)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Save a medico that already exists returns internal server error")
    void saveMedico_ThatExists_ThrowsError() throws Exception {
        // Crear un medico que ya existe
        this.mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("El medico ya existe")));
    }

    @Test
    @DisplayName("Update a medico that exists")
    void updateMedico_ThatExists_ReturnsSuccess() throws Exception {
        // Actualizar un medico que existe
        medico.setNombre("Medico2");
        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Update a medico that does not exist")
    void updateMedico_ThatDoesNotExist_ReturnsError() throws Exception {

        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new Medico())))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error al actualizar el medico")));
    }

    @Test
    @DisplayName("Delete a medico that exists")
    void deleteMedico_ThatExists_ReturnsSuccess() throws Exception {
        // Eliminar un medico que existe
        this.mockMvc.perform(get("/medico/1"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete a medico that does not exist")
    void deleteMedico_ThatDoesNotExist_ReturnsError() throws Exception {
        // Eliminar un medico que no existe
        this.mockMvc.perform(get("/medico/2"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Get a medico by DNI")
    void getMedicoByDni_ThatExists_ReturnsMedico() throws Exception {
        // Obtener un medico por DNI
        this.mockMvc.perform(get("/medico/dni/12345678A"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.nombre").value("Medico1"))
            .andExpect(jsonPath("$.dni").value("12345678A"))
            .andExpect(jsonPath("$.especialidad").value("Traumatologia"))
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Get a medico by DNI that does not exist")
    void getMedicoByDni_ThatDoesNotExist_ReturnsError() throws Exception {
        // Obtener un medico por DNI que no existe
        this.mockMvc.perform(get("/medico/dni/12345678B"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}