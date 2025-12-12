package com.users.UsuariosYEmpleados.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class BCryptUtils {

    @Value("${app.security.bcrypt.rounds:10}")
    private int rounds;

    public String hashPassword(String password) {
        String salt = BCrypt.gensalt(rounds);
        return BCrypt.hashpw(password, salt);
    }

    public boolean comparePassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}