import java.util.*;

// Simula el modelo de gestión de recursos de Kubernetes: requests, limits y HPA.
// En K8s real: kubectl apply -f deployment.yaml con resources: requests/limits.
public class ExpResourceManagement {

    // ── Modelo de datos ───────────────────────────────────────────────────────
    record Resources(double cpuCores, int memoryMB) {
        @Override public String toString() {
            return String.format("CPU=%.2f cores, MEM=%dMB", cpuCores, memoryMB);
        }
    }

    static class Pod {
        final String name;
        final Resources requests; // mínimo garantizado → para el scheduler
        final Resources limits;   // máximo permitido → para cgroups del nodo
        double currentCpu;        // uso actual simulado

        Pod(String name, Resources requests, Resources limits) {
            this.name = name; this.requests = requests; this.limits = limits;
        }
        @Override public String toString() { return name; }
    }

    static class Node {
        final String name;
        final Resources capacity;
        final List<Pod> pods = new ArrayList<>();

        Node(String name, Resources capacity) { this.name = name; this.capacity = capacity; }

        // Recursos comprometidos = suma de requests de pods schedulados
        double committedCpu() { return pods.stream().mapToDouble(p -> p.requests.cpuCores()).sum(); }
        int committedMem()    { return pods.stream().mapToInt(p -> p.requests.memoryMB()).sum(); }

        // El scheduler solo mira requests, no limits ni uso actual
        boolean canSchedule(Pod p) {
            return (committedCpu() + p.requests.cpuCores()) <= capacity.cpuCores()
                && (committedMem() + p.requests.memoryMB()) <= capacity.memoryMB();
        }
    }

    // ── 1. REQUESTS vs LIMITS ─────────────────────────────────────────────────
    // requests: el scheduler los usa para encontrar un nodo con suficiente espacio.
    //           El contenedor tiene GARANTIZADOS estos recursos.
    // limits:   el kernel (cgroups) throttlea CPU si lo supera;
    //           OOMKill si la memoria supera el límite.
    //
    // Antipatrón: limits >> requests → scheduling incorrecto (nodo sobrecomprometido).
    // Buena práctica: limits = requests * 1.5-2.0 para CPU; = requests para MEM.
    static void requestsVsLimits() {
        System.out.println("── 1. Requests vs Limits ──");

        List<Pod> pods = List.of(
            new Pod("api-pod",     new Resources(0.25, 256),  new Resources(0.5,  512)),
            new Pod("worker-pod",  new Resources(0.5,  512),  new Resources(1.0, 1024)),
            new Pod("db-sidecar",  new Resources(0.1,  128),  new Resources(0.2,  256))
        );

        Node node = new Node("node-1", new Resources(2.0, 4096));

        for (Pod p : pods) {
            if (node.canSchedule(p)) {
                node.pods.add(p);
                System.out.printf("  %-15s schedulado → requests: %s | limits: %s%n",
                        p.name, p.requests, p.limits);
            } else {
                System.out.printf("  %-15s NO schedulado — nodo sin recursos%n", p.name);
            }
        }

        System.out.printf("%n  Nodo comprometido: CPU=%.2f/%.2f  MEM=%d/%dMB%n",
                node.committedCpu(), node.capacity.cpuCores(),
                node.committedMem(), node.capacity.memoryMB());
        System.out.println("  (el nodo puede estar subutilizado si el uso real < requests)");
    }

    // ── 2. CPU THROTTLING — cuando el pod supera su limit de CPU ─────────────
    // K8s usa CFS (Completely Fair Scheduler) con períodos de 100ms.
    // Si un pod tiene limit=0.5 CPU → puede usar 50ms de cada 100ms.
    // Si intenta usar más → throttled (espera hasta el siguiente período).
    // Esto no es OOMKill — el proceso no muere, solo es más lento.
    static void cpuThrottling() {
        System.out.println("\n── 2. CPU Throttling ──");

        double cpuLimit = 0.5; // 0.5 cores → 50ms cada 100ms
        double period   = 100; // ms

        double[] usages = { 0.3, 0.5, 0.7, 1.0 }; // intentos de uso en cores
        for (double usage : usages) {
            double effective  = Math.min(usage, cpuLimit);
            double throttled  = Math.max(0, usage - cpuLimit);
            double throttlePct = usage > 0 ? (throttled / usage) * 100 : 0;
            System.out.printf("  Intent=%.1f cores → effectivo=%.1f  throttled=%.1f (%.0f%%)%n",
                    usage, effective, throttled, throttlePct);
        }
        System.out.println();
        System.out.println("  OOMKill: ocurre cuando mem > limits.memory (inmediato)");
        System.out.println("  CPU throttle: proceso más lento, no muere");
        System.out.println("  Tip: evitar limits.cpu muy bajos en apps Java (GC necesita picos)");
    }

