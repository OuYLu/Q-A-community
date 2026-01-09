package com.community.common.context;

public class CurrentUserContext {
    private static final ThreadLocal<UserInfo> CONTEXT = new ThreadLocal<>();

    public static void set(UserInfo userInfo) {
        CONTEXT.set(userInfo);
    }

    public static UserInfo get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static class UserInfo {
        private Long userId;
        private String username;

        public UserInfo(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }
    }
}
