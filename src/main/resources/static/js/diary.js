// js/diary.js (Session Cookie Version)

let selectedDate = null; // 현재 선택된 날짜 저장 변수
let currentMode = 'manual'; // 'manual' or 'ai'

// -----------------------------------------------------------
// 1. 모드 전환 및 초기화
// -----------------------------------------------------------

function attachDiaryFormEvents() {
    const saveBtn = document.getElementById('save-diary-btn');
    const deleteBtn = document.getElementById('delete-diary-btn');

    // 모드 전환 버튼 이벤트 연결 (HTML onclick으로 되어있지만, 안전장치로 확인)
    // HTML: onclick="setEntryMode(...)" 사용 중이므로 JS에서는 생략 가능

    if(saveBtn) saveBtn.addEventListener('click', handleSaveButtonClick);
    if(deleteBtn) deleteBtn.addEventListener('click', deleteDiary);
}

// 모드 전환 함수 (HTML 버튼 클릭 시 실행)
function setEntryMode(mode) {
    currentMode = mode;

    // 버튼 스타일 변경
    document.getElementById('mode-manual-btn').classList.toggle('active', mode === 'manual');
    document.getElementById('mode-ai-btn').classList.toggle('active', mode === 'ai');

    // UI 변경
    const colorArea = document.getElementById('manual-input-area');
    const saveBtn = document.getElementById('save-diary-btn');

    if (mode === 'ai') {
        if(colorArea) colorArea.style.display = 'none'; // 색상 선택기 숨김
        saveBtn.textContent = '🤖 분석 및 저장'; // 버튼 텍스트 변경
    } else {
        if(colorArea) colorArea.style.display = 'block'; // 색상 선택기 보임
        saveBtn.textContent = '저장';
    }
}


// -----------------------------------------------------------
// 2. 저장 로직 (분기 처리)
// -----------------------------------------------------------

// 저장 버튼 클릭 시 실행되는 메인 함수
function handleSaveButtonClick() {
    // 공통 유효성 검사
    if (!selectedDate) { alert("날짜를 선택해주세요."); return; }

    const content = document.getElementById('diary-text-input').value;
    if (!content.trim()) { alert("일기 내용을 입력해주세요."); return; }

    // ★ AI 모드라면 -> 분석 API 먼저 호출
    if (currentMode === 'ai') {
        requestAiAnalysis(content);
    } else {
        // ★ 직접 모드라면 -> 색상값 가져와서 바로 저장
        const manualColor = document.getElementById('emotion-color-picker').value;
        submitDiarySave(content, manualColor);
    }
}

// -----------------------------------------------------------
// 3. AI 분석 관련
// -----------------------------------------------------------

async function requestAiAnalysis(content) {
    try {
        // 로딩 표시 (선택사항)
        document.getElementById('save-diary-btn').textContent = '분석 중...⏳';

        const response = await fetch('/api/diary/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: content }),
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();
            showAiModal(data);
        } else {
            alert("AI 분석에 실패했습니다.");
        }
    } catch (e) {
        console.error(e);
        alert("분석 중 오류 발생");
    } finally {
        // 버튼 텍스트 원상복구
        document.getElementById('save-diary-btn').textContent = '🤖 분석 및 저장';
    }
}

// 모달 관련 변수 및 함수
let pendingAiResult = null; // 저장 대기 중인 데이터

function showAiModal(data) {
    pendingAiResult = data; // 결과 임시 저장

    // 모달 내용 채우기
    const circle = document.getElementById('ai-color-circle');
    const text = document.getElementById('ai-emotion-text');

    if(circle) circle.style.backgroundColor = data.hexCode;
    if(text) text.innerText = `분석된 감정: ${data.emotionType}`;

    // 모달 보이기
    const modal = document.getElementById('ai-result-modal');
    if(modal) modal.style.display = 'flex';
}

function closeAiModal() {
    const modal = document.getElementById('ai-result-modal');
    if(modal) modal.style.display = 'none';
}

// 모달 이벤트 리스너 (DOM 로드 시 연결되지 않을 수 있어 확인 필요)
document.addEventListener('DOMContentLoaded', () => {
    const confirmBtn = document.getElementById('ai-confirm-btn');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function() {
            if (pendingAiResult) {
                const content = document.getElementById('diary-text-input').value;
                // AI가 준 색상으로 최종 저장 요청
                submitDiarySave(content, pendingAiResult.hexCode);
                closeAiModal();
            }
        });
    }
});


// -----------------------------------------------------------
// 4. 최종 DB 저장/수정 함수 (Fetch)
// -----------------------------------------------------------

function submitDiarySave(content, hexCode) {
    const deleteBtn = document.getElementById('delete-diary-btn');
    const recordId = deleteBtn.getAttribute('data-id');

    const payload = {
        recordDate: selectedDate,
        content: content,
        hexCode: hexCode,
    };

    const method = recordId ? 'PUT' : 'POST';
    const url = recordId ? `/api/diary/${recordId}` : `/api/diary`;

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        credentials: 'include'
    })
        .then(async response => {
            if (response.ok) {
                alert("성공적으로 저장되었습니다!");
                window.location.reload(); // 저장 후 새로고침하여 캘린더 반영
            } else {
                const errorText = await response.text();
                alert('저장 실패: ' + errorText);
            }
        });
}

// -----------------------------------------------------------
// 5. 삭제 함수
// -----------------------------------------------------------

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

// -----------------------------------------------------------
// 6. 데이터 로드 (캘린더 날짜 클릭 시 호출)
// -----------------------------------------------------------

async function loadDiaryForDate(dateKey) {
    selectedDate = dateKey; // 전역 변수 업데이트

    const textInput = document.getElementById('diary-text-input');
    const colorPicker = document.getElementById('emotion-color-picker');
    const deleteBtn = document.getElementById('delete-diary-btn');
    const label = document.getElementById('selected-emotion-label');

    // UI 초기화
    textInput.value = '데이터를 확인 중...';
    deleteBtn.style.display = 'none';

    try {
        const response = await fetch(`/api/diary?recordDate=${dateKey}`, {
            credentials: 'include'
        });

        let data = null;
        if (response.ok) {
            // 내용이 없을 경우(204)를 대비
            const text = await response.text();
            if (text) data = JSON.parse(text);
        }

        if (data && data.content) {
            // 데이터 있음: 수정 모드
            textInput.value = data.content;
            if(colorPicker) colorPicker.value = data.hexCode;
            if(label) label.textContent = ` (${data.emotionType || '감정'})`;

            deleteBtn.style.display = 'inline-block';
            deleteBtn.setAttribute('data-id', data.diaryId || data.diaryId);
        } else {
            // 데이터 없음: 새 글 작성 모드
            textInput.value = '';
            if(colorPicker) colorPicker.value = '#ff9900';
            if(label) label.textContent = ' (새 기록)';
            deleteBtn.style.display = 'none';
            deleteBtn.removeAttribute('data-id');
        }

    } catch (error) {
        console.error("일기 로드 실패:", error);
        textInput.value = '';
    }
}