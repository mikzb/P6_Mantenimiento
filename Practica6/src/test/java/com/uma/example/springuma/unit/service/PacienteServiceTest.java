package com.uma.example.springuma.unit.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.uma.example.springuma.model.Paciente;
import com.uma.example.springuma.model.PacienteService;
import com.uma.example.springuma.model.RepositoryPaciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @InjectMocks
    private PacienteService pacienteService;
    @Mock
    private RepositoryPaciente pacienteRepository;

    private Paciente paciente;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cristian", "francisco"})
    @DisplayName("Happy Path Test: save paciente")
    void givenCorrectPacienteDTO_whenSavePaciente_thenReturnPacienteDTO(String name) {

        // arrange
        paciente = new Paciente();
        paciente.setNombre(name);
        paciente.setDni("122");
        paciente.setEdad(16);
        paciente.setId(1);

        doReturn(paciente).when(pacienteRepository).saveAndFlush(any());

        // act
        Paciente savePaciente = pacienteService.addPaciente(paciente);

        // assert
        verify(pacienteRepository).saveAndFlush(any());
        assertEquals(name, savePaciente.getNombre());
    }

    @Test
    @DisplayName("Test retrieving existing paciente by id should return the optional paciente")
    void givenPacienteId_whenGetPaciente_thenReturnOptionalPaciente() {
        // arrange
        paciente = new Paciente();
        paciente.setNombre("name");
        paciente.setDni("122");
        paciente.setEdad(16);
        paciente.setId(1);
        doReturn(paciente).when(pacienteRepository).getReferenceById(1L);

        // act
        Paciente retrievedPaciente = pacienteService.getPaciente(1L);

        // assert
        verify(pacienteRepository).getReferenceById(1L);
        assertEquals("name", retrievedPaciente.getNombre());
    }
}