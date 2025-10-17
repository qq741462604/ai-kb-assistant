<template>
  <div>
    <h2>Pending 字段列表</h2>

    <table border="1" cellspacing="0" cellpadding="8">
      <thead>
      <tr>
        <th>ID</th>
        <th>原始字段</th>
        <th>AI 推断字段</th>
        <th>描述</th>
        <th>别名</th>
        <th>置信度</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in pendingList" :key="item.id">
        <td>{{ item.id }}</td>
        <td>{{ item.originalField }}</td>
        <td>{{ item.aiCanonicalField }}</td>
        <td>{{ item.canonicalFieldDescription }}</td>
        <td>{{ item.aliases.join(', ') }}</td>
        <td>{{ item.confidence }}</td>
        <td>
          <button @click="approve(item)">通过</button>
          <button @click="reject(item)">拒绝</button>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- 分页控件 -->
    <div class="pagination">
      <button :disabled="pageNum === 1" @click="changePage(pageNum - 1)">上一页</button>
      <span>第 {{ pageNum }} 页 / 共 {{ Math.ceil(total / pageSize) }} 页</span>
      <button :disabled="pageNum >= Math.ceil(total / pageSize)" @click="changePage(pageNum + 1)">下一页</button>
    </div>

    <!-- 批量操作 -->
    <div style="margin-top:10px;">
      <button @click="batchApproveSelected">批量通过</button>
      <button @click="batchRejectSelected">批量驳回</button>
    </div>

    <!-- 编辑并通过 Modal -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <h3>编辑并通过</h3>
        <label>AI 规范字段：
          <input v-model="editItem.aiCanonicalField" />
        </label>
        <label>描述：
          <input v-model="editItem.canonicalFieldDescription" />
        </label>
        <label>别名（逗号分隔）：
          <input v-model="aliasesString" />
        </label>
        <div class="modal-actions">
          <button @click="confirmApprove">保存并通过</button>
          <button @click="cancelModal">取消</button>
        </div>
      </div>
    </div>
  </div>


</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import axios from 'axios';

const BASE_URL = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';

const total = ref(0);

const fullList = ref([]);
const pendingList = ref([]);
const pageNum = ref(1);
const pageSize = ref(5);
const totalPages = ref(1);

const selectedIds = ref([]);
const allSelected = ref(false);

const showModal = ref(false);
const editItem = ref({});
const aliasesString = ref('');


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

// 翻页
const changePage = (newPage) => {
  pageNum.value = newPage;
  fetchPending();
};


const updatePagedList = () => {
  const start = (pageNum.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  pendingList.value = fullList.value.slice(start, end);
};

const pagedList = computed(() => pendingList.value);


// 单条操作
const approve = async (item) => {
  try {
    item.status = 'APPROVED';
    await axios.post(`${BASE_URL}/field/pending/approve`, item);
    fetchPending(); // 刷新列表
  } catch (err) {
    console.error(err);
    alert('操作失败');
  }
};

const reject = async (item) => {
  try {
    item.status = 'REJECTED';
    await axios.post(`${BASE_URL}/field/pending/reject`, item);
    fetchPending(); // 刷新列表
  } catch (err) {
    console.error(err);
    alert('操作失败');
  }
};

// 批量操作
const batchApproveSelected = async () => {
  if (!selectedIds.value.length) { alert('请选择记录'); return; }
  try {
    await axios.post(`${BASE_URL}/field/pending/batchApprove`, selectedIds.value);
    alert('批量通过成功');
    selectedIds.value = [];
    allSelected.value = false;
    fetchPending();
  } catch (err) {
    console.error(err);
    alert('操作失败');
  }
};

const batchRejectSelected = async () => {
  if (!selectedIds.value.length) { alert('请选择记录'); return; }
  try {
    await axios.post(`${BASE_URL}/field/pending/batchReject`, selectedIds.value);
    alert('批量驳回成功');
    selectedIds.value = [];
    allSelected.value = false;
    fetchPending();
  } catch (err) {
    console.error(err);
    alert('操作失败');
  }
};

// 全选 / 取消全选
const toggleAll = () => {
  if (allSelected.value) {
    selectedIds.value = pagedList.value.map(i => i.id);
  } else {
    selectedIds.value = [];
  }
};

watch(pagedList, () => {
  if (!allSelected.value) selectedIds.value = [];
});

// Modal 相关
const openApproveModal = (item) => {
  editItem.value = { ...item };
  aliasesString.value = item.aliases.join(',');
  showModal.value = true;
};
const cancelModal = () => { showModal.value = false; };
const confirmApprove = async () => {
  try {
    // 更新别名列表
    editItem.value.aliases = aliasesString.value
        .split(',')
        .map(s => s.trim())
        .filter(Boolean);

    // 设置状态和更新时间
    editItem.value.status = 'APPROVED';
    editItem.value.updateTime = new Date();

    // 直接传整个对象给后端
    await axios.post(`${BASE_URL}/field/pending/approve`, editItem.value);

    alert('通过成功');
    showModal.value = false;
    fetchPending(); // 刷新列表
  } catch (err) {
    console.error(err);
    alert('操作失败');
  }
};

onMounted(() => { fetchPending(); });
</script>

<style scoped>
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 8px; }
th { background-color: #f2f2f2; }
.modal-overlay {
  position: fixed; top:0; left:0; right:0; bottom:0;
  background: rgba(0,0,0,0.4);
  display:flex; justify-content:center; align-items:center;
}
.modal-content {
  background:white; padding:20px; border-radius:5px; width:400px;
}
.modal-actions { margin-top:10px; display:flex; justify-content:space-between; }
</style>
