/* ============================================
   OceanLink - Sidebar dinámico por rol
   Esto arma el menú lateral según el rol que le pasemos
   Uso: <div id="sidebar-mount" data-role="admin" data-active="inicio" data-ctx="/OceanLink"></div>
   (versión JSP: los links apuntan a los Servlets; "#" = módulo aún no implementado)
   ============================================ */

// guardamos aquí todos los iconos en SVG para no tener que estar
// llamando a Figma cada vez que necesitamos uno (son del set Lucide)
const ICONS = {
  home: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>',
  users: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
  "shield-check": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/></svg>',
  globe: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20"/><path d="M2 12h20"/></svg>',
  "git-branch": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="6" x2="6" y1="3" y2="15"/><circle cx="18" cy="6" r="3"/><circle cx="6" cy="18" r="3"/><path d="M18 9a9 9 0 0 1-9 9"/></svg>',
  settings: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>',
  user: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
  "log-out": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/></svg>',
  menu: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" x2="20" y1="6" y2="6"/><line x1="4" x2="20" y1="12" y2="12"/><line x1="4" x2="20" y1="18" y2="18"/></svg>'
};

// aquí definimos qué opciones de menú le tocan a cada rol
// esto lo saqué directo de los 5 sidebars que vi en Figma
const ROLE_MENUS = {
  admin: {
    roleLabel: "Network Admin",
    items: [
      { id: "inicio", icon: "home", label: "Inicio", href: "index.jsp" },
      { id: "usuarios", icon: "users", label: "Usuarios", href: "UsuariosServlet" },
      { id: "roles", icon: "shield-check", label: "Roles", href: "#" },
      { id: "stations", icon: "globe", label: "Stations", href: "EstacionesServlet" },
      { id: "segmentos", icon: "git-branch", label: "Segmentos", href: "SegmentosServlet" },
      { id: "config", icon: "settings", label: "Configuración", href: "#" },
      { id: "perfil", icon: "user", label: "Perfil", href: "#" }
    ]
  },
  "capacity-planner": {
    roleLabel: "Capacity Planner",
    items: [
      { id: "inicio", icon: "home", label: "Inicio", href: "#" },
      { id: "capacidad", icon: "users", label: "Capacidad", href: "#" },
      { id: "solicitudes", icon: "shield-check", label: "Solicitudes", href: "SolicitudesServlet" },
      { id: "clientes", icon: "globe", label: "Clientes", href: "ClientesServlet" },
      { id: "servicios", icon: "git-branch", label: "Servicios", href: "#" },
      { id: "rutas", icon: "git-branch", label: "Rutas", href: "#" },
      { id: "historial", icon: "git-branch", label: "Historial de utilización", href: "#" },
      { id: "config", icon: "settings", label: "Configuración", href: "#" },
      { id: "perfil", icon: "user", label: "Perfil", href: "#" }
    ]
  },
  supervisor: {
    // ya tiene pantallas de contenido diseñadas en Figma (Home + Reportes)
    roleLabel: "Supervisor",
    items: [
      { id: "inicio", icon: "home", label: "Inicio", href: "#" },
      { id: "reportes", icon: "users", label: "Reportes", href: "#" },
      { id: "config", icon: "settings", label: "Configuración", href: "#" },
      { id: "perfil", icon: "user", label: "Perfil", href: "#" }
    ]
  },
  "network-operator": {
    // ya tiene pantallas de contenido diseñadas en Figma (Home, Incidencias, Historial)
    // "estado-red" se quitó del menú: ese contenido ya vive dentro del Home, no es una pantalla aparte
    roleLabel: "Network Operator",
    items: [
      { id: "inicio", icon: "home", label: "Inicio", href: "#" },
      { id: "incidencias", icon: "shield-check", label: "Incidencias", href: "IncidenciasServlet" },
      { id: "historial-incidencias", icon: "globe", label: "Historial de incidencias", href: "#" },
      { id: "config", icon: "settings", label: "Configuración", href: "#" },
      { id: "perfil", icon: "user", label: "Perfil", href: "#" }
    ]
  },
  "maintenance-coordinator": {
    roleLabel: "Maintenance Coord.",
    items: [
      { id: "inicio", icon: "home", label: "Inicio", href: "index.jsp" },
      { id: "historial-mant", icon: "users", label: "Historial de mantenimientos", href: "MantenimientosServlet" },
      { id: "config", icon: "settings", label: "Configuración", href: "#" },
      { id: "perfil", icon: "user", label: "Perfil", href: "#" }
    ]
  }
};

// esta función arma el HTML del sidebar y lo mete dentro del div que le pasemos
function renderSidebar(mountEl) {
  // el rol viene del atributo data-role del div (ej: data-role="admin")
  const role = mountEl.dataset.role;
  // y el item activo (para pintarlo distinto) viene de data-active
  const active = mountEl.dataset.active || "";
  // context path de la app (ej: /OceanLink), lo manda el JSP en data-ctx
  const ctx = mountEl.dataset.ctx || "";
  const menu = ROLE_MENUS[role];

  // si mandan un rol que no existe, mejor avisar en consola y no romper nada
  if (!menu) {
    console.error("Rol de sidebar desconocido:", role);
    return;
  }

  // recorremos cada opción del menú y armamos su HTML
  // si el id coincide con el activo le agregamos la clase "active"
  const itemsHtml = menu.items.map(item => {
    const activeClass = item.id === active ? " active" : "";
    const href = item.href === "#" ? "#" : `${ctx}/${item.href}`;
    return `<a class="nav-item${activeClass}" href="${href}">
      <span class="icon">${ICONS[item.icon]}</span>
      <span>${item.label}</span>
    </a>`;
  }).join("");

  // ahora sí, armamos todo el bloque del sidebar (logo, menú, botón salir, perfil)
  mountEl.innerHTML = `
    <div class="sidebar" id="oceanlink-sidebar">
      <div class="top-content">
        <div class="brand-lockup">
          <img class="brand-logo" src="${ctx}/assets/img/logo-oceanlink.png" alt="Ocean Link">
          <div class="brand-text">
            <p class="brand-name">OCEAN LINK</p>
            <p class="brand-role">${menu.roleLabel}</p>
          </div>
        </div>
        <nav class="nav-list">
          ${itemsHtml}
        </nav>
      </div>
      <div class="sidebar-bottom">
        <div class="sidebar-divider"></div>
        <a class="nav-item logout" href="${ctx}/index.jsp">
          <span class="icon">${ICONS["log-out"]}</span>
          <span>Cerrar sesión</span>
        </a>
        <div class="user-profile-badge">
          <div class="user-avatar"></div>
          <div class="user-meta">
            <p class="user-name">Soporte Técnico</p>
            <p class="user-email">admin@oceanlink.net</p>
          </div>
        </div>
      </div>
    </div>
  `;
}

// cuando la página termina de cargar, buscamos todos los divs
// con id="sidebar-mount" y les metemos su sidebar correspondiente
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("#sidebar-mount").forEach(renderSidebar);

  // el único botón que oculta/muestra el sidebar vive en el header,
  // del lado derecho de cada pantalla (junto a la campana) — el mismo
  // lado hacia donde se abre el sidebar. Ya no hay un botón duplicado
  // dentro del propio sidebar.
  const headerToggle = document.getElementById("header-menu-toggle");
  const sidebarEl = document.getElementById("oceanlink-sidebar");
  if (headerToggle && sidebarEl) {
    headerToggle.addEventListener("click", () => {
      sidebarEl.classList.toggle("collapsed");
    });
  }
});
