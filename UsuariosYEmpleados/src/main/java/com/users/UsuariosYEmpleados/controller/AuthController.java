package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.domain.dto.UserDTO;
import com.users.UsuariosYEmpleados.domain.dto.TokenDriverDTO;
import com.users.UsuariosYEmpleados.domain.dto.ClientDTO;
import com.users.UsuariosYEmpleados.domain.dto.EmpleadoDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import com.users.UsuariosYEmpleados.service.UserService;
import com.users.UsuariosYEmpleados.service.TokenService;
import com.users.UsuariosYEmpleados.service.ClientService;
import com.users.UsuariosYEmpleados.service.EmpleadoService;
import com.users.UsuariosYEmpleados.service.apis.EmailService;
import com.users.UsuariosYEmpleados.util.JwtUtils;
import com.users.UsuariosYEmpleados.util.ResponseUtils;
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
    private final ClientService clientService;
    private final EmpleadoService empleadoService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final String nodeEnv;

    public AuthController(UserService userService,
            TokenService tokenService,
            ClientService clientService,
            EmpleadoService empleadoService,
            EmailService emailService,
            JwtUtils jwtUtils,
            @Value("${spring.profiles.active:development}") String nodeEnv) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.clientService = clientService;
        this.empleadoService = empleadoService;
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
                    return ResponseUtils.error(HttpStatus.BAD_REQUEST, "Tipo de usuario inválido");
                }
            }

            // Registrar usuario (validaciones en UserService)
            UserDTO usuarioCreado = userService.register(correo, contrasena, tipoUsuario);

            // Crear y enviar token de verificación
            TokenDriverDTO tokenDTO = tokenService.createToken(usuarioCreado.getIdUsuario(), 1);
            emailService.sendVerificationEmail(correo, tokenDTO.getToken());

                Map<String, Object> data = new HashMap<>();
                data.put("userId", usuarioCreado.getIdUsuario());

                return ResponseUtils.created(data,
                    "Usuario registrado correctamente. Revisa tu correo para verificar la cuenta.");

        } catch (IllegalArgumentException e) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception error) {
                return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al registrar usuario");
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "Token de verificación requerido");
            }

            // Validar token
            if (!tokenService.isValidToken(token)) {
                return ResponseUtils.error(HttpStatus.NOT_FOUND,
                    "Token de verificación inválido o expirado");
            }

            TokenDriverDTO tokenDTO = tokenService.findByToken(token);
            if (tokenDTO == null) {
                return ResponseUtils.error(HttpStatus.NOT_FOUND, "Token de verificación no encontrado");
            }

            // Activar usuario (validación en UserService)
            userService.activateUser(tokenDTO.getIdUsuario());
            
            UserDTO usuario = userService.findById(tokenDTO.getIdUsuario());

            // Eliminar token usado
            tokenService.deleteByToken(token);

                Map<String, Object> data = new HashMap<>();
                data.put("email", usuario.getCorreo());

                return ResponseUtils.ok(data, "Se verificó el correo " + usuario.getCorreo());

        } catch (IllegalArgumentException e) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception error) {
                return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al verificar email");
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

                Map<String, Object> data = new HashMap<>();
                data.put("user", Map.of(
                    "id", user.getIdUsuario(),
                    "correo", user.getCorreo(),
                    "tipoUsuario", user.getTipoUsuario(),
                    "activo", user.getActivo()));

                return ResponseUtils.ok(data, "Inicio de sesión exitoso");

        } catch (IllegalArgumentException e) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception error) {
                return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al iniciar sesión");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String token,
            HttpServletResponse response) {
        try {
            // Obtener el refreshToken de las cookies
            if (token == null) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "No se proporcionó refresh token");
            }

            // Verificar refreshToken JWT
            Map<String, Object> payload = jwtUtils.verifyRefreshToken(token);

            // Ya no verificamos si payload es null porque verifyRefreshToken ahora lanza excepción
            // Buscar usuario usando UserService
            Integer userId = (Integer) payload.get("id");
            UserDTO user = userService.findById(userId);

            if (user == null || Boolean.FALSE.equals(user.getActivo())) {
                return ResponseUtils.error(HttpStatus.FORBIDDEN, "Usuario no válido o inactivo");
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
            responseBody.put("accessToken", newAccessToken);

            return ResponseUtils.ok(responseBody, "Nuevo access token generado");

        } catch (io.jsonwebtoken.JwtException e) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED,
                    "Token inválido o expirado: " + e.getMessage());
        } catch (RuntimeException e) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception error) {
            System.err.println("[REFRESH TOKEN ERROR] " + error.getMessage());
                return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al refrescar token");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, Object> requestBody) {
        try {
            String correo = (String) requestBody.get("correo");
            if (correo == null || correo.trim().isEmpty()) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "El campo correo es requerido");
            }

            // Buscar usuario por correo usando UserService
            UserDTO usuario;
            try {
                usuario = userService.findByCorreo(correo);
            } catch (RuntimeException e) {
                return ResponseUtils.error(HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }

            if (Boolean.TRUE.equals(usuario.getActivo())) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                    "El usuario ya está verificado");
            }

            // Eliminar tokens de verificación antiguos del usuario
            tokenService.deleteByUsuario(usuario.getIdUsuario());

            // Generar nuevo token de verificación usando TokenService
            TokenDriverDTO TokenDriverDTO = tokenService.createToken(usuario.getIdUsuario(), 1);

            // Enviar correo de verificación
            emailService.sendVerificationEmail(correo, TokenDriverDTO.getToken());

            return ResponseUtils.ok(Map.of(), "Correo de verificación reenviado");

        } catch (RuntimeException e) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception error) {
                return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al reenviar correo de verificación");
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

            return ResponseUtils.ok(Map.of(), "Sesión cerrada exitosamente");

        } catch (Exception error) {
            System.err.println("[LOGOUT ERROR] " + error.getMessage());
                return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al cerrar sesión");
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@CookieValue(value = "accessToken", required = false) String token,
            @RequestBody Map<String, Object> requestBody) {
        try {
            if (token == null) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "No autenticado");
            }

            Map<String, Object> payload = jwtUtils.verifyAccessToken(token);
            Integer userId = (Integer) payload.get("id");

            if (userId == null) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "ID de usuario no encontrado en token");
            }

            UserDTO user = userService.findById(userId);

            String nombre = (String) requestBody.get("nombre");
            String telefono = (String) requestBody.get("telefono");
            String direccion = (String) requestBody.get("direccion");
            String documento = (String) requestBody.get("documento");
            String correo = (String) requestBody.get("correo");

            if (correo != null) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                        "El correo no puede modificarse desde este endpoint");
            }

            boolean noUpdates = (nombre == null && telefono == null && direccion == null && documento == null);
            if (noUpdates) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "No se proporcionaron campos para actualizar");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getIdUsuario());
            response.put("correo", user.getCorreo());
            response.put("tipoUsuario", user.getTipoUsuario());
            response.put("activo", user.getActivo());

            if (user.getTipoUsuario() == TipoUsuario.cliente) {
                ClientDTO updateDTO = new ClientDTO();
                updateDTO.setIdUsuario(userId);
                updateDTO.setNombre(nombre);
                updateDTO.setTelefono(telefono);
                updateDTO.setDireccion(direccion);

                ClientDTO updated = clientService.updateForExistingUser(userId, updateDTO);

                Map<String, Object> clienteInfo = new HashMap<>();
                clienteInfo.put("nombre", updated.getNombre());
                clienteInfo.put("telefono", updated.getTelefono());
                clienteInfo.put("direccion", updated.getDireccion());
                response.put("cliente", clienteInfo);

            } else if (user.getTipoUsuario() == TipoUsuario.empleado) {
                EmpleadoDTO updateDTO = new EmpleadoDTO();
                updateDTO.setIdUsuario(userId);
                updateDTO.setNombre(nombre);
                updateDTO.setTelefono(telefono);
                updateDTO.setDocumento(documento);

                EmpleadoDTO updated = empleadoService.updatePersonalInfo(userId, updateDTO);

                Map<String, Object> empleadoInfo = new HashMap<>();
                empleadoInfo.put("nombre", updated.getNombre());
                empleadoInfo.put("telefono", updated.getTelefono());
                empleadoInfo.put("documento", updated.getDocumento());
                empleadoInfo.put("cargo", updated.getCargo());
                response.put("empleado", empleadoInfo);

            } else {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "Tipo de usuario no soportado");
            }

            return ResponseUtils.ok(response, "Información personal actualizada correctamente");

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        } catch (RuntimeException e) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception error) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al actualizar información personal");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@CookieValue(value = "accessToken", required = false) String token,
            @RequestBody Map<String, Object> requestBody) {
        try {
            if (token == null) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "No autenticado");
            }

            Map<String, Object> payload = jwtUtils.verifyAccessToken(token);
            Integer userId = (Integer) payload.get("id");

            if (userId == null) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "ID de usuario no encontrado en token");
            }

            String contrasenaActual = (String) requestBody.get("contrasenaActual");
            String nuevaContrasena = (String) requestBody.get("nuevaContrasena");
            String confirmarContrasena = (String) requestBody.get("confirmarContrasena");

            userService.changePassword(userId, contrasenaActual, nuevaContrasena, confirmarContrasena);

            return ResponseUtils.ok(Map.of(), "Contraseña actualizada correctamente");

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        } catch (RuntimeException e) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception error) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al cambiar contraseña");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, Object> requestBody) {
        try {
            String correo = (String) requestBody.get("correo");
            if (correo == null || correo.trim().isEmpty()) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "El correo es requerido");
            }

            UserDTO usuario = userService.findByCorreo(correo);
            TokenDriverDTO tokenDTO = tokenService.createToken(usuario.getIdUsuario(), 1);
            emailService.sendPasswordResetEmail(correo, tokenDTO.getToken());

            return ResponseUtils.ok(Map.of(), "Se envió el correo para restablecer la contraseña");

        } catch (RuntimeException e) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception error) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al enviar correo de recuperación");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> requestBody) {
        try {
            String token = (String) requestBody.get("token");
            String nuevaContrasena = (String) requestBody.get("nuevaContrasena");
            String confirmarContrasena = (String) requestBody.get("confirmarContrasena");

            if (token == null || token.trim().isEmpty()) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "Token de recuperación requerido");
            }

            if (!tokenService.isValidToken(token)) {
                return ResponseUtils.error(HttpStatus.NOT_FOUND, "Token inválido o expirado");
            }

            TokenDriverDTO tokenDTO = tokenService.findByToken(token);
            userService.resetPassword(tokenDTO.getIdUsuario(), nuevaContrasena, confirmarContrasena);

            tokenService.deleteByToken(token);

            return ResponseUtils.ok(Map.of(), "Contraseña restablecida correctamente");

        } catch (RuntimeException e) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception error) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al restablecer contraseña");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@CookieValue(value = "accessToken", required = false) String token) {
        try {
            if (token == null) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "No autenticado");
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
                return ResponseUtils.error(HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }

            // Crear respuesta sin información sensible
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getIdUsuario());
            response.put("correo", user.getCorreo());
            response.put("tipoUsuario", user.getTipoUsuario());
            response.put("activo", user.getActivo());

            return ResponseUtils.ok(response, "Perfil obtenido correctamente");

        } catch (io.jsonwebtoken.JwtException e) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED,
                    "Token inválido o expirado: " + e.getMessage());
        } catch (RuntimeException e) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception error) {
                return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al obtener perfil");
        }
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) {
        try {
            boolean exists = userService.existsByCorreo(email);
            return ResponseUtils.ok(Map.of("exists", exists), "Verificación completada");
        } catch (Exception error) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error al verificar email");
        }
    }
}