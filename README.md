# Análisis semántico de estructuras de control, métodos y clases (práctica actual)

Traductores de Lenguaje · C++ (subconjunto) · JavaCUP + JFlex.
Integrantes: Cancelada de la O, Gerardo Alexander · Castellanos Hernández, Alan · Lara Jiménez, Pablo César · Medina Robledo, Brandon Ernie.

Se construyó **sobre el proyecto de la práctica anterior** (léxico, sintaxis, tabla de símbolos, ámbitos, verificación de tipos).
Todo eso se conserva: las 62 pruebas anteriores siguen dando el mismo resultado. El respaldo del proyecto anterior (sin modificar)
está en `../respaldo_verificacion_tipos.zip`.

## Qué se agregó

| Archivo | Cambio |
|---|---|
| `Nodo.java` (nuevo) | Resultado de una sentencia: árbol + información de flujo (`retorna`, `hayBreak`) para verificar el `return` en todos los caminos. |
| `Argumentos.java` (nuevo) | Lista de argumentos de una llamada (cada uno con tipo y posición). |
| `MarcaAmbito.java` (nuevo) | Foto del contexto al abrir un bloque; permite recuperar los ámbitos tras un error sintáctico. |
| `ManejadorAmbitos.java` (nuevo) | Pila de ámbitos `GLOBAL / CLASE / METODO / BLOQUE` con nombres únicos y jerárquicos (`Calculadora.ejecutar.bloque1`). |
| `ManejadorClases.java` (nuevo) | Herencia, resolución de miembros, visibilidad (`public/private/protected`) y compatibilidad entre objetos. |
| `ManejadorMetodos.java` (nuevo) | Resolución de sobrecargas y correspondencia argumentos ↔ parámetros (cantidad y tipos). |
| `Simbolo.java` / `TablaSimbolos.java` | Visibilidad, clase base, parámetros y posición; búsqueda con herencia; categorías `CONSTRUCTOR` y `PARAMETRO`; la tabla impresa añade las columnas *Visib.* y *Detalle*. |
| `Expr.java` | Marca `constante` (literal o `const`), usada para validar las etiquetas de `case`. |
| `Parser.cup` | Acciones semánticas nuevas (ver abajo) y reglas para constructores, herencia, modificadores, `obj.miembro`, `obj.metodo()`, declaraciones globales. 0 conflictos. |
| `Lexer.jflex` | Palabra reservada `protected`. |
| `Main.java` | **Dos pasadas** (la primera, silenciosa, recolecta las declaraciones del archivo) y el nuevo reporte semántico con resumen por categoría. |

## Reglas implementadas

**Estructuras de control.** Condiciones `bool` en `if / else if / while / do-while / for`; `break` solo dentro de un ciclo o `switch`;
`continue` solo dentro de un ciclo; `return` solo dentro de un método (se detecta también a nivel de clase o global);
`switch`: expresión entera, `case` literal o constante, etiquetas `case` duplicadas y `default` repetido.
El `for` y el cuerpo del `switch` tienen ámbito propio.

**Métodos.** Registro con firma completa; duplicados por firma (la sobrecarga con distintos parámetros es válida);
la llamada se resuelve contra la clase actual (y sus bases) o las funciones globales, con **sobrecarga**: cantidad de argumentos, tipo de cada
argumento (con las promociones permitidas) y ambigüedad. Tipo de retorno compatible, `void` sin valor, y **todos los caminos de ejecución
devuelven un valor** (`if/else`, cadenas `else if`, `switch` con `default`, `while(true)` sin `break`). Se pueden usar métodos declarados más abajo.

**Clases.** Clases duplicadas, clase usada como tipo sin declarar, herencia (`class B : public A`, clase base inexistente, herencia de sí misma),
constructores (nombre igual a la clase, sobrecarga, constructor sin parámetros para `Persona p;`), atributos y métodos con visibilidad
(`public:`/`private:`/`protected:` o modificador por miembro; en `class` el valor por defecto es `private`), acceso `obj.atributo`,
`obj.atributo = valor` y `obj.metodo(args)`, atributo/método inexistente, miembro privado o protegido desde un contexto no autorizado,
compatibilidad de asignación entre objetos (una `Derivada` cabe en una `Base`).

