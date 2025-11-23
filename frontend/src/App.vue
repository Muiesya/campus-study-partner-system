<template>
  <main class="page">
    <header>
      <h1>校园学习伙伴推荐演示</h1>
      <p>使用 Java Spring Boot 后端 + Vue 前端快速体验学生、课程、兴趣及合作记录。</p>
    </header>

    <section class="card">
      <h2>创建学生</h2>
      <form @submit.prevent="createStudent">
        <label>姓名 <input v-model="newStudent.name" required /></label>
        <label>专业 <input v-model="newStudent.major" required /></label>
        <button type="submit">创建</button>
      </form>
      <p v-if="students.length">学生列表：
        <span v-for="s in students" :key="s.id" class="pill">{{ s.id }} - {{ s.name }} ({{ s.major }})</span>
      </p>
    </section>

    <section class="card">
      <h2>创建课程 / 标签</h2>
      <div class="grid">
        <form @submit.prevent="createCourse">
          <label>课程名 <input v-model="newCourse.name" required /></label>
          <label>简介 <input v-model="newCourse.description" /></label>
          <button type="submit">新增课程</button>
        </form>
        <form @submit.prevent="createTag">
          <label>兴趣标签 <input v-model="newTag.name" required /></label>
          <button type="submit">新增标签</button>
        </form>
      </div>
      <div class="stacked">
        <div v-if="courses.length">课程：<span v-for="c in courses" :key="c.id" class="pill">{{ c.id }} - {{ c.name }}</span></div>
        <div v-if="tags.length">标签：<span v-for="t in tags" :key="t.id" class="pill">{{ t.id }} - {{ t.name }}</span></div>
      </div>
    </section>

    <section class="card">
      <h2>绑定课程 / 标签</h2>
      <form @submit.prevent="bindCourse">
        <label>学生ID <input v-model.number="bind.course.studentId" required type="number" /></label>
        <label>课程ID <input v-model.number="bind.course.courseId" required type="number" /></label>
        <button type="submit">绑定课程</button>
      </form>
      <form @submit.prevent="bindTag">
        <label>学生ID <input v-model.number="bind.tag.studentId" required type="number" /></label>
        <label>标签ID <input v-model.number="bind.tag.tagId" required type="number" /></label>
        <button type="submit">绑定标签</button>
      </form>
    </section>

    <section class="card">
      <h2>记录合作</h2>
      <form @submit.prevent="recordCollaboration">
        <label>学生A ID <input v-model.number="collaboration.studentId1" required type="number" /></label>
        <label>学生B ID <input v-model.number="collaboration.studentId2" required type="number" /></label>
        <button type="submit">记录一次合作</button>
      </form>
    </section>

    <section class="card">
      <h2>获取推荐</h2>
      <form @submit.prevent="fetchRecommendations">
        <label>学生ID <input v-model.number="targetStudent" required type="number" /></label>
        <label>数量 <input v-model.number="limit" min="1" type="number" /></label>
        <button type="submit">查询</button>
      </form>
      <div v-if="recommendations.length">
        <article v-for="item in recommendations" :key="item.studentId" class="recommendation">
          <h3>{{ item.studentName }}（ID {{ item.studentId }}）</h3>
          <p>得分：{{ item.score.toFixed(2) }}</p>
          <ul>
            <li v-for="reason in item.reasons" :key="reason">{{ reason }}</li>
          </ul>
        </article>
      </div>
    </section>

    <p class="error" v-if="error">{{ error }}</p>
  </main>
</template>

<script setup>
import axios from 'axios';
import { onMounted, reactive, ref } from 'vue';

const client = axios.create({ baseURL: 'http://localhost:8080/api' });
const students = ref([]);
const courses = ref([]);
const tags = ref([]);
const recommendations = ref([]);
const error = ref('');

const newStudent = reactive({ name: '', major: '' });
const newCourse = reactive({ name: '', description: '' });
const newTag = reactive({ name: '' });
const bind = reactive({ course: { studentId: null, courseId: null }, tag: { studentId: null, tagId: null } });
const collaboration = reactive({ studentId1: null, studentId2: null });
const targetStudent = ref(null);
const limit = ref(5);

const resetError = () => (error.value = '');

const loadBasics = async () => {
  try {
    const [s, c, t] = await Promise.all([
      client.get('/students'),
      client.get('/courses'),
      client.get('/tags')
    ]);
    students.value = s.data;
    courses.value = c.data;
    tags.value = t.data;
  } catch (e) {
    error.value = '无法加载基础数据，请确认后端已启动。';
  }
};

const createStudent = async () => {
  resetError();
  await client.post('/students', newStudent);
  newStudent.name = '';
  newStudent.major = '';
  await loadBasics();
};

const createCourse = async () => {
  resetError();
  await client.post('/courses', newCourse);
  newCourse.name = '';
  newCourse.description = '';
  await loadBasics();
};

const createTag = async () => {
  resetError();
  await client.post('/tags', newTag);
  newTag.name = '';
  await loadBasics();
};

const bindCourse = async () => {
  resetError();
  await client.post(`/students/${bind.course.studentId}/courses/${bind.course.courseId}`);
};

const bindTag = async () => {
  resetError();
  await client.post(`/students/${bind.tag.studentId}/tags/${bind.tag.tagId}`);
};

const recordCollaboration = async () => {
  resetError();
  await client.post('/collaborations', collaboration);
};

const fetchRecommendations = async () => {
  resetError();
  const { data } = await client.get(`/students/${targetStudent.value}/recommendations`, { params: { limit: limit.value } });
  recommendations.value = data;
};

onMounted(loadBasics);
</script>

<style scoped>
:global(body) {
  font-family: "Inter", system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  background: #f5f7fb;
  margin: 0;
}

.page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

header {
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.04);
}

.card {
  background: white;
  padding: 16px 20px;
  margin-top: 18px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.04);
}

form {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

label {
  display: flex;
  flex-direction: column;
  font-size: 14px;
  color: #333;
}

input, button {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #d5d8e1;
}

button {
  background: #2563eb;
  color: white;
  cursor: pointer;
  border: none;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 12px;
}

.stacked { margin-top: 12px; }
.pill {
  display: inline-block;
  background: #eef2ff;
  color: #312e81;
  padding: 4px 8px;
  border-radius: 999px;
  margin: 4px 6px 0 0;
  font-size: 13px;
}

.recommendation {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  margin-top: 10px;
}

.error { color: #b91c1c; font-weight: 600; }
</style>
