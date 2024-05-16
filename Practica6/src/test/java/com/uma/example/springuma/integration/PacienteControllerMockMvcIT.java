//Cristian Ruiz Martín y Mikolaj Zabski

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private Medico medico;

    @BeforeEach
    void setUp() throws Exception{
        // Crear medico
        medico = new Medico();
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


        // Crear paciente
        paciente = new Paciente();
        paciente.setNombre("Paciente1");
        paciente.setDni("12345678A");
        paciente.setEdad(30);
        paciente.setId(1);
        paciente.setMedico(medico);

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
                .andExpect(jsonPath("$.nombre").value(paciente.getNombre()))
                .andExpect(jsonPath("$.dni").value(paciente.getDni()))
                .andExpect(jsonPath("$.edad").value(paciente.getEdad()))
                .andExpect(jsonPath("$.id").value(paciente.getId()));

    }

    @Test
    @DisplayName("Get all pacientes from a medico")
    void getPacientesFromMedico_ReturnsPacientes() throws Exception {


        // Crear nuevo paciente

        Paciente paciente2 = new Paciente();
        paciente2.setNombre("Paciente2");
        paciente2.setDni("12345678C");
        paciente2.setEdad(45);
        paciente2.setId(2);
        paciente2.setMedico(medico);

        // Guardar paciente

        this.mockMvc.perform(post("/paciente")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(paciente2)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        // Obtener los pacientes (paciente y paciente2) del medico
        this.mockMvc.perform(get("/paciente/medico/{id}", medico.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value(paciente.getNombre()))
                .andExpect(jsonPath("$[0].dni").value(paciente.getDni()))
                .andExpect(jsonPath("$[0].edad").value(paciente.getEdad()))
                .andExpect(jsonPath("$[0].id").value(paciente.getId()))
                .andExpect(jsonPath("$[1].nombre").value(paciente2.getNombre()))
                .andExpect(jsonPath("$[1].dni").value(paciente2.getDni()))
                .andExpect(jsonPath("$[1].edad").value(paciente2.getEdad()))
                .andExpect(jsonPath("$[1].id").value(paciente2.getId()));

    }

    @Test
    @DisplayName("Reassign a medico to a paciente that already has a medico")
    public void reassignMedicoToPaciente_ReturnsPaciente() throws Exception {
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


        // Crear nuevo paciente con medico1 asignado
        Paciente paciente2 = new Paciente();
        paciente2.setNombre("Paciente2");
        paciente2.setDni("12345678B");
        paciente2.setEdad(30);
        paciente2.setId(2);
        paciente2.setMedico(medico);

        // Guardar paciente
        this.mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente2)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        // Comprobar que el paciente tiene el medico asignado
        this.mockMvc.perform(get("/paciente/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(paciente2.getId()))
                .andExpect(jsonPath("$.medico.id").value(medico.getId()));

        // Crear medico
        Medico medico2 = new Medico();
        medico2.setId(2);
        medico2.setNombre("Medico2");
        medico2.setDni("92345678B");
        medico2.setEspecialidad("Traumatologia");

        // Guardar medico
        this.mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico2)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        // Reasignar medico
        paciente2.setMedico(medico2);

        
        // Actualizar paciente
        this.mockMvc.perform(put("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente2)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        // Comprobar que el paciente tiene el nuevo medico asignado
        this.mockMvc.perform(get("/paciente/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(paciente2.getId()))
                .andExpect(jsonPath("$.medico.id").value(medico2.getId()));
        }

    @Test
    @DisplayName("Delete a paciente that exists")
    void deletePaciente_ThatExists_ReturnsSuccess() throws Exception {
        // Eliminar el paciente
        this.mockMvc.perform(delete("/paciente/1"))
                .andDo(print())
                .andExpect(status().isOk());

        // Comprobar que el paciente ha sido eliminado
        this.mockMvc.perform(get("/paciente/1"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Update a paciente that exists with new data")
    void updatePaciente_ThatExists_ReturnsSuccess() throws Exception {
        // Cambiar los datos del paciente
        paciente.setNombre("Paciente2");
        paciente.setDni("12345678B");
        paciente.setEdad(45);
        paciente.setId(1);

        // Actualizar el paciente
        this.mockMvc.perform(put("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        // Comprobar que el paciente ha sido actualizado
        this.mockMvc.perform(get("/paciente/{id}", paciente.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.nombre").value(paciente.getNombre()))
                .andExpect(jsonPath("$.dni").value(paciente.getDni()))
                .andExpect(jsonPath("$.edad").value(paciente.getEdad()))
                .andExpect(jsonPath("$.id").value(paciente.getId()));
    }
}
