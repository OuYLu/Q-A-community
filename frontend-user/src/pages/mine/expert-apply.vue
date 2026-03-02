<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { ensurePageAuth } from "@/utils/auth-guard";
import { useAuthStore } from "@/stores/auth";
import { expertApi, type ExpertProofFileDTO } from "@/api/expert";
import { BASE_URL } from "@/utils/constants";

type ProofType = "license" | "employment" | "title" | "education" | "other";

const authStore = useAuthStore();
const realName = ref("");
const organization = ref("");
const title = ref("");
const expertise = ref("");

const licenseFiles = ref<ExpertProofFileDTO[]>([]);
const employmentFiles = ref<ExpertProofFileDTO[]>([]);
const titleFiles = ref<ExpertProofFileDTO[]>([]);
const educationFiles = ref<ExpertProofFileDTO[]>([]);
const otherFiles = ref<ExpertProofFileDTO[]>([]);

const submitting = ref(false);
const uploading = ref(false);

const canSubmit = computed(() => {
  return (
    !!realName.value.trim() &&
    !!expertise.value.trim() &&
    licenseFiles.value.length > 0 &&
    employmentFiles.value.length > 0 &&
    !submitting.value
  );
});

onShow(() => {
  ensurePageAuth();
});

function filesByType(type: ProofType) {
  if (type === "license") return licenseFiles.value;
  if (type === "employment") return employmentFiles.value;
  if (type === "title") return titleFiles.value;
  if (type === "education") return educationFiles.value;
  return otherFiles.value;
}

async function pickProof(type: ProofType) {
  const current = filesByType(type);
  const remain = 9 - current.length;
  if (remain <= 0) {
    uni.showToast({ title: "最多上传9张", icon: "none" });
    return;
  }
  try {
    const choose = await uni.chooseImage({
      count: remain,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"]
    });
    const paths = choose.tempFilePaths || [];
    if (!paths.length) return;
    uploading.value = true;
    for (const filePath of paths) {
      const file = await uploadProof(filePath);
      current.push(file);
      if (current.length >= 9) break;
    }
    uni.showToast({ title: "上传成功", icon: "success" });
  } catch (err: any) {
    uni.showToast({ title: err?.message || "上传失败", icon: "none" });
  } finally {
    uploading.value = false;
  }
}

function removeProof(type: ProofType, index: number) {
  const current = filesByType(type);
  current.splice(index, 1);
}

async function uploadProof(filePath: string): Promise<ExpertProofFileDTO> {
  const upload = await uni.uploadFile({
    url: `${BASE_URL}/api/common/upload?bizType=expert-proof`,
    filePath,
    name: "file",
    header: authStore.token
      ? {
          Authorization: `Bearer ${authStore.token}`
        }
      : undefined
  });
  if (upload.statusCode < 200 || upload.statusCode >= 300) {
    throw new Error(`HTTP ${upload.statusCode}`);
  }
  const body = JSON.parse(upload.data || "{}");
  const data = body?.data || {};
  const url = String(data.url || "");
  if (!url) {
    throw new Error(body?.message || body?.msg || "上传失败");
  }
  const fullUrl =
    url.startsWith("http://") || url.startsWith("https://")
      ? url
      : url.startsWith("/")
      ? `${BASE_URL}${url}`
      : `${BASE_URL}/${url}`;
  return {
    url: fullUrl,
    name: String(data.originalName || data.filename || "proof"),
    size: Number(data.size || 0),
    mime: String(data.contentType || "image/jpeg")
  };
}

