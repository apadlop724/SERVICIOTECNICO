# Servicio Técnico - Gestión de Averías

Esta es una aplicación Android diseñada para la gestión integral de un servicio técnico de reparación de electrodomésticos. La aplicación facilita la interacción entre los clientes y el servicio técnico, permitiendo un seguimiento detallado de cada reparación desde su registro hasta su cierre.

## 🚀 Características

### Para Clientes
- **Registro y Autenticación**: Acceso seguro mediante número de teléfono y contraseña.
- **Gestión de Averías**: Registro de incidencias especificando el electrodoméstico (Frigorífico, Lavadora, Horno, etc.), la marca y los síntomas.
- **Historial Personal**: Consulta de todas las averías enviadas y visualización de su estado actual (*Pendiente* o *Reparado*).
- **Contacto Rápido**: Botón de llamada directa para contactar con el técnico.

### Para Administradores
- **Gestión de Clientes**: Búsqueda de clientes por teléfono, creación de nuevas cuentas, edición de datos y eliminación.
- **Control de Reparaciones**:
    - Acceso a un listado global de todas las **averías pendientes**.
    - Actualización de estados de reparación.
    - Gestión de datos económicos: asignación de costes internos e importes finales para el cliente.
    - Registro de soluciones técnicas para cada caso.
