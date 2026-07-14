import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# Crear directorio para almacenar las gráficas
output_dir = 'graficas'
os.makedirs(output_dir, exist_ok=True)

# Cargar los datos consolidados
df = pd.read_csv('resultados_benchmark.csv')
paillier_data = df[df['Metodo'] == 'Paillier'].reset_index(drop=True)
elgamal_data = df[df['Metodo'] == 'ElGamal'].reset_index(drop=True)

sizes = df['Size'].unique()
x = np.arange(len(sizes))
width = 0.35

# ==============================================================================
# Gráfica 1: Tiempo Total Promedio (Escala Logarítmica) con Barras de Error
# ==============================================================================
fig1, ax1 = plt.subplots(figsize=(8, 6))
ax1.bar(x - width/2, paillier_data['TotalMeanMs'], width, yerr=paillier_data['TotalStdMs'], 
        label='Paillier', color='#d3d3d3', edgecolor='black', capsize=5)
ax1.bar(x + width/2, elgamal_data['TotalMeanMs'], width, yerr=elgamal_data['TotalStdMs'], 
        label='ElGamal', color='#696969', edgecolor='black', capsize=5)

ax1.set_yscale('log')
ax1.set_ylabel('Tiempo Total Promedio (ms) [Log]', fontweight='bold')
ax1.set_xlabel('Volumen de Votos', fontweight='bold')
ax1.set_title('Rendimiento Global: Tiempos de Ejecución', fontweight='bold')
ax1.set_xticks(x)
ax1.set_xticklabels([f"{int(s):,}" for s in sizes])
ax1.legend()
ax1.grid(axis='y', linestyle='--', alpha=0.7)
fig1.tight_layout()
plt.savefig(os.path.join(output_dir, '01_tiempo_total_log.png'), dpi=300)
plt.close(fig1)

# ==============================================================================
# Gráfica 2: Desglose de Fases (Cifrado, Suma, Descifrado) - Tiempos Lineales
# Permite ver qué operación es el "cuello de botella" en cada tamaño
# ==============================================================================
fig2, (ax_p, ax_e) = plt.subplots(1, 2, figsize=(14, 6), sharey=False)

# Fases a graficar
fases = ['EncryptMeanMs', 'SumMeanMs', 'DecryptMeanMs']
colores = ['#4A90E2', '#F5A623', '#7ED321']
labels = ['Cifrado', 'Suma Homomórfica', 'Descifrado']

# Apilado para Paillier
bottom_p = np.zeros(len(sizes))
for fase, color, label in zip(fases, colores, labels):
    ax_p.bar(x, paillier_data[fase], width=0.6, label=label, color=color, bottom=bottom_p, edgecolor='black')
    bottom_p += paillier_data[fase]

# Apilado para ElGamal
bottom_e = np.zeros(len(sizes))
for fase, color, label in zip(fases, colores, labels):
    ax_e.bar(x, elgamal_data[fase], width=0.6, label=label, color=color, bottom=bottom_e, edgecolor='black')
    bottom_e += elgamal_data[fase]

ax_p.set_title('Desglose de Operaciones - Paillier', fontweight='bold')
ax_p.set_ylabel('Tiempo Promedio (ms)', fontweight='bold')
ax_p.set_xticks(x)
ax_p.set_xticklabels([f"{int(s):,}" for s in sizes])
ax_p.set_yscale('log')
ax_p.grid(axis='y', linestyle='--', alpha=0.5)

ax_e.set_title('Desglose de Operaciones - ElGamal', fontweight='bold')
ax_e.set_xticks(x)
ax_e.set_xticklabels([f"{int(s):,}" for s in sizes])
ax_e.set_yscale('log')
ax_e.grid(axis='y', linestyle='--', alpha=0.5)

handles, labels = ax_p.get_legend_handles_labels()
fig2.legend(handles, labels, loc='upper center', ncol=3, bbox_to_anchor=(0.5, 1.05))
fig2.tight_layout()
plt.savefig(os.path.join(output_dir, '02_desglose_operaciones.png'), dpi=300, bbox_inches='tight')
plt.close(fig2)

# ==============================================================================
# Gráfica 3: Throughput (Votos procesados por segundo)
# ==============================================================================
fig3, ax3 = plt.subplots(figsize=(8, 6))

ax3.plot(x, paillier_data['ThroughputMeanVps'], marker='o', linestyle='-', color='#d3d3d3', 
         linewidth=2, markersize=8, markeredgecolor='black', label='Paillier')
ax3.plot(x, elgamal_data['ThroughputMeanVps'], marker='s', linestyle='--', color='#696969', 
         linewidth=2, markersize=8, markeredgecolor='black', label='ElGamal')

ax3.set_yscale('log')
ax3.set_ylabel('Throughput (Votos por Segundo) [Log]', fontweight='bold')
ax3.set_xlabel('Volumen de Votos', fontweight='bold')
ax3.set_title('Tasa de Procesamiento (Throughput)', fontweight='bold')
ax3.set_xticks(x)
ax3.set_xticklabels([f"{int(s):,}" for s in sizes])
ax3.legend()
ax3.grid(True, linestyle='--', alpha=0.7)
fig3.tight_layout()
plt.savefig(os.path.join(output_dir, '03_throughput.png'), dpi=300)
plt.close(fig3)

# ==============================================================================
# Gráfica 4: Consumo de CPU Promedio (%)
# CPU: 13th Gen Intel® Core™ i7-13620H, 10 núcleos (6P+4E), 16 hilos, 2.4 GHz base, 4.9 GHz turbo
# ==============================================================================
fig4, ax4 = plt.subplots(figsize=(8, 6))

ax4.bar(x - width/2, paillier_data['CPUPercentMean'], width, label='Paillier', color='#FF9999', edgecolor='black')
ax4.bar(x + width/2, elgamal_data['CPUPercentMean'], width, label='ElGamal', color='#99CCFF', edgecolor='black')

ax4.set_ylabel('Uso Medio de CPU (%)', fontweight='bold')
ax4.set_xlabel('Volumen de Votos', fontweight='bold')
ax4.set_title('Impacto en Hardware: Carga de Procesamiento', fontweight='bold')
ax4.set_xticks(x)
ax4.set_xticklabels([f"{int(s):,}" for s in sizes])
ax4.set_ylim(0, 100)
ax4.legend(loc='upper left')
ax4.grid(axis='y', linestyle='--', alpha=0.7)

# Etiquetas sobre las barras
ax4.bar_label(ax4.containers[0], fmt='%.1f%%', padding=3)
ax4.bar_label(ax4.containers[1], fmt='%.1f%%', padding=3)

fig4.tight_layout()
plt.savefig(os.path.join(output_dir, '04_consumo_cpu.png'), dpi=300)
plt.close(fig4)

print("¡Proceso completado! Las gráficas se han guardado exitosamente en la carpeta 'graficas'.")