package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.dto.ApiResponse;
import com.users.UsuariosYEmpleados.dto.ClientDTO;
import com.users.UsuariosYEmpleados.service.clientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final clientService clientService;

    public ClienteController(clientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/enriquecido")
    public ResponseEntity<ApiResponse<List<ClientDTO>>> getAllClientesEnriquecidos() {
        List<ClientDTO> clientes = clientService.findAll();
        return ResponseEntity.ok(ApiResponse.success(clientes, "Clientes enriquecidos cargados correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientDTO>> getClienteById(@PathVariable Integer id) {
        ClientDTO cliente = clientService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(cliente, "Cliente cargado correctamente"));
    }
}
