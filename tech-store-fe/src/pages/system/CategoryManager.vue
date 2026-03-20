<template>
  <div class="cm-page">

    <!-- ── Header ─────────────────────────────────────────────────── -->
    <div class="cm-header">
      <div class="cm-header__left">
        <div class="cm-header__eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
            <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
          </svg>
          Quản trị
        </div>
        <h1 class="cm-header__title">Quản lý danh mục</h1>
        <p class="cm-header__sub">Quản lý cấu trúc danh mục sản phẩm</p>
      </div>
      <div class="cm-header__actions">
        <button class="cm-btn cm-btn--outline" @click="load" :disabled="loading">
          <svg :class="{ 'cm-spin': loading }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M23 4v6h-6M1 20v-6h6"/>
            <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
          </svg>
          {{ loading ? 'Đang tải…' : 'Tải lại' }}
        </button>
        <button class="cm-btn cm-btn--primary" @click="openCreate">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Thêm danh mục
        </button>
      </div>
    </div>

    <!-- ── Table Card ──────────────────────────────────────────────── -->
    <div class="cm-card">

      <!-- Toolbar -->
      <div class="cm-toolbar">
        <div class="cm-toolbar__info">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 3H5a2 2 0 00-2 2v4m6-6h10a2 2 0 012 2v4M9 3v18m0 0h10a2 2 0 002-2V9M9 21H5a2 2 0 01-2-2V9m0 0h18"/>
          </svg>
          Cấu trúc cây danh mục
        </div>
        <span class="cm-count">
          <span class="cm-count__num">{{ treeData.length }}</span>
          <span class="cm-count__label">danh mục gốc</span>
        </span>
      </div>

      <!-- Table -->
      <div class="cm-table-wrap" :class="{ 'cm-table-wrap--loading': loading }">
        <div class="cm-loader-overlay" v-if="loading">
          <div class="cm-loader-ring"/>
        </div>

        <table class="cm-table">
          <thead>
            <tr>
              <th style="width:60px; text-align:center">ID</th>
              <th style="min-width:220px">Tên danh mục</th>
              <th style="width:100px; text-align:center">Ảnh</th>
              <th style="min-width:200px">Mô tả</th>
              <th style="width:160px; text-align:center">Danh mục cha</th>
              <th style="width:120px; text-align:center">Trạng thái</th>
              <th style="width:150px; text-align:center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <!-- Empty state -->
            <tr v-if="!loading && treeData.length === 0">
              <td colspan="7" class="cm-empty">
                <div class="cm-empty__inner">
                  <div class="cm-empty__icon">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4">
                      <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
                      <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
                    </svg>
                  </div>
                  <p>Chưa có danh mục nào</p>
                  <button class="cm-btn cm-btn--primary cm-btn--sm" @click="openCreate">Thêm danh mục đầu tiên</button>
                </div>
              </td>
            </tr>

            <!-- Root rows -->
            <template v-for="row in treeData" :key="row.id">
              <tr class="cm-row cm-row--root">
                <td style="text-align:center">
                  <span class="cm-id">#{{ row.id }}</span>
                </td>
                <td>
                  <div class="cm-name-cell">
                    <button
                      v-if="row.children?.length"
                      class="cm-expand-btn"
                      :class="{ 'cm-expand-btn--open': !collapsedRows.has(row.id) }"
                      @click.stop="toggleCollapse(row.id)"
                    >
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.8">
                        <polyline points="6 9 12 15 18 9"/>
                      </svg>
                    </button>
                    <span v-else class="cm-expand-placeholder"></span>
                    <span class="cm-name cm-name--root">{{ row.name }}</span>
                    <span v-if="row.children?.length" class="cm-child-count">{{ row.children.length }}</span>
                  </div>
                </td>
                <td style="text-align:center">
                  <div class="cm-img-wrap">
                    <img v-if="row.imageUrl" :src="fixImageUrl(row.imageUrl)" class="cm-img" :alt="row.name" />
                    <div v-else class="cm-img-empty">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/>
                        <polyline points="21 15 16 10 5 21"/>
                      </svg>
                    </div>
                  </div>
                </td>
                <td><span class="cm-desc">{{ row.description || '—' }}</span></td>
                <td style="text-align:center">
                  <span class="cm-root-badge">— Gốc —</span>
                </td>
                <td style="text-align:center">
                  <span class="cm-tag" :class="row.isActive ? 'cm-tag--green' : 'cm-tag--red'">
                    <span class="cm-tag__dot"></span>
                    {{ row.isActive ? 'Hoạt động' : 'Ẩn' }}
                  </span>
                </td>
                <td style="text-align:center">
                  <div class="cm-dropdown-wrap">
                    <button class="cm-dropdown-trigger" @click.stop="toggleDropdown(row.id)">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/>
                      </svg>
                    </button>
                    <Transition name="cm-dropdown-pop">
                      <div v-if="openDropdownId === row.id" class="cm-dropdown-menu">
                        <button class="cm-dropdown-item" @click="openEdit(row); closeDropdown()">
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                            <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                          </svg>
                          Chỉnh sửa
                        </button>
                        <div class="cm-dropdown-divider"></div>
                        <button class="cm-dropdown-item cm-dropdown-item--danger" @click="openDeleteConfirm(row); closeDropdown()">
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                            <path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
                          </svg>
                          Xóa
                        </button>
                      </div>
                    </Transition>
                  </div>
                </td>
              </tr>

              <!-- Level 1 children (ẩn khi root bị collapse) -->
              <template v-if="!collapsedRows.has(row.id)" v-for="child in (row.children || [])" :key="child.id">
                <tr class="cm-row cm-row--child">
                  <td style="text-align:center">
                    <span class="cm-id cm-id--child">#{{ child.id }}</span>
                  </td>
                  <td>
                    <div class="cm-name-cell cm-name-cell--l1">
                      <span class="cm-tree-connector">
                        <svg width="16" height="20" viewBox="0 0 16 20" fill="none">
                          <path d="M2 0 v12 q0 4 4 4 h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" fill="none" opacity="0.35"/>
                        </svg>
                      </span>
                      <button
                        v-if="child.children?.length"
                        class="cm-expand-btn cm-expand-btn--sm"
                        :class="{ 'cm-expand-btn--open': !collapsedRows.has(child.id) }"
                        @click.stop="toggleCollapse(child.id)"
                      >
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.8">
                          <polyline points="6 9 12 15 18 9"/>
                        </svg>
                      </button>
                      <span class="cm-name cm-name--child">{{ child.name }}</span>
                      <span v-if="child.children?.length" class="cm-child-count cm-child-count--sm">{{ child.children.length }}</span>
                    </div>
                  </td>
                  <td style="text-align:center">
                    <div class="cm-img-wrap">
                      <img v-if="child.imageUrl" :src="fixImageUrl(child.imageUrl)" class="cm-img" :alt="child.name" />
                      <div v-else class="cm-img-empty">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                          <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/>
                          <polyline points="21 15 16 10 5 21"/>
                        </svg>
                      </div>
                    </div>
                  </td>
                  <td><span class="cm-desc">{{ child.description || '—' }}</span></td>
                  <td style="text-align:center">
                    <span class="cm-parent-tag">{{ row.name }}</span>
                  </td>
                  <td style="text-align:center">
                    <span class="cm-tag" :class="child.isActive ? 'cm-tag--green' : 'cm-tag--red'">
                      <span class="cm-tag__dot"></span>
                      {{ child.isActive ? 'Hoạt động' : 'Ẩn' }}
                    </span>
                  </td>
                  <td style="text-align:center">
                    <div class="cm-dropdown-wrap">
                      <button class="cm-dropdown-trigger" @click.stop="toggleDropdown(child.id)">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                          <circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/>
                        </svg>
                      </button>
                      <Transition name="cm-dropdown-pop">
                        <div v-if="openDropdownId === child.id" class="cm-dropdown-menu">
                          <button class="cm-dropdown-item" @click="openEdit(child); closeDropdown()">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                              <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                              <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                            </svg>
                            Chỉnh sửa
                          </button>
                          <div class="cm-dropdown-divider"></div>
                          <button class="cm-dropdown-item cm-dropdown-item--danger" @click="openDeleteConfirm(child); closeDropdown()">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                              <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                              <path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
                            </svg>
                            Xóa
                          </button>
                        </div>
                      </Transition>
                    </div>
                  </td>
                </tr>

                <!-- Level 2 grandchildren (ẩn khi child bị collapse) -->
                <tr v-if="!collapsedRows.has(child.id)" v-for="grandchild in (child.children || [])" :key="grandchild.id" class="cm-row cm-row--grandchild">
                  <td style="text-align:center">
                    <span class="cm-id cm-id--child">#{{ grandchild.id }}</span>
                  </td>
                  <td>
                    <div class="cm-name-cell cm-name-cell--l2">
                      <span class="cm-tree-connector cm-tree-connector--l2">
                        <svg width="24" height="20" viewBox="0 0 24 20" fill="none">
                          <path d="M2 0 v12 q0 4 4 4 h14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" fill="none" opacity="0.25"/>
                        </svg>
                      </span>
                      <span class="cm-name cm-name--grandchild">{{ grandchild.name }}</span>
                    </div>
                  </td>
                  <td style="text-align:center">
                    <div class="cm-img-wrap">
                      <img v-if="grandchild.imageUrl" :src="fixImageUrl(grandchild.imageUrl)" class="cm-img" :alt="grandchild.name" />
                      <div v-else class="cm-img-empty">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                          <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/>
                          <polyline points="21 15 16 10 5 21"/>
                        </svg>
                      </div>
                    </div>
                  </td>
                  <td><span class="cm-desc">{{ grandchild.description || '—' }}</span></td>
                  <td style="text-align:center">
                    <span class="cm-parent-tag">{{ child.name }}</span>
                  </td>
                  <td style="text-align:center">
                    <span class="cm-tag" :class="grandchild.isActive ? 'cm-tag--green' : 'cm-tag--red'">
                      <span class="cm-tag__dot"></span>
                      {{ grandchild.isActive ? 'Hoạt động' : 'Ẩn' }}
                    </span>
                  </td>
                  <td style="text-align:center">
                    <div class="cm-dropdown-wrap">
                      <button class="cm-dropdown-trigger" @click.stop="toggleDropdown(grandchild.id)">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                          <circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/>
                        </svg>
                      </button>
                      <Transition name="cm-dropdown-pop">
                        <div v-if="openDropdownId === grandchild.id" class="cm-dropdown-menu">
                          <button class="cm-dropdown-item" @click="openEdit(grandchild); closeDropdown()">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                              <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                              <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                            </svg>
                            Chỉnh sửa
                          </button>
                          <div class="cm-dropdown-divider"></div>
                          <button class="cm-dropdown-item cm-dropdown-item--danger" @click="openDeleteConfirm(grandchild); closeDropdown()">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                              <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                              <path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
                            </svg>
                            Xóa
                          </button>
                        </div>
                      </Transition>
                    </div>
                  </td>
                </tr>
              </template>
            </template>
          </tbody>
        </table>
      </div>

      <!-- Footer -->
      <div class="cm-footer" v-if="!loading && treeData.length > 0">
        <span class="cm-footer__info">
          Tổng cộng
          <strong>{{ treeData.length }}</strong>
          danh mục gốc
        </span>
      </div>
    </div>

    <!-- ── Dialog ──────────────────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="cm-dialog-fade">
        <div v-if="dlg.open" class="cm-overlay" @click.self="dlg.open = false">
          <div class="cm-dialog">

            <!-- Dialog header band -->
            <div class="cm-dialog__band">
              <div class="cm-dialog__band-circles">
                <span class="cm-band-circle cm-band-circle--1"/>
                <span class="cm-band-circle cm-band-circle--2"/>
                <span class="cm-band-circle cm-band-circle--3"/>
              </div>
              <div class="cm-dialog__icon-wrap">
                <div class="cm-dialog__icon-ring">
                  <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
                    <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
                  </svg>
                </div>
              </div>
            </div>

            <!-- Dialog body -->
            <div class="cm-dialog__body">
              <div class="cm-dialog__badge">
                {{ dlg.mode === 'create' ? 'Tạo mới' : 'Cập nhật' }}
              </div>
              <h2 class="cm-dialog__title">
                {{ dlg.mode === 'create' ? 'Tạo danh mục mới' : 'Cập nhật danh mục' }}
              </h2>

              <!-- Alert -->
              <div class="cm-alert" v-if="dlg.alert">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                {{ dlg.alert }}
              </div>

              <!-- Form fields -->
              <div class="cm-form">
                <div class="cm-field">
                  <label class="cm-field__label">Tên danh mục <span class="cm-required">*</span></label>
                  <input
                    v-model="dlg.form.name"
                    class="cm-input"
                    :class="{ 'cm-input--error': dlg.alert && !dlg.form.name }"
                    placeholder="Nhập tên danh mục"
                  />
                </div>

                <div class="cm-field">
                  <label class="cm-field__label">Danh mục cha</label>
                  <select v-model="dlg.form.parentId" class="cm-select">
                    <option :value="null">— Không có (danh mục gốc) —</option>
                    <option
                      v-for="c in parentOptions"
                      :key="c.id"
                      :value="c.id"
                      :disabled="c.id === dlg.id"
                    >{{ c.name }}</option>
                  </select>
                </div>

                <div class="cm-field">
                  <label class="cm-field__label">Mô tả</label>
                  <textarea
                    v-model="dlg.form.description"
                    class="cm-textarea"
                    rows="3"
                    placeholder="Nhập mô tả danh mục (tùy chọn)"
                  />
                </div>

                <div class="cm-field">
                  <label class="cm-field__label">Ảnh danh mục</label>
                  <div class="cm-file-wrap">
                    <label class="cm-file-label">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                        <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
                        <polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
                      </svg>
                      {{ dlg.form.imageFile ? dlg.form.imageFile.name : 'Chọn ảnh…' }}
                      <input type="file" accept="image/*" style="display:none" @change="onPickFile" />
                    </label>
                  </div>
                  <p class="cm-field__hint" v-if="dlg.mode === 'edit' && !dlg.form.imageFile">
                    Để trống để giữ ảnh hiện tại.
                  </p>
                </div>
              </div>

              <!-- Dialog actions -->
              <div class="cm-dialog__actions">
                <button class="cm-btn cm-btn--ghost" @click="dlg.open = false">Hủy bỏ</button>
                <button
                  class="cm-btn cm-btn--primary"
                  :class="{ 'cm-btn--loading': dlg.loading }"
                  @click="save"
                >
                  <span class="cm-spinner" v-if="dlg.loading"/>
                  <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  {{ dlg.mode === 'create' ? 'Tạo danh mục' : 'Lưu thay đổi' }}
                </button>
              </div>
            </div>

          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ── Delete Confirm Dialog ─────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="cm-dialog-fade">
        <div v-if="deleteConfirm.open" class="cm-overlay" @click.self="deleteConfirm.open = false">
          <div class="cm-dialog">

            <!-- Red band -->
            <div class="cm-dialog__band cm-dialog__band--red">
              <div class="cm-dialog__band-circles">
                <span class="cm-band-circle cm-band-circle--1"/>
                <span class="cm-band-circle cm-band-circle--2"/>
                <span class="cm-band-circle cm-band-circle--3"/>
              </div>
              <div class="cm-dialog__icon-wrap">
                <div class="cm-dialog__icon-ring">
                  <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                    <path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
                  </svg>
                </div>
              </div>
            </div>

            <!-- Body -->
            <div class="cm-dialog__body">
              <div class="cm-dialog__badge cm-dialog__badge--red">Xác nhận xóa</div>
              <h2 class="cm-dialog__title">Xóa danh mục này?</h2>
              <p class="cm-dialog__desc">
                Danh mục
                <strong class="cm-dialog__highlight cm-dialog__highlight--red">{{ deleteConfirm.name }}</strong>
                sẽ bị ẩn khỏi hệ thống. Danh mục con (nếu có) vẫn được giữ nguyên.
              </p>

              <div class="cm-dialog__notice cm-dialog__notice--red">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                Hành động này không thể hoàn tác sau khi xác nhận.
              </div>

              <div class="cm-dialog__actions">
                <button class="cm-btn cm-btn--ghost" @click="deleteConfirm.open = false">Hủy bỏ</button>
                <button
                  class="cm-btn cm-btn--danger"
                  :class="{ 'cm-btn--loading': deleteConfirm.loading }"
                  @click="confirmRemove"
                >
                  <span class="cm-spinner" v-if="deleteConfirm.loading"/>
                  <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                    <path d="M10 11v6M14 11v6"/>
                  </svg>
                  Xác nhận xóa
                </button>
              </div>
            </div>

          </div>
        </div>
      </Transition>
    </Teleport>

  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, reactive, ref } from "vue";
