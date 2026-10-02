// ==========================================================================
// SCALER STORE - High-End E-Commerce Application Engine
// Connects to Spring Cloud API Gateway (Port 8080)
// ==========================================================================

const API_BASE = 'http://localhost:8080';

// Global Store State
let state = {
  token: localStorage.getItem('scaler_jwt') || null,
  user: JSON.parse(localStorage.getItem('scaler_user') || 'null'),
  cart: { items: [], totalAmount: 0, totalQuantity: 0 },
  products: [],
  filteredProducts: [],
  categories: [],
  currentCategoryId: null,
  currentPriceFilter: 'all',
  inStockOnly: true,
  currentSort: 'featured',
  wishlist: JSON.parse(localStorage.getItem('scaler_wishlist') || '[]')
};

// Seed Catalog for instant zero-dependency hydration
const CATALOG_SEED = [
  {
    id: 1,
    sku: "SKU-MACBOOK-M3",
    name: "Apple MacBook Pro 16-inch M3 Max",
    description: "36GB Unified Memory, 1TB SSD Storage, Liquid Retina XDR display, Space Black finish.",
    price: 2499.00,
    originalPrice: 2799.00,
    stockQuantity: 45,
    imageUrl: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=80",
    categoryName: "Electronics",
    categoryId: 1,
    rating: 4.9,
    reviewCount: 84,
    badge: "Staff Pick"
  },
  {
    id: 2,
    sku: "SKU-SONY-WH1000XM5",
    name: "Sony WH-1000XM5 Wireless Noise-Cancelling Headphones",
    description: "Industry-leading active noise cancellation, 30-hour battery life, 2-device multipoint pairing.",
    price: 399.99,
    originalPrice: 449.99,
    stockQuantity: 120,
    imageUrl: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
    categoryName: "Electronics",
    categoryId: 1,
    rating: 4.8,
    reviewCount: 156,
    badge: "Popular"
  },
  {
    id: 3,
    sku: "SKU-LOGI-MXMASTER3S",
    name: "Logitech MX Master 3S Wireless Performance Mouse",
    description: "Quiet clicks, 8,000 DPI any-surface glass tracking, electromagnetic MagSpeed scroll wheel.",
    price: 99.99,
    originalPrice: 119.99,
    stockQuantity: 250,
    imageUrl: "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600&auto=format&fit=crop&q=80",
    categoryName: "Electronics",
    categoryId: 1,
    rating: 4.9,
    reviewCount: 312,
    badge: "Best Seller"
  },
  {
    id: 4,
    sku: "SKU-NIKE-PEGASUS40",
    name: "Nike Air Zoom Pegasus 40 Running Shoes",
    description: "Engineered mesh upper, dual Zoom Air units, durable waffle-inspired outsole for all-day comfort.",
    price: 130.00,
    originalPrice: 150.00,
    stockQuantity: 85,
    imageUrl: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80",
    categoryName: "Footwear & Apparel",
    categoryId: 2,
    rating: 4.7,
    reviewCount: 94,
    badge: "New Season"
  },
  {
    id: 5,
    sku: "SKU-DELL-U2723QE",
    name: "Dell UltraSharp 27-inch 4K USB-C Hub Monitor",
    description: "IPS Black technology, 2000:1 contrast ratio, 98% DCI-P3 color gamut, 90W power delivery.",
    price: 579.50,
    originalPrice: 659.00,
    stockQuantity: 60,
    imageUrl: "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600&auto=format&fit=crop&q=80",
    categoryName: "Electronics",
    categoryId: 1,
    rating: 4.8,
    reviewCount: 68,
    badge: "Top Rated"
  },
  {
    id: 6,
    sku: "SKU-BOOK-DDIA",
    name: "Designing Data-Intensive Applications",
    description: "The definitive reference for distributed data systems, storage architectures, and consensus algorithms.",
    price: 42.00,
    originalPrice: 49.99,
    stockQuantity: 300,
    imageUrl: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80",
    categoryName: "Books & Media",
    categoryId: 4,
    rating: 5.0,
    reviewCount: 420,
    badge: "Essential"
  }
];

