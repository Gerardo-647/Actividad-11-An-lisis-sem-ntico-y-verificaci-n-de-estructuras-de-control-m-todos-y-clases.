import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/** Analiza léxico, sintaxis y semántica (tabla de símbolos, tipos, estructuras de control, métodos, clases y ámbitos); no ejecuta el programa C++. */
public class Main {
    private static void imprimirEncabezado() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TRADUCTORES DE LENGUAJE - PRÁCTICA: ANÁLISIS SEMÁNTICO - ESTRUCTURAS DE CONTROL, MÉTODOS Y CLASES");
        System.out.println("INTEGRANTES:");
        System.out.println("1. Cancelada de la O Gerardo Alexander");
        System.out.println("2. Castellanos Hernández Alan");
        System.out.println("3. Lara Jiménez Pablo César");
        System.out.println("4. Medina Robledo Brandon Ernie");
        System.out.println("--------------------------------------------------------------------------------");
    }

    /** Reporte del resultado del análisis semántico de tipos. */
    private static void imprimirReporteSemantico(Parser p, Lexer lexer) {
        int otros = p.numErroresSemanticos - p.numErroresTipos - p.numErroresConstantes - p.numErroresControl - p.numErroresMetodos
            - p.numErroresClases - p.numErroresAmbitos - p.numErroresDuplicados;
        System.out.println("================================================================================");
        System.out.println("REPORTE DEL ANÁLISIS SEMÁNTICO DE TIPOS (lenguaje: C++, tipado estático)");
        System.out.println("================================================================================");
        System.out.println("Verificaciones realizadas:");
        System.out.println("  Declaraciones y asignaciones (variables, atributos, constantes): " + p.verifAsignaciones);
        System.out.println("  Operaciones aritméticas (+ - * / %):                             " + p.verifAritmeticas);
        System.out.println("  Operaciones relacionales (> < >= <= == !=):                      " + p.verifRelacionales);
        System.out.println("  Operaciones lógicas (&& || !):                                   " + p.verifLogicas);
        System.out.println("  Condiciones de control (if, while, do-while, for):               " + p.verifCondiciones);
        System.out.println("  Retornos de método:                                              " + p.verifRetornos);
        System.out.println("Errores detectados:");
        System.out.println("  Incompatibilidad de tipos:                  " + p.numErroresTipos);
        System.out.println("  Modificación de constantes (inmutabilidad): " + p.numErroresConstantes);
        System.out.println("  Otros semánticos (no declarado, duplicado, convención, inicialización): " + otros);
        boolean sinErroresSem = p.numErroresSemanticos == 0;
        System.out.println("Resultado semántico: " + (sinErroresSem ? "CORRECTO (sin errores semánticos)"
            : "CON ERRORES (" + p.numErroresSemanticos + " errores semánticos)"));
        System.out.println("================================================================================");
    }

    /** Reporte del análisis semántico de estructuras de control, métodos, clases y ámbitos. */
    private static void imprimirReporteEstructuras(Parser p) {
        int clasificados = p.numErroresTipos + p.numErroresConstantes + p.numErroresControl + p.numErroresMetodos
            + p.numErroresClases + p.numErroresAmbitos + p.numErroresDuplicados;
        int otros = p.numErroresSemanticos - clasificados;
        System.out.println("================================================================================");
        System.out.println("REPORTE DEL ANÁLISIS SEMÁNTICO: ESTRUCTURAS DE CONTROL, MÉTODOS, CLASES Y ÁMBITOS");
        System.out.println("================================================================================");
        System.out.println("Verificaciones realizadas:");
        System.out.println("  Condiciones de control (if, while, do-while, for):         " + p.verifCondiciones);
        System.out.println("  Instrucciones break / continue / return (contexto):        " + p.verifBreakContinue);
        System.out.println("  Verificaciones de switch (expresión, case, default):       " + p.verifSwitch);
        System.out.println("  Declaraciones de métodos y constructores:                  " + p.verifMetodosDecl);
        System.out.println("  Llamadas a métodos y constructores:                        " + p.verifLlamadas);
        System.out.println("  Argumentos comparados contra parámetros:                   " + p.verifArgumentos);
        System.out.println("  Retornos de método (tipo) y métodos con retorno completo:  " + p.verifRetornos + " / " + p.verifRetornosCompletos);
        System.out.println("  Clases declaradas o referenciadas:                         " + p.verifClases);
        System.out.println("  Accesos a miembros de objetos (atributos y métodos):       " + p.verifMiembros);
        System.out.println("  Resoluciones de identificadores (ámbitos):                 " + p.verifIdentificadores);
        System.out.println("  Ámbitos de bloque abiertos (bloque, for, switch):          " + p.verifAmbitos);
        System.out.println("Errores detectados por categoría:");
        System.out.println("  Estructuras de control (break/continue/return/switch):     " + p.numErroresControl);
        System.out.println("  Métodos (llamadas, argumentos, firmas, retorno completo):  " + p.numErroresMetodos);
        System.out.println("  Clases y miembros (herencia, existencia, visibilidad):     " + p.numErroresClases);
        System.out.println("  Ámbitos e identificadores (no declarado / fuera de ámbito): " + p.numErroresAmbitos);
        System.out.println("  Declaraciones duplicadas:                                  " + p.numErroresDuplicados);
        System.out.println("  Tipos y constantes:                                        " + (p.numErroresTipos + p.numErroresConstantes));
        System.out.println("  Otros (convención de nombres, inicialización):             " + otros);
        System.out.println("Resultado general: " + (p.numErroresSemanticos == 0 ? "CORRECTO (sin errores semánticos)"
            : "CON ERRORES (" + p.numErroresSemanticos + " errores semánticos en total)"));
        System.out.println("================================================================================");
    }

    /** Primera pasada: recolecta las declaraciones de todo el archivo sin imprimir nada (permite usar métodos y atributos declarados más abajo). */
    private static TablaSimbolos primeraPasada(String fuente, PrintStream salida) {
        Parser p1 = new Parser(new Lexer(new StringReader(fuente)));
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        try { p1.parse(); }
        catch (Exception e) { /* tabla parcial: basta para resolver nombres */ }
        finally { System.setOut(salida); }
        return p1.tabla;
    }

    /** Analiza un archivo y devuelve el código de salida: 0 válido, 1 inválido, 2 error de lectura. */
    private static int analizar(String ruta, boolean mostrarTokens) {
        PrintStream salida = System.out;
        try {
            String fuente = new String(Files.readAllBytes(Paths.get(ruta)), StandardCharsets.UTF_8);
            TablaSimbolos prototipos = primeraPasada(fuente, salida);
            Lexer lexer = new Lexer(new StringReader(fuente));
            lexer.mostrarTokens = mostrarTokens;
            Parser parser = new Parser(lexer);
            parser.protos = prototipos;
            System.out.println("Analisis semantico: estructuras de control, metodos, clases y ambitos (C++, tipado estatico)\nArchivo: " + ruta);
            boolean completo = true;
            try { parser.parse(); }
            catch (Exception e) { completo = false; }
            boolean valido = completo && parser.numErrores == 0 && parser.numErroresSemanticos == 0 && lexer.numErrores == 0;
            imprimirReporteSemantico(parser, lexer);
            imprimirReporteEstructuras(parser);
            if (valido) parser.tabla.imprimirTabla();
            else System.out.println("Tabla de simbolos no generada: el analisis contiene errores.");
            System.out.println("RESULTADO: " + (valido ? "VALIDO" : "INVALIDO")
                + " | errores sintacticos: " + parser.numErrores
                + " | errores semanticos: " + parser.numErroresSemanticos
                + " | errores lexicos: " + lexer.numErrores
                + " | analisis " + (completo ? "completo" : "detenido"));
            return valido ? 0 : 1;
        } catch (IOException e) {
            System.err.println("No se pudo leer el archivo: " + e.getMessage());
            return 2;
        }
    }

    /** Menú interactivo (sin argumentos): lista las pruebas de pruebas_semantica y ejecuta SOLO la que se elija. */
    private static void menu() {
        File carpeta = new File("pruebas_semantica");
        File[] archivos = carpeta.listFiles((d, n) -> n.endsWith(".txt"));
        if (archivos == null || archivos.length == 0) {
            System.out.println("No se encontró la carpeta pruebas_semantica con archivos .txt (ejecute desde la carpeta del proyecto).");
            System.exit(2);
        }
        Arrays.sort(archivos);
        List<File> lista = new ArrayList<File>(Arrays.asList(archivos));
        Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8);
        while (true) {
            System.out.println();
            System.out.println("PRUEBAS DISPONIBLES (pruebas_semantica):");
            for (int i = 0; i < lista.size(); i++) System.out.println("  " + (i + 1) + ". " + lista.get(i).getName());
            System.out.println("  0. Salir");
            System.out.print("Elija el número de la prueba a ejecutar: ");
            if (!entrada.hasNextLine()) return;
            String texto = entrada.nextLine().trim();
            int n;
            try { n = Integer.parseInt(texto); }
            catch (NumberFormatException e) { System.out.println("Opción no válida."); continue; }
            if (n == 0) return;
            if (n < 1 || n > lista.size()) { System.out.println("Opción no válida."); continue; }
            System.out.println();
            analizar(lista.get(n - 1).getPath(), false);
            System.out.print("\nPresione Enter para volver al menú...");
            if (!entrada.hasNextLine()) return;
            entrada.nextLine();
        }
    }

    public static void main(String[] args) {
        // La terminal también debe usar UTF-8: en PowerShell, chcp 65001.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        imprimirEncabezado();
        if (args.length == 0) {          // sin argumentos: menú para elegir una sola prueba
            menu();
            System.exit(0);
        }
        System.exit(analizar(args[0], args.length > 1 && args[1].equals("--tokens")));
    }
}