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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Pruebas de <b>sistema (end-to-end)</b> de los endpoints de consulta y gestion:
 * {@code GET /voters/{id}}, {@code GET /voters}, {@code DELETE /voters/{id}} y
 * {@code GET /stats}. Levantan el contexto completo de Spring Boot en un puerto
 * aleatorio y validan codigos HTTP y contenido con {@link TestRestTemplate}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VotersControllerSystemTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private RegistryRepositoryPort repo;

    @Before
    public void cleanDatabase() throws Exception {
        repo.deleteAll();
    }

    private void register(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        rest.postForEntity("/register", new HttpEntity<>(json, headers), String.class);
    }

    private String voter(int id, String name, int age, String gender) {
        return String.format(
                "{\"name\":\"%s\",\"id\":%d,\"age\":%d,\"gender\":\"%s\",\"alive\":true}",
                name, id, age, gender);
    }

    @Test
    public void shouldGetVoterWhenExists() {
        // Arrange
        register(voter(100, "Ana", 30, "FEMALE"));

        // Act
        ResponseEntity<String> resp = rest.getForEntity("/voters/100", String.class);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().contains("Ana"));
    }

    @Test
    public void shouldReturn404WhenVoterNotFound() {
        // Act
        ResponseEntity<String> resp = rest.getForEntity("/voters/999", String.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    public void shouldListRegisteredVoters() {
        // Arrange
        register(voter(1, "Ana", 30, "FEMALE"));
        register(voter(2, "Beto", 40, "MALE"));

        // Act
        ResponseEntity<String> resp = rest.getForEntity("/voters", String.class);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().contains("Ana"));
        assertTrue(resp.getBody().contains("Beto"));
    }

    @Test
    public void shouldDeleteVoterAndThenReturn404() {
        // Arrange
        register(voter(100, "Ana", 30, "FEMALE"));

        // Act
        ResponseEntity<Void> del = rest.exchange("/voters/100", HttpMethod.DELETE, null, Void.class);
        ResponseEntity<String> afterGet = rest.getForEntity("/voters/100", String.class);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, del.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, afterGet.getStatusCode());
    }

    @Test
    public void shouldReturn404WhenDeletingNonExistentVoter() {
        // Act
        ResponseEntity<Void> del = rest.exchange("/voters/999", HttpMethod.DELETE, null, Void.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, del.getStatusCode());
    }

    @Test
    public void shouldReturnStatsOfRegisteredVoters() {
        // Arrange
        register(voter(1, "Ana", 30, "FEMALE"));
        register(voter(2, "Beto", 40, "MALE"));

        // Act
        ResponseEntity<String> resp = rest.getForEntity("/stats", String.class);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().contains("\"total\":2"));
        assertTrue(resp.getBody().contains("\"minAge\":30"));
        assertTrue(resp.getBody().contains("\"maxAge\":40"));
    }
}
