// js/dashboard.js (세션 쿠키 기반 인증 버전)

let currentYear = new Date().getFullYear();
let currentMonth = new Date().getMonth() + 1;

// 1. 공통 헬퍼 함수
async function fetchData(url) {
    try {
        const response = await fetch(url, { credentials: 'include' });
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                window.location.href = 'login.html';
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error("Fetch Error:", error);
        return {};
    }
}

// 2. 초기화
document.addEventListener('DOMContentLoaded', () => {
    initYearAndMonthSelectors();

    if (document.getElementById('year-select') && document.getElementById('month-select')) {
        document.getElementById('year-select').addEventListener('change', handlePeriodChange);
        document.getElementById('month-select').addEventListener('change', handlePeriodChange);
    }

    loadDashboardData(currentYear, currentMonth);

    if (typeof attachDiaryFormEvents === 'function') {
        attachDiaryFormEvents();
    }
});

// 3. 드롭다운 초기화
function initYearAndMonthSelectors() {
    const yearSelector = document.getElementById('year-select');
    const monthSelector = document.getElementById('month-select');
    const currentFullYear = new Date().getFullYear();

    if (!yearSelector || !monthSelector) return;

    for (let y = currentFullYear; y >= currentFullYear - 2; y--) {
        const option = document.createElement('option');
        option.value = y;
        option.textContent = `${y}년`;
        yearSelector.appendChild(option);
    }
    yearSelector.value = currentFullYear;

    for (let m = 1; m <= 12; m++) {
        const option = document.createElement('option');
        const monthStr = m.toString().padStart(2, '0');
        option.value = monthStr;
        option.textContent = `${m}월`;
        monthSelector.appendChild(option);
    }
    monthSelector.value = currentMonth.toString().padStart(2, '0');
}

function handlePeriodChange() {
    const year = document.getElementById('year-select').value;
    const month = document.getElementById('month-select').value;
    currentYear = Number(year);
    currentMonth = Number(month);
    loadDashboardData(currentYear, currentMonth);
}

// 4. 데이터 로드
async function loadDashboardData(year, month) {
    const monthStr = month.toString().padStart(2, '0');
    const heatmapData = await fetchData(`/api/analysis/tonemap?year=${year}&month=${monthStr}`);
    renderHueMap(year, month, heatmapData);

    // (선택사항) 통계 데이터 로드 부분이 있다면 유지
    // const statsData = await fetchData(`/api/analysis/stats?year=${year}&month=${monthStr}`);
    // if (typeof updateChart === 'function') updateChart(statsData);
}

// ★★★ 5. 캘린더 렌더링 (가장 많이 수정된 부분) ★★★
function renderHueMap(year, month, heatmapData) {
    const grid = document.getElementById('calendar-grid');
    grid.innerHTML = '';

    // 요일 헤더
    const days = ['월', '화', '수', '목', '금', '토', '일'];
    days.forEach(day => {
        const header = document.createElement('div');
        header.className = 'day-header';
        header.textContent = day;
        grid.appendChild(header);
    });

    // 날짜 계산
    const firstDay = new Date(year, month - 1, 1).getDay();
    const numDays = new Date(year, month, 0).getDate();
    const startOffset = (firstDay === 0) ? 6 : firstDay - 1;

    // 빈 칸 채우기
    for (let i = 0; i < startOffset; i++) {
        grid.appendChild(document.createElement('div'));
    }

    // 날짜 박스 생성
    for (let day = 1; day <= numDays; day++) {
        const monthStr = month.toString().padStart(2, '0');
        const dayStr = day.toString().padStart(2, '0');
        const dateKey = `${year}-${monthStr}-${dayStr}`;
        const data = heatmapData[dateKey];

        const block = document.createElement('div');
        block.className = 'date-block';

        // ★ 수정: 날짜 숫자를 span으로 감싸서 독립적으로 관리
        const dateNum = document.createElement('span');
        dateNum.innerText = day;
        dateNum.style.fontWeight = 'bold';
        block.appendChild(dateNum);

        block.style.cursor = 'pointer';

        if (data) {
            block.style.backgroundColor = data.hexCode;
            block.title = data.content;
            block.classList.add('has-diary');

            // ★★★ 추가된 부분: 메모 내용이 있으면 달력에 표시 ★★★
            if (data.content) {
                const memoDiv = document.createElement('div');
                memoDiv.className = 'memo-preview'; // CSS 클래스 적용
                memoDiv.innerText = data.content;   // 일기 내용 넣기
                block.appendChild(memoDiv);
            }
        }

        // 클릭 이벤트
        block.addEventListener('click', function() {
            const prevSelected = document.querySelector('.date-block.selected');
            if (prevSelected) prevSelected.classList.remove('selected');

            block.classList.add('selected');

            const dateDisplay = document.getElementById('date-display-area');
            if (dateDisplay) {
                dateDisplay.innerText = `📅 선택된 날짜: ${dateKey}`;
            }
            if (typeof loadDiaryForDate === 'function') {
                loadDiaryForDate(dateKey);
            }
        });

        grid.appendChild(block);
    }
}