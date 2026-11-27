# Despliegue de OceanLink en AWS (EC2 + RDS)

Arquitectura: **tu PC** genera `OceanLink.war` → **EC2** (Amazon Linux 2023 + Java 17 + Tomcat 10.1) → **RDS** (MySQL, privado).
La app se abre en `http://IP-PUBLICA:8080/OceanLink/`.

> Regla de oro: **nunca** subas a Git el `.pem`, la clave de RDS ni la contraseña de aplicación de Gmail.

---------------------------------------------------------------------------------------------------

## 0. Antes de empezar
- Entra a AWS Academy → Learner Lab → **Start Lab** → cuando el círculo esté verde, clic en **AWS**.
- El sandbox dura **4 horas** y luego apaga todo (no lo destruye). Planifica la demo con tiempo.
- Región: la que te abra el Learner Lab (normalmente `us-east-1`). Usa **la misma** para EC2 y RDS.

## 1. Preparar el proyecto y generar el .war (en tu PC)
0. **Antes:** `git pull` en tu rama (Jesús subió cambios a `feat/correo-reportes`; ver nota del chat).
1. Copia `DaoBase.java` del zip a `src/main/java/com/oceanlink/daos/` y haz un cambio de 1 línea en el `pom.xml`.
   - `DaoBase` ahora lee la BD de variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`) y, si no existen, de `db.properties`. Así el `.war` **no lleva claves**.
   - En `pom.xml`, dentro de `<build>` y **antes** de `<plugins>`, agrega: `<finalName>OceanLink</finalName>`. El archivo se llamará `OceanLink.war` y la app quedará en `/OceanLink`.
2. IntelliJ → pestaña **Maven** (derecha) → *OceanLink* → **Lifecycle → clean**, luego **package** (doble clic).
3. El archivo queda en `target/OceanLink.war`.
4. (Opcional) Confirma que corre en tu PC con Smart Tomcat antes de subirlo.

## 2. Crear la VM (EC2)
Consola AWS → **EC2** → **Lanzar instancias**:
- **Nombre:** `oceanlink-vm`
- **AMI:** Amazon Linux 2023
- **Tipo:** `t3.micro`
- **Par de claves:** crear nuevo → `oceanlink-key` (.pem) → **guárdalo bien; si lo pierdes no hay acceso**.
- **Configuraciones de red → grupo de seguridad nuevo** con estas reglas de entrada:
  | Tipo | Puerto | Origen |
  |---|---|---|
  | SSH | 22 | Mi IP |
  | TCP personalizado | 8080 | Anywhere IPv4 (0.0.0.0/0) |
- **Almacenamiento:** 8 GB por defecto está bien.
- **Lanzar** y esperar a que las comprobaciones de estado pasen (2/2).
- **IP elástica (recomendado):** EC2 → *Direcciones IP elásticas* → *Asignar* → *Asociar* a tu instancia. Así la IP no cambia cuando el sandbox se apaga.

Conectarte (CMD de Windows, en la carpeta del .pem):
```
ssh -i oceanlink-key.pem ec2-user@TU-IP-PUBLICA
```
La primera vez escribe `yes`.

## 3. Crear la base de datos (RDS)
Consola → **RDS** → **Crear base de datos** → *Creación estándar*:
- **Motor:** MySQL (versión 8.4.x, la que ofrezca; el dump es de MySQL 8.0 y es compatible)
- **Plantilla:** *Capa gratuita* (Free tier)
- **Identificador:** `oceanlink-db`
- **Usuario maestro:** `admin` · contraseña: *autogenerada* o la tuya → **guárdala (solo se muestra una vez)**
- **Clase:** `db.t3.micro` · **Almacenamiento:** 20 GB y **desactiva el autoscaling**
- **Conectividad:** *No conectarse a un recurso EC2*; **VPC: la misma que tu EC2**; **Acceso público: NO**
- **Grupo de seguridad nuevo:** `rds-oceanlink`
- **Configuración adicional → Nombre de la base de datos inicial:** `proyecto_siete`  ← importante
- Crear (tarda unos minutos).

Permitir que la VM entre a la BD: EC2 → **Security Groups** → `rds-oceanlink` → *Reglas de entrada* → *Editar*:
- Tipo **MYSQL/Aurora (3306)** · Origen: el **grupo de seguridad de tu EC2** (más seguro que Anywhere).

Cuando el estado sea **Disponible**, anota el **Punto de enlace (endpoint)**, algo como
`oceanlink-db.xxxx.us-east-1.rds.amazonaws.com`.

## 4. Importar la base de datos
Los scripts están en la carpeta `db/` del proyecto. **Orden obligatorio:**
`proyecto_siete.sql` → `cambios_esquema.sql` → `datos_prueba.sql`.

**Opción A – desde la VM (más simple).** En tu PC (CMD, en la carpeta raíz del proyecto):
```
scp -i oceanlink-key.pem db\proyecto_siete.sql db\cambios_esquema.sql db\datos_prueba.sql ec2-user@TU-IP:~
```
En la VM:
```
sudo dnf install -y mariadb105
mysql -h ENDPOINT-RDS -u admin -p proyecto_siete < proyecto_siete.sql
mysql -h ENDPOINT-RDS -u admin -p proyecto_siete < cambios_esquema.sql
mysql -h ENDPOINT-RDS -u admin -p proyecto_siete < datos_prueba.sql
mysql -h ENDPOINT-RDS -u admin -p -e "USE proyecto_siete; SHOW TABLES;"
```
Debes ver las 18 tablas (clientes, usuarios, servicios, segmentos, ...).

**Opción B – MySQL Workbench por túnel SSH:** *Standard TCP/IP over SSH*; SSH Hostname = IP de la VM, usuario `ec2-user`, key file = tu `.pem` (no `.ppk`); MySQL Hostname = endpoint del RDS, puerto 3306, usuario `admin`. Luego ejecuta los 3 scripts en orden.

Las contraseñas de los usuarios de prueba (`Demo1234`) se convierten a hash automáticamente cuando arranca la aplicación.

## 5. Instalar Java y Tomcat en la VM
```
sudo yum update -y
sudo yum install java-17-amazon-corretto-headless -y
java -version
```
Tomcat 10.1 (copia el enlace **tar.gz** de https://tomcat.apache.org/download-10.cgi, rama 10.1.x):
```
wget URL_DEL_TAR_GZ
tar xvf apache-tomcat-10.1.*.tar.gz
sudo mv apache-tomcat-10.1.* /usr/share/apache-tomcat
sudo useradd -M -d /usr/share/apache-tomcat tomcat
sudo chown -R tomcat /usr/share/apache-tomcat
```
**Usuario del manager** (para subir el .war desde el navegador). `sudo nano /usr/share/apache-tomcat/conf/tomcat-users.xml` y antes de `</tomcat-users>`:
```xml
<role rolename="manager-gui" />
<user username="manager" password="PON_UNA_CLAVE_PROPIA_FUERTE" roles="manager-gui" />
```
> No uses la clave de ejemplo de la clase (`telecom2022`): la VM está en internet.

Permitir el manager desde fuera: `sudo nano /usr/share/apache-tomcat/webapps/manager/META-INF/context.xml` y comenta la línea `<Valve ... />` con `<!--  -->`.

## 6. Variables de entorno de la aplicación (BD y correo)
Crea el archivo de secretos (solo root puede leerlo):
```
sudo nano /etc/oceanlink.env
```
Contenido (sin comillas ni espacios alrededor del `=`):
```
DB_URL=jdbc:mysql://ENDPOINT-RDS:3306/proyecto_siete?serverTimezone=America/Lima
DB_USER=admin
DB_PASSWORD=LA_CLAVE_DE_RDS
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu.cuenta@gmail.com
SMTP_PASSWORD=CONTRASENA_DE_APLICACION_DE_16_CARACTERES
SMTP_FROM_NAME=OceanLink
# Si usan Brevo en vez de Gmail: SMTP_HOST=smtp-relay.brevo.com, SMTP_PORT=587, SMTP_USER=<login SMTP de Brevo>,
# SMTP_PASSWORD=<clave SMTP de Brevo> y SMTP_FROM=<remitente verificado en Brevo> (distinto del usuario SMTP)
# Opcional (modo demo): todos los correos llegan a esta bandeja
# CORREO_REDIRIGIR_A=tu.correo@gmail.com
```
```
sudo chmod 600 /etc/oceanlink.env
```
Servicio de Tomcat: `sudo nano /etc/systemd/system/tomcat.service`
```
[Unit]
Description=Tomcat
After=syslog.target network.target

