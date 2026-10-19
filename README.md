# Minimarket El Puente (versión Java)

Migración de la aplicación VB.NET WinForms `minimarket-1.0.1.5` a **Java 17 + Swing + JDBC (PostgreSQL)**.

## Requisitos
- JDK 17 o superior y Maven
- PostgreSQL con la base `MINIMARKET` y la tabla `productos(codigo, producto, precio)` (ver `sql/schema.sql`)

## Ejecutar
```bash
mvn package
java -jar target/minimarket-1.0.1.5.jar
```

## Configuración de la base de datos
Por defecto usa los mismos valores del original (`postgres` / `root` @ `localhost:5432`, base `MINIMARKET`).
Se pueden cambiar con variables de entorno: `MINIMARKET_URL`, `MINIMARKET_USER`, `MINIMARKET_PASSWORD`.

## Equivalencias
| VB.NET                         | Java                                   |
|--------------------------------|----------------------------------------|
| Conexión.vb (módulo)           | `Conexion`, `ProductoDao`              |
| menu_principal                 | `MenuPrincipal`                        |
| sumar_productos                | `VentaDialog` + `Carrito`              |
| buscar(venta) / buscar_por_nombre | `BuscarPorNombreDialog`             |
| Scan_productos                 | `ScanProductosDialog`                  |
| Cargar Productos               | `CargarProductosDialog`                |
| editar_precios                 | `EditarPreciosDialog`                  |
| Eliminar_productos             | `EliminarProductosDialog`              |
