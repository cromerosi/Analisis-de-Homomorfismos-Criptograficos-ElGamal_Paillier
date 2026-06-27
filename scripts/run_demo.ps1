Param(
    [int]$KeySize = 512
)

# Compila y ejecuta la clase Demo (PowerShell)
mvn -q -DskipTests package
java -cp target/classes com.cromerosi.homomorfismos.tools.Demo
