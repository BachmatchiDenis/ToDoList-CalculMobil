# ToDo List - Android Application

## Descriere
Aplicație Android pentru gestionarea task-urilor zilnice, cu salvare online și sincronizare printr-un REST API.

## Funcționalități Implementate

### ✅ Cerințe Minime
- **2 Activity-uri**: LoginActivity, MainActivity (+ TaskDetailActivity)
- **Web Access**: Retrofit pentru comunicare REST API

### ✅ Funcționalități Complete

#### 1. Autentificare (Local + API)
- Login local cu Room Database
- Login API cu DummyJSON (`https://dummyjson.com/auth/login`)
- Înregistrare utilizatori locali
- Demo credentials: `emilys` / `emilyspass`

#### 2. Liste (RecyclerView)
- RecyclerView cu TaskAdapter
- DiffUtil pentru actualizări eficiente
- Filtrare: All / Pending / Completed
- SwipeRefreshLayout pentru refresh

#### 3. Threads & Coroutines
- Kotlin Coroutines pentru operații asincrone
- Flow pentru observarea datelor
- viewModelScope pentru lifecycle-aware coroutines

#### 4. Services
- SyncService pentru sincronizare în background
- WorkManager pentru sync periodic (15 min)

#### 5. Storage
- Room Database pentru persistență locală
- Entities: Task, User
- DAO-uri pentru operații CRUD

#### 6. Retrofit
- TodoApiService pentru operații CRUD
- AuthApiService pentru autentificare
- OkHttp cu logging interceptor
- Gson converter

#### 7. Notificări
- Notificări la deadline (30 min înainte)
- NotificationReceiver pentru broadcast
- BootReceiver pentru reprogramare după restart

## Structura Proiectului

```
com.example.todolist/
├── adapter/
│   └── TaskAdapter.kt          # RecyclerView Adapter
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt      # Room Database
│   │   ├── TaskDao.kt          # Task DAO
│   │   └── UserDao.kt          # User DAO
│   ├── remote/
│   │   ├── ApiService.kt       # Retrofit Services
│   │   └── RetrofitClient.kt   # Retrofit Configuration
│   └── repository/
│       ├── TaskRepository.kt   # Task Repository
│       └── UserRepository.kt   # User Repository
├── model/
│   ├── Task.kt                 # Task Entity
│   └── User.kt                 # User Entity
├── receiver/
│   ├── BootReceiver.kt         # Boot Completed Receiver
│   └── NotificationReceiver.kt # Notification Receiver
├── service/
│   └── SyncService.kt          # Background Sync Service
├── ui/
│   ├── LoginActivity.kt        # Login/Register Screen
│   ├── MainActivity.kt         # Main Task List Screen
│   └── TaskDetailActivity.kt   # Task Detail/Edit Screen
├── viewmodel/
│   ├── LoginViewModel.kt       # Login ViewModel
│   └── TaskViewModel.kt        # Task ViewModel
├── worker/
│   └── SyncWorker.kt           # WorkManager Worker
└── TodoApplication.kt          # Application Class
```

## API Utilizate

### DummyJSON API
- Base URL: `https://dummyjson.com/`
- GET `/todos` - Fetch todos
- POST `/todos/add` - Add todo
- PUT `/todos/{id}` - Update todo
- DELETE `/todos/{id}` - Delete todo
- POST `/auth/login` - Login

## Tehnologii Folosite

| Tehnologie | Versiune | Utilizare |
|------------|----------|-----------|
| Kotlin | 2.0.21 | Limbaj principal |
| Room | 2.6.1 | Database local |
| Retrofit | 2.9.0 | REST API client |
| OkHttp | 4.12.0 | HTTP client |
| Coroutines | 1.7.3 | Async operations |
| Lifecycle | 2.7.0 | ViewModel, LiveData |
| WorkManager | 2.9.0 | Background sync |
| Material3 | 1.13.0 | UI Components |

## Configurare

1. Deschide proiectul în Android Studio
2. Sync Gradle
3. Run pe emulator/device

## Permisiuni

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

## Screenshots

Aplicația include:
- Ecran de login cu design Material
- Lista de task-uri cu filtre
- Dialog pentru adăugare task
- Ecran detalii task cu deadline picker
- Notificări pentru deadline-uri

