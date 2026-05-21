import java.util.*;

public class Ejercicio2 {

    record TenantUser(String username, String tenantId, Set<String> roles) {}

    static class TenantContext {
        private static final ThreadLocal<TenantUser> ctx = new ThreadLocal<>();
        static void set(TenantUser u) { ctx.set(u); }
        static TenantUser get()       { return ctx.get(); }
        static void clear()           { ctx.remove(); }
        static String tenantId()      { TenantUser u = get(); return u != null ? u.tenantId() : null; }
    }

    static class TenantUserStore {
        private final Map<String, Map<String, TenantUser>> store = new HashMap<>();

        void add(TenantUser u) {
            store.computeIfAbsent(u.tenantId(), k -> new HashMap<>()).put(u.username(), u);
        }
        Optional<TenantUser> find(String tenantId, String username) {
            return Optional.ofNullable(store.getOrDefault(tenantId, Map.of()).get(username));
        }
    }

    static class TenantAuthService {
        private final TenantUserStore users;
        TenantAuthService(TenantUserStore u) { this.users = u; }

        void login(String tenantId, String username) {
            TenantUser u = users.find(tenantId, username)
                .orElseThrow(() -> new SecurityException("Usuario no encontrado: " + username + "@" + tenantId));
            TenantContext.set(u);
            System.out.println("[Auth] Login: " + username + "@" + tenantId + " roles=" + u.roles());
        }
    }

    static class TenantResource {
        void read(String resource) {
            TenantUser u = TenantContext.get();
            if (u == null) throw new SecurityException("No autenticado");
            System.out.println("[Resource] " + u.username() + "@" + u.tenantId() + " lee " + resource);
        }

        void adminAction() {
            TenantUser u = TenantContext.get();
            if (u == null) throw new SecurityException("No autenticado");
            if (!u.roles().contains("ADMIN"))
                throw new SecurityException("Requiere ADMIN — tenant=" + u.tenantId() + " user=" + u.username());
            System.out.println("[Resource] Admin action por " + u.username() + "@" + u.tenantId());
        }

        void crossTenantCheck(String targetTenantId) {
            TenantUser u = TenantContext.get();
            if (u == null) throw new SecurityException("No autenticado");
            if (!u.tenantId().equals(targetTenantId))
                throw new SecurityException("Acceso denegado: " + u.tenantId() + " no puede acceder a " + targetTenantId);
            System.out.println("[Resource] Acceso al tenant propio (" + targetTenantId + ") OK");
        }
    }

    public static void main(String[] args) {
        TenantUserStore store = new TenantUserStore();
        store.add(new TenantUser("alice", "acme",   Set.of("ADMIN", "USER")));
        store.add(new TenantUser("bob",   "acme",   Set.of("USER")));
        store.add(new TenantUser("alice", "globex", Set.of("USER")));
        store.add(new TenantUser("carol", "globex", Set.of("ADMIN")));

        TenantAuthService auth     = new TenantAuthService(store);
        TenantResource    resource = new TenantResource();

        System.out.println("=== alice@acme (ADMIN) ===");
        auth.login("acme", "alice");
        resource.read("/datos");
        resource.adminAction();
        resource.crossTenantCheck("acme");
        TenantContext.clear();

        System.out.println("\n=== alice@globex (USER) intenta acción admin ===");
        auth.login("globex", "alice");
        resource.read("/datos");
        try { resource.adminAction(); } catch (SecurityException e) { System.out.println("Bloqueado: " + e.getMessage()); }
        TenantContext.clear();

        System.out.println("\n=== Aislamiento: bob@acme intenta acceder a globex ===");
        auth.login("acme", "bob");
        try { resource.crossTenantCheck("globex"); } catch (SecurityException e) { System.out.println("Bloqueado: " + e.getMessage()); }
        TenantContext.clear();

        System.out.println("\n=== Usuario inexistente en tenant ===");
        try { auth.login("globex", "bob"); } catch (SecurityException e) { System.out.println("Bloqueado: " + e.getMessage()); }
    }
}