[Service]
Type=forking
User=tomcat
Group=tomcat
EnvironmentFile=/etc/oceanlink.env
Environment=JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto
Environment='JAVA_OPTS=-Djava.awt.headless=true -Duser.timezone=America/Lima -Xmx512m'
Environment=CATALINA_HOME=/usr/share/apache-tomcat
Environment=CATALINA_BASE=/usr/share/apache-tomcat
Environment=CATALINA_PID=/usr/share/apache-tomcat/temp/tomcat.pid
ExecStart=/usr/share/apache-tomcat/bin/catalina.sh start
ExecStop=/usr/share/apache-tomcat/bin/catalina.sh stop

[Install]
WantedBy=multi-user.target
```
(`EnvironmentFile` lo lee systemd como root, por eso el archivo puede ser 600.)
```
sudo systemctl daemon-reload
sudo systemctl enable tomcat
sudo systemctl restart tomcat
sudo ss -tunelp | grep 8080
```

## 7. Desplegar el .war
**Opción A – navegador:** abre `http://TU-IP:8080/manager/html`, entra con el usuario del paso 5, baja a *Desplegar → Archivo WAR a desplegar*, elige `OceanLink.war` y *Desplegar*.

**Opción B – por consola:** en tu PC `scp -i oceanlink-key.pem target\OceanLink.war ec2-user@TU-IP:~` y en la VM:
```
sudo mv ~/OceanLink.war /usr/share/apache-tomcat/webapps/
sudo chown tomcat:tomcat /usr/share/apache-tomcat/webapps/OceanLink.war
```
Abre: **http://TU-IP:8080/OceanLink/**

