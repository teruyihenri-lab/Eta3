package io.github.mangi.eta.ui.preview

import io.github.mangi.eta.ui.model.AgentChatHomeUiState
import io.github.mangi.eta.ui.model.AgentChatMessageUi
import io.github.mangi.eta.ui.model.AgentChatUiState
import io.github.mangi.eta.ui.model.AgentToolsUiState
import io.github.mangi.eta.ui.model.ConversationModeUi
import io.github.mangi.eta.ui.model.ConversationPaneUiState
import io.github.mangi.eta.ui.model.ConversationSummaryUi
import io.github.mangi.eta.ui.model.PermissionHealthItemUi
import io.github.mangi.eta.ui.model.PermissionHealthUiState
import io.github.mangi.eta.ui.model.PermissionStatusUi
import io.github.mangi.eta.ui.model.ThinkingMessageUi
import io.github.mangi.eta.ui.model.TokenUsageUi
import io.github.mangi.eta.ui.model.ToolActivityMessageUi
import io.github.mangi.eta.ui.model.ToolActivityStatusUi
import io.github.mangi.eta.ui.model.ToolGroupUi
import io.github.mangi.eta.ui.model.ToolItemUi
import io.github.mangi.eta.ui.model.UserMessageUi
import io.github.mangi.eta.ui.model.AgentMessageUi

internal object FakeAgentUiStates {

    val conversations = ConversationPaneUiState(
        selectedConversationId = "c-001",
        searchQuery = "",
        conversations = listOf(
            ConversationSummaryUi(
                id = "c-001",
                title = "Lejo Eta-n të kontrollojë telefonin",
                preview = "Gati për kontrollin e ekranit dhe mjeteve; në pritje të detyrës",
                timeLabel = "Tani",
                mode = ConversationModeUi.PhoneAgent,
                isPinned = true,
                isActiveRun = true,
            ),
            ConversationSummaryUi(
                id = "c-002",
                title = "Plani dhe kujtesat e ditës",
                preview = "Kontrollo motin, sinkronizo kalendarin dhe vendos kujtesë për daljen",
                timeLabel = "10:41",
                mode = ConversationModeUi.Chat,
                isPinned = true,
            ),
            ConversationSummaryUi(
                id = "c-003",
                title = "Hap NetEase Cloud Music dhe luaj sugjerimet ditore",
                preview = "U përfunduan 4 thirrje mjetesh në 8 sekonda",
                timeLabel = "10:23",
                mode = ConversationModeUi.PhoneAgent,
            ),
            ConversationSummaryUi(
                id = "c-004",
                title = "Analizo pamjen e ekranit aktual",
                preview = "Përmblidh strukturën e faqes dhe problemet e butonave",
                timeLabel = "Dje",
                mode = ConversationModeUi.Chat,
            ),
            ConversationSummaryUi(
                id = "c-005",
                title = "Kontrollo terminalin Alpine",
                preview = "Listo komandat, versionet Python/Node dhe detyrat në sfond",
                timeLabel = "E premte",
                mode = ConversationModeUi.Terminal,
            ),
            ConversationSummaryUi(
                id = "c-006",
                title = "Organizo Shkarkimet çdo natë",
                preview = "Skano skedarët dhe klasifiko imazhet, APK-të dhe dokumentet",
                timeLabel = "5-18",
                mode = ConversationModeUi.Automation,
            ),
        ),
    )

    val chatHome: AgentChatHomeUiState = AgentChatHomeUiState(
        messages = emptyList(),
        input = "",
        isStreaming = false,
        thinkingEnabled = false,
    )

    val chat = AgentChatUiState(
        messages = listOf(
            UserMessageUi(
                id = "m-01",
                content = "Hap optimizimin e baterisë te Cilësimet",
            ),
            AgentMessageUi(
                id = "m-02",
                content = "Ekrani aktual i telefonit shfaq **faqen kryesore**. Informacioni kryesor:\n\n| Elementi | Përmbajtja |\n| --- | --- |\n| Ora | 09:55 |\n| Rrjeti | 5G + Wi-Fi |\n| Bateria | 68% |",
                usage = TokenUsageUi(
                    contextTokens = 17100,
                    inputTokens = 9000,
                    outputTokens = 111,
                    reasoningTokens = 8200,
                ),
            ),
            ThinkingMessageUi(
                id = "thinking-01",
                content = "Përdoruesi kërkon të shoh ekranin aktual. Fillimisht duhet të përdor observe_screen, pastaj të përmbledh çfarë shfaqet.",
                isStreaming = false,
                elapsedSeconds = 24,
                collapsed = true,
            ),
            ToolActivityMessageUi(
                id = "tools-01",
                toolName = "observe_screen",
                status = ToolActivityStatusUi.Success,
                argumentsSummary = "{\"include_screenshot\":true}",
                resultSummary = "ok=true, chars=1820, images=1",
                imageCount = 1,
            ),
        ),
        input = "",
        isStreaming = false,
        thinkingEnabled = true,
    )

