import http from "./http";

export const notificationsApi = {
  getMyNotifications(unreadOnly = false) {
    return http.get(`/api/auth/notifications/my?unreadOnly=${unreadOnly}`);
  },
  getUnreadCount() {
    return http.get("/api/auth/notifications/my/unread-count");
  },
  markAsRead(notificationId) {
    return http.put(`/api/auth/notifications/${notificationId}/read`);
  },
  markAllAsRead() {
    return http.put("/api/auth/notifications/read-all");
  },
  deleteNotification(notificationId) {
    return http.delete(`/api/auth/notifications/${notificationId}`);
  },
};

// Birthday API
export const birthdayApi = {
  getTodayBirthdays() {
    return http.get("/api/auth/admin/birthdays/today");
  },
  getBirthdaysByMonth(month) {
    return http.get(`/api/auth/admin/birthdays/month/${month}`);
  },
  getStatistics() {
    return http.get("/api/auth/admin/birthdays/statistics");
  },
  getUpcomingBirthdays(days = 7) {
    return http.get(`/api/auth/admin/birthdays/upcoming?days=${days}`);
  },
  sendGreeting(customerId, message) {
    return http.post(`/api/auth/admin/birthdays/send-greeting/${customerId}`, { message });
  },
  getNotificationHistory(from, to) {
    let url = "/api/auth/admin/birthdays/notification-history";
    const params = new URLSearchParams();
    if (from) params.append("from", from);
    if (to) params.append("to", to);
    if (params.toString()) url += `?${params.toString()}`;
    return http.get(url);
  },
};

// Purchase Reminder API
export const purchaseReminderApi = {
  // Admin: Lấy lịch sử reminder đã gửi
  getHistory() {
    return http.get("/api/auth/notifications/purchase-reminders/history");
  },

  // Admin: Trigger thủ công (test hoặc gửi ngay không chờ scheduler)
  triggerNow() {
    return http.post("/api/auth/notifications/purchase-reminders/trigger");
  },

  // Lọc reminder theo segment từ history trả về
  // (filter client-side vì backend trả toàn bộ)
  getHistoryBySegment(segment) {
    return http.get("/api/auth/notifications/purchase-reminders/history").then((res) => {
      if (!segment) return res;
      return {
        ...res,
        data: res.data.filter((n) => {
          if (segment === "COLD_30") return n.type === "PURCHASE_REMINDER" && n.title?.includes("mới về");
          if (segment === "COLD_60") return n.type === "PURCHASE_REMINDER" && n.title?.includes("48h");
          if (segment === "WINBACK") return n.type === "WINBACK";
          return true;
        }),
      };
    });
  },
};