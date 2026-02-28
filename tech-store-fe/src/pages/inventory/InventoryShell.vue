<template>
  <div class="layout">
    <aside class="sidebar">
      <h2>Inventory</h2>

      <router-link to="/inventory/orders"> Orders </router-link>
      <router-link to="/inventory/orders/paid" class="menu">
        Paid Orders
      </router-link>

      <router-link to="/inventory/orders/processing" class="menu">
        Processing Orders
      </router-link>
    </aside>

    <main class="main">
      <header class="header">Inventory Portal</header>

      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 220px;
  background: #1f2937;
  color: white;
  padding: 20px;
}
.main {
  flex: 1;
}
.header {
  height: 60px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  padding-left: 20px;
}
</style>
