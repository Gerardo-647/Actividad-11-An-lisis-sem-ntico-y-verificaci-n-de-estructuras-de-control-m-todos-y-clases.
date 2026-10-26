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
 * Reglas semanticas de clases (C++): herencia, resolucion de miembros, visibilidad
 * (public / private / protected) y compatibilidad de asignacion entre objetos.
 * Solo contiene logica de decision; el reporte de errores lo hace Parser.cup.
 */
public final class ManejadorClases {
    private ManejadorClases() {}

    private static final int MAX_NIVELES = 50;   // proteccion contra jerarquias circulares

    public static boolean esClase(TablaSimbolos t, String nombre) {
        return nombre != null && t.buscarClase(nombre) != null;
    }

    /** true si 'sub' es 'base' o hereda (directa o indirectamente) de ella. */
    public static boolean esSubclase(TablaSimbolos t, String sub, String base) {
        String c = sub;
        for (int i = 0; c != null && i < MAX_NIVELES; i++) {
            if (c.equals(base)) return true;
            c = t.padreDe(c);
        }
        return false;
    }

    /** Atributo/constante buscado en la clase y, si no esta, en sus clases base (la mas cercana gana). */
    public static Simbolo buscarAtributo(TablaSimbolos t, String clase, String id) {
        String c = clase;
        for (int i = 0; c != null && i < MAX_NIVELES; i++) {
            Simbolo s = t.atributoEn(c, id);
            if (s != null) return s;
            c = t.padreDe(c);
        }
        return null;
    }

    /**
     * Metodos con ese nombre visibles desde la clase: los de la clase mas cercana de la cadena
     * de herencia que declare ese nombre (un metodo de la clase derivada oculta los de la base).
     */
    public static List<Simbolo> buscarMetodos(TablaSimbolos t, String clase, String id) {
        String c = clase;
        for (int i = 0; c != null && i < MAX_NIVELES; i++) {
            List<Simbolo> m = t.metodosEn(c, id);
            if (!m.isEmpty()) return m;
            c = t.padreDe(c);
        }
        return new ArrayList<Simbolo>();
    }

    /** true si 'contexto' (clase desde la que se accede, o null) puede usar el miembro segun su visibilidad. */
    public static boolean puedeAcceder(TablaSimbolos t, Simbolo miembro, String contexto) {
        String v = miembro.getVisibilidad();
        if (v.equals("-") || v.equals("public")) return true;
        String duena = miembro.getAmbito();
        if (contexto == null) return false;
        if (v.equals("private")) return contexto.equals(duena);
        return esSubclase(t, contexto, duena);          // protected
    }

    /** ¿Puede asignarse un valor de tipo 'origen' a un destino de tipo 'destino'? (primitivos y objetos) */
    public static boolean asignable(TablaSimbolos t, String destino, String origen) {
        if (ManejadorTipos.ERROR.equals(destino) || ManejadorTipos.ERROR.equals(origen)) return true;
        boolean destClase = esClase(t, destino), origClase = esClase(t, origen);
        if (ManejadorTipos.esPrimitivo(destino) && ManejadorTipos.esPrimitivo(origen)) return ManejadorTipos.asignable(destino, origen);
        if (destClase) return origClase && esSubclase(t, origen, destino);   // un objeto Derivada cabe en una referencia Base
        if (ManejadorTipos.esPrimitivo(destino)) return !origClase;          // objeto -> primitivo: incompatible
        return true;                                                         // destino desconocido (p. ej. N/A): no se verifica
    }

    /** Coste de convertir un argumento al tipo del parametro: 0 exacto, 1 conversion permitida, -1 incompatible. */
    public static int costoConversion(TablaSimbolos t, String param, String arg) {
        if (ManejadorTipos.ERROR.equals(param) || ManejadorTipos.ERROR.equals(arg)) return 0;
        if (param.equals(arg)) return 0;
        return asignable(t, param, arg) ? 1 : -1;
    }
}
