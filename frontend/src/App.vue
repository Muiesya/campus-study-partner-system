<template>
  <main class="page">
    <section class="card hero">
      <div>
        <p class="eyebrow">校园学习伙伴 · Demo</p>
        <h1>协同推荐可视化面板</h1>
        <p class="muted">使用 Spring Boot + MySQL + Neo4j 后端，配合 Vue 3 前端快速体验学生、课程、兴趣标签及合作记录的推荐流程。</p>
        <div class="tags">
          <span class="pill">MySQL 持久化</span>
          <span class="pill">Neo4j 图同步</span>
          <span class="pill">Vue 3 + Vite</span>
        </div>
      </div>
      <div class="hero-status">
        <p class="muted">接口基址：{{ apiBase }}</p>
        <p class="status" :class="online ? 'ok' : 'warn'">{{ online ? '后端已联通' : '等待连接' }}</p>
        <button class="ghost" @click="loadBasics" :disabled="loading">刷新数据</button>
      </div>
    </section>

    <section class="card">
      <div class="section-header">
        <h2>数据总览</h2>
        <p class="muted">实时掌握学生、课程、标签数量以及已生成的推荐结果数量。</p>
      </div>
      <div class="stats">
        <div class="stat" v-for="stat in stats" :key="stat.label">
          <p class="label">{{ stat.label }}</p>
          <p class="value">{{ stat.value }}</p>
        </div>
      </div>
    </section>

    <section class="card grid">
      <div class="panel">
        <div class="section-header">
          <h2>创建学生</h2>
          <p class="muted">填写姓名与专业，创建后会同步到 MySQL 与 Neo4j。</p>
        </div>
        <form @submit.prevent="createStudent">
          <label>姓名 <input v-model="newStudent.name" required /></label>
          <label>专业 <input v-model="newStudent.major" required /></label>
          <button type="submit" :disabled="loading">创建学生</button>
        </form>
        <div class="list-row" v-if="students.length">
          <p class="muted">当前学生</p>
          <div class="chips">
            <span v-for="s in students" :key="s.id" class="pill strong">{{ s.id }} · {{ s.name }}（{{ s.major }}）</span>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="section-header">
          <h2>记录合作</h2>
          <p class="muted">为两位学生叠加一次合作次数，推荐时会提升分数。</p>
        </div>
        <form @submit.prevent="recordCollaboration">
          <label>学生 A ID <input v-model.number="collaboration.studentId1" required type="number" min="1" /></label>
          <label>学生 B ID <input v-model.number="collaboration.studentId2" required type="number" min="1" /></label>
          <button type="submit" :disabled="loading">记录合作</button>
        </form>
      </div>
    </section>

    <section class="card grid">
      <div class="panel">
        <div class="section-header">
          <h2>课程与标签</h2>
          <p class="muted">先补充课程与兴趣标签，再为学生绑定。</p>
        </div>
        <div class="inline-forms">
          <form @submit.prevent="createCourse">
            <label>课程名 <input v-model="newCourse.name" required /></label>
            <label>简介 <input v-model="newCourse.description" placeholder="可选" /></label>
            <button type="submit" :disabled="loading">新增课程</button>
          </form>
          <form @submit.prevent="createTag">
            <label>兴趣标签 <input v-model="newTag.name" required /></label>
            <button type="submit" :disabled="loading">新增标签</button>
          </form>
        </div>
        <div class="list-row" v-if="courses.length || tags.length">
          <div>
            <p class="muted">课程列表</p>
            <div class="chips" v-if="courses.length">
              <span v-for="c in courses" :key="c.id" class="pill">{{ c.id }} · {{ c.name }}</span>
            </div>
          </div>
          <div>
            <p class="muted">标签列表</p>
            <div class="chips" v-if="tags.length">
              <span v-for="t in tags" :key="t.id" class="pill">{{ t.id }} · {{ t.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="section-header">
          <h2>绑定关系</h2>
          <p class="muted">将学生与课程、兴趣关联，推荐将综合这些相似度。</p>
        </div>
        <form @submit.prevent="bindCourse">
          <div class="two-cols">
            <label>学生 ID <input v-model.number="bind.course.studentId" required type="number" min="1" /></label>
            <label>课程 ID <input v-model.number="bind.course.courseId" required type="number" min="1" /></label>
          </div>
          <button type="submit" :disabled="loading">绑定课程</button>
        </form>
        <form @submit.prevent="bindTag">
          <div class="two-cols">
            <label>学生 ID <input v-model.number="bind.tag.studentId" required type="number" min="1" /></label>
            <label>标签 ID <input v-model.number="bind.tag.tagId" required type="number" min="1" /></label>
          </div>
          <button type="submit" :disabled="loading">绑定标签</button>
        </form>
      </div>
    </section>

    <section class="card">
      <div class="section-header">
        <div>
          <h2>获取推荐</h2>
          <p class="muted">基于共同课程、兴趣及合作次数加权排序，返回最佳学习伙伴。</p>
        </div>
        <button class="ghost" type="button" @click="fetchRecommendations" :disabled="loading || !targetStudent">立即获取</button>
      </div>
      <form class="inline" @submit.prevent="fetchRecommendations">
        <label>学生 ID <input v-model.number="targetStudent" required type="number" min="1" /></label>
        <label>数量 <input v-model.number="limit" min="1" type="number" /></label>
        <button type="submit" :disabled="loading">查询</button>
      </form>

      <div v-if="recommendations.length" class="recommendations">
        <article v-for="item in recommendations" :key="item.studentId" class="recommendation">
          <div class="recommendation-header">
            <div>
              <p class="muted">推荐对象</p>
              <h3>{{ item.studentName }}（ID {{ item.studentId }}）</h3>
            </div>
            <span class="score">{{ item.score.toFixed(2) }}</span>
          </div>
          <ul>
            <li v-for="reason in item.reasons" :key="reason">{{ reason }}</li>
          </ul>
        </article>
      </div>
      <p v-else class="muted empty">暂无推荐结果，先录入数据并提交查询。</p>
    </section>

    <p class="error" v-if="error">{{ error }}</p>
    <div class="loading" v-if="loading">
      <span class="dot"></span>
      <span class="dot"></span>
      <span class="dot"></span>
      <span class="muted">正在与后端通信...</span>
    </div>
  </main>
</template>

<script setup>
import axios from 'axios';
import { computed, onMounted, reactive, ref } from 'vue';

const apiBase = 'http://localhost:8080/api';
const client = axios.create({ baseURL: apiBase });
const students = ref([]);
const courses = ref([]);
const tags = ref([]);
const recommendations = ref([]);
const error = ref('');
const loading = ref(false);
const online = ref(false);

const newStudent = reactive({ name: '', major: '' });
const newCourse = reactive({ name: '', description: '' });
const newTag = reactive({ name: '' });
const bind = reactive({ course: { studentId: null, courseId: null }, tag: { studentId: null, tagId: null } });
const collaboration = reactive({ studentId1: null, studentId2: null });
const targetStudent = ref(null);
const limit = ref(5);

const stats = computed(() => [
  { label: '学生', value: students.value.length },
  { label: '课程', value: courses.value.length },
  { label: '兴趣标签', value: tags.value.length },
  { label: '推荐结果', value: recommendations.value.length }
]);

const resetError = () => (error.value = '');

const handleRequest = async (task, fallbackMessage) => {
  resetError();
  loading.value = true;
  try {
    await task();
    online.value = true;
  } catch (e) {
    error.value = e?.response?.data?.message || fallbackMessage || '请求失败，请稍后重试。';
    online.value = false;
  } finally {
    loading.value = false;
  }
};

const loadBasicsRaw = async () => {
  const [s, c, t] = await Promise.all([
    client.get('/students'),
    client.get('/courses'),
    client.get('/tags')
  ]);
  students.value = s.data;
  courses.value = c.data;
  tags.value = t.data;
};

const loadBasics = async () => {
  await handleRequest(loadBasicsRaw, '无法加载基础数据，请确认后端已启动。');
};

const createStudent = async () => {
  await handleRequest(async () => {
    await client.post('/students', newStudent);
    newStudent.name = '';
    newStudent.major = '';
    await loadBasicsRaw();
  }, '创建学生失败，请检查输入。');
};

const createCourse = async () => {
  await handleRequest(async () => {
    await client.post('/courses', newCourse);
    newCourse.name = '';
    newCourse.description = '';
    await loadBasicsRaw();
  }, '新增课程失败，请确认名称未重复。');
};

const createTag = async () => {
  await handleRequest(async () => {
    await client.post('/tags', newTag);
    newTag.name = '';
    await loadBasicsRaw();
  }, '新增标签失败，请确认名称未重复。');
};

const bindCourse = async () => {
  await handleRequest(async () => {
    await client.post(`/students/${bind.course.studentId}/courses/${bind.course.courseId}`);
  }, '绑定课程失败，请检查学生与课程是否存在。');
};

const bindTag = async () => {
  await handleRequest(async () => {
    await client.post(`/students/${bind.tag.studentId}/tags/${bind.tag.tagId}`);
  }, '绑定标签失败，请检查学生与标签是否存在。');
};

const recordCollaboration = async () => {
  await handleRequest(async () => {
    await client.post('/collaborations', collaboration);
  }, '记录合作失败，请确认学生 ID 正确。');
};

const fetchRecommendations = async () => {
  await handleRequest(async () => {
    const { data } = await client.get(`/students/${targetStudent.value}/recommendations`, { params: { limit: limit.value } });
    recommendations.value = data;
  }, '获取推荐失败，请确认学生存在。');
};

onMounted(loadBasics);
</script>

<style scoped>
:global(body) {
  font-family: "Inter", system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  background: linear-gradient(180deg, #eef2ff 0%, #f9fafb 40%, #f5f7fb 100%);
  margin: 0;
  color: #0f172a;
}

.page {
  max-width: 1180px;
  margin: 0 auto 40px;
  padding: 26px 18px 40px;
}

.card {
  background: white;
  padding: 18px 20px 20px;
  margin-top: 18px;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.06);
  border: 1px solid #e5e7eb;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  background: radial-gradient(circle at 20% 20%, #e0e7ff 0, #fff 60%);
}

.hero-status {
  min-width: 220px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
  text-align: right;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.section-header h2 {
  margin: 0;
}

.eyebrow {
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #6366f1;
  margin: 0;
  font-weight: 700;
}

h1 {
  margin: 6px 0;
  font-size: 28px;
}

h2 {
  margin: 0 0 6px;
}

h3 {
  margin: 0;
}

.muted {
  color: #6b7280;
  margin: 0;
}

.tags, .chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.pill {
  display: inline-flex;
  align-items: center;
  background: #eef2ff;
  color: #312e81;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 13px;
}

.pill.strong {
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 600;
}

.status {
  padding: 8px 10px;
  border-radius: 10px;
  font-weight: 700;
  margin: 0;
}

.status.ok { background: #ecfdf3; color: #15803d; border: 1px solid #bbf7d0; }
.status.warn { background: #fff7ed; color: #c2410c; border: 1px solid #fed7aa; }

button {
  padding: 10px 14px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(90deg, #4f46e5, #6366f1);
  color: white;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.1s ease, box-shadow 0.2s ease;
  box-shadow: 0 10px 20px rgba(99, 102, 241, 0.25);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

button:hover:not(:disabled) {
  transform: translateY(-1px);
}

button.ghost {
  background: white;
  color: #4f46e5;
  border: 1px solid #e5e7eb;
  box-shadow: none;
}

form {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: flex-end;
  margin: 8px 0 12px;
}

.inline-forms {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.inline {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 6px;
}

label {
  display: flex;
  flex-direction: column;
  font-size: 14px;
  color: #111827;
  gap: 6px;
}

input {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #d5d8e1;
  min-width: 180px;
  font-size: 14px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 18px;
}

.panel {
  padding: 10px 4px;
  border-left: 3px solid #e5e7eb;
}

.list-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
  margin-top: 10px;
}

.two-cols {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.stat {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  padding: 12px;
  border-radius: 12px;
}

.stat .label {
  margin: 0;
  color: #6b7280;
}

.stat .value {
  margin: 4px 0 0;
  font-weight: 800;
  font-size: 26px;
}

.recommendations {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.recommendation {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px 14px;
  background: #f8fafc;
}

.recommendation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.score {
  background: #fff;
  border: 1px solid #c7d2fe;
  color: #4338ca;
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 800;
}

.recommendation ul {
  padding-left: 18px;
  margin: 10px 0 0;
  color: #1f2937;
}

.empty {
  margin: 12px 0 0;
}

.error {
  color: #b91c1c;
  font-weight: 700;
  margin-top: 16px;
}

.loading {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
}

.loading .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #6366f1;
  animation: pulse 1.2s infinite ease-in-out;
}

.loading .dot:nth-child(2) { animation-delay: 0.15s; }
.loading .dot:nth-child(3) { animation-delay: 0.3s; }

@keyframes pulse {
  0%, 80%, 100% { opacity: 0.2; transform: scale(0.9); }
  40% { opacity: 1; transform: scale(1); }
}
</style>