**Ámbitos.** Cada bloque `{ }`, cada `for` y cada `switch` abren un ámbito; las declaraciones de otro método o clase no chocan.
Al usar un identificador no visible se distingue: *no declarado*, *fuera de ámbito* (se indica en qué ámbito sí existe) y
*usado antes de su declaración*. Variables globales y constantes globales.

## Mensajes (línea, columna, token y causa)

```
Error semántico en línea 23, columna 21: El método 'sumar' requiere 2 argumentos, pero se proporcionó 1 (encontrado: 'sumar').
Error semántico en línea 26, columna 27: El argumento 1 del método 'sumar' es de tipo 'string'; se esperaba un tipo compatible con 'int' (encontrado: '"Hola"').
Error semántico en línea 32, columna 9: La instrucción 'break' no se encuentra dentro de una estructura que permita su utilización (ciclo o switch) (encontrado: 'break').
Error semántico en línea 39, columna 21: El identificador 'valorLocal' no está disponible en el ámbito actual (está declarado en el ámbito 'CalculadoraErrores.ejecutar.bloque2') (encontrado: 'valorLocal').
Error semántico en línea 43, columna 1: El método 'depositar' debe devolver un valor compatible con el tipo 'bool' en todos los caminos de ejecución (encontrado: '}').
```

## Recuperación de errores

Un error semántico no detiene el análisis: se registra con su ubicación, la expresión incorrecta toma el tipo `ERROR` (no se generan errores
en cascada), las declaraciones inválidas (firma duplicada, clase duplicada) **no** se insertan en la tabla, y al cerrar cada bloque se restaura
el contexto (ámbitos, ciclos, switch) aunque un error sintáctico haya saltado producciones. Al final se imprime el reporte con el resumen por
categoría y el resultado general. Con errores no se imprime la tabla de símbolos (comportamiento heredado).

## Compilar, ejecutar y probar

```bat
compilar.bat
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_valido_calculadora.txt
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_invalido_calculadora_errores.txt
```
En Linux/macOS: `sh compilar.sh` y classpath con `:`. `demo_semantica.bat` ejecuta las pruebas principales con pausas.
Para regenerar tras editar `Parser.cup` o `Lexer.jflex`: `regenerar.bat` (CUP: 0 conflictos).
`python verificar.py` ejecuta las **75 pruebas** (62 anteriores + 13 nuevas) y comprueba el código de salida y el número exacto de errores semánticos.
`python generar_evidencias_semantica.py` regenera `evidencias_semantica/` (salidas reales y `tabla_resultados_semantica.md`, esperado vs obtenido).

## Pruebas (`pruebas_semantica/`, salidas en `evidencias_semantica/`)

| Archivo | Resultado | Qué demuestra |
|---|---|---|
| `sem_valido_calculadora.txt` | VÁLIDO | Código válido del enunciado (adaptado a C++). Imprime la tabla de símbolos. |
| `sem_invalido_calculadora_errores.txt` | INVÁLIDO, 8 | Los 7 errores del enunciado + retorno incompatible, con recuperación. |
| `sem_valido_control.txt` / `sem_invalido_control.txt` | VÁLIDO / INVÁLIDO, 11 | Condiciones, `break`/`continue`, `switch` (duplicados, no constantes, `default` doble), ámbito del `for`. |
| `sem_valido_metodos.txt` / `sem_invalido_metodos.txt` | VÁLIDO / INVÁLIDO, 11 | Sobrecarga, recursividad, llamadas anticipadas; cantidad/tipo de argumentos, `void`, firma duplicada, método inexistente. |
| `sem_invalido_retorno_caminos.txt` | INVÁLIDO, 6 | Métodos con caminos sin `return` (`if`, `if/else`, `while`, `switch`, `while(true)` con `break`). |
| `sem_invalido_return_fuera.txt` | INVÁLIDO, 2 | `return` fuera de un método (global y a nivel de clase). |
| `sem_valido_clases.txt` / `sem_invalido_clases.txt` | VÁLIDO / INVÁLIDO, 23 | Objetos, constructores, herencia, visibilidad, miembros inexistentes, clases no declaradas/duplicadas. |
| `sem_valido_ambitos.txt` / `sem_invalido_ambitos.txt` | VÁLIDO / INVÁLIDO, 13 | Bloques anidados, sombreado, duplicados, fuera de ámbito, usado antes de declarar, miembros de otra clase. |
| `sem_invalido_multiple.txt` | INVÁLIDO, 14 | Errores de todas las categorías en un archivo y resumen final. |

