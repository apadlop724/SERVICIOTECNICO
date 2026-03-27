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

### 1. Conexión (`config.php`)
Esencial para que todos los demás scripts funcionen.
```php
<?php
$hostname = "tu_host";
$database = "tu_db";
$username = "tu_usuario";
$password = "tu_password";
// Configurar mysqli para que lance excepciones en caso de error (PHP 8+)
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

try {
    $conexion = new mysqli($hostname, $username, $password, $database);
    $conexion->set_charset("utf8");
} catch (mysqli_sql_exception $e) {
    die("Fallo al conectar a MySQL: " . $e->getMessage());
}
?>
```

### 2. Insertar Cliente (`insertar_cliente.php`)
```php
<?php
include 'config.php';

$nombre = $_POST['nombre'];
$direccion = $_POST['direccion'];
$telefono = $_POST['telefono'];
$email = $_POST['email'];
$contrasena = $_POST['contrasena'];

// Verificamos que el teléfono no esté vacío
if(empty($telefono)){
    echo "El telefono es obligatorio";
    exit();
}

$consulta = "INSERT INTO cliente (nombre, direccion, telefono, email, contrasena) VALUES (?, ?, ?, ?, ?)";
$stmt = $conexion->prepare($consulta);

if (!$stmt) {
    echo "Error en la consulta SQL: " . $conexion->error;
    exit();
}

$stmt->bind_param("sssss", $nombre, $direccion, $telefono, $email, $contrasena);

if ($stmt->execute()) {
    echo "Operacion exitosa";
} else {
    // Si el error es 1062, significa que el teléfono ya existe
    if ($stmt->errno == 1062) {
        echo "El telefono ya esta registrado";
    } else {
        echo "Error al insertar cliente";
    }
}

$stmt->close();
$conexion->close();
?>
```

### 3. Buscar Cliente (`buscar_cliente.php`)
```php
<?php
include 'config.php';

// Recogemos el teléfono de la URL
$telefono = $_GET['telefono'];

// Buscamos solo al cliente que coincida con ese teléfono
//$consulta = "SELECT * FROM cliente WHERE telefono = ?";

// Buscamos el cliente y los datos de su última avería (si existe)
$consulta = "SELECT c.*, a.id_averia, a.electrodomestico, a.marca, a.sintoma, a.solucion, a.coste, a.precio, a.estado, a.fecha 
             FROM cliente c 
             LEFT JOIN averia a ON c.id_cliente = a.id_cliente 
             WHERE c.telefono = ? 
             ORDER BY a.id_averia DESC LIMIT 1";

$stmt = $conexion->prepare($consulta);
$stmt->bind_param("s", $telefono);
$stmt->execute();

$resultado = $stmt->get_result();

$cliente = array();
while ($fila = $resultado->fetch_assoc()) {
    $cliente[] = $fila;
}

// Enviamos el resultado a Android
echo json_encode($cliente);

$stmt->close();
$conexion->close();
?>
```
### 4. Actualizar cliente (`editar_cliente.php`)
```php
<?php
include 'config.php';

// Recibimos los datos de Android
$telefono_antiguo = $_POST['telefono_antiguo'];
$telefono_nuevo = $_POST['telefono_nuevo'];
$nombre = $_POST['nombre'];
$direccion = $_POST['direccion'];
$email = $_POST['email'];
$contrasena = $_POST['contrasena']; 

// Validar si el telefono nuevo ya existe (solo si es diferente al antiguo)
if ($telefono_nuevo != $telefono_antiguo) {
    $checkQuery = "SELECT id_cliente FROM cliente WHERE telefono = ?";
    $stmtCheck = $conexion->prepare($checkQuery);
    $stmtCheck->bind_param("s", $telefono_nuevo);
    $stmtCheck->execute();
    $stmtCheck->store_result();
    if ($stmtCheck->num_rows > 0) {
        echo "El telefono ya existe";
        $stmtCheck->close();
        exit();
    }
    $stmtCheck->close();
}

// SQL: Actualizamos los datos buscando por el teléfono antiguo, y actualizamos también el teléfono
$consulta = "UPDATE cliente SET nombre=?, direccion=?, email=?, contrasena=?, telefono=? WHERE telefono=?";
$stmt = $conexion->prepare($consulta);

if (!$stmt) {
    echo "Error en la consulta SQL: " . $conexion->error;
    exit();
}

$stmt->bind_param("ssssss", $nombre, $direccion, $email, $contrasena, $telefono_nuevo, $telefono_antiguo);

if ($stmt->execute()) {
// Verificamos si realmente se encontró el teléfono y se editó algo
    if ($stmt->affected_rows > 0) {
        echo "Cliente editado";
    } else {
        echo "No se realizaron cambios o el telefono no existe";
    }
} else {
    echo "Error al procesar la edicion";
}
$stmt->close();
$conexion->close();
?>
```

### 5. Eliminar cliente (`eliminar_cliente.php`)
```php
<?php
include 'config.php';

$telefono = $_POST['telefono'];

if (empty($telefono)) {
    echo "Debe proporcionar un telefono";
    exit();
}

// Preparamos la eliminación
$consulta = "DELETE FROM cliente WHERE telefono = ?";
$stmt = $conexion->prepare($consulta);
$stmt->bind_param("s", $telefono);

if ($stmt->execute()) {
    // Verificamos si realmente se borró algo (si el teléfono existía)
    if ($stmt->affected_rows > 0) {
        echo "Cliente eliminado";
    } else {
        echo "No se encontró ningún cliente con ese telefono";
    }
} else {
    echo "Error al intentar eliminar el cliente";
}

$stmt->close();
$conexion->close();
?>
```



