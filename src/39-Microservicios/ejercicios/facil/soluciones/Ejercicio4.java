import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio4 {

    enum InstanceStatus { UP, DOWN }

    static class ServiceInstance {
        final String instanceId;
        final String name;
        final String host;
        final int port;
        volatile InstanceStatus status;

        ServiceInstance(String instanceId, String name, String host, int port) {
            this.instanceId = instanceId;
            this.name = name;
            this.host = host;
            this.port = port;
            this.status = InstanceStatus.UP;
        }

        @Override
        public String toString() {
            return instanceId + " [" + host + ":" + port + "] " + status;
        }
    }

    static class ServiceRegistry {
        private final Map<String, ServiceInstance> instances = new ConcurrentHashMap<>();
        private final AtomicInteger idCounter = new AtomicInteger(1);

        String register(String name, String host, int port) {
            String instanceId = name + "-" + idCounter.getAndIncrement();
            instances.put(instanceId, new ServiceInstance(instanceId, name, host, port));
            System.out.println("Registrado: " + instanceId + " [" + host + ":" + port + "]");
            return instanceId;
        }

        void deregister(String instanceId) {
            instances.remove(instanceId);
            System.out.println("Eliminado: " + instanceId);
        }

        List<ServiceInstance> discover(String name) {
            List<ServiceInstance> result = new ArrayList<>();
            for (ServiceInstance inst : instances.values()) {
                if (inst.name.equals(name) && inst.status == InstanceStatus.UP) {
                    result.add(inst);
                }
            }
            return result;
        }

        ServiceInstance getInstance(String instanceId) {
            return instances.get(instanceId);
        }
    }

    static class HealthChecker {
        private final ServiceRegistry registry;

        HealthChecker(ServiceRegistry registry) {
            this.registry = registry;
        }

        void markDown(String instanceId) {
            ServiceInstance inst = registry.getInstance(instanceId);
            if (inst != null) {
                inst.status = InstanceStatus.DOWN;
                System.out.println("HealthChecker marcó DOWN: " + instanceId);
            }
        }

        void markUp(String instanceId) {
            ServiceInstance inst = registry.getInstance(instanceId);
            if (inst != null) {
                inst.status = InstanceStatus.UP;
                System.out.println("HealthChecker marcó UP: " + instanceId);
            }
        }
    }

    public static void main(String[] args) {
        ServiceRegistry registry = new ServiceRegistry();
        HealthChecker healthChecker = new HealthChecker(registry);

        String id1 = registry.register("inventario-service", "10.0.0.1", 8080);
        String id2 = registry.register("inventario-service", "10.0.0.2", 8080);
        String id3 = registry.register("inventario-service", "10.0.0.3", 8080);

        System.out.println("\nInstancias UP antes del fallo:");
        registry.discover("inventario-service").forEach(i -> System.out.println("  " + i));

        healthChecker.markDown(id2);

        System.out.println("\nInstancias UP después del fallo:");
        List<ServiceInstance> available = registry.discover("inventario-service");
        available.forEach(i -> System.out.println("  " + i));
        System.out.println("Total disponibles: " + available.size());
    }
}
