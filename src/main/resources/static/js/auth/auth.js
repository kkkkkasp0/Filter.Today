// js/auth/auth.js (Session Cookie Version)

document.addEventListener('DOMContentLoaded', () => {

    const loginForm = document.getElementById('login-form');

    // ★ 이 if 문이 없어서 에러가 난 것입니다. 추가해주세요!
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    document.getElementById('login-btn').addEventListener('click', () => {
        window.location.href = '/login';
    });

    document.getElementById('logout-btn').addEventListener('click', () => {
        handleLogout();
    });

    checkInitialAuthStatus();
});

function checkInitialAuthStatus() {
    // JWT 토큰 대신 isLoggenIn 플래그만 확인
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    updateAuthState(isLoggedIn);
}

function updateAuthState(isLoggedIn) {
    const loginBtn = document.getElementById('login-btn');
    const logoutBtn = document.getElementById('logout-btn');

    if (isLoggedIn) {
        loginBtn.style.display = 'none';
        logoutBtn.style.display = 'inline-block';
        localStorage.setItem('isLoggedIn', 'true');
    } else {
        loginBtn.style.display = 'inline-block';
        logoutBtn.style.display = 'none';
        localStorage.setItem('isLoggedIn', 'false');
    }
}

async function handleLogout() {
    try {
        // ❗ Spring Security의 기본 로그아웃 URL(/logout)로 POST 요청 ❗
        const response = await fetch('/logout', {
            method: 'POST',
            credentials: 'include' // 세션 쿠키 전송
        });

        // 폼 기반 로그아웃이므로 응답 확인 대신 클라이언트 상태 즉시 초기화
        localStorage.removeItem('isLoggedIn');

        alert("Filter.today에서 로그아웃 되었습니다.");
        updateAuthState(false);
        window.location.href = 'login.html';

    } catch (error) {
        console.error("Logout error:", error);
        // 오류가 발생해도 로컬 상태는 지우고 로그인 페이지로 이동
        localStorage.removeItem('isLoggedIn');
        window.location.href = 'login.html';
    }
}