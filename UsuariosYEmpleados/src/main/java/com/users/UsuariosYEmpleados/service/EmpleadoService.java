package com.users.UsuariosYEmpleados.service;

import com.users.UsuariosYEmpleados.domain.entity.Empleado;
import com.users.UsuariosYEmpleados.domain.entity.Usuario;
import com.users.UsuariosYEmpleados.domain.repositories.EmpleadoRepository;
import com.users.UsuariosYEmpleados.dto.EmpleadoDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final com.users.UsuariosYEmpleados.domain.repositories.UsuarioRepository usuarioRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository,
                           com.users.UsuariosYEmpleados.domain.repositories.UsuarioRepository usuarioRepository) {
        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ========== Métodos de Búsqueda ==========

    public List<EmpleadoDTO> findAll() {
        return empleadoRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmpleadoDTO findById(Integer id) {
        return convertToDTO(requireEmpleado(id));
    }

    public EmpleadoDTO findByDocumento(String documento) {
        Empleado empleado = empleadoRepository.findByDocumento(documento)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con documento: " + documento));
        return convertToDTO(empleado);
    }

    public EmpleadoDTO findByTelefono(String telefono) {
        Empleado empleado = empleadoRepository.findByTelefono(telefono)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con teléfono: " + telefono));
        return convertToDTO(empleado);
    }

    public EmpleadoDTO findByUsuarioId(Integer idUsuario) {
        Empleado empleado = empleadoRepository.findByIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado para usuario id: " + idUsuario));
        return convertToDTO(empleado);
    }

    public List<EmpleadoDTO> findByCargo(String cargo) {
        return empleadoRepository.findByCargo(cargo)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EmpleadoDTO> findByNombre(String nombre) {
        return empleadoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== Métodos de Verificación ==========

    public boolean existsByDocumento(String documento) {
        return empleadoRepository.existsByDocumento(documento);
    }

    public boolean existsByTelefono(String telefono) {
        return empleadoRepository.findByTelefono(telefono).isPresent();
    }

    // ========== Métodos CRUD ==========

    public EmpleadoDTO create(Empleado empleado) {
        // Validar documento único
        if (empleado.getDocumento() != null &&
                empleadoRepository.existsByDocumento(empleado.getDocumento())) {
            throw new RuntimeException("Ya existe un empleado con el documento: " + empleado.getDocumento());
        }

        // Validar teléfono único
        if (empleado.getTelefono() != null &&
                empleadoRepository.findByTelefono(empleado.getTelefono()).isPresent()) {
            throw new RuntimeException("Ya existe un empleado con el teléfono: " + empleado.getTelefono());
        }

        // Asegurar que el usuario sea de tipo empleado y esté gestionado
        if (empleado.getIdUsuario() != null) {
            // Buscamos la entidad gestionada por Hibernate
            Usuario managedUsuario = usuarioRepository.findById(empleado.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            managedUsuario.setTipoUsuario(TipoUsuario.empleado);
            // El idUsuario ya está establecido en el empleado
        } else {
            throw new RuntimeException("El empleado debe tener un idUsuario válido");
        }

        Empleado savedEmpleado = empleadoRepository.save(empleado);
        return convertToDTO(savedEmpleado);
    }

    public EmpleadoDTO update(Integer id, EmpleadoDTO empleadoDTO) {
        // Verificar existencia
        Empleado existingEmpleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));

        // Validar documento único (si cambia)
        if (empleadoDTO.getDocumento() != null &&
                !empleadoDTO.getDocumento().equals(existingEmpleado.getDocumento()) &&
                empleadoRepository.existsByDocumento(empleadoDTO.getDocumento())) {
            throw new RuntimeException("El documento ya está en uso por otro empleado");
        }

        // Validar teléfono único (si cambia)
        if (empleadoDTO.getTelefono() != null &&
                !empleadoDTO.getTelefono().equals(existingEmpleado.getTelefono()) &&
                empleadoRepository.findByTelefono(empleadoDTO.getTelefono()).isPresent()) {
            throw new RuntimeException("El teléfono ya está en uso por otro empleado");
        }

        // Actualizar campos
        if (empleadoDTO.getCargo() != null) {
            existingEmpleado.setCargo(empleadoDTO.getCargo());
        }
        if (empleadoDTO.getNombre() != null) {
            existingEmpleado.setNombre(empleadoDTO.getNombre());
        }
        if (empleadoDTO.getDocumento() != null) {
            existingEmpleado.setDocumento(empleadoDTO.getDocumento());
        }
        if (empleadoDTO.getTelefono() != null) {
            existingEmpleado.setTelefono(empleadoDTO.getTelefono());
        }

        Empleado updatedEmpleado = empleadoRepository.save(existingEmpleado);
        return convertToDTO(updatedEmpleado);
    }

    public void delete(Integer id) {
        Empleado empleado = requireEmpleado(id);
        empleadoRepository.delete(empleado);
    }

    // ========== Métodos de Negocio ==========

    public List<EmpleadoDTO> buscarPorCargoONombre(String busqueda) {
        // Buscar por cargo
        List<Empleado> porCargo = empleadoRepository.findByCargo(busqueda);

        // Buscar por nombre
        List<Empleado> porNombre = empleadoRepository.findByNombreContainingIgnoreCase(busqueda);

        // Combinar y eliminar duplicados
        List<Empleado> resultados = porCargo;
        for (Empleado emp : porNombre) {
            if (!resultados.contains(emp)) {
                resultados.add(emp);
            }
        }

        return resultados.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long countByCargo(String cargo) {
        return empleadoRepository.findByCargo(cargo).size();
    }

    public List<String> getAllCargos() {
        return empleadoRepository.findAll()
                .stream()
                .map(Empleado::getCargo)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ========== Métodos de Conversión ==========

    private EmpleadoDTO convertToDTO(Empleado empleado) {
        // Obtener datos del usuario asociado
        Usuario usuario = usuarioRepository.findById(empleado.getIdUsuario())
                .orElse(null);
        
        String correo = usuario != null ? usuario.getCorreo() : null;
        Boolean activo = usuario != null ? usuario.getActivo() : null;
        
        return new EmpleadoDTO(
            empleado.getIdUsuario(),
            empleado.getCargo(),
            empleado.getNombre(),
            empleado.getDocumento(),
            empleado.getTelefono(),
            correo,
            activo
        );
    }

    private Empleado requireEmpleado(Integer id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
    }
}