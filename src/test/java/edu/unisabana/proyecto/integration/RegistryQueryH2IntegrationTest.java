package edu.unisabana.proyecto.integration;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;
import edu.unisabana.proyecto.application.usecase.Registry;
import edu.unisabana.proyecto.domain.model.Gender;
import edu.unisabana.proyecto.domain.model.Person;
import edu.unisabana.proyecto.domain.model.Stats;
import edu.unisabana.proyecto.infrastructure.persistence.RegistryRecord;
import edu.unisabana.proyecto.infrastructure.persistence.RegistryRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Pruebas de <b>integracion</b> (H2 en memoria) de las nuevas operaciones de
 * consulta y gestion: listar, consultar, eliminar y estadisticas, verificando la
 * colaboracion real entre el caso de uso {@link Registry} y el adaptador JDBC.
 */
public class RegistryQueryH2IntegrationTest {

    private static final String JDBC_URL = "jdbc:h2:mem:itquerydb;DB_CLOSE_DELAY=-1";

    private RegistryRepositoryPort repo;
    private Registry registry;

    @Before
    public void setUp() throws Exception {
        repo = new RegistryRepository(JDBC_URL);
        repo.initSchema();
        repo.deleteAll();
        registry = new Registry(repo);
    }

    @Test
    public void shouldListAllPersistedVoters() {
        // Arrange
        registry.registerVoter(new Person("Ana", 1, 30, Gender.FEMALE, true));
        registry.registerVoter(new Person("Beto", 2, 40, Gender.MALE, true));

        // Act
        List<RegistryRecord> all = registry.listVoters();

        // Assert
        assertEquals(2, all.size());
    }

    @Test
    public void shouldFindPersistedVoter() {
        // Arrange
        registry.registerVoter(new Person("Ana", 100, 30, Gender.FEMALE, true));

        // Act
        Optional<RegistryRecord> found = registry.findVoter(100);

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Ana", found.get().getName());
    }

    @Test
    public void shouldDeleteExistingVoter() {
        // Arrange
        registry.registerVoter(new Person("Ana", 100, 30, Gender.FEMALE, true));

        // Act
        boolean removed = registry.removeVoter(100);

        // Assert
        assertTrue(removed);
        assertFalse(registry.findVoter(100).isPresent());
    }

    @Test
    public void shouldReturnFalseWhenDeletingNonExistentVoter() {
        // Act / Assert
        assertFalse(registry.removeVoter(999));
    }

    @Test
    public void shouldComputeStatsFromPersistedVoters() {
        // Arrange
        registry.registerVoter(new Person("Ana", 1, 20, Gender.FEMALE, true));
        registry.registerVoter(new Person("Beto", 2, 30, Gender.MALE, true));
        registry.registerVoter(new Person("Caro", 3, 40, Gender.FEMALE, true));

        // Act
        Stats stats = registry.getStats();

        // Assert
        assertEquals(3, stats.total());
        assertEquals(30.0, stats.averageAge(), 0.001);
        assertEquals(20, stats.minAge());
        assertEquals(40, stats.maxAge());
    }
}
