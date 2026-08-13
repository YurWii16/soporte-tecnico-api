/* ==========================================================================
   SOPORTE TI - CLIENT APPLICATION LOGIC (VANILLA JS)
   ========================================================================== */

// Configuración de endpoints (Microservicios)
const USERS_API_URL = "http://localhost:8083/api/v1/auth";
const TICKETS_API_URL = "http://localhost:8080/api/v1/solicitudes-soporte";

// Estado global de la aplicación
const AppState = {
    token: localStorage.getItem("soporte_ti_token") || null,
    user: null, // Contendrá { username, rol } extraídos del JWT
    tickets: [],
    currentPage: 0,
    totalPages: 1,
    pageSize: 10,
    editingTicketSolicitante: null,
    filters: {
        search: "",
        status: "ALL",
        priority: "ALL"
    }
};

// ==========================================================================
// 1. UTILIDADES Y AUXILIARES
// ==========================================================================

// Decodificar JWT localmente
function decodeToken(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error("Error al decodificar token JWT:", e);
        return null;
    }
}

// Formatear Fecha
function formatDate(dateString) {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Mostrar notificaciones Toast deslizantes
function showToast(title, message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'fa-circle-info';
    if (type === 'success') icon = 'fa-circle-check';
    if (type === 'error') icon = 'fa-circle-xmark';
    
    toast.innerHTML = `
        <i class="fa-solid ${icon}"></i>
        <div class="toast-content">
            <h4>${title}</h4>
            <p>${message}</p>
        </div>
    `;
    
    container.appendChild(toast);
    
    // Forzar reflujo del navegador
    toast.offsetHeight;
    toast.classList.add('show');
    
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ==========================================================================
// 2. NAVEGACIÓN Y CONTROL DE VISTAS (SPA)
// ==========================================================================

function updateUIForRole() {
    if (!AppState.user) return;
    
    const role = AppState.user.rol;
    
    // Ocultar / Mostrar elementos según rol
    document.querySelectorAll(".admin-only").forEach(el => {
        if (role === "ADMIN") el.classList.remove("hidden");
        else el.classList.add("hidden");
    });
    
    document.querySelectorAll(".client-only").forEach(el => {
        if (role === "CLIENTE") el.classList.remove("hidden");
        else el.classList.add("hidden");
    });

    document.querySelectorAll(".tecnico-only").forEach(el => {
        if (role === "TECNICO") el.classList.remove("hidden");
        else el.classList.add("hidden");
    });

    document.querySelectorAll(".client-admin-only").forEach(el => {
        if (role === "ADMIN" || role === "CLIENTE") el.classList.remove("hidden");
        else el.classList.add("hidden");
    });

    // Actualizar perfil del sidebar
    document.getElementById("profile-name").textContent = AppState.user.username;
    
    const roleBadge = document.getElementById("profile-role");
    roleBadge.textContent = role;
    roleBadge.className = "badge";
    roleBadge.classList.add(`badge-${role.toLowerCase()}`);
    
    // Avatar
    const avatar = document.getElementById("profile-avatar");
    if (role === "ADMIN") avatar.innerHTML = '<i class="fa-solid fa-user-shield"></i>';
    else if (role === "TECNICO") avatar.innerHTML = '<i class="fa-solid fa-user-gear"></i>';
    else avatar.innerHTML = '<i class="fa-solid fa-user-injured"></i>';

    // Saludo
    document.getElementById("welcome-message").textContent = `¡Hola, ${AppState.user.username}!`;
    document.getElementById("current-date").textContent = new Date().toLocaleDateString('es-ES', { 
        weekday: 'long', 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
}

function setActiveTab(tabName) {
    // Reset active class on nav items
    document.getElementById("nav-tickets").classList.remove("active");
    document.getElementById("nav-users").classList.remove("active");
    document.getElementById("nav-metrics").classList.remove("active");

    // Hide all main containers
    document.getElementById("metrics-container").classList.add("hidden");
    document.querySelector(".control-panel").classList.add("hidden");
    document.querySelector(".tickets-section").classList.add("hidden");
    document.getElementById("users-section").classList.add("hidden");
    document.getElementById("stats-section").classList.add("hidden");

    // Show selected container
    if (tabName === "tickets") {
        document.getElementById("nav-tickets").classList.add("active");
        document.getElementById("metrics-container").classList.remove("hidden");
        document.querySelector(".control-panel").classList.remove("hidden");
        document.querySelector(".tickets-section").classList.remove("hidden");
        fetchTickets();
    } else if (tabName === "users") {
        document.getElementById("nav-users").classList.add("active");
        document.getElementById("users-section").classList.remove("hidden");
        fetchUsers();
    } else if (tabName === "metrics") {
        document.getElementById("nav-metrics").classList.add("active");
        document.getElementById("stats-section").classList.remove("hidden");
        fetchStats();
    }
}

function showScreen(screen) {
    const loginContainer = document.getElementById("login-container");
    const appContainer = document.getElementById("app-container");
    
    if (screen === "login") {
        loginContainer.classList.remove("hidden");
        appContainer.classList.add("hidden");
    } else {
        loginContainer.classList.add("hidden");
        appContainer.classList.remove("hidden");
        updateUIForRole();
        setActiveTab("tickets");
    }
}

// ==========================================================================
// 3. LLAMADAS API Y LÓGICA DE NEGOCIO
// ==========================================================================

// Autenticación (Login)
async function handleLogin(username, password) {
    try {
        // Enviar como parámetros de URL (requisito del endpoint spring-boot)
        const params = new URLSearchParams();
        params.append("username", username);
        params.append("password", password);

        const response = await fetch(`${USERS_API_URL}/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params
        });

        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.error || "Credenciales inválidas");
        }

        // Guardar Token
        AppState.token = data.token;
        localStorage.setItem("soporte_ti_token", data.token);
        
        // Decodificar Token
        const decoded = decodeToken(data.token);
        AppState.user = {
            username: decoded.sub,
            rol: decoded.rol
        };

        showToast("Sesión iniciada", `Bienvenido ${AppState.user.username} (${AppState.user.rol})`, "success");
        showScreen("dashboard");
    } catch (e) {
        showToast("Error de Acceso", e.message, "error");
    }
}

// Cerrar Sesión
function logout() {
    AppState.token = null;
    AppState.user = null;
    AppState.tickets = [];
    localStorage.removeItem("soporte_ti_token");
    showToast("Sesión cerrada", "Has cerrado sesión correctamente", "info");
    showScreen("login");
}

// Obtener Solicitudes de Soporte
async function fetchTickets() {
    if (!AppState.token) return;
    
    const grid = document.getElementById("tickets-grid");
    const loader = document.getElementById("tickets-loader");
    const emptyState = document.getElementById("tickets-empty");
    const paginator = document.getElementById("pagination-container");
    
    grid.innerHTML = "";
    loader.classList.remove("hidden");
    emptyState.classList.add("hidden");
    paginator.classList.add("hidden");

    try {
        let url = TICKETS_API_URL;
        const role = AppState.user.rol;
        let isBackendFiltered = false;

        // Definición de URL según el rol
        if (role === "CLIENTE") {
            url = `${TICKETS_API_URL}/mis-solicitudes`;
        } else if (role === "TECNICO") {
            url = `${TICKETS_API_URL}/mis-asignaciones`;
        } else {
            // ADMIN usa listado paginado o filtros del servidor
            const activeOnly = document.getElementById("filter-active-only")?.checked;
            const statusFilter = document.getElementById("filter-status").value;
            
            if (activeOnly) {
                url = `${TICKETS_API_URL}/activas`;
                isBackendFiltered = true;
            } else if (statusFilter !== "ALL") {
                url = `${TICKETS_API_URL}/estado/${statusFilter}`;
                isBackendFiltered = true;
            } else {
                // ADMIN usa listado paginado por defecto
                url = `${TICKETS_API_URL}?page=${AppState.currentPage}&size=${AppState.pageSize}`;
            }
        }

        const response = await fetch(url, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${AppState.token}`,
                "Content-Type": "application/json"
            }
        });

        if (response.status === 403) {
            throw new Error("No tienes permisos suficientes para esta consulta.");
        }
        
        if (!response.ok) {
            throw new Error("Error al consultar las solicitudes.");
        }

        const result = await response.json();
        
        let ticketsList = [];
        
        if (role === "ADMIN" && !isBackendFiltered) {
            // El endpoint de ADMIN retorna un ResponseDTO con PageResponse adentro
            if (result.data && result.data.content) {
                ticketsList = result.data.content;
                AppState.totalPages = result.data.totalPages || 1;
                // Mostrar paginador
                if (AppState.totalPages > 1) {
                    paginator.classList.remove("hidden");
                    document.getElementById("page-indicator").textContent = `Página ${AppState.currentPage + 1} de ${AppState.totalPages}`;
                    document.getElementById("btn-page-prev").disabled = AppState.currentPage === 0;
                    document.getElementById("btn-page-next").disabled = AppState.currentPage === AppState.totalPages - 1;
                }
            }
        } else {
            // Clientes, Técnicos y consultas directas en backend retornan un List plano
            ticketsList = result;
        }

        AppState.tickets = ticketsList;
        renderTicketsList(applyClientFilters(ticketsList));

    } catch (e) {
        showToast("Error de Red", e.message, "error");
        emptyState.classList.remove("hidden");
    } finally {
        loader.classList.add("hidden");
    }
}

// Filtros Locales del Cliente (Búsqueda y Selectores)
function applyClientFilters(tickets) {
    if (!tickets || !Array.isArray(tickets)) return [];
    
    const { search, status, priority } = AppState.filters;
    
    return tickets.filter(ticket => {
        const title = (ticket.titulo || "").toString().toLowerCase();
        const location = (ticket.clinica || "").toString().toLowerCase();
        const ticketId = (ticket.id || "").toString();
        const term = (search || "").trim().toLowerCase();

        const matchesSearch = !term || 
            title.includes(term) ||
            location.includes(term) ||
            ticketId.includes(term);
            
        const matchesStatus = !status || status === "ALL" || ticket.estado === status;
        const matchesPriority = !priority || priority === "ALL" || ticket.prioridad === priority;
        
        return matchesSearch && matchesStatus && matchesPriority;
    });
}

// Renderizar tarjetas en la grilla
function renderTicketsList(tickets) {
    const grid = document.getElementById("tickets-grid");
    const emptyState = document.getElementById("tickets-empty");
    const countLabel = document.getElementById("tickets-count-label");
    
    // Clear first to prevent duplication
    grid.innerHTML = "";
    
    countLabel.textContent = `${tickets.length} incidencias encontradas`;

    if (tickets.length === 0) {
        emptyState.classList.remove("hidden");
        return;
    }

    emptyState.classList.add("hidden");
    
    tickets.forEach(ticket => {
        const card = document.createElement("div");
        card.className = "ticket-card";
        
        const prio = (ticket.prioridad || "MEDIA").toLowerCase();
        const est = (ticket.estado || "PENDIENTE").toLowerCase();
        
        // Indicador lateral de prioridad
        const indicator = document.createElement("div");
        indicator.className = `priority-indicator-bar border-${prio}`;
        card.appendChild(indicator);

        // Header tarjeta
        const header = `
            <div class="ticket-card-header">
                <span class="clinic-info">
                    <i class="fa-solid fa-building"></i> ${escapeHTML(ticket.clinica || 'Sede General')}
                </span>
                <span class="badge badge-${prio}">
                    <i class="fa-solid fa-circle-exclamation"></i> ${ticket.prioridad || 'MEDIA'}
                </span>
            </div>
        `;

        // Detalles metadatos
        const meta = `
            <div class="ticket-meta-details">
                <div class="meta-row">
                    <i class="fa-solid fa-clipboard-list"></i>
                    <span class="label">ID Incidencia:</span>
                    <strong>#${ticket.id}</strong>
                </div>
                <div class="meta-row">
                    <i class="fa-solid fa-user-pen"></i>
                    <span class="label">Solicitante:</span>
                    <span>${escapeHTML(ticket.solicitante || 'Usuario')} (${escapeHTML(ticket.creadoPorUsername || 'Web')})</span>
                </div>
                <div class="meta-row">
                    <i class="fa-solid fa-user-gear"></i>
                    <span class="label">Técnico Asignado:</span>
                    <strong class="${ticket.tecnicoAsignadoUsername ? 'text-blue' : 'text-muted'}">
                        ${ticket.tecnicoAsignadoUsername ? escapeHTML(ticket.tecnicoAsignadoUsername) : 'Sin asignar'}
                    </strong>
                </div>
            </div>
        `;

        // Body tarjeta
        const body = `
            <div class="ticket-card-body">
                <h3>${escapeHTML(ticket.titulo || 'Sin Asunto')}</h3>
                <p class="ticket-desc-text">${escapeHTML(ticket.descripcion || 'Sin descripción.')}</p>
                ${meta}
            </div>
        `;

        // Footer tarjeta
        const footer = `
            <div class="ticket-card-footer">
                <span class="ticket-date">Creado: ${formatDate(ticket.fechaCreacion)}</span>
                <span class="badge badge-${est}">${est.replace('_', ' ')}</span>
            </div>
        `;

        // Botones de acción contextual según Rol
        let actionsHtml = "";
        const role = AppState.user.rol;

        if (role === "ADMIN") {
            actionsHtml = `
                <div class="ticket-actions">
                    <button class="btn btn-outline btn-view-history" data-id="${ticket.id}" title="Ver historial de auditoría"><i class="fa-solid fa-clock-rotate-left"></i> Historial</button>
                    <button class="btn btn-outline btn-assign" data-id="${ticket.id}"><i class="fa-solid fa-user-plus"></i> Asignar</button>
                    <button class="btn btn-primary btn-edit" data-id="${ticket.id}"><i class="fa-solid fa-pen"></i></button>
                    
                    <select class="btn btn-outline select-status-patch" data-id="${ticket.id}" style="padding: 4px; font-size: 0.8rem;">
                        <option value="" disabled selected>Cambiar Estado</option>
                        <option value="PENDIENTE">Pendiente</option>
                        <option value="EN_PROCESO">En Proceso</option>
                        <option value="RESUELTO">Resuelto</option>
                        <option value="CERRADO">Cerrado</option>
                    </select>

                    <button class="btn btn-danger btn-delete" data-id="${ticket.id}"><i class="fa-solid fa-trash"></i></button>
                </div>
            `;
        } else if (role === "TECNICO") {
            actionsHtml = `
                <div class="ticket-actions">
                    <button class="btn btn-outline btn-view-history" data-id="${ticket.id}" title="Ver historial de auditoría"><i class="fa-solid fa-clock-rotate-left"></i> Historial</button>
                    <button class="btn btn-success btn-status-change" data-id="${ticket.id}" data-status="EN_PROCESO" ${ticket.estado === 'EN_PROCESO' ? 'disabled' : ''}>
                        <i class="fa-solid fa-spinner"></i> Iniciar
                    </button>
                    <button class="btn btn-primary btn-status-change" data-id="${ticket.id}" data-status="RESUELTO" ${ticket.estado === 'RESUELTO' ? 'disabled' : ''}>
                        <i class="fa-solid fa-circle-check"></i> Resolver
                    </button>
                </div>
            `;
        } else if (role === "CLIENTE") {
            actionsHtml = `
                <div class="ticket-actions">
                    <button class="btn btn-outline btn-view-history" data-id="${ticket.id}" title="Ver historial de auditoría" style="width: 100%;"><i class="fa-solid fa-clock-rotate-left"></i> Ver Historial y Auditoría del Ticket</button>
                </div>
            `;
        }

        card.innerHTML += header + body + footer + actionsHtml;
        grid.appendChild(card);
    });

    // Registrar Eventos a los botones inyectados
    addCardActionsEventListeners();
}

// Enlazar eventos para editar, borrar, asignar o cambiar de estado
function addCardActionsEventListeners() {
    // Ver Historial (Todos)
    document.querySelectorAll(".btn-view-history").forEach(btn => {
        btn.addEventListener("click", () => openHistoryModal(btn.dataset.id));
    });

    // Editar
    document.querySelectorAll(".btn-edit").forEach(btn => {
        btn.addEventListener("click", () => openEditTicketModal(btn.dataset.id));
    });

    // Eliminar (ADMIN)
    document.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", () => handleDeleteTicket(btn.dataset.id));
    });

    // Asignar Técnico (ADMIN)
    document.querySelectorAll(".btn-assign").forEach(btn => {
        btn.addEventListener("click", () => openAssignModal(btn.dataset.id));
    });

    // Cambiar estado select (ADMIN)
    document.querySelectorAll(".select-status-patch").forEach(select => {
        select.addEventListener("change", (e) => handlePatchStatus(select.dataset.id, e.target.value));
    });

    // Cambiar estado botón (TECNICO)
    document.querySelectorAll(".btn-status-change").forEach(btn => {
        btn.addEventListener("click", () => handlePatchStatus(btn.dataset.id, btn.dataset.status));
    });
}

