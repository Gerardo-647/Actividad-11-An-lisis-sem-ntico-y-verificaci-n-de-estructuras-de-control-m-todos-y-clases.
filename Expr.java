/*
 * INTEGRANTES:
 * 1. Cancelada de la O Gerardo Alexander
 * 2. Castellanos Hernández Alan
 * 3. Lara Jiménez Pablo César
 * 4. Medina Robledo Brandon Ernie
 */

/**
 * Resultado de evaluar una expresión durante el análisis semántico.
 * Los no terminales de expresión de Parser.cup devuelven un objeto Expr:
 * el texto sigue sirviendo para imprimir reglas y guardar el valor en la
 * tabla de símbolos; el tipo es el que se usa para verificar compatibilidad.
 */
public class Expr {
    public final String texto;   // representación textual de la expresión
    public final String tipo;    // int, float, double, char, bool, string, void, ERROR...
    public final int linea;      // posición del primer token de la expresión
    public final int columna;
    public String arbol = "";    // solo llamadas a método: salida de regla() para el árbol de sentencias
    public boolean constante = false; // literal o constante (const/final): válido como etiqueta de 'case'

    public Expr(String texto, String tipo, int linea, int columna) {
        this.texto = texto;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
    }

    /** Marca la expresión como constante y la devuelve (para encadenar). */
    public Expr comoConstante(boolean c) { this.constante = c; return this; }

    /** true si la expresión ya tiene un error reportado (evita errores en cascada). */
    public boolean esError() {
        return ManejadorTipos.ERROR.equals(tipo);
    }

    @Override public String toString() { return texto; }
}
