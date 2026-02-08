import http from "./http";

export const notificationsApi = {
  // Lấy danh sách thông báo
  getMyNotifications(unreadOnly = false) {
    return http.get(`/api/auth/notifications/my?unreadOnly=${unreadOnly}`);
  },

  // Đếm số thông báo chưa đọc
  getUnreadCount() {
    return http.get("/api/auth/notifications/my/unread-count");
  },

  // Đánh dấu thông báo đã đọc
  markAsRead(notificationId) {
    return http.put(`/api/auth/notifications/${notificationId}/read`);
  },

  // Đánh dấu tất cả đã đọc
  markAllAsRead() {
    return http.put("/api/auth/notifications/read-all");
  },

  // Xóa thông báo
  deleteNotification(notificationId) {
    return http.delete(`/api/auth/notifications/${notificationId}`);
  },
};

// Birthday API
export const birthdayApi = {
  // Admin: Lấy sinh nhật hôm nay
  getTodayBirthdays() {
    return http.get("/api/auth/admin/birthdays/today");
  },

  // Admin: Lấy sinh nhật theo tháng
  getBirthdaysByMonth(month) {
    return http.get(`/api/auth/admin/birthdays/month/${month}`);
  },

  // Admin: Lấy thống kê
  getStatistics() {
    return http.get("/api/auth/admin/birthdays/statistics");
  },

  // Admin: Lấy sinh nhật sắp tới
  getUpcomingBirthdays(days = 7) {
    return http.get(`/api/auth/admin/birthdays/upcoming?days=${days}`);
  },

  // Admin: Gửi lời chúc
  sendGreeting(customerId, message) {
    return http.post(`/api/auth/admin/birthdays/send-greeting/${customerId}`, {
      message,
    });
  },
  getNotificationHistory(from, to) {
    let url = "/api/auth/admin/birthdays/notification-history";
    const params = new URLSearchParams();
    
    if (from) params.append("from", from);
    if (to) params.append("to", to);
    
    if (params.toString()) {
      url += `?${params.toString()}`;
    }
    
    return http.get(url);
  },
};