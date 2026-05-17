<div align="center">
  <a href="#"><img src="../../assets/modules/banner-30-devops-pipelines-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Qué diferencia hay entre CI y CD?**
CI (Continuous Integration): cada commit se integra automáticamente compilando y ejecutando tests — detecta problemas rápido. CD puede ser Continuous Delivery (el artefacto está siempre listo para desplegar, el despliegue puede requerir aprobación manual) o Continuous Deployment (cada commit que pasa CI se despliega automáticamente a producción).

---

**¿Cómo defines un workflow en GitHub Actions?**
Con un archivo YAML en `.github/workflows/nombre.yml`. Define: `on` (eventos que lo disparan: push, pull_request, schedule...), `jobs` (trabajos paralelos o secuenciales), cada job con `runs-on` (tipo de runner) y `steps` (acciones o comandos secuenciales).

---

**¿Qué diferencia hay entre un job y un step en GitHub Actions?**
Los jobs son unidades de trabajo que pueden ejecutarse en paralelo (por defecto) o en secuencia (con `needs`). Cada job corre en su propio runner (VM limpia). Los steps son acciones secuenciales dentro de un job que comparten el mismo runner y sistema de archivos.

---

**¿Qué es un Jenkinsfile?**
Un archivo de texto en el repositorio que define el pipeline de Jenkins como código. Existen dos sintaxis: Declarativa (estructura fija con `pipeline { stages { ... } }` — recomendada) y Scripted (Groovy puro — más flexible pero más compleja). Tenerlo en el repositorio permite versionar y revisar los cambios del pipeline.

---

**¿Cómo compartes artefactos entre jobs en GitLab CI?**
Con `artifacts`: defines qué archivos guardar al final de un job (`paths: [target/*.jar]`) y los jobs posteriores los reciben automáticamente. Puedes configurar `expire_in` para limpiar artefactos antiguos. Para artefactos grandes o compartidos entre pipelines, se usa un registry (Docker, Maven Nexus).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
