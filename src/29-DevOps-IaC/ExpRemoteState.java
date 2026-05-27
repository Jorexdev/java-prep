import java.util.*;
import java.util.concurrent.locks.*;

// Simula el backend de estado remoto de Terraform: almacenamiento, locking y data sources.
// En Terraform real: backend "s3" { bucket="tf-state" dynamodb_table="tf-locks" }
public class ExpRemoteState {

    // ── Modelo: State Backend con locking ─────────────────────────────────────
    static class StateBackend {
        private final Map<String, Map<String, String>> states = new HashMap<>(); // workspace → state
        private final Map<String, String> locks = new HashMap<>();                // workspace → owner
        private final ReentrantLock mutex = new ReentrantLock();

        // Adquirir lock (DynamoDB en AWS, Table Storage en Azure, GCS object lock en GCP)
        boolean acquireLock(String workspace, String owner) {
            mutex.lock();
            try {
                if (locks.containsKey(workspace)) {
                    System.out.println("  LOCK DENIED: " + workspace + " bloqueado por " + locks.get(workspace));
                    return false;
                }
                locks.put(workspace, owner);
                System.out.println("  LOCK ACQUIRED: " + workspace + " por " + owner);
                return true;
            } finally { mutex.unlock(); }
        }

        void releaseLock(String workspace, String owner) {
            mutex.lock();
            try {
                if (owner.equals(locks.get(workspace))) {
                    locks.remove(workspace);
                    System.out.println("  LOCK RELEASED: " + workspace);
                } else {
                    System.out.println("  WARN: " + owner + " no puede liberar lock de " + workspace);
                }
            } finally { mutex.unlock(); }
        }

        void writeState(String workspace, Map<String, String> state) {
            states.put(workspace, new HashMap<>(state));
            System.out.println("  STATE WRITTEN: " + workspace + " → " + state);
        }

        Map<String, String> readState(String workspace) {
            return states.getOrDefault(workspace, Map.of());
        }
    }

    // ── 1. FLUJO DE terraform apply CON REMOTE STATE ─────────────────────────
    // 1. `terraform init`    → descarga providers, configura backend
    // 2. `terraform plan`    → lee state remoto, calcula diff (no adquiere lock)
    // 3. `terraform apply`   → adquiere lock → aplica cambios → escribe state → libera lock
    static void terraformApplyFlow(StateBackend backend) {
        System.out.println("── 1. Flujo terraform apply con remote state ──");

        String workspace = "prod";
        String engineer  = "jorge";

        // Estado inicial (infraestructura existente)
        backend.writeState(workspace, Map.of(
            "aws_instance.api",  "i-abc123 (t3.medium)",
            "aws_rds.db",        "db-xyz (postgres 14)"
        ));

        System.out.println("\n  [plan] Leyendo estado remoto...");
        Map<String, String> currentState = backend.readState(workspace);
        System.out.println("  Estado actual: " + currentState);

        System.out.println("\n  [apply] Adquiriendo lock...");
        if (backend.acquireLock(workspace, engineer)) {
            try {
                // Aplicar cambios
                Map<String, String> newState = new HashMap<>(currentState);
                newState.put("aws_instance.api", "i-abc123 (t3.large)"); // cambio de tipo
                newState.put("aws_s3.backups",   "s3://backups-prod");   // recurso nuevo
                backend.writeState(workspace, newState);
                System.out.println("  [apply] Cambios aplicados");
            } finally {
                backend.releaseLock(workspace, engineer);
            }
        }
    }

