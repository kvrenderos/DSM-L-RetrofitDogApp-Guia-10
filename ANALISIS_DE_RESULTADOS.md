# Análisis de resultados - Guía de laboratorio 10

**Materia:** Desarrollo de Software para Móviles (DSM104)  
**Práctica:** Consumo de API REST con Retrofit  
**Proyecto:** RetrofitDogApp  
**Fecha de verificación:** 20 de octubre de 2026

## Objetivo y alcance

La práctica permite comprender el consumo de servicios REST mediante una aplicación Android en Kotlin. Se conserva la búsqueda de imágenes de perros por raza y se incorpora el ejercicio de la sección IV de la guía: buscar un usuario de GitHub y mostrar sus repositorios públicos. El ejercicio y este análisis se agregaron por solicitud del usuario; la guía se utilizó como material de referencia.

## Problemas encontrados y correcciones

La compilación inicial falló porque el proyecto utilizaba bibliotecas AndroidX sin habilitar `android.useAndroidX=true`. Además, el manifiesto contenía texto de relleno dentro de `application` y no declaraba la actividad principal ni el filtro de inicio. Se completó el manifiesto y se conservó el permiso de Internet. Esto permite que Android identifique y abra la aplicación instalada.

La configuración incluía Gradle 9.3.1 junto con Android Gradle Plugin 8.7.3. Para mantener una configuración conservadora con ese plugin se fijó Gradle 8.10.2, incluyendo su suma SHA-256. Esta decisión es un ajuste de compatibilidad; no se presenta como la causa del primer fallo observado, que fue AndroidX. También se declararon directamente RecyclerView y CardView, que antes llegaban de forma transitiva por Material, y se eliminaron kapt y Data Binding porque el proyecto utiliza View Binding y no necesita procesadores de anotaciones.

En el flujo de perros se añadió tratamiento de respuestas HTTP fallidas y listas vacías. Las imágenes nulas se filtran antes de enviarlas al adaptador. Las búsquedas cancelan la petición anterior y descartan respuestas que ya no corresponden a la consulta vigente, para evitar que una respuesta lenta reemplace datos más recientes. El mismo criterio se aplica a GitHub.

Lint detectó una referencia a notificaciones dentro de la dependencia Picasso 2.8. Esta aplicación solamente carga imágenes en ImageView y no publica notificaciones. Se documentó esa situación en `app/lint.xml` y se excluyó puntualmente el diagnóstico `NotificationPermission` cuyo mensaje menciona `com.squareup.picasso.RemoteViewsAction.NotificationAction`; no se agregó un permiso de notificaciones innecesario ni se desactivó la revisión completa de Lint.

## Funcionamiento de Retrofit y presentación de datos

En la búsqueda de perros se usa una solicitud GET a `https://dog.ceo/api/breed/{raza}/images`. Gson transforma las propiedades JSON `status` y `message` en el modelo `DogsResponse`; la lista de direcciones se presenta mediante RecyclerView y Picasso carga las imágenes. El procesamiento se realiza con `enqueue`, por lo que la espera de red no bloquea el hilo de la interfaz.

Para GitHub se agregó un servicio independiente con la URL base `https://api.github.com/` y el endpoint `users/{username}/repos`. A diferencia de Dog CEO, este endpoint devuelve un arreglo de repositorios directamente, por lo que el método de Retrofit retorna `Call<List<GithubRepository>>`. El modelo relaciona `stargazers_count` con el número de estrellas y admite descripciones y lenguajes nulos. La interfaz muestra valores alternativos cuando esos campos no están disponibles.

El listado se obtiene en páginas de 30 elementos. La cabecera `Link` de la respuesta indica si existe una página siguiente; en ese caso se habilita **Cargar más**. No se confunde la primera página con el total de repositorios del usuario: la pantalla indica cuántos se han cargado. Una nueva búsqueda limpia la lista anterior.

## Manejo de resultados y errores

Una respuesta exitosa con una lista vacía se muestra como un usuario sin repositorios públicos. Un 404 se informa como usuario no encontrado. Los códigos 403 y 429 se presentan como una restricción de solicitudes; no se analiza en detalle la causa de cada 403. Las fallas de transporte o conversión pasan por `onFailure` y muestran un mensaje de conexión y una opción para reintentar. En estos casos el mensaje es general y no distingue entre todos los posibles errores de transporte y formato.

