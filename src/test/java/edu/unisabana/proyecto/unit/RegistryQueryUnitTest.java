package edu.unisabana.proyecto.unit;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;
import edu.unisabana.proyecto.application.usecase.Registry;
import edu.unisabana.proyecto.domain.model.Stats;
import edu.unisabana.proyecto.infrastructure.persistence.RegistryRecord;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas <b>unitarias</b> (TDD) de las nuevas operaciones de consulta y gestion del
 * caso de uso {@link Registry}: consultar, listar, eliminar y estadisticas.
 *
 * <p>Se aisla la logica con un mock del puerto {@link RegistryRepositoryPort}.
 * Patron <b>AAA</b> (Arrange - Act - Assert).</p>
 */
public class RegistryQueryUnitTest {

    private RegistryRepositoryPort repo;
    private Registry registry;

    @Before
    public void setUp() {
        repo = mock(RegistryRepositoryPort.class);
        registry = new Registry(repo);
    }

    @Test
    public void shouldFindVoterWhenExists() throws Exception {
        // Arrange
        RegistryRecord rec = new RegistryRecord(100, "Ana", 30, true);
        when(repo.findById(100)).thenReturn(Optional.of(rec));

        // Act
        Optional<RegistryRecord> found = registry.findVoter(100);

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Ana", found.get().getName());
    }

    @Test
    public void shouldReturnEmptyWhenVoterDoesNotExist() throws Exception {
        // Arrange
        when(repo.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<RegistryRecord> found = registry.findVoter(999);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    public void shouldListAllVoters() throws Exception {
        // Arrange
        List<RegistryRecord> all = Arrays.asList(
                new RegistryRecord(1, "Ana", 30, true),
                new RegistryRecord(2, "Beto", 40, true));
        when(repo.findAll()).thenReturn(all);

        // Act
        List<RegistryRecord> result = registry.listVoters();

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    public void shouldReturnTrueWhenVoterRemoved() throws Exception {
        // Arrange
        when(repo.deleteById(5)).thenReturn(true);

        // Act / Assert
        assertTrue(registry.removeVoter(5));
    }

    @Test
    public void shouldReturnFalseWhenRemovingNonExistentVoter() throws Exception {
        // Arrange
        when(repo.deleteById(5)).thenReturn(false);

        // Act / Assert
        assertFalse(registry.removeVoter(5));
    }

    @Test
    public void shouldComputeStatsFromRegisteredVoters() throws Exception {
        // Arrange
        when(repo.findAll()).thenReturn(Arrays.asList(
                new RegistryRecord(1, "Ana", 20, true),
                new RegistryRecord(2, "Beto", 30, true),
                new RegistryRecord(3, "Caro", 40, true)));

        // Act
        Stats stats = registry.getStats();

        // Assert
        assertEquals(3, stats.total());
        assertEquals(30.0, stats.averageAge(), 0.001);
        assertEquals(20, stats.minAge());
        assertEquals(40, stats.maxAge());
    }

    @Test
    public void shouldReturnZeroedStatsWhenNoVoters() throws Exception {
        // Arrange
        when(repo.findAll()).thenReturn(Collections.emptyList());

        // Act
        Stats stats = registry.getStats();

        // Assert
        assertEquals(0, stats.total());
        assertEquals(0.0, stats.averageAge(), 0.001);
        assertEquals(0, stats.minAge());
        assertEquals(0, stats.maxAge());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldWrapRepositoryFailureOnFindVoter() throws Exception {
        // Arrange
        when(repo.findById(7)).thenThrow(new RuntimeException("fallo de BD"));

        // Act -> debe traducirse a IllegalStateException controlado
        registry.findVoter(7);
    }
}
