<div align="center">
  <a href="#"><img src="../../assets/modules/banner-30-devops-pipelines-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**CI/CD** (Continuous Integration / Continuous Delivery) es la práctica de automatizar la integración, testing y despliegue del código. Cada cambio en el repositorio desencadena un pipeline que verifica y despliega la aplicación.

- **CI**: integración continua — compilar, testear, analizar el código automáticamente en cada push.
- **CD**: entrega/despliegue continuo — desplegar automáticamente a staging o producción tras CI exitosa.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**GitHub Actions:**
```yaml
# .github/workflows/ci.yml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21' }
      - run: mvn clean verify
      - run: docker build -t miapp:${{ github.sha }} .
```

**Jenkins (Declarative Pipeline):**
```groovy
// Jenkinsfile
pipeline {
    agent any
    stages {
        stage('Build') { steps { sh 'mvn clean package' } }
        stage('Test')  { steps { sh 'mvn test' } }
        stage('Deploy') { steps { sh 'kubectl apply -f k8s/' } }
    }
}
```

**GitLab CI:**
```yaml
# .gitlab-ci.yml
stages: [build, test, deploy]
build-job:
  stage: build
  script: mvn clean package
  artifacts:
    paths: [target/*.jar]
test-job:
  stage: test
  script: mvn test
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Detección temprana de errores: cada commit se verifica automáticamente.
- Despliegues frecuentes y seguros: proceso automatizado y reproducible.
- Trazabilidad: cada despliegue está vinculado a un commit específico.
- Rollback rápido: pipelines con aprobación manual y rollback automatizado.

Este módulo es solo teoría — los pipelines se configuran en el repositorio del proyecto.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
