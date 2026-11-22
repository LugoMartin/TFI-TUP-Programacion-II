# Concesionaria Multimarcas — TFI Programación II (UTN FRRe)

Sistema de gestión de stock para una concesionaria de automóviles, camionetas y motocicletas,
desarrollado con Java 17+ y Programación Orientada a Objetos.

## Arquitectura

El proyecto separa estrictamente diseño y negocio:

```
src/com/concesionaria/
├── Main.java                 # Punto de entrada y composición de dependencias
├── modelo/                   # Entidades y reglas propias del dominio
│   ├── Vehiculo.java         # Clase abstracta, Serializable, POJO/JavaBean
│   ├── Automovil.java
│   ├── Camioneta.java
│   ├── Motocicleta.java
│   ├── enums/                # CondicionVehiculo, TipoCarroceria, TipoMotocicleta
│   ├── interfaces/           # Cotizable, Mantenible
│   └── excepciones/          # DatoInvalidoException, VehiculoDuplicadoException,
│                             # VehiculoNoEncontradoException
├── persistencia/             # RepositorioGenerico<T, ID> y VehiculoArchivoDAO (CSV)
├── servicio/                 # ConcesionariaService: inventario, validaciones y filtros
└── vista/                    # ConsolaHelper y MenuConsola
```

- La capa `vista` solo conoce `ConcesionariaService`; nunca accede a `persistencia`.
- La capa `servicio` nunca imprime por consola: devuelve resultados y lanza excepciones propias.
- `Main` actúa como *composition root*: inyecta el DAO concreto en el servicio.

## Reglas de negocio

- Un vehículo **NUEVO** debe tener **0 km**; uno **USADO**, **más de 0 km**.
- El año de fabricación debe estar entre 1900 y el año actual + 1.
- Precio base positivo; obligatorios identificador, marca, modelo y color.
- Automóviles y camionetas requieren carrocería; motocicletas requieren tipo y cilindrada.
- Precio final: IVA 10 % para nuevos y arancel 3 % para usados, más recargos por
  carrocería (SUV/PICKUP 5 %, PICKUP/UTILITARIO en camionetas 8 %) o cilindrada
  mayor a 500 cc (7 %).
- Service: cada 10 000 km para autos y camionetas usados; cada 5 000 km para motos.

## Persistencia

`VehiculoArchivoDAO` graba el stock en `stock_vehiculos.csv` (el archivo se crea
automáticamente). Cada línea tiene 11 campos separados por `;`:

```
identificador;tipo;marca;modelo;año;color;kilometraje;precioBase;condicion;detalle;cilindrada
```

## Compilación y ejecución

Requiere **JDK 17 o superior**.

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem src -Recurse -Filter *.java).FullName
java -cp out com.concesionaria.Main
```

Opcionalmente se puede indicar otra ruta de archivo:

```powershell
java -cp out com.concesionaria.Main "C:\datos\mi_stock.csv"
```

## Funcionalidades del menú

1. Alta de vehículo (automóvil, camioneta o motocicleta).
2. Baja con confirmación.
3. Modificación conservando el tipo y el identificador.
4. Búsqueda por identificador con precio final y estado de service.
5. Listado general.
6. Reportes: por marca, tipo, condición, rango de años, filtro combinado,
   valor total del inventario, service pendiente y cantidad por condición.
