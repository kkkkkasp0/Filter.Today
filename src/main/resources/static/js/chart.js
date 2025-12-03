// js/chart.js

let emotionChart = null;

/**
 * C. 감성 통계 차트를 업데이트하거나 새로 생성합니다.
 * @param {Array<Object>} statsData - /api/analysis/stats에서 받은 데이터
 */
export function updateChart(statsData) {
    const ctx = document.getElementById('emotion-pie-chart').getContext('2d');

    // Diary 엔티티 필드명: hexCode, emotionType 사용 가정
    const labels = statsData.map(item => item.emotionType_kr || item.emotionType);
    const data = statsData.map(item => item.count);
    const backgroundColors = statsData.map(item => item.hexCode);

    const chartData = {
        labels: labels,
        datasets: [{
            data: data,
            backgroundColor: backgroundColors,
            hoverOffset: 4
        }]
    };

    if (emotionChart) {
        emotionChart.data = chartData;
        emotionChart.update();
    } else {
        emotionChart = new Chart(ctx, {
            type: 'doughnut',
            data: chartData,
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'right',
                    },
                    title: {
                        display: false,
                    }
                }
            }
        });
    }
}