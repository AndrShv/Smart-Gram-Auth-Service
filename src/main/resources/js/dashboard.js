 // Проверка авторизации
        const token = localStorage.getItem('token');
        const userDataString = localStorage.getItem('user');

        if (!token || !userDataString) {
            // Если нет токена, перенаправить на страницу входа
            window.location.href = 'login.html';
        }

        // Получить данные пользователя
        const userData = JSON.parse(userDataString);

        // Заполнить данные пользователя
        document.getElementById('userName').textContent = userData.username;
        document.getElementById('userAvatar').textContent = userData.username.charAt(0).toUpperCase();
        document.getElementById('userId').textContent = userData.id;
        document.getElementById('userNameDetail').textContent = userData.username;
        document.getElementById('userEmail').textContent = userData.email;
        document.getElementById('userRole').textContent = userData.role;

        // Показать приветственное сообщение для новых пользователей
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('new') === 'true') {
            document.getElementById('welcomeAlert').classList.add('show');
            setTimeout(() => {
                document.getElementById('welcomeAlert').classList.remove('show');
            }, 5000);
        }

        // Выход
        document.getElementById('logoutBtn').addEventListener('click', () => {
            // Очистить localStorage
            localStorage.removeItem('token');
            localStorage.removeItem('user');

            // Перенаправить на страницу входа
            window.location.href = 'login.html';
        });

        // Функция для проверки токена (можно вызывать периодически)
        async function verifyToken() {
            try {
                const response = await fetch('/api/user/profile', {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!response.ok) {
                    throw new Error('Unauthorized');
                }
            } catch (error) {
                // Если токен недействителен, выйти
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = 'login.html';
            }
        }

        // Проверить токен при загрузке
        // verifyToken(); // Раскомментировать, когда API будет готов