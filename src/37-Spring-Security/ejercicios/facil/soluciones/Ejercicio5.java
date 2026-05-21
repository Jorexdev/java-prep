import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio5 {

    interface UserDetails {
        String getUsername();
        String getPassword();
        List<String> getAuthorities();
        boolean isEnabled();
        boolean isAccountNonLocked();
    }

    static class User implements UserDetails {
        private final String username;
        private final String password;
        private final List<String> authorities;
        private final boolean enabled;
        private final boolean accountNonLocked;

        User(String username, String password, List<String> authorities,
             boolean enabled, boolean accountNonLocked) {
            this.username = username;
            this.password = password;
            this.authorities = List.copyOf(authorities);
            this.enabled = enabled;
            this.accountNonLocked = accountNonLocked;
        }

        @Override public String getUsername()          { return username; }
        @Override public String getPassword()          { return password; }
        @Override public List<String> getAuthorities() { return authorities; }
        @Override public boolean isEnabled()           { return enabled; }
        @Override public boolean isAccountNonLocked()  { return accountNonLocked; }
    }

    static class UsernameNotFoundException extends RuntimeException {
        UsernameNotFoundException(String msg) { super(msg); }
    }

    static class InMemoryUserDetailsService {
        private final Map<String, UserDetails> store = new HashMap<>();

        void register(UserDetails user) {
            store.put(user.getUsername(), user);
        }

        UserDetails loadUserByUsername(String username) {
            UserDetails user = store.get(username);
            if (user == null) throw new UsernameNotFoundException("Usuario no encontrado: " + username);
            return user;
        }
    }

    public static void main(String[] args) {
        InMemoryUserDetailsService service = new InMemoryUserDetailsService();

        service.register(new User("jorge",   "{bcrypt}hash1", List.of("ADMIN"),  true,  true));
        service.register(new User("ana",     "{bcrypt}hash2", List.of("USER"),   true,  true));
        service.register(new User("roberto", "{bcrypt}hash3", List.of("USER"),   true,  false));

        for (String name : List.of("jorge", "ana", "roberto")) {
            UserDetails u = service.loadUserByUsername(name);
            System.out.printf("%-10s | roles=%-12s | enabled=%-5s | nonLocked=%s%n",
                    u.getUsername(), u.getAuthorities(), u.isEnabled(), u.isAccountNonLocked());
        }

        System.out.println();
        try {
            service.loadUserByUsername("fantasma");
        } catch (UsernameNotFoundException e) {
            System.out.println("Excepción esperada: " + e.getMessage());
        }
    }
}
