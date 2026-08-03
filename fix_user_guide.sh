sed -i 's/val tabs = listOf("📖 默认数据说明", "🚀 软件快速上手", "⚙️ AI\/API中转站教程", "📊 资产与功能指南")/val tabs = listOf("📖 默认数据说明", "🚀 软件快速上手", "⚙️ AI\/API中转站教程", "📊 资产与功能指南", "🔐 商业多账户体系")/' app/src/main/java/com/example/ui/components/UserGuideDialog.kt

sed -i '/3 -> FeaturesGuideSection()/a \                        4 -> MultiUserSystemGuideSection()' app/src/main/java/com/example/ui/components/UserGuideDialog.kt
