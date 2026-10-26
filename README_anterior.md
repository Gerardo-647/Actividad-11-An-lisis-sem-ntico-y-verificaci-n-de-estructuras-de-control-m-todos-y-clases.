# Traductores de Lenguaje — Analizador Léxico y Sintáctico

Proyecto de la materia **Traductores de Lenguaje** (profesor José Navarro Ríos).
Implementado con **JFlex** (analizador léxico) y **CUP** (analizador sintáctico) en Java.

**Integrantes:**
- Cancelada de la O, Gerardo Alexander
- Castellanos Hernández, Alan
- Lara Jiménez, Pablo César
- Medina Robledo, Brandon Ernie

---

## 📌 Práctica actual: Análisis sintáctico de expresiones aritméticas y lógicas

Esta entrega agrega al analizador el reconocimiento de **sentencias de lectura, escritura
y asignaciones con expresiones** dentro de un método (`void nombre() { ... }`), junto con
su **manejo y recuperación de errores**. Todo lo de las prácticas anteriores se conserva
(respaldo en la etiqueta git `respaldo-practica-declaraciones`).

- Nuevas palabras reservadas: `leer`, `escribir`.
- Los métodos pueden ir dentro de la clase o como métodos sueltos en el archivo.
- Expresiones **aritméticas** (`+ - * / %`) y **lógicas** (`&& || !`), con paréntesis
  anidados, identificadores y valores (números y booleanos).
- Retroalimentación positiva: cada regla reconocida imprime `Regla reconocida: <regla>`.
  Si una sentencia contiene errores **no** se imprime su "Regla reconocida".
- Errores reportados con **línea, columna, token encontrado y causa**; el análisis
  continúa con las sentencias posteriores.

| Error de entrada | Causa reportada |
|---|---|
| `leer(a b);` | Falta ',' entre identificadores |
| `escribir("Inicio"` (sin `)` ni `;`) | Falta ')' o ';' en la sentencia de escritura |
| `escribir("a";` / `leer(a;` | Falta ')' |
| `escribir(a)` / `leer(a)` / `x = a + b` (sin `;`) | Falta ';' |
| `total = a + ;` / `y = a && ;` / `z = !;` | Expresión aritmética/lógica inválida: falta un operando |
| `= a + b;` | Identificador esperado al inicio de la asignación |
| `x = ;` | Falta la expresión en la asignación |
| `leer(a,,b);` / `escribir(a,,b);` | Coma duplicada |
| `escribir(,a);` / `leer(,a);` | Falta un elemento/identificador antes de la coma |
| `escribir(a,);` / `leer(a,);` | Falta un elemento/identificador después de la coma |
| `escribir();` / `leer();` | Falta al menos un elemento/identificador |
| Cualquier otra sentencia mal formada | Sentencia inválida (se descarta hasta el siguiente `;`) |

### Gramática desarrollada (nueva, agregada a `Parser.cup`)

```
metodo                -> 'void' identificador '(' ')' '{' cuerpo_metodo '}'
cuerpo_metodo         -> (sentencia)*
sentencia             -> lectura | escritura | asignacion

lectura               -> 'leer' '(' lista_identificadores ')' ';'
escritura             -> 'escribir' '(' lista_elementos ')' ';'
lista_elementos       -> elemento (',' elemento)*
elemento              -> cadena | expresion
asignacion            -> identificador '=' expresion ';'

expresion             -> expresion_aritmetica | expresion_logica | identificador | valor
expresion_aritmetica  -> termino_aritmetico (operador_aritmetico termino_aritmetico)*
termino_aritmetico    -> identificador | valor | '(' expresion_aritmetica ')'
expresion_logica      -> termino_logico (operador_logico termino_logico)*
termino_logico        -> identificador | valor | '!' termino_logico | '(' expresion_logica ')'
valor                 -> numero | booleano
```

Notas de implementación:
- `lista_identificadores` (de la práctica anterior) se reutiliza en `leer`.
- Para que la gramática sea LALR(1) sin conflictos, `expresion_aritmetica` y
  `expresion_logica` requieren al menos un operador (o una negación / paréntesis); un
  identificador o valor solo se reconoce por `expresion -> identificador | valor`, y
  `elemento -> identificador | valor` queda incluido en `elemento -> expresion`.
- No se mezclan operadores aritméticos y lógicos sin paréntesis, tal como define la gramática.

---

## 📚 Práctica anterior: Declaración de Atributos, Variables y Constantes

La práctica anterior agregó al analizador el reconocimiento de **declaraciones de atributos,
variables y constantes** dentro del cuerpo de una clase:

- Declaraciones con y sin inicialización (`int edad;` / `float salario = 5000;`).
- Declaraciones múltiples (`String nombre, apellido;`).
- Constantes obligatoriamente inicializadas con la palabra reservada `final`
  (`final double PI = 3.1416;`).
