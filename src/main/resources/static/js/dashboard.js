// js/dashboard.js (세션 쿠키 기반 인증 버전)

// 현재 선택된 년월 상태
let currentYear = new Date().getFullYear();
let currentMonth = new Date().getMonth() + 1;

// -----------------------------------------------------------
// 1. 공통 헬퍼 함수 (세션 쿠키 사용)
// -----------------------------------------------------------

async function fetchData(url) {
    // 세션 쿠키를 사용하므로, 토큰 관련 코드는 모두 제거합니다.

    try {
        const response = await fetch(url, {
            // ❗ API 요청 시 쿠키(JSESSIONID) 자동 첨부를 위해 필수 ❗
            credentials: 'include'
        });

        if (!response.ok) {
            // 401 또는 403 에러 발생 시 세션 만료로 간주하여 로그인 페이지로 리디렉션
            if (response.status === 401 || response.status === 403) {
                alert("세션이 만료되었거나 접근 권한이 없습니다. Filter.today에 다시 로그인해주세요.");
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

// -----------------------------------------------------------
// 2. DOMContentLoaded 이벤트 핸들러 (메인 시작점)
// -----------------------------------------------------------

document.addEventListener('DOMContentLoaded', () => {

    // 1. 네비게이션 초기화 (연도/월 선택기)
    initYearAndMonthSelectors();

    // 2. 연도와 월 두 드롭다운 모두에 이벤트 리스너 연결
    if (document.getElementById('year-select') && document.getElementById('month-select')) {
        document.getElementById('year-select').addEventListener('change', handlePeriodChange);
        document.getElementById('month-select').addEventListener('change', handlePeriodChange);
    }

    // 3. 초기 데이터 로드 호출
    loadDashboardData(currentYear, currentMonth);

    // 4. 기록 폼 이벤트 연결 (diary.js에 정의된 함수 호출 가정)
    if (typeof attachDiaryFormEvents === 'function') {
        attachDiaryFormEvents();
    }
});

// -----------------------------------------------------------
// 3. 드롭다운 초기화 및 핸들러 함수
// -----------------------------------------------------------

/**
 * 연도와 월 선택 드롭다운 초기화 (현재 연도부터 과거 2년 포함)
 */
function initYearAndMonthSelectors() {
    const yearSelector = document.getElementById('year-select');
    const monthSelector = document.getElementById('month-select');
    const currentFullYear = new Date().getFullYear();

    if (!yearSelector || !monthSelector) return;

    // 연도 드롭다운 채우기 (현재 연도부터 과거 2년까지)
    for (let y = currentFullYear; y >= currentFullYear - 2; y--) {
        const option = document.createElement('option');
        option.value = y;
        option.textContent = `${y}년`;
        yearSelector.appendChild(option);
    }
    yearSelector.value = currentFullYear;


    // 월 드롭다운 채우기
    for (let m = 1; m <= 12; m++) {
        const option = document.createElement('option');
        const monthStr = m.toString().padStart(2, '0');
        option.value = monthStr;
        option.textContent = `${m}월`;
        monthSelector.appendChild(option);
    }
    monthSelector.value = currentMonth.toString().padStart(2, '0');
}

/**
 * 연도 또는 월 변경 이벤트 핸들러
 */
function handlePeriodChange() {
    const year = document.getElementById('year-select').value;
    const month = document.getElementById('month-select').value;

    currentYear = Number(year);
    currentMonth = Number(month);

    loadDashboardData(currentYear, currentMonth);
}

// -----------------------------------------------------------
// 4. 데이터 로드 및 렌더링 함수
// -----------------------------------------------------------

/**
 * 대시보드 데이터 로드 (API 호출 및 차트/톤맵 렌더링을 담당)
 */
async function loadDashboardData(year, month) {
    const monthStr = month.toString().padStart(2, '0');

    // 1. 톤 맵 데이터 (/api/analysis/tonemap)
    const heatmapData = await fetchData(`/api/analysis/tonemap?year=${year}&month=${monthStr}`);
    renderHueMap(year, month, heatmapData);

    // 2. 감성 통계 데이터 (/api/analysis/stats)
    const statsData = await fetchData(`/api/analysis/stats?year=${year}&month=${monthStr}`);
    if (typeof updateChart === 'function') {
        updateChart(statsData);
    }
}

/**
 * B. 감성 톤 맵 렌더링
 */
function renderHueMap(year, month, heatmapData) {
    const grid = document.getElementById('calendar-grid');
    grid.innerHTML = '';

    // 요일 헤더를 다시 추가
    const days = ['월', '화', '수', '목', '금', '토', '일'];
    days.forEach(day => {
        const header = document.createElement('div');
        header.className = 'day-header';
        header.textContent = day;
        grid.appendChild(header);
    });

    // 달력 날짜 계산
    const firstDay = new Date(year, month - 1, 1).getDay();
    const numDays = new Date(year, month, 0).getDate();

    // 1일의 요일에 맞추어 빈 블록 추가 (Grid offset)
    const startOffset = (firstDay === 0) ? 6 : firstDay - 1;
    for (let i = 0; i < startOffset; i++) {
        grid.appendChild(document.createElement('div'));
    }

    // 날짜 블록 생성
    for (let day = 1; day <= numDays; day++) {
        const monthStr = month.toString().padStart(2, '0');
        const dayStr = day.toString().padStart(2, '0');
        const dateKey = `${year}-${monthStr}-${dayStr}`;
        const data = heatmapData[dateKey];

        const block = document.createElement('div');
        block.className = 'date-block';
        block.textContent = day;

        if (data) {
            // Diary 엔티티의 필드명 사용: hexCode, content
            block.style.backgroundColor = data.hexCode;
            block.setAttribute('data-date', dateKey);
            block.setAttribute('data-memo', data.content);
            block.title = `${dateKey}: ${data.content || '(기록 없음)'}`;

            if (typeof loadDiaryForDate === 'function') {
                block.addEventListener('click', () => loadDiaryForDate(dateKey));
            }
        }

        grid.appendChild(block);
    }
}