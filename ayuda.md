# Guía de uso — Sistema de Gestión de Concesionaria Multimarcas (TFI Programación II)

Esta guía explica, paso a paso, cómo compilar, iniciar y usar el sistema para administrar el
inventario de vehículos de una concesionaria: **Automóviles**, **Camionetas** y **Motocicletas**,
nuevos (0 km) y usados. Permite dar de alta, baja y modificar unidades, buscar por identificador,
listar el stock y consultar **reportes y filtros** (por marca, tipo, condición, año, valor total e
indicadores de service).

Todo se guarda en un único archivo de texto (`stock_vehiculos.csv`). **No hay base de datos, ni
registro de clientes, ni ventas**: el programa trabaja solo sobre el inventario.

Si buscás la explicación técnica de cómo está construido el programa, consultá el
[README](README.md). Si querés saber **cómo se usa**, este es el archivo correcto.

---

## Índice

1. [Requisitos](#1-requisitos)
2. [Compilar el programa](#2-compilar-el-programa)
3. [Iniciar el programa](#3-iniciar-el-programa)
4. [La pantalla principal](#4-la-pantalla-principal)
5. [Cómo usar cada opción del menú](#5-cómo-usar-cada-opción-del-menú)
6. [Los reportes y filtros (opción 6)](#6-los-reportes-y-filtros-opción-6)
7. [Valores permitidos (tipos, carrocerías y condiciones)](#7-valores-permitidos-tipos-carrocerías-y-condiciones)
8. [Rangos y validaciones](#8-rangos-y-validaciones)
9. [Cómo se calcula el precio final y el service](#9-cómo-se-calcula-el-precio-final-y-el-service)
10. [El archivo de datos](#10-el-archivo-de-datos)
11. [Mensajes frecuentes y qué hacer](#11-mensajes-frecuentes-y-qué-hacer)
12. [Preguntas frecuentes](#12-preguntas-frecuentes)
13. [Verificación rápida](#13-verificación-rápida)

---

## 1. Requisitos

- **Java (JDK) 8 o superior** instalado y disponible en la terminal (el código compila con el
  JDK 8 que trae VS Code; también funciona con JDK 17 o superior).
- Una terminal (PowerShell, CMD, bash, etc.).
- La carpeta del proyecto completa (`src/`, `README.md` y esta guía). No hace falta internet ni
  configuración adicional.

Comprobá que Java esté instalado:

```powershell
java -version
javac -version
```

Si ambos comandos muestran una versión, estás listo. Si `javac` no existe, falta el JDK (el JRE
solo no alcanza para compilar). Esta guía usa como ejemplo `javac 1.8.0_502`.

---

## 2. Compilar el programa

Ubicate en la carpeta raíz del repositorio (donde están `src/`, `README.md`, `ayuda.md` y
`.gitignore`) y ejecutá:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem src -Recurse -Filter *.java).FullName
```

- El comando compila los **18 archivos `.java`** de `src/` y deja los `.class` en `out/`.
- `out/` está excluido por `.gitignore`, así que no se sube al repositorio.
- Si la compilación no muestra mensajes, terminó bien. Si aparece un error, revisá el fuente o
  volvé a la versión original antes de continuar.

Para compilar controlando advertencias:

```powershell
javac -encoding UTF-8 -Xlint:all -d out (Get-ChildItem src -Recurse -Filter *.java).FullName
```

En Linux/macOS podés usar:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

---

## 3. Iniciar el programa

Siempre desde la carpeta raíz del proyecto:

```powershell
java -cp out com.concesionaria.Main
```

> **Importante**: el archivo de stock se busca y se guarda con la **ruta relativa** al directorio
> donde ejecutás el comando. Si iniciás el programa desde otra carpeta, se creará un archivo nuevo
> en esa carpeta y no verás tus vehículos.

Opcionalmente podés indicar otra ruta de archivo como primer argumento:

```powershell
java -cp out com.concesionaria.Main "C:\datos\mi_stock.csv"
```

Al arrancar vas a ver uno de estos dos mensajes:

- `Stock cargado. Vehiculos registrados: N` — el archivo se leyó correctamente (o no existe
  todavía, en cuyo caso `N` es `0`).
- `No se pudo cargar el stock inicial: <motivo>` — el archivo existe pero no se pudo leer: revisá
  el tema **10. El archivo de datos**.

**No se generan vehículos de ejemplo**: si es la primera ejecución, el inventario arranca vacío y
tenés que dar de alta las unidades con la opción `1`.

Después aparece el **MENÚ PRINCIPAL**.

---

## 4. La pantalla principal

```
===== CONCESIONARIA MULTIMARCAS =====
1) Alta de vehiculo
2) Baja de vehiculo
3) Modificacion de vehiculo
4) Busqueda por identificador
5) Listado general
6) Reportes y filtros para directivos
0) Salir
Seleccione una opcion:
```

**Cómo responder**: escribí el número de la opción y presioná **Enter**. Si escribís un valor que
no sea un número entero, el programa vuelve a preguntar con
`Debe ingresar un numero entero valido.`. Si es un número pero no está entre `0` y `6`, muestra
`Opcion invalida. Intente nuevamente.` Nada de esto rompe el programa.

| Opción | Qué hace |
|--------|----------|
| `1` | Da de alta un automóvil, camioneta o motocicleta, dato por dato |
| `2` | Da de baja una unidad por identificador (pide confirmación) |
| `3` | Modifica una unidad existente conservando su tipo y su identificador |
| `4` | Busca una unidad por identificador y muestra su ficha completa |
| `5` | Lista todas las unidades del inventario, ordenadas por identificador |
| `6` | Abre el submenú de reportes y filtros para directivos |
| `0` | Cierra el programa |

Después de cada opción (salvo `0`) aparece `Presione ENTER para continuar...`: apretá **Enter**
para volver al menú. Al salir se muestra
`Gracias por utilizar el sistema de gestion de la concesionaria.`

---

## 5. Cómo usar cada opción del menú

### Opción 1 — Alta de vehículo

Muestra `--- ALTA DE VEHICULO ---` y te guía en este orden:

1. **Tipo de vehículo**: `1` AUTOMOVIL, `2` CAMIONETA, `3` MOTOCICLETA.
2. **Identificador (patente o chasis)**: texto obligatorio y **único** en el inventario.
3. **Marca**, **Modelo** y **Color**: textos obligatorios, elegidos libremente (no hay lista
   cerrada de marcas ni colores).
4. **Año de fabricación**: entero entre `1900` y el año actual + 1.
5. **Kilometraje**: entero mayor o igual a `0`.
   - Si la condición es `NUEVO`, **tenés que escribir `0`** (no se completa automáticamente).
   - Si es `USADO`, **tenés que escribir más de `0`**.
6. **Precio base**: número decimal **mayor que cero**. Usá punto decimal (`32500.75`); con coma
   (`32500,75`) el programa no lo reconoce.
7. **Condición**: `1` NUEVO o `2` USADO.
8. **Dato propio del tipo**:

   | Tipo | Dato que se pide |
   |------|------------------|
   | AUTOMOVIL | Carrocería: `SEDAN`, `HATCHBACK`, `SUV`, `PICKUP` o `UTILITARIO` |
   | CAMIONETA | Carrocería: la misma lista de 5 opciones |
   | MOTOCICLETA | Tipo (`NAKED`, `DEPORTIVA`, `ENDURO`, `SCOOTER`, `CUSTOM`) y cilindrada en cc (entero > 0) |

9. Al terminar muestra `Vehiculo registrado correctamente: <identificador>` y **guarda
   inmediatamente** en el archivo.

Ejemplo de secuencia de respuestas para una camioneta usada:

```
1                 (Alta de vehículo)
2                 (CAMIONETA)
CAM-777           (Identificador)
Ford              (Marca)
Ranger XLT        (Modelo)
2021              (Año)
Gris Plata        (Color)
45000             (Kilometraje)
32500.75          (Precio base, con punto)
2                 (USADO)
4                 (PICKUP)
```

> La lista de carrocerías es la misma para automóviles y camionetas; la numeración siempre arranca
> en `1` y se muestra en pantalla antes de elegir.

### Opción 2 — Baja de vehículo

1. Muestra `--- BAJA DE VEHICULO ---` y pide `Identificador a dar de baja:`.
2. Muestra la ficha de la unidad encontrada.
3. Pregunta `Confirma la baja (s/n):`.
   - Con `s`, `si` o `sí` se elimina y muestra `Vehiculo eliminado correctamente.`
   - Con `n` o `no` muestra `Operacion cancelada.`

> La baja escribe el archivo sin esa unidad. Si te equivocaste, tenés que volver a darla de alta
> con la opción `1`.

### Opción 3 — Modificación de vehículo

1. Pide `Identificador del vehiculo a modificar:`.
2. Muestra la ficha original.
3. Vuelve a pedir **todos** los datos, con el valor anterior entre paréntesis, por ejemplo:
   `Marca (Ford): `, `Anio de fabricacion (2021): `, `Precio base (32500.75): `,
   `Condicion actual: `, `Carroceria: `.
4. Muestra `Vehiculo modificado correctamente.` y guarda el cambio.

> **Ojo**: presionar **Enter** no conserva el valor anterior: los textos vacíos se rechazan con
> `El campo no puede quedar vacio. Intente nuevamente.` Hay que volver a escribir cada dato.
> El **identificador y el tipo no se preguntan**: no se pueden cambiar desde esta opción. Para
> cambiarlos, dá de baja la unidad y cargala de nuevo con otro identificador/tipo.

### Opción 4 — Búsqueda por identificador

Pide `Identificador a buscar: ` y muestra:

```
Vehiculo [identificador=CAM-777, marca=Ford, modelo=Ranger XLT, anio=2021, color=Gris Plata, kilometraje=45000, precioBase=32500.75, condicion=USADO, detalle=PICKUP]
Categoria: Camioneta
Detalle especifico: PICKUP
Precio final: $36153.83
Requiere service: SI
```

> En la ficha se muestra el valor numérico completo (sin formatear); en el ejemplo se redondeó a
> dos decimales para facilitar la lectura.

> La búsqueda es **exacta y distingue mayúsculas de minúsculas**: `cam-777` no encuentra
> `CAM-777`. Si no existe, se muestra
> `Error: No se encontro ningun vehiculo con el identificador: cam-777`.

### Opción 5 — Listado general

Muestra `--- LISTADO GENERAL ---` y una tabla con este encabezado:

```
IDENTIFICADOR   | MARCA        | MODELO       |   ANIO | CONDICION | TIPO         |   PRECIO FINAL
```

Cada fila trae identificador, marca, modelo, año, condición, categoría y precio final; al final
indica `Total de resultados: N`. El orden es alfabético por identificador.

Si el inventario está vacío, muestra:

```
No se encontraron vehiculos para los criterios indicados.
```

### Opción 0 — Salir

Cierra el programa. **No hay un guardado final**: cada alta, baja o modificación ya guardó su
cambio en el archivo. Al salir solo se muestra el mensaje de despedida.

---

## 6. Los reportes y filtros (opción 6)

Al elegir `6` aparece:

```
--- REPORTES Y FILTROS ---
1) Filtrar por marca
2) Filtrar por tipo de vehiculo
3) Filtrar por condicion
4) Filtrar por rango de anio
5) Filtro combinado
6) Valor total del inventario
7) Vehiculos con service pendiente
8) Cantidad por condicion
0) Volver al menu principal
Seleccione un reporte:
```

| Opción | Qué hace |
|--------|----------|
| `1` | Pide `Marca a buscar: `. Busca coincidencias **parciales** y sin distinguir mayúsculas (`ford` encuentra `Ford`) |
| `2` | Pide `Tipo (Automovil/Camioneta/Motocicleta): `. Hay que escribir la categoría exacta (sin tilde y con mayúscula inicial opcional) |
| `3` | Muestra la lista de condiciones (`NUEVO`/`USADO`) y filtra por la elegida |
| `4` | Pide `Anio desde: ` y `Anio hasta: `, ambos ≥ 1900 y `desde ≤ hasta` |
| `5` | Abre el filtro combinado (ver más abajo) |
| `6` | Muestra `Valor total del inventario: $<total>` (suma de los precios finales) |
| `7` | Lista las unidades usadas que ya superaron el kilometraje de service |
| `8` | Muestra `NUEVO: N vehiculo(s)` y `USADO: N vehiculo(s)` |
| `0` | Vuelve al menú principal |

Las opciones `1`, `2`, `3`, `4`, `5` y `7` muestran el mismo formato de tabla que la opción `5`
del menú principal, con `Total de resultados: N` al final. Si no hay coincidencias, muestran
`No se encontraron vehiculos para los criterios indicados.`

### Filtro combinado (opción 5 del submenú)

Respondé estas preguntas en orden:

```
Marca (ENTER para todos):
Tipo (ENTER para todos):
Filtrar por condicion (s/n):
Anio desde (0 para sin limite):
Anio hasta (0 para sin limite):
```

- Marca y tipo: presioná **Enter** para no filtrar por ese criterio.
- Condición: si respondés `s`, aparece la lista `NUEVO`/`USADO`; con `n` se ignora.
- Años: `0` significa "sin límite". **O ponés `0` en ambos o ponés un valor válido en ambos**;
  si dejás uno en `0` y el otro con valor, se muestra
  `Error: Debe indicar el anio desde y el anio hasta.`

---

## 7. Valores permitidos (tipos, carrocerías y condiciones)

Las listas se muestran en pantalla y se eligen con el **número** que aparece al lado. No se
escribe el nombre en esos casos.

### Tipos de vehículo (opción 1, primer paso)

1. AUTOMOVIL · 2. CAMIONETA · 3. MOTOCICLETA

### Carrocerías (para AUTOMOVIL y para CAMIONETA)

1. SEDAN · 2. HATCHBACK · 3. SUV · 4. PICKUP · 5. UTILITARIO

> Aunque la lista es igual para ambos tipos, los recargos de precio son distintos (ver sección 9).

### Tipos de motocicleta

1. NAKED · 2. DEPORTIVA · 3. ENDURO · 4. SCOOTER · 5. CUSTOM

### Condición

1. NUEVO (0 km) · 2. USADO (más de 0 km)

### Respuestas sí/no

Acepta `s`, `si` y `sí` como afirmación; `n` y `no` como negación, sin importar mayúsculas. Si
escribís otra cosa: `Ingrese 's' para afirmar o 'n' para negar.`

---

## 8. Rangos y validaciones

| Dato | Regla |
|------|-------|
| Identificador | Obligatorio (no puede ser solo espacios) y **único** en el inventario; distingue mayúsculas |
| Marca, modelo y color | Obligatorios; texto libre |
| Año de fabricación | Entero entre `1900` y el año actual + 1 (por ejemplo, hasta `2027` si corre en 2026) |
| Precio base | Decimal **mayor que cero**; se usa `.` como separador decimal; se rechazan `NaN` e infinitos |
| Kilometraje | Entero ≥ `0`; exactamente `0` si la condición es `NUEVO`; > `0` si es `USADO` |
| Condición | Obligatoria: `NUEVO` o `USADO` |
| Automóvil / camioneta | Requieren una carrocería de la lista |
| Motocicleta | Requiere un tipo de la lista y cilindrada entera > `0` (sin límite superior) |
| Rango de años (reporte 4) | Ambos ≥ `1900` y `desde ≤ hasta` |
| Filtro combinado (reporte 5) | Años: ambos en `0` o ambos con valor; `desde ≤ hasta` si se indican |
| Confirmaciones (s/n) | `s`, `si`, `sí` = sí; `n`, `no` = no |
| Entries numéricos | Enteros con `Integer.valueOf`; decimales con `Double.valueOf` (no acepta coma) |

Si un dato es inválido, el programa muestra `Error: <motivo>` y volvé al menú: **no se pierde lo
que ya estaba guardado**, pero la operación en curso se cancela y hay que repetirla desde el
principio.

---

## 9. Cómo se calcula el precio final y el service

El precio final se calcula en tres pasos:

1. **Según la condición**:
   - `NUEVO`: precio base × `1.10` (10 % adicional).
   - `USADO`: precio base × `1.03` (3 % adicional).
2. **Recargo por las características del tipo**:

   | Tipo | Cuándo aplica | Recargo adicional |
   |------|---------------|-------------------|
   | AUTOMOVIL | Carrocería `SUV` o `PICKUP` | 5 % |
   | CAMIONETA | Carrocería `PICKUP` o `UTILITARIO` | 8 % |
   | MOTOCICLETA | Cilindrada **mayor** a `500` cc (500 exacto no suma) | 7 % |

3. El resultado es el `Precio final` que se muestra en la ficha, en los listados y en el reporte
   `Valor total del inventario`.

Ejemplo: camioneta usada con precio base `32500.75` y carrocería `PICKUP`:
`32500.75 × 1.03 = 33475.77`, y `33475.7725 × 1.08 = 36153.83` aproximadamente.

El estado de **service** se calcula aparte:

| Tipo | Condición | Umbral |
|------|-----------|--------|
| AUTOMOVIL | Usado | `10000` km o más |
| CAMIONETA | Usado | `10000` km o más |
| MOTOCICLETA | Usado | `5000` km o más |
| Cualquiera | Nuevo | Siempre `NO` |

En la ficha aparece como `Requiere service: SI` o `Requiere service: NO`. El reporte `7` lista
todas las unidades con `SI`.

---

## 10. El archivo de datos

Todo el inventario vive en un único archivo de texto plano:

| Archivo | Contenido | ¿Cuándo se escribe? |
|---------|-----------|---------------------|
| `stock_vehiculos.csv` | Una línea por vehículo, sin línea de encabezado | En cada alta, baja o modificación |

Por defecto se usa `stock_vehiculos.csv` en el directorio desde donde se inició el programa. Se
puede cambiar con el primer argumento de `java` (ver sección 3).

Cada línea tiene **11 campos separados por `;`**, en este orden:

```
identificador;tipo;marca;modelo;anio;color;kilometraje;precioBase;condicion;detalle;cilindrada
```

- `tipo`: `AUTOMOVIL`, `CAMIONETA` o `MOTOCICLETA`.
- `condicion`: `NUEVO` o `USADO` (en mayúsculas, porque son nombres de enum).
- `detalle`: carrocería (`SEDAN`, `HATCHBACK`, `SUV`, `PICKUP`, `UTILITARIO`) o tipo de moto
  (`NAKED`, `DEPORTIVA`, `ENDURO`, `SCOOTER`, `CUSTOM`).
- `cilindrada`: solo se usa en motocicletas; en autos y camionetas queda vacía (la línea termina
  con `;`).

Ejemplo de dos líneas válidas:

```
AUT-101;AUTOMOVIL;Fiat;Cronos;2024;Rojo;0;10000.0;NUEVO;SEDAN;
MOT-005;MOTOCICLETA;Honda;CB500;2022;Negro;6000;6000.0;USADO;NAKED;500
```

Recomendaciones:

- **Hacé una copia de respaldo** antes de editar el archivo a mano o de probar cambios: las bajas
  y modificaciones reescriben el archivo completo, sin copia temporal ni historial.
- Si escribís un `;` dentro de un texto (por ejemplo una marca), el programa lo guarda escapado
  como `\;`; un `\` se guarda como `\\`. Si editás el archivo manualmente, respetá esas reglas.
- Las líneas vacías se ignoran.
- Si una línea no tiene 11 campos, tiene un número inválido o un enum desconocido, el programa
  muestra `No se pudo cargar el stock inicial: Error en la linea N: ...`. Corregí esa línea y
  volvé a iniciar el programa (no hay opción de recarga en el menú).
- Si el archivo no existe, se crea automáticamente al dar de alta la primera unidad.
- Para empezar de cero, cerrá el programa y borrá o renombrá `stock_vehiculos.csv`.

---

## 11. Mensajes frecuentes y qué hacer

| Mensaje | Significado | Qué hacer |
|---------|-------------|-----------|
| `El campo no puede quedar vacio. Intente nuevamente.` | Dejaste un texto en blanco | Escribí un valor |
| `Debe ingresar un numero entero valido.` | Letras o vacío donde se espera un entero | Escribí solo dígitos (puede ir con signo `-`) |
| `Debe ingresar un numero decimal valido.` | Precio mal escrito | Usá punto decimal, por ejemplo `32500.75` |
| `Opcion fuera de rango. Intente nuevamente.` | Elegiste un número que no está en la lista | Elegí uno de los números mostrados |
| `Ingrese 's' para afirmar o 'n' para negar.` | Confirmación no reconocida | Respondé `s` o `n` |
| `Opcion invalida. Intente nuevamente.` | Opción de menú fuera de `0-6` | Elegí una opción válida |
| `Opcion invalida.` | Opción de reporte fuera de `0-8` | Elegí una opción válida del submenú |
| `Error: Ya existe un vehiculo registrado con el identificador: X` | Identificador repetido | Usá otro identificador |
| `Error: No se encontro ningun vehiculo con el identificador: X` | No existe esa unidad | Verificá el código con la opción `5` (y las mayúsculas) |
| `Error: Un vehiculo NUEVO debe tener 0 kilometros.` | Cargaste km en un nuevo | Corregí el kilometraje a `0` |
| `Error: Un vehiculo USADO debe tener mas de 0 kilometros.` | Cargaste `0` km en un usado | Corregí el kilometraje |
| `Error: El precio base debe ser mayor que cero.` | Precio `0`, negativo o vacío | Ingresá un precio positivo |
| `Error: El anio debe estar entre 1900 y <año actual + 1>.` | Año fuera de rango | Ingresá un año válido |
| `Error: La cilindrada debe ser mayor que cero.` | Cilindrada `0` o negativa | Ingresá un entero positivo |
| `Error: Debe indicar el anio desde y el anio hasta.` | En el filtro combinado dejaste solo un año | Poné ambos años o dejá ambos en `0` |
| `Error: El anio desde no puede ser mayor que el anio hasta.` | Rango invertido | Invertí los valores |
| `No se encontraron vehiculos para los criterios indicados.` | El filtro no dio resultados | Probá con otros criterios |
| `No se pudo cargar el stock inicial: <motivo>` | El CSV no se pudo leer | Corregí el archivo y reiniciá |
| `Presione ENTER para continuar...` | Pausa normal entre operaciones | Apretá **Enter** |

---

## 12. Preguntas frecuentes

**¿Necesito instalar una base de datos?**
No. Toda la persistencia está en `stock_vehiculos.csv`.

**¿Puedo registrar clientes o vender vehículos?**
No. Esta versión solo administra el inventario: altas, bajas, modificaciones, búsquedas y
reportes. No hay clientes, ventas ni historial de operaciones.

**¿Los identificadores distinguen mayúsculas?**
Sí. `AUT-101` y `aut-101` son dos registros distintos y la búsqueda es exacta.

**¿Puedo cargar un vehículo nuevo con kilometraje?**
No. La validación rechaza `NUEVO` con cualquier kilometraje distinto de `0`. Tampoco se permite
`USADO` con `0` km.

**¿Puedo cambiar el identificador o el tipo de un vehículo?**
No desde el menú. La opción `3` conserva ambos. Dalo de baja y cargalo de nuevo con los datos que
quieras.

**¿Puedo dejar un campo igual con Enter en la modificación?**
No. La modificación pide todos los datos otra vez; el valor anterior aparece entre paréntesis
solo como referencia.

**¿Los precios son en dólares o en pesos?**
El programa los muestra con el símbolo `$`, pero el código no define ninguna moneda: se trabaja
con un número decimal. Usá siempre `.` como separador decimal.

**¿Los valores de la tabla salen con dos decimales fijos?**
En el listado, el precio final se muestra con dos decimales (`%12.2f`). En la ficha individual se
muestra el valor numérico completo.

**¿Se guarda algo al elegir "salir"?**
No hace falta: cada alta, baja o modificación ya escribió el archivo. La opción `0` solo cierra.

**¿Puedo editar el CSV a mano?**
Sí, pero respetá los 11 campos, el separador `;`, los nombres de enum en mayúsculas y el escape
`\;` / `\\`. Los cambios se ven recién cuando reiniciás el programa, porque no hay opción de
recarga en el menú.

**¿Qué pasa si el archivo tiene una línea corrupta?**
El programa no carga ningún stock y muestra `No se pudo cargar el stock inicial: Error en la
linea N: ...`. Corregí esa línea antes de volver a abrirlo.

**¿Por qué el recargo de moto no se aplica a una de 500 cc?**
Porque la condición del código es `cilindrada > 500`, no `>= 500`.

---

## 13. Verificación rápida

El repositorio **no incluye una suite de pruebas automatizadas**. Para verificar que el proyecto
está sano, compilá con todas las advertencias:

```powershell
javac -encoding UTF-8 -Xlint:all -d out (Get-ChildItem src -Recurse -Filter *.java).FullName
```

Si no hay errores ni advertencias, hacé una prueba manual corta:

1. Iniciá con `java -cp out com.concesionaria.Main` en una carpeta de prueba.
2. Opción `1`: cargá un automóvil `NUEVO` con `0` km.
3. Opción `1`: cargá una motocicleta `USADO` con `6000` km y cilindrada `600`.
4. Opción `5`: confirmá que aparecen las dos unidades con su precio final.
5. Opción `6` → `6`: el total debe ser la suma de esos dos precios.
6. Opción `6` → `7`: solo la moto debe aparecer con service pendiente.
7. Salí con `0`, reiniciá el programa y confirmá que el stock sigue con dos unidades.

---

## ¿Y si quiero entender el código?

- [README](README.md) — arquitectura por capas, reglas de negocio y formato del archivo.
- `src/com/concesionaria/modelo/` — entidades, enums, interfaces y excepciones del dominio.
- `src/com/concesionaria/servicio/` — validaciones, filtros y cálculos del inventario.
- `src/com/concesionaria/persistencia/` — lectura y escritura del CSV.
- `src/com/concesionaria/vista/` — menú y lectura de datos por consola.
- `src/com/concesionaria/Main.java` — punto de entrada: arma el DAO, el servicio y el menú.