    // ── 3. HPA — Horizontal Pod Autoscaler ───────────────────────────────────
    // HPA ajusta el número de réplicas según métricas (CPU, memoria, custom).
    // Fórmula K8s: desiredReplicas = ceil(currentMetric / targetMetric * currentReplicas)
    //
    // Ciclo de control: cada 15s el HPA lee métricas del metrics-server y calcula.
    static void horizontalPodAutoscaler() {
        System.out.println("── 3. HPA — Horizontal Pod Autoscaler ──");

        int    currentReplicas  = 3;
        double targetCpuPct     = 50.0; // objetivo: 50% de uso promedio
        int    minReplicas      = 2;
        int    maxReplicas      = 10;

        // Simular varias lecturas del metrics-server
        double[] currentCpuReadings = { 30.0, 55.0, 80.0, 20.0, 95.0 };

        System.out.printf("  Objetivo CPU: %.0f%%  Min: %d  Max: %d%n",
                targetCpuPct, minReplicas, maxReplicas);
        System.out.println();

        for (double currentCpu : currentCpuReadings) {
            double ratio   = currentCpu / targetCpuPct;
            int desired    = (int) Math.ceil(ratio * currentReplicas);
            desired        = Math.max(minReplicas, Math.min(maxReplicas, desired));
            String accion  = desired > currentReplicas ? "SCALE UP"
                           : desired < currentReplicas ? "SCALE DOWN"
                           : "SIN CAMBIO";
            System.out.printf("  CPU=%.0f%%  ratio=%.2f  desired=%d  actual=%d → %s%n",
                    currentCpu, ratio, desired, currentReplicas, accion);
            currentReplicas = desired; // siguiente ciclo parte del nuevo estado
        }

        System.out.println();
        System.out.println("  Cooldown: HPA espera 5min antes de scale-down (evita flapping)");
        System.out.println("  Scale-up: más rápido — actúa en el siguiente ciclo (15s)");
    }

    // ── 4. QoS Classes ────────────────────────────────────────────────────────
    // K8s asigna una clase de QoS según requests y limits:
    // Guaranteed:  requests == limits (nunca se matará antes que otros)
    // Burstable:   requests < limits  (se puede matar bajo presión de memoria)
    // BestEffort:  sin requests ni limits (primero en morir)
    static void qosClasses() {
        System.out.println("\n── 4. QoS Classes ──");

        record QosCase(String pod, Double reqCpu, Double limCpu, Integer reqMem, Integer limMem) {
            String qos() {
                if (reqCpu == null && limCpu == null) return "BestEffort";
                if (Objects.equals(reqCpu, limCpu) && Objects.equals(reqMem, limMem)) return "Guaranteed";
                return "Burstable";
            }
        }

        List<QosCase> cases = List.of(
            new QosCase("prod-api",   0.5, 0.5, 512, 512),  // Guaranteed
            new QosCase("worker",     0.2, 1.0, 256, 512),  // Burstable
            new QosCase("batch-job",  null, null, null, null) // BestEffort
        );

        for (QosCase c : cases) {
            System.out.printf("  %-12s → QoS: %-12s (orden de evicción bajo memoria: Best→Burst→Guar)%n",
                    c.pod(), c.qos());
        }
    }

    public static void main(String[] args) {
        requestsVsLimits();
        cpuThrottling();
        horizontalPodAutoscaler();
        qosClasses();
    }
}