import axios from "axios";
import { toast } from "../../ui/toast";

const BASE_URL = "http://localhost:8080/api/categories";

const loading = ref(false);
const treeData = ref([]); 
const parentOptions = ref([]); 

// --- Dropdown state ---
const openDropdownId = ref(null);

// --- Collapse/expand state ---
const collapsedRows = ref(new Set());

function toggleDropdown(id) {
  openDropdownId.value = openDropdownId.value === id ? null : id;
}
function closeDropdown() {
  openDropdownId.value = null;
}
function toggleCollapse(id) {
  const s = new Set(collapsedRows.value);
  if (s.has(id)) s.delete(id);
  else s.add(id);
  collapsedRows.value = s;
}
function onClickOutside() {
  closeDropdown();
}
onMounted(() => document.addEventListener('click', onClickOutside));
onBeforeUnmount(() => document.removeEventListener('click', onClickOutside));

// --- Delete confirm state ---
const deleteConfirm = reactive({
  open: false,
  loading: false,
  id: null,
  name: '',
});

function openDeleteConfirm(row) {
  deleteConfirm.id = row.id;
  deleteConfirm.name = row.name;
  deleteConfirm.loading = false;
  deleteConfirm.open = true;
}

async function confirmRemove() {
  deleteConfirm.loading = true;
  try {
    await axios.delete(`${BASE_URL}/${deleteConfirm.id}`);
    toast("Danh mục đã được ẩn thành công.", "success");
    deleteConfirm.open = false;
    load();
  } catch (e) {
    console.error(e);
    toast("Xóa thất bại.", "error");
  } finally {
    deleteConfirm.loading = false;
  }
}

