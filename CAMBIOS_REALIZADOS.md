# Cambios realizados en DomoTask

Fecha: 20/02/2026

---

## 1. BUGS CRÍTICOS CORREGIDOS

### Status.kt — UUID sobreescribía IDs de Firestore
- **Antes:** `val id: String = UUID.randomUUID().toString()` — cada vez que Firestore deserializaba un `Status`, se generaba un nuevo UUID ignorando el ID real del documento.
- **Después:** `val id: String = ""` — el ID se asigna correctamente desde Firestore con `.copy(id = it.id)`.

### Ticket.kt — parentId con default incorrecto
- **Antes:** `val parentId: String? = ""` — un string vacío no es lo mismo que null y causaba confusión al evaluar `isNullOrEmpty()`.
- **Después:** `val parentId: String? = null` — semántica correcta: null significa que no tiene padre.

### User.kt — Colección mutable en data class
- **Antes:** `val invitations: ArrayList<String> = ArrayList()` — usar `ArrayList` mutable en una data class es peligroso para el estado reactivo.
- **Después:** `val invitations: List<String> = emptyList()` — inmutable, seguro para StateFlow.

### BoardViewModel.kt — Llamadas suspend sin contexto de corrutina
- **Antes:** `board.members.mapNotNull { memberId -> userUseCases.getUser(memberId) }` — `mapNotNull` no es una función suspend, por lo que las llamadas a `getUser()` no se ejecutaban correctamente.
- **Después:** Se usa `coroutineScope { board.members.map { async { userUseCases.getUser(it) } }.awaitAll().filterNotNull() }` — ejecución paralela correcta con corrutinas.

### HomeViewModel.kt — Llamadas bloqueantes en transformación de Flow
- **Antes:** `userUseCases.getUser(board.createdBy)` dentro de `.map {}` en un Flow — bloqueaba el hilo.
- **Después:** Se usa `coroutineScope { list.map { async { ... } }.awaitAll() }` — ejecución asíncrona correcta dentro del Flow.

### UserRepository.kt — Crash potencial en getUserByInvitationCode
- **Antes:** Usaba `snap.documents.firstOrNull()` seguido de `snap.documents.first()` — podía lanzar NoSuchElementException si la query estaba vacía.
- **Después:** Se guarda el documento en una variable `doc` y se usa consistentemente.

### UserRepository.kt — try-catch inútil en updateFcmToken
- **Antes:** `try { ... } catch (e: Exception) { throw e }` — capturaba para volver a lanzar sin hacer nada.
- **Después:** Se eliminó el try-catch innecesario, dejando que la excepción se propague naturalmente.

---

## 2. SEGURIDAD

### build.gradle.kts — Minificación habilitada en release
- **Antes:** `isMinifyEnabled = false` — código sin ofuscar en builds de producción.
- **Después:** `isMinifyEnabled = true` y `isShrinkResources = true` — el código se ofusca y los recursos no usados se eliminan.

### build.gradle.kts — Dependencias duplicadas eliminadas
- Se eliminaron 3 duplicados de `play.services.fido` y 1 duplicado de `foundation.android`.

### build.gradle.kts — BuildConfig habilitado
- Se añadió `buildFeatures { buildConfig = true }` para poder usar `BuildConfig.GOOGLE_CLIENT_ID`.

### AppModule.kt — Google Client ID movido a BuildConfig
- **Antes:** El Client ID estaba hardcodeado como string literal en el código fuente.
- **Después:** Se define en `defaultConfig` como `buildConfigField` y se accede vía `BuildConfig.GOOGLE_CLIENT_ID`.
- Se eliminó la anotación `@Named("googleClientId")` y el provider dedicado.

### AndroidManifest.xml — Permiso INTERNET y backup deshabilitado
- Se añadió `<uses-permission android:name="android.permission.INTERNET" />`.
- Se cambió `android:allowBackup="true"` a `android:allowBackup="false"` para evitar que tokens y datos sensibles se respalden.

