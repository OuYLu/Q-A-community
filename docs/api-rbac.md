# RBAC API 文档

鉴权方式：HTTP Header `Authorization: Bearer <token>`

统一返回：`Result<T>`，字段 `code`(0=成功)/`message`/`data`/`timestamp`

错误码：40001 参数错误；40101 未登录或过期；40301 无权限；40401 资源不存在；50001 业务错误；50000 系统异常

---

## 认证

### 登录
- `POST /api/auth/login`
- Body
```json
{"username":"admin","password":"password"}
```
- Response
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "<jwt-token>",
    "expiresAt": 1736380000000
  },
  "timestamp": 1736300000000
}
```

---

## 权限（Permission）

### 查询权限列表（可搜索）
- `GET /api/admin/permissions?keyword=xxx`
- 权限：`admin:permission:read`
- Response.data：`Permission[]`，字段 `id, code, name, type, pathOrApi, method, createdAt`

---

## 角色（Role）

### 查询角色列表
- `GET /api/admin/roles?keyword=xxx`
- 权限：`admin:role:read`
- Response.data：`Role[]`

### 新增角色
- `POST /api/admin/roles`
- 权限：`admin:role:create`
- Body
```json
{"code":"operator","name":"运营人员","description":"..."}
```

### 编辑角色
- `PUT /api/admin/roles/{roleId}`
- 权限：`admin:role:update`
- Body
```json
{"name":"新名字","description":"更新描述"}
```

### 删除角色
- `DELETE /api/admin/roles/{roleId}`
- 权限：`admin:role:delete`
- Response.data：`"删除成功"`

---

## 角色权限（role -> permissions）

### 查询角色已有权限
- `GET /api/admin/roles/{roleId}/permissions`
- 权限：`admin:role:read`
- Query 参数：`group`（可选，true 按 code 前缀分组）
- Response.data
```json
{
  "codes": ["admin:ping","user:ping"],
  "items": [
    {
      "id": 1,
      "code": "admin:ping",
      "name": "系统管理",
      "description": null,
      "type": "API",
      "pathOrApi": "/api/admin/ping",
      "method": "GET"
    }
  ],
  "groups": [
    {
      "moduleKey": "admin",
      "moduleName": "系统管理",
      "children": [ { "...": "同上 items" } ]
    }
  ]
}
```

### 覆盖式设置角色权限
- `PUT /api/admin/roles/{roleId}/permissions`
- 权限：`admin:role:grant`
- Body
```json
{"permissionCodes":["admin:permission:read","admin:role:read"]}
```

---

## 用户角色（user -> roles）

### 查询用户列表（分页，可按角色过滤）
- `GET /api/admin/users?page=1&pageSize=10&keyword=xxx&roleCode=admin`
- 权限：`admin:user:read`
- Response.data：
```json
{
  "total": 1,
  "list": [
    {"id":1,"username":"admin","nickname":null,"status":1,"createdAt":"2025-12-16T00:00:00"}
  ]
}
```

### 查询用户详情
- `GET /api/admin/users/{userId}`
- 权限：`admin:user:read`
- Response.data：`{id,username,phone,nickname,status,createdAt}`

### 冻结/解冻用户
- `PUT /api/admin/users/{userId}/status`
- 权限：`admin:user:update`
- Body
```json
{"status":1}  // 1=正常, 0=冻结
```
- Response.data：`"更新用户状态成功"`

### 创建用户
- `POST /api/admin/users`
- 权限：`admin:user:create`
- Body
```json
{
  "username":"operator1",
  "password":"secret123",
  "phone":"18800000001",
  "nickname":"运营1",
  "roleCodes":["operator"]
}
```
- Response.data：新建用户基本信息（不含密码）

### 重置密码
- `PUT /api/admin/users/{userId}/password`
- 权限：`admin:user:update`
- Body
```json
{"newPassword":"newSecret123"}
```
- Response.data：`"重置密码成功"`

### 查询用户已有角色
- `GET /api/admin/users/{userId}/roles`
- 权限：`admin:user:assign-role`
- Response.data：`["customer","expert"]`

### 覆盖式设置用户角色
- `PUT /api/admin/users/{userId}/roles`
- 权限：`admin:user:assign-role`
- Body
```json
{"roleCodes":["customer","expert"]}
```

---

## 示例错误响应
```json
{"code":40101,"message":"未登录或登录已过期","data":null,"timestamp":1736300000000}
```
