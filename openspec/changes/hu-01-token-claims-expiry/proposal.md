# Change: HU-01 - Claims mínimos y expiración coherente

## Why
Los consumidores necesitan comprobar la identidad del usuario y cliente, la integridad del token y su periodo de validez. El Issue 11 define los claims mínimos, la coherencia de `expiresIn`, la protección del secreto y la ausencia de valores sensibles en logs.

## What Changes

- Exigir tokens firmados con los claims `sub`, `client`, `iat` y `exp`, sin escoger aquí el mecanismo de firma.
- **BREAKING**: `client`, hoy opcional en el contrato, pasa a ser obligatorio y no puede estar en blanco; una solicitud sin un valor válido se rechaza con `400`, acorde con la validación de entrada existente.
- Mantener `expiresIn: 3600` y requerir coherencia con `exp - now`; se propone una tolerancia máxima de un segundo por la precisión en segundos de los tiempos.
- Cubrir con escenarios observables el login válido, las credenciales inválidas, la expiración, la redacción de logs y el uso del secreto.
- Documentar la decisión entre HMAC y una biblioteca JWT cuando se prepare la implementación, sin elegir entre ellas en esta propuesta.

## Fuente y baseline

- GitHub Issue [#11 - HU-01: Token firmado con expiración y claims mínimos](https://github.com/user-bcp-student40/workshop-auth-backend/issues/11).
- Baseline obligatorio: [`docs/ENDPOINTS.md`](../../../docs/ENDPOINTS.md), contrato del Día 1 para `POST /auth/login`.
- Ese contrato requiere `username` y `password`, acepta `client` como opcional e indica que hoy no participa en la autenticación. Un login válido responde `200` con `token`, `expiresIn: 3600`, `user` (`username`, `displayName`, `role`) y `debug`. Los datos requeridos ausentes responden `400`; las credenciales inválidas, `401`; y los errores no controlados, `500`.
- El baseline describe el token actual como Base64 sin firma. La incorporación de firma y claims cambia intencionalmente el contenido/formato del token por el Issue; no cambia los demás campos de respuesta ni las respuestas HTTP existentes. La obligatoriedad de `client` es también un cambio intencional al contrato de entrada.
- Hay una discrepancia de evidencia que permanece abierta: el contrato describe Base64, las pruebas actuales verifican un JWT con `RS256`, y el Issue menciona HMAC. Esta propuesta no decide cuál mecanismo conservar o adoptar.

## Capabilities

### New Capabilities

- `authentication-token`: claims mínimos, validación de cliente, expiración, protección de material sensible y preservación del contrato de login.

### Modified Capabilities

- Ninguna. El repo no tiene una spec base bajo `openspec/specs/`; este cambio añade una capacidad verificable.

## Scope

### In scope

- Claims requeridos `sub`, `client`, `iat` y `exp`, y firma verificable del token emitido.
- `client` obligatorio, no nulo ni en blanco; validaciones adicionales de formato o una lista permitida requieren una decisión posterior.
- Vigencia nominal de 3600 segundos y coherencia de `expiresIn` con `exp - now`, con tolerancia propuesta de un segundo.
- Rechazo observable de solicitudes inválidas, credenciales incorrectas y tokens expirados.
- Logs sin tokens, secretos ni material de firma; secreto predeterminado limitado y documentado para laboratorio.
- Documentación de la decisión entre HMAC y una biblioteca JWT.

### Out of scope

- Elegir algoritmo, biblioteca, claims adicionales, rotación de claves o arquitectura de autenticación.
- Cambiar la forma de respuesta exitosa, el valor nominal de expiración, las reglas de credenciales, roles o autorización.
- Cambiar los estados HTTP o las respuestas existentes para los casos documentados, excepto el nuevo rechazo de `client` ausente, nulo o en blanco.
- Cambiar consumidores o endpoints de otros repositorios, planificar tareas o modificar código funcional.

## Behavior to Preserve

- `POST /auth/login` conserva `200` para autenticación válida, `400` para credenciales requeridas ausentes, `401` para credenciales inválidas y `500` para errores no controlados, según el contrato del Día 1.
- La respuesta exitosa conserva `token`, `expiresIn`, `user` (`username`, `displayName`, `role`) y `debug`; no se agrega `client` a la respuesta.
- La vigencia nominal sigue siendo 3600 segundos. El Issue agrega coherencia con los tiempos del token, no un cambio de duración.
- Excepción explícita al contrato de entrada: `client` deja de ser opcional porque el Issue requiere obtenerlo de la solicitud y emitirlo como claim obligatorio. Su ausencia, nulidad o valor en blanco pasa a responder `400` sin emitir token.
- No se altera el comportamiento documentado de `GET /q/health`.

## Impact

- Cambia la compatibilidad de solicitudes que omiten `client` y el contenido/formato interno del token; los campos de respuesta y resultados HTTP existentes se preservan en los demás casos.
- La tolerancia de un segundo es una propuesta verificable para revisión, no una cifra indicada por el Issue; debe confirmarse antes de implementación.
- Riesgo pendiente: decidir el mecanismo de firma ante la diferencia entre el baseline, las pruebas actuales y la referencia a HMAC del Issue. La spec no prescribe una solución técnica.