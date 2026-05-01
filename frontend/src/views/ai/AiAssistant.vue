<template>
  <div class="ai-assistant">
    <el-card shadow="hover" class="chat-card">
      <div slot="header" class="card-header">
        <span class="card-header-title">
          <i class="el-icon-magic-stick"></i>
          智慧渔业助手
        </span>
        <el-button type="text" icon="el-icon-delete" @click="clearChat">清空对话</el-button>
      </div>
      
      <div class="quick-actions">
        <el-tag type="info" @click="analyzeDashboard" class="action-tag">
          <i class="el-icon-data-analysis"></i>
          分析仪表盘
        </el-tag>
        <el-tag type="success" @click="analyzeWaterQuality" class="action-tag">
          <i class="el-icon-water"></i>
          分析水质
        </el-tag>
        <el-tag type="warning" @click="analyzeAlerts" class="action-tag">
          <i class="el-icon-bell"></i>
          分析告警
        </el-tag>
        <el-tag type="danger" @click="analyzeFeeding" class="action-tag">
          <i class="el-icon-food"></i>
          分析投喂
        </el-tag>
        <el-tag type="primary" @click="analyzeDisease" class="action-tag">
          <i class="el-icon-s-tools"></i>
          分析病害
        </el-tag>
        <el-tag type="primary" @click="analyzeWeather" class="action-tag">
          <i class="el-icon-sunny"></i>
          分析天气
        </el-tag>
        <el-tag type="primary" @click="analyzeCage" class="action-tag">
          <i class="el-icon-s-grid"></i>
          分析网箱
        </el-tag>
        <el-tag type="primary" @click="analyzeStock" class="action-tag">
          <i class="el-icon-s-goods"></i>
          分析库存
        </el-tag>
        <el-tag type="primary" @click="generateReport" class="action-tag">
          <i class="el-icon-document"></i>
          综合报告
        </el-tag>
      </div>
      
      <div class="quick-suggests">
        <span class="suggest-label">快速建议：</span>
        <el-button size="mini" type="text" @click="getSuggest('water')">水质管理</el-button>
        <el-button size="mini" type="text" @click="getSuggest('feeding')">投喂管理</el-button>
        <el-button size="mini" type="text" @click="getSuggest('disease')">病害防治</el-button>
        <el-button size="mini" type="text" @click="getSuggest('emergency')">应急处理</el-button>
        <el-button size="mini" type="text" @click="getSuggest('general')">综合建议</el-button>
      </div>
      
      <div class="chat-container" ref="chatContainer">
        <div class="message-list">
          <div 
            v-for="(msg, index) in messages" 
            :key="index" 
            :class="['message', msg.role]"
          >
            <div class="message-avatar">
              <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-s-custom'"></i>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="msg.content"></div>
              <div class="message-actions">
                <el-button 
                  type="text" 
                  size="mini" 
                  icon="el-icon-delete" 
                  @click="deleteMessage(index)"
                  title="删除这条消息"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
          
          <div v-if="loading" class="message assistant">
            <div class="message-avatar">
              <i class="el-icon-s-custom"></i>
            </div>
            <div class="message-content">
              <div class="message-text typing">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="请输入您的问题..."
          @keyup.enter.ctrl="sendMessage"
          :disabled="loading"
        ></el-input>
        <div class="input-actions">
          <span class="hint">Ctrl + Enter 发送</span>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="sendMessage"
            icon="el-icon-s-promotion"
          >
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { chat, analyzeDashboard, analyzeWaterQuality, analyzeAlerts, analyzeFeeding, smartSuggest, analyzeDisease, analyzeWeather, analyzeCage, analyzeStock, generateComprehensiveReport } from '@/api/ai';

