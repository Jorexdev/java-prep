# ============================================================
# Etapa 1: Construcción (Build)
# ============================================================

# Imagen base con Maven + JDK 21 para compilar la app
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias (mejor caché)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente y compilamos el proyecto
COPY src ./src
RUN mvn clean package -DskipTests

# ============================================================
# Etapa 2: Ejecución (Runtime)
# ============================================================

# Imagen base ligera solo con JRE
FROM eclipse-temurin:21-jre

# Directorio de trabajo de la app
WORKDIR /app

# Copiamos solo el JAR construido desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Variable de entorno opcional (puedes modificarla en ejecución)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de arranque de la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
