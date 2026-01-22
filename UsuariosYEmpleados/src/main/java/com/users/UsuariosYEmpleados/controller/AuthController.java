package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.domain.dto.UserDTO;
import com.users.UsuariosYEmpleados.domain.dto.TokenDriverDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import com.users.UsuariosYEmpleados.service.UserService;
import com.users.UsuariosYEmpleados.service.TokenService;
import com.users.UsuariosYEmpleados.service.apis.EmailService;
import com.users.UsuariosYEmpleados.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final String nodeEnv;

    public AuthController(UserService userService,
            TokenService tokenService,
            EmailService emailService,
            JwtUtils jwtUtils,
            @Value("${spring.profiles.active:development}") String nodeEnv) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.nodeEnv = nodeEnv;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> requestBody) {
        try {
            String correo = (String) requestBody.get("correo");
            String contrasena = (String) requestBody.get("contrasena");
            String tipoUsuarioStr = (String) requestBody.get("tipoUsuario");

            // Parsear tipo de usuario
            TipoUsuario tipoUsuario = TipoUsuario.cliente;
            if (tipoUsuarioStr != null) {
                try {
                    tipoUsuario = TipoUsuario.valueOf(tipoUsuarioStr.toLowerCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("message", "Tipo de usuario inválido"));
                }
            }

            // Registrar usuario (validaciones en UserService)
            UserDTO usuarioCreado = userService.register(correo, contrasena, tipoUsuario);

            // Crear y enviar token de verificación
            TokenDriverDTO tokenDTO = tokenService.createToken(usuarioCreado.getIdUsuario(), 1);
            emailService.sendVerificationEmail(correo, tokenDTO.getToken());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message",
                            "Usuario registrado correctamente. Revisa tu correo para verificar la cuenta.",
                            "userId", usuarioCreado.getIdUsuario()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al registrar usuario"));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Token de verificación requerido"));
            }

            // Validar token
            if (!tokenService.isValidToken(token)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Token de verificación inválido o expirado"));
            }

            TokenDriverDTO tokenDTO = tokenService.findByToken(token);
            if (tokenDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Token de verificación no encontrado"));
            }

            // Activar usuario (validación en UserService)
            userService.activateUser(tokenDTO.getIdUsuario());
            
            UserDTO usuario = userService.findById(tokenDTO.getIdUsuario());

            // Eliminar token usado
            tokenService.deleteByToken(token);

            return ResponseEntity.ok(Map.of(
                    "message", "Se verificó el correo " + usuario.getCorreo(),
                    "email", usuario.getCorreo()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al verificar email"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> requestBody,
            HttpServletResponse response) {
        try {
            String correo = (String) requestBody.get("correo");
            String contrasena = (String) requestBody.get("contrasena");

            // Validar login (validaciones en UserService)
            UserDTO user = userService.validateLogin(correo, contrasena);

            // Generar tokens JWT
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", user.getIdUsuario());
            payload.put("tipoUsuario", user.getTipoUsuario().toString().toLowerCase());
            payload.put("activo", user.getActivo());
    
            String accessToken = jwtUtils.generateAccessToken(payload);
            String refreshToken = jwtUtils.generateRefreshToken(payload);

            // Configurar cookies
            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure("production".equals(nodeEnv));
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(15 * 60); // 15 minutos
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure("production".equals(nodeEnv));
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 días
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(Map.of(
                    "message", "Inicio de sesión exitoso",
                    "user", Map.of(
                            "id", user.getIdUsuario(),
                            "correo", user.getCorreo(),
                            "tipoUsuario", user.getTipoUsuario(),
                            "activo", user.getActivo())));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al iniciar sesión"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String token,
            HttpServletResponse response) {
        try {
            // Obtener el refreshToken de las cookies
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "No se proporcionó refresh token"));
            }

            // Verificar refreshToken JWT
            Map<String, Object> payload = jwtUtils.verifyRefreshToken(token);

            // Ya no verificamos si payload es null porque verifyRefreshToken ahora lanza excepción
            // Buscar usuario usando UserService
            Integer userId = (Integer) payload.get("id");
            UserDTO user = userService.findById(userId);

            if (user == null || Boolean.FALSE.equals(user.getActivo())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Usuario no válido o inactivo"));
            }

            // Verificar que el token también exista en nuestra base de datos
            // boolean tokenValidoEnDB = tokenService.isValidTokenForUsuario(token, userId);
            // if (!tokenValidoEnDB) {
            //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            //             .body(Map.of("message", "Refresh token no válido"));
            // }

            // Generar nuevo accessToken
            Map<String, Object> newPayload = new HashMap<>();
            newPayload.put("id", user.getIdUsuario());
            newPayload.put("tipoUsuario", user.getTipoUsuario().toString().toLowerCase());
            newPayload.put("activo", user.getActivo());

            String newAccessToken = jwtUtils.generateAccessToken(newPayload);

            // Configurar cookie con nuevo accessToken
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure("production".equals(nodeEnv));
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(15 * 60); // 15 minutos
            response.addCookie(accessTokenCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Nuevo access token generado");
            responseBody.put("accessToken", newAccessToken);

            return ResponseEntity.ok(responseBody);

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token inválido o expirado: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception error) {
            System.err.println("[REFRESH TOKEN ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al refrescar token"));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, Object> requestBody) {
        try {
            String correo = (String) requestBody.get("correo");
            if (correo == null || correo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El campo correo es requerido"));
            }

            // Buscar usuario por correo usando UserService
            UserDTO usuario;
            try {
                usuario = userService.findByCorreo(correo);
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Usuario no encontrado"));
            }

            if (Boolean.TRUE.equals(usuario.getActivo())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El usuario ya está verificado"));
            }

            // Eliminar tokens de verificación antiguos del usuario
            tokenService.deleteByUsuario(usuario.getIdUsuario());

            // Generar nuevo token de verificación usando TokenService
            TokenDriverDTO TokenDriverDTO = tokenService.createToken(usuario.getIdUsuario(), 1);

            // Enviar correo de verificación
            emailService.sendVerificationEmail(correo, TokenDriverDTO.getToken());

            return ResponseEntity.ok(Map.of("message", "Correo de verificación reenviado"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al reenviar correo de verificación"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        try {
            // Eliminar cookies de autenticación (sistema stateless, no requiere BD)
            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure("production".equals(nodeEnv));
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0); // Eliminar cookie
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure("production".equals(nodeEnv));
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0); // Eliminar cookie
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente"));

        } catch (Exception error) {
            System.err.println("[LOGOUT ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al cerrar sesión"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@CookieValue(value = "accessToken", required = false) String token) {
        try {
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "No autenticado"));
            }

            // Verificar token JWT
            Map<String, Object> payload = jwtUtils.verifyAccessToken(token);

            // Ya no verificamos si payload es null
            // Los accessToken JWT son stateless y no se validan contra la base de datos
            // Solo verificamos la firma JWT

            // Obtener usuario usando UserService
            Integer userId = (Integer) payload.get("id");
            UserDTO user = userService.findById(userId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Usuario no encontrado"));
            }

            // Crear respuesta sin información sensible
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getIdUsuario());
            response.put("correo", user.getCorreo());
            response.put("tipoUsuario", user.getTipoUsuario());
            response.put("activo", user.getActivo());

            return ResponseEntity.ok(response);

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token inválido o expirado: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al obtener perfil"));
        }
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) {
        try {
            boolean exists = userService.existsByCorreo(email);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al verificar email"));
        }
    }
}