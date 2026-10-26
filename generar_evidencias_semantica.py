"""Ejecutar después de compilar. Genera evidencias_semantica/*.out.txt (salida real de cada prueba) y
evidencias_semantica/tabla_resultados_semantica.md con la tabla esperado vs obtenido que pide el reporte."""
from pathlib import Path
import os, re, subprocess

p = Path(__file__).resolve().parent
ev = p / 'evidencias_semantica'
ev.mkdir(exist_ok=True)

def ejecutar(rel):
    r = subprocess.run(['java', '-cp', '.' + os.pathsep + 'java-cup-11b-runtime.jar', 'Main', rel],
                       cwd=p, encoding='utf-8', stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=20)
    return r.stdout

salidas = {}
for f in sorted((p / 'pruebas_semantica').glob('*.txt')):
    out = ejecutar('pruebas_semantica/' + f.name)
    salidas[f.name] = out
    (ev / (f.stem + '.out.txt')).write_text(out, encoding='utf-8')

# (prueba, tipo, resultado esperado, archivo, expresión buscada, ¿debe aparecer?)
CASOS = [
    ('Condición booleana en if', 'Válida', 'Aceptar la condición', 'sem_valido_calculadora.txt', r"condición 'if'", False),
    ('Condición numérica en if', 'Inválida', 'Reportar error de tipo', 'sem_invalido_calculadora_errores.txt', r"condición 'if' debe ser de tipo 'bool'", True),
    ('Condición booleana en un ciclo', 'Válida', 'Aceptar la condición', 'sem_valido_control.txt', r"condición '(while|for|do-while)'", False),
    ('Uso de break dentro de un ciclo', 'Válida', 'Aceptar la instrucción', 'sem_valido_control.txt', r"'break'", False),
    ('Uso de continue fuera de un ciclo', 'Inválida', 'Reportar error de contexto', 'sem_invalido_control.txt', r"'continue' solo puede", True),
    ('Llamada con número correcto de argumentos', 'Válida', 'Validar la firma del método', 'sem_valido_metodos.txt', r"requiere|Ninguna sobrecarga", False),
    ('Llamada con argumentos insuficientes', 'Inválida', 'Reportar cantidad incorrecta', 'sem_invalido_metodos.txt', r"requiere 2 argumentos, pero se proporcionó 1", True),
    ('Argumento de tipo incompatible', 'Inválida', 'Reportar incompatibilidad de tipos', 'sem_invalido_metodos.txt', r"El argumento 1 del método 'sumar'", True),
    ('Método no declarado', 'Inválida', 'Reportar método inexistente', 'sem_invalido_metodos.txt', r"'calcularTotal' no está declarado", True),
    ('Retorno compatible con el método', 'Válida', 'Aceptar el valor retornado', 'sem_valido_metodos.txt', r"retorna '", False),
    ('Retorno incompatible con el método', 'Inválida', 'Reportar error de retorno', 'sem_invalido_metodos.txt', r"'obtenerEdad' retorna 'int'", True),
    ('Atributo existente y accesible', 'Válida', 'Aceptar el acceso', 'sem_valido_clases.txt', r"acceder al miembro|no está declarado en la clase", False),
    ('Atributo inexistente', 'Inválida', 'Reportar miembro no declarado', 'sem_invalido_clases.txt', r"El atributo 'limite' no está declarado en la clase 'Calculadora'", True),
    ('Uso de variable fuera de ámbito', 'Inválida', 'Reportar identificador no visible', 'sem_invalido_ambitos.txt', r"no está disponible en el ámbito actual", True),
    ('Declaración duplicada', 'Inválida, según las reglas del lenguaje', 'Reportar conflicto de nombres', 'sem_invalido_ambitos.txt', r"ya fue declarado previamente en este ámbito", True),
    ('Método sin retorno obligatorio', 'Inválida, cuando corresponda', 'Reportar camino sin retorno válido', 'sem_invalido_retorno_caminos.txt', r"en todos los caminos de ejecución", True),
    ('Acceso a miembro privado', 'Inválida', 'Reportar visibilidad', 'sem_invalido_clases.txt', r"restricciones de visibilidad", True),
    ('Herencia con clase base inexistente', 'Inválida', 'Reportar clase base no declarada', 'sem_invalido_clases.txt', r"La clase base 'Padre' no está declarada", True),
    ('Clase no declarada como tipo', 'Inválida', 'Reportar clase inexistente', 'sem_invalido_clases.txt', r"La clase 'Fantasma' no está declarada", True),
    ('Clase duplicada', 'Inválida', 'Reportar conflicto de nombres', 'sem_invalido_clases.txt', r"'Calculadora' ya fue declarado", True),
    ('Sobrecarga con la misma firma', 'Inválida', 'Reportar firma duplicada', 'sem_invalido_clases.txt', r"misma firma", True),
    ('Return fuera de un método', 'Inválida', 'Reportar contexto inválido', 'sem_invalido_return_fuera.txt', r"'return' no puede utilizarse fuera de un método", True),
    ('Etiquetas case duplicadas', 'Inválida', 'Reportar etiqueta duplicada', 'sem_invalido_control.txt', r"está duplicada dentro del 'switch'", True),
    ('Métodos sobrecargados válidos', 'Válida', 'Elegir la sobrecarga correcta', 'sem_valido_metodos.txt', r"Ninguna sobrecarga|ambigua", False),
    ('Herencia y constructores válidos', 'Válida', 'Aceptar la jerarquía', 'sem_valido_clases.txt', r"constructor|clase base", False),
]

filas = ['| Prueba | Tipo | Resultado esperado | Resultado obtenido | Estado |', '|---|---|---|---|---|']
malos = 0
for nombre, tipo, esperado, arch, rx, debe in CASOS:
    lineas = [l for l in salidas[arch].splitlines() if l.startswith('Error') and re.search(rx, l)]
    if debe:
        obtenido = re.sub(r'^Error semántico en (línea \d+, columna \d+): ', r'\1: ', lineas[0]) if lineas else 'No se reportó el error'
        ok = bool(lineas)
    else:
        obtenido = 'Sin errores de este tipo (aceptado)' if not lineas else lineas[0]
        ok = not lineas
    malos += not ok
    filas.append('| %s | %s | %s | `%s` → %s | %s |' % (nombre, tipo, esperado, arch, obtenido.replace('|', '/'), 'Correcta' if ok else 'Incorrecta'))
(ev / 'tabla_resultados_semantica.md').write_text('\n'.join(filas) + '\n', encoding='utf-8')
print('Salidas generadas:', len(salidas), '| casos de la tabla:', len(CASOS), '| incorrectos:', malos)
