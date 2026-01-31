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


 document.addEventListener('DOMContentLoaded', function() {
     // Элементы формы
     const form = document.getElementById('registerForm');
     const username = document.getElementById('username');
     const email = document.getElementById('email');
     const password = document.getElementById('password');
     const confirmPassword = document.getElementById('confirmPassword');
     const loading = document.getElementById('loading');
     const errorAlert = document.getElementById('errorAlert');
     const successAlert = document.getElementById('successAlert');

     // Элементы индикатора силы пароля
     const passwordStrength = document.getElementById('passwordStrength');
     const passwordStrengthBar = document.getElementById('passwordStrengthBar');
     const passwordStrengthText = document.getElementById('passwordStrengthText');

     // Создание дополнительных комет динамически
     createComets();

     // Валидация в реальном времени
     username.addEventListener('input', function() {
         validateUsername();
     });

     email.addEventListener('input', function() {
         validateEmail();
     });

     password.addEventListener('input', function() {
         validatePassword();
         checkPasswordStrength();
     });

     confirmPassword.addEventListener('input', function() {
         validateConfirmPassword();
     });

     // Обработка отправки формы
     form.addEventListener('submit', function(e) {
         e.preventDefault();

         // Скрываем предыдущие сообщения
         hideAlerts();

         // Валидация всех полей
         const isUsernameValid = validateUsername();
         const isEmailValid = validateEmail();
         const isPasswordValid = validatePassword();
         const isConfirmPasswordValid = validateConfirmPassword();

         if (isUsernameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid) {
             // Показываем загрузку
             loading.classList.add('show');

             // Отправляем форму (симуляция)
             setTimeout(() => {
                 form.submit();
             }, 1000);
         } else {
             showError('Пожалуйста, исправьте ошибки в форме');
         }
     });

     // Функции валидации
     function validateUsername() {
         const usernameError = document.getElementById('usernameError');
         const value = username.value.trim();

         if (value === '') {
             showFieldError(username, usernameError, 'Имя пользователя обязательно');
             return false;
         } else if (value.length < 3) {
             showFieldError(username, usernameError, 'Минимум 3 символа');
             return false;
         } else {
             hideFieldError(username, usernameError);
             return true;
         }
     }

     function validateEmail() {
         const emailError = document.getElementById('emailError');
         const value = email.value.trim();
         const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

         if (value === '') {
             showFieldError(email, emailError, 'Email обязателен');
             return false;
         } else if (!emailRegex.test(value)) {
             showFieldError(email, emailError, 'Введите корректный email');
             return false;
         } else {
             hideFieldError(email, emailError);
             return true;
         }
     }

     function validatePassword() {
         const passwordError = document.getElementById('passwordError');
         const value = password.value;

         if (value === '') {
             showFieldError(password, passwordError, 'Пароль обязателен');
             return false;
         } else if (value.length < 6) {
             showFieldError(password, passwordError, 'Минимум 6 символов');
             return false;
         } else {
             hideFieldError(password, passwordError);
             return true;
         }
     }

     function validateConfirmPassword() {
         const confirmPasswordError = document.getElementById('confirmPasswordError');
         const value = confirmPassword.value;

         if (value === '') {
             showFieldError(confirmPassword, confirmPasswordError, 'Подтвердите пароль');
             return false;
         } else if (value !== password.value) {
             showFieldError(confirmPassword, confirmPasswordError, 'Пароли не совпадают');
             return false;
         } else {
             hideFieldError(confirmPassword, confirmPasswordError);
             return true;
         }
     }

     function checkPasswordStrength() {
         const value = password.value;
         let strength = 0;
         let strengthText = '';
         let strengthColor = '';

         if (value.length === 0) {
             passwordStrength.classList.remove('show');
             passwordStrengthText.classList.remove('show');
             return;
         }

         passwordStrength.classList.add('show');
         passwordStrengthText.classList.add('show');

         // Проверка длины
         if (value.length >= 6) strength += 25;
         if (value.length >= 10) strength += 25;

         // Проверка на наличие цифр
         if (/\d/.test(value)) strength += 25;

         // Проверка на наличие спецсимволов
         if (/[!@#$%^&*(),.?":{}|<>]/.test(value)) strength += 25;

         // Установка цвета и текста
         if (strength <= 25) {
             strengthText = 'Слабый пароль';
             strengthColor = '#ff4757';
         } else if (strength <= 50) {
             strengthText = 'Средний пароль';
             strengthColor = '#ffa502';
         } else if (strength <= 75) {
             strengthText = 'Хороший пароль';
             strengthColor = '#00d4ff';
         } else {
             strengthText = 'Отличный пароль';
             strengthColor = '#2ed573';
         }

         passwordStrengthBar.style.width = strength + '%';
         passwordStrengthBar.style.background = strengthColor;
         passwordStrengthText.textContent = strengthText;
         passwordStrengthText.style.color = strengthColor;
     }

     function showFieldError(field, errorElement, message) {
         field.classList.add('error');
         errorElement.textContent = message;
         errorElement.classList.add('show');
     }

     function hideFieldError(field, errorElement) {
         field.classList.remove('error');
         errorElement.classList.remove('show');
     }

     function showError(message) {
         errorAlert.textContent = message;
         errorAlert.classList.add('show');
         setTimeout(() => {
             errorAlert.classList.remove('show');
         }, 5000);
     }

     function showSuccess(message) {
         successAlert.textContent = message;
         successAlert.classList.add('show');
         setTimeout(() => {
             successAlert.classList.remove('show');
         }, 5000);
     }

     function hideAlerts() {
         errorAlert.classList.remove('show');
         successAlert.classList.remove('show');
     }

     // Создание дополнительных комет
     function createComets() {
         const cometsContainer = document.querySelector('.comets');
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
         showError('Ошибка регистрации. Попробуйте снова.');
     }
     if (urlParams.get('success')) {
         showSuccess('Регистрация успешна! Проверьте email для активации.');
     }
 });