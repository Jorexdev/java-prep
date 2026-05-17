<div align="center">
  <a href="#"><img src="../../assets/modules/banner-27-devops-docker-v1.svg" width="100%" alt=""/></a>
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

**¿Cuál es la diferencia entre una imagen y un contenedor?**
Una imagen es una plantilla inmutable (read-only) que define el sistema de archivos y la configuración. Un contenedor es una instancia ejecutable de esa imagen — tiene estado, puede modificar archivos (en su capa writable) y ejecuta procesos. Una imagen puede generar múltiples contenedores.

---

**¿Qué hace cada instrucción de un Dockerfile (FROM, COPY, RUN, CMD)?**
`FROM` define la imagen base. `COPY` copia archivos del host al contenedor. `RUN` ejecuta un comando durante la construcción de la imagen (ej. `apt install`). `CMD` define el comando por defecto al arrancar el contenedor. Cada instrucción crea una capa de imagen que se cachea.

---

**¿Qué diferencia hay entre CMD y ENTRYPOINT?**
`ENTRYPOINT` define el ejecutable fijo del contenedor (no se sobreescribe fácilmente). `CMD` define argumentos por defecto que pueden sobreescribirse al ejecutar `docker run`. Combinados: `ENTRYPOINT ["java"]` + `CMD ["-jar", "app.jar"]` permite sobrescribir solo los argumentos.

---

**¿Cuándo usarías volúmenes en Docker?**
Cuando los datos deben persistir más allá del ciclo de vida del contenedor: bases de datos, archivos subidos por usuarios, logs. Sin volumen, al eliminar el contenedor se pierden todos los datos escritos. También se usan para compartir archivos entre el host y el contenedor en desarrollo.

---

**¿Qué es docker-compose?**
Una herramienta para definir y ejecutar aplicaciones multi-contenedor mediante un archivo YAML (`docker-compose.yml`). Permite levantar toda la infraestructura local (app + DB + cache + message broker) con `docker compose up`. Ideal para desarrollo local y CI, no para producción (donde se usa Kubernetes).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
