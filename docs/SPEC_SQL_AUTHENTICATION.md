# Consultas SQL en autenticacion

## Fuente
Hallazgo aprobado: **SQL de autenticacion concatena credenciales**, repositorio `workshop-auth-backend`.

Evidencia: `findByCredentials` concatena `username` y `password` en la consulta SQL (`src/main/java/com/bcp/workshop/auth/LegacyDatabase.java:32`).

Impacto: entradas especiales podrian alterar la semantica de la consulta y comprometer la identidad autenticada.

## Contexto actual
`POST /auth/login` valida que usuario y contrasena no esten vacios y los pasa a `LegacyDatabase`. Si encuentra un usuario, responde con exito y los datos de sesion; si no, responde con credenciales invalidas.

## Problema confirmado
La consulta no garantiza que usuario y contrasena se interpreten exclusivamente como valores. La autenticacion podria depender de la semantica SQL de la entrada en lugar de requerir una coincidencia exacta de credenciales.

## Comportamiento a preservar
- Mantener `POST /auth/login` y su forma actual de solicitud.
- Mantener los valores de usuario y contrasena tal como se reciben para la busqueda; actualmente `trim()` solo valida que no esten vacios.
- Mantener las respuestas actuales: `400` para campos requeridos ausentes, `401` para credenciales invalidas y `200` para autenticacion exitosa.
- Mantener la forma de respuesta exitosa consumida por el frontend: `token`, `expiresIn` y `user` con `username`, `displayName` y `role`.
- Continuar aceptando el campo `client` que envia el frontend, sin cambiar su semantica en esta especificacion.

## Alcance
- Establecer que usuario y contrasena se procesen como datos literales en la busqueda de credenciales.
- Evitar que la sintaxis incluida en esos valores cambie los criterios de autenticacion.
- Definir criterios verificables para credenciales correctas, incorrectas y valores con caracteres especiales.

## Fuera de alcance
- Cambiar el formato o la firma del token.
- Cambiar el almacenamiento o la politica de contrasenas.
- Cambiar roles, permisos o la estructura del usuario.
- Definir normalizacion de usuario o contrasena.
- Refactorizar capas o clases del backend.

## Criterios de aceptacion
- Las credenciales validas existentes continuan permitiendo el login.
- Credenciales invalidas continuan produciendo `401`.
- Valores con caracteres especiales se tratan como datos y no pueden alterar la consulta ni autenticar como otra cuenta.
- Usuario o contrasena nulos o vacios continuan produciendo `400`.
- El login exitoso conserva el status y los campos de respuesta usados actualmente por el frontend.
- Un fallo de consulta nunca produce una autenticacion exitosa.

## Contratos afectados/protegidos
- `POST /auth/login`: conservar metodo, ruta, campos de solicitud y comportamiento HTTP vigente.
- Respuesta exitosa: preservar `token`, `expiresIn` y `user` con los campos consumidos por el cliente.
- Mantener el tratamiento actual de los valores originales: `trim()` valida que no esten vacios, pero no normaliza los valores enviados a la busqueda.

## Riesgos, supuestos y preguntas abiertas
- Confirmar si se permiten espacios al inicio o al final de usuario y contrasena; el comportamiento actual los conserva para la busqueda.
- `client` se recibe, pero su uso no esta definido por este hallazgo.
- `debug` aparece actualmente en la respuesta; confirmar si forma parte del contrato que debe conservarse.
- El manejo de errores de base de datos y la proteccion de contrasenas no forman parte de esta especificacion.