    val permissionHealth = PermissionHealthUiState(
        items = listOf(
            PermissionHealthItemUi(
                id = "accessibility",
                title = "Shërbimi i aksesueshmërisë",
                summary = "Aktiv; agjenti mund të kontrollojë ndërfaqen",
                status = PermissionStatusUi.Available,
                primaryActionLabel = null,
            ),
            PermissionHealthItemUi(
                id = "overlay",
                title = "Leja e dritares lundruese",
                summary = "U lejua; mund të shfaqet dritarja e statusit",
                status = PermissionStatusUi.Available,
                primaryActionLabel = null,
            ),
            PermissionHealthItemUi(
                id = "notification",
                title = "Leja e njoftimeve",
                summary = "Për njoftimet kur përfundojnë detyrat në sfond",
                status = PermissionStatusUi.Available,
                primaryActionLabel = null,
            ),
            PermissionHealthItemUi(
                id = "location",
                title = "Leja e vendndodhjes",
                summary = "Lexohet vetëm kur agjenti përdor mjetin",
                status = PermissionStatusUi.Available,
                primaryActionLabel = null,
            ),
            PermissionHealthItemUi(
                id = "root",
                title = "Leja Root",
                summary = "Nuk është dhënë; disa komanda terminali janë të kufizuara",
                status = PermissionStatusUi.Warning,
                primaryActionLabel = "Kontrollo",
            ),
            PermissionHealthItemUi(
                id = "shizuku",
                title = "Shizuku",
                summary = "Nuk është konfiguruar; aftësitë ADB nuk janë të disponueshme",
                status = PermissionStatusUi.Disabled,
                primaryActionLabel = "Konfiguro",
            ),
            PermissionHealthItemUi(
                id = "xposed",
                title = "Hook / Xposed",
                summary = "Framework-u është joaktiv; përmirësimet e sistemit nuk janë të disponueshme",
                status = PermissionStatusUi.Missing,
                primaryActionLabel = "Shfaq",
            ),
            PermissionHealthItemUi(
                id = "background",
                title = "Punimi në sfond",
                summary = "Optimizimi i baterisë është aktiv; detyrat e gjata mund të ndërpriten",
                status = PermissionStatusUi.Warning,
                primaryActionLabel = "Cilësimet",
            ),
        ),
    )

    val tools = AgentToolsUiState(
        groups = listOf(
            ToolGroupUi(
                id = "screen",
                title = "Kontrolli i ekranit",
                tools = listOf(
                    ToolItemUi("observe_screen", "Vëzhgo ekranin", "Lexo elementet e ndërfaqes dhe imazhin kur nevojitet"),
                    ToolItemUi("tap_element", "Prek", "Prek koordinatat ose elementin e caktuar"),
                    ToolItemUi("long_press", "Mbaj shtypur", "Mbaj shtypur elementin e caktuar"),
                    ToolItemUi("swipe", "Rrëshqit", "Rrëshqit, lëviz në faqe dhe kthehu"),
                ),
            ),
            ToolGroupUi(
                id = "input",
                title = "Teksti dhe tastet",
                tools = listOf(
                    ToolItemUi("input_text", "Shkruaj tekst", "Shkruaj tekst në fushën aktive"),
                    ToolItemUi("paste_text", "Ngjit tekstin", "Përdor kujtesën e kopjimit për të shkruar tekst"),
                    ToolItemUi("wait_for_text", "Prit tekstin", "Prit derisa teksti të shfaqet në ekran"),
                ),
            ),
            ToolGroupUi(
                id = "web",
                title = "Shfletimi i internetit",
                tools = listOf(
                    ToolItemUi("browser_use", "Shfletuesi i agjentit", "Shfleto në sfond me mundësi për të marrë kontrollin"),
                    ToolItemUi("browser_read", "Lexo faqen", "Nxirr tekstin e faqes së shfaqur"),
                    ToolItemUi("browser_interact", "Ndërvepro me faqen", "Gjej, prek dhe plotëso elementet"),
                    ToolItemUi("browser_screenshot", "Pamje e faqes", "Dërgo pamjen e faqes te modeli vizual"),
                ),
            ),
            ToolGroupUi(
                id = "app",
                title = "Aplikacionet dhe URI",
                tools = listOf(
                    ToolItemUi("get_current_context", "Ora dhe vendndodhja", "Lexo orën e sistemit dhe vendndodhjen e fundit"),
                    ToolItemUi("launch_app", "Hap aplikacionin", "Nis aplikacionin me emrin e paketës"),
                    ToolItemUi("open_uri", "Hap me aplikacion", "Kaloje kërkesën te aplikacioni i jashtëm"),
                ),
            ),
            ToolGroupUi(
                id = "terminal",
                title = "Terminali dhe skedarët",
                tools = listOf(
                    ToolItemUi("terminal", "Komanda terminali", "Ekzekuto komanda Shell në Android, Alpine ose Debian"),
                ),
            ),
        ),
    )
}