export default {
  name: 'AiAssistant',
  data() {
    return {
      messages: [],
      inputMessage: '',
      loading: false,
      isLoadingFromStorage: false
    };
  },
  watch: {
    messages: {
      handler(newVal) {
        if (!this.isLoadingFromStorage) {
          this.saveToLocalStorage();
        }
      },
      deep: true
    }
  },
  created() {
    this.loadFromLocalStorage();
  },
  methods: {
    loadFromLocalStorage() {
      this.isLoadingFromStorage = true;
      try {
        const saved = localStorage.getItem('ai_chat_history');
        if (saved) {
          const parsed = JSON.parse(saved);
          if (Array.isArray(parsed) && parsed.length > 0) {
            this.messages = parsed;
          } else {
            this.initMessages();
          }
        } else {
          this.initMessages();
        }
      } catch (error) {
        console.error('加载聊天历史失败:', error);
        this.initMessages();
      } finally {
        this.isLoadingFromStorage = false;
      }
    },
    saveToLocalStorage() {
      try {
        localStorage.setItem('ai_chat_history', JSON.stringify(this.messages));
      } catch (error) {
        console.error('保存聊天历史失败:', error);
      }
    },
    initMessages() {
      this.messages = [
        {
          role: 'assistant',
          content: '您好！我是智慧渔业助手，有什么可以帮助您的吗？<br><br>您可以点击上方的快捷按钮让我分析系统数据，也可以直接提问关于渔业养殖、水质管理、病害防治等方面的问题。'
        }
      ];
    },
    async sendMessage() {
      if (!this.inputMessage.trim() || this.loading) return;
      
      const userMessage = this.inputMessage.trim();
      this.messages.push({
        role: 'user',
        content: userMessage
      });
      this.inputMessage = '';
      this.loading = true;
      
      this.$nextTick(() => {
        this.scrollToBottom();
      });
      
      try {
        const res = await chat(userMessage);
        if (res.code === 200 && res.data) {
          this.messages.push({
            role: 'assistant',
            content: this.formatMessage(res.data.content)
          });
        } else {
          this.$message.error(res.msg || '获取AI回复失败');
        }
      } catch (error) {
        console.error('Error:', error);
        this.$message.error('网络错误，请稍后重试');
      } finally {
        this.loading = false;
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    },
    
    async analyzeDashboard() {
      await this.executeAnalysis(analyzeDashboard, '正在分析系统数据...');
    },
    
    async analyzeWaterQuality() {
      await this.executeAnalysis(analyzeWaterQuality, '正在分析水质数据...');
    },
    
    async analyzeAlerts() {
      await this.executeAnalysis(analyzeAlerts, '正在分析告警数据...');
    },
    
    async analyzeFeeding() {
      await this.executeAnalysis(analyzeFeeding, '正在分析投喂数据...');
    },

    async analyzeDisease() {
      await this.executeAnalysis(analyzeDisease, '正在分析病害数据...');
    },

    async analyzeWeather() {
      await this.executeAnalysis(analyzeWeather, '正在分析天气数据...');
    },

    async analyzeCage() {
      await this.executeAnalysis(analyzeCage, '正在分析网箱数据...');
    },

    async analyzeStock() {
      await this.executeAnalysis(analyzeStock, '正在分析库存数据...');
    },

    async generateReport() {
      await this.executeAnalysis(generateComprehensiveReport, '正在生成综合报告...');
    },

    async getSuggest(type) {
      const typeNames = {
        'water': '水质管理',
        'feeding': '投喂管理',
        'disease': '病害防治',
        'emergency': '应急处理',
        'general': '综合建议'
      };
      
      const userMsg = `请给我一些${typeNames[type]}建议`;
      this.messages.push({
        role: 'user',
        content: userMsg
      });
      
      this.loading = true;
      this.$nextTick(() => {
        this.scrollToBottom();
      });
      
      try {
        const res = await smartSuggest(type);
        if (res.code === 200 && res.data) {
          this.messages.push({
            role: 'assistant',
            content: this.formatMessage(res.data.content)
          });
        } else {
          this.$message.error(res.msg || '获取建议失败');
        }
      } catch (error) {
        console.error('Error:', error);
        this.$message.error('网络错误，请稍后重试');
      } finally {
        this.loading = false;
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    },
    
    async executeAnalysis(apiFunc, userMsg) {
      this.messages.push({
        role: 'user',
        content: userMsg
      });
      
      this.loading = true;
      this.$nextTick(() => {
        this.scrollToBottom();
      });
      
      try {
        const res = await apiFunc();
        if (res.code === 200 && res.data) {
          this.messages.push({
            role: 'assistant',
            content: this.formatMessage(res.data.content)
          });
        } else {
          this.$message.error(res.msg || '分析失败');
        }
      } catch (error) {
        console.error('Error:', error);
        this.$message.error('网络错误，请稍后重试');
      } finally {
        this.loading = false;
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    },
    
    formatMessage(content) {
      if (!content) return '';
      return content
        .replace(/\n/g, '<br>')
        .replace(/(\d+\.)/g, '<br>$1');
    },
    
    deleteMessage(index) {
      this.$confirm('确定要删除这条消息吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.messages.splice(index, 1);
        this.$message.success('已删除');
      }).catch(() => {});
    },

    clearChat() {
      this.$confirm('确定要清空对话历史吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.messages = [
          {
            role: 'assistant',
            content: '您好！我是智慧渔业助手，有什么可以帮助您的吗？<br><br>您可以点击上方的快捷按钮让我分析系统数据，也可以直接提问关于渔业养殖、水质管理、病害防治等方面的问题。'
          }
        ];
        this.$message.success('已清空');
      }).catch(() => {});
    },
    
    scrollToBottom() {
      const container = this.$refs.chatContainer;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }
  }
};
</script>

