<div align="center">
  <a href="#"><img src="../../assets/modules/banner-29-devops-iac-v1.svg" width="100%" alt=""/></a>
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

**¿Cuál es la diferencia entre Terraform y Ansible?**
Terraform es declarativo y se centra en provisionar infraestructura: crea VMs, redes, bases de datos en la nube. Gestiona estado. Ansible se centra en configurar y gestionar sistemas existentes: instala software, copia archivos, gestiona servicios. En la práctica se usan juntos: Terraform crea la VM, Ansible la configura.

---

**¿Qué es el state file en Terraform?**
Un archivo JSON (`terraform.tfstate`) que registra el estado actual de la infraestructura gestionada. Terraform lo usa para saber qué existe, qué ha cambiado y qué debe crear/modificar/destruir. En equipos se almacena en un backend remoto (S3, GCS, Terraform Cloud) con locking para evitar escrituras concurrentes.

---

**¿Qué significa idempotencia en Ansible?**
Que ejecutar el mismo playbook múltiples veces produce el mismo resultado final: si Nginx ya está instalado, no lo reinstala. Los módulos de Ansible verifican el estado actual antes de actuar. Esto hace que los playbooks sean seguros de re-ejecutar para corregir desviaciones o aplicar en nuevos servidores.

---

**¿Cuándo usarías Terraform vs Ansible?**
Terraform para gestionar el ciclo de vida de infraestructura cloud: crear, modificar y destruir recursos (VMs, redes, load balancers, RDS). Ansible para configurar esos recursos una vez creados: instalar dependencias, desplegar aplicaciones, gestionar configuración. Terraform = infraestructura; Ansible = configuración.

---

**¿Qué hace `terraform plan`?**
Muestra los cambios que Terraform aplicaría sin ejecutarlos realmente: qué recursos se crearían, modificarían o destruirían. Es el equivalente a un "dry run". Fundamental en pipelines CI/CD para revisar cambios antes de aplicar. El plan puede guardarse con `terraform plan -out=plan.tfplan` y aplicarse exactamente con `terraform apply plan.tfplan`.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
