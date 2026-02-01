# Frontend Admin

Vue 3 + Vite + TypeScript 的后台管理前端。内置 Element Plus、Pinia、Vue Router、Axios，基本路由/鉴权/布局都已搭好。

## 快速开始

- 安装依赖：`npm install`
- 本地开发：`npm run dev`
- 打包构建：`npm run build`，产物在 `dist/`

## 配置后端地址

- Axios 基础地址在 `src/utils/request.ts`，默认 `http://localhost:8080`。
- 如需区分环境，可改为读取环境变量（如 `import.meta.env.VITE_API_BASE`）后在 `.env.*` 中配置。

## 登录测试

- 访问 `/login`，输入任意用户名/密码点击登录，会写入 mock token 并跳转到角色管理页。
- 登录状态保存在 Pinia + `localStorage`。退出按钮在右上角。
- 路由守卫：`/roles` `/permissions` `/users` 需要登录；40101 会清空 token 并跳转登录，40301 会弹出无权限提示。

## 目录结构

- `src/router` 路由及守卫
- `src/store/auth.ts` 鉴权 store
- `src/utils/request.ts` Axios 封装
- `src/layout` 基础后台布局
- `src/views` 登录及各业务页面骨架
