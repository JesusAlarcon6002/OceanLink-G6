# OceanLink · Funciones nuevas (guía rápida)

## Qué trae
1. **Olvidé mi contraseña** (login → enlace): correo → código de 6 dígitos → clave nueva.
2. **Solicitar una cuenta** (login → enlace): formulario + código al correo → queda *Pendiente*. El **Network Admin** la aprueba (elige el rol) o rechaza en **Solicitudes de registro**. El admin **ya no crea usuarios a mano** (el botón ahora lleva a las solicitudes).
3. **Campana** en el encabezado de todos los roles (numerito de no leídos) + pantalla **Mensajes y avisos**: ahí ves copia de los correos que manda el sistema y los mensajes de otros usuarios, y puedes escribirle a cualquiera (también le llega por correo).
4. Botón **Ver** (solo lectura) junto a **Editar** en los 9 CRUD.
5. **Network Operator → Historial de incidencias**: lista de solo lectura.
6. Las notificaciones muestran la **etiqueta** (SEG-..., LS-...) en vez del id.

## Límites anti-abuso
- Código de recuperación: 10 min de vida, 5 intentos, 30 s entre reenvíos, 5 pedidos/15 min por IP y 3/15 min por correo. La respuesta no revela si el correo existe.
- Solicitud de registro: 5 intentos/15 min por IP, solo dominios permitidos (`@pucp.edu.pe`, `@pucp.pe`, `@gmail.com`), un correo con solicitud pendiente no se repite.
- Mensajes: 10 cada 10 min por usuario, asunto ≤150 y mensaje ≤2000 caracteres.
- Contadores en memoria: se reinician si reinicias Tomcat.

## Aplicar los cambios (en este orden)
1. Descomprime el zip **encima** de tu repo (mismas rutas). **No hay archivos para borrar** salvo los que ya te dije: `Servlet1.java` y `correoPrueba.jsp` si aún están.
2. **Base de datos en RDS** (crea 2 tablas; el script es seguro de repetir):
   - En tu PC: `scp -i C:\Users\Usuario\Downloads\oceanlink-key.pem db\cambios_esquema.sql ec2-user@3.223.226.40:/tmp/`
   - En el EC2, primero mira usuario y endpoint (sin mostrar la clave): `sudo sh -c 'grep -E "^DB_(URL|USER)" /etc/oceanlink.env'`
   - Luego, en **dos bloques separados** (para que `read` no se coma la línea siguiente):
     ```
     read -s -p "Clave RDS: " P; echo
     ```
     ```
     mysql -h oceanlink-db.c1ausuekm9pm.us-east-1.rds.amazonaws.com -u USUARIO -p"$P" < /tmp/cambios_esquema.sql; unset P
     ```
   - Al final debe listar `solicitudes_registro` y `notificaciones`.
3. En tu PC: `mvn clean package` y sube el war:
   ```
   scp -i C:\Users\Usuario\Downloads\oceanlink-key.pem target\OceanLink.war ec2-user@3.223.226.40:/tmp/
   ```
4. En el EC2:
   ```
   sudo systemctl stop tomcat
   sudo rm -rf /opt/tomcat/webapps/OceanLink /opt/tomcat/webapps/OceanLink.war
   sudo cp /tmp/OceanLink.war /opt/tomcat/webapps/OceanLink.war
   sudo chown tomcat:tomcat /opt/tomcat/webapps/OceanLink.war
   sudo systemctl start tomcat
   ```

## Checklist de prueba (en el navegador)
- [ ] Login normal sigue funcionando; aparecen los enlaces "¿Olvidaste tu contraseña?" y "Solicitar una cuenta".
- [ ] Cada rol: la campana sale en el encabezado y el menú tiene **Mensajes y avisos**.
- [ ] Ana → Usuarios: ya no hay "Registrar usuario", sino **Solicitudes de registro**. Probar "Ver" en un usuario (sin campo de contraseña).
- [ ] Network Operator → **Historial de incidencias** muestra la lista, sin botones de crear/editar/borrar.
- [ ] Escribirle un mensaje a otro usuario: le sube el numerito en la campana.
- [ ] Hacer una incidencia/mantenimiento y revisar que el aviso diga "Segmento SEG-..." (etiqueta).
- [ ] **Registro y recuperación necesitan el SMTP configurado** (ver abajo): sin él, recuperar contraseña queda desactivado y el registro se guarda sin código.

## Pendiente / limitaciones (dilo tal cual en la exposición)
- **SMTP no está configurado en AWS aún** (variables `SMTP_*` en `/etc/oceanlink.env`, pídele a Jesús el proveedor). Hasta entonces no llegan correos reales ni códigos.
- Lo probé con pruebas automáticas (compilación + filtros + límites + utilidades). **No pude probar** contra MySQL real, ni los JSP en Tomcat, ni envío real de correo: revisa el checklist.
- Sin HTTPS: las claves viajan sin cifrar por la red (solo demo).
- Si apruebas una solicitud, la persona entra con el correo y la clave que eligió; el usuario (etiqueta) se arma con lo que va antes de la `@`.
