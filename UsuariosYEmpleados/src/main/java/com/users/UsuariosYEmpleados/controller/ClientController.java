package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.domain.dto.ClientDTO;
import com.users.UsuariosYEmpleados.service.ClientService;
import com.users.UsuariosYEmpleados.util.JwtUtils;
import com.users.UsuariosYEmpleados.util.ResponseUtils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClientController {

    private final ClientService clientService;
    private final JwtUtils jwtUtils;

    public ClientController(ClientService clientService, JwtUtils jwtUtils) {
        this.clientService = clientService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo) {
        return ResponseUtils.ok(clientService.search(nombre, activo), "Clientes obtenidos correctamente");
    }

    // Rutas específicas (literales) ANTES que rutas genéricas (PathVariable)
    @GetMapping("/buscar")
    public ResponseEntity<?> getByNombre(@RequestParam String nombre) {
        try {
            List<ClientDTO> clientes = clientService.findByNombre(nombre);
            return ResponseUtils.ok(clientes, "Clientes obtenidos correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/enriquecido")
    public ResponseEntity<?> getAllEnriched(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo) {
        return ResponseUtils.ok(clientService.searchEnriched(nombre, activo),
                "Clientes enriquecidos obtenidos correctamente");
    }

    @GetMapping("/enriquecido/{id}")
    public ResponseEntity<?> getByIdEnriched(@PathVariable Integer id) {
        try {
            return ResponseUtils.ok(clientService.findByIdEnriched(id),
                    "Cliente enriquecido obtenido correctamente");
        } catch (RuntimeException ex) {
            return ResponseUtils.error(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    // Rutas genéricas (PathVariable) DESPUÉS de rutas específicas
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseUtils.ok(clientService.findById(id), "Cliente obtenido correctamente");
        } catch (RuntimeException ex) {
            return ResponseUtils.error(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PostMapping("/{idUsuario}")
    public ResponseEntity<?> createForUser(@PathVariable Integer idUsuario,
                                           @RequestBody ClientDTO body) {
        try {
            ClientDTO created = clientService.createForExistingUser(idUsuario, body);
            return ResponseUtils.created(created, "Cliente creado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception ex) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear cliente");
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<?> createClient(@CookieValue(value = "accessToken", required = false) String token,
                                          @RequestBody ClientDTO body) {
        try {
            if (token == null) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "No autenticado");
            }
            
            Map<String, Object> payload = jwtUtils.verifyAccessToken(token);
            Integer idUsuario = (Integer) payload.get("id");

            if (idUsuario == null) {
                return ResponseUtils.error(HttpStatus.UNAUTHORIZED,
                        "ID de usuario no encontrado en token");
            }
            
            ClientDTO created = clientService.createForExistingUser(idUsuario, body);
            return ResponseUtils.created(created, "Cliente creado correctamente");
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseUtils.error(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception ex) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear cliente");
        }
    }
}
