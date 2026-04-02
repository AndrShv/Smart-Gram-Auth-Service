document.addEventListener('DOMContentLoaded', function() {
    // Элементы формы
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    const errorAlert = document.getElementById('errorAlert');
    const loading = document.getElementById('loading');

    // Создание дополнительных комет динамически
    createComets();

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

    // Валидация email поля
    emailInput.addEventListener('blur', () => {
        if (!validateEmail(emailInput.value) && emailInput.value) {
            emailInput.classList.add('error');
            emailError.classList.add('show');
        } else {
            emailInput.classList.remove('error');
            emailError.classList.remove('show');
        }
    });

    // Валидация пароля поля
    passwordInput.addEventListener('blur', () => {
        if (passwordInput.value.length < 6 && passwordInput.value) {
            passwordInput.classList.add('error');
            passwordError.classList.add('show');
        } else {
            passwordInput.classList.remove('error');
            passwordError.classList.remove('show');
        }
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

    // Отправка формы
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        // Валидация
        let hasError = false;

        if (!validateEmail(email)) {
            emailInput.classList.add('error');
            emailError.classList.add('show');
            hasError = true;
        }

        if (password.length < 6) {
            passwordInput.classList.add('error');
            passwordError.classList.add('show');
            hasError = true;
        }

        if (hasError) {
            showError('Пожалуйста, исправьте ошибки в форме');
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

    // Создание дополнительных комет
    function createComets() {
        const cometsContainer = document.querySelector('.comets');
        if (!cometsContainer) return;

        const cometCount = 5;

        for (let i = 0; i < cometCount; i++) {
            const comet = document.createElement('div');
            comet.className = 'comet-trail';
            comet.style.cssText = `
                position: absolute;
                width: 2px;
                height: 2px;
                background: #fff;
                border-radius: 50%;
                box-shadow: 0 0 10px 2px rgba(255, 255, 255, 0.8);
                top: ${Math.random() * 100}%;
                left: ${Math.random() * 100}%;
                animation: comet ${8 + Math.random() * 4}s linear infinite;
                animation-delay: ${Math.random() * 8}s;
            `;
            cometsContainer.appendChild(comet);
        }
    }

    // Проверка URL параметров для отображения ошибок
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('error')) {
        showError('Неверный email или пароль');
    }
    if (urlParams.get('registered')) {
        // Можно добавить успешное сообщение о регистрации
        const successAlert = document.createElement('div');
        successAlert.className = 'alert alert-success show';
        successAlert.style.cssText = `
            background: rgba(46, 213, 115, 0.15);
            color: #2ed573;
            border-color: #2ed573;
        `;
        successAlert.textContent = 'Регистрация успешна! Теперь вы можете войти.';
        errorAlert.parentNode.insertBefore(successAlert, errorAlert);

        setTimeout(() => {
            successAlert.classList.remove('show');
        }, 5000);
    }
});