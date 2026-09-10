# Plataforma de Autocorrección de Prácticas — FFTI

Sistema web para la gestión y autocorrección de prácticas de laboratorio de la asignatura **Fundamentos Físicos y Tecnológicos de la Informática**.

---

## Requisitos previos

Antes de ejecutar la plataforma es necesario tener instalado:

| Herramienta | Versión mínima | Descarga |
|---|---|---|
| Java (JDK) | 17 | https://adoptium.net |
| Maven | 3.8 | https://maven.apache.org |
| PostgreSQL | 14 | https://www.postgresql.org |


---

## 1. Preparar la base de datos

Abre PostgreSQL y crea la base de datos y el usuario:

```sql
CREATE DATABASE plataforma_evaluacion;
CREATE USER nombre_usuario WITH PASSWORD 'contraseña_segura';
GRANT ALL PRIVILEGES ON DATABASE plataforma_evaluacion TO nombre_usuario;
```

> El esquema de tablas se crea automáticamente al arrancar la aplicación por primera vez. No es necesario ejecutar ningún script SQL.

---

## 2. Configurar el archivo application.properties

El archivo de configuración se encuentra en:

```
src/main/resources/application.properties
```

A continuación se describen **todos los valores que hay que cambiar** antes de ejecutar:

---

### Base de datos (obligatorio)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/plataforma_evaluacion
spring.datasource.username=CAMBIAR_POR_TU_USUARIO_POSTGRES
spring.datasource.password=CAMBIAR_POR_TU_CONTRASEÑA_POSTGRES
```

- `url`: si PostgreSQL corre en otro puerto o máquina, ajusta `localhost:5432`
- `username` y `password`: los del usuario de PostgreSQL que creaste en el paso anterior

---

### Directorio de imágenes subidas por alumnos (obligatorio)

```properties
uploads.path=./uploads
```

Por defecto las imágenes se guardan en una carpeta `uploads/` dentro del directorio donde se ejecuta la aplicación. Si quieres usar otra ruta:

```properties
uploads.path=/ruta/absoluta/a/tu/carpeta/uploads
```

Asegúrate de que la carpeta existe y tiene permisos de escritura.

---

### Corrección automática por IA (opcional)

```properties
ai.openai.endpoint=https://models.inference.ai.azure.com
ai.openai.key=AQUI_TU_TOKEN_DE_GITHUB
ai.openai.model=gpt-4o-mini
```

**Si no se configura esta sección, la plataforma funciona igualmente.** Las preguntas de tipo IMAGEN y TEXTO LIBRE quedarán pendientes de revisión manual por el profesor.

Para activarlo:

1. Ve a https://github.com/settings/tokens
2. Genera un nuevo token clásico con el scope `read:user`
3. Copia el token generado y ponlo en `ai.openai.key`

> Las cuentas con suscripción a GitHub Copilot tienen una cuota de uso mayor. Si dispones de licencias Copilot, úsalas para una cuota más alta.

---

### Sistema de correo electrónico (opcional)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=TU_CUENTA@gmail.com
spring.mail.password=AQUI_APP_PASSWORD_DE_GMAIL
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.base-url=http://localhost:8080
```

**Si no se configura esta sección, la plataforma funciona igualmente.** Se desactivan las notificaciones por email: confirmación de cuenta, recuperación de contraseña y aviso de nota publicada. Los alumnos podrán registrarse sin verificar el email y las cuentas quedarán activas directamente.

Para activarlo con Gmail:

1. Activa la verificación en dos pasos en tu cuenta de Google
2. Ve a Gestionar tu cuenta → Seguridad → Contraseñas de aplicaciones
3. Genera una contraseña para "Correo" en "Otro dispositivo"
4. Usa esa contraseña de 16 caracteres en `spring.mail.password` (con espacios incluidos)
5. Cambia `app.base-url` por la URL real donde esté desplegada la aplicación (ej. `http://ffti.fi.upm.es:8080`)

---

### Restricción de dominio de registro

Por defecto solo pueden registrarse correos con dominio `@alumnos.upm.es` (rol ALUMNO) y `@upm.es` (rol PROFESOR). Esta lógica está en `WebController.java`. Si necesitas cambiar los dominios permitidos, busca el método de registro y modifica las condiciones de validación.

---

## 3. Ejecutar la aplicación

Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

O si prefieres generar el ejecutable primero:

```bash
mvn clean package
java -jar target/tfgplatform-0.0.1-SNAPSHOT.jar
```

La aplicación arranca en:

```
http://localhost:8080
```

---

## 4. Primer acceso

Al arrancar por primera vez no hay ningún usuario creado. Ve a:

```
http://localhost:8080/registro
```

Regístrate con un correo `@upm.es` para obtener el rol de **Profesor** o con `@alumnos.upm.es` para el rol de **Alumno**.

> Si el sistema de email **no está configurado**, las cuentas se activan automáticamente al registrarse sin necesidad de confirmar el correo.
>
> Si el sistema de email **sí está configurado**, el usuario recibirá un email con un enlace de confirmación. Hasta que no lo confirme, no podrá iniciar sesión.

---

## 5. Flujo básico de uso

```
Profesor                          Alumno
   │                                 │
   ├─ Crear grupo                    │
   ├─ Añadir alumnos al grupo        │
   ├─ Crear ejercicio                │
   ├─ Añadir preguntas               │
   ├─ Asignar ejercicio al grupo     │
   │                                 ├─ Ver ejercicio pendiente
   │                                 ├─ Resolver preguntas
   │                                 └─ Enviar entrega
   │                                        │
   │          (autocorrección automática)   │
   │                                        │
   ├─ Revisar entrega                       │
   ├─ Ajustar nota si es necesario          │
   └─ Publicar calificación                 │
                                            ├─ Consultar nota y feedback
                                            └─ Interponer reclamación (si procede)
```

---

## 6. Tipos de pregunta disponibles

| Tipo | Corrección | Configuración necesaria |
|---|---|---|
| **NUMÉRICO** | Automática | Valor correcto + tolerancia |
| **TABLA** | Automática (parcial por celdas) | Filas, columnas, valores correctos, tolerancia |
| **IMAGEN** | Por IA (requiere token GitHub) | Rúbrica de corrección (opcional) |
| **TEXTO LIBRE** | Por IA (requiere token GitHub) | Rúbrica de corrección (opcional) |

---

## 7. Exportar calificaciones

Desde la vista de entregas de un ejercicio, el profesor puede descargar las calificaciones del grupo en formato CSV compatible con Excel. El fichero incluye: Nombre, Email, Ejercicio, Nota obtenida, Nota máxima y Estado.

---

## 8. Problemas frecuentes

**La aplicación no arranca y da error de conexión a la base de datos**
→ Verifica que PostgreSQL está corriendo y que el usuario y contraseña en `application.properties` son correctos.

**Los alumnos no pueden registrarse con su correo**
→ Comprueba que el dominio del correo es `@alumnos.upm.es`. Cualquier otro dominio es rechazado por defecto.

**Las preguntas de imagen y texto quedan pendientes de revisión manual**
→ El token de GitHub no está configurado o ha caducado. Genera uno nuevo en https://github.com/settings/tokens

**Los emails no se envían**
→ Verifica que la App Password de Gmail es correcta y que la verificación en dos pasos está activada en la cuenta. Asegúrate también de que `app.base-url` apunta a la URL correcta donde está desplegada la aplicación.

**El alumno no recibe el email de confirmación de cuenta**
→ Revisa la carpeta de spam. Si el problema persiste, desactiva la configuración de email y los alumnos podrán registrarse sin verificación.

---