    // ── 2. LOCKING — prevenir applies concurrentes ────────────────────────────
    // Problema sin locking: dos engineers hacen apply simultáneamente →
    // el state se corrompe porque ambos leen el mismo estado inicial.
    // Con locking: el segundo apply falla hasta que el primero termine.
    static void concurrentApplyDemo(StateBackend backend) {
        System.out.println("\n── 2. Locking — prevenir applies concurrentes ──");

        String workspace = "staging";
        backend.writeState(workspace, Map.of("aws_instance.worker", "i-def456"));

        // Primer engineer adquiere lock
        boolean lock1 = backend.acquireLock(workspace, "engineer-A");

        // Segundo engineer intenta apply mientras el primero trabaja
        boolean lock2 = backend.acquireLock(workspace, "engineer-B"); // debe fallar

        System.out.println("  engineer-A obtuvo lock: " + lock1);
        System.out.println("  engineer-B obtuvo lock: " + lock2 + " (correcto: debe ser false)");

        if (lock1) {
            backend.writeState(workspace, Map.of("aws_instance.worker", "i-def456 (actualizado)"));
            backend.releaseLock(workspace, "engineer-A");
        }

        // Ahora engineer-B puede reintentar
        boolean lock2retry = backend.acquireLock(workspace, "engineer-B");
        System.out.println("  engineer-B reintento: " + lock2retry + " (ahora sí)");
        if (lock2retry) backend.releaseLock(workspace, "engineer-B");
    }

    // ── 3. WORKSPACES — environments en el mismo backend ─────────────────────
    // terraform workspace new staging
    // terraform workspace select prod
    //
    // Cada workspace tiene su propio state file en el backend.
    // Ventaja: mismos módulos, distinta infraestructura por environment.
    // Limitación: workspaces no son sustituto de cuentas/proyectos distintos
    //             (los recursos comparten el mismo provider account).
    static void workspaces(StateBackend backend) {
        System.out.println("\n── 3. Workspaces ──");

        String[] envs = { "dev", "staging", "prod" };
        for (String env : envs) {
            backend.writeState(env, Map.of(
                "aws_instance.api", "i-" + env.substring(0,1) + "00" + Math.abs(env.hashCode() % 999),
                "environment",      env
            ));
        }

        System.out.println("  Estados por workspace:");
        for (String env : envs) {
            System.out.println("  [" + env + "] " + backend.readState(env));
        }
        System.out.println();
        System.out.println("  Cada workspace = state file independiente en s3://bucket/env/terraform.tfstate");
    }

    // ── 4. terraform_remote_state DATA SOURCE ─────────────────────────────────
    // Permite que un módulo lea el output de otro módulo.
    // Ej: módulo de networking exporta vpc_id → módulo de compute lo consume.
    //
    // data "terraform_remote_state" "network" {
    //   backend = "s3"
    //   config  = { bucket = "tf-state", key = "network/terraform.tfstate" }
    // }
    // resource "aws_instance" "api" {
    //   subnet_id = data.terraform_remote_state.network.outputs.public_subnet_id
    // }
    static void remoteStateDataSource(StateBackend backend) {
        System.out.println("── 4. terraform_remote_state data source ──");

        // Módulo de red escribe sus outputs al state
        backend.writeState("network", Map.of(
            "output.vpc_id",           "vpc-0a1b2c3d",
            "output.public_subnet_id", "subnet-11223344",
            "output.private_subnet_id","subnet-55667788"
        ));

        // Módulo de compute lee los outputs del módulo de red
        Map<String, String> networkState = backend.readState("network");
        String vpcId    = networkState.get("output.vpc_id");
        String subnetId = networkState.get("output.public_subnet_id");

        System.out.println("  Módulo compute lee de network state:");
        System.out.println("  vpc_id           = " + vpcId);
        System.out.println("  public_subnet_id = " + subnetId);
        System.out.println();
        System.out.println("  Ventaja: desacopla módulos sin pasar outputs como variables.");
        System.out.println("  Riesgo: dependencia implícita entre stacks — romper el output del producer rompe el consumer.");
    }

    public static void main(String[] args) {
        StateBackend backend = new StateBackend();
        terraformApplyFlow(backend);
        concurrentApplyDemo(backend);
        workspaces(backend);
        remoteStateDataSource(backend);
    }
}
