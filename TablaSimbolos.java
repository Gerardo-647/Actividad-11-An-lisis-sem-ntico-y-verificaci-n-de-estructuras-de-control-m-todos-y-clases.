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
 * Tabla de simbolos del compilador. Usa una lista dinamica (ArrayList) para los
 * registros y un ManejadorAmbitos (pila) para el control de ambitos, de forma que
 * una variable local no choque con la de otro metodo/bloque ni con los atributos
 * de la clase. Soporta clases (con herencia), atributos, metodos sobrecargados,
 * constructores, parametros, variables locales y constantes.
 */
public class TablaSimbolos {

    private List<Simbolo> simbolos;
    private ManejadorAmbitos ambitos;

    public TablaSimbolos() {
        simbolos = new ArrayList<Simbolo>();
        ambitos = new ManejadorAmbitos();
    }

    /* ---------------- ambitos (delegan en ManejadorAmbitos) ---------------- */

    public void entrarAmbito(String nombre, String tipo) { ambitos.entrar(nombre, tipo); }
    public String entrarAmbitoUnico(String base, String tipo) { return ambitos.entrarUnico(base, tipo); }
    public String entrarBloque(String etiqueta) { return ambitos.entrarBloque(etiqueta); }
    public void salirAmbito() { ambitos.salir(); }
    public int profundidad() { return ambitos.profundidad(); }
    public void restaurar(int profundidad) { ambitos.restaurar(profundidad); }
    public String ambitoActual() { return ambitos.actual(); }
    public List<String> ambitosActivos() { return ambitos.nombresActivos(); }

    /** true si se esta dentro de un metodo o de un bloque anidado en un metodo. */
    public boolean enMetodo() { return ambitos.enMetodo(); }

    /** true si el ambito actual es el cuerpo de una clase (declaracion de miembros). */
    public boolean enClase() { return ambitos.enClase(); }

    /** Clase que contiene el codigo actual, o null en el ambito global. */
    public String claseActual() { return ambitos.claseActual(); }

    /* ---------------- registro y busqueda ---------------- */

    /** Verifica si ya existe un identificador en el ambito indicado (colision). */
    public boolean existeEnAmbito(String id, String ambito) {
        for (Simbolo s : simbolos) {
            if (s.getId().equals(id) && s.getAmbito().equals(ambito)) {
                return true;
            }
        }
        return false;
    }

    /** true si existe en el ambito un simbolo del mismo nombre que NO sea metodo (los metodos se distinguen por firma). */
    public boolean existeNoMetodoEnAmbito(String id, String ambito) {
        for (Simbolo s : simbolos) {
            if (s.getId().equals(id) && s.getAmbito().equals(ambito) && !s.esMetodoOConstructor()) return true;
        }
        return false;
    }

    public void insertar(Simbolo s) {
        simbolos.add(s);
    }

    public void eliminar(Simbolo s) {
        simbolos.remove(s);
    }

    /**
     * Busca un identificador recorriendo la pila de ambitos desde el mas interno
     * (local) hasta el global. Al pasar por el ambito de una clase tambien revisa
     * sus clases base (herencia), antes de continuar con el ambito global.
     */
    public Simbolo buscar(String id) {
        for (ManejadorAmbitos.Ambito amb : ambitos.activos()) {
            Simbolo s = buscarEn(id, amb.nombre);
            if (s != null) return s;
            if (amb.tipo.equals("CLASE")) {
                String base = padreDe(amb.nombre);
                int guarda = 0;
                while (base != null && guarda++ < 50) {
                    Simbolo h = buscarEn(id, base);
                    if (h != null) return h;
                    base = padreDe(base);
                }
            }
        }
        return null;
    }

    private Simbolo buscarEn(String id, String ambito) {
        for (Simbolo s : simbolos) {
            if (s.getId().equals(id) && s.getAmbito().equals(ambito) && !s.esMetodoOConstructor()
                    && !s.getTipoSimbolo().equals("CLASE")) {
                return s;
            }
        }
        // las clases y los metodos no se buscan como variables: usarVariable los distingue
        for (Simbolo s : simbolos) {
            if (s.getId().equals(id) && s.getAmbito().equals(ambito)) return s;
        }
        return null;
    }

    /* ---------------- clases, miembros y metodos ---------------- */

    /** Simbolo CLASE con ese nombre (las clases se declaran en el ambito global), o null. */
    public Simbolo buscarClase(String nombre) {
        for (Simbolo s : simbolos) {
            if (s.getTipoSimbolo().equals("CLASE") && s.getId().equals(nombre)) return s;
        }
        return null;
    }

    public String padreDe(String clase) {
        Simbolo c = buscarClase(clase);
        return c == null ? null : c.getPadre();
    }

    /** Atributo o constante de clase declarado directamente en esa clase (sin herencia), o null. */
    public Simbolo atributoEn(String clase, String id) {
        for (Simbolo s : simbolos) {
            if (s.getId().equals(id) && s.getAmbito().equals(clase)
                    && (s.getTipoSimbolo().equals("ATRIBUTO") || s.getTipoSimbolo().equals("CONSTANTE"))) return s;
        }
        return null;
    }

    /** Metodos con ese nombre declarados en el ambito dado (clase o GLOBAL): las sobrecargas. */
    public List<Simbolo> metodosEn(String ambito, String id) {
        List<Simbolo> r = new ArrayList<Simbolo>();
        for (Simbolo s : simbolos) {
            if (s.getTipoSimbolo().equals("METODO") && s.getId().equals(id) && s.getAmbito().equals(ambito)) r.add(s);
        }
        return r;
    }

