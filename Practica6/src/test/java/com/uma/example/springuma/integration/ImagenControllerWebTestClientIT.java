//Cristian Ruiz Martín y Mikolaj Zabski

package com.uma.example.springuma.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.Duration;
import java.util.Calendar;
import java.util.stream.Stream;

import com.uma.example.springuma.integration.base.AbstractIntegration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImagenControllerWebTestClientIT extends AbstractIntegration {


    @LocalServerPort
    private Integer port;

    private WebTestClient webTestClient;

    private Imagen imagen;
    private Medico medico;
    private Paciente paciente;

    private static final File healthyImage= new File("src/test/resources/healthy.png");
    private static final File notHealthyImage= new File("src/test/resources/no_healthty.png");

    @PostConstruct
    public void init() throws Exception {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofMillis(30000))
                .build();

        imagen = new Imagen();
        imagen.setNombre("Imagen 1");
        Calendar fecha = Calendar.getInstance();
        fecha.set(2021, Calendar.DECEMBER, 12);
        imagen.setFecha(Calendar.getInstance());
        imagen.setId(1);

        medico = new Medico("123a","Dr. Smith", "Cardiology");
        paciente = new Paciente("John Doe",69,"12/12/2021","1234B",medico);
        paciente.setId(1);
        medico.setId(1);
        paciente.setMedico(medico);

        // Creación del medico y del paciente en la base de datos
        webTestClient.post().uri("/medico")
                .body(Mono.just(medico), Medico.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult();
        paciente.setMedico(medico);

        webTestClient.post().uri("/paciente")
                .body(Mono.just(paciente), Paciente.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult();
    }

    private static Stream<Arguments> provideImagesForTesting() {
        return Stream.of(
                Arguments.of(healthyImage),
                Arguments.of(notHealthyImage)
        );
    }

    @ParameterizedTest
    @MethodSource("provideImagesForTesting")
    @DisplayName("Upload an image and check if it was uploaded correctly")
    public void uploadImage_WithValidData_ReturnsSuccess(File imageFile) throws Exception {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("paciente", paciente);
        builder.part("image", new FileSystemResource(healthyImage));

        // Subimos la imagen a la base de datos
        FluxExchangeResult<String> responseBody = webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is2xxSuccessful().returnResult(String.class);

        String result = responseBody.getResponseBody().blockFirst();

        assertEquals("{\"response\" : \"file uploaded successfully : healthy.png\"}", result);

        // Confirmamos que un GET lo hace bien
        webTestClient.get().uri("/imagen/info/{id}", imagen.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange() // hace la peticion
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody().jsonPath("$.nombre").isEqualTo("healthy.png");
    }

    @Test
    @DisplayName("Upload a healthy image and get the correct response")
    public void predictImage_ThatIsHealthy_ReturnsCorrectResponse() throws Exception {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("paciente", paciente);
        builder.part("image", new FileSystemResource(healthyImage));
        builder.part("id", "1");
        builder.part("fecha", "12/12/2021");
        builder.part("medico_id", medico.getId());

        //
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is2xxSuccessful();

        FluxExchangeResult<String> result = webTestClient.get().uri("/imagen/predict/{id}", imagen.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class);



        String respuesta = result.getResponseBody().blockFirst();

        assertEquals("Not cancer (label 0)", respuesta.substring(16, 36));
    }

    @Test
    @DisplayName("Upload a not healthy image and get the correct response")
    public void predictImage_ThatIsNotHealthy_ReturnsCorrectResponse() throws Exception {

        // Guardo el archivo de la imagen
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("paciente", paciente);
        builder.part("image", new FileSystemResource(notHealthyImage));

        // Subimos la imagen
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is2xxSuccessful();

        // Comprobamos que la clasificación es correcta
        FluxExchangeResult<String> result = webTestClient.get().uri("/imagen/predict/{id}", imagen.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class);

        String respuesta = result.getResponseBody().blockFirst();

        assertEquals("Cancer (label 1)", respuesta.substring(16, 32));
    }

    @Test
    @DisplayName("getImagenes, given a valid paciente and a valid id returns a list of images")
    public void getImagenes_ValidPacienteAndId_ReturnsListOfImages() throws Exception {
        // Crear y guardar una imagen
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("paciente", paciente);
        builder.part("image", new FileSystemResource(healthyImage));

        // Subir la imagen a la base de datos
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is2xxSuccessful();

        // Obtener la lista de imagenes del paciente
        webTestClient.get().uri("/imagen/paciente/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[0].nombre").isEqualTo("healthy.png");
    }

    @Test
    @DisplayName("given a valid valid image id, download gets same data")
    public void downloadImage_ValidId_ReturnsSameData() throws Exception {
        // Crear y guardar una imagen

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("paciente", paciente);
        builder.part("image", new FileSystemResource(healthyImage));
        builder.part("id","1");

        // Subir la imagen a la base de datos
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is2xxSuccessful();

        // Descargar la imagen
        webTestClient.get().uri("/imagen/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "image/png");
    }

    @Test
    @DisplayName("given a valid image id, delete removes the image")
    public void deleteImage_ValidId_RemovesImage() throws Exception {
        // Crear y guardar una imagen
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("paciente", paciente);
        builder.part("image", new FileSystemResource(healthyImage));

        // Subir la imagen a la base de datos
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is2xxSuccessful();

        // Eliminar la imagen
        webTestClient.delete().uri("/imagen/1")
                .exchange()
                .expectStatus().isNoContent();
    }

}