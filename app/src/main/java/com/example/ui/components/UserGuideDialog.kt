package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuideDialog(
    onDismissRequest: () -> Unit,
    onOpenAiSettings: () -> Unit = {},
    onOpenSmartAdd: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📖 默认数据说明", "🚀 软件快速上手", "⚙️ AI/API中转站教程", "📊 资产与功能指南", "🔐 商业多账户体系")

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("user_guide_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "使用说明书与操作指南",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "智能记账 · 资产管理 · AI中转站配置教程",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Tab Selector
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> DefaultDataExplanationSection(
                            onOpenSmartAdd = {
                                onDismissRequest()
                                onOpenSmartAdd()
                            }
                        )
                        1 -> QuickStartSection(
                            onOpenSmartAdd = {
                                onDismissRequest()
                                onOpenSmartAdd()
                            },
                            onOpenAiSettings = {
                                onDismissRequest()
                                onOpenAiSettings()
                            }
                        )
                        2 -> AiApiGuideSection(
                            onOpenAiSettings = {
                                onDismissRequest()
                                onOpenAiSettings()
                            }
                        )
                        3 -> FeaturesGuideSection()
                        4 -> MultiUserSystemGuideSection()
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismissRequest()
                            onOpenAiSettings()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("配置 AI 密钥")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = onDismissRequest) {
                        Text("我明白了")
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultDataExplanationSection(onOpenSmartAdd: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GuideCard(
            title = "1. 什么是默认程序数据？",
            icon = Icons.Default.Dataset,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Text(
                text = "为了方便用户首次打开软件时能够【开箱即用】，系统预置了一套初始演示数据。包括：",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            BulletItem("💵 预置资产账户：微信钱包（12,500元）、支付宝（8,800元）、招商银行（45,000元）、现金（800元）。")
            BulletItem("🏷️ 常用收支分类：餐饮美食、交通出行、日常购物、数码电子、理财收益、工资奖金等。")
            BulletItem("📈 示例投资与彩票：预置招商中证500基金、贵州茅台等投资样本及福利彩票数据。")
            BulletItem("🌐 预置 API 中转站配置：内置 Apimart (api.apimart.ai, api.apib.ai, api.aiuxu.com, api.aishuch.com) 及 英伟达 (integrate.api.nvidia.com) 等接口模板。")
        }

        GuideCard(
            title = "2. 我要如何添加与修改自己的真实数据？",
            icon = Icons.Default.AddCard,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ) {
            Text(
                text = "您可以通过以下多种简便方式添加您自己的真实账单和资产：",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            StepItem("方法一：✨ AI 极速语音/文本记账（最推荐）", "点击首页右上角的 ✨ 按钮或底部悬浮加号，输入如『今天早饭吃了15元微信支付』，AI 会自动识别金额、分类与账户并写入。")
            StepItem("方法二：➕ 手动记账与自定义分类", "点击首页右下角【+】号，输入金额、选择收支类型、支付账户及时间，支持添加文字备注与标签。")
            StepItem("方法三：🏦 净资产账户管理", "在『净资产概览』卡片中点击【+ 添加账户】，可新增您的银行卡、证券账户、债务等，并实时更新余额。")
            StepItem("方法四：📈 基金与理财记录", "在『基金与投资追踪』卡片中点击【+ 新增投资】，输入产品名称、持仓金额与买入成本。")

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onOpenSmartAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("立即体验 AI 极速记账添加数据")
            }
        }

        GuideCard(
            title = "3. 如何清除示例数据或导出备份？",
            icon = Icons.Default.CleaningServices,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ) {
            BulletItem("🧹 清除/恢复默认：在点击顶部【导出账单】按钮（📥图标）的弹窗中，支持重置初始化数据。")
            BulletItem("💾 导出 CSV 格式：您可以随时导出完整账单为 CSV 表格文件，使用 Excel 打开或备份。")
            BulletItem("🔄 离线自动保存：所有修改均实时保存于本地 Room 数据库，完全保护您的个人隐私。")
        }
    }
}

@Composable
private fun QuickStartSection(
    onOpenSmartAdd: () -> Unit,
    onOpenAiSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GuideCard(
            title = "第一步：配置 AI Key 或直接体验离线记账",
            icon = Icons.Default.VpnKey,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Text(
                text = "软件支持【无 API 模式】与【AI 智能模式】：",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            BulletItem("不需要 AI 时：所有记账、资产管理、汇率换算、统计图表均可完全离线正常运行。")
            BulletItem("开启 AI 功能时：点击顶部【⚙️ 设置】填入 API Key（如 Apimart, DeepSeek, Gemini 或 NVIDIA 密钥），开启自然语言理解与智能理财顾问。")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenAiSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("前往配置 AI 密钥")
            }
        }

        GuideCard(
            title = "第二步：极速记账的三种姿势",
            icon = Icons.Default.FlashOn,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ) {
            StepItem("1. 自然语言输入", "输入：『昨晚打车花了45元支付宝付款』，AI 会识别：金额 45元，分类 交通出行，账户 支付宝。")
            StepItem("2. 语音长按记账", "按住麦克风按钮直接说话，说出您的消费内容，放开后即可自动转文字并解析。")
            StepItem("3. 传统精准记账", "直接点击【+】号输入框，精准选择具体时间、标签和分类。")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onOpenSmartAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("打开 AI 极速记账")
            }
        }

        GuideCard(
            title = "第三步：查看分析报告与顾问建议",
            icon = Icons.Default.Analytics,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ) {
            BulletItem("📊 月度收支图表：自动计算本月预算使用比例，消费占比饼图与趋势图。")
            BulletItem("💡 AI 财务顾问：在『AI 财务智能顾问』卡片提问，如：『我本月餐饮花太多了吗？建议怎么理财？』")
            BulletItem("📄 月度分析报告：点击顶部【📊 报告】图标，生成完整的月度财务收支诊断报告。")
        }
    }
}

