package utilidades;

import org.mindrot.jbcrypt.BCrypt;

public class GenerarPassword {

    public static void main(String[] args) {

        String password = "admin123";

        String hash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        System.out.println("HASH GENERADO:");
        System.out.println(hash);

        System.out.println("VERIFICACION:");
        System.out.println(BCrypt.checkpw(password, hash));
    }
}