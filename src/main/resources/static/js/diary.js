// js/diary.js (Session Cookie Version)

let selectedDate = null;

export function attachDiaryFormEvents() {
    document.getElementById('save-diary-btn').addEventListener('click', saveDiary);
    document.getElementById('delete-diary-btn').addEventListener('click', deleteDiary);
}

// loadDiaryForDate 함수를 dashboard.js에서 호출하기 위해 export
export async function loadDiaryForDate(dateKey) {
    selectedDate = dateKey;
    document.getElementById('diary-text-input').value = '데이터를 불러오는 중...';
    document.getElementById('delete-diary-btn').style.display = 'none';

    try {
        const response = await fetch(`/api/diary?recordDate=${dateKey}`, {
            // ❗ 쿠키 자동 첨부를 위해 credentials: 'include' 옵션 필수 ❗
            credentials: 'include'
        });
        const data = await response.json();

        if (response.ok && data && data.content) {
            document.getElementById('diary-text-input').value = data.content;
            document.getElementById('emotion-color-picker').value = data.hexCode;
            document.getElementById('selected-emotion-label').textContent = ` (${data.emotionType})`;
            document.getElementById('delete-diary-btn').style.display = 'block';
            document.getElementById('delete-diary-btn').setAttribute('data-id', data.id);
        } else {
            document.getElementById('diary-text-input').value = '';
            document.getElementById('emotion-color-picker').value = '#ff9900';
            document.getElementById('selected-emotion-label').textContent = ' (새 기록)';
            document.getElementById('delete-diary-btn').style.display = 'none';
            document.getElementById('delete-diary-btn').removeAttribute('data-id');
        }

    } catch (error) {
        console.error("일기 로드 실패:", error);
    }
}

function saveDiary() {
    const content = document.getElementById('diary-text-input').value;
    const hexCode = document.getElementById('emotion-color-picker').value;
    const recordId = document.getElementById('delete-diary-btn').getAttribute('data-id');

    if (!selectedDate) {
        alert("기록할 날짜를 선택해주세요.");
        return;
    }

    const payload = {
        recordDate: selectedDate,
        content: content,
        hexCode: hexCode,
    };

    const method = recordId ? 'PUT' : 'POST';
    const url = recordId ? `/api/diary/${recordId}` : `/api/diary`;

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            // Authorization 헤더 제거됨
        },
        body: JSON.stringify(payload),
        // ❗ 쿠키 자동 첨부를 위해 credentials: 'include' 옵션 필수 ❗
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            alert(`기록이 Filter.today에 성공적으로 ${method === 'PUT' ? '수정' : '저장'}되었습니다!`);
            window.location.reload();
        } else {
            alert('기록 저장/수정에 실패했습니다.');
        }
    });
}

function deleteDiary() {
    const recordId = document.getElementById('delete-diary-btn').getAttribute('data-id');

    if (!recordId || !confirm("정말 이 기록을 삭제하시겠습니까?")) {
        return;
    }

    fetch(`/api/diary/${recordId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
        // ❗ 쿠키 자동 첨부를 위해 credentials: 'include' 옵션 필수 ❗
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            alert('기록이 Filter.today에서 성공적으로 삭제되었습니다!');
            window.location.reload();
        } else {
            alert('기록 삭제에 실패했습니다.');
        }
    });
}