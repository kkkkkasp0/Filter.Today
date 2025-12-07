// js/chart.js

let emotionChart = null;

/**
 * C. 감성 통계 차트를 업데이트하거나 새로 생성합니다.
 * @param {Array<Object>} statsData - /api/analysis/stats에서 받은 데이터
 */
function updateChart(statsData) {
    const canvas = document.getElementById('emotion-pie-chart');
    if (!canvas) return; // 캔버스가 없으면 에러 방지용으로 중단

    const ctx = canvas.getContext('2d');

    // 1. 데이터가 없을 때의 처리 (빈 차트 방지)
    if (!statsData || statsData.length === 0) {
        if (emotionChart) {
            emotionChart.destroy();
            emotionChart = null;
        }
        return;
    }

    // 2. DTO 필드명 매핑 (백엔드랑 이름 맞추기!)
    // 백엔드 AnalysisService에서 .emotionType_label()로 넣었으므로 여기서도 label로 받아야 함
    const labels = statsData.map(item => item.emotionType_label || item.emotionType);
    const data = statsData.map(item => item.count);
    const backgroundColors = statsData.map(item => item.hexCode);

    const chartData = {
        labels: labels,
        datasets: [{
            data: data,
            backgroundColor: backgroundColors,
            borderWidth: 1, // 테두리 두께
            hoverOffset: 10 // 마우스 올렸을 때 튀어나오는 효과
        }]
    };

    if (emotionChart) {
        // 이미 차트가 있으면 데이터만 바꿔치기 (부드러운 애니메이션)
        emotionChart.data.labels = labels;
        emotionChart.data.datasets[0].data = data;
        emotionChart.data.datasets[0].backgroundColor = backgroundColors;
        emotionChart.update();
    } else {
        // 차트가 없으면 새로 생성
        emotionChart = new Chart(ctx, {
            type: 'doughnut', // 'pie'로 바꾸면 파이 차트가 됨
            data: chartData,
            options: {
                responsive: true,
                maintainAspectRatio: false, // HTML 크기에 맞춤

                // [레이아웃] 차트 주변 여백 설정
                layout: {
                    padding: {
                        right: 10 // 전체 차트에서 오른쪽 여백을 살짝 줌
                    }
                },

                plugins: {
                    // 1. 범례 (오른쪽 글씨들) 설정
                    legend: {
                        position: 'right', // 오른쪽에 배치
                        align: 'center',   // 세로 가운데 정렬
                        labels: {
                            boxWidth: 15,  // 색상 박스 크기 작게 (기본값 40은 너무 큼)
                            padding: 15,   // 항목 간 간격
                            font: {
                                size: 11   // 글씨 크기도 살짝 줄임
                            }
                        }
                    },
                    // 2. 제목 설정
                    title: {
                        display: true,
                        text: '이달의 감정 통계',
                        font: { size: 14 },
                        padding: {
                            top: 10,
                            bottom: 5 // ★ [핵심] 제목과 차트 사이 간격을 5px로 확 줄임!
                        }
                    }
                },
                cutout: '55%', // 도넛 두께 조절 (구멍 크기)
            }
        });
    }
}