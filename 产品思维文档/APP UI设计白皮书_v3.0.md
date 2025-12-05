# 邢浩哲 - DreamAIChat 界面设计白皮书 - v3.0

> 版本时间：2025-12-03  
> 说明：本版本严格按照 **当前 App 实际界面** 与交互实现编写，替换 v2.0 的过期描述。

---

## 界面总览

```
LoginScreen (登录)
    ↓ （认证成功后持久化 Token）
MainActivity (容器)
├── ChatFragment       # 聊天页（默认）
├── HistoryFragment    # 历史页
└── ProfileFragment    # 我的页

Drawer / Sidebar
├── TemplateSelectionFragment
├── ConversationGraphFragment
├── ProfileFragment（快捷入口）
└── Logout Action

ProfileFragment 内嵌
└── AccountSettingsFragment（编辑资料）
```

*说明：抽屉入口与“我的”页中的“对话模板 / 对话图谱”按钮指向同一套 Fragment，保持体验一致；“智能路由”功能已经下线。*

---



## 界面一：LoginScreen - 登录

**功能**：账号登录 / 注册入口。

**布局模块**  
- a. **品牌展示区**  
  - 中央 48sp “Dream” 文字 + 副标题 `@string/app_subtitle`（“AI对话流创意工具”）  
  - 纯色渐变背景 + 白色文字
- b. **登录表单**  
  - `TextInputLayout`（FilledBox）收集手机号/邮箱 & 密码  
  - 右下角“登录”主按钮 + “注册账号”文字按钮  
  - 输入通过 `LoginViewModel`（MVI）处理，成功后保存 token 并跳转 Chat
- c. **底部提示**  
  - 文本：“登录即代表同意《用户协议》和《隐私政策》”

---



## 界面二：MainActivity - 根容器

**功能**：托管 Toolbar + Drawer + BottomNavigation + Fragment 容器。

**组成**  
- a. **Toolbar**  
  - 左：`☰` 图标 → 打开 Drawer  
  - 中：应用标题（“Dream”）  
  - 右：两个 `ImageButton` 样式的菜单项（新对话、设置），直接复用 `toolbar_menu.xml`
- b. **Drawer (NavigationView)**  
  - “对话模板”“对话图谱”“设置”“退出登录”四个条目
- c. **BottomNavigation**  
  - 三个 Tab：`Chat`, `History`, `Profile`  
  - 切换时通过 `showFragment()` 替换 `fragment_container`

---



## 界面三：ChatFragment - 聊天页

**功能**：AI 对话主场景。

**布局层级**  
1. **模型信息头（model_header）**  
   - 左：彩色圆点 `View`（显示当前模型）  
   - 中：模型名称（如 “DeepSeek · 精准”）  
   - 右：`OutlinedButton` 打开模型选择对话框
2. **生成提示条（generatingBar）**  
   - 当 `viewModel.isGenerating = true` 时显示“Dream 正在生成” + ProgressBar
3. **消息列表（rvMessages）**  
   - `RecyclerView + ChatMessageAdapter`  
   - 支持用户/AI/系统三种气泡；AI 气泡底部有“复制”小按钮；支持多图缩略图
4. **快捷区域（action_container）**  
   - 图片附件预览 `HorizontalScrollView`（可移除单张）  
   - `ChipGroup` 承载 `QuickAction`（如“写周报”“润色”等）
5. **输入区（input_container）**  
   - `TextInputLayout` 多行输入框，错误提示直接显示在控件内  
   - **图片按钮**：`ImageButton`，背景为 `?attr/selectableItemBackgroundBorderless`，`padding=12dp`，图标 `ic_attach`  
   - **发送按钮**：同样是 `ImageButton`，图标 `ic_send`，着色 `@color/primary`  
   - 点击图片 → 调用系统文件选择器（多选）；点击发送 → 调用 `ChatViewModel.sendMessage`

**交互要点**  
- 选中多图后输入框上方出现缩略图，必须手动再点“发送”才会真正提交  
- 模型切换 / 新对话都通过 ViewModel 更新 LiveData，UI 自动滚动到底部  
- 每次模板、图谱返回时会把长文本放入输入框而不是直接发送，让用户确认

---



## 界面四：HistoryFragment - 历史记录

**功能**：快速检索过往会话。

