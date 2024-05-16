//Cristian Ruiz Martín y Mikolaj Zabski

package com.uma.example.springuma.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.time.Duration;
import java.util.Calendar;

import com.uma.example.springuma.integration.base.AbstractIntegration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Informe;
import com.uma.example.springuma.model.Paciente;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InformeControllerWebTestClientIT extends AbstractIntegration {

    @LocalServerPort
    private Integer port;

    private WebTestClient webTestClient;

    private Informe informe;

    @PostConstruct
    public void init() throws Exception {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofMillis(30000))
                .build();

        Paciente paciente = new Paciente();
        paciente.setId(1);

        webTestClient.post().uri("/paciente")
                .body(Mono.just(paciente), Paciente.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult();

        Imagen imagen = new Imagen();
        imagen.setNombre("Imagen 1");
        imagen.setFecha(Calendar.getInstance());
        imagen.setId(1);
        imagen.setPaciente(paciente);

        // Cargar y guardar el archivo de la imagen
        File file = new File("src/test/resources/" + "healthy.png");

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("paciente", paciente);
        builder.part("image", new FileSystemResource(file));

        // Subimos la imagen a la base de datos
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is2xxSuccessful();

        FluxExchangeResult<String> result = webTestClient.get().uri("/imagen/predict/1")
                .exchange()
                .expectStatus().isOk().returnResult(String.class); // comprueba que la respuesta es de tipo String

        informe = new Informe();
        informe.setId(1);
        informe.setImagen(imagen);
    }

    @Test
    @DisplayName("Upload an informe and check if it was uploaded correctly")
    public void uploadInforme_WithValidData_ReturnsSuccess() throws Exception {

        // Creación del informe
        webTestClient.post().uri("/informe")
                .body(Mono.just(informe), Informe.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult();

        //Obtención del informe
        FluxExchangeResult<Informe> result = webTestClient.get().uri("/informe/{id}", informe.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Informe.class);

        Informe informeObtenido = result.getResponseBody().blockFirst();

        assertEquals(informe.getId(), informeObtenido.getId());
        assertEquals(informe.getContenido(), informeObtenido.getContenido());

    }

    @Test
    @DisplayName("Upload an informe and delete it afterwards to check if it was deleted correctly")
    public void deleteInforme_ThatExists_ReturnsSuccess() throws Exception {

        // Creación del informe
        webTestClient.post().uri("/informe")
                .body(Mono.just(informe), Informe.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult();

        //Eliminación del informe
        webTestClient.delete().uri("/informe/{id}", informe.getId())
                .exchange()
                .expectStatus().isNoContent()
                .returnResult(String.class);

        //Comprobación de que el informe ha sido eliminado
        FluxExchangeResult<Informe> result = webTestClient.get().uri("/informe/{id}", informe.getId())
                .exchange()
                .returnResult(Informe.class);

        Informe informeObtenido = result.getResponseBody().blockFirst();

        assertNull(informeObtenido);

    }
}
