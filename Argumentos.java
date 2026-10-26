/*
 * INTEGRANTES:
 * 1. Cancelada de la O Gerardo Alexander
 * 2. Castellanos Hernández Alan
 * 3. Lara Jiménez Pablo César
 * 4. Medina Robledo Brandon Ernie
 */

import java.util.ArrayList;

/** Lista de argumentos de una llamada, con su tipo y posición (cada uno es un Expr). */
public class Argumentos extends ArrayList<Expr> {
    private static final long serialVersionUID = 1L;

    public Argumentos() { super(); }

    public Argumentos con(Expr e) { add(e); return this; }

    /** Texto "a, b, c" de los argumentos, para imprimir la llamada. */
    public String texto() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(get(i).texto);
        }
        return sb.toString();
    }

    /** Texto de los tipos "int, string". */
    public String tipos() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(get(i).tipo);
        }
        return sb.toString();
    }
}