<style scoped>
.ai-assistant {
  padding: 20px;
  height: calc(100vh - 84px);
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #fffafc 0%, #ffffff 100%);
}

.chat-card {
  width: 100%;
  max-width: 900px;
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  overflow: hidden;
  border: 2px solid rgba(255, 105, 180, 0.15);
}

.chat-card >>> .el-card__header {
  background: linear-gradient(135deg, #ff69b4 0%, #ff85c0 100%);
  color: white;
  padding: 16px 24px;
  border-bottom: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-title {
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header-title i {
  font-size: 20px;
}

.chat-card >>> .el-card__header .el-button {
  color: white;
}

.chat-card >>> .el-card__header .el-button:hover {
  background: rgba(255, 255, 255, 0.2);
}

.chat-card >>> .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.quick-actions {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff5f9 0%, #ffffff 100%);
  border-bottom: 1px solid rgba(255, 105, 180, 0.1);
  flex-wrap: wrap;
}

.action-tag {
  cursor: pointer;
  font-size: 14px;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 20px;
  transition: all 0.3s;
}

.action-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.quick-suggests {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid rgba(255, 105, 180, 0.08);
  flex-wrap: wrap;
}

.suggest-label {
  color: #666;
  font-size: 13px;
  white-space: nowrap;
}

.quick-suggests .el-button {
  color: #ff69b4;
}

.quick-suggests .el-button:hover {
  background: rgba(255, 105, 180, 0.1);
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fffafc;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.assistant {
  align-self: flex-start;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #ff69b4 0%, #ff85c0 100%);
  color: white;
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-content {
  flex: 1;
}

.message-text {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.8;
  word-wrap: break-word;
}

.message.user .message-text {
  background: linear-gradient(135deg, #ff69b4 0%, #ff85c0 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-text {
  background: white;
  color: #2d3436;
  border: 1px solid rgba(255, 105, 180, 0.15);
  border-bottom-left-radius: 4px;
}

.message-actions {
  margin-top: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message:hover .message-actions {
  opacity: 1;
}

.message-actions .el-button {
  padding: 2px 8px;
  font-size: 12px;
  color: #999;
}

.message-actions .el-button:hover {
  color: #ff69b4;
}

.message-text.typing {
  display: inline-flex;
  gap: 4px;
  padding: 12px 16px;
}

.message-text.typing span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff69b4;
  animation: typing 1.4s infinite ease-in-out;
}

.message-text.typing span:nth-child(1) {
  animation-delay: -0.32s;
}

.message-text.typing span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.input-area {
  padding: 20px;
  background: white;
  border-top: 1px solid rgba(255, 105, 180, 0.1);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.hint {
  color: #b2bec3;
  font-size: 12px;
}

/* 自定义滚动条 */
.chat-container::-webkit-scrollbar {
  width: 6px;
}

.chat-container::-webkit-scrollbar-track {
  background: #fffafc;
}

.chat-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #ff69b4 0%, #ff85c0 100%);
  border-radius: 3px;
}

.chat-container::-webkit-scrollbar-thumb:hover {
  background: #ff69b4;
}
</style>
