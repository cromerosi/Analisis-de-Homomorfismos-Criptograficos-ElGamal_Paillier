#!/usr/bin/env bash
set -euo pipefail

# Compila y ejecuta la clase Demo
mvn -q -DskipTests package
java -cp target/classes com.cromerosi.homomorfismos.tools.Demo
