# Hallazgos candidatos - Auth backend

Inspeccion estatica del repositorio. Todos los puntos son candidatos para revision humana; no se crearon issues ni se ejecutaron pruebas. El laboratorio debe mantenerse aislado.

### B-01 - SQL de autenticacion concatena credenciales
- Estado: CANDIDATO
- Evidencia: `src/main/java/com/bcp/workshop/auth/LegacyDatabase.java:32` concatena `username` y `password` dentro de la consulta SQL.
- Impacto: Entradas especiales pueden alterar la semantica de la consulta de autenticacion y comprometer la identidad resultante.
- Prioridad propuesta: P1 si el servicio sale del laboratorio aislado.
- Contrato potencialmente afectado: `POST /auth/login`, campos `username` y `password`.
- Como validarlo: Agregar pruebas de integracion con valores de entrada que contengan caracteres especiales y comprobar que se traten como datos, no como parte de la consulta; verificar que no autentiquen a otro usuario.

### B-02 - Token codificado sin firma criptografica
- Estado: CANDIDATO
- Evidencia: `src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java:8` define un secreto estatico; `:14` concatena claims y secreto; `:15` solo codifica el resultado con Base64.
- Impacto: La codificacion Base64 no autentica ni protege los claims. El API consumidor tambien confia en el mismo formato y secreto, por lo que la identidad/rol del token podria no ser confiable.
- Prioridad propuesta: P1 si el token se acepta fuera del entorno de entrenamiento.
- Contrato potencialmente afectado: valor de `token` en la respuesta de `POST /auth/login` y su validacion por los consumidores.
- Como validarlo: En pruebas aisladas, confirmar que cambiar claims de un token invalida su verificacion; exigir una firma verificable y comprobar que claims alterados se rechacen.