async function submit() {
  if (!canSubmit.value) {
    uni.showToast({ title: "请补齐必填项", icon: "none" });
    return;
  }
  submitting.value = true;
  try {
    await expertApi.apply({
      realName: realName.value.trim(),
      organization: organization.value.trim() || undefined,
      title: title.value.trim() || undefined,
      expertise: expertise.value.trim(),
      proofUrls: {
        LICENSE: licenseFiles.value,
        EMPLOYMENT: employmentFiles.value,
        TITLE: titleFiles.value.length ? titleFiles.value : undefined,
        EDUCATION: educationFiles.value.length ? educationFiles.value : undefined,
        OTHER: otherFiles.value.length ? otherFiles.value : undefined
      }
    });
    uni.showToast({ title: "申请已提交", icon: "success" });
    setTimeout(() => {
      uni.navigateBack();
    }, 280);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "提交失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <view class="page">
    <view class="block">
      <view class="label">真实姓名 *</view>
      <input v-model="realName" class="input" maxlength="30" placeholder="请输入真实姓名" />

      <view class="label">机构</view>
      <input v-model="organization" class="input" maxlength="60" placeholder="如：某某医院/机构" />

      <view class="label">职称</view>
      <input v-model="title" class="input" maxlength="60" placeholder="如：主治医师/营养师" />

      <view class="label">擅长领域 *</view>
      <input v-model="expertise" class="input" maxlength="100" placeholder="如：睡眠健康、慢病管理" />
    </view>

    <view class="block">
      <view class="row">
        <view class="label mb0">执业资质 *</view>
        <view class="tip">至少1张</view>
      </view>
      <view class="images-wrap">
        <view v-for="(item, idx) in licenseFiles" :key="item.url + idx" class="img-item">
          <image class="img" :src="item.url" mode="aspectFill" />
          <view class="img-del" @click="removeProof('license', idx)">×</view>
        </view>
        <view class="img-uploader" @click="pickProof('license')">
          <text>{{ uploading ? "上传中..." : "+ 上传" }}</text>
        </view>
      </view>
    </view>

    <view class="block">
      <view class="row">
        <view class="label mb0">在职证明 *</view>
        <view class="tip">至少1张</view>
      </view>
      <view class="images-wrap">
        <view v-for="(item, idx) in employmentFiles" :key="item.url + idx" class="img-item">
          <image class="img" :src="item.url" mode="aspectFill" />
          <view class="img-del" @click="removeProof('employment', idx)">×</view>
        </view>
        <view class="img-uploader" @click="pickProof('employment')">
          <text>{{ uploading ? "上传中..." : "+ 上传" }}</text>
        </view>
      </view>
    </view>

    <view class="block">
      <view class="row">
        <view class="label mb0">职称证明</view>
        <view class="tip">非必填</view>
      </view>
      <view class="images-wrap">
        <view v-for="(item, idx) in titleFiles" :key="item.url + idx" class="img-item">
          <image class="img" :src="item.url" mode="aspectFill" />
          <view class="img-del" @click="removeProof('title', idx)">×</view>
        </view>
        <view class="img-uploader" @click="pickProof('title')">
          <text>{{ uploading ? "上传中..." : "+ 上传" }}</text>
        </view>
      </view>
    </view>

    <view class="block">
      <view class="row">
        <view class="label mb0">学历证明</view>
        <view class="tip">非必填</view>
      </view>
      <view class="images-wrap">
        <view v-for="(item, idx) in educationFiles" :key="item.url + idx" class="img-item">
          <image class="img" :src="item.url" mode="aspectFill" />
          <view class="img-del" @click="removeProof('education', idx)">×</view>
        </view>
        <view class="img-uploader" @click="pickProof('education')">
          <text>{{ uploading ? "上传中..." : "+ 上传" }}</text>
        </view>
      </view>
    </view>

    <view class="block">
      <view class="row">
        <view class="label mb0">其他材料</view>
        <view class="tip">非必填</view>
      </view>
      <view class="images-wrap">
        <view v-for="(item, idx) in otherFiles" :key="item.url + idx" class="img-item">
          <image class="img" :src="item.url" mode="aspectFill" />
          <view class="img-del" @click="removeProof('other', idx)">×</view>
        </view>
        <view class="img-uploader" @click="pickProof('other')">
          <text>{{ uploading ? "上传中..." : "+ 上传" }}</text>
        </view>
      </view>
    </view>

    <button class="submit" :class="{ disabled: !canSubmit }" @click="submit">
      {{ submitting ? "提交中..." : "提交申请" }}
    </button>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 20rpx;
  box-sizing: border-box;
}

.block {
  background: #fff;
  border: 2rpx solid #eadfbf;
  border-radius: 20rpx;
  padding: 18rpx;
  margin-bottom: 16rpx;
}

.label {
  margin: 12rpx 0 10rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #3f5871;
}

.mb0 {
  margin: 0;
}

.input {
  height: 76rpx;
  border: 2rpx solid #d8e8f8;
  border-radius: 14rpx;
  padding: 0 18rpx;
  background: #fdfdfb;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tip {
  color: #8ba0b3;
  font-size: 24rpx;
}

.images-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 10rpx;
}

.img-item {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  overflow: hidden;
  border: 2rpx solid #eadfbf;
  position: relative;
}

.img {
  width: 100%;
  height: 100%;
}

.img-del {
  position: absolute;
  right: 4rpx;
  top: 4rpx;
  width: 30rpx;
  height: 30rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  text-align: center;
  line-height: 30rpx;
  font-size: 20rpx;
}

.img-uploader {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  border: 2rpx dashed #d6cba8;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7d8f9f;
  font-size: 24rpx;
  background: #fffdf7;
}

.submit {
  margin-top: 24rpx;
  border: none;
  border-radius: 18rpx;
  background: #4ba7d9;
  color: #fff;
}

.submit.disabled {
  opacity: 0.65;
}
</style>