// --- Helper: Fix link ảnh ---
function fixImageUrl(url) {
  if (!url) return "https://via.placeholder.com/150?text=No+Image";
  if (url.startsWith("http")) return url;
  return `http://localhost:8080${url}`;
}

// --- Tree Builder Logic ---
function buildCategoryTree(flatList) {
  const map = {};
  const roots = [];

  flatList.forEach((item) => {
    map[item.id] = { ...item, children: [] };
  });

  flatList.forEach((item) => {
    const node = map[item.id];
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(node);
    } else {
      roots.push(node);
    }
  });

  Object.values(map).forEach((node) => {
    if (node.children.length === 0) {
      delete node.children;
    }
  });

  return roots;
}

// --- API Calls ---
async function load() {
  loading.value = true;
  try {
    // Lấy toàn bộ danh sách (không phân trang) để dựng cây
    const res = await axios.get(BASE_URL, { params: { activeOnly: false } });
    const flatList = res.data?.data || [];
    
    parentOptions.value = flatList;
    treeData.value = buildCategoryTree(flatList);
  } catch (e) {
    console.error(e);
    toast("Failed to load categories.", "error");
  } finally {
    loading.value = false;
  }
}

// --- Create / Edit Logic ---
const dlg = reactive({
  open: false,
  mode: "create",
  loading: false,
  alert: "",
  id: null,
  form: {
    name: "",
    description: "",
    parentId: null,
    imageFile: null,
  },
});