## Decisiones y límites

- Se conserva la regla de la práctica anterior: toda variable local de tipo primitivo se inicializa (el código de ejemplo del enunciado
  declara `int resultado;`, por eso las pruebas escriben `int resultado = 0;`). Un **objeto** local no la exige (se construye con su constructor).
- C++ usa `true/false` y `bool`; `escribir(...)`/`cout <<` para salida. `leer` y `escribir` son palabras reservadas heredadas.
- Un `if` con `int` se rechaza aunque C++ real lo acepte (decisión del enunciado). El acceso a miembros es de un nivel (`obj.miembro`, sin `a.b.c`).
- No se incluyen miembros `static`, métodos `virtual`, listas de inicialización de constructores ni punteros/referencias; el subconjunto de C++ del proyecto no los
  contempla. La herencia es simple y sin ocultamiento de atributos.
- La verificación de retorno es conservadora: no evalúa condiciones constantes salvo `while(true)` / `for(;;)`, y se omite en métodos cuyo cuerpo ya tiene un error sintáctico.

---

# Verificación semántica de tipos (práctica anterior)

Se construyó **sobre el proyecto de la práctica anterior** (gramática, tabla de símbolos, ámbitos,
convenciones de nombres, duplicados, uso de identificadores no declarados). Todo eso se conserva; se
agregó únicamente la lógica de verificación de tipos. El respaldo del trabajo anterior está en
`../respaldo_actividad_anterior.zip` (copia sin modificar del ZIP entregado en la práctica anterior).

## Lenguaje asignado y paradigma

**C++ (subconjunto)**: lenguaje **estáticamente tipado**. Cada variable, atributo y constante se declara con un
tipo explícito e inmutable (`int, float, double, char, bool, string`), y el compilador valida durante el análisis
semántico que el valor asignado sea compatible con ese tipo. El tipo de un identificador sale de la Tabla de
Símbolos (`Simbolo.getTipoDato()`), no del último valor asignado.

## Qué se agregó

| Archivo | Cambio |
|---|---|
| `Expr.java` (nuevo) | Resultado de una expresión: `texto`, `tipo`, `linea`, `columna`. |
| `ManejadorTipos.java` (nuevo) | Reglas puras de compatibilidad: promoción, asignabilidad, tipo resultante de cada operador y causa del error. |
| `Parser.cup` | Los no terminales de expresión (`expresion, log_or, ..., primario, valor, condicion, llamada_metodo`) ahora devuelven `Expr`. Se añadieron acciones de verificación en operadores, declaraciones, asignaciones, condiciones, `switch/case`, `return`, `++/--` y lectura. |
| `TablaSimbolos.java` | Método `buscarMetodo` (tipo de retorno de una llamada). |
| `Main.java` | Imprime el **reporte del análisis semántico de tipos** (verificaciones realizadas y errores por categoría). |

## Reglas de tipos implementadas

- **Asignación** (declaración de atributo/variable/constante y `=`, `+=`, `-=`, `*=`, `/=`): el valor debe tener el tipo
  declarado o ser un ensanchamiento numérico (`char → int → float → double`). `bool` no se guarda en `int`, ni `string` en `char`,
  ni `float` en `int` (se pierde información).
- **Aritméticas** `+ - * / %`: operandos numéricos; `int` con `float` da `float` (promoción). `%` solo con enteros (`int`/`char`).
  `string + string` es concatenación; con `bool` o con otros tipos es error.
