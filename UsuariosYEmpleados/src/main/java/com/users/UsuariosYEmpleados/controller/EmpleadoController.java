package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.domain.entity.Empleado;
import com.users.UsuariosYEmpleados.dto.ApiResponse;
import com.users.UsuariosYEmpleados.dto.UserDTO;
import com.users.UsuariosYEmpleados.dto.EmpleadoDTO;
import com.users.UsuariosYEmpleados.dto.CrearEmpleadoDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import com.users.UsuariosYEmpleados.service.EmpleadoService;
import com.users.UsuariosYEmpleados.service.UserService;
import com.users.UsuariosYEmpleados.util.BCryptUtils;
import com.users.UsuariosYEmpleados.util.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final UserService userService;
    private final BCryptUtils bCryptUtils;

    public EmpleadoController(EmpleadoService empleadoService,
            UserService userService,
            BCryptUtils bCryptUtils) {
        this.empleadoService = empleadoService;
        this.userService = userService;
        this.bCryptUtils = bCryptUtils;
    }

    // Obtener todos los empleados (solo admin)
    @GetMapping
    public ResponseEntity<?> getEmpleados(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipoUsuario,
            @RequestParam(required = false) String estado) {

        try {
            if (tipoUsuario != null && !"empleado".equalsIgnoreCase(tipoUsuario.trim())) {
                return ResponseUtils.ok(List.of(), "No hay empleados para el tipo de usuario solicitado");
            }

            final Boolean estadoFiltro = estado != null ? "true".equalsIgnoreCase(estado.trim()) : null;

            List<Map<String, Object>> resultado = empleadoService.search(nombre, estadoFiltro).stream()
                    .map(emp -> {
                        Map<String, Object> item = new HashMap<>();

                        // Información del empleado
                        item.put("idUsuario", emp.getIdUsuario());
                        item.put("nombre", emp.getNombre());
                        item.put("documento", emp.getDocumento());
                        item.put("telefono", emp.getTelefono());
                        item.put("cargo", emp.getCargo());
                        item.put("correo", emp.getCorreo());
                        item.put("activo", emp.getActivo());

                        // Información del usuario (si está disponible)
                        Map<String, Object> usuarioInfo = new HashMap<>();
                        usuarioInfo.put("correo", emp.getCorreo());
                        usuarioInfo.put("activo", emp.getActivo());

                        item.put("usuario", usuarioInfo);

                        return item;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(resultado, "Empleados cargados correctamente"));

        } catch (Exception error) {
            System.err.println("[GET empleados ERROR] " + error.getMessage());
            error.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener empleados: " + error.getMessage()));
        }
    }

    // Obtener empleado por id (solo admin)
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmpleadoById(@PathVariable Integer id) {
        try {
            // TODO: Validar autenticación y permisos

            EmpleadoDTO empleado = empleadoService.findById(id);

            if (empleado == null) {
                return ResponseUtils.error(HttpStatus.NOT_FOUND, "Empleado no encontrado");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", empleado.getIdUsuario());
            response.put("nombre", empleado.getNombre());
            response.put("documento", empleado.getDocumento());
            response.put("telefono", empleado.getTelefono());
            response.put("cargo", empleado.getCargo());
            response.put("correo", empleado.getCorreo());
            response.put("activo", empleado.getActivo());

            // Información del usuario
            
            response.put("correo", empleado.getCorreo());
            response.put("activo", empleado.getActivo());

            

            return ResponseEntity.ok(ApiResponse.success(response, "Empleado cargado correctamente"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[GET empleado BY ID ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener empleado"));
        }
    }

    // Obtener empleado por documento
    @GetMapping("/documento/{documento}")
    public ResponseEntity<?> getEmpleadoByDocumento(@PathVariable String documento) {
        try {
            EmpleadoDTO empleado = empleadoService.findByDocumento(documento);

            Map<String, Object> response = new HashMap<>();
            response.put("id", empleado.getIdUsuario());
            response.put("nombre", empleado.getNombre());
            response.put("documento", empleado.getDocumento());
            response.put("telefono", empleado.getTelefono());
            response.put("cargo", empleado.getCargo());
            response.put("correo", empleado.getCorreo());
            response.put("activo", empleado.getActivo());

            // Información del usuario
            Map<String, Object> usuarioInfo = new HashMap<>();
            usuarioInfo.put("correo", empleado.getCorreo());
            usuarioInfo.put("activo", empleado.getActivo());

            response.put("usuario", usuarioInfo);

            return ResponseEntity.ok(ApiResponse.success(response, "Empleado cargado correctamente"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[GET empleado BY DOCUMENTO ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener empleado por documento"));
        }
    }

    // Crear nuevo empleado (solo admin)
    @PostMapping
    public ResponseEntity<?> createEmpleado(@RequestBody Map<String, Object> requestBody) {
        try {
            // TODO: Validar autenticación y permisos de admin

            // Validar campos obligatorios
            String nombre = (String) requestBody.get("nombre");
            String documento = (String) requestBody.get("documento");
            String correo = (String) requestBody.get("correo");
            String telefono = (String) requestBody.get("telefono");
            String cargo = (String) requestBody.get("cargo");
            String contrasena = (String) requestBody.get("contrasena");

            if (nombre == null || documento == null || correo == null ||
                    cargo == null || contrasena == null) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                    "Faltan campos obligatorios: nombre, documento, correo, cargo, contraseña");
            }

            // Validar duplicados: correo en Usuario
            if (userService.existsByCorreo(correo)) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                    "Ya existe un usuario con ese correo");
            }

            // Validar documento duplicado
            if (empleadoService.existsByDocumento(documento)) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                    "Ya existe un empleado con ese documento");
            }

            // Hash de contraseña
            String hashed = bCryptUtils.hashPassword(contrasena);

            // 1. Crear el usuario usando UserService
            UserDTO userDTO = new UserDTO(
                    null, // id será generado
                    correo,
                    hashed,
                    TipoUsuario.empleado,
                    true);

            UserDTO usuarioCreado = userService.create(userDTO);

            // 2. Crear el empleado
            // Primero necesitamos crear la entidad Empleado
            Empleado empleado = new Empleado();
            empleado.setNombre(nombre);
            empleado.setDocumento(documento);
            empleado.setTelefono(telefono);
            empleado.setCargo(cargo);

            // Crear y asociar el usuario (usando solo el idUsuario)
            empleado.setIdUsuario(usuarioCreado.getIdUsuario());

            // Guardar empleado usando el servicio
            EmpleadoDTO empleadoCreado = empleadoService.create(empleado);

            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", empleadoCreado.getIdUsuario());
            response.put("idUsuario", empleadoCreado.getIdUsuario());
            response.put("nombre", empleadoCreado.getNombre());
            response.put("documento", empleadoCreado.getDocumento());
            response.put("telefono", empleadoCreado.getTelefono());
            response.put("cargo", empleadoCreado.getCargo());
            response.put("correo", usuarioCreado.getCorreo());
            response.put("activo", usuarioCreado.getActivo());

            Map<String, Object> usuarioResponse = new HashMap<>();
            usuarioResponse.put("correo", usuarioCreado.getCorreo());
            usuarioResponse.put("tipoUsuario", usuarioCreado.getTipoUsuario());
            usuarioResponse.put("activo", usuarioCreado.getActivo());

            response.put("usuario", usuarioResponse);
            response.put("message", "Empleado creado exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Empleado creado exitosamente"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[CREATE empleado ERROR] " + error.getMessage());
            error.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al crear empleado"));
        }
    }

    // Versión alternativa usando EmpleadoDTO
    @PostMapping("/completo")
    public ResponseEntity<?> createEmpleadoCompleto(@RequestBody CrearEmpleadoDTO request) {
        try {
            // Validar campos obligatorios
            if (request.getNombre() == null || request.getDocumento() == null ||
                    request.getCorreo() == null || request.getCargo() == null ||
                    request.getContrasena() == null) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST, "Faltan campos obligatorios");
            }

            // Validar duplicados
            if (userService.existsByCorreo(request.getCorreo())) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                    "Ya existe un usuario con ese correo");
            }

            if (empleadoService.existsByDocumento(request.getDocumento())) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                    "Ya existe un empleado con ese documento");
            }

            // Hash de contraseña
            String hashed = bCryptUtils.hashPassword(request.getContrasena());

            // Crear usuario
            UserDTO UserDTO = new UserDTO();
            UserDTO.setCorreo(request.getCorreo());
            UserDTO.setContrasena(hashed);
            UserDTO.setTipoUsuario(TipoUsuario.empleado);
            UserDTO.setActivo(true);

            UserDTO usuarioCreado = userService.create(UserDTO);

            // Crear empleado
            Empleado empleado = new Empleado();
            empleado.setNombre(request.getNombre());
            empleado.setDocumento(request.getDocumento());
            empleado.setTelefono(request.getTelefono());
            empleado.setCargo(request.getCargo());

            // Establecer la relación con el usuario usando solo el idUsuario
            empleado.setIdUsuario(usuarioCreado.getIdUsuario());

            EmpleadoDTO empleadoCreado = empleadoService.create(empleado);

            // Preparar respuesta simplificada
            Map<String, Object> response = new HashMap<>();
            response.put("empleadoId", empleadoCreado.getIdUsuario());
            response.put("correo", empleadoCreado.getCorreo());

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Empleado creado exitosamente"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[CREATE empleado COMPLETO ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al crear empleado"));
        }
    }

    // Actualizar empleado (solo admin)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmpleado(@PathVariable Integer id, @RequestBody Map<String, Object> requestBody) {
        try {
            // TODO: Validar autenticación y permisos de admin
            // TODO: Obtener userId actual del contexto de seguridad

            // Verificar que el empleado existe
            EmpleadoDTO empleadoExistente = empleadoService.findById(id);

            // Validar campos a actualizar
            String nombre = (String) requestBody.get("nombre");
            String documento = (String) requestBody.get("documento");
            String correo = (String) requestBody.get("correo");
            String telefono = (String) requestBody.get("telefono");
            String cargo = (String) requestBody.get("cargo");
            String tipoUsuarioStr = (String) requestBody.get("tipoUsuario");
            String contrasena = (String) requestBody.get("contrasena");

            // TODO: Evitar que el admin se cambie su propio rol
            // Integer currentUserId = obtener del contexto de seguridad
            // if (currentUserId.equals(id) && tipoUsuarioStr != null) { ... }

            // Validar correo duplicado (si se cambia)
            if (correo != null && !correo.equals(empleadoExistente.getCorreo())) {
                // Buscar usuario por correo usando UserService
                try {
                    UserDTO usuarioConCorreo = userService.findByCorreo(correo);
                    if (usuarioConCorreo != null && !usuarioConCorreo.getIdUsuario().equals(id)) {
                        return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                            "El correo ya está en uso por otro usuario");
                    }
                } catch (RuntimeException e) {
                    // Si no encuentra usuario con ese correo, está bien
                }
            }

            // Validar documento duplicado (si se cambia)
            if (documento != null && !documento.equals(empleadoExistente.getDocumento())) {
                try {
                    EmpleadoDTO empleadoConDocumento = empleadoService.findByDocumento(documento);
                    if (empleadoConDocumento != null && !empleadoConDocumento.getIdUsuario().equals(id)) {
                        return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                            "El documento ya está en uso por otro empleado");
                    }
                } catch (RuntimeException e) {
                    // Si no encuentra empleado con ese documento, está bien
                }
            }

            // Actualizar información del empleado
            EmpleadoDTO updateDTO = new EmpleadoDTO();
            updateDTO.setIdUsuario(id);
            if (nombre != null)
                updateDTO.setNombre(nombre);
            if (documento != null)
                updateDTO.setDocumento(documento);
            if (telefono != null)
                updateDTO.setTelefono(telefono);
            if (cargo != null)
                updateDTO.setCargo(cargo);

            EmpleadoDTO empleadoActualizado = empleadoService.update(id, updateDTO);

            // Actualizar información del usuario (si es necesario)
            if (correo != null || tipoUsuarioStr != null || contrasena != null) {
                UserDTO userUpdateDTO = new UserDTO();
                userUpdateDTO.setIdUsuario(id);

                if (correo != null)
                    userUpdateDTO.setCorreo(correo);
                if (tipoUsuarioStr != null) {
                    try {
                        userUpdateDTO.setTipoUsuario(TipoUsuario.valueOf(tipoUsuarioStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                            "Tipo de usuario inválido");
                    }
                }
                if (contrasena != null) {
                    userUpdateDTO.setContrasena(bCryptUtils.hashPassword(contrasena));
                }

                UserDTO usuarioActualizado = userService.update(id, userUpdateDTO);
            }

            // Obtener los datos actualizados completos
            EmpleadoDTO empleadoFinal = empleadoService.findById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("id", empleadoFinal.getIdUsuario());
            response.put("nombre", empleadoFinal.getNombre());
            response.put("documento", empleadoFinal.getDocumento());
            response.put("telefono", empleadoFinal.getTelefono());
            response.put("cargo", empleadoFinal.getCargo());
            response.put("correo", empleadoFinal.getCorreo());
            response.put("activo", empleadoFinal.getActivo());

            Map<String, Object> usuarioInfo = new HashMap<>();
            usuarioInfo.put("correo", empleadoFinal.getCorreo());
            usuarioInfo.put("activo", empleadoFinal.getActivo());

            response.put("usuario", usuarioInfo);
            response.put("message", "Empleado actualizado correctamente");

            return ResponseEntity.ok(ApiResponse.success(response, "Empleado actualizado correctamente"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[UPDATE empleado ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al actualizar empleado: " + error.getMessage()));
        }
    }

    // Eliminar/desactivar empleado (solo admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmpleado(@PathVariable Integer id) {
        try {
            // TODO: Validar autenticación y permisos de admin
            // TODO: Obtener userId actual del contexto de seguridad

            // Verificar que el empleado existe
            EmpleadoDTO empleado = empleadoService.findById(id);

            // TODO: Evitar que el admin se elimine a sí mismo
            // Integer currentUserId = obtener del contexto de seguridad
            // if (currentUserId.equals(id)) { ... }

            // Desactivar usuario primero
            UserDTO userUpdateDTO = new UserDTO();
            userUpdateDTO.setIdUsuario(id);
            userUpdateDTO.setActivo(false);

            userService.update(id, userUpdateDTO);

            // Eliminar empleado
            empleadoService.delete(id);

            return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Empleado eliminado/desactivado correctamente", "empleadoId", id), "Empleado eliminado"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception error) {
            System.err.println("[DELETE empleado ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al eliminar empleado: " + error.getMessage()));
        }
    }

    // Verificar si documento existe
    @GetMapping("/verificar-documento/{documento}")
    public ResponseEntity<?> verificarDocumento(@PathVariable String documento) {
        try {
            boolean existe = empleadoService.existsByDocumento(documento);
            return ResponseEntity.ok(ApiResponse.success(Map.of("existe", existe), "Verificación completada"));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al verificar documento"));
        }
    }

    // Obtener empleados por cargo
    @GetMapping("/cargo/{cargo}")
    public ResponseEntity<?> getEmpleadosByCargo(@PathVariable String cargo) {
        try {
            List<EmpleadoDTO> empleados = empleadoService.findByCargo(cargo);

            List<Map<String, Object>> resultado = empleados.stream()
                    .map(emp -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", emp.getIdUsuario());
                        item.put("nombre", emp.getNombre());
                        item.put("documento", emp.getDocumento());
                        item.put("telefono", emp.getTelefono());
                        item.put("cargo", emp.getCargo());

                        Map<String, Object> usuarioInfo = new HashMap<>();
                        usuarioInfo.put("correo", emp.getCorreo());
                        usuarioInfo.put("activo", emp.getActivo());

                        item.put("usuario", usuarioInfo);
                        return item;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(resultado, "Empleados por cargo cargados correctamente"));

        } catch (Exception error) {
            System.err.println("[GET empleadoS BY CARGO ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener empleados por cargo"));
        }
    }

    // Buscar empleados por nombre
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarEmpleados(@RequestParam String nombre) {
        try {
            List<EmpleadoDTO> empleados = empleadoService.findByNombre(nombre);

            List<Map<String, Object>> resultado = empleados.stream()
                    .map(emp -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", emp.getIdUsuario());
                        item.put("nombre", emp.getNombre());
                        item.put("documento", emp.getDocumento());
                        item.put("telefono", emp.getTelefono());
                        item.put("cargo", emp.getCargo());

                        Map<String, Object> usuarioInfo = new HashMap<>();
                        usuarioInfo.put("correo", emp.getCorreo());
                        usuarioInfo.put("activo", emp.getActivo());

                        item.put("usuario", usuarioInfo);
                        return item;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(resultado, "Búsqueda de empleados completada"));

        } catch (Exception error) {
            System.err.println("[BUSCAR empleadoS ERROR] " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al buscar empleados"));
        }
    }
}