/*
 * INTEGRANTES:
 * 1. Cancelada de la O Gerardo Alexander
 * 2. Castellanos Hernández Alan
 * 3. Lara Jiménez Pablo César
 * 4. Medina Robledo Brandon Ernie
 */

/**
 * Resultado de analizar una sentencia: además del árbol textual que ya se
 * construía, guarda información de flujo de control para verificar que un
 * método no vacío devuelva un valor en todos sus caminos de ejecución.
 */
public class Nodo {
    public final String texto;      // árbol textual de la sentencia
    public final boolean retorna;   // true si TODOS los caminos de la sentencia terminan en un return
    public final boolean hayBreak;  // true si contiene un break que puede sacar del ciclo/switch actual
    public final boolean defecto;   // solo para casos de switch: contiene 'default'
    public int lineaCierre = 0;     // posición de la '}' que cierra un bloque (para el error de retorno faltante)
    public int columnaCierre = 0;

    public Nodo(String texto) { this(texto, false, false, false); }
    public Nodo(String texto, boolean retorna, boolean hayBreak) { this(texto, retorna, hayBreak, false); }
    public Nodo(String texto, boolean retorna, boolean hayBreak, boolean defecto) {
        this.texto = texto;
        this.retorna = retorna;
        this.hayBreak = hayBreak;
        this.defecto = defecto;
    }

    public static Nodo vacio() { return new Nodo(""); }

    @Override public String toString() { return texto; }
}
