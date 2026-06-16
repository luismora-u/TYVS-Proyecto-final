package edu.unisabana.proyecto.delivery.rest;

import edu.unisabana.proyecto.application.usecase.Registry;
import edu.unisabana.proyecto.domain.model.Gender;
import edu.unisabana.proyecto.domain.model.Person;
import edu.unisabana.proyecto.domain.model.RegisterResult;
import edu.unisabana.proyecto.domain.model.rq.PersonDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone el registro de votantes.
 *
 * <p>Endpoint: {@code POST /register} (consume y produce texto plano con el
 * nombre del {@link RegisterResult}).</p>
 */
@RestController
@RequestMapping("/register")
public class RegistryController {

    private final Registry registry;

    public RegistryController(Registry registry) {
        this.registry = registry;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String register(@RequestBody PersonDTO dto) {
        Gender gender = parseGender(dto.getGender());
        Person p = new Person(dto.getName(), dto.getId(), dto.getAge(), gender, dto.isAlive());
        RegisterResult result = registry.registerVoter(p);
        return result.name();
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
