document.addEventListener('DOMContentLoaded', function() {
    const resetPasswordForm = document.getElementById('resetPasswordForm');
    const tokenInput = document.getElementById('token');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const tokenError = document.getElementById('tokenError');
    const passwordError = document.getElementById('passwordError');
    const confirmPasswordError = document.getElementById('confirmPasswordError');
    const errorAlert = document.getElementById('errorAlert');
    const successAlert = document.getElementById('successAlert');
    const loading = document.getElementById('loading');
    const emailText = document.getElementById('emailText');
    const resendBtn = document.getElementById('resendBtn');
    const timer = document.getElementById('timer');
    const passwordStrength = document.getElementById('passwordStrength');
    const passwordStrengthBar = document.getElementById('passwordStrengthBar');
    const passwordStrengthText = document.getElementById('passwordStrengthText');

    // Создание дополнительных комет динамически
    createComets();

    // Получить email из sessionStorage
    const resetEmail = sessionStorage.getItem('resetEmail');
    if (resetEmail) {
        emailText.textContent = resetEmail;
    } else {
        // Если email не найден, перенаправить на страницу восстановления
        window.location.href = 'forgot-password.html';
    }

    // Таймер для повторной отправки кода
    let resendTimeout = 60; // 60 секунд
    let resendInterval;

    function startResendTimer() {
        resendBtn.disabled = true;
        resendTimeout = 60;

        resendInterval = setInterval(() => {
            resendTimeout--;
            timer.textContent = `Повторная отправка доступна через ${resendTimeout} сек`;

            if (resendTimeout <= 0) {
                clearInterval(resendInterval);
                resendBtn.disabled = false;
                timer.textContent = '';
            }
        }, 1000);
    }

    // Запустить таймер при загрузке страницы
    startResendTimer();

    // Проверка силы пароля
    function checkPasswordStrength(password) {
        let strength = 0;
        if (password.length >= 6) strength++;
        if (password.length >= 10) strength++;
        if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
        if (/\d/.test(password)) strength++;
        if (/[^a-zA-Z\d]/.test(password)) strength++;

        return strength;
    }

    // Показать силу пароля
    newPasswordInput.addEventListener('input', () => {
        const password = newPasswordInput.value;
        if (password.length > 0) {
            passwordStrength.classList.add('show');
            passwordStrengthText.classList.add('show');

            const strength = checkPasswordStrength(password);
            let width, color, text;

            if (strength <= 2) {
                width = '33%';
                color = '#ff4757';
                text = 'Слабый пароль';
            } else if (strength <= 3) {
                width = '66%';
                color = '#ffa502';
                text = 'Средний пароль';
            } else {
                width = '100%';
                color = '#2ed573';
                text = 'Сильный пароль';
            }

            passwordStrengthBar.style.width = width;
            passwordStrengthBar.style.background = color;
            passwordStrengthText.textContent = text;
            passwordStrengthText.style.color = color;
        } else {
            passwordStrength.classList.remove('show');
            passwordStrengthText.classList.remove('show');
        }

        newPasswordInput.classList.remove('error');
        passwordError.classList.remove('show');
    });

    // Показать ошибку
    function showError(message) {
        errorAlert.textContent = message;
        errorAlert.classList.add('show');
        successAlert.classList.remove('show');
        setTimeout(() => {
            errorAlert.classList.remove('show');
        }, 5000);
    }

    // Показать успех
    function showSuccess(message) {
        successAlert.textContent = message;
        successAlert.classList.add('show');
        errorAlert.classList.remove('show');
    }

    // Валидация кода (только цифры)
    tokenInput.addEventListener('input', (e) => {
        e.target.value = e.target.value.replace(/\D/g, '');
        tokenInput.classList.remove('error');
        tokenError.classList.remove('show');
    });

    // Валидация при потере фокуса
    tokenInput.addEventListener('blur', () => {
        if (tokenInput.value.length !== 6 && tokenInput.value) {
            tokenInput.classList.add('error');
            tokenError.classList.add('show');
        }
    });

    newPasswordInput.addEventListener('blur', () => {
        if (newPasswordInput.value.length < 6 && newPasswordInput.value) {
            newPasswordInput.classList.add('error');
            passwordError.classList.add('show');
        }
    });

    confirmPasswordInput.addEventListener('blur', () => {
        if (confirmPasswordInput.value !== newPasswordInput.value && confirmPasswordInput.value) {
            confirmPasswordInput.classList.add('error');
            confirmPasswordError.classList.add('show');
        }
    });

    // Убрать ошибки при вводе
    confirmPasswordInput.addEventListener('input', () => {
        confirmPasswordInput.classList.remove('error');
        confirmPasswordError.classList.remove('show');
    });

    // Отправка формы
    resetPasswordForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const token = tokenInput.value.trim();
        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        // Валидация
        let hasError = false;

        if (token.length !== 6) {
            tokenInput.classList.add('error');
            tokenError.classList.add('show');
            hasError = true;
        }

        if (newPassword.length < 6) {
            newPasswordInput.classList.add('error');
            passwordError.classList.add('show');
            hasError = true;
        }

        if (newPassword !== confirmPassword) {
            confirmPasswordInput.classList.add('error');
            confirmPasswordError.classList.add('show');
            hasError = true;
        }

        if (hasError) {
            showError('Пожалуйста, исправьте ошибки в форме');
            return;
        }

        // Показать загрузку
        loading.classList.add('show');
        resetPasswordForm.querySelector('.btn-primary').disabled = true;

        try {
            const response = await fetch('/api/password/reset/confirm', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    token,
                    newPassword
                })
            });

            const data = await response.json();

            if (response.ok) {
                showSuccess('✅ Пароль успешно изменён! Перенаправление на страницу входа...');

                // Очистить sessionStorage
                sessionStorage.removeItem('resetEmail');

                // Очистить форму
                resetPasswordForm.reset();
                passwordStrength.classList.remove('show');
                passwordStrengthText.classList.remove('show');

                // Перенаправление на страницу входа через 2 секунды
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 2000);
            } else {
                throw new Error(data.message || data || 'Ошибка сброса пароля');
            }
        } catch (error) {
            showError(error.message || 'Произошла ошибка. Попробуйте снова.');
        } finally {
            loading.classList.remove('show');
            resetPasswordForm.querySelector('.btn-primary').disabled = false;
        }
    });

    // Повторная отправка кода
    resendBtn.addEventListener('click', async () => {
        if (!resetEmail) return;

        resendBtn.disabled = true;

        try {
            const response = await fetch('/api/password/reset/request', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email: resetEmail })
            });

            if (response.ok) {
                showSuccess('📧 Код отправлен повторно!');
                startResendTimer();
            } else {
                throw new Error('Ошибка отправки кода');
            }
        } catch (error) {
            showError('Не удалось отправить код. Попробуйте позже.');
            resendBtn.disabled = false;
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
});