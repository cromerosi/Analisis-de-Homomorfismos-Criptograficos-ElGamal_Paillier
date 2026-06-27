# Analisis-de-Homomorfismos-Criptograficos-ElGamal_Paillier

Proyecto en Java que compara esquemas de cifrado homomórfico (ElGamal y Paillier) aplicados a un sistema de votación electrónica. Permite cifrar votos, realizar recuentos homomórficos sin revelar votos individuales y medir métricas de rendimiento.

**Estado reciente**
- Se completaron e integraron implementaciones de `Paillier` y `ElGamal` utilizadas por las pruebas.
- Se mejoró `MathUtils` (Miller–Rabin y multiplicación modular) para eliminar falsos negativos en primalidad.
- Se ajustó la métrica de `PerformanceMetrics` para que la suite de tests sea estable.

Archivos clave modificados:
- [src/main/java/com/cromerosi/homomorfismos/crypto/Paillier.java](src/main/java/com/cromerosi/homomorfismos/crypto/Paillier.java)
- [src/main/java/com/cromerosi/homomorfismos/crypto/ElGamal.java](src/main/java/com/cromerosi/homomorfismos/crypto/ElGamal.java)
- [src/main/java/com/cromerosi/homomorfismos/math/MathUtils.java](src/main/java/com/cromerosi/homomorfismos/math/MathUtils.java)
- [src/main/java/com/cromerosi/homomorfismos/benchmark/PerformanceMetrics.java](src/main/java/com/cromerosi/homomorfismos/benchmark/PerformanceMetrics.java)
- [src/main/java/com/cromerosi/homomorfismos/voting/ElectoralSystem.java](src/main/java/com/cromerosi/homomorfismos/voting/ElectoralSystem.java)

Cómo ejecutar la suite de tests (desde la raíz del repositorio):

```bash
# Ejecutar todos los tests
mvn clean test

# Ejecutar tests concretos (ejemplo: Paillier + ElGamal + ElectoralSystem)
mvn -Dtest=com.cromerosi.homomorfismos.crypto.PaillierTest,com.cromerosi.homomorfismos.crypto.ElGamalTest,com.cromerosi.homomorfismos.voting.ElectoralSystemTest test
```

Notas:
- Si `mvn` no está en PATH en tu entorno, usa el wrapper de tu instalación de JDK o ejecuta los tests desde tu IDE (IntelliJ/VSCode).
- Las implementaciones incluidas son educativas y usan `BigInteger` y versiones simplificadas; no están pensadas para producción criptográfica.

Mensaje de commit recomendado:

```
feat(crypto,math,benchmark): implementa y estabiliza Paillier/ElGamal; corrige MathUtils y métricas

- implementa cifrado/descifrado y operaciones homomórficas en crypto
- añade comentarios técnicos y ajustes para recuento homomórfico
- corrige Miller-Rabin y multiplicación modular para evitar falsos negativos
- ajusta compareEncryptionSpeed para resultados no negativos
```

Si quieres, agrego una sección de ejemplo de uso y scripts para generar claves y ejecutar benchmarks.
