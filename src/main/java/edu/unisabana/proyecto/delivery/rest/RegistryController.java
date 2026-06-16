package edu.unisabana.proyecto.delivery.rest;

import edu.unisabana.proyecto.application.usecase.Registry;
import edu.unisabana.proyecto.domain.model.Gender;
import edu.unisabana.proyecto.domain.model.Person;
import edu.unisabana.proyecto.domain.model.RegisterResult;
import edu.unisabana.proyecto.domain.model.Stats;
import edu.unisabana.proyecto.domain.model.rq.PersonDTO;
import edu.unisabana.proyecto.infrastructure.persistence.RegistryRecord;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de la Registraduria.
 *
 * <ul>
 *   <li>{@code POST /register} - registra un votante (texto plano con el resultado).</li>
 *   <li>{@code GET /voters/{id}} - consulta un votante (404 si no existe).</li>
 *   <li>{@code GET /voters} - lista los votantes registrados.</li>
 *   <li>{@code DELETE /voters/{id}} - elimina un votante (404 si no existe).</li>
 *   <li>{@code GET /stats} - estadisticas agregadas del padron.</li>
 * </ul>
 */
@RestController
public class RegistryController {

    private final Registry registry;

    public RegistryController(Registry registry) {
        this.registry = registry;
    }

    @PostMapping(path = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String register(@RequestBody PersonDTO dto) {
        Gender gender = parseGender(dto.getGender());
        Person p = new Person(dto.getName(), dto.getId(), dto.getAge(), gender, dto.isAlive());
        RegisterResult result = registry.registerVoter(p);
        return result.name();
    }

    @GetMapping(path = "/voters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegistryRecord> getVoter(@PathVariable int id) {
        return registry.findVoter(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/voters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RegistryRecord> listVoters() {
        return registry.listVoters();
    }

    @DeleteMapping(path = "/voters/{id}")
    public ResponseEntity<Void> deleteVoter(@PathVariable int id) {
        boolean removed = registry.removeVoter(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public Stats stats() {
        return registry.getStats();
    }

    /**
     * Convierte el genero recibido como texto a {@link Gender}. Un valor nulo o
     * no reconocido se trata como entrada invalida (HTTP 400), no como error
     * interno (HTTP 500).
     */
    private Gender parseGender(String value) {
        if (value == null) {
            throw new IllegalArgumentException("El genero es obligatorio");
        }
        try {
            return Gender.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Genero no valido: " + value);
        }
    }
}