- **Relacionales** `> < >= <=`: solo numéricos. `==` y `!=`: numéricos entre sí, o dos `string`, `char` o `bool` del mismo tipo. Resultado siempre `bool`.
- **Lógicos** `&& || !`: operandos estrictamente `bool`; resultado `bool`. Unarios `-` y `+`: numéricos.
- **Condiciones** de `if`, `else if`, `while`, `do-while` y `for`: deben ser `bool` (un `int` es error).
- **switch/case**: la expresión del `switch` y los valores de `case` deben ser enteros (`int`/`char`).
- **return**: debe coincidir con el tipo del método; un método `void` no puede devolver valor y un método no-`void` no puede hacer `return;`.
  Una llamada a un método `void` no puede usarse como valor.
- **Identificadores**: se busca en la Tabla de Símbolos (ámbito actual y contenedores) y se usa su tipo en la expresión.
- **Inmutabilidad de constantes**: se pueden leer, pero `=`, `+=`, `++`, `--`, `cin >>` y `leer()` sobre una constante son error.
- **Sin errores en cascada**: una subexpresión con error toma el tipo `ERROR` y no genera más errores arriba de ella.

## Errores semánticos reportados

Mismo estilo que el resto del proyecto: `Error semántico en línea X, columna Y: causa (encontrado: 'token').`

```
Error semántico en línea 11, columna 9: Incompatibilidad de tipos: No se puede asignar un valor de tipo 'string' a la variable 'a' de tipo 'int' (encontrado: 'a').
Error semántico en línea 14, columna 28: El operador aritmético '*' no es aplicable a los operandos de tipo 'bool' y 'int' (encontrado: '*').
Error semántico en línea 17, columna 13: La expresión dentro de la condición 'if' debe ser de tipo 'bool', se encontró 'int' (encontrado: 'contador + 5').
Error semántico en línea 22, columna 9: No se puede reasignar un valor a la constante 'LIMITE' (encontrado: 'LIMITE').
Error semántico en línea 25, columna 13: la variable 'variableInexistente' no ha sido declarada (encontrado: 'variableInexistente').
```

La posición es la del operador en errores de operadores, la del identificador en asignaciones/constantes,
y la del inicio de la expresión en condiciones. Las reglas válidas ahora imprimen también el tipo:
`Regla reconocida: expresion_aritmetica -> (base + factor) * PI [tipo: float]`.

## Compilar y ejecutar

```bat
compilar.bat
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_tipos\tipos_valido_basico.txt
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_tipos\tipos_invalido_basico.txt
```

`demo_tipos.bat` ejecuta las pruebas principales con pausas. Para regenerar el parser tras editar `Parser.cup`: `regenerar.bat`
(CUP: **0 conflictos**). En Linux/macOS cambia `;` por `:` en el classpath. `python verificar.py` ejecuta las 62 pruebas y
comprueba el código de salida y, en las de tipos, el **número exacto** de errores semánticos.

## Pruebas de verificación de tipos (`pruebas_tipos/`, salidas en `evidencias_tipos/`)

| Archivo | Resultado | Qué demuestra |
|---|---|---|
| `tipos_valido_basico.txt` | VÁLIDO | Código válido del enunciado (adaptado a C++): aritmética con variables/constantes, relacional, lógica, `if`. Imprime la tabla de símbolos. |
| `tipos_invalido_basico.txt` | INVÁLIDO, 5 errores | Los 5 errores del enunciado: asignación incompatible, `bool * int`, condición entera, reasignar constante, variable no declarada. |
| `tipos_valido_promocion.txt` | VÁLIDO | Promoción `int→float→double`, `char`, `%`, concatenación, compuestas, relacionales, lógicas, `switch`, `for`, llamadas a métodos. |
| `tipos_invalido_declaraciones.txt` | INVÁLIDO, 10 | Inicializaciones incompatibles en atributos, constantes y variables locales. |
| `tipos_invalido_operadores.txt` | INVÁLIDO, 19 | Todos los operadores con tipos no permitidos, unarios, compuestas, `++` sobre `bool`. |
| `tipos_invalido_condiciones.txt` | INVÁLIDO, 10 | Condiciones no booleanas en `if/else if/while/do-while/for`; `switch`/`case` no enteros. |
| `tipos_invalido_constantes.txt` | INVÁLIDO, 6 | `=`, `+=`, `++`, `--`, `cin >>`, `leer()` sobre constantes. |
| `tipos_invalido_retorno.txt` | INVÁLIDO, 6 | `return` incompatible, en `void`, vacío; llamada `void` como valor. |
| `tipos_invalido_cascada.txt` | INVÁLIDO, 3 | Un error no produce errores repetidos en las expresiones que lo contienen. |

