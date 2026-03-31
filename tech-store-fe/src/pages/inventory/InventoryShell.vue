<template>
  <el-container style="height: 100vh; overflow: hidden;">

    <!-- ═══════════════════════════════════
         SIDEBAR
    ═══════════════════════════════════ -->
    <el-aside
      width="256px"
      style="position: fixed; top: 0; left: 0; height: 100vh; background: #0f172a;
             display: flex; flex-direction: column; overflow: hidden;
             border-right: 1px solid rgba(255,255,255,0.05); z-index: 100;"
    >
      <!-- Brand -->
      <el-button
        text
        style="padding: 20px 20px 16px; height: auto; justify-content: flex-start; width: 100%;"
      >
        <el-space :size="10">
          <el-avatar
            :size="36"
            shape="square"
            style="background: #3b82f6; border-radius: 8px; flex-shrink: 0;"
          >
            <el-icon :size="18"><Box /></el-icon>
          </el-avatar>
          <el-space direction="vertical" :size="1">
            <el-text tag="b" style="color: #f8fafc; font-size: 15px; letter-spacing: -0.3px;">TechStore</el-text>
            <el-text size="small" style="color: #64748b; letter-spacing: 0.5px;">Inventory Portal</el-text>
          </el-space>
        </el-space>
      </el-button>

      <!-- User pill -->
      <el-card
        shadow="never"
        style="margin: 0 14px 12px; background: rgba(255,255,255,0.05);
               border: 1px solid rgba(255,255,255,0.08); border-radius: 8px;"
        :body-style="{ padding: '9px 11px' }"
      >
        <el-space :size="9">
          <el-avatar
            :size="30"
            shape="square"
            style="background: linear-gradient(135deg, #3b82f6, #06b6d4); border-radius: 7px;
                   flex-shrink: 0; font-size: 10px; font-weight: 700;"
          >
            
          </el-avatar>
          <el-space direction="vertical" :size="3" style="min-width: 0;">
            <el-text
              size="small"
              style="color: #e2e8f0; font-weight: 600; display: block;
                     overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 148px;"
            >Inventory Staff</el-text>
            <el-tag
              size="small"
              effect="plain"
              style="color: #22d3ee; background: rgba(34,211,238,0.1);
                     border-color: rgba(34,211,238,0.2); font-size: 9px; letter-spacing: 0.8px;
                     height: 18px; padding: 0 6px;"
            >INVENTORY</el-tag>
          </el-space>
        </el-space>
      </el-card>

      <el-divider style="margin: 0 0 4px; border-color: rgba(255,255,255,0.06);" />

      <!-- Scrollable menu -->
      <el-scrollbar style="flex: 1;">
        <el-menu
          router
          :default-active="$route.path"
          background-color="#0f172a"
          text-color="#94a3b8"
          active-text-color="#93c5fd"
          style="border: none; padding: 0 8px;"
        >
          <el-menu-item-group title="QUẢN LÝ ĐƠN HÀNG">
            <el-menu-item index="/inventory/orders/paid">
              <el-icon><DocumentChecked /></el-icon>
              <template #title>
                <el-space :size="6" style="width: 100%; justify-content: space-between;">
                  <span>Đơn đã thanh toán</span>
                  <el-tag size="small" type="success" effect="plain" style="margin-left: auto; font-size: 8.5px; height: 18px; padding: 0 6px;">Mới</el-tag>
                </el-space>
              </template>
            </el-menu-item>

            <el-menu-item index="/inventory/orders/processing">
              <el-icon><Timer /></el-icon>
              <span>Đang xử lý</span>
            </el-menu-item>
          </el-menu-item-group>
        </el-menu>
      </el-scrollbar>

      <!-- Sidebar footer -->
      <el-space
        direction="vertical"
        fill
        :size="6"
        style="padding: 10px 14px 14px; border-top: 1px solid rgba(255,255,255,0.06);"
      >
        <el-button type="danger" plain @click="logout" style="width: 100%;">
          <el-icon><SwitchButton /></el-icon>
          Đăng xuất
        </el-button>
      </el-space>
    </el-aside>

    <!-- ═══════════════════════════════════
         MAIN CONTENT
    ═══════════════════════════════════ -->
    <el-main style="margin-left: 256px; height: 100vh; overflow-y: auto; padding: 24px;">
      <router-view />
    </el-main>

  </el-container>
</template>

<script setup>
import { Box, DocumentChecked, SwitchButton, Timer } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { confirmModal } from "../../ui/confirm";
import { toast } from "../../ui/toast";
import { clearLastAuthResponse, clearSession } from "../../stores/auth";

const router = useRouter();

async function logout() {
  const ok = await confirmModal("Do you want to log out?", "Confirm", "Log out", true);
  if (!ok) return;
  clearSession();
  clearLastAuthResponse();
  toast("Logged out.", "success");
  router.push("/system/login");
}
</script>

<style></style>