@Composable
private fun AiApiGuideSection(onOpenAiSettings: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GuideCard(
            title = "中转站 API 地址格式与后缀说明",
            icon = Icons.Default.Dns,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Text(
                text = "软件根据标准 OpenAI 协议要求，自动对域名与 Path 进行补全转换：",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            BulletItem("自动补全后缀：如填写『https://api.apimart.ai/v1』或『https://integrate.api.nvidia.com/v1』，系统发起请求时会自动路由至对应的 /chat/completions 节点。")
            BulletItem("已预置的中转站节点列表：")
            Text(
                text = "1. Apimart 中转站 1: https://api.apimart.ai/v1\n" +
                        "2. Apimart 中转站 2: https://api.apib.ai/v1\n" +
                        "3. Apimart 中转站 3: https://api.aiuxu.com/v1\n" +
                        "4. Apimart 中转站 4: https://api.aishuch.com/v1\n" +
                        "5. 英伟达 NVIDIA NIM: https://integrate.api.nvidia.com/v1\n" +
                        "6. DeepSeek 官方: https://api.deepseek.com/v1\n" +
                        "7. 硅基流动 SiliconFlow: https://api.siliconflow.cn/v1",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
            )
        }

        GuideCard(
            title = "如何配置与测试中转站 API Key",
            icon = Icons.Default.SettingsSuggest,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ) {
            StepItem("1. 打开配置窗口", "点击顶部工具栏的 ⚙️ 设置图标。")
            StepItem("2. 切换服务厂商", "在『服务厂商预设』下拉菜单中选择 Apimart 或 NVIDIA NIM，或选择自定义中转站。")
            StepItem("3. 输入 API 密钥", "粘贴您的 API Key，如 nvapi-xxxx 或 sk-xxxx。")
            StepItem("4. 自动/测试拉取模型", "点击【拉取可用模型】按钮，系统会连接中转站的 /models 接口并列出可用模型供您选择（如 gpt-4o, deepseek-chat, meta/llama-3.1-405b-instruct）。")
            StepItem("5. 本地缓存保护", "即便中转站暂时不可用，软件也会读取本地 Persistent Cache 缓存模型列表，确保使用不受阻。")

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onOpenAiSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("立即进入 AI 网络配置")
            }
        }
    }
}

