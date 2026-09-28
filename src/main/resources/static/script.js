// Base URL for the Spring Boot REST API
const API_BASE_URL = '/api/contracts';

// Run on page load
document.addEventListener('DOMContentLoaded', () => {
    loadAllData();
});

// Load both all contracts and expiring contracts
function loadAllData() {
    loadContracts();
    loadExpiringContracts();
}

// Show alert banner message
function showAlert(message, type = 'success') {
    const alertBox = document.getElementById('alertBox');
    alertBox.className = `alert-box alert-${type}`;
    alertBox.textContent = message;
    alertBox.style.display = 'block';

    setTimeout(() => {
        alertBox.style.display = 'none';
    }, 4500);
}

// Helper to format status with badges
function formatStatusBadge(status) {
    const s = (status || 'ACTIVE').toUpperCase();
    let badgeClass = 'status-active';

    if (s === 'RENEWAL_DUE') {
        badgeClass = 'status-renewal_due';
    } else if (s === 'RENEWED') {
        badgeClass = 'status-renewed';
    } else if (s === 'TERMINATED') {
        badgeClass = 'status-terminated';
    }

    return `<span class="status-badge ${badgeClass}">${s.replace('_', ' ')}</span>`;
}

// 1. loadContracts() - GET /api/contracts
function loadContracts() {
    fetch(API_BASE_URL)
        .then(response => {
            if (!response.ok) throw new Error('Failed to load contracts');
            return response.json();
        })
        .then(contracts => {
            const tableBody = document.getElementById('contractsTableBody');
            const totalBadge = document.getElementById('totalContractsBadge');
            totalBadge.textContent = `${contracts.length} Total`;

            if (!contracts || contracts.length === 0) {
                tableBody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No contracts found. Use the form above to add one.</td></tr>`;
                return;
            }

            tableBody.innerHTML = contracts.map(contract => `
                <tr>
                    <td><strong>#${contract.id}</strong></td>
                    <td>${escapeHtml(contract.vendor)}</td>
                    <td>${escapeHtml(contract.contractName)}</td>
                    <td>${contract.startDate || '-'}</td>
                    <td>${contract.endDate || '-'}</td>
                    <td>${contract.noticePeriod} days</td>
                    <td>${formatStatusBadge(contract.status)}</td>
                    <td>
                        <div class="table-actions">
                            <button class="btn btn-secondary btn-sm" onclick="editContract(${contract.id})">Edit</button>
                            ${contract.status !== 'TERMINATED' ? `
                                <button class="btn btn-success btn-sm" onclick="renewContract(${contract.id}, '${escapeJs(contract.vendor)} - ${escapeJs(contract.contractName)}')">Renew</button>
                                <button class="btn btn-warning btn-sm" onclick="terminateContract(${contract.id})">Terminate</button>
                            ` : ''}
                            <button class="btn btn-danger btn-sm" onclick="deleteContract(${contract.id})">Delete</button>
                        </div>
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => {
            console.error('Error in loadContracts:', error);
            showAlert('Could not load contracts. Please check if the server and MySQL are running.', 'error');
        });
}

// 2. loadExpiringContracts() - GET /api/contracts/expiring
function loadExpiringContracts() {
    fetch(`${API_BASE_URL}/expiring`)
        .then(response => {
            if (!response.ok) throw new Error('Failed to load expiring contracts');
            return response.json();
        })
        .then(contracts => {
            const tableBody = document.getElementById('expiringTableBody');
            const countBadge = document.getElementById('expiringCountBadge');
            countBadge.textContent = `${contracts.length} Contracts`;

            if (!contracts || contracts.length === 0) {
                tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">No contracts expiring in the next 30 days.</td></tr>`;
                return;
            }

            tableBody.innerHTML = contracts.map(contract => `
                <tr>
                    <td><strong>#${contract.id}</strong></td>
                    <td>${escapeHtml(contract.vendor)}</td>
                    <td>${escapeHtml(contract.contractName)}</td>
                    <td><strong>${contract.endDate}</strong></td>
                    <td>${contract.noticePeriod} days</td>
                    <td>${formatStatusBadge(contract.status)}</td>
                    <td>
                        <div class="table-actions">
                            ${contract.status !== 'TERMINATED' ? `
                                <button class="btn btn-success btn-sm" onclick="renewContract(${contract.id}, '${escapeJs(contract.vendor)} - ${escapeJs(contract.contractName)}')">Renew</button>
                                <button class="btn btn-warning btn-sm" onclick="terminateContract(${contract.id})">Terminate</button>
                            ` : ''}
                        </div>
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => {
            console.error('Error in loadExpiringContracts:', error);
        });
}

// Form submit handler: routes to addContract() or updateContract()
function handleFormSubmit(event) {
    event.preventDefault();
    const id = document.getElementById('contractId').value;

    const contractData = {
        vendor: document.getElementById('vendor').value.trim(),
        contractName: document.getElementById('contractName').value.trim(),
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value,
        noticePeriod: parseInt(document.getElementById('noticePeriod').value, 10)
    };

    if (new Date(contractData.startDate) > new Date(contractData.endDate)) {
        showAlert('Start Date cannot be after End Date.', 'error');
        return;
    }

    if (id) {
        updateContract(id, contractData);
    } else {
        addContract(contractData);
    }
}

// 3. addContract() - POST /api/contracts
function addContract(contractData) {
    fetch(API_BASE_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(contractData)
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to create contract');
        return response.json();
    })
    .then(savedContract => {
        showAlert(`Contract #${savedContract.id} added successfully!`, 'success');
        clearForm();
        loadAllData();
    })
    .catch(error => {
        console.error('Error in addContract:', error);
        showAlert('Failed to add contract. Ensure the backend and database are reachable.', 'error');
    });
}

// 4. updateContract() - PUT /api/contracts/{id}
function updateContract(id, contractData) {
    fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(contractData)
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to update contract');
        return response.json();
    })
    .then(updated => {
        showAlert(`Contract #${id} updated successfully!`, 'success');
        clearForm();
        loadAllData();
    })
    .catch(error => {
        console.error('Error in updateContract:', error);
        showAlert(`Failed to update contract #${id}.`, 'error');
    });
}

// 5. deleteContract() - DELETE /api/contracts/{id}
function deleteContract(id) {
    if (!confirm(`Are you sure you want to delete contract #${id}? This action cannot be undone.`)) {
        return;
    }

    fetch(`${API_BASE_URL}/${id}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to delete contract');
        showAlert(`Contract #${id} deleted successfully.`, 'success');
        loadAllData();
    })
    .catch(error => {
        console.error('Error in deleteContract:', error);
        showAlert(`Failed to delete contract #${id}.`, 'error');
    });
}