// Document Initialization
document.addEventListener("DOMContentLoaded", () => {
  renderAuthStatus();
  fetchProducts();
  if (state.token) {
    fetchCart();
    fetchNotificationCount();
  }
});

// Toast Messenger
function showToast(msg) {
  const toast = document.getElementById("toast");
  const msgEl = document.getElementById("toastMsg");
  msgEl.innerText = msg;
  toast.classList.add("show");
  setTimeout(() => toast.classList.remove("show"), 3500);
}

// Modal Helpers
function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add("active");
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove("active");
}

function scrollToCatalog() {
  const el = document.getElementById("catalogSection");
  if (el) el.scrollIntoView({ behavior: 'smooth' });
}

// ==========================================================================
// Authentication Handling
// ==========================================================================
function renderAuthStatus() {
  const container = document.getElementById("authContainer");
  if (state.user && state.token) {
    const firstName = state.user.fullName ? state.user.fullName.split(' ')[0] : 'User';
    const initial = firstName.charAt(0).toUpperCase();
    container.innerHTML = `
      <div style="display: flex; align-items: center; gap: 0.6rem;">
        <div class="auth-user-tag">
          <div class="auth-user-avatar">${initial}</div>
          <span>${firstName}</span>
        </div>
        <button class="action-btn" onclick="handleLogout()" style="padding: 0.4rem 0.65rem; font-size: 0.8rem; color: var(--slate-500);">
          Logout
        </button>
      </div>
    `;
  } else {
    container.innerHTML = `
      <button class="action-btn" onclick="openAuthModal()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        <span>Sign In</span>
      </button>
    `;
  }
}

function openAuthModal() {
  switchAuthTab('login');
  openModal('authModal');
}

function switchAuthTab(tab) {
  const loginForm = document.getElementById("loginForm");
  const regForm = document.getElementById("registerForm");
  const loginTab = document.getElementById("loginTabBtn");
  const regTab = document.getElementById("registerTabBtn");
  const title = document.getElementById("authModalTitle");

  if (tab === 'login') {
    loginForm.style.display = 'block';
    regForm.style.display = 'none';
    loginTab.style.backgroundColor = 'var(--slate-950)';
    loginTab.style.color = '#fff';
    loginTab.style.border = 'none';
    regTab.style.backgroundColor = 'transparent';
    regTab.style.color = 'var(--slate-600)';
    regTab.style.border = '1px solid var(--border-light)';
    title.innerText = 'Sign In to Your Account';
  } else {
    loginForm.style.display = 'none';
    regForm.style.display = 'block';
    regTab.style.backgroundColor = 'var(--slate-950)';
    regTab.style.color = '#fff';
    regTab.style.border = 'none';
    loginTab.style.backgroundColor = 'transparent';
    loginTab.style.color = 'var(--slate-600)';
    loginTab.style.border = '1px solid var(--border-light)';
    title.innerText = 'Create a New Account';
  }
}

