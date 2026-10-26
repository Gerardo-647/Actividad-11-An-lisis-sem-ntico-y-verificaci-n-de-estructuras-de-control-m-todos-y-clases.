/*
 * INTEGRANTES:
 * 1. Cancelada de la O Gerardo Alexander
 * 2. Castellanos Hernández Alan
 * 3. Lara Jiménez Pablo César
 * 4. Medina Robledo Brandon Ernie
 */

/**
 * Reglas de compatibilidad de tipos de C++ (lenguaje estáticamente tipado).
 * Solo contiene lógica de decisión; el reporte de errores lo hace Parser.cup.
 *
 * Tipos primitivos: char < int < float < double (promoción numérica),
 * más bool y string. Cualquier otro nombre es un tipo de clase (no verificable).
 * ERROR es el tipo de una subexpresión que ya fue reportada.
 */
public final class ManejadorTipos {
    public static final String ERROR = "ERROR";

    private ManejadorTipos() {}

    public static boolean esNumerico(String t) {
        return "int".equals(t) || "float".equals(t) || "double".equals(t) || "char".equals(t);
    }

    /** Tipos enteros: los únicos válidos para % y para switch/case. */
    public static boolean esEntero(String t) {
        return "int".equals(t) || "char".equals(t);
    }

    public static boolean esPrimitivo(String t) {
        return esNumerico(t) || "bool".equals(t) || "string".equals(t);
    }

    private static int rango(String t) {
        switch (t) {
            case "char": return 1;
            case "int": return 2;
            case "float": return 3;
            case "double": return 4;
            default: return 0;
        }
    }

    /** Promoción: el tipo de mayor rango, y nunca menor que int (char + char = int). */
    public static String promover(String a, String b) {
        String mayor = rango(a) >= rango(b) ? a : b;
        return rango(mayor) < rango("int") ? "int" : mayor;
    }

    /** ¿Puede guardarse un valor de tipo 'origen' en un destino de tipo 'destino'? */
    public static boolean asignable(String destino, String origen) {
        if (!esPrimitivo(destino)) return true;   // tipo de clase: no se verifica
        if (ERROR.equals(origen)) return true;    // ya reportado
        if (destino.equals(origen)) return true;
        return esNumerico(destino) && esNumerico(origen) && rango(origen) <= rango(destino); // solo ensanchamiento
    }

    /** Tipo resultante de a op b, o null si la combinación es inválida. */
    public static String tipoBinario(String op, String a, String b) {
        switch (op) {
            case "+":
                if ("string".equals(a) && "string".equals(b)) return "string"; // concatenación
                // sigue al caso numérico
            case "-": case "*": case "/":
                return esNumerico(a) && esNumerico(b) ? promover(a, b) : null;
            case "%":
                return esEntero(a) && esEntero(b) ? "int" : null;
            case "<": case ">": case "<=": case ">=":
                return esNumerico(a) && esNumerico(b) ? "bool" : null;
            case "==": case "!=":
                if (esNumerico(a) && esNumerico(b)) return "bool";
                return esPrimitivo(a) && a.equals(b) ? "bool" : null;
            case "&&": case "||":
                return "bool".equals(a) && "bool".equals(b) ? "bool" : null;
            default:
                return null;
        }
    }

    /** Causa exacta del error para una combinación inválida. */
    public static String causaBinario(String op, String a, String b) {
        String par = "'" + a + "' y '" + b + "'";
        switch (op) {
            case "%":
                return "El operador '%' solo es aplicable a operandos enteros (int/char), se encontró " + par + ".";
            case "+": case "-": case "*": case "/":
                if (op.equals("+") && ("string".equals(a) || "string".equals(b)))
                    return "El operador '+' solo concatena operandos de tipo 'string', se encontró " + par + ".";
                return "El operador aritmético '" + op + "' no es aplicable a los operandos de tipo " + par + ".";
            case "<": case ">": case "<=": case ">=":
                return "El operador relacional '" + op + "' requiere operandos numéricos, se encontró " + par
                     + "; con string y bool solo se permiten == y !=.";
            case "==": case "!=":
                return "Incompatibilidad de tipos: no se pueden comparar con '" + op + "' operandos de tipo " + par + ".";
            default:
                return "El operador lógico '" + op + "' requiere operandos de tipo 'bool', se encontró " + par + ".";
        }
    }

    /** Tipo de un operador unario (!, -, +), o null si es inválido. */
    public static String tipoUnario(String op, String t) {
        if (op.equals("!")) return "bool".equals(t) ? "bool" : null;
        return esNumerico(t) ? promover(t, "int") : null;
    }

    public static String causaUnario(String op, String t) {
        if (op.equals("!"))
            return "El operador lógico '!' requiere un operando de tipo 'bool', se encontró '" + t + "'.";
        return "El operador unario '" + op + "' requiere un operando numérico, se encontró '" + t + "'.";
    }
}
