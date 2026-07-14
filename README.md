# 📊 Análisis comparativo: ElGamal vs Paillier

![build](https://img.shields.io/badge/BUILD-MANUAL-blue)
![java](https://img.shields.io/badge/Java-21-blueviolet)
![tests](https://img.shields.io/badge/TEST-PASSING-brightgreen)
![license](https://img.shields.io/badge/MATEMATICAS-DISCRETAS-lightgrey)

Proyecto en Java que compara dos esquemas homomórficos (ElGamal y Paillier) aplicados a un sistema de votación electrónica. El objetivo es educativo y experimental: cifrar votos, acumular recuentos homomórficos y medir rendimiento.

## ✅ Qué contiene este repositorio
- Implementaciones de `Paillier` y `ElGamal` (BigInteger, orientadas a estudio/ensayo).
- Utilidades matemáticas (`MathUtils`) para operaciones con `BigInteger`.
- Un runner de benchmarks: `com.cromerosi.homomorfismos.App` que procesa archivos de votos y produce métricas y CSVs.

## 🎯 Resultados que genera el benchmark
- `resultados_parciales.csv` — se va escribiendo por archivo procesado (útil para interrupciones).
- `resultados_benchmark.csv` — resumen final por tamaño y método.
- `informe_benchmark.txt` — resumen ejecutivo con speedups y observaciones.

## 🛠 Requisitos
- JDK 17+ (el proyecto se probó con JDK 21 en este equipo).
- Opcional: Maven (`mvn`) para compilar y ejecutar tests desde terminal.

Si no tienes `mvn` en PATH, puedes compilar con tu IDE o ejecutar la clase `App` directamente apuntando al classpath compilado.

## ▶️ Cómo ejecutar (rápido)

Compilar con Maven (si lo tienes):

```bash
mvn -DskipTests package
```

Ejecutar `App` (ejemplo):

```bash
java -cp target/classes com.cromerosi.homomorfismos.App
```

Ejecutar los tests con Maven:

```bash
mvn test
```

Si tu entorno no tiene `mvn`, usa tu IDE para ejecutar las pruebas o compilar.

## 🧪 Formato esperado de los archivos de votos
- Carpeta por tamaño recomendada: `benchmarks_data/<size>/` con archivos `.txt`.
- Cada línea debe contener `0`, `1` o `2` (opciones de voto). Ejemplo:

```
0
1
2
1
```


## 📂 Estructura principal
Árbol simplificado (raíz del repositorio):

```text
.
├── .vscode/
├── benchmarks_data/       # datos de entrada para el benchmark (no incluidos)
├── docs/
├── scripts/               # utilidades para demo/benchmarks
├── src/
│   ├── main/
│   │   └── java/com/cromerosi/homomorfismos/
│   │       ├── benchmark/
│   │       ├── crypto/
│   │       ├── math/
│   │       ├── tools/
│   │       └── voting/
│   └── test/
└── README.md

```