async function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById("loginEmail").value;
  const password = document.getElementById("loginPassword").value;

  try {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const data = await res.json();
    if (data.success && data.data) {
      state.token = data.data.token;
      state.user = {
        id: data.data.userId,
        email: data.data.email,
        fullName: data.data.fullName,
        role: data.data.role
      };
      localStorage.setItem('scaler_jwt', state.token);
      localStorage.setItem('scaler_user', JSON.stringify(state.user));
      closeModal('authModal');
      renderAuthStatus();
      showToast(`Welcome back, ${state.user.fullName}!`);
      fetchCart();
      fetchNotificationCount();
    } else {
      alert(data.message || 'Login failed');
    }
  } catch (err) {
    console.warn("API Gateway offline - activating local session:", err.message);
    state.token = "demo-session-token";
    state.user = { id: 1, email: email, fullName: "Aakash Panchal", role: "ROLE_USER" };
    localStorage.setItem('scaler_jwt', state.token);
    localStorage.setItem('scaler_user', JSON.stringify(state.user));
    closeModal('authModal');
    renderAuthStatus();
    showToast("Signed in as Aakash Panchal");
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const fullName = document.getElementById("regName").value;
  const email = document.getElementById("regEmail").value;
  const password = document.getElementById("regPassword").value;

  try {
    const res = await fetch(`${API_BASE}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, email, password, role: 'BUYER' })
    });
    const data = await res.json();
    if (data.success && data.data) {
      state.token = data.data.token;
      state.user = {
        id: data.data.userId,
        email: data.data.email,
        fullName: data.data.fullName,
        role: data.data.role
      };
      localStorage.setItem('scaler_jwt', state.token);
      localStorage.setItem('scaler_user', JSON.stringify(state.user));
      closeModal('authModal');
      renderAuthStatus();
      showToast("Account created successfully!");
    } else {
      alert(data.message || 'Registration failed');
    }
  } catch (err) {
    alert("Connection error: Ensure API Gateway is running on http://localhost:8080");
  }
}

function handleLogout() {
  state.token = null;
  state.user = null;
  state.cart = { items: [], totalAmount: 0, totalQuantity: 0 };
  localStorage.removeItem('scaler_jwt');
  localStorage.removeItem('scaler_user');
  renderAuthStatus();
  updateCartBadge();
  renderDrawerCart();
  showToast("Signed out successfully");
}

// ==========================================================================
// Catalog Fetching, Filtering, & Rendering
// ==========================================================================
async function fetchProducts(searchQuery = null, categoryId = null) {
  let url = `${API_BASE}/api/products?size=24`;

  if (searchQuery) {
    url += `&search=${encodeURIComponent(searchQuery)}`;
  } else if (categoryId) {
    url += `&categoryId=${categoryId}`;
  }

  try {
    const res = await fetch(url);
    const data = await res.json();
    if (data.success && data.data && data.data.content && data.data.content.length > 0) {
      state.products = data.data.content.map(p => enrichProductMetadata(p));
    } else {
      state.products = CATALOG_SEED;
    }
  } catch (err) {
    state.products = CATALOG_SEED;
  }

  applyFiltersAndRender();
}

function enrichProductMetadata(p) {
  const match = CATALOG_SEED.find(s => s.sku === p.sku || s.id === p.id);
  return {
    ...p,
    originalPrice: p.originalPrice || (match ? match.originalPrice : (Number(p.price) * 1.15).toFixed(2)),
    rating: p.rating || (match ? match.rating : 4.8),
    reviewCount: p.reviewCount || (match ? match.reviewCount : 54),
    badge: p.badge || (match ? match.badge : "In Stock"),
    imageUrl: p.imageUrl || (match ? match.imageUrl : "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600&auto=format&fit=crop&q=80")
  };
}

let searchDebounceTimeout = null;
function handleSearchInput(e) {
  clearTimeout(searchDebounceTimeout);
  const q = e.target.value.trim();
  searchDebounceTimeout = setTimeout(() => {
    fetchProducts(q.length > 0 ? q : null, state.currentCategoryId);
  }, 250);
}

function filterCategory(catId, btnEl = null) {
  state.currentCategoryId = catId;

  // Update Sub-nav pills
  const subNavItems = document.querySelectorAll('.sub-nav-item');
  subNavItems.forEach(el => el.classList.remove('active'));
  if (btnEl && btnEl.classList.contains('sub-nav-item')) {
    btnEl.classList.add('active');
  } else {
    const matchingSub = document.querySelector(`.sub-nav-item[onclick*="${catId}"]`) || subNavItems[0];
    if (matchingSub) matchingSub.classList.add('active');
  }

  // Update Sidebar buttons
  const sidebarBtns = document.querySelectorAll('#sidebarCategoryList .filter-btn');
  sidebarBtns.forEach(el => el.classList.remove('active'));
  const targetSidebarBtn = Array.from(sidebarBtns).find(btn => btn.getAttribute('onclick').includes(catId === null ? 'null' : catId));
  if (targetSidebarBtn) targetSidebarBtn.classList.add('active');

  const titles = {
    null: "All Products",
    1: "Electronics & Computing",
    2: "Footwear & Apparel",
    3: "Home & Kitchen",
    4: "Books & Media"
  };
  document.getElementById("catalogHeading").innerText = titles[catId] || "Curated Collection";

  fetchProducts(null, catId);
}

function applyPriceFilter(range) {
  state.currentPriceFilter = range;
  applyFiltersAndRender();
}

function toggleInStockFilter(el) {
  state.inStockOnly = el.checked;
  applyFiltersAndRender();
}

function handleSortChange(sortVal) {
  state.currentSort = sortVal;
  applyFiltersAndRender();
}

function applyFiltersAndRender() {
  let list = [...state.products];

  // Category filter
  if (state.currentCategoryId !== null) {
    list = list.filter(p => p.categoryId === state.currentCategoryId || (p.category && p.category.id === state.currentCategoryId));
  }

  // Price range filter
  if (state.currentPriceFilter === 'under-100') {
    list = list.filter(p => Number(p.price) < 100);
  } else if (state.currentPriceFilter === '100-500') {
    list = list.filter(p => Number(p.price) >= 100 && Number(p.price) <= 500);
  } else if (state.currentPriceFilter === 'over-500') {
    list = list.filter(p => Number(p.price) > 500);
  }

  // In-stock filter
  if (state.inStockOnly) {
    list = list.filter(p => (p.stockQuantity === undefined || p.stockQuantity > 0));
  }

  // Sorting
  if (state.currentSort === 'price-asc') {
    list.sort((a, b) => Number(a.price) - Number(b.price));
  } else if (state.currentSort === 'price-desc') {
    list.sort((a, b) => Number(b.price) - Number(a.price));
  } else if (state.currentSort === 'name-asc') {
    list.sort((a, b) => a.name.localeCompare(b.name));
  }

  state.filteredProducts = list;
  document.getElementById("catalogCountNote").innerText = `Showing ${list.length} curated items`;
  renderProductsGrid(list);
}

function renderProductsGrid(products) {
  const grid = document.getElementById("productsGrid");
  if (!products || products.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 4rem 1rem; color: var(--slate-400);">
        <p style="font-weight: 700; font-size: 1.1rem; color: var(--slate-700); margin-bottom: 0.5rem;">No products found</p>
        <p style="font-size: 0.875rem;">Try adjusting your search query or price filters.</p>
      </div>
    `;
    return;
  }

  grid.innerHTML = products.map(p => {
    const isFav = state.wishlist.includes(p.id);
    const catName = p.categoryName || (p.category ? p.category.name : "Curated");
    const origPrice = p.originalPrice ? `$${Number(p.originalPrice).toFixed(2)}` : '';
    const discount = p.originalPrice && Number(p.originalPrice) > Number(p.price)
      ? `${Math.round(((p.originalPrice - p.price) / p.originalPrice) * 100)}% OFF`
      : null;

    return `
      <article class="product-card">
        <div class="product-media-wrap">
          <img src="${p.imageUrl}" alt="${p.name}" class="product-img" loading="lazy">
          ${p.badge ? `<span class="product-badge-tag">${p.badge}</span>` : ''}
          <button class="btn-wishlist ${isFav ? 'active' : ''}" onclick="toggleWishlist(${p.id}, this)" title="Add to Wishlist">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
          </button>
        </div>

        <div class="product-info">
          <div class="product-category-meta">${catName}</div>
          <h3 class="product-title" title="${p.name}">${p.name}</h3>

          <div class="rating-row">
            <span class="stars-icon">★★★★★</span>
            <span class="rating-count">(${p.reviewCount || 48})</span>
          </div>

          <div class="product-pricing-row">
            <span class="current-price">$${Number(p.price).toFixed(2)}</span>
            ${origPrice ? `<span class="original-price">${origPrice}</span>` : ''}
            ${discount ? `<span class="discount-pill">${discount}</span>` : ''}
          </div>

          <button class="btn-add-cart" onclick="addToCart(${p.id}, '${escapeQuote(p.name)}', '${p.sku}', ${p.price}, '${p.imageUrl}')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
              <line x1="3" y1="6" x2="21" y2="6"></line>
            </svg>
            <span>Add to Bag</span>
          </button>
        </div>
      </article>
    `;
  }).join('');
}

function escapeQuote(str) {
  return str.replace(/'/g, "\\'");
}

function toggleWishlist(id, btn) {
  if (state.wishlist.includes(id)) {
    state.wishlist = state.wishlist.filter(x => x !== id);
    btn.classList.remove('active');
    showToast("Removed from wishlist");
  } else {
    state.wishlist.push(id);
    btn.classList.add('active');
    showToast("Added to wishlist");
  }
  localStorage.setItem('scaler_wishlist', JSON.stringify(state.wishlist));
}

// ==========================================================================
// Slide-Over Cart Drawer & Cart Logic
// ==========================================================================
function openCartDrawer() {
  const backdrop = document.getElementById("cartDrawerBackdrop");
  renderDrawerCart();
  backdrop.classList.add("active");
  document.body.style.overflow = "hidden";
}

function closeCartDrawer(e) {
  const backdrop = document.getElementById("cartDrawerBackdrop");
  backdrop.classList.remove("active");
  document.body.style.overflow = "";
}

async function fetchCart() {
  if (!state.token || !state.user) return;
  try {
    const res = await fetch(`${API_BASE}/api/cart`, {
      headers: {
        'Authorization': `Bearer ${state.token}`
      }
    });
    const data = await res.json();
    if (data.success && data.data) {
      state.cart = {
        items: data.data.items || [],
        totalAmount: data.data.totalAmount || 0,
        totalQuantity: (data.data.items || []).reduce((sum, i) => sum + i.quantity, 0)
      };
      updateCartBadge();
      renderDrawerCart();
    }
  } catch (err) {
    console.warn("Cart sync using client session:", err.message);
  }
}

async function addToCart(productId, productName, productSku, unitPrice, imageUrl = null) {
  const payload = {
    productId,
    productName,
    productSku,
    unitPrice,
    quantity: 1
  };

  if (!imageUrl) {
    const p = state.products.find(x => x.id === productId);
    imageUrl = p ? p.imageUrl : "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=80";
  }

  try {
    if (state.token && state.user) {
      const res = await fetch(`${API_BASE}/api/cart/items?userId=${state.user.id}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${state.token}`
        },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (data.success && data.data) {
        state.cart = {
          items: data.data.items.map(i => ({ ...i, imageUrl })),
          totalAmount: data.data.totalAmount,
          totalQuantity: data.data.items.reduce((sum, i) => sum + i.quantity, 0)
        };
      } else {
        updateLocalCart(productId, productName, productSku, unitPrice, imageUrl);
      }
    } else {
      updateLocalCart(productId, productName, productSku, unitPrice, imageUrl);
    }
  } catch (err) {
    updateLocalCart(productId, productName, productSku, unitPrice, imageUrl);
  }

  updateCartBadge();
  openCartDrawer();
  showToast(`Added "${productName}" to bag`);
}

function updateLocalCart(productId, productName, productSku, unitPrice, imageUrl) {
  let existing = state.cart.items.find(i => i.productId === productId);
  if (existing) {
    existing.quantity += 1;
    existing.subtotal = existing.quantity * unitPrice;
  } else {
    state.cart.items.push({
      productId,
      productName,
      productSku,
      unitPrice,
      quantity: 1,
      subtotal: unitPrice,
      imageUrl
    });
  }
  state.cart.totalQuantity = state.cart.items.reduce((sum, i) => sum + i.quantity, 0);
  state.cart.totalAmount = state.cart.items.reduce((sum, i) => sum + (i.subtotal || i.unitPrice * i.quantity), 0);
}

function updateCartBadge() {
  const badge = document.getElementById("cartBadge");
  const count = state.cart.totalQuantity || 0;
  badge.innerText = count;
  document.getElementById("cartDrawerCount").innerText = count;
}

function renderDrawerCart() {
  const container = document.getElementById("drawerCartItems");
  const footer = document.getElementById("drawerFooter");
  const freeShipThreshold = 99.00;
  const total = Number(state.cart.totalAmount) || 0;

  // Free shipping progress calculation
  const meterText = document.getElementById("shippingMeterText");
  const meterFill = document.getElementById("shippingMeterFill");
  if (total >= freeShipThreshold) {
    meterText.innerHTML = `<strong>Congratulations!</strong> You unlocked Free Express Shipping.`;
    meterFill.style.width = `100%`;
  } else {
    const diff = (freeShipThreshold - total).toFixed(2);
    const pct = Math.min(100, Math.round((total / freeShipThreshold) * 100));
    meterText.innerText = `Add $${diff} more to unlock Free Express Shipping`;
    meterFill.style.width = `${pct}%`;
  }

  if (!state.cart.items || state.cart.items.length === 0) {
    container.innerHTML = `
      <div class="drawer-empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
          <line x1="3" y1="6" x2="21" y2="6"></line>
        </svg>
        <p style="font-weight: 700; font-size: 1rem; color: var(--slate-800); margin-bottom: 0.25rem;">Your bag is empty</p>
        <p style="font-size: 0.85rem;">Discover our latest arrivals and essentials.</p>
      </div>
    `;
    footer.style.display = "none";
    return;
  }

  container.innerHTML = state.cart.items.map(i => {
    const p = state.products.find(x => x.id === i.productId);
    const img = i.imageUrl || (p ? p.imageUrl : "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=200&auto=format&fit=crop&q=80");
    const subtotal = Number(i.subtotal || i.unitPrice * i.quantity).toFixed(2);

    return `
      <div class="cart-item-row">
        <img src="${img}" alt="${i.productName}" class="cart-item-thumb">
        <div class="cart-item-content">
          <div class="cart-item-name">${i.productName}</div>
          <div class="cart-item-sku">${i.productSku || 'SKU-ITEM'}</div>
          <div class="cart-qty-control">
            <button class="qty-step-btn" onclick="updateItemQty(${i.productId}, ${i.quantity - 1})">−</button>
            <span class="qty-step-val">${i.quantity}</span>
            <button class="qty-step-btn" onclick="updateItemQty(${i.productId}, ${i.quantity + 1})">+</button>
          </div>
        </div>
        <div class="cart-item-total">$${subtotal}</div>
      </div>
    `;
  }).join('');

  footer.style.display = "block";
  document.getElementById("drawerSubtotal").innerText = `$${total.toFixed(2)}`;
}

async function updateItemQty(productId, newQty) {
  if (newQty <= 0) {
    state.cart.items = state.cart.items.filter(i => i.productId !== productId);
  } else {
    const item = state.cart.items.find(i => i.productId === productId);
    if (item) {
      item.quantity = newQty;
      item.subtotal = item.quantity * item.unitPrice;
    }
  }

  state.cart.totalQuantity = state.cart.items.reduce((sum, i) => sum + i.quantity, 0);
  state.cart.totalAmount = state.cart.items.reduce((sum, i) => sum + (i.subtotal || i.unitPrice * i.quantity), 0);

  updateCartBadge();
  renderDrawerCart();

  // Async sync with Cart Service
  if (state.token && state.user) {
    try {
      await fetch(`${API_BASE}/api/cart/items?userId=${state.user.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${state.token}`
        },
        body: JSON.stringify({ productId, quantity: newQty })
      });
    } catch (e) {
      // Quiet background failure handled locally
    }
  }
}

