import java.util.*;

public class Ejercicio3 {

    static class Pod {
        String name;
        Map<String, String> labels;

        Pod(String name, Map<String, String> labels) {
            this.name   = name;
            this.labels = new LinkedHashMap<>(labels);
        }

        boolean matchesSelector(Map<String, String> selector) {
            if (selector == null || selector.isEmpty()) return true;
            for (Map.Entry<String, String> e : selector.entrySet()) {
                if (!e.getValue().equals(labels.get(e.getKey()))) return false;
            }
            return true;
        }
    }

    static class NetworkRule {
        Map<String, String> podSelector;  // selector del origen (ingress) o destino (egress)
        List<Integer> ports;              // puertos permitidos, vacío = todos

        NetworkRule(Map<String, String> podSelector, List<Integer> ports) {
            this.podSelector = podSelector;
            this.ports       = ports != null ? ports : List.of();
        }

        boolean allowsPort(int port) {
            return ports.isEmpty() || ports.contains(port);
        }
    }

    static class NetworkPolicy {
        Map<String, String> podSelector;   // a qué pods aplica esta policy
        List<NetworkRule> ingressRules;
        List<NetworkRule> egressRules;

        NetworkPolicy(Map<String, String> podSelector,
                      List<NetworkRule> ingressRules,
                      List<NetworkRule> egressRules) {
            this.podSelector  = podSelector;
            this.ingressRules = ingressRules != null ? ingressRules : List.of();
            this.egressRules  = egressRules  != null ? egressRules  : List.of();
        }

        boolean appliesTo(Pod pod) {
            return pod.matchesSelector(podSelector);
        }
    }

    static class NetworkPolicyEngine {
        List<NetworkPolicy> policies = new ArrayList<>();
        List<Pod> pods = new ArrayList<>();

        void addPolicy(NetworkPolicy p) { policies.add(p); }
        void addPod(Pod p)              { pods.add(p); }

        boolean isAllowed(Pod src, Pod dst, int port) {
            // Buscar policies que apliquen al destino (ingress)
            List<NetworkPolicy> dstPolicies = policies.stream()
                    .filter(p -> p.appliesTo(dst) && !p.ingressRules.isEmpty())
                    .toList();

            if (dstPolicies.isEmpty()) return true; // sin policy → permitido

            for (NetworkPolicy policy : dstPolicies) {
                for (NetworkRule rule : policy.ingressRules) {
                    if (src.matchesSelector(rule.podSelector) && rule.allowsPort(port)) {
                        return true;
                    }
                }
            }
            return false;
        }

        void check(Pod src, Pod dst, int port) {
            boolean allowed = isAllowed(src, dst, port);
            System.out.printf("  %-10s → %-10s port=%-6d : %s%n",
                    src.name, dst.name, port, allowed ? "PERMITIDO" : "BLOQUEADO");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kubernetes NetworkPolicy Demo ===\n");

        Pod podA = new Pod("pod-a", Map.of("app", "frontend"));
        Pod podB = new Pod("pod-b", Map.of("app", "api"));
        Pod podC = new Pod("pod-c", Map.of("app", "db"));

        // Policy para pod-b: solo permite ingress desde frontend en puerto 8080
        NetworkPolicy policyB = new NetworkPolicy(
                Map.of("app", "api"),
                List.of(new NetworkRule(Map.of("app", "frontend"), List.of(8080))),
                null
        );

        // Policy para pod-c: no permite ingress de nadie (lista vacía de rules = deny all)
        NetworkPolicy policyC = new NetworkPolicy(
                Map.of("app", "db"),
                List.of(), // sin reglas de ingress → bloqueo total
                null
        );

        NetworkPolicyEngine engine = new NetworkPolicyEngine();
        engine.addPod(podA);
        engine.addPod(podB);
        engine.addPod(podC);
        engine.addPolicy(policyB);
        engine.addPolicy(policyC);

        System.out.println("Políticas activas:");
        System.out.println("  pod-b: ingress desde frontend:8080 permitido");
        System.out.println("  pod-c: deny-all ingress\n");

        System.out.println("Verificaciones:");
        engine.check(podA, podB, 8080);   // PERMITIDO
        engine.check(podA, podB, 9090);   // BLOQUEADO (puerto distinto)
        engine.check(podC, podB, 8080);   // BLOQUEADO (src no es frontend)
        engine.check(podA, podC, 5432);   // BLOQUEADO (deny-all en pod-c)
        engine.check(podB, podC, 5432);   // BLOQUEADO (deny-all en pod-c)
        engine.check(podA, podA, 8080);   // PERMITIDO (sin policy en pod-a)
    }
}