## Pruebas heredadas ajustadas

Con la regla "una condición debe ser `bool`", siete pruebas heredadas que declaraban `int activo` y lo usaban como condición se
cambiaron a `bool activo`: `prueba4`, `prueba9`, `prueba14` (`bool esPar`), `pruebas/valido_agrupacion`, `valido_anidamiento`,
`valido_condicion_compuesta` y `valido_if_simple`. Su clasificación (válido/inválido) no cambió. El código de ejemplo del enunciado
declara `int resultado;` sin valor; el proyecto conserva la regla de la práctica anterior (toda variable local se inicializa), por lo que
las pruebas la inicializan. `verificar.py` arrastraba tres checks que ya no coincidían con la salida de `Main` (prueba9/10/11); se actualizaron.

## Límites de esta etapa

- Una llamada a un método **declarado más abajo** en el archivo queda sin tipo (no se verifica su resultado) y solo se valida su existencia al final;
  no se comparan cantidad ni tipos de argumentos contra los parámetros.
- Los tipos de clase (`Persona p;`) no se verifican en asignaciones.
- `int` usado como condición se rechaza aunque C++ real lo acepte, y `float → int` se rechaza como estrechamiento; ambas son decisiones del enunciado.

---

## Versión del lenguaje

Subconjunto de **C++98 (ISO/IEC 14882:1998)**. Herramientas: JFlex 1.9.1, JavaCUP 0.11b y JDK 17 o superior.
Extensiones heredadas que no son C++98: `package`, `final` (para constantes), `leer`, `escribir`, `String` y `boolean`.

## Ajustes posteriores a la revisión (Actividad 9)

- **Variables y métodos no declarados**: ahora se reconoce el uso de un identificador
  no declarado. Una variable/atributo/constante usada en una expresión, asignación,
  `cin` o incremento debe existir en el ámbito actual o en uno contenedor; una
  llamada a método debe corresponder a un método declarado (se valida al final del
  archivo porque en una clase un método puede usarse antes de definirse).
  Prueba: `act9_no_declarado.txt`.
- **Si hay errores no se imprime la tabla de símbolos**: `Main` solo la imprime cuando
  no hubo errores léxicos, sintácticos ni semánticos; en caso contrario muestra
  "Tabla de simbolos no generada".
- **Conteo separado**: el resumen final distingue `errores sintacticos`, `errores semanticos` y `errores lexicos`.
- Las pruebas válidas anteriores se ajustaron para declarar las variables que usan.

# Actividad 9: Análisis semántico — tabla de símbolos y tipos de datos

Se construyó **sobre este mismo proyecto** (Actividad 7/8, estructuras de control y
métodos), conservando toda su gramática y funcionalidad. Lo nuevo:

- **`Simbolo.java`**: clase con `id, tipoSimbolo, tipoDato, valor, ambito, linea,
  columna, token`.
- **`TablaSimbolos.java`**: estructura dinámica (`ArrayList`) + pila de ámbitos
  (`ArrayDeque`) que distingue `GLOBAL`, una `CLASE` o un `METODO`, para que una
  variable local de un método no choque con la de otro método ni con los
  atributos/constantes de la clase que lo contiene.
- **`Parser.cup`**: se agregaron acciones semánticas (usando el soporte de CUP
  para acciones intermedias dentro de una producción) en `clase`, `metodo`,
  `parametro`, `declaracion`/`nombres`/`inicializacion` (atributos y variables) y
  las dos formas de `FINAL ... ;` (constantes), que clasifican cada identificador,
  validan su convención de nombre (PascalCase para clases; camelCase para métodos,
  atributos y variables; SNAKE_CASE o camelCase para constantes), validan la regla
  de inicialización (obligatoria en variable/constante, opcional en atributo),
  detectan duplicados en el ámbito actual, y lo insertan en la tabla.