// ==========================================================================
// Checkout & Order Placement
// ==========================================================================
function openCheckoutModal() {
  if (!state.token) {
    closeCartDrawer();
    openAuthModal();
    showToast("Please sign in to proceed with checkout");
    return;
  }

  closeCartDrawer();
  document.getElementById("checkoutAmountText").innerText = Number(state.cart.totalAmount).toFixed(2);
  openModal("checkoutModal");
}

async function handleCheckout(e) {
  e.preventDefault();
  const btn = document.getElementById("paySubmitBtn");
  btn.disabled = true;
  btn.innerText = "Securing transaction...";

  const checkoutPayload = {
    paymentMethod: document.getElementById("payMethod").value,
    shippingAddress: {
      street: document.getElementById("shipStreet").value,
      city: document.getElementById("shipCity").value,
      state: document.getElementById("shipState").value,
      postalCode: document.getElementById("shipZip").value,
      country: "USA"
    },
    items: state.cart.items.map(i => ({
      productId: i.productId,
      productName: i.productName,
      productSku: i.productSku,
      quantity: i.quantity,
      unitPrice: i.unitPrice,
      subtotal: i.subtotal || (i.unitPrice * i.quantity)
    }))
  };

  try {
    const res = await fetch(`${API_BASE}/api/orders/checkout?userId=${state.user.id}&email=${encodeURIComponent(state.user.email)}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${state.token}`
      },
      body: JSON.stringify(checkoutPayload)
    });
    const data = await res.json();

    btn.disabled = false;
    btn.innerText = "Authorize & Place Order";

    if (data.success && data.data) {
      closeModal("checkoutModal");
      document.getElementById("confirmedOrderNumber").innerText = data.data.orderNumber;
      openModal("orderConfirmModal");

      // Reset cart
      state.cart = { items: [], totalAmount: 0, totalQuantity: 0 };
      updateCartBadge();
      renderDrawerCart();

      setTimeout(() => fetchNotificationCount(), 1500);
    } else {
      alert(data.message || "Order checkout could not be completed.");
    }
  } catch (err) {
    btn.disabled = false;
    btn.innerText = "Authorize & Place Order";
    // Simulated confirmation for local demo
    closeModal("checkoutModal");
    const fakeOrder = "ORD-" + new Date().toISOString().replace(/[-:T.Z]/g, '').slice(0, 14) + "-A92";
    document.getElementById("confirmedOrderNumber").innerText = fakeOrder;
    openModal("orderConfirmModal");
    state.cart = { items: [], totalAmount: 0, totalQuantity: 0 };
    updateCartBadge();
    renderDrawerCart();
  }
}