// Crear / Editar Solicitud de Soporte (POST y PUT)
async function handleSaveTicket(event) {
    event.preventDefault();
    
    const id = document.getElementById("ticket-id").value;
    const clinica = document.getElementById("ticket-clinic").value;
    const solicitante = id ? (AppState.editingTicketSolicitante || AppState.user.username) : AppState.user.username;
    const titulo = document.getElementById("ticket-title").value;
    const descripcion = document.getElementById("ticket-desc").value;
    const prioridad = document.getElementById("ticket-priority").value;
    
    // Construir body
    const body = { clinica, solicitante, titulo, descripcion, prioridad };
    
    // Si somos Admin, también podemos mandar la asignación de técnico inicial
    if (AppState.user.rol === "ADMIN") {
        const tecnicoAsignadoUsername = document.getElementById("ticket-assignee").value;
        if (tecnicoAsignadoUsername) {
            body.tecnicoAsignadoUsername = tecnicoAsignadoUsername;
        }
    }

    try {
        let url = TICKETS_API_URL;
        let method = "POST";

        if (id) {
            url = `${TICKETS_API_URL}/${id}`;
            method = "PUT";
        }

        const response = await fetch(url, {
            method: method,
            headers: {
                "Authorization": `Bearer ${AppState.token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.error || "Ocurrió un error al guardar la solicitud.");
        }

        showToast("Éxito", id ? "Solicitud actualizada correctamente" : "Solicitud registrada con éxito", "success");
        closeModal("ticket-modal");
        fetchTickets();
        if (AppState.user.rol === "ADMIN") fetchStats();
    } catch (e) {
        showToast("Error de Operación", e.message, "error");
    }
}

// Eliminar Solicitud (Solo ADMIN)
async function handleDeleteTicket(id) {
    if (!confirm("¿Está seguro de que desea eliminar permanentemente esta solicitud de soporte? Esta acción no se puede deshacer.")) {
        return;
    }

    try {
        const response = await fetch(`${TICKETS_API_URL}/${id}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${AppState.token}`
            }
        });

        if (!response.ok) {
            throw new Error("No se pudo eliminar la solicitud.");
        }

        showToast("Eliminado", "La solicitud fue eliminada correctamente", "success");
        fetchTickets();
        fetchStats();
    } catch (e) {
        showToast("Error de Operación", e.message, "error");
    }
}

// Actualizar Estado de Solicitud (ADMIN y TECNICO)
async function handlePatchStatus(id, nuevoEstado) {
    try {
        const response = await fetch(`${TICKETS_API_URL}/${id}/estado?estado=${nuevoEstado}`, {
            method: "PATCH",
            headers: {
                "Authorization": `Bearer ${AppState.token}`
            }
        });

        if (!response.ok) {
            throw new Error("No tienes permisos o no se pudo actualizar el estado.");
        }

        showToast("Estado Actualizado", `La incidencia #${id} cambió a ${nuevoEstado}`, "success");
        fetchTickets();
        if (AppState.user.rol === "ADMIN") fetchStats();
    } catch (e) {
        showToast("Error de Operación", e.message, "error");
    }
}

// Asignar Técnico (ADMIN)
async function handleAssignTecnico(event) {
    event.preventDefault();
    
    const id = document.getElementById("assign-ticket-id").value;
    const tecnico = document.getElementById("assign-tecnico-username").value;

    try {
        const response = await fetch(`${TICKETS_API_URL}/${id}/asignar-tecnico?tecnico=${tecnico}`, {
            method: "PATCH",
            headers: {
                "Authorization": `Bearer ${AppState.token}`
            }
        });

        if (!response.ok) {
            throw new Error("Error al asignar técnico. Verifique el usuario.");
        }

        showToast("Técnico Asignado", `Incidencia #${id} asignada a ${tecnico}`, "success");
        closeModal("assign-modal");
        fetchTickets();
        fetchStats();
    } catch (e) {
        showToast("Error de Operación", e.message, "error");
    }
}

// Obtener estadísticas en tiempo real (ADMIN)
async function fetchStats() {
    try {
        const response = await fetch(`${TICKETS_API_URL}/estadisticas/por-estado`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${AppState.token}`
            }
        });

        if (!response.ok) return;

        const data = await response.json();
        
        // Rellenar métricas
        const total = Object.values(data).reduce((a, b) => a + b, 0);
        const pendiente = data["PENDIENTE"] || 0;
        const enProceso = data["EN_PROCESO"] || 0;
        const resuelto = data["RESUELTO"] || 0;
        const cerrado = data["CERRADO"] || 0;

        const metricsGrid = document.getElementById("metrics-container");
        const cards = metricsGrid.querySelectorAll(".metric-card");
        
        cards[0].querySelector("h3").textContent = total;
        cards[1].querySelector("h3").textContent = pendiente;
        cards[2].querySelector("h3").textContent = enProceso;
        cards[3].querySelector("h3").textContent = resuelto + cerrado;

        // Renderizar barra de estadísticas
        renderStatusChart(data);

        // Resumen del sistema
        const summary = document.getElementById("system-summary-list");
        summary.innerHTML = `
            <div class="summary-item">
                <span class="summary-item-label">Incidencias Críticas</span>
                <span class="summary-item-value badge badge-critica">${AppState.tickets.filter(t => t.prioridad === 'CRITICA').length}</span>
            </div>
            <div class="summary-item">
                <span class="summary-item-label">Incidencias Sin Asignar</span>
                <span class="summary-item-value text-yellow font-weight-bold">${AppState.tickets.filter(t => !t.tecnicoAsignadoUsername).length}</span>
            </div>
            <div class="summary-item">
                <span class="summary-item-label">Total Técnicos Operativos</span>
                <span class="summary-item-value text-blue">1 (tecnico)</span>
            </div>
        `;

    } catch (e) {
        console.error("Error al obtener estadísticas:", e);
    }
}

// Dibujar gráfico de barras en CSS
function renderStatusChart(data) {
    const chart = document.getElementById("status-bar-chart");
    chart.innerHTML = "";

    const estados = ["PENDIENTE", "EN_PROCESO", "RESUELTO", "CERRADO"];
    const maxVal = Math.max(...Object.values(data), 1);

    estados.forEach(estado => {
        const val = data[estado] || 0;
        const pct = (val / maxVal) * 100;
        
        const barWrapper = document.createElement("div");
        barWrapper.className = "chart-bar-wrapper";

        let color = "var(--color-primary)";
        if (estado === "PENDIENTE") color = "var(--color-warning)";
        if (estado === "EN_PROCESO") color = "var(--color-purple)";
        if (estado === "RESUELTO") color = "var(--color-success)";
        if (estado === "CERRADO") color = "var(--text-muted)";

        barWrapper.innerHTML = `
            <div class="chart-bar" style="height: ${pct}%; background: ${color};">
                <span class="bar-value">${val}</span>
            </div>
            <span class="bar-label">${estado.replace('_', ' ')}</span>
        `;
        chart.appendChild(barWrapper);
    });
}

// ==========================================================================
// 4. DIÁLOGOS Y VENTANAS MODAL
// ==========================================================================

function openModal(modalId) {
    document.getElementById(modalId).classList.remove("hidden");
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.add("hidden");
}

// Abrir modal de historial y auditoría de tickets
async function openHistoryModal(id) {
    try {
        const response = await fetch(`${TICKETS_API_URL}/${id}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${AppState.token}`
            }
        });

        if (!response.ok) {
            throw new Error("No se pudo obtener el detalle de la solicitud");
        }

        const ticket = await response.json();
        
        document.getElementById("history-ticket-id").textContent = `#${ticket.id}`;
        document.getElementById("history-ticket-title").textContent = ticket.titulo;
        document.getElementById("history-ticket-desc").textContent = ticket.descripcion || "Sin descripción.";
        document.getElementById("history-ticket-clinic").textContent = ticket.clinica;
        document.getElementById("history-ticket-solicitante").textContent = ticket.solicitante;
        document.getElementById("history-ticket-creator").textContent = ticket.creadoPorUsername || "Web";
        document.getElementById("history-ticket-assignee").textContent = ticket.tecnicoAsignadoUsername || "Sin asignar";
        
        const statusEl = document.getElementById("history-ticket-status");
        statusEl.textContent = ticket.estado.replace('_', ' ');
        statusEl.className = `summary-item-value badge badge-${ticket.estado.toLowerCase()}`;
        
        document.getElementById("history-ticket-created-date").textContent = formatDate(ticket.fechaCreacion);
        document.getElementById("history-ticket-updated-date").textContent = formatDate(ticket.fechaActualizacion);
        
        openModal("history-modal");
    } catch (e) {
        showToast("Error de Carga", e.message, "error");
    }
}

// Registrar usuario desde el dashboard de ADMIN (Interno)
async function handleInternalRegister(event) {
    event.preventDefault();
    
    const username = document.getElementById("internal-register-username").value.trim();
    const password = document.getElementById("internal-register-password").value;
    const role = document.getElementById("internal-register-role").value;

    try {
        const params = new URLSearchParams();
        params.append("username", username);
        params.append("password", password);
        params.append("rol", role);

        const response = await fetch(`${USERS_API_URL}/register`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Error al registrar el usuario");
        }

        showToast("Usuario Creado", `El usuario ${data.username} con rol ${data.rol} fue creado con éxito.`, "success");
        document.getElementById("internal-register-form").reset();
        fetchUsers(); // Actualizar la lista en pantalla inmediatamente sin recargar
    } catch (e) {
        showToast("Error de Registro", e.message, "error");
    }
}

// Obtener todos los usuarios del sistema (ADMIN)
async function fetchUsers() {
    try {
        const response = await fetch(`${USERS_API_URL}/users`, {
            method: "GET"
        });

        if (!response.ok) {
            throw new Error("No se pudo obtener la lista de usuarios");
        }

        const users = await response.json();
        renderUsersList(users);
    } catch (e) {
        showToast("Error al Cargar Usuarios", e.message, "error");
    }
}

// Renderizar la lista de usuarios en la tabla de ADMIN
function renderUsersList(users) {
    const tbody = document.getElementById("internal-users-list-body");
    if (!tbody) return;
    tbody.innerHTML = "";

    users.forEach(u => {
        const tr = document.createElement("tr");
        tr.style.borderBottom = "1px solid var(--border-color)";
        
        let badgeClass = "badge-cliente";
        if (u.rol === "ADMIN") badgeClass = "badge-admin";
        else if (u.rol === "TECNICO") badgeClass = "badge-tecnico";

        tr.innerHTML = `
            <td style="padding: 10px 8px; font-weight: 600;">${escapeHTML(u.username)}</td>
            <td style="padding: 10px 8px;"><span class="badge ${badgeClass}">${u.rol}</span></td>
            <td style="padding: 10px 8px; text-align: right;">
                <button class="btn btn-outline btn-edit-user" data-username="${escapeHTML(u.username)}" data-rol="${u.rol}" style="padding: 4px 8px; font-size: 0.8rem; margin-right: 4px;">
                    <i class="fa-solid fa-pen"></i> Editar
                </button>
                <button class="btn btn-danger btn-delete-user" data-username="${escapeHTML(u.username)}" ${u.username === 'admin' ? 'disabled' : ''} style="padding: 4px 8px; font-size: 0.8rem;">
                    <i class="fa-solid fa-trash"></i> Eliminar
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    // Enlazar eventos de botones
    document.querySelectorAll(".btn-edit-user").forEach(btn => {
        btn.addEventListener("click", () => {
            const username = btn.dataset.username;
            const rol = btn.dataset.rol;
            openEditUserModal(username, rol);
        });
    });

    document.querySelectorAll(".btn-delete-user").forEach(btn => {
        btn.addEventListener("click", () => {
            const username = btn.dataset.username;
            handleDeleteUser(username);
        });
    });
}

// Abrir el modal de edición de usuario
function openEditUserModal(username, rol) {
    document.getElementById("edit-user-username").value = username;
    document.getElementById("edit-user-password").value = "";
    document.getElementById("edit-user-role").value = rol;
    openModal("user-edit-modal");
}

// Guardar los cambios del usuario editado (ADMIN)
async function handleSaveUserEdit(event) {
    event.preventDefault();

    const username = document.getElementById("edit-user-username").value;
    const password = document.getElementById("edit-user-password").value;
    const role = document.getElementById("edit-user-role").value;

    try {
        const params = new URLSearchParams();
        if (password.trim() !== "") {
            params.append("password", password);
        }
        params.append("rol", role);

        const response = await fetch(`${USERS_API_URL}/users/${encodeURIComponent(username)}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "No se pudo actualizar el usuario");
        }

        showToast("Usuario Actualizado", `El usuario ${data.username} fue modificado con éxito.`, "success");
        closeModal("user-edit-modal");
        
        // Si el usuario editado es el actual, y se cambió su propio rol, refrescar sesión
        if (username === AppState.user.username && role !== AppState.user.rol) {
            showToast("Rol Modificado", "Tu rol ha cambiado, inicia sesión de nuevo para aplicar cambios", "info");
            logout();
        } else {
            fetchUsers(); // Actualizar la lista en pantalla inmediatamente sin recargar
        }
    } catch (e) {
        showToast("Error de Edición", e.message, "error");
    }
}

// Eliminar un usuario (ADMIN)
async function handleDeleteUser(username) {
    if (!confirm(`¿Está seguro de que desea eliminar al usuario "${username}"?`)) {
        return;
    }

    try {
        const response = await fetch(`${USERS_API_URL}/users/${encodeURIComponent(username)}`, {
            method: "DELETE"
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "No se pudo eliminar el usuario");
        }

        showToast("Usuario Eliminado", `El usuario "${username}" fue eliminado correctamente.`, "success");
        fetchUsers(); // Actualizar la lista en pantalla inmediatamente sin recargar
    } catch (e) {
        showToast("Error de Operación", e.message, "error");
    }
}

// Cargar ticket en modal para edición
async function openEditTicketModal(id) {
    try {
        const response = await fetch(`${TICKETS_API_URL}/${id}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${AppState.token}`
            }
        });

        if (!response.ok) {
            throw new Error("No se pudo obtener el detalle de la solicitud");
        }

        const ticket = await response.json();
        
        document.getElementById("ticket-id").value = ticket.id;
        document.getElementById("ticket-clinic").value = ticket.clinica;
        AppState.editingTicketSolicitante = ticket.solicitante;
        document.getElementById("ticket-title").value = ticket.titulo;
        document.getElementById("ticket-desc").value = ticket.descripcion;
        document.getElementById("ticket-priority").value = ticket.prioridad;
        
        if (AppState.user.rol === "ADMIN") {
            document.getElementById("ticket-assignee").value = ticket.tecnicoAsignadoUsername || "";
        }

        document.getElementById("modal-ticket-title").textContent = `Editar Incidencia #${ticket.id}`;
        openModal("ticket-modal");

    } catch (e) {
        showToast("Error de Carga", e.message, "error");
    }
}

function openAssignModal(id) {
    document.getElementById("assign-ticket-id").value = id;
    document.getElementById("assign-tecnico-username").value = "tecnico";
    openModal("assign-modal");
}

// Evitar inyección HTML en renderizado
function escapeHTML(str) {
    if (!str) return '';
    return str.replace(/[&<>'"]/g, 
        tag => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            "'": '&#39;',
            '"': '&quot;'
        }[tag] || tag)
    );
}

