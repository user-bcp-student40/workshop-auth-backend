# Integridad del token de autenticacion

## Fuente
Hallazgo aprobado por el usuario: **Integridad del token**.

Evidencia: el backend construye el token concatenando claims y un secreto estatico, y luego solo aplica Base64 (`src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java:8`, `:14`, `:15`). Requirements API decodifica el token y compara el secreto y la expiracion, sin verificar una firma ([main.py](../../workshop-requirements-api/app/main.py#L14), [main.py](../../workshop-requirements-api/app/main.py#L22)).

Impacto: los consumidores no pueden verificar criptograficamente que la identidad y los demas claims provengan del emisor y no hayan sido alterados.

## Contexto actual
`POST /auth/login` emite un token con usuario, rol, expiracion y secreto estatico. Requirements API recibe ese valor como Bearer, lo decodifica y obtiene usuario y rol para las solicitudes protegidas.

## Problema confirmado
La aceptacion del token se basa en el contenido decodificado y la comparacion con un secreto compartido, no en una prueba criptografica verificable de integridad. Los consumidores necesitan confiar en los claims antes de autorizar una solicitud.

## Comportamiento a preservar
- Mantener `POST /auth/login`, sus campos de entrada y el flujo de credenciales validas e invalidas.
- Mantener la forma de la respuesta exitosa usada por los consumidores: `token`, `expiresIn` y `user` con `username`, `displayName` y `role`.
- Mantener el transporte del token mediante `Authorization: Bearer ...`.
- Mantener la identidad y el rol efectivos para los endpoints protegidos.
- Mantener la duracion vigente de una hora y el rechazo de tokens expirados, salvo aprobacion explicita de un cambio.

## Alcance
- Especificar la integridad verificable de los tokens emitidos y aceptados por el flujo de autenticacion.
- Definir el comportamiento esperado ante tokens validos, expirados, malformados o alterados.
- Mantener interoperabilidad entre Auth backend y Requirements API.

## Fuera de alcance
- Cambiar la politica de roles o la autorizacion por propietario de tareas.
- Cambiar el mecanismo de credenciales del login.
- Cambiar los endpoints o la forma de la respuesta de login.
- Elegir algoritmo, biblioteca, estructura interna de claims o estrategia de rotacion de claves sin una decision aprobada.

## Criterios de aceptacion
- Un token emitido tras un login valido es aceptado por Requirements API mientras sea valido y no haya expirado.
- Las solicitudes protegidas rechazan con `401` los tokens invalidos, expirados, malformados o con claims alterados.
- La identidad y el rol usados por Requirements API corresponden a los claims validados del token.
- La respuesta exitosa de `POST /auth/login` conserva los campos y tipos consumidos actualmente, y `expiresIn` continua representando la duracion vigente.
- Los clientes continuan enviando el token como Bearer.

## Contratos afectados/protegidos
- `POST /auth/login`: conservar entrada, respuesta exitosa y estados HTTP existentes para login valido e invalido.
- Bearer entre Auth backend y Requirements API: conservar el header y la semantica de identidad, rol y expiracion.
- Endpoints protegidos de Requirements API: tokens validos continuan autorizando solicitudes; tokens invalidos o expirados se rechazan.
- La respuesta actual incluye `debug`; confirmar si forma parte del contrato que debe preservarse antes de modificarla.

## Riesgos, supuestos y preguntas abiertas
- La transicion debe coordinar emisor y consumidor para evitar que una version rechace tokens emitidos por la otra.
- [HU-01.md](../HU-01.md) propone claims `sub`, `client`, `iat` y `exp`; confirmar si esa propuesta esta aprobada.
- Confirmar si se mantiene exactamente la duracion de 3600 segundos y como debe relacionarse con `expiresIn`.
- Definir configuracion de claves, ciclo de rotacion y compatibilidad temporal en una decision posterior; esta spec no fija una alternativa.
