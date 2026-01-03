package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.domain.dto.ClientDTO;
import com.users.UsuariosYEmpleados.domain.dto.ClientEnrichedDTO;
import com.users.UsuariosYEmpleados.service.ClientService;
import com.users.UsuariosYEmpleados.util.JwtUtils;
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
    public ResponseEntity<List<ClientDTO>> getAll() {
        return ResponseEntity.ok(clientService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(clientService.findById(id));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> getByNombre(@RequestParam String nombre) {
        try {
            List<ClientDTO> clientes = clientService.findByNombre(nombre);
            return ResponseEntity.ok(clientes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/enriquecido")
    public ResponseEntity<List<ClientEnrichedDTO>> getAllEnriched() {
        return ResponseEntity.ok(clientService.findAllEnriched());
    }

    @GetMapping("/enriquecido/{id}")
    public ResponseEntity<?> getByIdEnriched(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(clientService.findByIdEnriched(id));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{idUsuario}")
    public ResponseEntity<?> createForUser(@PathVariable Integer idUsuario,
                                           @RequestBody ClientDTO body) {
        try {
            ClientDTO created = clientService.createForExistingUser(idUsuario, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al crear cliente"));
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<?> createClient(@CookieValue(value = "accessToken", required = false) String token,
                                          @RequestBody ClientDTO body) {
        try {
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "No autenticado"));
            }
            
            Map<String, Object> payload = jwtUtils.verifyAccessToken(token);
            Integer idUsuario = (Integer) payload.get("id");

            if (idUsuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "ID de usuario no encontrado en token"));
            }
            
            ClientDTO created = clientService.createForExistingUser(idUsuario, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token inválido o expirado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al crear cliente"));
        }
    }
}
