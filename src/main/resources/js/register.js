 const registerForm = document.getElementById('registerForm');
        const usernameInput = document.getElementById('username');
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirmPassword');
        const usernameError = document.getElementById('usernameError');
        const emailError = document.getElementById('emailError');
        const passwordError = document.getElementById('passwordError');
        const confirmPasswordError = document.getElementById('confirmPasswordError');
        const errorAlert = document.getElementById('errorAlert');
        const successAlert = document.getElementById('successAlert');
        const loading = document.getElementById('loading');
        const googleRegisterBtn = document.getElementById('googleRegisterBtn');
        const passwordStrength = document.getElementById('passwordStrength');
        const passwordStrengthBar = document.getElementById('passwordStrengthBar');
        const passwordStrengthText = document.getElementById('passwordStrengthText');

        // Валидация email
        function validateEmail(email) {
            const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return re.test(email);
        }

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
        passwordInput.addEventListener('input', () => {
            const password = passwordInput.value;
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
                    color = '#26de81';
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

            passwordInput.classList.remove('error');
            passwordError.classList.remove('show');
        });

        // Показать ошибку
        function showError(message) {
            errorAlert.textContent = message;
            errorAlert.classList.add('show');
            setTimeout(() => {
                errorAlert.classList.remove('show');
            }, 5000);
        }

        // Показать успех
        function showSuccess(message) {
            successAlert.textContent = message;
            successAlert.classList.add('show');
            setTimeout(() => {
                successAlert.classList.remove('show');
            }, 3000);
        }

        // Валидация полей
        usernameInput.addEventListener('blur', () => {
            if (!usernameInput.value.trim()) {
                usernameInput.classList.add('error');
                usernameError.classList.add('show');
            } else {
                usernameInput.classList.remove('error');
                usernameError.classList.remove('show');
            }
        });

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

        confirmPasswordInput.addEventListener('blur', () => {
            if (confirmPasswordInput.value !== passwordInput.value && confirmPasswordInput.value) {
                confirmPasswordInput.classList.add('error');
                confirmPasswordError.classList.add('show');
            } else {
                confirmPasswordInput.classList.remove('error');
                confirmPasswordError.classList.remove('show');
            }
        });

        // Убрать ошибки при вводе
        usernameInput.addEventListener('input', () => {
            usernameInput.classList.remove('error');
            usernameError.classList.remove('show');
        });

        emailInput.addEventListener('input', () => {
            emailInput.classList.remove('error');
            emailError.classList.remove('show');
        });

        confirmPasswordInput.addEventListener('input', () => {
            confirmPasswordInput.classList.remove('error');
            confirmPasswordError.classList.remove('show');
        });

        // Отправка формы
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const username = usernameInput.value.trim();
            const email = emailInput.value.trim();
            const password = passwordInput.value;
            const confirmPassword = confirmPasswordInput.value;

            // Валидация
            let hasError = false;

            if (!username) {
                usernameInput.classList.add('error');
                usernameError.classList.add('show');
                hasError = true;
            }

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

            if (password !== confirmPassword) {
                confirmPasswordInput.classList.add('error');
                confirmPasswordError.classList.add('show');
                hasError = true;
            }

            if (hasError) return;

            // Показать загрузку
            loading.classList.add('show');
            registerForm.querySelector('.btn-primary').disabled = true;

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        username,
                        email,
                        password,
                        role: 'USER'
                    })
                });

                const data = await response.json();

                if (response.ok) {
                    showSuccess('Регистрация успешна! Перенаправление на страницу входа...');

                    // Очистить форму
                    registerForm.reset();
                    passwordStrength.classList.remove('show');
                    passwordStrengthText.classList.remove('show');

                    // Перенаправление на страницу входа через 2 секунды
                    setTimeout(() => {
                        window.location.href = 'login.html?registered=true';
                    }, 2000);
                } else {
                    throw new Error(data.message || data || 'Ошибка регистрации');
                }
            } catch (error) {
                showError(error.message || 'Произошла ошибка. Попробуйте снова.');
            } finally {
                loading.classList.remove('show');
                registerForm.querySelector('.btn-primary').disabled = false;
            }
        });

        // Регистрация через Google
        googleRegisterBtn.addEventListener('click', () => {
            window.location.href = '/oauth2/authorization/google';
        });