package io.github.mangi.eta.ui.screens.prompt

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.data.repository.AgentUserPromptMode
import io.github.mangi.eta.data.repository.AgentUserPromptStore
import io.github.mangi.eta.ui.components.EtaPreference
import io.github.mangi.eta.ui.components.EtaPreferenceColors
import io.github.mangi.eta.ui.components.EtaPreferenceDivider
import io.github.mangi.eta.ui.components.EtaPreferenceGroup
import io.github.mangi.eta.ui.components.EtaPreferenceGroupTitle
import io.github.mangi.eta.ui.components.EtaSwitch
import io.github.mangi.eta.ui.components.EtaRadioButtonPreference
import io.github.mangi.eta.ui.components.EtaTextButton
import io.github.mangi.eta.ui.components.EtaWindowDialog
import io.github.mangi.eta.ui.components.MiuixDialogActions
import io.github.mangi.eta.ui.components.MiuixScaffoldPage
import io.github.mangi.eta.ui.model.AgentUserPromptAction
import io.github.mangi.eta.ui.model.AgentUserPromptUiState
import java.text.NumberFormat
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Protokolli i punës页：Teksti来自 `filesDir/prompts/` 下第一个非空 `.md`。
 *
 * 页面顺序刻意是「先看到状态、再决定是否启用」——默认Çaktivizuar，文件存在也不改变行为，
 * 避免用户没开就发现行为变了还找不到原因。
 */
