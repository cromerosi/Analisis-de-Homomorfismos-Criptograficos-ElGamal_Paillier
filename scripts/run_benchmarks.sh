#!/usr/bin/env bash
set -euo pipefail

# Ejecuta los tests de benchmark
mvn -Dtest=com.cromerosi.homomorfismos.benchmark.BenchmarkTest test
