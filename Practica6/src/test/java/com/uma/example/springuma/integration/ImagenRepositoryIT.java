package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import com.uma.example.springuma.model.RepositoryImagen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ImagenRepositoryIT {

    @Autowired
    private RepositoryImagen imagenRepository;

    @Test
    @DisplayName("Check imagen is persited in the BBDD when created")
    void givenImagenEntity_whenSaveUser_thenUserIsPersisted() {
        // arrange
        Calendar Fecha = Calendar.getInstance();
        Fecha.set(2021, Calendar.DECEMBER, 12);
        Medico medico = new Medico("123", "Dr. Smith", "Cardiology");
        Paciente paciente = new Paciente("Alumno", 16, "12/12/2021", "122", medico);
        byte[] fileContent = new byte[10];
        //randomize fileContent
        for (int i = 0; i < fileContent.length; i++) {
            fileContent[i] = (byte) (Math.random() * 256);
        }

        Imagen imagen = new Imagen();
        imagen.setNombre("imagen1");
        imagen.setId(1);
        imagen.setPaciente(paciente);
        imagen.setFecha(Fecha);
        imagen.setFile_content(fileContent);

        imagen.setFecha(Fecha);

        // act
        imagenRepository.save(imagen);

        // assert
        Optional<Imagen> retrievedImagen = imagenRepository.findById(1L);
        assertTrue(retrievedImagen.isPresent());
        assertEquals("imagen1", retrievedImagen.get().getNombre());
        assertEquals(1, retrievedImagen.get().getId());
        assertEquals(paciente.getId(), retrievedImagen.get().getPaciente().getId());
        assertEquals(Fecha, retrievedImagen.get().getFecha());
        assertEquals(Arrays.toString(fileContent), Arrays.toString(retrievedImagen.get().getFile_content()));


    }
}