@Composable
internal fun AgentUserPromptScreen(
    state: AgentUserPromptUiState,
    onAction: (AgentUserPromptAction) -> Unit,
) {
    var showClearDialog by remember { mutableStateOf(false) }
    // .md/.txt 在部分文件提供方会被报成 application/octet-stream，与其猜 MIME 不如放开后按内容校验。
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { onAction(AgentUserPromptAction.Import(it)) }
    }

    MiuixScaffoldPage(
        title = "Protokolli i punës",
        onBack = { onAction(AgentUserPromptAction.NavigateBack) },
        modifier = Modifier.imePadding(),
    ) {
        item(key = "intro") {
            Text(
                text = "Vendos udhëzimet e përhershme në skedarin e parë .md jo bosh te prompts/ (parazgjedhja:${
                    AgentUserPromptStore.DEFAULT_FILE_NAME
                }), ose importoji nga skedari. Udhëzimi dërgohet si mesazh i veçantë system; në rast konflikti merr përparësi mbi kontratën e integruar.",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }

        item(key = "status") {
            EtaPreferenceGroup {
                EtaPreference(
                    title = "Fusha e zbatimit",
                    summary = modeSummary(state),
                )
                EtaPreferenceDivider()
                EtaPreference(
                    title = "Skedari aktual",
                    summary = fileSummary(state),
                )
                EtaPreferenceDivider()
                EtaPreference(
                    title = "Versioni i integruar",
                    summary = builtinSummary(state),
                )
            }
        }

        item(key = "inject-title") { EtaPreferenceGroupTitle("Shtimi i udhëzimeve") }
        item(key = "inject") {
            EtaPreferenceGroup {
                EtaPreference(
                    title = "Protokolli ndjek aftësitë e analizës së kundërt",
                    summary = injectSummary(state) + "；Përdor çelësin kryesor të aftësive të analizës së kundërt te Cilësimet ose Aftësitë",
                )
            }
        }

        item(key = "editor-title") { EtaPreferenceGroupTitle("Teksti") }
        item(key = "editor") {
            if (state.showEditor) {
                TextField(
                    value = state.draft,
                    onValueChange = { onAction(AgentUserPromptAction.DraftChanged(it)) },
                    label = "Teksti i protokollit të punës",
                    enabled = !state.isLoading && !state.isSaving && !state.editingLocked,
                    singleLine = false,
                    minLines = 10,
                    maxLines = 14,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                )
            } else {
                LargeContentPreview(
                    state = state,
                    onOpenEditor = { onAction(AgentUserPromptAction.OpenEditor) },
                )
            }
        }

        item(key = "budget") {
            Text(
                text = budgetSummary(state),
                style = MiuixTheme.textStyles.footnote1,
                color = budgetColor(state),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp),
            )
        }

        item(key = "sources-title") { EtaPreferenceGroupTitle("Burimi i tekstit") }
        item(key = "sources") {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                EtaTextButton(
                    text = "Importo nga skedari",
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    enabled = !state.isLoading && !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.builtinAvailable) {
                    Spacer(Modifier.height(4.dp))
                    EtaTextButton(
                        text = if (state.builtinInstalled) "Riaktivizo versionin e integruar" else "Aktivizo versionin e integruar",
                        onClick = { onAction(AgentUserPromptAction.InstallBuiltin) },
                        enabled = !state.isLoading && !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item(key = "actions") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EtaTextButton(
                    text = "Ringarko",
                    onClick = { onAction(AgentUserPromptAction.Reload) },
                    enabled = !state.isLoading && !state.isSaving,
                    modifier = Modifier.weight(1f),
                )
                EtaTextButton(
                    text = "Ruaj",
                    onClick = { onAction(AgentUserPromptAction.Save) },
                    enabled = state.canSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }

        item(key = "clear") {
            EtaTextButton(
                text = "Pastro (krijo më parë kopje rezervë)",
                onClick = { showClearDialog = true },
                enabled = !state.isLoading && !state.isSaving && state.savedContent.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        item(key = "footer") {
            Text(
                text = "Ndryshimet zbatohen në bisedën e ardhshme. Ndryshimi i skedarit gjatë bisedës nuk prek kërkesën aktive.",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }

    if (showClearDialog) {
        EtaWindowDialog(
            show = true,
            title = "Pastro protokollin e punës",
            summary = "Skedari aktual ruhet fillimisht te prompts/.backup/ dhe pastaj pastrohet. Nuk do të dërgohet më protokoll.",
            onDismissRequest = { showClearDialog = false },
        ) {
            MiuixDialogActions(
                confirmText = "Pastro",
                destructive = true,
                confirmEnabled = !state.isSaving,
                onCancel = { showClearDialog = false },
                onConfirm = {
                    showClearDialog = false
                    onAction(AgentUserPromptAction.Clear)
                },
            )
        }
    }

    state.pendingImport?.let { pending ->
        EtaWindowDialog(
            show = true,
            title = "Importo protokollin e punës",
            summary = buildString {
                appendLine("${pending.fileName}｜${formatNumber(pending.charCount)}  karaktere｜${formatBytes(pending.byteSize)}")
                if (pending.overLimit) {
                    appendLine("Tejkalon kufirin prej${formatNumber(AgentUserPromptStore.MAX_CHARS)}karakteresh; pas importimit do të përdoret versioni i shkurtuar.")
                }
                append("Pas konfirmimit zëvendësohet skedari aktiv; origjinali ruhet te prompts/.backup/.")
            },
            onDismissRequest = { onAction(AgentUserPromptAction.CancelImport) },
        ) {
            MiuixDialogActions(
                confirmText = "Importo",
                confirmEnabled = !state.isSaving,
                onCancel = { onAction(AgentUserPromptAction.CancelImport) },
                onConfirm = { onAction(AgentUserPromptAction.ConfirmImport) },
            )
        }
    }

    state.notice?.let { notice ->
        EtaWindowDialog(
            show = true,
            title = "Protokolli i punës",
            summary = notice,
            onDismissRequest = { onAction(AgentUserPromptAction.DismissNotice) },
        ) {
            EtaTextButton(
                text = "E kuptova",
                onClick = { onAction(AgentUserPromptAction.DismissNotice) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Teksti过长时的只读预览。
 *
 * 8 万 karaktere塞进 TextField，每次重排都要整段测量，手机上会明显卡顿；而这条链路的真实用法是
 * 「Importo整份提示词再启用」，不需要逐字编辑。所以默认只渲染前 [AgentUserPromptUiState.PREVIEW_CHARS]
 * 个 karaktere供核对，要手改再显式展开全文。
 */
@Composable
private fun LargeContentPreview(
    state: AgentUserPromptUiState,
    onOpenEditor: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = state.previewText,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 24,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.previewTruncated) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Shfaqen vetëm${formatNumber(AgentUserPromptUiState.PREVIEW_CHARS)}karakteret e para; teksti i plotë ka${
                    formatNumber(state.draft.length)
                }karaktere. Ndryshimi i të gjithit mund të ngadalësojë telefonin; rekomandohet importimi nga skedari.",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Spacer(Modifier.height(6.dp))
        EtaTextButton(
            text = "Hap tekstin e plotë për redaktim",
            onClick = onOpenEditor,
            enabled = !state.isLoading && !state.isSaving && !state.editingLocked,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Versioni i integruar状态：随包多少 karaktere、是否就是当前生效文件——换版或手改过会显示「与当前不同」。 */
private fun builtinSummary(state: AgentUserPromptUiState): String {
    val chars = state.builtinCharCount ?: return "Nuk përfshihet në paketë; ngjite tekstin ose importoje nga skedari"
    val size = "${formatNumber(chars)}  karaktere"
    return when {
        state.builtinInstalled -> "I integruar:$size · aktiv si skedar aktual"
        state.sourceFileName.isNullOrBlank() -> "I integruar:$size · ende joaktiv"
        else -> "I integruar:$size · ndryshon nga skedari aktual (mund ta zëvendësosh)"
    }
}

private fun injectSummary(state: AgentUserPromptUiState): String = when {
    state.sourceFileName.isNullOrBlank() -> "Nuk ka tekst të disponueshëm. Aktivizo versionin e integruar ose importoje."
    state.enabled -> "Aktiv · dërgohet në çdo bisedë"
    else -> "Joaktiv · nuk dërgohet"
}

private fun modeSummary(state: AgentUserPromptUiState): String = when {
    state.enabled -> "Po dërgohen udhëzimet"
    state.mode == AgentUserPromptMode.Off && state.pendingActivation ->
        "Teksti u gjet, por aktivizimi është i fikur; nuk dërgohet"
    state.mode == AgentUserPromptMode.Off -> "Çaktivizuar"
    state.mode == AgentUserPromptMode.Global -> "Dërgimi i udhëzimeve është joaktiv"
    else -> "Dërgimi i udhëzimeve është joaktiv"
}

private fun fileSummary(state: AgentUserPromptUiState): String {
    val name = state.sourceFileName
    if (name.isNullOrBlank()) {
        return "Nuk u gjet tekst: nuk ka skedar .md jo bosh te prompts/"
    }
    val size = "${formatNumber(state.charCount)}  karaktere"
    return if (state.truncated) {
        "prompts/$name · $size(tejkalon${formatNumber(AgentUserPromptStore.MAX_CHARS)} kufirin; dërgohet versioni i shkurtuar dhe redaktimi është i kyçur)"
    } else {
        "prompts/$name · $size"
    }
}

/** 体积警告：实时 karaktere数、与上限的关系以及模型上下文窗口——超限用红、接近上限用黄。 */
private fun budgetSummary(state: AgentUserPromptUiState): String = buildString {
    val limit = AgentUserPromptStore.MAX_CHARS
    append("${formatNumber(state.draftCharCount)} / ${formatNumber(limit)}  karaktere")
    when {
        state.truncated -> append(" Skedari tejkalon kufirin; do të përdoret versioni i shkurtuar")
        state.draftCharCount > limit -> append(" Kufiri u tejkalua; pas ruajtjes do të përdoret versioni i shkurtuar")
        state.nearBudget -> append(" Afër kufirit; kontrollo përdorimin e kontekstit")
        else -> append(" Dërgohet me udhëzimin e sistemit në çdo kërkesë")
    }
    state.contextWindow?.let { append(" Dritarja e kontekstit:${formatNumber(it)} token") }
}

@Composable
private fun budgetColor(state: AgentUserPromptUiState) = when {
    state.truncated || state.draftCharCount > AgentUserPromptStore.MAX_CHARS -> MiuixTheme.colorScheme.error
    state.nearBudget -> EtaPreferenceColors.Yellow
    else -> MiuixTheme.colorScheme.onSurfaceVariantSummary
}

private fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance().format(value)

private fun formatBytes(bytes: Int): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_024 * 1_024 -> "%.1f KiB".format(bytes / 1_024.0)
    bytes % (1_024 * 1_024) == 0 -> "${bytes / (1_024 * 1_024)} MiB"
    else -> "%.2f MiB".format(bytes / (1_024.0 * 1_024.0))
}
