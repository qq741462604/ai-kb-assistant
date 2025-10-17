<template>
  <div>
    <div class="topbar">
      <span>待审核: {{ stats.totalPending }}</span>
      <span>已人工通过: {{ stats.totalApproved }}</span>
      <span>已驳回: {{ stats.totalRejected }}</span>
      <span>自动通过: {{ stats.totalAutoApproved }}</span>
      <button @click="triggerAutoReview">触发自动审核</button>
    </div>

    <!-- 这里是你现有的列表/表格 -->
  </div>

  <div>
    <h1 class="text-xl font-bold mb-4">KB Pending 管理系统</h1>

    <!-- ✅ 自动审核按钮 -->
    <button @click="autoApprove" class="bg-purple-500 text-white px-4 py-2 rounded mr-4">
      自动审核（高于阈值）
    </button>

    <!-- 原来的 批量通过 / 批量驳回 按钮 -->
    <button @click="batchApprove" class="bg-green-500 text-white px-4 py-2 rounded mr-2">
      批量通过
    </button>
    <button @click="batchReject" class="bg-red-500 text-white px-4 py-2 rounded">
      批量驳回
    </button>

    <!-- 表格 -->
    <table class="mt-4 w-full border-collapse border border-gray-300">
      ...
    </table>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const BASE_URL = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';
const stats = ref({ totalPending: 0, totalApproved: 0, totalRejected: 0, totalAutoApproved: 0 });
const pageNum = ref(1);
const pageSize = ref(5);
const total = ref(0);
const pendingList = ref([]);
async function loadStats() {
  try {
    const res = await axios.get(`${BASE_URL}/field/pending/stats`);
    Object.assign(stats.value, res.data);
  } catch (e) {
    console.error(e);
  }
}

const   fetchPending = async () => {
  try {
    const res = await axios.get(`${BASE_URL}/field/pending/page`, {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    });
    pendingList.value = res.data.data;   // 当前页数据
    total.value = res.data.total;        // 总条数
  } catch (err) {
    console.error(err);
    alert('加载失败');
  }
};
async function triggerAutoReview() {
  try {
    const res = await axios.post(`${BASE_URL}/field/pending/autoReview?topN=50`);
    alert('自动通过数量: ' + res.data);
    await loadStats();
    // 刷新列表
    await fetchPending(); // 你原来的 fetchPending
  } catch (e) {
    console.error(e);
    alert('触发失败');
  }
}

async function autoApprove() {
  if (!confirm("确认根据高置信度自动审核吗？")) {
    return;
  }
  try {
    const res = await fetch(`${BASE_URL}/field/pending/autoApprove`, {
      method: "POST",
    });

    const text = await res.text();
    alert(text || "自动审核完成");
    await fetchPending(); // 取代 fetchPending()
  } catch (err) {
    alert("自动审核失败，请检查后端日志");
  }

}
onMounted(() => {
  loadStats();
});



</script>
