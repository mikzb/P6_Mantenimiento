package com.uma.example.springuma.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PacienteControllerMockMvcIT extends AbstractIntegration {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Paciente paciente;

    @BeforeEach
    void setUp() throws Exception{
        paciente = new Paciente();
        paciente.setNombre("Paciente1");
        paciente.setDni("12345678A");
        paciente.setEdad(30);
        paciente.setId(1);

        //Creación del paciente
        this.mockMvc.perform(post("/paciente")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(paciente)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Get by id a paciente that exists")
    void getPaciente_ThatExists_ReturnsPaciente() throws Exception {

        // Obtener el paciente
        this.mockMvc.perform(get("/paciente/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.nombre").value("Paciente1"))
                .andExpect(jsonPath("$.dni").value("12345678A"))
                .andExpect(jsonPath("$.edad").value(30))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Get by id a paciente that does not exist")
    void getPaciente_ThatDoesNotExist_ReturnsNotFound() throws Exception {

        // Obtener el paciente
        this.mockMvc.perform(get("/paciente/2"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Get all pacientes from a medico")
    void getPacientesFromMedico_ReturnsPacientes() throws Exception {
        // Crear medico
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("Medico1");
        medico.setDni("92345678A");
        medico.setEspecialidad("Traumatologia");

        // Guardar medico
        this.mockMvc.perform(post("/medico")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(medico)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());


        // Crear nuevo pacientes
        Paciente paciente2 = new Paciente();
        paciente2.setNombre("Paciente2");
        paciente2.setDni("12345678B");
        paciente2.setEdad(30);
        paciente2.setId(2);
        paciente2.setMedico(medico);

        Paciente paciente3 = new Paciente();
        paciente3.setNombre("Paciente3");
        paciente3.setDni("12345678C");
        paciente3.setEdad(45);
        paciente3.setId(3);
        paciente3.setMedico(medico);

        // Guardar pacientes
        this.mockMvc.perform(post("/paciente")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(paciente2)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(post("/paciente")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(paciente3)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        // Obtener el paciente
        this.mockMvc.perform(get("/paciente/medico/{id}", medico.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Paciente2"))
                .andExpect(jsonPath("$[0].dni").value("12345678B"))
                .andExpect(jsonPath("$[0].edad").value(30))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Paciente3"))
                .andExpect(jsonPath("$[1].dni").value("12345678C"))
                .andExpect(jsonPath("$[1].edad").value(45))
                .andExpect(jsonPath("$[1].id").value(3));

    }
}