@Composable
private fun FeaturesGuideSection() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GuideCard(
            title = "🏦 净资产与多币种概览",
            icon = Icons.Default.AccountBalance,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            BulletItem("自动汇总所有账户（现金、支付宝、微信、银行卡、证券）的总资产与负债。")
            BulletItem("支持多币种（CNY, USD, EUR, JPY, HKD, GBP）实时汇率转换。")
        }

        GuideCard(
            title = "📈 证券理财与基金持仓（实时联网行情）",
            icon = Icons.Default.ShowChart,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            BulletItem("支持新增股票、公募基金（如 510300, 161725 等）、债券或微信余额宝理财。")
            BulletItem("点击『刷新行情』卡片按钮：系统自动连接公募基金估值与证券市场接口，实时同步最新的净值波动与持仓估值。")
            BulletItem("自动计算各资产持仓盈亏、投资回报率 (ROI)，并提供理性配置预警。")
        }

        GuideCard(
            title = "⚽ 足彩与彩票记账（联网/AI开奖核对）",
            icon = Icons.Default.SportsFootball,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            BulletItem("竞彩足球与大乐透/福利彩票记录：支持记录注单、比赛名称（如 曼城VS阿森纳）与投注金额。")
            BulletItem("点击『🌐 联网开奖/核对彩果』按钮：系统会自动检索比分或大乐透开奖号，自动判定中奖状态与派彩金额，实现自动对账！")
            BulletItem("内置理性风控：高风险和高负盈亏自动触发理性购彩预警。")
        }

        GuideCard(
            title = "🌐 离线模式与网络状态监测",
            icon = Icons.Default.WifiOff,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            BulletItem("顶部状态栏实时显示 🌐 联网正常 或 🔴 离线模式。")
            BulletItem("无网络时，本地记账与数据存取100%不受影响。")
        }
    }
}

@Composable
private fun GuideCard(
    title: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StepItem(step: String, detail: String) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = step,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
        )
    }
}

@Composable
private fun MultiUserSystemGuideSection() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GuideCard(
            title = "🔐 企业级多用户身份认证",
            icon = Icons.Default.Lock,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        ) {
            Text(
                text = "系统采用符合商业标准的多租户（多用户）隔离架构，确保每个用户的财务数据严格隔离与安全：",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            BulletItem("多重认证方式：支持【密码登录】、【短信验证码】（测试阶段验证码固定为123456）以及【注册新账户】。")
            BulletItem("数据硬隔离：底层数据库通过 User ID 实现了全面的跨表关联，确保同一台设备上，用户A只能看到和操作用户A的账单与心愿单，绝对无法越权访问。")
            BulletItem("设备缓存机制：基于 Room DB 存储机制，保障不同登录态的数据私密性。")
        }
        
        GuideCard(
            title = "📱 短信验证码模拟流程",
            icon = Icons.Default.Sms,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ) {
            BulletItem("在登录页面点击【短信验证码】选项卡。")
            BulletItem("输入 11 位手机号码（如：13800138000）。")
            BulletItem("点击获取后系统会进入 60 秒倒计时防刷机制。")
            BulletItem("在验证码框输入演示环境专用核验码：123456，即可一键安全登录。")
        }

        GuideCard(
            title = "🔄 注销与重登录",
            icon = Icons.Default.Logout,
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ) {
            BulletItem("为确保商业系统的闭环，如需切换账号，您可以在应用完全退出后重新启动（后续版本将加入全局登出按钮）。")
            BulletItem("每个账户在注册后，会自动生成符合该用户生命周期的“初始化种子数据”以便快速体验。")
        }
    }
}