// ==========================================================================
// Order Tracking & Stepper
// ==========================================================================
function openOrderTrackModal() {
  openModal("orderTrackModal");
}

async function trackOrder(e) {
  e.preventDefault();
  const orderNum = document.getElementById("trackInput").value.trim();
  const resContainer = document.getElementById("trackResult");
  resContainer.style.display = "block";
  resContainer.innerHTML = `<p style="text-align: center; color: var(--slate-500);">Checking shipment status...</p>`;

  try {
    const res = await fetch(`${API_BASE}/api/orders/track/${encodeURIComponent(orderNum)}`);
    const data = await res.json();

    if (data.success && data.data) {
      const ord = data.data;
      resContainer.innerHTML = `
        <div style="background-color: var(--slate-50); border: 1px solid var(--border-light); border-radius: var(--radius-md); padding: 1.25rem;">
          <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 0.75rem;">
            <div>
              <div style="font-size: 0.75rem; text-transform: uppercase; color: var(--slate-400); font-weight: 700;">Shipment Status</div>
              <div style="font-size: 1.1rem; font-weight: 800; color: var(--slate-950);">${ord.status}</div>
            </div>
            <div style="font-size: 0.9rem; font-weight: 700; color: var(--slate-700);">$${Number(ord.totalAmount).toFixed(2)}</div>
          </div>

          <div class="tracking-stepper">
            <div class="step-item active">
              <div class="step-bullet">1</div>
              <div class="step-label">Placed</div>
            </div>
            <div class="step-item ${ord.status !== 'PLACED' ? 'active' : ''}">
              <div class="step-bullet">2</div>
              <div class="step-label">Confirmed</div>
            </div>
            <div class="step-item">
              <div class="step-bullet">3</div>
              <div class="step-label">Processing</div>
            </div>
            <div class="step-item">
              <div class="step-bullet">4</div>
              <div class="step-label">Delivered</div>
            </div>
          </div>

          <div style="font-size: 0.8rem; color: var(--slate-500); border-top: 1px solid var(--border-light); padding-top: 0.75rem;">
            Destination: ${ord.shippingAddress ? `${ord.shippingAddress.city}, ${ord.shippingAddress.country}` : 'Verified Address'}
          </div>
        </div>
      `;
    } else {
      resContainer.innerHTML = `<p style="color: var(--danger); text-align: center; font-size: 0.9rem;">Order number not found. Please verify and try again.</p>`;
    }
  } catch (err) {
    resContainer.innerHTML = `
      <div style="background-color: var(--slate-50); border: 1px solid var(--border-light); border-radius: var(--radius-md); padding: 1.25rem;">
        <div style="font-weight: 700; color: var(--slate-900); margin-bottom: 0.5rem;">Shipment Status: IN TRANSIT</div>
        <p style="font-size: 0.85rem; color: var(--slate-600); margin-bottom: 0.75rem;">Your package has been dispatched from our regional fulfillment facility.</p>
        <div class="tracking-stepper">
          <div class="step-item active"><div class="step-bullet">1</div><div class="step-label">Placed</div></div>
          <div class="step-item active"><div class="step-bullet">2</div><div class="step-label">Confirmed</div></div>
          <div class="step-item active"><div class="step-bullet">3</div><div class="step-label">In Transit</div></div>
          <div class="step-item"><div class="step-bullet">4</div><div class="step-label">Delivered</div></div>
        </div>
      </div>
    `;
  }
}

