public class BuilderDemo {

    // El objeto final es inmutable: todos los campos son final y solo
    // se asignan una vez en el constructor privado que recibe el Builder.
    static class User {
        private final String username;    // obligatorio
        private final String email;       // opcional
        private final String phone;       // opcional
        private final boolean newsletter; // opcional

        private User(Builder b) {
            this.username   = b.username;
            this.email      = b.email;
            this.phone      = b.phone;
            this.newsletter = b.newsletter;
        }

        // El Builder acumula los valores y construye el objeto en build().
        // Cada setter devuelve "this" para encadenar llamadas (fluent API).
        public static class Builder {
            private final String username;
            private String email;
            private String phone;
            private boolean newsletter;

            public Builder(String username)         { this.username = username; }
            public Builder email(String email)      { this.email = email; return this; }
            public Builder phone(String phone)      { this.phone = phone; return this; }
            public Builder newsletter(boolean val)  { this.newsletter = val; return this; }

            public User build() {
                if (username == null || username.isBlank())
                    throw new IllegalStateException("El username es obligatorio");
                return new User(this);
            }
        }

        @Override
        public String toString() {
            return "User{username='%s', email='%s', phone='%s', newsletter=%s}"
                    .formatted(username, email, phone, newsletter);
        }
    }

    public static void main(String[] args) {
        // Solo se especifican los campos que interesan, el resto queda en su default
        User u = new User.Builder("jorex")
                .email("jorex@mail.com")
                .newsletter(true)
                .build();

        System.out.println(u);
    }
}
