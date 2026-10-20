# Workshop Auth Backend — Java Quarkus

> **Laboratorio intencionalmente vulnerable.** Ejecutar únicamente en local, repositorios privados y ambientes aislados. No desplegar en Internet, CERT o PROD.

Backend Quarkus que autentica usuarios de laboratorio almacenados en SQLite y entrega un token consumido por el frontend Angular y la API Python.

## Ejecución

Requisitos: JDK 21 y Maven 3.9+.

```bash
mvn quarkus:dev
```

Servicio: `http://localhost:8080`

Credenciales locales:

- `student` / `Workshop123!`
- `trainer` / `Trainer123!`

## Endpoints

- `POST /auth/login`
- `GET /q/health`

## Propósito de la rama `main`

La rama contiene deuda técnica y de seguridad deliberada: SQL concatenado, contraseñas en texto plano, token reversible, secreto hardcodeado, CORS permisivo, método largo, manejo genérico de excepciones y dependencias con CVEs.

## Clave de firma del token

El emisor crea JWT RS256. Configura `SMALLRYE_JWT_SIGN_KEY_LOCATION` con la ruta a la clave privada RSA antes de iniciar el servicio; Requirements API debe configurarse con la clave publica correspondiente. No incluyas la clave privada en la imagen ni uses las claves de `src/test/resources` fuera de pruebas.

PowerShell local:

```powershell
$env:SMALLRYE_JWT_SIGN_KEY_LOCATION = 'C:\secrets\auth-private.pem'
mvn quarkus:dev
```
