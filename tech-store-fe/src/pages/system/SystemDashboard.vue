<template>
  <div class="dash">
    <div class="page-head">
      <div>
        <div class="kicker">System</div>
        <div class="title">Session / Token</div>
        <div class="sub ts-muted">
          Thông tin session đang đăng nhập (readonly).
        </div>
      </div>
    </div>

    <div class="card ts-card w-100">
      <div class="card-header bg-transparent border-0 pt-4 px-4">
        <div class="hdr">
          <div class="h">Session / Token</div>
          <span class="badge text-bg-success ts-pill text-uppercase">{{
            role || "n/a"
          }}</span>
        </div>
      </div>

      <div class="card-body px-4 pb-4">
        <div class="row g-3">
          <!-- LEFT: stats -->
          <div class="col-12 col-lg-4">
            <div class="stats">
              <div class="stat">
                <div class="stat__k ts-muted">Token</div>
                <div class="stat__v">
                  <span v-if="token" class="badge text-bg-success ts-pill"
                    >Available</span
                  >
                  <span v-else class="badge text-bg-danger ts-pill"
                    >Missing</span
                  >
                </div>
              </div>

              <div class="stat">
                <div class="stat__k ts-muted">Length</div>
                <div class="stat__v">
                  <b>{{ token ? token.length : 0 }}</b>
                </div>
              </div>

              <div class="stat">
                <div class="stat__k ts-muted">Role</div>
                <div class="stat__v">
                  <span class="badge text-bg-success ts-pill text-uppercase">{{
                    role || "n/a"
                  }}</span>
                </div>
              </div>

              <div class="stat">
                <div class="stat__k ts-muted">Status</div>
                <div class="stat__v">
                  <span v-if="token" class="ok">Logged in</span>
                  <span v-else class="bad">Not logged in</span>
                </div>
              </div>
            </div>

            <div class="hint ts-muted mt-3">
              Tip: token chỉ để debug nhanh. Bạn có thể copy dán vào Postman khi
              test.
            </div>
          </div>

          <!-- RIGHT: token textarea -->
          <div class="col-12 col-lg-8">
            <div class="block">
              <div class="label">Access token</div>
              <textarea
                class="form-control ts-input"
                :value="token"
                rows="16"
                readonly
                placeholder="No token"
              ></textarea>
              <div class="ts-muted small mt-1">(Readonly)</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { getToken, getRole } from "../../stores/auth";

const token = computed(() => getToken());
const role = computed(() => (getRole() || "").toUpperCase());
</script>

<style scoped>
.dash {
  display: grid;
  gap: 16px;
  width: 100%;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding: 4px 2px;
}

.kicker {
  font-size: 12px;
  opacity: 0.75;
  font-weight: 900;
  text-transform: uppercase;
}
.title {
  font-weight: 950;
  font-size: 22px;
  letter-spacing: -0.3px;
}
.sub {
  font-size: 13px;
}

.hdr {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.h {
  font-weight: 900;
  font-size: 16px;
}

.stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.stat {
  padding: 12px 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.6);
}
.stat__k {
  font-size: 12px;
  margin-bottom: 6px;
}
.stat__v {
  font-size: 14px;
}
.ok {
  font-weight: 800;
}
.bad {
  font-weight: 800;
  opacity: 0.7;
}

.block {
  display: grid;
  gap: 8px;
}
.label {
  font-weight: 800;
}
.small {
  font-size: 12px;
}

@media (max-width: 640px) {
  .stats {
    grid-template-columns: 1fr;
  }
}
</style>
