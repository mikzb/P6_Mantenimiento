package com.uma.example.springuma.unit.controller;

import com.uma.example.springuma.controller.MedicoController;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.MedicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicoControllerTest {

    @InjectMocks
    private MedicoController medicoController;

    @Mock
    private MedicoService medicoService;

    private Medico medico;

    @BeforeEach
    void setUp() {
        medico = new Medico();
    }

    @Test
    @DisplayName("When creating a new medico the medico is created in the service and the necessary methods and invoked")
    void saveMedico_WhenCalledController_MedicoServiceSaveMedicoAndGet201Code()  {
        // arrange
        medico = new Medico();
        medico.setNombre("name");
        medico.setDni("122");
        medico.setEspecialidad("Cardiology");
        medico.setId(1);
        doReturn(medico).when(medicoService).addMedico(any());

        // act
        ResponseEntity<String> savedMedico = (ResponseEntity<String>) medicoController.saveMedico(medico);

        // asset
        verify(medicoService, times(1)).addMedico(medico);
        assertEquals(HttpStatus.CREATED, savedMedico.getStatusCode());
    }
}
