# 🔐 Módulo de Usuarios y Empleados - Don Papa

Microservicio para gestión de usuarios, empleados y clientes del sistema Don Papa.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Requisitos Previos](#requisitos-previos)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [API Endpoints](#api-endpoints)
- [Variables de Entorno](#variables-de-entorno)

## ✨ Características

- ✅ **Autenticación JWT** con tokens de acceso y refresh
- ✅ **Gestión de Usuarios** (Clientes, Empleados, Administradores)
- ✅ **Verificación de Email** mediante tokens
- ✅ **Hash de Contraseñas** con BCrypt
- ✅ **Manejo Global de Errores** con respuestas consistentes
- ✅ **Seguridad** con Spring Security
- ✅ **Variables de Entorno** para configuración sensible

## 🛠 Tecnologías

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security**
- **MySQL 8** (desarrollo) / **SQL Server** (producción)
- **JWT (jjwt 0.13.0)**
- **BCrypt** para hashing de contraseñas
- **Maven** para gestión de dependencias

## 📦 Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- MySQL 8.0+ (para desarrollo local)
- Variables de entorno configuradas (ver `.env.example`)

## ⚙️ Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/Don-Papa-Organization/Backend-Don-Papa.git
cd Backend-Don-Papa/ms2-user-service/UsuariosYEmpleados
```

### 2. Configurar Variables de Entorno

Copiar el archivo de ejemplo y configurar:

```bash
cp .env.example .env
```

**IMPORTANTE:** Editar `.env` con valores reales (ver sección [Variables de Entorno](#variables-de-entorno))

### 3. Crear Base de Datos

```sql
CREATE DATABASE don_papa;
```

### 4. Compilar el Proyecto

```bash
mvn clean install
```

## 🚀 Ejecución

### Modo Desarrollo

```bash
mvn spring-boot:run
```

### Con Variables de Entorno

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=don_papa
export DB_USERNAME=root
export DB_PASSWORD=tu_password
export JWT_SECRET=tu_secreto_jwt_muy_largo_y_seguro_minimo_32_caracteres
export JWT_REFRESH_SECRET=tu_secreto_refresh_jwt_muy_largo_y_seguro_32_caracteres

mvn spring-boot:run
```

### Modo Producción

```bash
mvn clean package
java -jar target/UsuariosYEmpleados-0.0.1-SNAPSHOT.jar
```

El servicio estará disponible en: `http://localhost:4002`

## 📁 Estructura del Proyecto

```
src/main/java/com/users/UsuariosYEmpleados/
├── config/                    # Configuraciones de seguridad y JWT
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
├── controller/                # Controladores REST
│   ├── AuthController.java   # Autenticación (login, register, logout)
│   ├── UserController.java   # Gestión de usuarios
│   ├── ClientController.java # Gestión de clientes
│   └── EmpleadoController.java # Gestión de empleados
├── domain/
│   ├── entity/               # Entidades JPA
│   │   ├── Usuario.java
│   │   ├── Cliente.java
│   │   ├── Empleado.java
│   │   └── ManejadorTokens.java
│   └── repositories/         # Repositorios JPA
│       ├── UsuarioRepository.java
│       ├── ClienteRepository.java
│       ├── EmpleadoRepository.java
│       └── ManejadorTokensRepository.java
├── dto/                      # Data Transfer Objects
│   ├── UserDTO.java
│   ├── CreateUserDTO.java
│   ├── UpdateUserDTO.java
│   ├── UserResponseDTO.java
│   ├── ClientDTO.java
│   ├── ClientEnrichedDTO.java
│   ├── EmpleadoDTO.java
│   ├── CrearEmpleadoDTO.java
│   └── TokenDriverDTO.java
├── enums/                    # Enumeraciones
│   └── TipoUsuario.java
├── exception/                # Excepciones personalizadas y handlers
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   └── InvalidCredentialsException.java
├── service/                  # Lógica de negocio
│   ├── UserService.java
│   ├── ClientService.java
│   ├── EmpleadoService.java
│   ├── TokenService.java
│   └── apis/
│       └── EmailService.java
└── util/                     # Utilidades
    ├── BCryptUtils.java
    └── JwtUtils.java
```

## 🌐 API Endpoints

### Autenticación (`/api/auth`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/register` | Registrar nuevo usuario | No |
| POST | `/login` | Iniciar sesión | No |
| POST | `/logout` | Cerrar sesión | Sí |
| GET | `/verify-email?token=` | Verificar email | No |
| POST | `/resend-verification` | Reenviar email verificación | No |
| POST | `/refresh-token` | Refrescar access token | Sí |
| GET | `/profile` | Obtener perfil usuario | Sí |
| GET | `/check-email/{email}` | Verificar si email existe | No |

### Usuarios (`/api/usuarios`)

| Método | Endpoint | Descripción | Auth | Rol |
|--------|----------|-------------|------|-----|
| GET | `/` | Listar todos los usuarios | Sí | Admin |
| GET | `/{id}` | Obtener usuario por ID | Sí | Admin |
| GET | `/correo/{correo}` | Obtener usuario por correo | Sí | Admin |

### Clientes (`/api/clientes`)

| Método | Endpoint | Descripción | Auth | Rol |
|--------|----------|-------------|------|-----|
| GET | `/` | Listar todos los clientes | Sí | Admin |
| GET | `/{id}` | Obtener cliente por ID | Sí | Admin |
| GET | `/buscar?nombre=` | Buscar clientes por nombre | Sí | Admin |
| GET | `/enriquecido` | Listar clientes con datos usuario | Sí | Admin |
| GET | `/enriquecido/{id}` | Cliente enriquecido por ID | Sí | Admin |
| POST | `/{idUsuario}` | Crear cliente para usuario existente | Sí | Admin |
| POST | `/crear` | Crear cliente (usuario actual) | Sí | Cliente |

### Empleados (`/api/empleados`)

| Método | Endpoint | Descripción | Auth | Rol |
|--------|----------|-------------|------|-----|
| GET | `/` | Listar todos los empleados | Sí | Admin |
| GET | `/{id}` | Obtener empleado por ID | Sí | Admin |
| GET | `/documento/{documento}` | Obtener por documento | Sí | Admin/Empleado |
| GET | `/cargo/{cargo}` | Listar por cargo | Sí | Admin |
| GET | `/buscar?nombre=` | Buscar por nombre | Sí | Admin |
| GET | `/verificar-documento/{doc}` | Verificar si documento existe | Sí | Admin |
| POST | `/` | Crear nuevo empleado | Sí | Admin |
| POST | `/completo` | Crear empleado completo | Sí | Admin |
| PUT | `/{id}` | Actualizar empleado | Sí | Admin |
| DELETE | `/{id}` | Eliminar/desactivar empleado | Sí | Admin |

## 🔐 Variables de Entorno

### Base de Datos (Requeridas)

```env
DB_HOST=localhost                  # Host de la base de datos
DB_PORT=3306                       # Puerto de la base de datos
DB_NAME=don_papa                   # Nombre de la base de datos
DB_USERNAME=root                   # Usuario de la base de datos
DB_PASSWORD=password_seguro        # Contraseña de la base de datos
```

### Seguridad (Requeridas)

```env
JWT_SECRET=secreto_jwt_minimo_32_caracteres_muy_seguro
JWT_REFRESH_SECRET=secreto_refresh_minimo_32_caracteres_muy_seguro
```

**IMPORTANTE:** 
- Los secretos JWT deben tener al menos 32 caracteres
- Deben ser únicos y aleatorios
- NO usar valores por defecto en producción
- Generar con: `openssl rand -base64 32`

### Opcionales (con valores por defecto)

```env
SERVER_PORT=4002                   # Puerto del servidor
LOG_LEVEL=INFO                     # Nivel de logging
SPRING_PROFILES_ACTIVE=development # Perfil activo
APP_SECURITY_BCRYPT_ROUNDS=10      # Rondas BCrypt
EMAIL_SERVICE_URL=http://email-service:4007  # URL servicio email
```

## 📝 Notas Importantes

### Seguridad

- ✅ Las contraseñas se hashean con BCrypt (10 rounds)
- ✅ Los tokens JWT expiran en 15 minutos (access) / 7 días (refresh)
- ✅ Las cookies son HttpOnly y Secure en producción
- ✅ Las contraseñas deben tener min. 8 caracteres, 1 mayúscula, 1 minúscula, 1 número

### Base de Datos

- ✅ Auto-creación de tablas con Hibernate (ddl-auto: update)
- ✅ Soporte para MySQL (desarrollo) y SQL Server (producción)
- ✅ Pool de conexiones configurado con HikariCP

### Mejoras Implementadas

- ✅ Excepciones personalizadas con manejo global
- ✅ DTOs separados para entrada/salida
- ✅ Logging centralizado (reemplaza System.out)
- ✅ Variables sensibles externalizadas
- ✅ Servicios renombrados según convenciones Java
- ✅ Packages corregidos
- ✅ UserService implementado

## 🐛 Solución de Problemas

### Error: "JWT_SECRET must be set"
Configurar las variables de entorno JWT_SECRET y JWT_REFRESH_SECRET

### Error: "Access denied"
Verificar que el usuario tiene el rol correcto para el endpoint

### Error: "Connection refused"
Verificar que la base de datos está corriendo y las credenciales son correctas

### Error: "Token expired"
El access token expiró, usar el endpoint `/refresh-token` con el refreshToken

## 📄 Licencia

Este proyecto es parte del sistema Don Papa - Todos los derechos reservados

## 👥 Contacto

Para más información, contactar al equipo de desarrollo.
