#!/usr/bin/env pwsh

# Ejecuta los tests de benchmark (PowerShell)
mvn -Dtest=com.cromerosi.homomorfismos.benchmark.BenchmarkTest test
