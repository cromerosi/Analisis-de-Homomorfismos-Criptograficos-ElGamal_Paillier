import os
import random

# =============================================================================
# CONFIGURACIÓN DEL BENCHMARK - PARÁMETROS MODIFICABLES
# =============================================================================

# Semilla fija para reproducibilidad científica
# ¡NO CAMBIAR! Garantiza que todos los experimentos usen los mismos datos exactos
SEMILLA = 72 #Unico numero primo de Sheldon Cooper

# Tamaños de votación a evaluar
TAMANOS_VOTACION = [50, 500, 5000, 50000, 500000, 5000000, 50000000]

# Número de archivos por cada tamaño (para análisis estadístico: media, desviación estándar)
ARCHIVOS_POR_TAMANO = 25

# Ruta de salida - Fuera de src/main/resources para no inflar el JAR
DIRECTORIO_SALIDA = "benchmarks_data"

# -----------------------------------------------------------------------------
# CONFIGURACIÓN DE CANDIDATOS Y DISTRIBUCIÓN DE VOTOS
# -----------------------------------------------------------------------------

# IDs de candidatos (formato texto para escritura eficiente en archivo)
# 0 = Candidato A, 1 = Candidato B, 2 = Candidato C
CANDIDATOS = ["0", "1", "2"]

# Probabilidades ponderadas: [Candidato A: 45%, Candidato B: 40%, Candidato C: 15%]
# Distribución realista NO uniforme - crucial para evaluar ElGamal (log discreto)
PROBABILIDADES = [0.45, 0.40, 0.15]

# Validación básica de configuración
assert len(CANDIDATOS) == len(PROBABILIDADES), "Debe haber una probabilidad por cada candidato"
assert abs(sum(PROBABILIDADES) - 1.0) < 0.001, "Las probabilidades deben sumar 1.0"

# =============================================================================
# FUNCIÓN PRINCIPAL DE GENERACIÓN
# =============================================================================

def generar_archivos():
    """Genera todos los archivos de votación para el benchmark criptográfico."""
    
    # Fijar semilla para reproducibilidad
    random.seed(SEMILLA)
    
    # Crear directorio de salida si no existe
    os.makedirs(DIRECTORIO_SALIDA, exist_ok=True)
    
    # Estadísticas globales para reporte final
    total_archivos = len(TAMANOS_VOTACION) * ARCHIVOS_POR_TAMANO
    archivos_generados = 0
    
    print("=" * 70)
    print("GENERADOR DE DATOS PARA BENCHMARK CRIPTOGRÁFICO")
    print("=" * 70)
    print(f"Semilla fija: {SEMILLA}")
    print(f"Distribución: A={PROBABILIDADES[0]:.0%}, B={PROBABILIDADES[1]:.0%}, C={PROBABILIDADES[2]:.0%}")
    print(f"Archivos por tamaño: {ARCHIVOS_POR_TAMANO}")
    print(f"Total archivos a generar: {total_archivos}")
    print("=" * 70)
    
    for tamano in TAMANOS_VOTACION:
        print(f"\n[+] Generando {ARCHIVOS_POR_TAMANO} archivos de {tamano:,} votos...")
        
        for i in range(1, ARCHIVOS_POR_TAMANO + 1):
            nombre_archivo = f"votos_{tamano}_{i}.txt"
            ruta_completa = os.path.join(DIRECTORIO_SALIDA, nombre_archivo)
            
            with open(ruta_completa, 'w') as archivo:
                votos_restantes = tamano
                bloque_size = 1_000_000  # Lotes de 1M para no saturar RAM
                
                while votos_restantes > 0:
                    votos_a_generar = min(bloque_size, votos_restantes)
                    # random.choices es óptimo: implementado en C, muy rápido
                    votos = random.choices(CANDIDATOS, weights=PROBABILIDADES, k=votos_a_generar)
                    archivo.write('\n'.join(votos) + '\n')
                    votos_restantes -= votos_a_generar
            
            archivos_generados += 1
            # Barra de progreso simple
            if i % 5 == 0 or i == ARCHIVOS_POR_TAMANO:
                print(f"    [{i}/{ARCHIVOS_POR_TAMANO}] Completado para {tamano:,} votos")
    
    print("\n" + "=" * 70)
    print(f"✓ GENERACIÓN COMPLETADA: {archivos_generados} archivos creados")
    print(f"✓ Ubicación: {os.path.abspath(DIRECTORIO_SALIDA)}")
    print("=" * 70)

# =============================================================================
# PUNTO DE ENTRADA
# =============================================================================

if __name__ == "__main__":
    generar_archivos()