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
 * Reglas semanticas de metodos: resolucion de sobrecargas y correspondencia entre
 * los argumentos de una llamada y los parametros declarados (cantidad y tipos).
 * Solo contiene logica de decision; el reporte de errores lo hace Parser.cup.
 */
public final class ManejadorMetodos {
    private ManejadorMetodos() {}

    /** Resultado de resolver una llamada: el metodo elegido, o el error (con el argumento culpable si aplica). */
    public static final class Resultado {
        public Simbolo metodo = null;
        public String error = null;
        public Expr culpable = null;      // argumento incompatible (null: el error es de la llamada completa)
        public int argumentosVerificados = 0;
    }

    private static String plural(int n, String singular, String plural) {
        return n + " " + (n == 1 ? singular : plural);
    }

    /** Primer problema de una llamada contra UN candidato, o null si es aplicable. Rellena 'r' con el detalle. */
    private static String problema(TablaSimbolos t, String etiqueta, Simbolo cand, Argumentos args, Resultado r) {
        int n = cand.getTiposParam().size();
        if (n != args.size()) {
            r.culpable = null;
            return "El " + etiqueta + " requiere " + plural(n, "argumento", "argumentos") + ", pero se "
                + (args.size() == 1 ? "proporcionó 1" : "proporcionaron " + args.size());
        }
        for (int i = 0; i < n; i++) {
            Expr a = args.get(i);
            String esperado = cand.getTiposParam().get(i);
            if (ManejadorClases.costoConversion(t, esperado, a.tipo) < 0) {
                r.culpable = a;
                return "El argumento " + (i + 1) + " del " + etiqueta + " es de tipo '" + a.tipo
                    + "'; se esperaba un tipo compatible con '" + esperado + "'";
            }
        }
        return null;
    }

    private static int costoTotal(TablaSimbolos t, Simbolo cand, Argumentos args) {
        int total = 0;
        for (int i = 0; i < args.size(); i++) total += Math.max(0, ManejadorClases.costoConversion(t, cand.getTiposParam().get(i), args.get(i).tipo));
        return total;
    }

    /**
     * Elige la sobrecarga aplicable. 'etiqueta' describe el metodo en los mensajes
     * (por ejemplo "método 'sumar'" o "constructor de la clase 'Persona'").
     */
    public static Resultado resolver(TablaSimbolos t, String etiqueta, List<Simbolo> candidatos, Argumentos args) {
        Resultado r = new Resultado();
        r.argumentosVerificados = args.size();
        if (candidatos.size() == 1) {                      // un solo metodo con ese nombre: diagnostico especifico
            Simbolo c = candidatos.get(0);
            String p = problema(t, etiqueta, c, args, r);
            if (p == null) r.metodo = c; else r.error = p;
            return r;
        }
        List<Simbolo> aplicables = new ArrayList<Simbolo>();
        for (Simbolo c : candidatos) if (problema(t, etiqueta, c, args, new Resultado()) == null) aplicables.add(c);
        if (aplicables.isEmpty()) {
            List<String> firmas = new ArrayList<String>();
            for (Simbolo c : candidatos) firmas.add(c.firma());
            r.error = "Ninguna sobrecarga del " + etiqueta + " acepta los argumentos (" + args.tipos()
                + "); sobrecargas disponibles: " + String.join(", ", firmas);
            return r;
        }
        Simbolo mejor = null;
        int mejorCosto = Integer.MAX_VALUE;
        boolean empate = false;
        for (Simbolo c : aplicables) {
            int costo = costoTotal(t, c, args);
            if (costo < mejorCosto) { mejor = c; mejorCosto = costo; empate = false; }
            else if (costo == mejorCosto) empate = true;
        }
        if (empate) {
            List<String> firmas = new ArrayList<String>();
            for (Simbolo c : aplicables) if (costoTotal(t, c, args) == mejorCosto) firmas.add(c.firma());
            r.error = "La llamada al " + etiqueta + " con los argumentos (" + args.tipos() + ") es ambigua entre: " + String.join(", ", firmas);
            return r;
        }
        r.metodo = mejor;
        return r;
    }
}