- Al terminar el análisis de todo el archivo (si no hubo un error fatal), se
  imprime la tabla de símbolos completa con el formato de columnas solicitado.
- Los errores semánticos usan el mismo estilo que los sintácticos del proyecto
  (`Error semántico en línea X, columna Y: causa (encontrado: 'token').`) y
  también se cuentan en `numErrores`, así que afectan el resultado VÁLIDO/INVÁLIDO.

Pruebas nuevas en la raíz del proyecto: `act9_valido_completo.txt` (clase,
atributos, constante, método con parámetro y variable local, todo correcto),
`act9_naming_invalido.txt` (clase/atributo/método con convención incorrecta),
`act9_init_invalido.txt` (constante y variable local sin inicializar) y
`act9_duplicado.txt` (identificador repetido en el mismo ámbito). Las pruebas de
la actividad 7/8 se conservan; `prueba7_control_valido.txt` y
`pruebas/valido_compatibilidad.txt` se ajustaron para inicializar sus variables
locales, ya que la actividad 9 exige esa inicialización y antes no se verificaba.

## Actividad 7: Análisis sintáctico de estructuras de control

Traductores de Lenguaje · C++ · JavaCUP + JFlex
Docente: José Navarro Ríos (según el README anterior).
Equipo 2. Grupo: pendiente de completar.

## Integrantes

1. Cancelada de la O, Gerardo Alexander.
2. Castellanos Hernández, Alan.
3. Lara Jiménez, Pablo César.
4. Medina Robledo, Brandon Ernie.

## Qué se agregó

- `if`, `if/else`, cadenas `else if`, `switch/case/default/break`.
- `while`, `do...while`, `for`, con anidamiento mediante bloques recursivos.
- Relaciones `> < >= <= == !=`; lógica `&& || !`; aritmética `+ - * / %`.
- Precedencia por niveles de expresión y paréntesis agrupadores.
- `cin >> a`, `cout << "texto" << a`; `const`, `string`, `bool`.
- Incremento/decremento, asignación y asignación compuesta; declaraciones locales.
- Funciones sin parámetros con retorno `void` o tipo; `return`.
- Mensajes de reglas, diagnóstico con línea/columna y árbol textual de anidamiento.
- Recuperación con producciones `error` de CUP y sincronización en `;`, `)` y `}`.
- Resultado global VÁLIDO/INVÁLIDO y códigos de salida 0/1; 2 indica un problema de uso/archivo.

Se conservan las entradas de prácticas anteriores. `package`, `final`, `String`,
`boolean`, `leer` y `escribir` son extensiones heredadas, no sintaxis nativa de C++.
El respaldo original está en `../respaldo_actividad_anterior.zip`.
La gramática de expresiones anterior se reorganizó por precedencia para integrar
condiciones que combinan aritmética, comparación y lógica sin ambigüedades.

## Compilar en Windows

Instala un JDK (se verificó con Java 17), abre una terminal dentro de `proyecto`
y ejecuta:

```bat
chcp 65001
javac -encoding UTF-8 -cp ".;java-cup-11b-runtime.jar" Lexer.java sym.java Parser.java Main.java
```

También puedes ejecutar `compilar.bat`. Si `javac` no se reconoce, necesitas el
JDK y su carpeta `bin` en PATH. No basta con una instalación que solo ejecute Java.
Los archivos Java generados ya están incluidos; no hay que regenerarlos para probar.

## Pruebas de la actividad 7

```bat
java -cp ".;java-cup-11b-runtime.jar" Main prueba7_control_valido.txt
java -cp ".;java-cup-11b-runtime.jar" Main prueba8_control_invalido.txt
java -cp ".;java-cup-11b-runtime.jar" Main prueba9_anidamiento.txt
java -cp ".;java-cup-11b-runtime.jar" Main prueba10_recuperacion.txt
java -cp ".;java-cup-11b-runtime.jar" Main prueba11_combinado.txt
```

