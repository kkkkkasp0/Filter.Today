// js/auth/login.js (Session Cookie Version)

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
});

async function handleLogin(e) {
    e.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const messageEl = document.getElementById('message');
    messageEl.textContent = '';
    messageEl.style.color = 'red';

    try {
        const response = await fetch('/api/member/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
            // ❗ 세션 쿠키를 주고받기 위해 credentials: 'include' 옵션 필수 ❗
            credentials: 'include'
        });

        if (response.ok) {
            // 서버가 쿠키(JSESSIONID)를 자동으로 설정하므로 클라이언트에서 별도로 토큰을 저장할 필요가 없습니다.
            localStorage.setItem('isLoggedIn', 'true'); // 상태 플래그만 유지

            messageEl.style.color = 'green';
            messageEl.textContent = '로그인 성공! Filter.today 대시보드로 이동합니다.';

            setTimeout(() => { window.location.href = 'index.html'; }, 1000);
// js/auth/login.js (Session Cookie Version)

            document.addEventListener('DOMContentLoaded', () => {
                const loginForm = document.getElementById('login-form');
                if (loginForm) {
                    loginForm.addEventListener('submit', handleLogin);
                }
            });

            async function handleLogin(e) {
                e.preventDefault();

                const email = document.getElementById('email').value;
                const password = document.getElementById('password').value;
                const messageEl = document.getElementById('message');
                messageEl.textContent = '';
                messageEl.style.color = 'red';

                try {
                    const response = await fetch('/api/member/login', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ email, password }),
                        // ❗ 세션 쿠키를 주고받기 위해 credentials: 'include' 옵션 필수 ❗
                        credentials: 'include'
                    });

                    if (response.ok) {
                        // 서버가 쿠키(JSESSIONID)를 자동으로 설정하므로 클라이언트에서 별도로 토큰을 저장할 필요가 없습니다.
                        localStorage.setItem('isLoggedIn', 'true'); // 상태 플래그만 유지

                        messageEl.style.color = 'green';
                        messageEl.textContent = '로그인 성공! Filter.today 대시보드로 이동합니다.';

                        setTimeout(() => { window.location.href = '/'; }, 1000);

                    } else {
                        const errorData = await response.json();
                        messageEl.textContent = errorData.message || '로그인 실패. 정보를 확인하세요.';
                    }
                } catch (error) {
                    console.error('Login error:', error);
                    messageEl.textContent = '서버 통신 오류.';
                }
            }
        } else {
            const errorData = await response.json();
            messageEl.textContent = errorData.message || '로그인 실패. 정보를 확인하세요.';
        }
    } catch (error) {
        console.error('Login error:', error);
        messageEl.textContent = '서버 통신 오류.';
    }
}