La validación local impide enviar usuarios vacíos o con caracteres no admitidos. Las solicitudes son públicas y no se incluye ningún token en el código. Por ello, su disponibilidad está sujeta a los límites del servicio.

## Verificación

Las pruebas de conectividad realizadas desde este equipo obtuvieron:

| Consulta real | Resultado observado |
| --- | --- |
| Dog CEO, raza `husky` | HTTP 200; 191 direcciones de imágenes en la respuesta de ese momento. |
| GitHub, usuario `octocat`, página 1 con 2 elementos | HTTP 200; 2 repositorios y cabecera `Link` con página siguiente. |
| GitHub, usuario de prueba inexistente | HTTP 404. |

Estos resultados corresponden a consultas HTTP desde el equipo y no prueban por sí solos el renderizado de imágenes en Android. El número de imágenes y los repositorios disponibles pueden cambiar.

La compilación `assembleDebug` finalizó correctamente y generó un APK de depuración. `testDebugUnitTest` ejecutó 4 pruebas sin fallos: 3 verifican contratos de datos y solicitudes HTTP, y 1 corresponde al ejemplo original del proyecto. Las pruebas de contrato utilizan un interceptor local de OkHttp, por lo que no dependen de la disponibilidad de Internet.

La revisión `lintDebug` terminó con 0 errores y 36 advertencias, principalmente sobre versiones más recientes de dependencias y herramientas. Se mantuvieron las versiones del proyecto y de la guía para limitar el alcance de la corrección; una migración general a versiones nuevas requiere sus propias pruebas. La exclusión específica del diagnóstico de Picasso se describe en la sección de correcciones.

`connectedDebugAndroidTest` ejecutó 2 pruebas sin fallos en el emulador Medium_Phone_2 con Android 7.0 / API 24. Se comprobó el paquete de la aplicación y la navegación desde perros hasta GitHub, el rechazo de un usuario con caracteres inválidos y el regreso a la pantalla inicial. Estas pruebas de interfaz no incluyen descargar y visualizar imágenes reales ni completar una búsqueda válida de GitHub dentro del emulador. La validación final conjunta de compilación, Lint y pruebas instrumentadas terminó con `BUILD SUCCESSFUL`; el registro se incluye en `VERIFICACION_COMPILACION.txt` dentro del proyecto entregado.

## Interpretación de los resultados

La implementación muestra que Retrofit permite reutilizar el mismo patrón de consumo para servicios con estructuras JSON distintas. La URL base, los parámetros y el modelo de respuesta cambian; el flujo de solicitud asíncrona, validación HTTP y actualización del adaptador permanece similar. View Binding facilita el acceso a las vistas y evita conversiones manuales de identificadores.

La separación entre servicio, cliente, modelo, adaptador y actividad permite localizar cada responsabilidad. El modelo describe los datos, Retrofit construye la solicitud, Gson realiza la conversión y RecyclerView representa la colección. Manejar únicamente `onFailure` sería insuficiente: un 404 es una respuesta HTTP recibida y debe revisarse en `onResponse`.

## Conclusiones y limitaciones

La práctica integra dos API públicas y cubre tanto datos válidos como estados vacíos y errores. La búsqueda de repositorios cumple el objetivo de la discusión de resultados sin retirar el ejercicio original de perros.

La aplicación requiere Internet, no guarda resultados para uso sin conexión y no conserva la lista al recrearse una actividad. Como mejoras futuras se pueden incorporar ViewModel para conservar el estado, caché local y una estrategia de reintentos más específica. La verificación indicada se limita a los escenarios realmente ejecutados y no implica una prueba exhaustiva en todos los dispositivos Android.

## Referencias

- Guía DSM104, laboratorio 10, secciones III y IV, proporcionada por el usuario.
- Android Developers, compatibilidad de Android Gradle Plugin 8.7: https://developer.android.com/build/releases/agp-8-7-0-release-notes
- GitHub Docs, List repositories for a user: https://docs.github.com/en/rest/repos/repos#list-repositories-for-a-user
