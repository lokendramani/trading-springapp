// Show status message
function showStatus(type, message) {
    const statusMessage = document.getElementById('statusMessage');
    statusMessage.className = `alert alert-${type}`;
    statusMessage.textContent = message;
    statusMessage.style.display = 'block';
}

// Update strategy details
function updateStrategyDetails(data) {
    document.getElementById('niftyPrice').textContent = data.niftyOpenPrice;
    document.getElementById('strikePrice').textContent = data.strikePrice;
    document.getElementById('ceSymbol').textContent = data.ceSymbol;
    document.getElementById('peSymbol').textContent = data.peSymbol;
    document.getElementById('status').textContent = data.status;
    document.getElementById('strategyDetails').style.display = 'block';
}

// Initialize monitoring button state
let isMonitoring = false;

// Execute strategy
async function executeStrategy() {
    const executeBtn = document.getElementById('executeBtn');
    try {
        executeBtn.disabled = true;
        const response = await fetch('/api/strategy/short-straddle', {
            method: 'POST'
        });
        const data = await response.json();
        
        if (response.ok) {
            showStatus('success', 'Strategy executed successfully!');
            updateStrategyDetails(data);
        } else {
            showStatus('danger', `Error: ${data.message}`);
        }
    } catch (error) {
        showStatus('danger', `Error: ${error.message}`);
    } finally {
        executeBtn.disabled = false;
    }
}

// Toggle monitoring
async function toggleMonitoring() {
    const monitorBtn = document.getElementById('monitorBtn');
    
    if (!isMonitoring) {
        try {
            const response = await fetch('/monitor/start', {
                method: 'GET'
            });
            if (response.ok) {
                isMonitoring = true;
                monitorBtn.textContent = 'Stop Monitoring';
                monitorBtn.classList.remove('btn-primary');
                monitorBtn.classList.add('btn-danger');
                showStatus('success', 'Monitoring started');
            }
        } catch (error) {
            showStatus('danger', `Error starting monitoring: ${error.message}`);
        }
    } else {
        try {
            const response = await fetch('/monitor/stop', {
                method: 'GET'
            });
            if (response.ok) {
                isMonitoring = false;
                monitorBtn.textContent = 'Start Monitoring';
                monitorBtn.classList.remove('btn-danger');
                monitorBtn.classList.add('btn-primary');
                showStatus('success', 'Monitoring stopped');
            }
        } catch (error) {
            showStatus('danger', `Error stopping monitoring: ${error.message}`);
        }
    }
}

// Add event listeners when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    const executeBtn = document.getElementById('executeBtn');
    const monitorBtn = document.getElementById('monitorBtn');
    
    executeBtn.addEventListener('click', executeStrategy);
    monitorBtn.addEventListener('click', toggleMonitoring);
}); 