- Tipos soportados: `int`, `float`, `double`, `char`, `boolean`, `String`, o un
  identificador (tipo definido por el usuario).
- **Detección y recuperación de errores**:
  - Falta de coma entre identificadores (`int edad nombre;`).
  - Constante sin inicializar (`final int MAX;`).
  - Declaraciones incompletas o mal formadas (recuperación genérica hasta el
    siguiente `;`).
  - Cada error reporta línea, columna, token encontrado y una descripción clara,
    y el análisis **continúa** con el resto del archivo en vez de detenerse.

### Gramática desarrollada (nueva, agregada a `Parser.cup`)

```
cuerpo_clase -> (atributo | constante)*

atributo -> tipo lista_identificadores ('=' valor)? ';'

constante -> 'final' tipo identificador '=' valor ';'

lista_identificadores -> identificador (',' identificador)*

tipo -> 'int' | 'float' | 'double' | 'char' | 'boolean' | 'String' | identificador

valor -> numero | cadena | caracter | booleano

identificador -> [a-zA-Z_][a-zA-Z0-9_]*
```

---

## 📁 Estructura del repositorio

```
traductoresLenguajes/
├── Lexer.jflex                  # Especificación del analizador léxico (fuente)
├── Parser.cup                   # Especificación del analizador sintáctico / gramática (fuente)
├── Lexer.java                   # Generado por JFlex a partir de Lexer.jflex
├── Parser.java, sym.java        # Generados por CUP a partir de Parser.cup
├── Main.java                    # Punto de entrada del programa
├── java-cup-11b.jar             # Herramienta CUP (generador del parser)
├── java-cup-11b-runtime.jar     # Runtime necesario para compilar/ejecutar
├── jflex-full.jar                # Herramienta JFlex (generador del lexer)
├── act1_Traductores.txt         # Archivo de prueba de la práctica anterior
├── prueba1_valido.txt           # Prueba (declaraciones): válidas
├── prueba2_invalido.txt         # Prueba (declaraciones): errores sintácticos
├── prueba3_combinado.txt        # Prueba (declaraciones): válidas e inválidas combinadas
├── prueba_extra_incompleta.txt  # Prueba (declaraciones): declaración incompleta
├── prueba4_expresiones_valido.txt      # Prueba (expresiones): leer, escribir y asignaciones válidas
├── prueba5_expresiones_invalido.txt    # Prueba (expresiones): errores sintácticos
└── prueba6_expresiones_combinado.txt   # Prueba (expresiones): clase con estructuras válidas e inválidas
```

---

## ▶️ Cómo ejecutar el proyecto

Requiere **Java (JDK) 17 o superior** instalado (`java -version` / `javac -version`).

### 1. Generar el lexer y el parser (solo si se modifica la gramática)

```bash
java -jar jflex-full.jar Lexer.jflex
java -jar java-cup-11b.jar -parser Parser -symbols sym Parser.cup
```

> Ya se incluyen `Lexer.java`, `Parser.java` y `sym.java` generados, así que este
> paso puede omitirse si no se modificó `Lexer.jflex` ni `Parser.cup`.

### 2. Compilar

```bash
javac -cp .:java-cup-11b-runtime.jar Lexer.java sym.java Parser.java Main.java
```

En Windows, usar `;` en vez de `:` en el classpath:
```bash
javac -cp .;java-cup-11b-runtime.jar Lexer.java sym.java Parser.java Main.java
```

### 3. Ejecutar con un archivo de prueba

```bash
java -cp .:java-cup-11b-runtime.jar Main prueba1_valido.txt
java -cp .:java-cup-11b-runtime.jar Main prueba2_invalido.txt
java -cp .:java-cup-11b-runtime.jar Main prueba3_combinado.txt
java -cp .:java-cup-11b-runtime.jar Main prueba4_expresiones_valido.txt
java -cp .:java-cup-11b-runtime.jar Main prueba5_expresiones_invalido.txt
java -cp .:java-cup-11b-runtime.jar Main prueba6_expresiones_combinado.txt
```

Cada ejecución imprime en consola el análisis léxico (token por token) y el
análisis sintáctico (reglas reconocidas y errores detectados, con línea, columna,
token y descripción).

---

## 🧩 Historial de prácticas

Este repositorio acumula el desarrollo del analizador a lo largo del curso.
La práctica más reciente (expresiones, lectura, escritura y asignaciones) está
descrita arriba; la de declaraciones de atributos/variables/constantes y las
estructuras generales del lenguaje (paquete, importaciones, delimitación de la clase)
se conservan de prácticas anteriores. La versión de recuperación de la práctica
previa está en la etiqueta `respaldo-practica-declaraciones`
(`git checkout respaldo-practica-declaraciones`).
