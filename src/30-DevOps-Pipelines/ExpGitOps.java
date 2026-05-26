import java.util.*;

public class ExpGitOps {

    // Represents a single resource declaration stored in git
    static class Manifest {
        private final String resource;
        private final Map<String, String> spec;

        Manifest(String resource, Map<String, String> spec) {
            this.resource = resource;
            this.spec = new LinkedHashMap<>(spec);
        }

        String getResource()          { return resource; }
        Map<String, String> getSpec() { return spec; }

        @Override public boolean equals(Object o) {
            if (!(o instanceof Manifest m)) return false;
            return resource.equals(m.resource) && spec.equals(m.spec);
        }
        @Override public int hashCode() { return Objects.hash(resource, spec); }
    }

    // Source of truth: what's committed in the git repo
    static class GitRepo {
        private final Map<String, Manifest> committed = new LinkedHashMap<>();
        private int revision = 0;

        void commit(Manifest m) {
            committed.put(m.getResource(), m);
            revision++;
            System.out.printf("  [GIT COMMIT rev=%d] %s → %s%n", revision, m.getResource(), m.getSpec());
        }

        Map<String, Manifest> getState() { return Collections.unmodifiableMap(committed); }
        int getRevision()                 { return revision; }
    }

    // Live cluster state (can lag behind git until agent syncs)
    static class ClusterState {
        private final Map<String, Manifest> applied = new LinkedHashMap<>();
        private int lastSyncedRevision = 0;

        void apply(Manifest m) {
            applied.put(m.getResource(), m);
        }

        Map<String, Manifest> getApplied()      { return applied; }
        int getLastSyncedRevision()              { return lastSyncedRevision; }
        void setLastSyncedRevision(int rev)      { lastSyncedRevision = rev; }
    }

    // Reconcile loop: fetch git state, diff against cluster, apply changes
    static class GitOpsAgent {
        private final GitRepo git;
        private final ClusterState cluster;
        private int loopCount = 0;

        GitOpsAgent(GitRepo git, ClusterState cluster) {
            this.git = git;
            this.cluster = cluster;
        }

        void reconcile() {
            loopCount++;
            System.out.printf("%n  [AGENT loop #%d] git_rev=%d  last_synced_rev=%d%n",
                    loopCount, git.getRevision(), cluster.getLastSyncedRevision());

            if (git.getRevision() == cluster.getLastSyncedRevision()) {
                System.out.println("  No hay cambios en git — cluster ya converge.");
                return;
            }

            // Diff: find manifests in git that differ from cluster
            boolean anyChange = false;
            for (Map.Entry<String, Manifest> e : git.getState().entrySet()) {
                String resource = e.getKey();
                Manifest desired = e.getValue();
                Manifest actual = cluster.getApplied().get(resource);

                if (!desired.equals(actual)) {
                    anyChange = true;
                    System.out.printf("  [DRIFT] %s — desired=%s  actual=%s%n",
                            resource, desired.getSpec(),
                            actual == null ? "<not deployed>" : actual.getSpec());
                    System.out.printf("  [APPLY] %s → %s%n", resource, desired.getSpec());
                    cluster.apply(desired);
                }
            }

            if (!anyChange) {
                System.out.println("  Sin drift — cluster ya converge con git.");
            }

            cluster.setLastSyncedRevision(git.getRevision());
            System.out.printf("  Cluster sincronizado con rev=%d%n", git.getRevision());
        }
    }

    public static void main(String[] args) {

        System.out.println("═".repeat(60));
        System.out.println("  GITOPS RECONCILIATION LOOP — simulación");
        System.out.println("═".repeat(60));

        GitRepo git     = new GitRepo();
        ClusterState cluster = new ClusterState();
        GitOpsAgent agent   = new GitOpsAgent(git, cluster);

        // ── Commit inicial en git ─────────────────────────────────
        System.out.println("\n[Paso 1] Commit inicial en git");
        System.out.println("─".repeat(60));
        git.commit(new Manifest("deployment/api",
                Map.of("image", "myapp:v1.0", "replicas", "3")));
        git.commit(new Manifest("service/api",
                Map.of("port", "8080", "type", "ClusterIP")));

        // ── Primer reconcile: git_rev=2, cluster_rev=0 ────────────
        System.out.println("\n[Loop 1] Primer reconcile — cluster vacío");
        System.out.println("─".repeat(60));
        agent.reconcile();

        // ── Segundo reconcile: nada nuevo ─────────────────────────
        System.out.println("\n[Loop 2] Sin cambios en git");
        System.out.println("─".repeat(60));
        agent.reconcile();

        // ── Desarrollador hace commit de nueva versión ────────────
        System.out.println("\n[Paso 2] Desarrollador actualiza imagen en git");
        System.out.println("─".repeat(60));
        git.commit(new Manifest("deployment/api",
                Map.of("image", "myapp:v2.0", "replicas", "3")));

        // ── Agente detecta drift y sincroniza ─────────────────────
        System.out.println("\n[Loop 3] Agente detecta drift y aplica cambio");
        System.out.println("─".repeat(60));
        agent.reconcile();

        System.out.println("\n── Conclusión ──");
        System.out.println("  GitOps: git es la única fuente de verdad.");
        System.out.println("  El agente asegura que el cluster converge con lo declarado en git.");
    }
}
