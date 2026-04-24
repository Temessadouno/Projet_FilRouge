// ── Thème CSS Variables ───────────────────────────────────
const themes = {
  dark: {

    '--text':  '#f0f0f0', '--muted': '#9ca3af', '--border':'#2e2e2e',
    '--accent':'#F5C518', '--input': '#242424', '--badge': '#2a2a2a',
  },
  light: {
    '--bg':    '#ffffff', '--nav':   '#f9f9f9', '--card':  '#ffffff',
    '--text':  '#111111', '--muted': '#6b7280', '--border':'#e5e7eb',
    '--accent':'#1a1a1a', '--input': '#f9f9f9', '--badge': '#f3f4f6',
  }
};

function applyTheme(name) {
  const root = document.documentElement;
  root.setAttribute('data-theme', name);
  const vars = themes[name] || themes.dark;
  Object.entries(vars).forEach(([k, v]) => root.style.setProperty(k, v));
  document.body.style.background = vars['--bg'];
  document.body.style.color      = vars['--text'];
}

function toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme') || 'dark';
  const next    = current === 'dark' ? 'light' : 'dark';
  applyTheme(next);
  localStorage.setItem('theme', next);
}

(function () {
  const saved = localStorage.getItem('theme') || 'dark';
  applyTheme(saved);
})();

// ── Flash messages auto-hide ─────────────────────────────
document.querySelectorAll('.flash-msg').forEach(el => {
  setTimeout(() => { el.style.opacity = '0'; el.style.transition = 'opacity 0.5s'; }, 3500);
  setTimeout(() => el.remove(), 4000);
});

// ── Quantité panier auto-submit ──────────────────────────
document.querySelectorAll('.qty-auto-submit').forEach(input => {
  input.addEventListener('change', () => input.closest('form').submit());
});