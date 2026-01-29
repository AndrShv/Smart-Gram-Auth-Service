 const forgotPasswordForm = document.getElementById('forgotPasswordForm');
        const emailInput = document.getElementById('email');
        const emailError = document.getElementById('emailError');
        const errorAlert = document.getElementById('errorAlert');
        const successAlert = document.getElementById('successAlert');
        const loading = document.getElementById('loading');

        // Валидация email
        function validateEmail(email) {
            const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return re.test(email);
        }

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

        // Валидация при потере фокуса
        emailInput.addEventListener('blur', () => {
            if (!validateEmail(emailInput.value) && emailInput.value) {
                emailInput.classList.add('error');
                emailError.classList.add('show');
            } else {
                emailInput.classList.remove('error');
                emailError.classList.remove('show');
            }
        });

        // Убрать ошибку при вводе
        emailInput.addEventListener('input', () => {
            emailInput.classList.remove('error');
            emailError.classList.remove('show');
        });

        // Отправка формы
        forgotPasswordForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = emailInput.value.trim();

            // Валидация
            if (!validateEmail(email)) {
                emailInput.classList.add('error');
                emailError.classList.add('show');
                return;
            }

            // Показать загрузку
            loading.classList.add('show');
            forgotPasswordForm.querySelector('.btn-primary').disabled = true;

            try {
                const response = await fetch('/api/password/reset/request', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ email })
                });

                const data = await response.json();

                if (response.ok) {
                    showSuccess('Код отправлен! Проверьте вашу почту.');

                    // Сохранить email для следующего шага
                    sessionStorage.setItem('resetEmail', email);

                    // Перенаправление на страницу ввода кода через 2 секунды
                    setTimeout(() => {
                        window.location.href = 'reset-password.html';
                    }, 2000);
                } else {
                    throw new Error(data.message || data || 'Ошибка отправки кода');
                }
            } catch (error) {
                showError(error.message || 'Произошла ошибка. Попробуйте снова.');
            } finally {
                loading.classList.remove('show');
                forgotPasswordForm.querySelector('.btn-primary').disabled = false;
            }
        });