## 8. Verificación (lista para la demo)
- [ ] La página de login carga en `http://TU-IP:8080/OceanLink/`.
- [ ] Entras con un usuario de prueba (clave `Demo1234`) y el dashboard muestra datos.
- [ ] `sudo tail -n 80 /usr/share/apache-tomcat/logs/catalina.out` muestra `[Seguridad] Contraseñas convertidas a hash...` y ningún error de conexión.
- [ ] Como admin, "Registro de correos" dice **Configurado**.
- [ ] Creas una incidencia y el correo llega (o aparece "Enviado" en el registro).
- [ ] Reportes: PDF, Excel y CSV descargan.

Capturas útiles para el entregable: EC2 *en ejecución*, RDS *disponible* (sin acceso público), reglas de los security groups, la app abierta con la IP pública, y el login con un usuario.

## 9. Operación y problemas frecuentes
| Síntoma | Causa probable / solución |
|---|---|
| El navegador no abre `:8080` | Falta la regla de entrada 8080 en el security group de la EC2, o Tomcat no corre (`sudo systemctl status tomcat`). |
| 404 en `/OceanLink/` | El .war no se llama `OceanLink.war` o aún está desplegando; mira `catalina.out`. |
| Error de BD / `Communications link failure` | Endpoint mal escrito en `/etc/oceanlink.env`, regla 3306 sin el SG de la EC2, o EC2 y RDS en VPC distintas. Tras editar el env: `sudo systemctl restart tomcat`. |
| `Access denied for user` | Usuario o clave de RDS incorrectos. |
| Login falla con usuarios de prueba | No se importó `datos_prueba.sql`, o se importó antes que los otros dos. |
| Correos no salen | Revisa "Registro de correos": clave de aplicación de Gmail mal copiada o sin verificación en 2 pasos. |
| Se apagó el sandbox | Vuelve a *Start Lab*; enciende RDS y la EC2 (Tomcat arranca solo). Sin IP elástica la IP pública cambió. |
| Fechas con 5 horas de diferencia | Confirma `-Duser.timezone=America/Lima` en el servicio y `serverTimezone` en `DB_URL`. |

Cuidado con los créditos: apaga/elimina los recursos cuando termines el curso (RDS y la IP elástica siguen consumiendo crédito).