### data_extraction_rules.xml — Configurado correctamente
- **Antes:** Secciones vacías con TODOs.
- **Después:** Se excluyen `root`, `database` y `sharedpref` de cloud-backup y device-transfer.

### backup_rules.xml — Configurado correctamente
- **Antes:** Template vacío.
- **Después:** Se excluyen `sharedpref` y `database` del backup.

### proguard-rules.pro — Reglas añadidas
- **Antes:** Solo comentarios de template.
- **Después:** Reglas para Firebase, modelos de dominio, Hilt/Dagger, Coroutines y line numbers para crash reports.

---

## 3. AUTENTICACIÓN — De callbacks a suspend functions

### AuthRepository.kt — Migración completa a corrutinas
- **Antes:** `signInWithEmail`, `signInWithGoogle` y `registerWithEmail` usaban callbacks (`onResult: (Boolean, String?) -> Unit`).
- **Después:** Los tres métodos son ahora `suspend fun` que usan `.await()` de kotlinx-coroutines-play-services.
- Se eliminó la dependencia de `FirebaseFirestore` del constructor (no se usaba).

### LoginViewModel.kt — Simplificado con corrutinas
- **Antes:** Callback hell con `viewModelScope.launch` anidados dentro de callbacks de Firebase.
- **Después:** Un solo `viewModelScope.launch` con try-catch. FCM token se actualiza con una función `suspend` dedicada.
- `sendPasswordReset` también convertido a suspend con `.await()`.

### RegisterViewModel.kt — Simplificado con corrutinas
- **Antes:** Callback con `viewModelScope.launch` anidado.
- **Después:** Un solo `viewModelScope.launch` con try-catch limpio.
- `RegisterState` ahora usa `data object` en vez de `object` para Idle, Loading y Success.

---

## 4. CAPA DE DATOS

### BoardRepository.kt — addMemberToBoard optimizado
- **Antes:** Usaba `firestore.runTransaction` para leer el board completo y actualizar la lista de miembros manualmente.
- **Después:** Usa `FieldValue.arrayUnion(userId)` — operación atómica de Firestore, más eficiente y sin race conditions.

### BoardRepository.kt — addSubTicket corregido (doble fuente de verdad)
- **Antes:** Los sub-tickets se guardaban en una subcolección separada (`subtickets`) dentro del ticket padre, creando una fuente de verdad duplicada.
- **Después:** Los sub-tickets se guardan en la misma colección `tickets` con el campo `parentId` apuntando al padre. Consistente con el query en `getTicket()` que ya buscaba por `parentId`.

---

## 5. UI / PRESENTACIÓN

### SplashViewModel.kt — Migrado a StateFlow
- **Antes:** Usaba `mutableStateOf<Boolean?>()` (Compose state) — inconsistente con el resto de ViewModels.
- **Después:** Usa `MutableStateFlow<Boolean?>` / `StateFlow<Boolean?>` — consistente con toda la app.

### SplashScreen.kt — Actualizado para StateFlow
- **Antes:** `viewModel.isUserLoggedIn.value` (acceso directo a Compose state).
- **Después:** `viewModel.isUserLoggedIn.collectAsState()` (collect del StateFlow).

### MainScreen.kt — Strings hardcodeados y ruta logout
- **Antes:** "Perfil" y "Salir" hardcodeados; "logout" se marcaba como `selected` en la NavigationBar.
- **Después:** Se usan `stringResource(R.string.nav_profile)` y `stringResource(R.string.nav_logout)`. La ruta logout nunca se marca como seleccionada. Se usa constante `LOGOUT_ROUTE` en vez de magic string. El diálogo de logout ahora usa string resources.

### HomeScreen.kt — LaunchedEffect y @SuppressLint
- **Antes:** `LaunchedEffect(viewModel.acceptRejectState.collectAsState().value)` — colectaba un StateFlow dentro de los argumentos de LaunchedEffect, causando recomposiciones innecesarias.
- **Después:** Se separa el `collectAsState()` en una variable y se pasa al `LaunchedEffect`.
- Se eliminó `@SuppressLint("UnrememberedMutableState")` del TopBar.
- Se eliminó el import de `android.annotation.SuppressLint`.

