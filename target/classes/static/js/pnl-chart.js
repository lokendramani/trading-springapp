// Initialize the chart
let pnlChart;
const ctx = document.getElementById('pnlChart').getContext('2d');

function initChart() {
    pnlChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Total PnL',
                data: [],
                borderColor: 'rgb(75, 192, 192)',
                tension: 0.1,
                fill: false
            }, {
                label: 'CE Value',
                data: [],
                borderColor: 'rgb(255, 99, 132)',
                tension: 0.1,
                fill: false
            }, {
                label: 'PE Value',
                data: [],
                borderColor: 'rgb(54, 162, 235)',
                tension: 0.1,
                fill: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                intersect: false,
                mode: 'index'
            },
            scales: {
                x: {
                    type: 'time',
                    time: {
                        unit: 'minute',
                        displayFormats: {
                            minute: 'HH:mm'
                        }
                    },
                    title: {
                        display: true,
                        text: 'Time'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Value (₹)'
                    }
                }
            },
            plugins: {
                title: {
                    display: true,
                    text: 'Real-time PnL Monitoring'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.dataset.label}: ₹${context.parsed.y.toFixed(2)}`;
                        }
                    }
                }
            }
        }
    });
}

// Function to update chart with new data
async function updateChart() {
    try {
        console.log('Fetching PnL data...');
        const response = await fetch('/api/pnl-data/recent');
        const data = await response.json();
        console.log('Received PnL data:', data);
        
        if (data && data.length > 0) {
            const timestamps = data.map(record => new Date(record.timestamp));
            const pnlValues = data.map(record => record.pnlValue);
            const ceValues = data.map(record => record.ceValue);
            const peValues = data.map(record => record.peValue);

            pnlChart.data.labels = timestamps;
            pnlChart.data.datasets[0].data = pnlValues;
            pnlChart.data.datasets[1].data = ceValues;
            pnlChart.data.datasets[2].data = peValues;
            
            pnlChart.update();
            console.log('Chart updated with new data');
        } else {
            console.log('No PnL data available');
        }
    } catch (error) {
        console.error('Error updating chart:', error);
    }
}

// Initialize chart when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    console.log('Initializing chart...');
    initChart();
    
    // Update chart every minute
    setInterval(updateChart, 60000);
    console.log('Chart update interval set to 1 minute');
    
    // Initial update
    updateChart();
}); 