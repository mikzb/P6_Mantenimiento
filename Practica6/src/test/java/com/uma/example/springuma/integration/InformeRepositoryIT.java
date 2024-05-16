//Cristian Ruiz Martín y Mikolaj Zabski

package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Informe;
import com.uma.example.springuma.model.RepositoryInforme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class InformeRepositoryIT {

    @Autowired
    private RepositoryInforme informeRepository;

    @Test
    @DisplayName("Check informe is persited in the BBDD when created")
    void givenInformeEntity_whenSaveUser_thenUserIsPersisted() {
        // arrange

        Imagen imagen = new Imagen();
        Informe informe = new Informe("prediction","content", imagen);



        // act
        informeRepository.save(informe);

        // assert
        Optional<Informe> retrievedInforme = informeRepository.findById(1L);
        assertTrue(retrievedInforme.isPresent());
        assertEquals("prediction", retrievedInforme.get().getPrediccion());
        assertEquals("content", retrievedInforme.get().getContenido());
        assertEquals(imagen.getId(), retrievedInforme.get().getImagen().getId());
    }
}
