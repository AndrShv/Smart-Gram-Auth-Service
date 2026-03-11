# SmartGram - Инструкция по настройке контроллеров

## 📁 Структура проекта

```
src/main/
├── java/com/example/project/
│   ├── controller/
│   │   ├── AuthViewController.java          # Контроллер страниц аутентификации
│   │   ├── MainViewController.java          # Контроллер основных страниц
│   │   └── CustomErrorController.java       # Контроллер ошибок
│   ├── configs/
│   │   ├── WebMvcConfig.java               # Конфигурация MVC
│   │   └── SecurityConfig.java             # Уже существует
│   └── rest/
│       ├── AuthRestController.java         # Уже существует
│       └── PasswordResetRestController.java # Уже существует
│
└── resources/
    ├── static/                             # Статические ресурсы
    │   ├── login.html
    │   ├── register.html
    │   ├── forgot-password.html
    │   ├── reset-password.html
    │   ├── dashboard.html
    │   ├── css/                            # Опционально: отдельные CSS файлы
    │   ├── js/                             # Опционально: отдельные JS файлы
    │   └── images/                         # Изображения
    └── application.properties
```

## 🔧 Настройка

### 1. Добавьте контроллеры в ваш проект

Скопируйте Java файлы в соответствующие пакеты:
- `AuthViewController.java` → `src/main/java/com/example/project/controller/`
- `MainViewController.java` → `src/main/java/com/example/project/controller/`
- `CustomErrorController.java` → `src/main/java/com/example/project/controller/`
- `WebMvcConfig.java` → `src/main/java/com/example/project/configs/`

### 2. Разместите HTML файлы

Скопируйте все HTML файлы в `src/main/resources/static/`:
- login.html
- register.html
- forgot-password.html
- reset-password.html
- dashboard.html

### 3. Обновите SecurityConfig

Убедитесь, что ваш `SecurityConfig` разрешает доступ к HTML страницам без авторизации.

Уже настроено в вашем текущем конфиге:
```java
.requestMatchers(
    "/api/auth/**",
    "/api/password/reset/**",
    "/auth/**",           // ✅ Страницы аутентификации
    "/oauth2/**",         // ✅ OAuth2
    "/css/**",            // ✅ Статические ресурсы
    "/static/**",
    "/images/**",
    "/js/**"
).permitAll()
```

### 4. Настройте application.properties (опционально)

```properties
# Настройки веб-сервера
server.port=8080
server.servlet.context-path=/

# Настройки статических ресурсов
spring.web.resources.static-locations=classpath:/static/
spring.web.resources.cache.period=3600

# Настройки OAuth2 Google (если еще не настроены)
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
```

## 🚀 URL маршруты

### Страницы аутентификации (доступны без авторизации):
- `http://localhost:8080/auth/login` - Страница входа
- `http://localhost:8080/auth/register` - Страница регистрации
- `http://localhost:8080/auth/forgot-password` - Восстановление пароля
- `http://localhost:8080/auth/reset-password` - Сброс пароля

### Основные страницы (требуют авторизации):
- `http://localhost:8080/` - Перенаправление на dashboard
- `http://localhost:8080/dashboard` - Главная страница

### REST API (уже настроены):
- `POST /api/auth/register` - Регистрация
- `POST /api/auth/login` - Вход
- `POST /api/password/reset/request` - Запрос кода для сброса пароля
- `POST /api/password/reset/confirm` - Подтверждение сброса пароля

## 🔐 OAuth2 настройка

### Получение Client ID и Secret от Google:

1. Перейдите в [Google Cloud Console](https://console.cloud.google.com/)
2. Создайте новый проект или выберите существующий
3. Перейдите в "APIs & Services" > "Credentials"
4. Нажмите "Create Credentials" > "OAuth 2.0 Client ID"
5. Выберите "Web application"
6. Добавьте Authorized redirect URIs:
    - `http://localhost:8080/login/oauth2/code/google`
    - `http://localhost:8080/oauth2/authorization/google`
7. Скопируйте Client ID и Client Secret в application.properties

## 📝 Дополнительные настройки

### Если вы хотите использовать Thymeleaf вместо статических HTML:

1. Добавьте зависимость в `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

2. Переместите HTML файлы в `src/main/resources/templates/`

3. Обновите `WebMvcConfig` для использования Thymeleaf

### Создание страниц ошибок (опционально):

Создайте файлы в `src/main/resources/static/`:
- `error-404.html` - Страница не найдена
- `error-403.html` - Доступ запрещен
- `error-500.html` - Ошибка сервера
- `error.html` - Общая страница ошибки

## 🧪 Тестирование

1. Запустите приложение
2. Откройте браузер и перейдите на `http://localhost:8080/auth/login`
3. Попробуйте:
    - ✅ Регистрацию нового пользователя
    - ✅ Вход с существующим пользователем
    - ✅ Вход через Google OAuth2
    - ✅ Восстановление пароля

## 🔍 Устранение проблем

### HTML страницы не загружаются:
- Проверьте, что файлы находятся в `src/main/resources/static/`
- Убедитесь, что в SecurityConfig разрешен доступ к этим страницам
- Проверьте логи на наличие ошибок

### OAuth2 не работает:
- Проверьте Client ID и Secret в application.properties
- Убедитесь, что redirect URI корректный в Google Console
- Проверьте, что в SecurityConfig настроен `.oauth2Login()`

### API возвращает 401:
- Убедитесь, что токен JWT правильно сохраняется в localStorage
- Проверьте, что в заголовках запроса есть `Authorization: Bearer <token>`
- Проверьте настройки CORS, если фронтенд на другом домене

## 📚 Дополнительная информация

Все HTML страницы используют vanilla JavaScript и не требуют дополнительных библиотек.
Они полностью интегрированы с вашим Spring Boot backend через REST API.

### Особенности:
- ✅ Современный адаптивный дизайн
- ✅ Валидация форм на клиенте
- ✅ Обработка ошибок
- ✅ Индикатор силы пароля
- ✅ Сохранение токенов в localStorage
- ✅ OAuth2 интеграция с Google
- ✅ Таймер для повторной отправки кода
- ✅ Плавные анимации и переходы

---

**SmartGram** - Социальная сеть нового поколения 📱✨