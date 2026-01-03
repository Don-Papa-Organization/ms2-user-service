package com.users.UsuariosYEmpleados.service;

import com.users.UsuariosYEmpleados.domain.entity.Cliente;
import com.users.UsuariosYEmpleados.domain.entity.Usuario;
import com.users.UsuariosYEmpleados.domain.repositories.ClienteRepository;
import com.users.UsuariosYEmpleados.domain.repositories.UsuarioRepository;
import com.users.UsuariosYEmpleados.domain.dto.ClientDTO;
import com.users.UsuariosYEmpleados.domain.dto.ClientEnrichedDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientService {
    
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ClientService(ClienteRepository clienteRepository,
                         UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    public List<ClientDTO> findAll() {
        return clienteRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ClientDTO findById(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));
        return convertToDTO(cliente);
    }
    
    public ClientDTO findByTelefono(String telefono) {
        Cliente cliente = clienteRepository.findByTelefono(telefono)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con teléfono: " + telefono));
        return convertToDTO(cliente);
    }
    
    public List<ClientDTO> findByNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido para la búsqueda");
        }
        return clienteRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ClientEnrichedDTO> findAllEnriched() {
        return clienteRepository.findAll()
                .stream()
                .map(this::convertToEnrichedDTO)
                .collect(Collectors.toList());
    }

    public ClientEnrichedDTO findByIdEnriched(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));
        return convertToEnrichedDTO(cliente);
    }

    public ClientDTO createForExistingUser(Integer idUsuario, ClientDTO clientData) {
        // Validaciones de entrada
        validateClientData(clientData);
        
        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + idUsuario));

        // Validar tipo de usuario
        if (usuario.getTipoUsuario() != TipoUsuario.cliente) {
            throw new IllegalArgumentException("El usuario no es de tipo cliente");
        }

        // Validar que no exista cliente duplicado
        if (clienteRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException("Ya existe un cliente asociado a este usuario");
        }

        // Validar teléfono único
        if (clientData.getTelefono() != null && clienteRepository.existsByTelefono(clientData.getTelefono())) {
            throw new IllegalArgumentException("El teléfono ya está registrado para otro cliente");
        }

        Cliente cliente = new Cliente();
        cliente.setIdUsuario(idUsuario);
        cliente.setDireccion(clientData.getDireccion());
        cliente.setNombre(clientData.getNombre());
        cliente.setTelefono(clientData.getTelefono());

        Cliente saved = clienteRepository.save(cliente);
        return convertToDTO(saved);
    }

    // ========== VALIDACIONES PRIVADAS ==========

    private void validateClientData(ClientDTO clientData) {
        if (clientData == null) {
            throw new IllegalArgumentException("Los datos del cliente son requeridos");
        }
        if (clientData.getDireccion() == null || clientData.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección es requerida");
        }
        if (clientData.getNombre() == null || clientData.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        if (clientData.getTelefono() == null || clientData.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es requerido");
        }
    }
    
    public ClientDTO save(Cliente cliente) {
        Cliente savedCliente = clienteRepository.save(cliente);
        return convertToDTO(savedCliente);
    }
    
    public void deleteById(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }
    
    public boolean existsByTelefono(String telefono) {
        return clienteRepository.existsByTelefono(telefono);
    }
    
    private ClientDTO convertToDTO(Cliente cliente) {
        // Obtener datos del usuario asociado
        Usuario usuario = usuarioRepository.findById(cliente.getIdUsuario())
                .orElse(null);
        
        String correo = usuario != null ? usuario.getCorreo() : null;
        Boolean activo = usuario != null ? usuario.getActivo() : null;
        
        return new ClientDTO(
            cliente.getIdUsuario(),
            cliente.getDireccion(),
            cliente.getNombre(),
            cliente.getTelefono(),
            correo,
            activo
        );
    }

    private ClientEnrichedDTO convertToEnrichedDTO(Cliente cliente) {
        Usuario usuario = usuarioRepository.findById(cliente.getIdUsuario())
                .orElse(null);
        
        String correo = usuario != null ? usuario.getCorreo() : null;
        TipoUsuario tipoUsuario = usuario != null ? usuario.getTipoUsuario() : null;
        Boolean activo = usuario != null ? usuario.getActivo() : null;
        
        return new ClientEnrichedDTO(
            cliente.getIdUsuario(),
            cliente.getDireccion(),
            cliente.getNombre(),
            cliente.getTelefono(),
            correo,
            tipoUsuario,
            activo
        );
    }
}