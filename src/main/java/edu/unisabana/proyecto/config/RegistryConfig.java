package edu.unisabana.proyecto.config;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;
import edu.unisabana.proyecto.application.usecase.Registry;
import edu.unisabana.proyecto.infrastructure.persistence.RegistryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cableado de la aplicacion (inyeccion por constructor).
 *
 * <p>Se expone una base H2 en memoria como adaptador de persistencia. La URL usa
 * {@code DB_CLOSE_DELAY=-1} para mantener viva la base mientras exista la JVM.</p>
 */
@Configuration
public class RegistryConfig {

    private static final String H2_URL = "jdbc:h2:mem:regdb;DB_CLOSE_DELAY=-1";

    @Bean
    public RegistryRepositoryPort registryRepositoryPort() throws Exception {
        RegistryRepository repo = new RegistryRepository(H2_URL);
        repo.initSchema();
        return repo;
    }

    @Bean
    public Registry registry(RegistryRepositoryPort port) {
        return new Registry(port);
    }
}
