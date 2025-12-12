package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.dto.UserDTO;
import com.users.UsuariosYEmpleados.dto.TokenDriverDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import com.users.UsuariosYEmpleados.service.UserService;
import com.users.UsuariosYEmpleados.service.TokenService;
import com.users.UsuariosYEmpleados.service.apis.emailService;
import com.users.UsuariosYEmpleados.util.BCryptUtils;
import com.users.UsuariosYEmpleados.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final emailService emailService;
    private final BCryptUtils bCryptUtils;
    private final JwtUtils jwtUtils;
    private final String nodeEnv;

    public AuthController(UserService userService,
            TokenService tokenService,
            emailService emailService,
            BCryptUtils bCryptUtils,
            JwtUtils jwtUtils,
            @Value("${spring.profiles.active:development}") String nodeEnv) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.bCryptUtils = bCryptUtils;
        this.jwtUtils = jwtUtils;
        this.nodeEnv = nodeEnv;
    }

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PASSWORD_REGEX = Pattern
            .compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$");
    private static final String MSG_EMAIL_PASSWORD_REQUIRED = "Correo y contraseña son obligatorios";

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> requestBody) {
        try {
            // Validaciones de entrada
            String correo = (String) requestBody.get("correo");
            String contrasena = (String) requestBody.get("contrasena");
            String tipoUsuarioStr = (String) requestBody.get("tipoUsuario");

            System.out.println("[REGISTER] Datos recibidos - correo: " + correo +
                    ", tipoUsuario: " + tipoUsuarioStr);

            if (isBlank(correo) || isBlank(contrasena)) {
                return badRequest(MSG_EMAIL_PASSWORD_REQUIRED);
            }
            if (!EMAIL_REGEX.matcher(correo).matches()) {
                return badRequest("Formato de correo inválido");
            }
            if (!PASSWORD_REGEX.matcher(contrasena).matches()) {
                return badRequest(
                        "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número");
            }

            // Validar tipoUsuario
            TipoUsuario tipoUsuario = TipoUsuario.cliente; // valor por defecto
            if (tipoUsuarioStr != null) {
                try {
                    tipoUsuario = TipoUsuario.valueOf(tipoUsuarioStr.toLowerCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("message", "Tipo de usuario inválido"));
                }
            }

            // Validar correo duplicado usando UserService
            if (userService.existsByCorreo(correo)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "El correo ya está registrado"));
            }

            // Hash de contraseña
            String hashedPassword = bCryptUtils.hashPassword(contrasena);

            // Crear DTO para usuario
            UserDTO userDTO = new UserDTO();

            userDTO.setCorreo(correo);
            userDTO.setContrasena(hashedPassword);
            userDTO.setTipoUsuario(tipoUsuario);

            // En desarrollo, activar automáticamente; en producción, requiere verificación
            // de email
            boolean activo = false;
            userDTO.setActivo(activo);

            // Crear usuario usando UserService
            UserDTO usuarioCreado = userService.create(userDTO);

            System.out.println("[REGISTER] Usuario creado: { id: " + usuarioCreado.getIdUsuario() +
                    ", correo: " + usuarioCreado.getCorreo() +
                    ", activo: " + usuarioCreado.getActivo() +
                    ", env: " + nodeEnv + " }");

            // Si no está activo, crear y enviar token de verificación
            if (!activo) {
                // Crear token de verificación usando TokenService
                TokenDriverDTO TokenDriverDTO = tokenService.createToken(
                        usuarioCreado.getIdUsuario(),
                        1 // Expira en 1 día para verificación
                );

                // Enviar correo de verificación
                emailService.sendVerificationEmail(correo, TokenDriverDTO.getToken());

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of(
                                "message",
                                "Usuario registrado correctamente. Revisa tu correo para verificar la cuenta.",
                                "userId", usuarioCreado.getIdUsuario()));
            } else {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of(
                                "message", "Usuario registrado y activado correctamente.",
                                "userId", usuarioCreado.getIdUsuario()));
            }

        } catch (RuntimeException e) {
            // Manejar excepciones específicas del servicio
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception error) {
            System.err.println("[REGISTER ERROR] " + error.getMessage());
            error.printStackTrace();
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

            // Validar token usando TokenService
            if (!tokenService.isValidToken(token)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Token de verificación inválido o expirado"));
            }

            // Buscar token usando TokenService
            TokenDriverDTO TokenDriverDTO = tokenService.findByToken(token);

            if (TokenDriverDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Token de verificación no encontrado"));
            }

            // Buscar usuario usando UserService
            UserDTO usuario = userService.findById(TokenDriverDTO.getIdUsuario());

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Usuario no encontrado para este token"));
            }

            if (Boolean.TRUE.equals(usuario.getActivo())) {
                return ResponseEntity.ok(Map.of("message", "El usuario ya está verificado"));
            }

            // Actualizar usuario a activo usando UserService
            UserDTO updateDTO = new UserDTO();
            updateDTO.setActivo(true);
            UserDTO updated = userService.update(usuario.getIdUsuario(), updateDTO);

            // Eliminar el token de verificación ya usado
            tokenService.deleteByToken(token);

            return ResponseEntity.ok(Map.of(
                    "message", "Se verificó el correo " + usuario.getCorreo(),
                    "email", usuario.getCorreo()));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception error) {
            System.err.println("[VERIFY     AIL ERROR] " + error.getMessage());
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

            // Validar campos requeridos
            if (correo == null || contrasena == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email y contraseña son obligatorios"));
            }

            // Validar formato de email
            if (!EMAIL_REGEX.matcher(correo).matches()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Formato de email inválido"));
            }

            // Buscar usuario usando UserService
            UserDTO user;
            try {
                System.out.println("/////////////////////");
                System.out.println(correo);
                user = userService.findByCorreoForLogin(correo);
                System.out.println(user);
            } catch (RuntimeException e) {
                System.out.println("//////////////////////////// ERROR IN LOGIN ////////////////////////////");
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Error: " + e.getMessage()));
            }

            System.out.println("[LOGIN] Usuario encontrado: { id: " + user.getIdUsuario() +
                    ", correo: " + user.getCorreo() +
                    ", activo: " + user.getActivo() +
                    ", tipoUsuario: " + user.getTipoUsuario() + " }");

            // Validar usuario activo
            if (Boolean.FALSE.equals(user.getActivo())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "El usuario no está activo. Por favor verifica tu correo"));
            }

            // Validar contraseña
            // NOTA: userService.findByCorreo no devuelve la contraseña (por seguridad)
            // Necesitamos un método específico para login que sí devuelva la contraseña
            // Por ahora, asumimos que BCryptUtils puede verificar directamente
            boolean isValid = bCryptUtils.comparePassword(contrasena, user.getContrasena());

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Usuario o contraseña incorrectos"));
            }

            // Generar tokens JWT
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", user.getIdUsuario());
            payload.put("tipoUsuario", user.getTipoUsuario().toString().toLowerCase());
            payload.put("activo", user.getActivo());
    
            System.out.println("payload//////////////////////////////////////////////////");
            System.out.println(payload);

            
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

            // Crear token en base de datos usando TokenService
            tokenService.createToken(user.getIdUsuario(), 7); // Token válido por 7 días

            System.out.println("[LOGIN] Tokens generados exitosamente para usuario: " + user.getCorreo());

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Inicio de sesión exitoso");

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getIdUsuario());
            userInfo.put("email", user.getCorreo());
            userInfo.put("tipoUsuario", user.getTipoUsuario());
            userInfo.put("activo", user.getActivo());

            responseBody.put("user", userInfo);

            return ResponseEntity.ok(responseBody);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message catch", e.getMessage()));
        } catch (Exception error) {
            System.err.println("[LOGIN ERROR] " + error.getMessage());
            error.printStackTrace();
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

            // Ya no verificamos si payload es null porque verifyRefreshToken ahora lanza
            // excepción
            String username = (String) payload.get("sub");

            // Buscar usuario usando UserService
            Integer userId = (Integer) payload.get("id");
            UserDTO user = userService.findById(userId);

            if (user == null || Boolean.FALSE.equals(user.getActivo())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Usuario no válido o inactivo"));
            }

            // Verificar que el token también exista en nuestra base de datos
            boolean tokenValidoEnDB = tokenService.isValidTokenForUsuario(token, userId);
            if (!tokenValidoEnDB) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Refresh token no válido"));
            }

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
            System.err.println("[RESEND VERIFICATION ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al reenviar correo de verificación"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        try {
            // Si tenemos un refresh token, eliminarlo de la base de datos
            if (refreshToken != null) {
                try {
                    tokenService.deleteByToken(refreshToken);
                } catch (Exception e) {
                    // Si no existe el token, no hay problema
                    System.out.println("[LOGOUT] Token no encontrado en DB: " + refreshToken);
                }
            }

            // Eliminar cookies
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

            // Verificar que el token también exista en nuestra base de datos
            Integer userId = (Integer) payload.get("id");
            boolean tokenValidoEnDB = tokenService.isValidTokenForUsuario(token, userId);

            if (!tokenValidoEnDB) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Token no válido"));
            }

            // Obtener usuario usando UserService
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
            System.err.println("[PROFILE ERROR] " + error.getMessage());
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message));
    }

}