function openCreate() {
  dlg.mode = "create";
  dlg.id = null;
  dlg.form = { name: "", description: "", parentId: null, imageFile: null };
  dlg.alert = "";
  dlg.open = true;
}

function openEdit(row) {
  dlg.mode = "edit";
  dlg.id = row.id;
  dlg.form = {
    name: row.name,
    description: row.description,
    parentId: row.parentId,
    imageFile: null, 
  };
  dlg.alert = "";
  dlg.open = true;
}

function onPickFile(e) {
  const file = e.target.files[0];
  if (file) {
    dlg.form.imageFile = file;
  }
}

async function save() {
  dlg.alert = "";
  if (!dlg.form.name) {
    dlg.alert = "Category name is required.";
    return;
  }

  dlg.loading = true;
  try {
    const formData = new FormData();
    formData.append("name", dlg.form.name);
    formData.append("description", dlg.form.description || "");
    
    if (dlg.form.parentId) {
      formData.append("parentId", dlg.form.parentId);
    }
    
    if (dlg.form.imageFile) {
      formData.append("imageFile", dlg.form.imageFile);
    }

    if (dlg.mode === "create") {
      await axios.post(BASE_URL, formData);
      toast("Category created successfully.", "success");
    } else {
      await axios.put(`${BASE_URL}/${dlg.id}`, formData);
      toast("Category updated successfully.", "success");
    }

    dlg.open = false;
    load(); 
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Save failed";
    dlg.alert = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    dlg.loading = false;
  }
}