| Archivo | Resultado esperado | Qué demuestra |
|---|---|---|
| prueba7 | VÁLIDO | Todas las estructuras y condiciones |
| prueba8 | INVÁLIDO, análisis completo | Cinco errores y continuación |
| prueba9 | VÁLIDO | Jerarquía for / if / while y más anidamientos |
| prueba10 | INVÁLIDO, análisis completo | Recuperación y `CONTINUACION_RECUPERADA` |
| prueba11 | INVÁLIDO, análisis completo | Funciones anteriores + nuevas + error |

**Que una prueba con errores diga INVÁLIDO es lo correcto.** Recuperarse no convierte
el archivo en válido. Un bloque o método que contiene `[ERROR]` se muestra en el
árbol para ubicarlo, pero no recibe el mensaje positivo de regla reconocida.
`demo.bat` ejecuta estas cinco pruebas con pausas. Añade `--tokens` al final de un
comando para ver el análisis léxico, por ejemplo:

```bat
java -cp ".;java-cup-11b-runtime.jar" Main prueba7_control_valido.txt --tokens
```

Las pruebas anteriores siguen usando los mismos comandos que compartió Alan.
En Linux/macOS cambia `;` por `:` dentro del classpath.

## Regenerar después de editar la gramática o el lexer

Edita `Parser.cup` y `Lexer.jflex`, no las tablas generadas de `Parser.java`.

```bat
java -jar java-cup-11b.jar -parser Parser -symbols sym Parser.cup
java -jar jflex-full.jar Lexer.jflex
javac -encoding UTF-8 -cp ".;java-cup-11b-runtime.jar" Lexer.java sym.java Parser.java Main.java
```

O ejecuta `regenerar.bat`. CUP genera con **0 conflictos**; las advertencias de
terminales sin uso corresponden a tokens heredados reservados para otras etapas.

## Verificación y evidencias

Se ejecutaron 45 archivos: 20 válidos y 25 inválidos/mixtos. Los 45 tuvieron la
clasificación esperada. Además se comprobó la presencia de todas las reglas de
control, el anidamiento y la continuación posterior al error. Hay 33 pruebas
específicas en `pruebas/` y 12 entradas en la raíz (incluidas las anteriores).
`evidencias/` contiene salidas reales de ejecución, resumen JSON, tabla Markdown y
la salida del generador CUP. `python verificar.py` repite la suite tras compilar;
Python solo es necesario para esa verificación automática, no para el analizador.

## Alcance y límites

Es un analizador léxico/sintáctico educativo, no ejecuta los ciclos ni genera
código máquina. Los nombres de variables de algunos ejemplos se asumen existentes
porque no se comprueba la tabla de símbolos. (En la actividad 7 no se verificaban tipos; ahora sí, ver arriba.) No verifica que los `case`
sean constantes, duplicados de `case/default` ni el contexto semántico de `break`.
Los cuerpos de control requieren llaves. Las funciones son sin parámetros;
no se implementan llamadas generales, plantillas, punteros, arreglos, `std::`,
`for` por rango ni todo el estándar C++. Para `cin/cout`, usa `using namespace std;`.
La recuperación no garantiza continuar tras cualquier error: un archivo truncado
con llaves faltantes puede terminar como “análisis detenido”. Los diagnósticos
señalan el token donde se detecta el problema, no siempre donde comenzó.

## Entrega

El ZIP contiene el reporte PDF, fuente Markdown del reporte, guion del video,
proyecto y respaldo. Completa el grupo de portada y revisa los datos institucionales.
Graba el video con audio, máximo 5 minutos y mínimo 720p, con fecha y hora visibles;
publica el enlace público. Sube el proyecto al repositorio del equipo y entrega
su enlace. Esas publicaciones no se han realizado desde este entorno.

## Encabezado y evidencias actualizados

Main imprime la asignatura y los cuatro integrantes antes de cada análisis.
La salida de Java es UTF-8; en PowerShell ejecuta `chcp 65001` antes de las pruebas.
Para ejecutar el script en PowerShell escribe `.\demo.bat`, desde `proyecto`.
Las capturas aportadas por Pablo se conservan sin alterar en el reporte como
evidencias previas al ajuste del encabezado y de codificación.
