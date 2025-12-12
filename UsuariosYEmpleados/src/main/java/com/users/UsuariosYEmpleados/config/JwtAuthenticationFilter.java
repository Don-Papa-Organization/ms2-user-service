package com.users.UsuariosYEmpleados.config;

import com.users.UsuariosYEmpleados.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // 1. Obtener el token de las cookies (Mirroring logic: const cookieHeader =
        // req.headers.cookie...)
        // En Java HttpServletRequest, es más seguro y estándar usar getCookies()
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            // 2. Verificar el token
            try {
                Claims claims = jwtUtils.verifyAccessToken(token);

                if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Extraer información del payload (claims)
                    Integer userId = claims.get("id", Integer.class);
                    String tipoUsuario = claims.get("tipoUsuario", String.class);

                    // Crear lista de autoridades basada en tipoUsuario
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (tipoUsuario != null) {
                        // Agregar rol en formato Spring Security: ROLE_TIPOUSUARIO
                        String rol = "ROLE_" + tipoUsuario.toUpperCase();
                        authorities.add(new SimpleGrantedAuthority(rol));
                    }

                    // 3. Crear objeto de autenticación
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId != null ? userId.toString() : "unknown", null, authorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 4. Establecer en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (io.jsonwebtoken.JwtException e) {
                // Token inválido, expirado o mal formado: responder con 401 y detener filtro
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + e.getMessage() + "\"}");
                return; // Importante: detener la cadena de filtros aquí
            }
        }

        // Continúa el filtro (si no hubo token o fue inválido, el usuario seguirá como
        // anónimo)
        filterChain.doFilter(request, response);
    }
}