### 6. Insertar Avería (`insertar_averia.php`)
```php
<?php
include 'config.php';

// Validación en el servidor
if(!isset($_POST['id_cliente']) || !isset($_POST['sintoma'])){
    echo "Error: Datos insuficientes para el registro";
    exit;
}

$id_cliente = $_POST['id_cliente'];
$electro = $_POST['electrodomestico'];
$marca = $_POST['marca'];
$sintoma = $_POST['sintoma'];
$fecha = $_POST['fecha'];

// Si el admin no envió estos datos, asignamos valores seguros
$solucion = $_POST['solucion'] ?? "";
$coste = $_POST['coste'] ?? 0.0;
$precio = $_POST['precio'] ?? 0.0;
$estado = $_POST['estado'] ?? "Pendiente";

if(!empty($fecha)){
    // Si la fecha tiene 10 caracteres (AAAA-MM-DD), le añadimos la hora
    if(strlen($fecha) == 10){
        $fecha = $fecha . " 00:00:00";
    }
} else {
    // Si viene vacía, ponemos la fecha y hora actual del servidor
    $fecha = date("Y-m-d H:i:s");
}


$consulta = "INSERT INTO averia (id_cliente, electrodomestico, marca, sintoma, solucion, coste, precio, estado, fecha) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
$stmt = $conexion->prepare($consulta);
$stmt->bind_param("issssddss", $id_cliente, $electro, $marca, $sintoma, $solucion, $coste, $precio, $estado, $fecha);

if ($stmt->execute()) {
    echo "Registro de averia exitoso";
} else {
    echo "Error de base de datos: " . $stmt->error;
}

$stmt->close();
$conexion->close();
?>
```

### 7. Actualizar Avería (`actualizar_averia.php`)
```php
<?php
include 'config.php';

// Recogemos los datos enviados por POST
$id_averia = $_POST['id_averia'];
$electro   = $_POST['electrodomestico'];
$marca     = $_POST['marca'];
$sintoma   = $_POST['sintoma'];
$solucion  = $_POST['solucion'];
$estado    = $_POST['estado'];
$coste     = $_POST['coste'];
$precio    = $_POST['precio'];
$fecha     = $_POST['fecha'];

// Preparamos la consulta de actualización
$sql = "UPDATE averia SET 
        electrodomestico = ?, 
        marca = ?, 
        sintoma = ?, 
        solucion = ?, 
        estado = ?, 
        coste = ?, 
        precio = ?, 
        fecha = ? 
        WHERE id_averia = ?";

$stmt = $conexion->prepare($sql);

// "sssssddsi" indica los tipos: s (string), d (double/decimal), i (integer)
$stmt->bind_param("sssssddsi", $electro, $marca, $sintoma, $solucion, $estado, $coste, $precio, $fecha, $id_averia);

if ($stmt->execute()) {
    echo "Actualizado";
} else {
    echo "Error: " . $stmt->error;
}

$stmt->close();
$conexion->close();
?>
```
### 8. Eliminar averia (`eliminar_averia.php`)
```php
<?php
include 'config.php';

$id_averia = $_POST['id_averia'];

// Preparamos el borrado
$sql = "DELETE FROM averia WHERE id_averia = ?";

$stmt = $conexion->prepare($sql);
$stmt->bind_param("i", $id_averia);

if ($stmt->execute()) {
    echo "Eliminado";
} else {
    echo "Error al eliminar";
}

$stmt->close();
$conexion->close();
?>
```

### 9. Listar averias Pendientes (`listar_pendientes.php`)
```php
<?php
include 'config.php';

// Consulta global: buscamos por estado, no por cliente
$consulta = "SELECT a.*, c.id_cliente, c.nombre, c.telefono 
             FROM averia a 
             INNER JOIN cliente c ON a.id_cliente = c.id_cliente 
             WHERE a.estado = 'Pendiente' 
             ORDER BY a.fecha ASC"; // ASC para ver las más antiguas primero

$resultado = mysqli_query($conexion, $consulta);

$pendientes = array();
while($fila = mysqli_fetch_assoc($resultado)){
    $pendientes[] = $fila;
}

echo json_encode($pendientes);
mysqli_close($conexion);
?>
```

### 10. Listar averias de un cliente (`listar_averias.php`)
```php
<?php
include 'config.php';
$id_cliente = $_GET['id_cliente'];

// $consulta = "SELECT id_averia, fecha, electrodomestico, estado, marca, sintoma, solucion, coste, precio FROM averia WHERE id_cliente = '$id_cliente' ORDER BY fecha DESC";

$consulta = "SELECT a.*, c.id_cliente, c.nombre, c.telefono 
             FROM averia a 
             INNER JOIN cliente c ON a.id_cliente = c.id_cliente 
             WHERE a.id_cliente = '$id_cliente' 
             ORDER BY a.fecha DESC";

$resultado = mysqli_query($conexion, $consulta);

$averias = array();
while($fila = mysqli_fetch_assoc($resultado)){
    $averias[] = $fila;
}

echo json_encode($averias);
mysqli_close($conexion);
?>
```

---
*Desarrollado como proyecto intermodular para la gestión de servicios técnicos.*