// ==========================================================================
// Notifications & Activity
// ==========================================================================
function openNotificationsModal() {
  fetchNotificationHistory();
  openModal("notifModal");
}

async function fetchNotificationCount() {
  if (!state.token) return;
  try {
    const res = await fetch(`${API_BASE}/api/notifications/history`, {
      headers: { 'Authorization': `Bearer ${state.token}` }
    });
    const data = await res.json();
    if (data.success && Array.isArray(data.data)) {
      document.getElementById("notifBadge").innerText = data.data.length;
    }
  } catch (e) {
    // Silent fallback
  }
}

async function fetchNotificationHistory() {
  const container = document.getElementById("notifListContainer");
  try {
    const res = await fetch(`${API_BASE}/api/notifications/history`, {
      headers: state.token ? { 'Authorization': `Bearer ${state.token}` } : {}
    });
    const data = await res.json();
    if (data.success && Array.isArray(data.data) && data.data.length > 0) {
      container.innerHTML = data.data.map(n => `
        <div style="padding: 0.85rem; border-bottom: 1px solid var(--border-light); display: flex; flex-direction: column; gap: 0.25rem;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-size: 0.8rem; font-weight: 700; color: var(--slate-900);">${n.type || 'NOTIFICATION'}</span>
            <span style="font-size: 0.7rem; color: var(--slate-400);">${n.status || 'Delivered'}</span>
          </div>
          <div style="font-size: 0.875rem; font-weight: 600; color: var(--slate-800);">${n.subject}</div>
          <div style="font-size: 0.8rem; color: var(--slate-500); white-space: pre-line;">${n.body}</div>
        </div>
      `).join('');
    } else {
      container.innerHTML = `<p style="text-align: center; color: var(--slate-400); padding: 2rem;">No new activity or notices found.</p>`;
    }
  } catch (e) {
    container.innerHTML = `
      <div style="padding: 1rem; border-radius: var(--radius-md); background-color: var(--slate-50); border: 1px solid var(--border-light);">
        <div style="font-weight: 700; font-size: 0.85rem; margin-bottom: 0.25rem;">Welcome Email Dispatched</div>
        <div style="font-size: 0.8rem; color: var(--slate-600);">Your account activation was verified. All order receipts are delivered directly to your inbox.</div>
      </div>
    `;
  }
}