### BoardScreen.kt — @SuppressLint e imports no usados
- Se eliminó `@SuppressLint("UnrememberedMutableState")` del TopBar.
- Se eliminaron imports no usados: `android.annotation.SuppressLint`, `android.widget.Space`.

### TicketScreen.kt — @SuppressLint
- Se eliminó `@SuppressLint("UnrememberedMutableState")` del TicketScaffold.
- Se eliminó el import de `android.annotation.SuppressLint`.

### ProfileScreen.kt — Diálogo de términos no descartable + string hardcodeado
- **Antes:** `onDismissRequest = { }` impedía cerrar el diálogo de términos con el botón atrás.
- **Después:** `onDismissRequest = { showTermsDialog = false }`.
- Se cambió "Consultar políticas de uso" por `stringResource(R.string.view_terms)`.

### TicketViewModel.kt & ProfileViewModel.kt — Procesamiento de imágenes seguro
- **Antes:** `Bitmap.createScaledBitmap(original, original.width / 2, original.height / 2, true)` — sin límite de tamaño, podía causar OutOfMemoryError con imágenes grandes.
- **Después:** Se calcula un factor de escala basado en una dimensión máxima de 1024px. Se verifica null del bitmap y del inputStream. Se mejoró la calidad de compresión de 30 a 50.

### strings.xml — Nuevos strings añadidos
- `nav_profile` — "Perfil"
- `nav_logout` — "Salir"
- `logout_confirm_title` — "¿Estás seguro de que deseas cerrar sesión?"
- `logout_confirm_yes` — "Sí, cerrar"
- `view_terms` — "Consultar políticas de uso"

---

## 6. DEPENDENCIAS

### libs.versions.toml — Versiones actualizadas
| Librería | Antes | Después |
|----------|-------|---------|
| Compose BOM | 2024.04.01 | 2025.02.00 |
| Coil | 2.4.0 | 2.7.0 |

### build.gradle.kts — Dependencias duplicadas eliminadas
- `play.services.fido`: 4 → 1
- `foundation.android`: 2 → 1

---

## ARCHIVOS MODIFICADOS (resumen)

| Archivo | Tipo de cambio |
|---------|----------------|
| `domain/model/Status.kt` | Bug fix |
| `domain/model/Ticket.kt` | Bug fix |
| `domain/model/User.kt` | Bug fix |
| `domain/repository/AuthRepository.kt` | Refactor completo |
| `domain/repository/UserRepository.kt` | Bug fixes |
| `domain/repository/BoardRepository.kt` | Optimización + fix |
| `presentation/splash/SplashViewModel.kt` | Refactor |
| `presentation/splash/SplashScreen.kt` | Actualización |
| `presentation/login/LoginViewModel.kt` | Refactor completo |
| `presentation/register/RegisterViewModel.kt` | Refactor completo |
| `presentation/board/BoardViewModel.kt` | Bug fix |
| `presentation/home/HomeViewModel.kt` | Bug fix |
| `presentation/main/MainScreen.kt` | UI fixes |
| `presentation/home/HomeScreen.kt` | UI fixes |
| `presentation/board/BoardScreen.kt` | Cleanup |
| `presentation/ticket/TicketScreen.kt` | Cleanup |
| `presentation/ticket/TicketViewModel.kt` | Safe bitmap |
| `presentation/profile/ProfileScreen.kt` | UI fixes |
| `presentation/profile/ProfileViewModel.kt` | Safe bitmap |
| `di/AppModule.kt` | Seguridad |
| `AndroidManifest.xml` | Seguridad |
| `res/xml/data_extraction_rules.xml` | Seguridad |
| `res/xml/backup_rules.xml` | Seguridad |
| `res/values/strings.xml` | Nuevos strings |
| `proguard-rules.pro` | Reglas añadidas |
| `app/build.gradle.kts` | Seguridad + cleanup |
| `gradle/libs.versions.toml` | Versiones actualizadas |

