/*
 * INTEGRANTES:
 * 1. Cancelada de la O Gerardo Alexander
 * 2. Castellanos Hernández Alan
 * 3. Lara Jiménez Pablo César
 * 4. Medina Robledo Brandon Ernie
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una entrada de la tabla de simbolos: un identificador
 * (clase, metodo, atributo, variable o constante) reconocido durante
 * el analisis sintactico, junto con su clasificacion, tipo de dato,
 * valor, ambito y la posicion exacta donde fue declarado.
 */
public class Simbolo {

    private String id;
    private String tipoSimbolo; // CLASE, METODO, CONSTRUCTOR, ATRIBUTO, VARIABLE, PARAMETRO, CONSTANTE
    private String tipoDato;    // int, float, double, char, bool, string, void, N/A
    private String valor;       // valor inicial en texto, o "null" / "sin_inicializar"
    private String ambito;      // GLOBAL, nombre de clase o nombre de metodo
    private int linea;
    private int columna;
    private String token;       // token/lexema que origino el registro (ej. "ID")

    // --- Informacion semantica de clases, metodos y parametros (practica de estructuras de control, metodos y clases) ---
    private String visibilidad = "-";                 // public / private / protected (miembros de clase); "-" si no aplica
    private String padre = null;                      // CLASE: nombre de la clase base (herencia), o null
    private int posicion = 0;                         // PARAMETRO: posicion (1..n) dentro del metodo
    private List<String> tiposParam = new ArrayList<String>();    // METODO/CONSTRUCTOR: tipos de los parametros
    private List<String> nombresParam = new ArrayList<String>();  // METODO/CONSTRUCTOR: nombres de los parametros

    public Simbolo(String id, String tipoSimbolo, String tipoDato, String valor,
                   String ambito, int linea, int columna, String token) {
        this.id = id;
        this.tipoSimbolo = tipoSimbolo;
        this.tipoDato = tipoDato;
        this.valor = valor;
        this.ambito = ambito;
        this.linea = linea;
        this.columna = columna;
        this.token = token;
    }

    public String getId() { return id; }
    public String getTipoSimbolo() { return tipoSimbolo; }
    public String getTipoDato() { return tipoDato; }
    public String getValor() { return valor; }
    public String getAmbito() { return ambito; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    public String getToken() { return token; }

    public String getVisibilidad() { return visibilidad; }
    public void setVisibilidad(String v) { this.visibilidad = v; }
    public String getPadre() { return padre; }
    public void setPadre(String p) { this.padre = p; }
    public int getPosicion() { return posicion; }
    public void setPosicion(int p) { this.posicion = p; }
    public List<String> getTiposParam() { return tiposParam; }
    public List<String> getNombresParam() { return nombresParam; }

    public void agregarParametro(String tipo, String nombre) {
        tiposParam.add(tipo);
        nombresParam.add(nombre);
    }

    public boolean esMetodoOConstructor() {
        return tipoSimbolo.equals("METODO") || tipoSimbolo.equals("CONSTRUCTOR");
    }

    /** Firma "nombre(int, float)" usada en mensajes de error y en la busqueda de sobrecargas. */
    public String firma() {
        return id + "(" + String.join(", ", tiposParam) + ")";
    }

    /** Parametros con nombre "(int a, int b)" para la tabla de simbolos. */
    public String parametrosConNombre() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < tiposParam.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(tiposParam.get(i)).append(' ').append(nombresParam.get(i));
        }
        return sb.append(')').toString();
    }

    /** Dos metodos del mismo nombre tienen la misma firma si coinciden los tipos de sus parametros. */
    public boolean mismaFirma(Simbolo otro) {
        return tiposParam.equals(otro.tiposParam);
    }
}
