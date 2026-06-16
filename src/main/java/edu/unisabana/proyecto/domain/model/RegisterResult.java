package edu.unisabana.proyecto.domain.model;

/**
 * Resultado de un intento de registro de un votante.
 *
 * <ul>
 *   <li>{@code VALID}      - registrado correctamente.</li>
 *   <li>{@code DUPLICATED} - el documento ya estaba registrado.</li>
 *   <li>{@code INVALID}    - datos no validos (persona nula o documento &lt;= 0).</li>
 *   <li>{@code DEAD}       - la persona no esta viva.</li>
 *   <li>{@code UNDERAGE}   - la persona es menor de edad.</li>
 * </ul>
 */
public enum RegisterResult {
    VALID, DUPLICATED, INVALID, DEAD, UNDERAGE
}
