# RetrofitDogApp - Guía 10

Aplicación Android en Kotlin que consulta imágenes de perros por raza y repositorios públicos de GitHub por nombre de usuario. El ejercicio de GitHub corresponde a la sección IV, Discusión de resultados, de la guía DSM104.

## Abrir y compilar

1. Extraer el ZIP y abrir la carpeta `RetrofitDogApp` en Android Studio.
2. Seleccionar el JDK integrado de Android Studio (se verificó con JDK 21; AGP 8.7 requiere como mínimo JDK 17).
3. Instalar Android SDK Platform 35 desde SDK Manager y sincronizar Gradle.
4. Ejecutar en un teléfono o emulador con Android 7.0 / API 24 o superior.

La distribución no incluye `local.properties`: Android Studio genera la ruta del SDK de cada equipo. Si se compila desde terminal, definir `ANDROID_HOME` o crear `local.properties` con `sdk.dir`.

En Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

El último comando requiere un dispositivo o emulador conectado. El APK de depuración se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Uso

- Perros: escribir una raza en inglés, como `husky`, y pulsar Buscar en el teclado.
- GitHub: pulsar **GitHub: repositorios públicos**, ingresar `octocat` u otro usuario y pulsar Buscar.
- Cada repositorio muestra nombre, descripción, lenguaje y estrellas. **Cargar más** obtiene la siguiente página cuando existe.
- **Volver a perros** regresa a la práctica original.

Las consultas públicas de GitHub no requieren un token. La aplicación informa errores de conexión, usuarios inexistentes y restricciones de solicitudes. Se necesita conexión a Internet para obtener datos nuevos.

## Cambios principales

- AndroidX habilitado en `gradle.properties`.
- Manifiesto completo con tema, actividad principal, permiso de Internet y actividad de GitHub.
- Wrapper fijado en Gradle 8.10.2 con verificación SHA-256; se conserva AGP 8.7.3 y Kotlin 2.0.21.
- Dependencias directas de RecyclerView y CardView declaradas.
- View Binding conservado; Data Binding y kapt retirados porque no se usan.
- Retirada la configuración generada que forzaba descargar un JDK: se utiliza el JDK configurado en Android Studio o `JAVA_HOME`.
- Búsquedas anteriores canceladas para evitar que respuestas viejas sustituyan resultados recientes; cancelación al destruir actividades.
- Manejo de respuestas HTTP fallidas, listas vacías y campos opcionales.
- Agregados modelos, cliente, servicio Retrofit, adaptador y pantalla de repositorios de GitHub.
- Exclusión documentada en `app/lint.xml` para el diagnóstico de `NotificationPermission` que menciona exclusivamente `Picasso.RemoteViewsAction.NotificationAction`: la app carga imágenes en ImageView y no publica notificaciones.

El análisis académico y los resultados de verificación están en `ANALISIS_DE_RESULTADOS.md`.

## Referencias técnicas

- Compatibilidad de AGP 8.7: https://developer.android.com/build/releases/agp-8-7-0-release-notes
- Repositorios públicos por usuario: https://docs.github.com/en/rest/repos/repos#list-repositories-for-a-user

La app no conserva los resultados al recrearse una actividad: después de una rotación puede ser necesario volver a buscar. La paginación es manual y no se implementó almacenamiento sin conexión.