onMounted(() => {
  load();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

.cm-page {
  --c-bg: #f6f7f9;
  --c-card: #ffffff;
  --c-border: #e4e7ec;
  --c-border-light: #f0f2f5;
  --c-text: #0f1117;
  --c-muted: #6b7280;
  --c-subtle: #9ca3af;
  --c-blue: #2563eb;
  --c-blue-bg: #eff6ff;
  --c-blue-border: #bfdbfe;
  --c-green: #16a34a;
  --c-green-bg: #f0fdf4;
  --c-green-border: #bbf7d0;
  --c-red: #dc2626;
  --c-red-bg: #fff1f2;
  --c-red-border: #fecdd3;
  --c-orange: #d97706;
  --c-orange-bg: #fffbeb;
  --radius: 12px;
  --radius-sm: 8px;
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  --shadow-lg: 0 24px 64px rgba(0,0,0,0.13), 0 8px 24px rgba(0,0,0,0.06);

  font-family: 'Plus Jakarta Sans', sans-serif;
  background: var(--c-bg);
  min-height: 100vh;
  padding: 32px 40px 60px;
  color: var(--c-text);
  box-sizing: border-box;
}
*, *::before, *::after { box-sizing: border-box; }

/* ── Header ────────────────────────────────────────────────────── */
.cm-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.cm-header__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--c-muted);
  margin-bottom: 6px;
}
.cm-header__title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 4px;
}
.cm-header__sub {
  font-size: 13.5px;
  color: var(--c-muted);
  margin: 0;
  font-weight: 500;
}
.cm-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  padding-top: 4px;
}