- **Comunicación**: Capacidad de llamar al cliente directamente desde la ficha de la avería.

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Java
- **Plataforma**: Android SDK (compatibilidad con Material Design y Edge-to-Edge).
- **Comunicación en Red**: [Volley](https://github.com/google/volley) para el intercambio de datos con el servidor mediante peticiones POST y GET.
- **Formato de Datos**: JSON para la recepción de historiales y búsqueda de registros.
- **Backend**: Servidor remoto con PHP y base de datos MySQL (alojado en `serviciotecnicosevilla.com`).

## 📋 Requisitos e Instalación

1.  Clonar el repositorio o descargar el código fuente.
2.  Abrir el proyecto en **Android Studio**.
3.  Sincronizar el proyecto con los archivos de Gradle para instalar la dependencia de Volley.
4.  Asegurarse de que el dispositivo o emulador tenga acceso a Internet.
5.  Compilar y ejecutar la aplicación.

## 📂 Estructura de Clases Principales

- **`MainActivity.java`**: Punto de entrada que gestiona el login, el registro de clientes y el panel de control del administrador.
- **`AveriaActivity.java`**: Gestiona el formulario detallado de una avería. Permite insertar, actualizar y eliminar registros, controlando la visibilidad de campos sensibles (como costes) según el rol del usuario.
- **`activity_historial.java`**: Implementa listas dinámicas para mostrar las averías de un cliente específico o todas las reparaciones pendientes de la base de datos.

## 🗄️ Base de Datos (MySQL)

Consulta SQL para inicializar el sistema:

```sql
-- 1. Crear la tabla de Clientes
CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    telefono VARCHAR(20) UNIQUE,
    email VARCHAR(100),
    contrasena VARCHAR(8) 
) ENGINE=InnoDB;

-- 2. Crear la tabla de Averías
CREATE TABLE averia (
    id_averia INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT,
    electrodomestico VARCHAR(50),
    marca VARCHAR(50),
    sintoma TEXT,
    solucion TEXT,
    coste DECIMAL(10,2) DEFAULT 0.00,
    precio DECIMAL(10,2) DEFAULT 0.00,
    estado VARCHAR(20) DEFAULT 'Pendiente',
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_cliente_averia 
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE CASCADE
) ENGINE=InnoDB;
```

## 🌐 Backend (PHP Scripts)

Estos scripts deben alojarse en el directorio `dbst/` de tu servidor. 

### 1. Conexión (`conexion.php`)
Esencial para que todos los demás scripts funcionen.
```php
<?php
$hostname = "tu_host";
$database = "tu_db";
$username = "tu_usuario";
$password = "tu_password";
$conexion = new mysqli($hostname, $username, $password, $database);
if ($conexion->connect_errno) {
    echo "Error de conexion";
}
?>
```

### 2. Insertar Cliente (`insertar_cliente.php`)
```php
<?php
include 'conexion.php';
$nombre = $_POST['nombre'];
$direccion = $_POST['direccion'];
$telefono = $_POST['telefono'];
$email = $_POST['email'];
$contrasena = $_POST['contrasena'];

$consulta_existente = "SELECT * FROM cliente WHERE telefono='$telefono'";
$resultado = $conexion->query($consulta_existente);

if($resultado->num_rows > 0){
    echo "El telefono ya esta registrado";
} else {
    $consulta = "INSERT INTO cliente (nombre, direccion, telefono, email, contrasena) VALUES ('$nombre', '$direccion', '$telefono', '$email', '$contrasena')";
    if($conexion->query($consulta)){
        echo "Operacion exitosa";
    }
}
?>
```

### 3. Buscar Cliente (`buscar_cliente.php`)
```php
<?php
include 'conexion.php';
$telefono = $_GET['telefono'];
$consulta = "SELECT * FROM cliente WHERE telefono = '$telefono'";
$resultado = $conexion->query($consulta);
while($fila = $resultado->fetch_array()){
    $cliente[] = array_map('utf8_encode', $fila);
}
echo json_encode($cliente);
$resultado->close();
?>
```

### 4. Insertar Avería (`insertar_averia.php`)
```php
<?php
include 'conexion.php';
$id_cliente = $_POST['id_cliente'];
$electrodomestico = $_POST['electrodomestico'];
$marca = $_POST['marca'];
$sintoma = $_POST['sintoma'];
$fecha = $_POST['fecha'];
$estado = $_POST['estado'];

// Opcionales (Admin)
$solucion = isset($_POST['solucion']) ? $_POST['solucion'] : "";
$coste = isset($_POST['coste']) ? $_POST['coste'] : 0;
$precio = isset($_POST['precio']) ? $_POST['precio'] : 0;

$consulta = "INSERT INTO averia (id_cliente, electrodomestico, marca, sintoma, fecha, estado, solucion, coste, precio) 
             VALUES ('$id_cliente', '$electrodomestico', '$marca', '$sintoma', '$fecha', '$estado', '$solucion', '$coste', '$precio')";

if($conexion->query($consulta)){
    echo "Avería registrada correctamente";
}
?>
```

### 5. Actualizar Avería (`actualizar_averia.php`)
```php
<?php
include 'conexion.php';
$id_averia = $_POST['id_averia'];
$electrodomestico = $_POST['electrodomestico'];
$marca = $_POST['marca'];
$sintoma = $_POST['sintoma'];
$solucion = $_POST['solucion'];
$estado = $_POST['estado'];
$coste = $_POST['coste'];
$precio = $_POST['precio'];
$fecha = $_POST['fecha'];

$consulta = "UPDATE averia SET electrodomestico='$electrodomestico', marca='$marca', sintoma='$sintoma', 
             solucion='$solucion', estado='$estado', coste='$coste', precio='$precio', fecha='$fecha' 
             WHERE id_averia='$id_averia'";

if($conexion->query($consulta)){
    echo "Actualizado";
}
?>
```

### 6. Listar Pendientes (`listar_pendientes.php`)
```php
<?php
include 'conexion.php';
$consulta = "SELECT a.*, c.nombre, c.telefono FROM averia a INNER JOIN cliente c ON a.id_cliente = c.id_cliente WHERE a.estado = 'Pendiente' ORDER BY a.fecha ASC";
$resultado = $conexion->query($consulta);
$averias = array();
while($fila = $resultado->fetch_assoc()){
    $averias[] = $fila;
}
echo json_encode($averias);
?>
```

---
*Desarrollado como proyecto intermodular para la gestión de servicios técnicos.*
