# campus-study-partner-system

校园学习伙伴推荐系统，后端基于 **Java Spring Boot + JPA + MySQL**，并额外将核心实体关系同步至 **Neo4j** 图数据库；前端基于 **Vue 3 + Vite + Axios**，覆盖学生、课程、兴趣标签、合作记录与推荐能力的最小可运行原型。

## 后端（Java / Spring Boot）
### 运行
```bash
cd backend
mvn spring-boot:run
```
默认监听 `http://localhost:8080`，连接本地 MySQL (默认 `jdbc:mysql://localhost:3306/campus_partner`，用户名 `root`，密码 `123456`)，启动时自动迁移表结构，并在 Neo4j (默认 `bolt://localhost:7687`，用户名 `neo4j`，密码 `changeme`) 中同步学生、课程、兴趣标签及合作关系。健康检查：`GET /api/health`。

> 如需调整数据库，编辑 `backend/src/main/resources/application.properties` 或设置环境变量：
> - `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD` 覆盖 MySQL 连接
> - `SPRING_NEO4J_URI`、`SPRING_NEO4J_AUTHENTICATION_USERNAME`、`SPRING_NEO4J_AUTHENTICATION_PASSWORD` 覆盖 Neo4j 连接

### 主要接口
- 创建学生：`POST /api/students` (body: `{ "name": "张三", "major": "CS" }`)
- 创建课程 / 标签：`POST /api/courses`，`POST /api/tags`
- 绑定课程 / 标签：`POST /api/students/{id}/courses/{courseId}`，`POST /api/students/{id}/tags/{tagId}`
- 记录合作：`POST /api/collaborations` (body: `{ "studentId1":1, "studentId2":2 }`)
- 推荐：`GET /api/students/{id}/recommendations?limit=5`

推荐评分规则：共同课程 0.6、共同兴趣 0.3、历史合作次数 0.2 权重叠加，并返回理由列表。

## 前端（Vue 3 / Vite）
### 运行
```bash
cd frontend
npm install
npm run dev
```
默认通过 Vite 在 `http://localhost:5173` 提供页面，Axios 指向 `http://localhost:8080/api`。界面包含学生/课程/标签创建、绑定、合作记录以及推荐查询，方便端到端验证。

## 目录结构
- `backend/`：Spring Boot 源码与 `pom.xml`
- `frontend/`：Vue 3 + Vite 前端工程
- `app/`：已移除的早期 Python 原型代码（全部功能由 Java 实现提供）

## 设计文档
根目录提供《小组2概要设计说明书.docx》，后端模型与接口遵循该文档中的实体与推荐需求。
