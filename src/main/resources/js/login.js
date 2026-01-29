 // Проверка URL параметров для отображения ошибок
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('error')) {
        showError('Неверный email или пароль');
    }
    if (urlParams.get('registered')) {
        // Можно добавить успешное сообщение о регистрации
    }

    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    const errorAlert = document.getElementById('errorAlert');
    const loading = document.getElementById('loading');
    const googleLoginBtn = document.getElementById('googleLoginBtn');

    // Валидация email
    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    // Показать ошибку
    function showError(message) {
        errorAlert.textContent = message;
        errorAlert.classList.add('show');
        setTimeout(() => {
            errorAlert.classList.remove('show');
        }, 5000);
    }

    // Валидация формы
    emailInput.addEventListener('blur', () => {
        if (!validateEmail(emailInput.value) && emailInput.value) {
            emailInput.classList.add('error');
            emailError.classList.add('show');
        } else {
            emailInput.classList.remove('error');
            emailError.classList.remove('show');
        }
    });

    passwordInput.addEventListener('blur', () => {
        if (passwordInput.value.length < 6 && passwordInput.value) {
            passwordInput.classList.add('error');
            passwordError.classList.add('show');
        } else {
            passwordInput.classList.remove('error');
            passwordError.classList.remove('show');
        }
    });

    // Отправка формы
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        // Валидация
        if (!validateEmail(email)) {
            emailInput.classList.add('error');
            emailError.classList.add('show');
            return;
        }

        if (password.length < 6) {
            passwordInput.classList.add('error');
            passwordError.classList.add('show');
            return;
        }

        // Показать загрузку
        loading.classList.add('show');
        loginForm.querySelector('.btn-primary').disabled = true;

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok) {
                // Сохранить токен
                localStorage.setItem('token', data.token);
                localStorage.setItem('user', JSON.stringify({
                    id: data.id,
                    username: data.username,
                    email: data.email,
                    role: data.role
                }));

                // Перенаправление на главную страницу
                window.location.href = '/dashboard.html';
            } else {
                throw new Error(data.message || 'Ошибка входа');
            }
        } catch (error) {
            showError(error.message || 'Произошла ошибка. Попробуйте снова.');
        } finally {
            loading.classList.remove('show');
            loginForm.querySelector('.btn-primary').disabled = false;
        }
    });

    // Вход через Google
    googleLoginBtn.addEventListener('click', () => {
        window.location.href = '/oauth2/authorization/google';
    });

    // Убрать ошибки при вводе
    emailInput.addEventListener('input', () => {
        emailInput.classList.remove('error');
        emailError.classList.remove('show');
    });

    passwordInput.addEventListener('input', () => {
        passwordInput.classList.remove('error');
        passwordError.classList.remove('show');
    });