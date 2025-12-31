package com.users.UsuariosYEmpleados.service;

import com.users.UsuariosYEmpleados.domain.entity.Cliente;
import com.users.UsuariosYEmpleados.domain.entity.Usuario;
import com.users.UsuariosYEmpleados.domain.repositories.ClienteRepository;
import com.users.UsuariosYEmpleados.domain.repositories.UsuarioRepository;
import com.users.UsuariosYEmpleados.dto.ClientDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class clientService {
    
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public clientService(ClienteRepository clienteRepository,
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
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
        return convertToDTO(cliente);
    }
    
    public ClientDTO findByTelefono(String telefono) {
        Cliente cliente = clienteRepository.findByTelefono(telefono)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con teléfono: " + telefono));
        return convertToDTO(cliente);
    }
    
    public List<ClientDTO> findByNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ClientDTO createForExistingUser(Integer idUsuario, ClientDTO clientData) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + idUsuario));

        if (usuario.getTipoUsuario() != TipoUsuario.cliente) {
            throw new RuntimeException("El usuario no es de tipo cliente");
        }

        if (clienteRepository.existsById(idUsuario)) {
            throw new RuntimeException("Ya existe un cliente asociado a este usuario");
        }

        if (clientData.getTelefono() != null && clienteRepository.existsByTelefono(clientData.getTelefono())) {
            throw new RuntimeException("El teléfono ya está registrado para otro cliente");
        }

        Cliente cliente = new Cliente();
        cliente.setIdUsuario(idUsuario);
        cliente.setDireccion(clientData.getDireccion());
        cliente.setNombre(clientData.getNombre());
        cliente.setTelefono(clientData.getTelefono());

        Cliente saved = clienteRepository.save(cliente);
        return convertToDTO(saved);
    }
    
    public ClientDTO save(Cliente cliente) {
        Cliente savedCliente = clienteRepository.save(cliente);
        return convertToDTO(savedCliente);
    }
    
    public void deleteById(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con id: " + id);
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
}