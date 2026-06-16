package edu.unisabana.proyecto.system;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Prueba de <b>sistema</b> del endpoint raiz {@code GET /}, que expone un indice
 * (JSON) con la informacion del servicio y los endpoints disponibles, evitando el
 * 404 "Whitelabel" en la raiz.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HomeControllerSystemTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    public void shouldReturnApiInfoAtRoot() {
        // Act
        ResponseEntity<String> resp = rest.getForEntity("/", String.class);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        String body = resp.getBody();
        assertTrue(body.contains("Registraduria"));
        assertTrue(body.contains("/register"));
        assertTrue(body.contains("/voters"));
        assertTrue(body.contains("/stats"));
    }
}
