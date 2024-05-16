//Cristian Ruiz Martín y Mikolaj Zabski

package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.RepositoryMedico;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MedicoRepositoryIT {

    @Autowired
    private RepositoryMedico medicoRepository;

    @Test
    @DisplayName("Check medico is persited in the BBDD when created")
    void givenMedicoEntity_whenSaveUser_thenUserIsPersisted() {
        // arrange
        // arrange
        Medico medico = new Medico("123", "Dr. Smith", "Cardiology");

        // act
        medicoRepository.save(medico);

        // assert
        Optional<Medico> retrievedMedico = medicoRepository.findById(1L);
        assertTrue(retrievedMedico.isPresent());
        assertEquals("Dr. Smith", retrievedMedico.get().getNombre());
        assertEquals("123", retrievedMedico.get().getDni());
        assertEquals("Cardiology", retrievedMedico.get().getEspecialidad());
        //assertEquals(1, retrievedMedico.get().getId()); WRONG, we cannot assume the DB is empty


    }
}