// Edit button helper: loads contract into form
function editContract(id) {
    fetch(`${API_BASE_URL}/${id}`)
        .then(response => {
            if (!response.ok) throw new Error('Failed to fetch contract details');
            return response.json();
        })
        .then(contract => {
            document.getElementById('contractId').value = contract.id;
            document.getElementById('vendor').value = contract.vendor;
            document.getElementById('contractName').value = contract.contractName;
            document.getElementById('startDate').value = contract.startDate;
            document.getElementById('endDate').value = contract.endDate;
            document.getElementById('noticePeriod').value = contract.noticePeriod;

            document.getElementById('formTitle').textContent = `Edit Contract #${contract.id}`;
            document.getElementById('formModeTag').textContent = 'Edit Mode';
            document.getElementById('formModeTag').className = 'badge badge-warning';
            document.getElementById('submitBtn').textContent = 'Update Contract';
            document.getElementById('cancelEditBtn').style.display = 'inline-flex';

            // Scroll to the form
            window.scrollTo({ top: 0, behavior: 'smooth' });
        })
        .catch(error => {
            console.error('Error in editContract:', error);
            showAlert(`Could not load contract #${id} for editing.`, 'error');
        });
}

// 6. renewContract() - opens modal dialog
function renewContract(id, info = '') {
    document.getElementById('renewContractId').value = id;
    document.getElementById('modalContractInfo').textContent = `#${id} (${info})`;
    document.getElementById('newEndDate').value = '';
    document.getElementById('renewModal').style.display = 'flex';
}

function closeRenewModal() {
    document.getElementById('renewModal').style.display = 'none';
}

function submitRenewal() {
    const id = document.getElementById('renewContractId').value;
    const newEndDate = document.getElementById('newEndDate').value;

    if (!newEndDate) {
        alert('Please select a valid new end date.');
        return;
    }

    // PUT /api/contracts/{id}/renew?newEndDate=YYYY-MM-DD
    fetch(`${API_BASE_URL}/${id}/renew?newEndDate=${encodeURIComponent(newEndDate)}`, {
        method: 'PUT'
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to renew contract');
        return response.json();
    })
    .then(renewed => {
        closeRenewModal();
        showAlert(`Contract #${id} has been renewed with new end date: ${renewed.endDate}.`, 'success');
        loadAllData();
    })
    .catch(error => {
        console.error('Error in renewContract:', error);
        showAlert(`Failed to renew contract #${id}.`, 'error');
    });
}

// 7. terminateContract() - PUT /api/contracts/{id}/terminate
function terminateContract(id) {
    if (!confirm(`Are you sure you want to terminate contract #${id}? Once terminated, it cannot enter renewal due.`)) {
        return;
    }

    fetch(`${API_BASE_URL}/${id}/terminate`, {
        method: 'PUT'
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to terminate contract');
        return response.json();
    })
    .then(terminated => {
        showAlert(`Contract #${id} has been terminated.`, 'success');
        loadAllData();
    })
    .catch(error => {
        console.error('Error in terminateContract:', error);
        showAlert(`Failed to terminate contract #${id}.`, 'error');
    });
}

// 8. clearForm() - resets form back to create mode
function clearForm() {
    document.getElementById('contractForm').reset();
    document.getElementById('contractId').value = '';
    document.getElementById('formTitle').textContent = 'Add New Contract';
    document.getElementById('formModeTag').textContent = 'Create Mode';
    document.getElementById('formModeTag').className = 'badge badge-info';
    document.getElementById('submitBtn').textContent = 'Add Contract';
    document.getElementById('cancelEditBtn').style.display = 'none';
}

// Utility for escaping HTML
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/'/g, "&#039;");
}

function escapeJs(str) {
    if (!str) return '';
    return str.replace(/\\/g, '\\\\')
              .replace(/'/g, "\\'")
              .replace(/"/g, '\\"');
}
