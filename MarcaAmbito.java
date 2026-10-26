/*
 * INTEGRANTES:
 * 1. Cancelada de la O Gerardo Alexander
 * 2. Castellanos Hernández Alan
 * 3. Lara Jiménez Pablo César
 * 4. Medina Robledo Brandon Ernie
 */

/**
 * Fotografía del contexto semántico al abrir un bloque: profundidad de la pila
 * de ámbitos y contadores de ciclos/switch. Al cerrar el bloque se restaura, de
 * modo que un error sintáctico recuperado no deje ámbitos abiertos de más.
 */
public class MarcaAmbito {
    public final int ambitos;
    public final int ciclos;
    public final int switches;

    public MarcaAmbito(int ambitos, int ciclos, int switches) {
        this.ambitos = ambitos;
        this.ciclos = ciclos;
        this.switches = switches;
    }
}