**布局模块**  
- 顶部 `TextInputLayout` + 清除按钮（搜索）  
- 中部 `RecyclerView`，列表项包含标题、摘要、时间，点击后跳转 Chat 并加载对应会话  
- `ProgressBar` 在搜索期间显示  
- **EmptyState**：emoji + 提示文案 + “开始对话”按钮（返回 Chat）

---



## 界面五：ProfileFragment - 我的

**功能**：展示个人信息、使用统计、入口集合。

**模块**  
- a. **用户卡片**  
  - 圆形 `ImageView`（默认 `ic_user_avatar`，可按部署指南替换）  
  - 昵称 `tv_username`（登录 + 编辑资料后实时刷新）  
  - “编辑资料”按钮 → `AccountSettingsFragment`
- b. **数据统计卡片**  
  - Grid 3 列：对话数 / 消息数 / 使用天数  
  - 数据来自 Room（`ConversationDao` + `MessageDao`）和 `SessionManager`
- c. **特色入口卡片**  
  - 两个按钮：`对话模板`、`对话图谱`（图标来自 `ic_nav_templates / ic_nav_graph`）  
  - 点击后 `FragmentTransaction` 跳入各自的 Fragment，请求栈入栈

---



## 界面六：AccountSettingsFragment - 编辑资料

**功能**：修改昵称 & 注销账号。

**布局**  
- `MaterialToolbar`（左：返回箭头，右：标题）  
- 信息区：账号显示（只读）、`TextInputLayout` 输入昵称  
- 操作区：  
  - “保存”按钮：校验非空 → `SessionManager.setDisplayName()` → Toast + 返回  
  - “注销账号”文字按钮：`MaterialAlertDialog` 确认，清除 Session → 跳转 LoginActivity

---



## 界面七：TemplateSelectionFragment - 对话模板

**功能**：快速把预设提示词填入聊天输入框。

**布局**  
- `MaterialToolbar`（返回 + “对话模板”标题）  
- `RecyclerView` 采用 2 列 `GridLayout`，卡片使用 `MaterialCardView`  
- 每张卡片包含：图标（`ic_template_*`）、标题、描述  
- 点击卡片 → 通过 `ChatViewModel.applyQuickPrompt()` 回填输入框 → 自动 `onBackPressed` 回到 Chat

---



## 界面八：ConversationGraphFragment - 对话图谱（V3）

**功能**：从历史会话生成“总结 + 流程图提示”。

**实现差异（相对 v2.0）**  
- 不再渲染真实图谱，而是复用历史列表 + 文本预处理：  
  1. 请求 `HistoryViewModel` 的数据  
  2. 点击某条记录 → 读取对应 Room 消息 → 拼接成  
     ```
     用户：...
     AI：...
     ```
  3. 再附上一段“请根据以上内容生成总结 + 流程图描述”的提示语  
  4. 通过 `pendingPrompt` 回填到 Chat 输入框，等待用户手动发送
- UI：Toolbar + `RecyclerView` + 空态，与历史列表保持一致。

---



## 交互与状态说明

1. **多图发送流程**：从 Chat 的图片按钮调起系统文件选择 → 选中若干张后在输入框上方展示缩略图 → 可以删除单张 → 点击发送才会真正上传。  
2. **模板 / 图谱与聊天联动**：两者都会通过 `ChatViewModel` 的 `pendingPrompt` 机制预填输入框，确保用户能二次确认。  
3. **Drawer 与 “我的”页入口统一**：`对话模板/对话图谱/设置/退出登录` 无论从 Drawer 还是 Profile 卡片进入，行为完全一致。  
4. **联网/演示模式提示**：如果后端未配置 API Key，`ModelRouterService` 会返回“【演示模式】”提示；需要结合部署指南 §14 的诊断接口排查。

---



## 差异对比（v2.0 → v3.0）

| 项目 | v2.0 设计 | v3.0 实际实现 |
| --- | --- | --- |
| 模型路由界面 | 独立 Fragment + 多种模式 | 功能下线，仅在 Profile 留入口说明 |
| 对话图谱 | Canvas 图谱 + 节点面板 | 改成“历史列表 + 文本拼接 + 回填聊天框” |
| 输入按钮 | MaterialButton（带背景） | 无背景 `ImageButton`，与 Toolbar 图标统一 |
| 模板详情 | 需要浮层 | 直接回填到聊天框，不再弹出二级面板 |
| Profile 功能 | “免费版/升级/收藏” | 精简为昵称 + 统计卡片 + 模板/图谱入口 |

