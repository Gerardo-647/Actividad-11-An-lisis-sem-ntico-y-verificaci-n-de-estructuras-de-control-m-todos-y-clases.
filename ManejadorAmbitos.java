/*
 * INTEGRANTES:
 * 1. Cancelada de la O Gerardo Alexander
 * 2. Castellanos Hernández Alan
 * 3. Lara Jiménez Pablo César
 * 4. Medina Robledo Brandon Ernie
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejador de ambitos: pila de ambitos activos (GLOBAL, CLASE, METODO y BLOQUE).
 * Cada ambito tiene un nombre unico, de modo que dos bloques, dos metodos con el
 * mismo nombre en clases distintas o dos sobrecargas nunca compartan declaraciones.
 * Los nombres son jerarquicos: Calculadora.ejecutar.bloque1.for1
 */
public class ManejadorAmbitos {

    /** Un nivel de la pila de ambitos. */
    public static class Ambito {
        public final String nombre;
        public final String tipo;                       // GLOBAL, CLASE, METODO o BLOQUE
        private final Map<String, Integer> hijos = new HashMap<String, Integer>();
        Ambito(String nombre, String tipo) { this.nombre = nombre; this.tipo = tipo; }
        int siguienteHijo(String etiqueta) {
            int n = hijos.containsKey(etiqueta) ? hijos.get(etiqueta) + 1 : 1;
            hijos.put(etiqueta, n);
            return n;
        }
    }

    private final Deque<Ambito> pila = new ArrayDeque<Ambito>();
    private final Map<String, Integer> usados = new HashMap<String, Integer>();

    public ManejadorAmbitos() {
        pila.push(new Ambito("GLOBAL", "GLOBAL"));
    }

    /** Entra a un ambito con el nombre exacto indicado (clases). */
    public void entrar(String nombre, String tipo) {
        pila.push(new Ambito(nombre, tipo));
    }

    /** Entra a un ambito cuyo nombre se vuelve unico si ya se uso (p. ej. sobrecargas: sumar, sumar#2). */
    public String entrarUnico(String base, String tipo) {
        int n = usados.containsKey(base) ? usados.get(base) + 1 : 1;
        usados.put(base, n);
        String nombre = n == 1 ? base : base + "#" + n;
        pila.push(new Ambito(nombre, tipo));
        return nombre;
    }

    /** Entra a un bloque anidado (bloque, for, switch) dentro del ambito actual. */
    public String entrarBloque(String etiqueta) {
        Ambito padre = pila.peek();
        String nombre = padre.nombre + "." + etiqueta + padre.siguienteHijo(etiqueta);
        pila.push(new Ambito(nombre, "BLOQUE"));
        return nombre;
    }

    public void salir() {
        if (pila.size() > 1) pila.pop();
    }

    public int profundidad() { return pila.size(); }

    /** Cierra ambitos hasta volver a la profundidad indicada (recuperacion tras errores sintacticos). */
    public void restaurar(int profundidad) {
        while (pila.size() > profundidad && pila.size() > 1) pila.pop();
    }

    public String actual() { return pila.peek().nombre; }
    public String tipoActual() { return pila.peek().tipo; }

    /** Ambitos activos del mas interno al global. */
    public Iterable<Ambito> activos() { return pila; }

    public List<String> nombresActivos() {
        List<String> r = new ArrayList<String>();
        for (Ambito a : pila) r.add(a.nombre);
        return r;
    }

    /** true si hay un METODO en la pila (incluye bloques anidados dentro de un metodo). */
    public boolean enMetodo() {
        for (Ambito a : pila) if (a.tipo.equals("METODO")) return true;
        return false;
    }

    /** true si el ambito mas interno es una clase (declaracion de miembros). */
    public boolean enClase() { return pila.peek().tipo.equals("CLASE"); }

    /** Nombre de la clase mas interna de la pila, o null si no se esta dentro de una clase. */
    public String claseActual() {
        for (Ambito a : pila) if (a.tipo.equals("CLASE")) return a.nombre;
        return null;
    }
}