---

## 7. MATERIAL 3 — Migración completa a M3

### Theme.kt — Tipografía aplicada
- **Antes:** `MaterialTheme(colorScheme = colorScheme, content = content)` — la tipografía personalizada `AppTypography` (Blinker + Asul) estaba definida pero **no se aplicaba**.
- **Después:** `MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)` — las fuentes ahora se aplican en toda la app.

### themes.xml — Tema XML migrado a M3
- **Antes:** `android:Theme.Material.Light.NoActionBar` — tema antiguo del sistema.
- **Después:** `Theme.Material3.DayNight.NoActionBar` — tema M3 con soporte automático de modo oscuro.

### build.gradle.kts + libs.versions.toml — Dependencia Material Components añadida
- Se añadió `com.google.android.material:material:1.12.0` como dependencia explícita para soportar el tema XML M3.

### HomeScreen.kt — TopBar migrada a CenterAlignedTopAppBar
- **Antes:** `Box` con `.background(primaryContainer)` y posicionamiento manual con `Modifier.align()`.
- **Después:** `CenterAlignedTopAppBar` de M3 con `colors = TopAppBarDefaults.centerAlignedTopAppBarColors(...)` — layout automático, título centrado, acciones en slot dedicado.
- Se eliminó import no utilizado de `background`.

### BoardScreen.kt — TopBar migrada a TopAppBar
- **Antes:** `Box` con `.background()`, `.onGloballyPositioned()` y cálculo manual de offsets (`barWidthPx`, `menuWidthPx`, `offsetDp`, `LocalDensity`) para posicionar los `DropdownMenu`.
- **Después:** `TopAppBar` de M3 con `navigationIcon`, `title`, `colors` y `actions`. Los `DropdownMenu` ahora están anclados a sus botones respectivos usando `Box` wrappers dentro del slot `actions` — sin cálculos manuales de posición.
- Se eliminaron imports no utilizados: `onGloballyPositioned`, `LocalDensity`, `DpOffset`.

### TicketScreen.kt — TicketTopBar migrada a TopAppBar
- **Antes:** `Box` con `.background(primaryContainer)` y posicionamiento manual con `Modifier.align()`.
- **Después:** `TopAppBar` de M3 con `navigationIcon`, `title`, `colors` y `actions` — layout automático, colores gestionados por `TopAppBarDefaults.topAppBarColors()`.

### BoardScreen.kt — PostItTicketCard colores M3
- **Antes:** `Color.Black` hardcodeado para título y descripción del ticket. Overlay `Color.Black.copy(alpha = 0.05f)` innecesario.
- **Después:** Se usa `MaterialTheme.colorScheme.onSurface` para título y `MaterialTheme.colorScheme.onSurfaceVariant` para descripción. Se eliminó el overlay redundante.

### TicketScreen.kt — Indicador de urgencia de sub-tickets M3
- **Antes:** `Color.Green`, `Color.Yellow`, `Color.Red` hardcodeados para el indicador de urgencia.
- **Después:** Se usan tokens del tema: `MaterialTheme.colorScheme.secondary` (baja), `MaterialTheme.colorScheme.tertiary` (media), `MaterialTheme.colorScheme.error` (alta).
- Se eliminó import no utilizado de `Color`.

---

## ARCHIVOS MODIFICADOS (M3)

| Archivo | Tipo de cambio |
|---------|----------------|
| `ui/theme/Theme.kt` | Tipografía aplicada |
| `res/values/themes.xml` | Tema M3 |
| `app/build.gradle.kts` | Dependencia material |
| `gradle/libs.versions.toml` | Versión material añadida |
| `presentation/home/HomeScreen.kt` | CenterAlignedTopAppBar |
| `presentation/board/BoardScreen.kt` | TopAppBar + colores M3 |
| `presentation/ticket/TicketScreen.kt` | TopAppBar + colores M3 |