// ==========================================================================
// 5. REGISTRO DE EVENTOS (EVENT LISTENERS)
// ==========================================================================

document.addEventListener("DOMContentLoaded", () => {
    // A. Interceptar Login
    const loginForm = document.getElementById("login-form");
    loginForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const username = document.getElementById("login-username").value.trim();
        const password = document.getElementById("login-password").value;
        handleLogin(username, password);
    });

    // A2. Interceptar Registro Interno de ADMIN
    const internalRegisterForm = document.getElementById("internal-register-form");
    if (internalRegisterForm) {
        internalRegisterForm.addEventListener("submit", handleInternalRegister);
    }

    // B. Ver/Ocultar Contraseña
    const togglePass = document.getElementById("toggle-password");
    togglePass.addEventListener("click", () => {
        const passInput = document.getElementById("login-password");
        const type = passInput.getAttribute("type") === "password" ? "text" : "password";
        passInput.setAttribute("type", type);
        togglePass.querySelector("i").className = type === "password" ? "fa-regular fa-eye" : "fa-regular fa-eye-slash";
    });

    // B2. Manejo de Pestañas del Panel de Navegación Lateral (Sidebar)
    document.getElementById("nav-tickets").addEventListener("click", (e) => {
        e.preventDefault();
        setActiveTab("tickets");
    });
    
    document.getElementById("nav-users").addEventListener("click", (e) => {
        e.preventDefault();
        setActiveTab("users");
    });

    document.getElementById("nav-metrics").addEventListener("click", (e) => {
        e.preventDefault();
        setActiveTab("metrics");
    });

    // C. Accesos Rápidos
    document.querySelectorAll(".quick-user-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            document.getElementById("login-username").value = btn.dataset.user;
            document.getElementById("login-password").value = btn.dataset.pass;
            handleLogin(btn.dataset.user, btn.dataset.pass);
        });
    });

    // D. Logout
    document.getElementById("btn-logout").addEventListener("click", logout);

    // E. Crear Ticket (Abrir modal)
    document.getElementById("btn-new-ticket").addEventListener("click", () => {
        document.getElementById("ticket-form").reset();
        document.getElementById("ticket-id").value = "";
        document.getElementById("ticket-assignee").value = "";
        AppState.editingTicketSolicitante = null;
        document.getElementById("modal-ticket-title").textContent = "Registrar Solicitud";
        openModal("ticket-modal");
    });

    // F. Enviar Formulario de Ticket
    document.getElementById("ticket-form").addEventListener("submit", handleSaveTicket);

    // G. Enviar Formulario de Asignación
    document.getElementById("assign-form").addEventListener("submit", handleAssignTecnico);

    // G2. Enviar Formulario de Edición de Usuario (ADMIN)
    document.getElementById("user-edit-form").addEventListener("submit", handleSaveUserEdit);

    // H. Cerrar modales
    document.querySelectorAll(".btn-close-modal").forEach(btn => {
        btn.addEventListener("click", () => {
            closeModal("ticket-modal");
            closeModal("assign-modal");
            closeModal("history-modal");
            closeModal("user-edit-modal");
        });
    });

    // I. Búsqueda y filtros locales
    document.getElementById("search-input").addEventListener("input", (e) => {
        AppState.filters.search = e.target.value;
        renderTicketsList(applyClientFilters(AppState.tickets));
    });

    // Checkbox de Solo Activas (Servidor)
    document.getElementById("filter-active-only").addEventListener("change", (e) => {
        AppState.currentPage = 0;
        fetchTickets();
    });

    // Cambio en selector de estado (Servidor para ADMIN, cliente-side para otros)
    document.getElementById("filter-status").addEventListener("change", (e) => {
        AppState.filters.status = e.target.value;
        if (AppState.user.rol === "ADMIN") {
            AppState.currentPage = 0;
            fetchTickets();
        } else {
            renderTicketsList(applyClientFilters(AppState.tickets));
        }
    });

    document.getElementById("filter-priority").addEventListener("change", (e) => {
        AppState.filters.priority = e.target.value;
        renderTicketsList(applyClientFilters(AppState.tickets));
    });

    // J. Paginación (ADMIN)
    document.getElementById("btn-page-prev").addEventListener("click", () => {
        if (AppState.currentPage > 0) {
            AppState.currentPage--;
            fetchTickets();
        }
    });

    document.getElementById("btn-page-next").addEventListener("click", () => {
        if (AppState.currentPage < AppState.totalPages - 1) {
            AppState.currentPage++;
            fetchTickets();
        }
    });

    // K. Verificar si hay sesión activa previa al cargar
    if (AppState.token) {
        const decoded = decodeToken(AppState.token);
        if (decoded && decoded.exp * 1000 > Date.now()) {
            AppState.user = {
                username: decoded.sub,
                rol: decoded.rol
            };
            showScreen("dashboard");
        } else {
            logout();
        }
    } else {
        showScreen("login");
    }
});
