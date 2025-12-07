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

    updateUserNickname();

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

    //통계 데이터 로드 부분이 있다면 유지
    const statsData = await fetchData(`/api/analysis/stats?year=${year}&month=${monthStr}`);
    if (typeof updateChart === 'function') updateChart(statsData);

    document.getElementById('wordcloud-section').style.display = 'none';
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

    // ★ 오늘 날짜 구하기 (시간은 00:00:00으로 맞춰서 날짜만 비교)
    const today = new Date();
    today.setHours(0, 0, 0, 0);

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

        // 현재 달력 칸의 날짜 객체 생성
        const cellDate = new Date(year, month - 1, day);

        const block = document.createElement('div');
        block.className = 'date-block';

        const dateNum = document.createElement('span');
        dateNum.innerText = day;
        dateNum.style.fontWeight = 'bold';
        block.appendChild(dateNum);

        // 일기 데이터가 있으면 표시
        if (data) {
            block.style.backgroundColor = data.hexCode;
            block.title = data.content;
            block.classList.add('has-diary');

            if (data.content) {
                const memoDiv = document.createElement('div');
                memoDiv.className = 'memo-preview';
                memoDiv.innerText = data.content;
                block.appendChild(memoDiv);
            }
        }

        // ★★★ [핵심] 미래 날짜인지 확인 ★★★
        if (cellDate > today) {
            // 미래 날짜면: 클릭 불가 클래스 추가 & 이벤트 연결 안 함
            block.classList.add('future-day');
            block.title = "미래의 날짜는 선택할 수 없습니다.";
        } else {
            // 오늘 또는 과거라면: 클릭 이벤트 연결
            block.style.cursor = 'pointer';

            block.addEventListener('click', function() {
                const prevSelected = document.querySelector('.date-block.selected');
                if (prevSelected) prevSelected.classList.remove('selected');

                block.classList.add('selected');

                const dateDisplay = document.getElementById('date-display-area');
                if (dateDisplay) {
                    dateDisplay.innerText = `📅 선택된 날짜: ${dateKey}`;
                }

                // 날짜 클릭 시 loadDiaryForDate 호출 (diary.js)
                if (typeof loadDiaryForDate === 'function') {
                    loadDiaryForDate(dateKey);
                }
            });
        }

        grid.appendChild(block);
    }
}
async function updateUserNickname() {
    try {
        // 컨트롤러에 닉네임 요청
        const response = await fetch('/api/diary/nickname', { credentials: 'include' });
        if (response.ok) {
            const nickname = await response.text(); // 닉네임 텍스트 받기

            // 헤더의 이름 부분 변경
            const headerSpan = document.getElementById('current-user-nickname');
            if (headerSpan) {
                headerSpan.innerText = nickname; // "길동"으로 변경
            }
        }
    } catch (e) {
        console.error("닉네임 로드 실패", e);
    }
}
async function openWordCloud() {
    const section = document.getElementById('wordcloud-section');
    section.style.display = 'flex';

    // 1. 헤더에 있는 닉네임을 그대로 가져옴 (이미 위 함수에서 업데이트 됨)
    const headerSpan = document.getElementById('current-user-nickname');
    const nickname = headerSpan ? headerSpan.innerText : '사용자';

    // 2. 제목 업데이트
    const title = document.getElementById('wordcloud-title');
    if (title) {
        title.innerText = `☁️ 이번 달 ${nickname}님이 자주 하신 키워드 & 행동`;
    }

    section.scrollIntoView({ behavior: 'smooth' });

    const monthStr = currentMonth.toString().padStart(2, '0');
    const keywordData = await fetchData(`/api/diary/analysis/keywords?year=${currentYear}&month=${monthStr}`);

    renderWordCloud(keywordData);
}

function renderWordCloud(keywords) {
    const canvas = document.getElementById('word-cloud-canvas');
    const container = document.getElementById('cloud-container');
    const msg = document.getElementById('no-keyword-msg');

    if (!canvas || !container) return;

    // 캔버스 크기 맞춤
    canvas.width = container.offsetWidth;
    canvas.height = container.offsetHeight;

    if (!keywords || keywords.length === 0) {
        if(msg) msg.style.display = 'block';
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        return;
    }
    if(msg) msg.style.display = 'none';

    // 데이터 변환
    const list = keywords.map(k => [k.text, k.weight * 14]);

    // 라이브러리 실행
    WordCloud(canvas, {
        list: list,
        gridSize: 12,
        weightFactor: function (size) {
            return Math.pow(size, 0.9) * 1.8;
        },
        fontFamily: 'Segoe UI, sans-serif',
        color: function () {
            const colors = ['#FFD700', '#FFA500', '#32CD32', '#9370DB', '#FF4500', '#4682B4', '#555555'];
            return colors[Math.floor(Math.random() * colors.length)];
        },
        rotateRatio: 0,
        backgroundColor: '#fafafa',
        drawOutOfBound: false
    });
}

// 7. 이미지 다운로드 함수 (캘린더 함수 밖으로 꺼냄!)
function downloadCloudImage() {
    const canvas = document.getElementById('word-cloud-canvas');
    if (!canvas) return;

    const link = document.createElement('a');
    link.download = 'my-monthly-keywords.png';
    link.href = canvas.toDataURL("image/png");
    link.click();
}

