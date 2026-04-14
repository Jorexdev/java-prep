package devops.iac;

public class Intro {
/*
    INFRAESTRUCTURA COMO CÓDIGO (IaC) — Terraform y Ansible

    ► ¿Qué es IaC?
      Gestionar y provisionar infraestructura (servidores, redes, bases de datos,
      balanceadores) mediante archivos de configuración declarativos, en lugar de
      configurarlos manualmente.

      Ventajas:
        - Reproducibilidad: el mismo código genera la misma infraestructura.
        - Control de versiones: la infra vive en Git como el código.
        - Automatización: elimina errores humanos en configuración.
        - Documentación viva: el código describe el estado real.

    ── TERRAFORM ──────────────────────────────────────────────────────────────

    ► ¿Qué es?
      Herramienta de IaC de HashiCorp. Define infraestructura de forma declarativa
      usando HCL (HashiCorp Configuration Language).

      Soporta múltiples proveedores: AWS, GCP, Azure, Kubernetes, etc.

    ► Conceptos clave

      Provider  → plugin que habla con el API del proveedor (AWS, GCP...).
      Resource  → elemento de infraestructura (VM, VPC, base de datos...).
      State     → archivo terraform.tfstate que guarda el estado actual.
      Module    → grupo reutilizable de recursos.
      Plan      → previsualización de cambios antes de aplicarlos.

    ► Flujo de trabajo

      1. terraform init       → descarga providers y módulos.
      2. terraform plan       → muestra qué va a crear/modificar/destruir.
      3. terraform apply      → aplica los cambios.
      4. terraform destroy    → elimina toda la infraestructura.

    ► Ejemplo básico (instancia EC2 en AWS)

      # main.tf
      terraform {
        required_providers {
          aws = {
            source  = "hashicorp/aws"
            version = "~> 5.0"
          }
        }
      }

      provider "aws" {
        region = "eu-west-1"
      }

      resource "aws_instance" "web" {
        ami           = "ami-0c02fb55956c7d316"
        instance_type = "t3.micro"

        tags = {
          Name = "mi-servidor"
        }
      }

      output "public_ip" {
        value = aws_instance.web.public_ip
      }

    ► Variables y ficheros

      # variables.tf
      variable "region" {
        description = "AWS region"
        type        = string
        default     = "eu-west-1"
      }

      # terraform.tfvars (valores concretos, no se sube a Git)
      region = "us-east-1"

    ► Estado remoto (buena práctica en equipo)
      El estado se guarda remotamente para evitar conflictos:

      terraform {
        backend "s3" {
          bucket = "mi-terraform-state"
          key    = "prod/terraform.tfstate"
          region = "eu-west-1"
        }
      }

    ── ANSIBLE ────────────────────────────────────────────────────────────────

    ► ¿Qué es?
      Herramienta de automatización de configuración y aprovisionamiento.
      Se basa en SSH (agentless, sin instalar nada en los servidores destino).
      Usa YAML para definir tareas (playbooks).

      Diferencia clave con Terraform:
        Terraform   → provisiona infraestructura (crea/destruye recursos).
        Ansible     → configura la infraestructura ya existente (instala paquetes,
                       copia ficheros, gestiona servicios).

      En la práctica se usan juntos: Terraform crea la VM, Ansible la configura.

    ► Conceptos clave

      Inventory  → lista de servidores donde Ansible ejecuta las tareas.
      Playbook   → archivo YAML con las tareas a ejecutar.
      Task       → acción individual (instalar paquete, copiar fichero...).
      Module     → unidad de trabajo reutilizable (yum, apt, copy, service...).
      Role       → grupo de playbooks/tareas organizadas y reutilizables.
      Handler    → tarea que se ejecuta solo si otra tarea la notifica (ej: recargar nginx).

    ► Ejemplo de inventory

      # hosts.ini
      [webservers]
      192.168.1.10
      192.168.1.11

      [dbservers]
      192.168.1.20

    ► Ejemplo de Playbook

      # playbook.yml
      - name: Configurar servidor web
        hosts: webservers
        become: true           # equivalente a sudo

        tasks:
          - name: Instalar Java 21
            apt:
              name: openjdk-21-jdk
              state: present
              update_cache: true

          - name: Copiar JAR de la aplicación
            copy:
              src: target/app.jar
              dest: /opt/app/app.jar
              mode: '0755'

          - name: Crear servicio systemd
            template:
              src: app.service.j2
              dest: /etc/systemd/system/app.service
            notify: Recargar systemd

          - name: Iniciar aplicación
            service:
              name: app
              state: started
              enabled: true

        handlers:
          - name: Recargar systemd
            command: systemctl daemon-reload

    ► Comandos esenciales Ansible

      ansible all -i hosts.ini -m ping                         → verifica conectividad
      ansible-playbook -i hosts.ini playbook.yml               → ejecuta el playbook
      ansible-playbook -i hosts.ini playbook.yml --check       → dry run (sin cambios)
      ansible-playbook -i hosts.ini playbook.yml --diff        → muestra diferencias
      ansible-vault encrypt secrets.yml                        → cifra fichero sensible

    ── COMPARATIVA ────────────────────────────────────────────────────────────

      Herramienta | ¿Qué gestiona?         | Lenguaje | Agente
      ------------|------------------------|----------|--------
      Terraform   | Infraestructura (IaC)  | HCL      | No
      Ansible     | Configuración (CaC)    | YAML     | No (SSH)

    ► Preguntas típicas de entrevista
      - ¿Qué diferencia hay entre Terraform y Ansible?
      - ¿Qué es el estado en Terraform y por qué importa?
      - ¿Cómo evitarías conflictos de estado en un equipo con Terraform?
      - ¿Qué es un playbook idempotente?
      - ¿Cómo gestionas secretos en Terraform? (Variables de entorno, Vault)
      - ¿Qué ventaja tiene IaC frente a configuración manual?
*/
}
