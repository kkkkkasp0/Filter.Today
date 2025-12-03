// js/auth/find_password.js

document.addEventListener('DOMContentLoaded', () => {
    const findPasswordForm = document.getElementById('find-password-form');
    if (findPasswordForm) {
        findPasswordForm.addEventListener('submit', handleFindPassword);
    }
});

async function handleFindPassword(e) {
    e.preventDefault();

    const email = document.getElementById('email').value;
    const messageEl = document.getElementById('message');
    messageEl.textContent = '처리 중...';
    messageEl.style.color = '#007bff';

    try {
        // 백엔드 API 엔드포인트 가정: /api/member/find-password
        // 이메일을 Body에 담아 POST 요청
        const response = await fetch('/api/member/find-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email }),

            // ❗ 세션 쿠키를 사용하므로 credentials: 'include' 옵션 필수 ❗
            credentials: 'include'
        });

        if (response.ok) {
            // 성공 응답 (재설정 링크 전송 성공)
            messageEl.style.color = 'green';
            messageEl.textContent = '✔️ 비밀번호 재설정 링크가 이메일로 전송되었습니다. 메일함을 확인해 주세요.';

            // 폼 필드를 비워 사용자에게 완료되었음을 알림
            document.getElementById('email').value = '';

        } else {
            // 400 Bad Request, 404 Not Found 등 오류 처리
            const errorData = await response.json();
            messageEl.style.color = 'red';
            // 백엔드가 제공하는 오류 메시지를 표시
            messageEl.textContent = errorData.message || '가입되지 않은 이메일이거나 서버 처리 중 오류가 발생했습니다.';
        }
    } catch (error) {
        console.error('Password recovery error:', error);
        messageEl.style.color = 'red';
        messageEl.textContent = '서버와의 통신에 실패했습니다. 네트워크 연결을 확인하세요.';
    }
}