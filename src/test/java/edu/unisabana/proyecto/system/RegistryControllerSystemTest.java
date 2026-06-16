package edu.unisabana.proyecto.system;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

/**
 * Pruebas de <b>sistema (end-to-end)</b> sobre la capa REST.
 *
 * <p>Levantan el contexto completo de Spring Boot en un puerto aleatorio y
 * ejercitan el endpoint real {@code POST /register} con {@link TestRestTemplate},
 * validando el codigo de estado HTTP y el contenido de la respuesta.</p>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistryControllerSystemTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private RegistryRepositoryPort repo;

    @Before
    public void cleanDatabase() throws Exception {
        // Aislar cada prueba: la base H2 se comparte en el contexto de la app.
        repo.deleteAll();
    }

    private ResponseEntity<String> post(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity("/register", new HttpEntity<>(json, headers), String.class);
    }

    @Test
    public void shouldReturnValidWhenPostValidPerson() {
        // Arrange
        String json = "{\"name\":\"Ana\",\"id\":100,\"age\":30,\"gender\":\"FEMALE\",\"alive\":true}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("VALID", resp.getBody());
    }

    @Test
    public void shouldReturnUnderageWhenPostMinor() {
        // Arrange
        String json = "{\"name\":\"Pedro\",\"id\":101,\"age\":15,\"gender\":\"MALE\",\"alive\":true}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("UNDERAGE", resp.getBody());
    }

    @Test
    public void shouldReturnDuplicatedWhenSameDocumentPostedTwice() {
        // Arrange
        String json = "{\"name\":\"Ana\",\"id\":102,\"age\":30,\"gender\":\"FEMALE\",\"alive\":true}";

        // Act
        post(json);                          // primera inscripcion
        ResponseEntity<String> resp = post(json); // segunda con mismo documento

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("DUPLICATED", resp.getBody());
    }

    @Test
    public void shouldReturnBadRequestWhenGenderIsInvalid() {
        // Arrange: genero no reconocido -> debe ser 400, no 500
        String json = "{\"name\":\"Zoe\",\"id\":103,\"age\":30,\"gender\":\"OTRO\",\"alive\":true}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}
