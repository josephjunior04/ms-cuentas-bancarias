FROM openjdk:17
ADD target/ms-cuentas-bancarias-0.0.1-SNAPSHOT.jar ms-cuentas-bancarias-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "ms-cuentas-bancarias-0.0.1-SNAPSHOT.jar"]