"""Ejecutar después de compilar. No requiere paquetes de terceros.
Verifica: (1) código de salida esperado (0 válido / 1 inválido) de cada prueba y
(2) para las pruebas de verificación de tipos, el número EXACTO de errores semánticos."""
from pathlib import Path
import os, re, subprocess, sys
p = Path(__file__).resolve().parent
fallos = 0

# Pruebas de verificación de tipos: nombre -> errores semánticos esperados
ERRORES_TIPOS = {
    'tipos_valido_basico.txt': 0,
    'tipos_valido_promocion.txt': 0,
    'tipos_invalido_basico.txt': 5,          # los 5 errores del enunciado
    'tipos_invalido_declaraciones.txt': 10,
    'tipos_invalido_operadores.txt': 19,
    'tipos_invalido_condiciones.txt': 10,
    'tipos_invalido_constantes.txt': 6,
    'tipos_invalido_retorno.txt': 6,
    'tipos_invalido_cascada.txt': 3,
}

# Pruebas de estructuras de control, métodos, clases y ámbitos: nombre -> errores semánticos esperados
ERRORES_SEM = {
    'sem_valido_calculadora.txt': 0,
    'sem_valido_control.txt': 0,
    'sem_valido_metodos.txt': 0,
    'sem_valido_clases.txt': 0,
    'sem_valido_ambitos.txt': 0,
    'sem_invalido_calculadora_errores.txt': 8,   # los 7 errores del enunciado + el retorno de obtenerEdad
    'sem_invalido_control.txt': 11,
    'sem_invalido_metodos.txt': 11,
    'sem_invalido_retorno_caminos.txt': 6,
    'sem_invalido_return_fuera.txt': 2,
    'sem_invalido_clases.txt': 23,
    'sem_invalido_ambitos.txt': 13,
    'sem_invalido_multiple.txt': 14,
}

archivos = sorted(list(p.glob('prueba*.txt')) + list(p.glob('act9*.txt'))
                  + list((p/'pruebas').glob('*.txt')) + list((p/'pruebas_tipos').glob('*.txt')) + list((p/'pruebas_semantica').glob('*.txt')))
for f in archivos:
    esperado = 1 if any(x in f.name for x in ['invalido','incompleta','duplicado','no_declarado','prueba3','prueba6','prueba8','prueba10','prueba11']) else 0
    r = subprocess.run(['java','-cp','.'+os.pathsep+'java-cup-11b-runtime.jar','Main',str(f.relative_to(p))], cwd=p, encoding='utf-8', stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=15)
    ok = r.returncode == esperado
    if f.name == 'prueba7_control_valido.txt':
        ok = ok and all('Regla reconocida: '+x in r.stdout for x in ['seleccion_simple','seleccion_doble','seleccion_multiple','ciclo_mientras','ciclo_hacer_mientras','ciclo_para','condicion','expresion_logica'])
    if f.name == 'prueba9_anidamiento.txt':
        ok = ok and 'Regla reconocida: estructura_anidada' in r.stdout
    if f.name == 'prueba10_recuperacion.txt':
        ok = ok and all(x in r.stdout for x in ['Recuperación:', 'Regla reconocida: ciclo_mientras', 'analisis completo'])
        ok = ok and 'Regla reconocida: seleccion_simple' not in r.stdout
    if f.name == 'prueba11_combinado.txt':
        ok = ok and 'analisis completo' in r.stdout
    if f.name in ERRORES_TIPOS:
        m = re.search(r'errores semanticos: (\d+)', r.stdout)
        ok = ok and m is not None and int(m.group(1)) == ERRORES_TIPOS[f.name]
        ok = ok and 'REPORTE DEL ANÁLISIS SEMÁNTICO DE TIPOS' in r.stdout
    if f.name in ERRORES_SEM:
        m = re.search(r'errores semanticos: (\d+)', r.stdout)
        ok = ok and m is not None and int(m.group(1)) == ERRORES_SEM[f.name]
        ok = ok and 'REPORTE DEL ANÁLISIS SEMÁNTICO: ESTRUCTURAS DE CONTROL, MÉTODOS, CLASES Y ÁMBITOS' in r.stdout
        ok = ok and 'analisis completo' in r.stdout
    fallos += not ok
    print(('OK ' if ok else 'FALLO ') + str(f.relative_to(p)))
print('Archivos:', len(archivos), '| Fallos:', fallos)
sys.exit(1 if fallos else 0)