    /** Constructores declarados en una clase. */
    public List<Simbolo> constructoresDe(String clase) {
        List<Simbolo> r = new ArrayList<Simbolo>();
        for (Simbolo s : simbolos) {
            if (s.getTipoSimbolo().equals("CONSTRUCTOR") && s.getAmbito().equals(clase)) r.add(s);
        }
        return r;
    }

    /** Metodos/constructores del mismo nombre y categoria en un ambito (para detectar firmas duplicadas). */
    public List<Simbolo> mismosNombre(String id, String ambito, String categoria) {
        List<Simbolo> r = new ArrayList<Simbolo>();
        for (Simbolo s : simbolos) {
            if (s.getTipoSimbolo().equals(categoria) && s.getId().equals(id) && s.getAmbito().equals(ambito)) r.add(s);
        }
        return r;
    }

    /** Declaraciones de variables, parametros, constantes y atributos con ese nombre en CUALQUIER ambito. */
    public List<Simbolo> declaraciones(String id) {
        List<Simbolo> r = new ArrayList<Simbolo>();
        for (Simbolo s : simbolos) {
            String t = s.getTipoSimbolo();
            if (s.getId().equals(id) && (t.equals("VARIABLE") || t.equals("PARAMETRO")
                    || t.equals("CONSTANTE") || t.equals("ATRIBUTO"))) r.add(s);
        }
        return r;
    }

    /** true si existe algun metodo con ese nombre, en cualquier ambito. */
    public boolean existeMetodo(String id) {
        for (Simbolo s : simbolos) {
            if (s.getTipoSimbolo().equals("METODO") && s.getId().equals(id)) return true;
        }
        return false;
    }

    /** Devuelve el simbolo METODO con ese nombre (para conocer su tipo de retorno), o null. */
    public Simbolo buscarMetodo(String id) {
        for (Simbolo s : simbolos) {
            if (s.getTipoSimbolo().equals("METODO") && s.getId().equals(id)) return s;
        }
        return null;
    }

    public int total() {
        return simbolos.size();
    }

    /* ---------------- impresion ---------------- */

    /** Miembros declarados en una clase: atributos, constantes, constructores y metodos. */
    private String miembrosDe(String clase) {
        List<String> m = new ArrayList<String>();
        for (Simbolo s : simbolos) {
            String t = s.getTipoSimbolo();
            if (s.getAmbito().equals(clase) && (t.equals("ATRIBUTO") || t.equals("CONSTANTE")
                    || t.equals("METODO") || t.equals("CONSTRUCTOR"))) {
                m.add(s.esMetodoOConstructor() ? s.firma() : s.getId());
            }
        }
        return m.isEmpty() ? "sin miembros" : String.join(", ", m);
    }

    private String detalleDe(Simbolo s) {
        switch (s.getTipoSimbolo()) {
            case "CLASE":
                return (s.getPadre() != null ? "hereda de " + s.getPadre() + "; " : "") + "miembros: " + miembrosDe(s.getId());
            case "METODO":
            case "CONSTRUCTOR":
                return s.parametrosConNombre() + (s.getTipoSimbolo().equals("METODO") ? " -> " + s.getTipoDato() : "");
            case "PARAMETRO":
                return "posicion " + s.getPosicion();
            case "ATRIBUTO":
            case "CONSTANTE":
                return s.getVisibilidad().equals("-") ? "" : "clase propietaria: " + s.getAmbito();
            default:
                return "";
        }
    }

    /** Imprime la tabla de simbolos final (columnas de la practica anterior + visibilidad y detalle). */
    public void imprimirTabla() {
        int wId = 18, wValor = 16, wAmb = 17;
        for (Simbolo s : simbolos) {
            wId = Math.max(wId, s.getId().length());
            wValor = Math.max(wValor, Math.min(s.getValor().length(), 30));
            wAmb = Math.max(wAmb, s.getAmbito().length());
        }
        String fmt = "%-" + wId + "s %-13s %-11s %-" + wValor + "s %-" + wAmb + "s %-10s %-7s %-9s %-16s %s%n";
        int ancho = wId + 13 + 11 + wValor + wAmb + 10 + 7 + 9 + 16 + 10 + 45;
        String raya = repetir('=', ancho);
        String guion = repetir('-', ancho);
        String titulo = "TABLA DE SÍMBOLOS";
        int relleno = (ancho - titulo.length()) / 2;

        System.out.println(raya);
        System.out.println(repetir(' ', Math.max(relleno, 0)) + titulo);
        System.out.println(raya);
        System.out.printf(fmt, "ID", "Categoría", "Tipo Dato", "Valor", "Ámbito", "Visib.", "Línea", "Columna", "Token Originador", "Detalle");
        System.out.println(guion);

        for (Simbolo s : simbolos) {
            System.out.printf(fmt.replace("%-7s %-9s", "%-7d %-9d"),
                    s.getId(), s.getTipoSimbolo(), s.getTipoDato(), s.getValor(),
                    s.getAmbito(), s.getVisibilidad(), s.getLinea(), s.getColumna(), s.getToken(), detalleDe(s));
        }
        System.out.println(guion);
        System.out.println("Total de símbolos registrados: " + simbolos.size());
    }

    private static String repetir(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }
}
