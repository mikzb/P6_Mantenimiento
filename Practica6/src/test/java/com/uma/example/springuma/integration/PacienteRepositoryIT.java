package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import com.uma.example.springuma.model.RepositoryPaciente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PacienteRepositoryIT {

    @Autowired
    private RepositoryPaciente pacienteRepository;

    @Test
    @DisplayName("Check paciente is persited in the BBDD when created")
    void givenPacienteEntity_whenSaveUser_thenUserIsPersisted() {
        // arrange
        Medico medico = new Medico("123", "Dr. Smith", "Cardiology");
        Paciente paciente = new Paciente("Alumno", 16, "12/12/2021", "122", medico);

        // act
        pacienteRepository.save(paciente);

        // assert
        Optional<Paciente> retrievedPaciente = pacienteRepository.findById(paciente.getId());
        assertTrue(retrievedPaciente.isPresent());
        assertEquals(paciente.getNombre(), retrievedPaciente.get().getNombre());
        assertEquals(paciente.getDni(), retrievedPaciente.get().getDni());
        assertEquals(paciente.getEdad(), retrievedPaciente.get().getEdad());
        assertEquals(paciente.getCita(), retrievedPaciente.get().getCita());
        assertEquals(paciente.getMedico().getDni(), retrievedPaciente.get().getMedico().getDni());
    }


}