/* ── Buttons ───────────────────────────────────────────────────── */
.cm-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  font-size: 13.5px;
  font-weight: 700;
  border-radius: var(--radius-sm);
  border: none;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: 'Plus Jakarta Sans', sans-serif;
  white-space: nowrap;
}
.cm-btn--sm { padding: 7px 14px; font-size: 12.5px; }
.cm-btn--outline {
  background: var(--c-card);
  border: 1.5px solid var(--c-border);
  color: var(--c-text);
}
.cm-btn--outline:hover:not(:disabled) { border-color: #9ca3af; background: #f9fafb; }
.cm-btn--outline:disabled { opacity: 0.5; cursor: not-allowed; }
.cm-btn--primary {
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  color: #fff;
  box-shadow: 0 4px 12px rgba(37,99,235,0.3);
}
.cm-btn--primary:hover { background: linear-gradient(135deg, #1e40af, #1d4ed8); transform: translateY(-1px); box-shadow: 0 6px 18px rgba(37,99,235,0.35); }
.cm-btn--ghost {
  background: transparent;
  border: 1.5px solid var(--c-border);
  color: var(--c-muted);
  padding: 10px 20px;
}
.cm-btn--ghost:hover { background: var(--c-border-light); color: var(--c-text); }
.cm-btn--loading { opacity: 0.8; pointer-events: none; }

@keyframes spin-r { to { transform: rotate(360deg); } }
.cm-spin { animation: spin-r 0.7s linear infinite; }

/* ── Card ──────────────────────────────────────────────────────── */
.cm-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* ── Toolbar ───────────────────────────────────────────────────── */
.cm-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
}
.cm-toolbar__info {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--c-muted);
}
.cm-count { display: flex; align-items: baseline; gap: 4px; }
.cm-count__num { font-size: 16px; font-weight: 800; color: var(--c-text); }
.cm-count__label { font-size: 12px; color: var(--c-subtle); font-weight: 600; }

/* ── Table ─────────────────────────────────────────────────────── */
.cm-table-wrap {
  overflow-x: auto;
  position: relative;
  min-height: 160px;
}
.cm-table-wrap--loading { pointer-events: none; }

.cm-loader-overlay {
  position: absolute; inset: 0;
  background: rgba(255,255,255,0.75);
  backdrop-filter: blur(2px);
  display: flex; align-items: center; justify-content: center;
  z-index: 10;
}
.cm-loader-ring {
  width: 36px; height: 36px;
  border: 3px solid var(--c-border);
  border-top-color: var(--c-blue);
  border-radius: 50%;
  animation: spin-r 0.7s linear infinite;
}

.cm-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.cm-table thead tr {
  border-bottom: 2px solid var(--c-border-light);
}
.cm-table th {
  padding: 11px 14px;
  text-align: left;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--c-subtle);
  background: #fafbfc;
  white-space: nowrap;
}
.cm-table td {
  padding: 12px 14px;
  vertical-align: middle;
  border-bottom: 1px solid var(--c-border-light);
}

/* Root rows */
.cm-row { transition: background 0.12s; }
.cm-row:hover { background: #fafbfc; }
.cm-row:last-child td { border-bottom: none; }
.cm-row--child { background: #fefefe; }
.cm-row--child:hover { background: #f9fafb; }
.cm-row--grandchild { background: #fdfdfd; }
.cm-row--grandchild:hover { background: #f9fafb; }

/* ID */
.cm-id {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--c-blue);
  background: var(--c-blue-bg);
  padding: 2px 7px;
  border-radius: 5px;
}
.cm-id--child { background: #f3f4f6; color: var(--c-muted); }

/* Name cell with tree connector */
.cm-name-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
.cm-name-cell--l1 { padding-left: 8px; }
.cm-name-cell--l2 { padding-left: 8px; }

.cm-tree-connector { display: flex; align-items: center; flex-shrink: 0; color: var(--c-border); }


.cm-tree-line--root {
  display: inline-flex;
  align-items: center;
  color: var(--c-subtle);
  margin-right: 2px;
}

.cm-name { font-size: 13px; }
.cm-name--root { font-weight: 800; color: var(--c-text); }
.cm-name--branch { font-weight: 700; }
.cm-name--child { font-weight: 600; color: var(--c-text); }
.cm-name--grandchild { font-weight: 500; color: var(--c-muted); }

/* Image */
.cm-img-wrap {
  display: flex;
  justify-content: center;
}
.cm-img {
  width: 64px; height: 46px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  border: 1px solid var(--c-border-light);
}
.cm-img-empty {
  width: 64px; height: 46px;
  border-radius: var(--radius-sm);
  background: var(--c-border-light);
  border: 1px dashed var(--c-border);
  display: flex; align-items: center; justify-content: center;
  color: var(--c-subtle);
}

/* Desc */
.cm-desc {
  font-size: 12.5px;
  color: var(--c-muted);
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 240px;
}

/* Parent badge */
.cm-root-badge {
  font-size: 11.5px;
  font-weight: 700;
  color: var(--c-subtle);
  background: var(--c-border-light);
  padding: 3px 10px;
  border-radius: 999px;
  border: 1px solid var(--c-border);
}
.cm-parent-tag {
  font-size: 11.5px;
  font-weight: 700;
  color: var(--c-blue);
  background: var(--c-blue-bg);
  padding: 3px 10px;
  border-radius: 999px;
  border: 1px solid var(--c-blue-border);
  white-space: nowrap;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
}

/* Status tag */
.cm-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 700;
  white-space: nowrap;
  border: 1px solid transparent;
}
.cm-tag__dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.cm-tag--green { background: var(--c-green-bg); color: var(--c-green); border-color: var(--c-green-border); }
.cm-tag--green .cm-tag__dot { background: var(--c-green); }
.cm-tag--red { background: var(--c-red-bg); color: var(--c-red); border-color: var(--c-red-border); }
.cm-tag--red .cm-tag__dot { background: var(--c-red); }

/* Expand/collapse button */
.cm-expand-btn {
  width: 22px; height: 22px;
  display: inline-flex; align-items: center; justify-content: center;
  border: 1.5px solid var(--c-border);
  border-radius: 6px;
  background: var(--c-bg);
  color: var(--c-muted);
  cursor: pointer;
  transition: all 0.18s ease;
  flex-shrink: 0;
  padding: 0;
}
.cm-expand-btn:hover { border-color: var(--c-blue); color: var(--c-blue); background: var(--c-blue-bg); }
.cm-expand-btn svg { transition: transform 0.2s ease; }
.cm-expand-btn--open svg { transform: rotate(0deg); }
.cm-expand-btn:not(.cm-expand-btn--open) svg { transform: rotate(-90deg); }
.cm-expand-btn--sm { width: 18px; height: 18px; border-radius: 4px; }

.cm-expand-placeholder { width: 22px; flex-shrink: 0; display: inline-block; }

/* Child count badge */
.cm-child-count {
  font-size: 10.5px;
  font-weight: 800;
  color: var(--c-muted);
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  padding: 1px 6px;
  border-radius: 999px;
  margin-left: 4px;
  font-family: 'JetBrains Mono', monospace;
}
.cm-child-count--sm { font-size: 10px; padding: 1px 5px; }
.cm-dropdown-wrap {
  position: relative;
  display: inline-flex;
  justify-content: center;
}
.cm-dropdown-trigger {
  width: 32px; height: 32px;
  display: inline-flex; align-items: center; justify-content: center;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-card);
  color: var(--c-muted);
  cursor: pointer;
  transition: all 0.15s;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.cm-dropdown-trigger:hover {
  border-color: #9ca3af;
  color: var(--c-text);
  background: var(--c-bg);
}
.cm-dropdown-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 150px;
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-md);
  z-index: 100;
  overflow: hidden;
  padding: 4px;
}
.cm-dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: transparent;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text);
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.12s;
  text-align: left;
}
.cm-dropdown-item:hover { background: var(--c-bg); }
.cm-dropdown-item--danger { color: var(--c-red); }
.cm-dropdown-item--danger:hover { background: var(--c-red-bg); }
.cm-dropdown-divider {
  height: 1px;
  background: var(--c-border-light);
  margin: 3px 0;
}

