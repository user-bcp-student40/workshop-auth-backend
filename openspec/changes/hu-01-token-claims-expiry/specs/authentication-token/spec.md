# Authentication token

## Purpose

Esta capacidad define el contrato observable de identidad, integridad y expiración de los tokens emitidos por el login, preservando el comportamiento HTTP documentado.

## ADDED Requirements

### Requirement: El token emitido está firmado y contiene claims mínimos
Todo login válido MUST emitir un token firmado con los claims `sub`, `client`, `iat` y `exp`. `sub` identifica al usuario autenticado; `client` coincide con el valor aceptado de la solicitud; `iat` y `exp` expresan emisión y expiración, y `exp` MUST ser posterior a `iat` y futuro al emitirlo.

#### Scenario: Login válido emite un token verificable
- **WHEN** se completa `POST /auth/login` con credenciales válidas y un `client` aceptado
- **THEN** la respuesta es `200` y el token supera la verificación de integridad
- **AND** `sub` identifica al usuario autenticado y `client` coincide con el valor enviado
- **AND** `iat` y `exp` representan emisión y expiración, con `iat < exp` y `exp` futuro

### Requirement: El cliente es obligatorio en solicitudes de login
El login MUST obtener de la solicitud un `client` que no sea nulo, vacío ni compuesto solo por espacios. Este cambio modifica el contrato del Día 1, que aceptaba `client` como opcional. No se definen restricciones adicionales de formato o clientes permitidos.

#### Scenario: Client ausente o en blanco no emite token
- **WHEN** se envía `POST /auth/login` con credenciales válidas y `client` ausente, nulo o en blanco
- **THEN** la respuesta es `400`
- **AND** no se emite ni devuelve un token

### Requirement: La duración declarada corresponde con la expiración
La vigencia nominal del token MUST permanecer en 3600 segundos. La respuesta MUST conservar `expiresIn: 3600`, y la diferencia absoluta entre `expiresIn` y `exp - now` al generar la respuesta MUST ser como máximo un segundo. Esta tolerancia es una propuesta de contrato para revisión.

#### Scenario: La respuesta refleja la expiración
- **WHEN** se inspeccionan `expiresIn` y `exp` de un token recién emitido
- **THEN** `expiresIn` es `3600`
- **AND** la diferencia absoluta entre `expiresIn` y `exp - now` es como máximo un segundo

### Requirement: Los tokens expirados no son válidos
Un token MUST NOT considerarse válido cuando su instante `exp` es igual o anterior al instante actual.

#### Scenario: Token expirado es rechazado
- **WHEN** se evalúa un token cuya claim `exp` es igual o anterior al instante actual
- **THEN** el token se considera expirado y no válido

### Requirement: La respuesta HTTP documentada se preserva
Salvo el rechazo explícito de `client` ausente, nulo o en blanco, la emisión de claims MUST NOT cambiar los campos, tipos ni estados HTTP documentados para `POST /auth/login`.

#### Scenario: Login válido conserva los campos de respuesta
- **WHEN** se completa el login con credenciales y `client` válidos
- **THEN** la respuesta es `200` e incluye `token`, `expiresIn`, `user` y `debug`
- **AND** `user` conserva `username`, `displayName` y `role`
- **AND** no se requiere un campo `client` nuevo en la respuesta

#### Scenario: Faltan username o password
- **WHEN** `username` o `password` está ausente, nulo o en blanco en la solicitud
- **THEN** la respuesta conserva el estado `400` y no se emite un token

#### Scenario: Credenciales inválidas conservan el rechazo documentado
- **WHEN** se envían credenciales incorrectas a `POST /auth/login`
- **THEN** la respuesta conserva el estado `401` y no se emite un token

#### Scenario: Error no controlado conserva el estado documentado
- **WHEN** ocurre una excepción no controlada durante el login
- **THEN** la respuesta conserva el estado `500` y los campos de error `message` y `exception`

### Requirement: Los logs no exponen material sensible
Los logs del flujo de autenticación MUST NOT incluir tokens, secretos ni material de firma.

#### Scenario: El procesamiento de login no registra valores sensibles
- **WHEN** se procesa un login válido o inválido
- **THEN** los logs no contienen tokens, secretos ni material de firma

### Requirement: El secreto predeterminado se limita al laboratorio
Un secreto predeterminado, si se conserva, MUST estar documentado como exclusivo del laboratorio y MUST NOT ser el valor predeterminado fuera de ese entorno.

#### Scenario: Configuración fuera del laboratorio
- **WHEN** se evalúa la configuración de autenticación fuera del laboratorio
- **THEN** no se usa el secreto predeterminado de laboratorio
- **AND** la documentación identifica explícitamente cualquier secreto predeterminado como exclusivo del laboratorio

### Requirement: La decisión sobre el mecanismo de firma queda documentada
La documentación de la decisión técnica MUST indicar si se conserva HMAC o se adopta una biblioteca JWT y justificar la elección, sin cambiar los requisitos observables de esta spec.

#### Scenario: Decisión de firma disponible para revisión
- **WHEN** se revisa la documentación de la decisión técnica para implementar el Issue 11
- **THEN** identifica si se conserva HMAC o se adopta una biblioteca JWT
- **AND** incluye la justificación sin prescribir aquí algoritmo o biblioteca
