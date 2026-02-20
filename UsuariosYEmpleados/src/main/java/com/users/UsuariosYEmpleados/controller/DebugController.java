package com.users.UsuariosYEmpleados.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import com.users.UsuariosYEmpleados.util.ResponseUtils;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    @GetMapping("/security-context")
    public ResponseEntity<?> getSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseUtils.ok(Map.of(
                "authenticated", false
            ), "No autenticado");
        }
        
        return ResponseUtils.ok(Map.of(
            "authenticated", true,
            "principal", auth.getPrincipal(),
            "authorities", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()),
            "details", auth.getDetails()
        ), "Contexto de seguridad obtenido");
    }
    
    @GetMapping("/check-role/{role}")
    public ResponseEntity<?> checkRole(@PathVariable String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        boolean hasRole = auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.toUpperCase()));
        
        return ResponseUtils.ok(Map.of(
            "role", role,
            "hasRole", hasRole,
            "authorities", auth != null ? 
                auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()) : "null"
        ), "Verificación de rol completada");
    }
}
