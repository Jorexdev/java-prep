import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio6 {

    static class Instance {
        final String id;
        final int weight;

        Instance(String id, int weight) {
            this.id = id;
            this.weight = weight;
        }
    }

    interface Strategy {
        Instance select(List<Instance> instances);
    }

    static class RoundRobin implements Strategy {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Instance select(List<Instance> instances) {
            int idx = counter.getAndIncrement() % instances.size();
            return instances.get(idx);
        }
    }

    static class RandomStrategy implements Strategy {
        private final Random random = new Random(42);

        @Override
        public Instance select(List<Instance> instances) {
            return instances.get(random.nextInt(instances.size()));
        }
    }

    static class WeightedRoundRobin implements Strategy {
        private final List<Instance> expanded = new ArrayList<>();
        private final AtomicInteger counter = new AtomicInteger(0);

        WeightedRoundRobin(List<Instance> instances) {
            for (Instance inst : instances) {
                for (int i = 0; i < inst.weight; i++) {
                    expanded.add(inst);
                }
            }
        }

        @Override
        public Instance select(List<Instance> instances) {
            int idx = counter.getAndIncrement() % expanded.size();
            return expanded.get(idx);
        }
    }

    static class LoadBalancer {
        private final List<Instance> instances;
        private final Strategy strategy;

        LoadBalancer(List<Instance> instances, Strategy strategy) {
            this.instances = instances;
            this.strategy = strategy;
        }

        Instance next() {
            return strategy.select(instances);
        }
    }

    static void simulate(String name, LoadBalancer lb, int requests) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (int i = 0; i < requests; i++) {
            String id = lb.next().id;
            distribution.merge(id, 1, Integer::sum);
        }
        System.out.println(name + ":");
        distribution.forEach((id, count) ->
            System.out.printf("  %-12s → %d requests%n", id, count));
    }

    public static void main(String[] args) {
        List<Instance> instances = List.of(
            new Instance("srv-A", 3),
            new Instance("srv-B", 1),
            new Instance("srv-C", 2)
        );

        simulate("RoundRobin",
            new LoadBalancer(instances, new RoundRobin()), 12);

        System.out.println();
        simulate("Random",
            new LoadBalancer(instances, new RandomStrategy()), 12);

        System.out.println();
        simulate("WeightedRoundRobin",
            new LoadBalancer(instances, new WeightedRoundRobin(instances)), 12);
    }
}