/* Dropdown transition */
.cm-dropdown-pop-enter-active { transition: all 0.15s cubic-bezier(0.34,1.4,0.64,1); }
.cm-dropdown-pop-leave-active { transition: all 0.1s ease; }
.cm-dropdown-pop-enter-from { opacity: 0; transform: scale(0.95) translateY(-4px); }
.cm-dropdown-pop-leave-to   { opacity: 0; transform: scale(0.97) translateY(-2px); }

/* Empty state */
.cm-empty { text-align: center; padding: 64px 20px !important; }
.cm-empty__inner { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.cm-empty__icon {
  width: 56px; height: 56px;
  border-radius: 50%;
  background: var(--c-border-light);
  display: flex; align-items: center; justify-content: center;
  color: var(--c-subtle);
}
.cm-empty__inner p { color: var(--c-muted); font-size: 14px; margin: 0; font-weight: 500; }

/* Footer */
.cm-footer {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid var(--c-border-light);
  background: #fafbfc;
}
.cm-footer__info { font-size: 13px; color: var(--c-muted); font-weight: 500; }
.cm-footer__info strong { color: var(--c-text); font-weight: 800; }

/* ── Dialog ────────────────────────────────────────────────────── */
.cm-overlay {
  position: fixed; inset: 0;
  background: rgba(10,12,20,0.25);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.cm-dialog {
  background: #fff;
  border-radius: 20px;
  width: 100%;
  max-width: 480px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

/* Band */
.cm-dialog__band {
  position: relative;
  height: 110px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 60%, #60a5fa 100%);
  overflow: hidden;
}
.cm-dialog__band-circles { position: absolute; inset: 0; pointer-events: none; }
.cm-band-circle {
  position: absolute; border-radius: 50%;
  background: rgba(255,255,255,0.1);
}
.cm-band-circle--1 { width: 130px; height: 130px; top: -40px; right: -25px; }
.cm-band-circle--2 { width: 70px; height: 70px; bottom: -15px; left: 20px; }
.cm-band-circle--3 { width: 45px; height: 45px; top: 12px; left: 48%; background: rgba(255,255,255,0.07); }
.cm-dialog__icon-wrap { position: relative; z-index: 1; }
.cm-dialog__icon-ring {
  width: 60px; height: 60px;
  background: rgba(255,255,255,0.18);
  border: 2px solid rgba(255,255,255,0.35);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  backdrop-filter: blur(4px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.14), inset 0 1px 0 rgba(255,255,255,0.3);
}

/* Body */
.cm-dialog__body {
  padding: 26px 30px 28px;
  display: flex; flex-direction: column; gap: 14px;
}
.cm-dialog__badge {
  display: inline-flex; align-self: flex-start;
  font-size: 10.5px; font-weight: 800; letter-spacing: 0.1em;
  text-transform: uppercase;
  padding: 3px 10px; border-radius: 999px;
  background: var(--c-blue-bg); color: var(--c-blue);
  border: 1px solid var(--c-blue-border);
}
.cm-dialog__title {
  font-size: 20px; font-weight: 800; letter-spacing: -0.03em; margin: 0; line-height: 1.2;
}

/* Alert */
.cm-alert {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px;
  background: var(--c-red-bg);
  border: 1px solid var(--c-red-border);
  border-radius: var(--radius-sm);
  font-size: 13px; font-weight: 600; color: var(--c-red);
}

/* Form */
.cm-form { display: flex; flex-direction: column; gap: 14px; }
.cm-field { display: flex; flex-direction: column; gap: 6px; }
.cm-field__label {
  font-size: 11px; font-weight: 700;
  text-transform: uppercase; letter-spacing: 0.06em;
  color: var(--c-text);
}
.cm-required { color: var(--c-red); margin-left: 2px; }
.cm-field__hint { font-size: 11.5px; color: var(--c-subtle); margin: 3px 0 0; font-weight: 500; }

.cm-input, .cm-select, .cm-textarea {
  width: 100%;
  padding: 10px 13px;
  font-size: 13.5px;
  font-family: 'Plus Jakarta Sans', sans-serif;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  color: var(--c-text);
  background: #fafbfc;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.cm-input:focus, .cm-select:focus, .cm-textarea:focus {
  border-color: var(--c-blue);
  box-shadow: 0 0 0 3px rgba(37,99,235,0.08);
  background: #fff;
}
.cm-input--error { border-color: var(--c-red); background: var(--c-red-bg); }
.cm-textarea { resize: vertical; line-height: 1.5; }
.cm-select { cursor: pointer; }

/* File input */

.cm-file-label {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 9px 14px;
  border: 1.5px dashed var(--c-border);
  border-radius: var(--radius-sm);
  background: #fafbfc;
  font-size: 13px; font-weight: 600; color: var(--c-muted);
  cursor: pointer; transition: all 0.15s;
  width: 100%;
}
.cm-file-label:hover { border-color: var(--c-blue); color: var(--c-blue); background: var(--c-blue-bg); }

/* Dialog actions */
.cm-dialog__actions {
  display: flex; gap: 10px; justify-content: flex-end;
  padding-top: 4px;
}

/* Spinner */
.cm-spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin-r 0.6s linear infinite;
  flex-shrink: 0;
}

/* Red band variant */
.cm-dialog__band--red {
  background: linear-gradient(135deg, #b91c1c 0%, #dc2626 60%, #f87171 100%);
}

/* Dialog desc / notice */
.cm-dialog__desc {
  font-size: 14px;
  color: var(--c-muted);
  line-height: 1.65;
  margin: 0;
}
.cm-dialog__highlight {
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  padding: 1px 6px;
  border-radius: 5px;
}
.cm-dialog__highlight--red { color: var(--c-red); background: var(--c-red-bg); }

.cm-dialog__notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  font-size: 12.5px;
  font-weight: 600;
  line-height: 1.4;
}
.cm-dialog__notice--red {
  background: var(--c-red-bg);
  color: #b91c1c;
  border: 1px solid var(--c-red-border);
}

/* Badge red variant */
.cm-dialog__badge--red {
  background: var(--c-red-bg);
  color: var(--c-red);
  border: 1px solid var(--c-red-border);
}

/* Danger button */
.cm-btn--danger {
  background: linear-gradient(135deg, #b91c1c, #dc2626);
  color: #fff;
  box-shadow: 0 4px 12px rgba(220,38,38,0.3);
}
.cm-btn--danger:hover {
  background: linear-gradient(135deg, #991b1b, #b91c1c);
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(220,38,38,0.38);
}
.cm-btn--danger.cm-btn--loading { opacity: 0.75; pointer-events: none; transform: none; }

/* ── Transitions ───────────────────────────────────────────────── */
.cm-dialog-fade-enter-active,
.cm-dialog-fade-leave-active { transition: opacity 0.22s ease; }
.cm-dialog-fade-enter-active .cm-dialog,
.cm-dialog-fade-leave-active .cm-dialog {
  transition: transform 0.28s cubic-bezier(0.34,1.4,0.64,1), opacity 0.22s ease;
}
.cm-dialog-fade-enter-from, .cm-dialog-fade-leave-to { opacity: 0; }
.cm-dialog-fade-enter-from .cm-dialog { transform: scale(0.9) translateY(20px); opacity: 0; }
.cm-dialog-fade-leave-to .cm-dialog { transform: scale(0.96) translateY(8px); opacity: 0; }

/* ── Responsive ────────────────────────────────────────────────── */
@media (max-width: 900px) {
  .cm-page { padding: 20px 16px 40px; }
  .cm-header { flex-direction: column; }
  .cm-dialog__body { padding: 20px 20px 24px; }
}
</style>