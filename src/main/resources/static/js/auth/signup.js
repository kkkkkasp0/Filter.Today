// js/auth/signup.js (Session Cookie Version)

document.addEventListener('DOMContentLoaded', () => {
    const signupForm = document.getElementById('signup-form');
    if (signupForm) {
        signupForm.addEventListener('submit', handleSignup);
    }
});

async function handleSignup(e) {
    e.preventDefault();

    const email = document.getElementById('email').value;
    const nickname = document.getElementById('nickname').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    const messageEl = document.getElementById('message');
    messageEl.textContent = '';
    messageEl.style.color = 'red';

    if (password !== confirmPassword) {
        messageEl.textContent = '비밀번호가 일치하지 않습니다.';
        return;
    }

    try {
        const response = await fetch('/api/member/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password, nickname }),
            credentials: 'include'
        });

        if (response.ok) {
            messageEl.style.color = 'green';
            messageEl.textContent = 'Filter.today 회원가입 성공! 로그인 페이지로 이동합니다.';

            setTimeout(() => { window.location.href = 'login.html'; }, 2000);

        } else {
            const errorData = await response.json();
            messageEl.textContent = errorData.message || '회원가입 실패. 이메일 중복 확인.';
        }
    } catch (error) {
        console.error('Signup error:', error);
        messageEl.textContent = '서버 통신 오류.';
    }
}