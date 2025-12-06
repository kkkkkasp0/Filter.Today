// js/diary.js (Session Cookie Version)

let selectedDate = null; // 현재 선택된 날짜 저장 변수

function attachDiaryFormEvents() {
    const saveBtn = document.getElementById('save-diary-btn');
    const deleteBtn = document.getElementById('delete-diary-btn');

    if(saveBtn) saveBtn.addEventListener('click', saveDiary);
    if(deleteBtn) deleteBtn.addEventListener('click', deleteDiary);
}

// Dashboard.js에서 날짜 클릭 시 호출되는 함수
async function loadDiaryForDate(dateKey) {
    selectedDate = dateKey; // ★ 핵심: 클릭한 날짜를 전역 변수에 저장

    const textInput = document.getElementById('diary-text-input');
    const colorPicker = document.getElementById('emotion-color-picker');
    const deleteBtn = document.getElementById('delete-diary-btn');
    const label = document.getElementById('selected-emotion-label');

    // UI 초기화 (로딩 중 표시)
    textInput.value = '데이터를 확인 중...';
    deleteBtn.style.display = 'none';

    try {
        const response = await fetch(`/api/diary?recordDate=${dateKey}`, {
            credentials: 'include'
        });

        // 204 No Content 등 데이터가 없는 경우를 대비해 텍스트 처리
        let data = null;
        if (response.ok) {
            const text = await response.text();
            if (text) {
                data = JSON.parse(text);
            }
        }

        // 데이터가 존재하면 폼에 채우기
        if (data && data.content) {
            textInput.value = data.content;
            colorPicker.value = data.hexCode;
            label.textContent = ` (${data.emotionType || '감정'})`;

            // 삭제 버튼 보이기 & ID 심어두기
            deleteBtn.style.display = 'inline-block';
            deleteBtn.setAttribute('data-id', data.diaryId || data.id); // DTO 필드명 확인 필요
        } else {
            // ★ 데이터가 없으면 '새 글 작성' 모드로 초기화
            textInput.value = '';
            colorPicker.value = '#ff9900'; // 기본 색상
            label.textContent = ' (새 기록)';
            deleteBtn.style.display = 'none';
            deleteBtn.removeAttribute('data-id');
        }

    } catch (error) {
        console.error("일기 로드 실패:", error);
        // 에러 나면 그냥 새 글 모드로
        textInput.value = '';
    }
}

function saveDiary() {
    const content = document.getElementById('diary-text-input').value;
    const hexCode = document.getElementById('emotion-color-picker').value;
    const deleteBtn = document.getElementById('delete-diary-btn');
    // 삭제 버튼에 ID가 있으면 수정 모드, 없으면 저장 모드
    const recordId = deleteBtn.getAttribute('data-id');

    // ★ 날짜 선택 여부 확인
    if (!selectedDate) {
        alert("달력에서 날짜를 먼저 선택해주세요.");
        return;
    }

    if (!content.trim()) {
        alert("일기 내용을 입력해주세요.");
        return;
    }

    const payload = {
        recordDate: selectedDate,
        content: content,
        hexCode: hexCode,
    };

    const method = recordId ? 'PUT' : 'POST';
    // ID가 있으면 경로 뒤에 붙이고, 없으면 기본 경로
    const url = recordId ? `/api/diary/${recordId}` : `/api/diary`;

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
        credentials: 'include'
    })
        .then(async response => {
            if (response.ok) {
                alert(`성공적으로 ${method === 'PUT' ? '수정' : '저장'}되었습니다!`);
                window.location.reload(); // 화면 새로고침하여 캘린더 반영
            } else {
                const errorText = await response.text();
                alert('저장 실패: ' + errorText);
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
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
    })
        .then(response => {
            if (response.ok) {
                alert('삭제되었습니다!');
                window.location.reload();
            } else {
                alert('삭제 실패했습니다.');
            }
        });
}