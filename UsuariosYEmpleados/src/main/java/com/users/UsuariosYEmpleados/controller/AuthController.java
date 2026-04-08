package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.dto.ApiResponse;
import com.users.UsuariosYEmpleados.dto.UserDTO;
import com.users.UsuariosYEmpleados.dto.TokenDriverDTO;
import com.users.UsuariosYEmpleados.domain.entity.Cliente;
import com.users.UsuariosYEmpleados.domain.repositories.ClienteRepository;
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
    private final ClienteRepository clienteRepository;
    private final emailService emailService;
    private final BCryptUtils bCryptUtils;
    private final JwtUtils jwtUtils;
    private final String nodeEnv;

    public AuthController(UserService userService,
            TokenService tokenService,
            ClienteRepository clienteRepository,
            emailService emailService,
            BCryptUtils bCryptUtils,
            JwtUtils jwtUtils,
            @Value("${spring.profiles.active:development}") String nodeEnv) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.clienteRepository = clienteRepository;
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
                        .body(ApiResponse.success(Map.of(
                                "message",
                                "Usuario registrado correctamente. Revisa tu correo para verificar la cuenta.",
                                "userId", usuarioCreado.getIdUsuario()), "Registro exitoso"));
            } else {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(Map.of("message", "Usuario registrado y activado correctamente.", "userId", usuarioCreado.getIdUsuario()), "Registro exitoso"));
            }

        } catch (RuntimeException e) {
            // Manejar excepciones específicas del servicio
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[REGISTER ERROR] " + error.getMessage());
            error.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno al registrar usuario"));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Token de verificación requerido"));
            }

            // Validar token
            if (!tokenService.isValidToken(token)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Token de verificación inválido o expirado"));
            }

            // Buscar token usando TokenService
            TokenDriverDTO TokenDriverDTO = tokenService.findByToken(token);

            if (TokenDriverDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Token de verificación no encontrado"));
            }

            // Activar usuario (validación en UserService)
            userService.activateUser(tokenDTO.getIdUsuario());
            
            UserDTO usuario = userService.findById(tokenDTO.getIdUsuario());

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuario no encontrado para este token"));
            }

            if (Boolean.TRUE.equals(usuario.getActivo())) {
                return ResponseEntity.ok(ApiResponse.success(Map.of("message", "El usuario ya está verificado"), "Ya verificado"));
            }

            // Actualizar usuario a activo usando UserService
            UserDTO updateDTO = new UserDTO();
            updateDTO.setActivo(true);
            UserDTO updated = userService.update(usuario.getIdUsuario(), updateDTO);

            // Eliminar el token de verificación ya usado
            tokenService.deleteByToken(token);

            return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Se verificó el correo " + usuario.getCorreo(), "email", usuario.getCorreo()), "Verificación exitosa"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[VERIFY EMAIL ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno al verificar email"));
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
                        .body(ApiResponse.error("El usuario no está activo. Por favor verifica tu correo"));
            }

            // Validar contraseña
            // NOTA: userService.findByCorreo no devuelve la contraseña (por seguridad)
            // Necesitamos un método específico para login que sí devuelva la contraseña
            // Por ahora, asumimos que BCryptUtils puede verificar directamente
            boolean isValid = bCryptUtils.comparePassword(contrasena, user.getContrasena());

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Usuario o contraseña incorrectos"));
            }

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

            // Persistir ambos tokens para que /profile y refresh-token puedan validarlos contra DB
            tokenService.createToken(user.getIdUsuario(), 7, accessToken);
            tokenService.createToken(user.getIdUsuario(), 7, refreshToken);

                return ResponseUtils.ok(data, "Inicio de sesión exitoso");

            Map<String, Object> responseBody = new HashMap<>();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getIdUsuario());
            userInfo.put("email", user.getCorreo());
            userInfo.put("tipoUsuario", user.getTipoUsuario());
            userInfo.put("activo", user.getActivo());

            responseBody.put("user", userInfo);

            return ResponseEntity.ok(ApiResponse.success(responseBody, "Inicio de sesión exitoso"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        } catch (Exception error) {
            System.err.println("[LOGIN ERROR] " + error.getMessage());
            error.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno al iniciar sesión"));
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
            boolean tokenValidoEnDB = tokenService.isValidTokenForUsuario(token, userId);
            if (!tokenValidoEnDB) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Refresh token no válido"));
            }

            // Generar nuevo accessToken
            Map<String, Object> newPayload = new HashMap<>();
            newPayload.put("id", user.getIdUsuario());
            newPayload.put("tipoUsuario", user.getTipoUsuario().toString().toLowerCase());
            newPayload.put("activo", user.getActivo());

            String newAccessToken = jwtUtils.generateAccessToken(newPayload);

            // Persistir el access token renovado para que futuras validaciones de /profile lo reconozcan
            tokenService.createToken(user.getIdUsuario(), 7, newAccessToken);

            // Configurar cookie con nuevo accessToken
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure("production".equals(nodeEnv));
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(15 * 60); // 15 minutos
            response.addCookie(accessTokenCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("accessToken", newAccessToken);

            return ResponseEntity.ok(ApiResponse.success(responseBody, "Token refrescado correctamente"));

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token inválido o expirado: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[REFRESH TOKEN ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno al refrescar token"));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, Object> requestBody) {
        try {
            String correo = (String) requestBody.get("correo");
            if (correo == null || correo.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("El campo correo es requerido"));
            }

            // Buscar usuario por correo usando UserService
            UserDTO usuario;
            try {
                usuario = userService.findByCorreo(correo);
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuario no encontrado"));
            }

            if (Boolean.TRUE.equals(usuario.getActivo())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("El usuario ya está verificado"));
            }

            // Eliminar tokens de verificación antiguos del usuario
            tokenService.deleteByUsuario(usuario.getIdUsuario());

            // Generar nuevo token de verificación usando TokenService
            TokenDriverDTO TokenDriverDTO = tokenService.createToken(usuario.getIdUsuario(), 1);

            // Enviar correo de verificación
            emailService.sendVerificationEmail(correo, TokenDriverDTO.getToken());

            return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Correo de verificación reenviado"), "Correo enviado"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[RESEND VERIFICATION ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno al reenviar correo de verificación"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
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

            if (accessToken != null) {
                try {
                    tokenService.deleteByToken(accessToken);
                } catch (Exception e) {
                    System.out.println("[LOGOUT] Access token no encontrado en DB: " + accessToken);
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

            return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Sesión cerrada exitosamente"), "Logout exitoso"));

        } catch (Exception error) {
            System.err.println("[LOGOUT ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno al cerrar sesión"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@CookieValue(value = "accessToken", required = false) String token) {
        try {
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("No autenticado"));
            }

            // Verificar token JWT
            Map<String, Object> payload = jwtUtils.verifyAccessToken(token);

            // Ya no verificamos si payload es null

            // Verificar que el token también exista en nuestra base de datos
            Integer userId = (Integer) payload.get("id");
            boolean tokenValidoEnDB = tokenService.isValidTokenForUsuario(token, userId);

            if (!tokenValidoEnDB) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token no válido"));
            }

            // Obtener usuario usando UserService
            Integer userId = (Integer) payload.get("id");
            UserDTO user = userService.findById(userId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuario no encontrado"));
            }

            // Crear respuesta sin información sensible
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getIdUsuario());
            response.put("correo", user.getCorreo());
            response.put("tipoUsuario", user.getTipoUsuario());
            response.put("activo", user.getActivo());

            // Buscar cliente y agregarlo si existe
            Cliente cliente = clienteRepository.findByIdUsuario(userId).orElse(null);
            if (cliente != null) {
                Map<String, Object> clienteData = new HashMap<>();
                clienteData.put("nombre", cliente.getNombre());
                clienteData.put("telefono", cliente.getTelefono());
                clienteData.put("direccion", cliente.getDireccion());
                response.put("cliente", clienteData);
            }

            return ResponseEntity.ok(ApiResponse.success(response, "Perfil cargado correctamente"));

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token inválido o expirado: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[PROFILE ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno al obtener perfil"));
        }
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) {
        try {
            boolean exists = userService.existsByCorreo(email);
            return ResponseEntity.ok(ApiResponse.success(Map.of("exists", exists), "Verificación completada"));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al verificar email"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@CookieValue(value = "accessToken", required = false) String token,
            @RequestBody Map<String, Object> requestBody) {
        try {
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("No autenticado"));
            }

            // Verificar token JWT
            Map<String, Object> payload = jwtUtils.verifyAccessToken(token);

            // Verificar que el token también exista en nuestra base de datos
            Integer userId = (Integer) payload.get("id");
            boolean tokenValidoEnDB = tokenService.isValidTokenForUsuario(token, userId);

            if (!tokenValidoEnDB) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token no válido"));
            }

            // Obtener usuario usando UserService
            UserDTO user = userService.findById(userId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuario no encontrado"));
            }

            // Extraer datos del request
            String nombre = (String) requestBody.get("nombre");
            String telefono = (String) requestBody.get("telefono");
            String direccion = (String) requestBody.get("direccion");

            // Validar que al menos se proporcione un campo
            if (isBlank(nombre) && isBlank(telefono) && isBlank(direccion)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Debe proporcionar al menos nombre, teléfono o dirección"));
            }

            // Buscar cliente existente usando el repositorio
            Cliente cliente = clienteRepository.findByIdUsuario(userId).orElse(null);
            
            if (cliente == null) {
                // Cliente no existe, validar que se proporcionen todos los datos
                if (isBlank(nombre) || isBlank(telefono) || isBlank(direccion)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error("cliente no encontrado. Para crear un nuevo cliente debes proporcionar nombre, teléfono y dirección"));
                }
                cliente = new Cliente(userId, direccion, nombre, telefono);
            } else {
                // Cliente existe, actualizar datos proporcionados
                if (!isBlank(nombre)) {
                    cliente.setNombre(nombre);
                }
                if (!isBlank(telefono)) {
                    cliente.setTelefono(telefono);
                }
                if (!isBlank(direccion)) {
                    cliente.setDireccion(direccion);
                }
            }

            // Guardar cliente
            clienteRepository.save(cliente);

            System.out.println("[UPDATE PROFILE] Perfil actualizado para usuario: " + user.getCorreo());

            // Construir respuesta con perfil completo
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "Perfil actualizado correctamente");
            
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("id", user.getIdUsuario());
            profileData.put("correo", user.getCorreo());
            profileData.put("tipoUsuario", user.getTipoUsuario());
            profileData.put("activo", user.getActivo());
            
            // Agregar datos del cliente
            Map<String, Object> clienteData = new HashMap<>();
            clienteData.put("nombre", cliente.getNombre());
            clienteData.put("telefono", cliente.getTelefono());
            clienteData.put("direccion", cliente.getDireccion());
            profileData.put("cliente", clienteData);
            
            responseBody.put("data", profileData);
            responseBody.put("timestamp", new Date());

            return ResponseEntity.ok(responseBody);

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token inválido o expirado: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[UPDATE PROFILE ERROR] " + error.getMessage());
            error.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno al actualizar perfil"));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }


}