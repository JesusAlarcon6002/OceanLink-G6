# Correcciones de la auditoría (puntos 3 al 9)

| # | Hallazgo | Qué se hizo | Archivo(s) |
|---|----------|-------------|------------|
| 3 | Sin protección CSRF | Todo POST debe venir del mismo sitio (cabecera Origin/Referer = Host); si no, 403 | `AuthFilter.java` |
| 4 | Login sin límite de intentos | 5 fallos por correo+IP (o 20 por IP) en 15 min → bloqueo de 5 min (HTTP 429) | `IntentosLogin.java` (nuevo), `LoginServlet.java` |
| 5 | Sin política de contraseñas | 8–64 caracteres, con letras y números, sin espacios en los extremos (al crear y al cambiar); también valida la confirmación en el servidor | `Seguridad.java`, `UsuariosServlet.java` |
| 6 | Errores con traza y versión de Tomcat | Páginas de error propias (404, 405, 429, 500 y cualquier excepción); cookie de sesión HttpOnly + SameSite=Lax | `web.xml`, `error.jsp` (nuevo), `META-INF/context.xml` (nuevo) |
| 7 | Faltan cabeceras de seguridad | X-Frame-Options, X-Content-Type-Options, Referrer-Policy, Permissions-Policy en todas las respuestas | `AuthFilter.java` |
| 8 | Crear/editar usuario fallaba en silencio | Ahora avisa en el formulario si el correo o usuario ya existe (o si hubo error de BD) y no manda el correo de bienvenida | `UsuariosDao.java`, `UsuariosServlet.java` |
| 9 | Código muerto | **Borrar a mano** `src/main/java/com/oceanlink/servlets/Servlet1.java` | — |

## Cómo aplicarlo
1. Copia el contenido de `src/` de este zip sobre `src/` de tu repo (reemplazar). NO copies la carpeta `OceanLink-auditoria`.
2. Borra `Servlet1.java`.
3. IntelliJ: Maven → clean → package.

## Volver a desplegar en AWS
Desde CMD, en la carpeta del repo:
```
scp -i C:\Users\Usuario\Downloads\oceanlink-key.pem target\OceanLink.war ec2-user@3.223.226.40:/tmp/
```
En el SSH:
```
sudo systemctl stop tomcat
sudo rm -rf /opt/tomcat/webapps/OceanLink /opt/tomcat/webapps/OceanLink.war
sudo cp /tmp/OceanLink.war /opt/tomcat/webapps/OceanLink.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/OceanLink.war
sudo systemctl start tomcat
```

## Pruebas rápidas después de desplegar
- Login con clave mala 5 veces → al 6.º dice "Demasiados intentos fallidos. Espera 5 minutos…" (es por IP+correo; espera o prueba con otro correo).
- Crear usuario con clave `abc` → rechaza con el mensaje de política. Con `Demo1234` → acepta.
- Crear usuario con un correo que ya existe → aviso en el formulario (antes parecía creado).
- Abrir `http://IP:8080/OceanLink/noexiste` → página de error de OceanLink (sin versión de Tomcat).
- Chrome → F12 → Network → cualquier página → Response Headers: `X-Frame-Options: DENY`, etc.

## Límites (honestidad)
- No se agregó CSP (Content-Security-Policy): las pantallas usan scripts en línea y librerías por CDN; activarla sin probar podría romper gráficos.
- Falta HTTPS (necesita dominio y certificado): queda como limitación documentada. La cookie no lleva `Secure` por esa razón.
- El bloqueo de login es en memoria: se reinicia si se reinicia Tomcat.
- El anti-CSRF por Origin/Referer deja pasar peticiones que no traen ninguna de las dos cabeceras (los navegadores actuales siempre mandan Origin en los POST).
