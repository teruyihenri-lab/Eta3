package io.github.mangi.eta.ui.screens.memory

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mangi.eta.R
import io.github.mangi.eta.data.repository.KnowledgeDocument
import io.github.mangi.eta.data.repository.RetainedRelation
import io.github.mangi.eta.ui.components.EtaCard
import io.github.mangi.eta.ui.components.EtaPreference
import io.github.mangi.eta.ui.components.EtaPreferenceDefaults
import io.github.mangi.eta.ui.components.EtaPreferenceDivider
import io.github.mangi.eta.ui.components.EtaPreferenceGroup
import io.github.mangi.eta.ui.components.EtaSwitchPreference
import io.github.mangi.eta.ui.components.EtaTextButton
import io.github.mangi.eta.ui.components.EtaWindowDialog
import io.github.mangi.eta.ui.components.MiuixDialogActions
import io.github.mangi.eta.ui.components.MiuixScaffold
import io.github.mangi.eta.ui.layout.horizontalCutoutPadding
import io.github.mangi.eta.ui.markdown.MarkdownTone
import io.github.mangi.eta.ui.markdown.StaticMarkdown
import io.github.mangi.eta.ui.model.AgentMemoryAction
import io.github.mangi.eta.ui.model.AgentMemoryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class MemoryViewMode { Preview, Edit }

/**
 * 记忆页：顶部是开关与预算，下方 MEMORY.md 以“文档”形式占满剩余高度。
 * 默认渲染 Markdown 便于阅读，切到编辑后是无边框的整页编辑区；只有存在未Ruaj修改时才浮出Ruaj栏。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AgentMemoryScreen(
    state: AgentMemoryUiState,
    onAction: (AgentMemoryAction) -> Unit,
) {
    val appContext = LocalContext.current
    val importScope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }
    var showMemoryConflictDialog by remember { mutableStateOf(false) }
    var showKnowledgeList by remember { mutableStateOf(false) }
    var showKnowledgeEditor by remember { mutableStateOf(false) }
    var showKnowledgeDelete by remember { mutableStateOf(false) }
    var showRetainedList by remember { mutableStateOf(false) }
    var showRetainedClear by remember { mutableStateOf(false) }
    var selectedRetainedId by remember { mutableStateOf<String?>(null) }
    var selectedRetainedLink by remember { mutableStateOf<RetainedRelation?>(null) }
    var showSemanticModelDialog by remember { mutableStateOf(false) }
    var semanticModelDraft by remember { mutableStateOf("") }
    var syncEndpoint by remember { mutableStateOf("") }
    var syncUsername by remember { mutableStateOf("") }
    var syncPassword by remember { mutableStateOf("") }
    var syncPassphrase by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var knowledgeImportError by remember { mutableStateOf<String?>(null) }
    val knowledgeImporter = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importScope.launch {
                val imported = withContext(Dispatchers.IO) {
                    runCatching {
                        val resolver = appContext.contentResolver
                        val filename = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                            ?.use { cursor ->
                                if (cursor.moveToFirst()) cursor.getString(0) else null
                            } ?: "imported.md"
                        require(filename.endsWith(".md", true) || filename.endsWith(".markdown", true) ||
                            filename.endsWith(".txt", true)) {
                            "Mbështeten vetëm Markdown dhe skedarët tekst; PDF/Word kërkojnë analizues të veçantë"
                        }
                        val bytes = resolver.openInputStream(uri)?.use { it.readNBytes(52_000) }
                            ?: error("Skedari nuk mund të lexohet")
                        require(bytes.size < 52_000 && 0.toByte() !in bytes) { "Skedari është tepër i madh ose nuk përmban tekst të thjeshtë" }
                        val content = bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF")
                        require(content.isNotBlank() && content.length <= 12_000) {
                            "Çdo njohuri mund të ketë deri në 12 000 karaktere"
                        }
                        filename.substringBeforeLast('.').take(160) to content
                    }
                }
                imported.fold(
                    onSuccess = { (title, content) ->
                        knowledgeImportError = null
                        onAction(AgentMemoryAction.SaveKnowledge(
                            title = title, content = content, kind = "reference",
                            source = "Android document import", tags = emptyList(),
                        ))
                    },
                    onFailure = { knowledgeImportError = it.message ?: "Importimi dështoi" },
                )
            }
        }
    }
    var editDocument by remember { mutableStateOf<KnowledgeDocument?>(null) }
    var knowledgeTitle by remember { mutableStateOf("") }
    var knowledgeContent by remember { mutableStateOf("") }
    var knowledgeSource by remember { mutableStateOf("") }
    var knowledgeTags by remember { mutableStateOf("") }
    fun openKnowledgeEditor(document: KnowledgeDocument?) {
        editDocument = document
        knowledgeTitle = document?.title.orEmpty()
        knowledgeContent = document?.content.orEmpty()
        knowledgeSource = document?.source.orEmpty()
        knowledgeTags = document?.tags?.joinToString(",").orEmpty()
        showKnowledgeList = false
        showKnowledgeEditor = true
    }
    var mode by rememberSaveable { mutableStateOf<MemoryViewMode?>(null) }
    // 首次读到内容前不决定模式：空记忆直接进入编辑，已有内容先给出可读的预览。
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && mode == null) {
            mode = if (state.savedContent.isBlank()) MemoryViewMode.Edit else MemoryViewMode.Preview
        }
    }
    val currentMode = mode ?: MemoryViewMode.Preview
    val imeVisible = WindowInsets.isImeVisible

    MiuixScaffold(
        title = stringResource(R.string.ui_memory_b55ff5),
        onBack = { onAction(AgentMemoryAction.NavigateBack) },
        actions = {
            IconButton(
                onClick = { showClearDialog = true },
                enabled = !state.isLoading && !state.isSaving && state.draft.isNotEmpty(),
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.ui_clear_all_memory_a43bd3),
                    tint = if (!state.isLoading && state.draft.isNotEmpty()) {
                        MiuixTheme.colorScheme.onSurface
                    } else {
                        MiuixTheme.colorScheme.disabledOnSurface
                    },
                )
            }
        },
    ) { paddingValues, scrollBehavior, sidePadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalCutoutPadding()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = sidePadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .imePadding()
                .navigationBarsPadding(),
        ) {
            // 键盘弹出时收起设置区，把有限高度全部让给正在编辑的文档。
            AnimatedVisibility(
                visible = !imeVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                MemorySettingsGroup(
                    state = state, onAction = onAction,
                    onConfigureSync = {
                        syncEndpoint = state.syncEndpoint
                        syncUsername = state.syncUsername
                        syncPassword = ""
                        syncPassphrase = ""
                        showSyncDialog = true
                    },
                    onDisconnectSync = { showDisconnectDialog = true },
                    onOpenKnowledge = { showKnowledgeList = true },
                    onOpenRetained = { showRetainedList = true },
                    onConfigureSemanticModel = {
                        semanticModelDraft = state.semanticModel
                        showSemanticModelDialog = true
                    },
                    onResolveMemoryConflict = { showMemoryConflictDialog = true },
                )
            }
            MemoryDocumentCard(
                state = state,
                mode = currentMode,
                onModeChange = { mode = it },
                onDraftChange = { onAction(AgentMemoryAction.DraftChanged(it)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = EtaPreferenceDefaults.SidePadding),
            )
            MemorySaveBar(
                state = state,
                onDiscard = { onAction(AgentMemoryAction.DraftChanged(state.savedContent)) },
                onSave = { onAction(AgentMemoryAction.Save) },
            )
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showClearDialog) {
        EtaWindowDialog(
            show = true,
            title = stringResource(R.string.ui_clear_all_memory_a43bd3),
            summary = stringResource(R.string.ui_the_entire_contents_of_memory_md_will_be_deleted_and_83a8ac),
            onDismissRequest = { showClearDialog = false },
        ) {
            MiuixDialogActions(
                confirmText = stringResource(R.string.memory_clear),
                destructive = true,
                confirmEnabled = !state.isSaving,
                onCancel = { showClearDialog = false },
                onConfirm = {
                    showClearDialog = false
                    onAction(AgentMemoryAction.Clear)
                },
            )
        }
    }

    EtaWindowDialog(
        show = showSyncDialog,
        title = "Sinkronizim i enkriptuar WebDAV",
        onDismissRequest = { if (!state.syncBusy) showSyncDialog = false },
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
            Column(
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "Shkruaj adresën HTTPS të një dosjeje WebDAV. Eta krijon eta-sync-v1 brenda saj. Përdor të njëjtën frazëkalim në çdo pajisje.",
                    style = MiuixTheme.textStyles.body2,
                )
                TextField(
                    value = syncEndpoint,
                    onValueChange = { syncEndpoint = it },
                    label = "Adresa URL e dosjes WebDAV",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = syncUsername,
                    onValueChange = { syncUsername = it },
                    label = "Emri i përdoruesit (opsional)",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = syncPassword,
                    onValueChange = { syncPassword = it },
                    label = if (state.syncHasPassword) "Fjalëkalimi WebDAV (bosh për të ruajtur aktualin)" else "Fjalëkalimi WebDAV",
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = syncPassphrase,
                    onValueChange = { syncPassphrase = it },
                    label = if (state.syncHasPassphrase) "Fraza e enkriptimit (bosh për të ruajtur aktualen)" else "Fraza e enkriptimit (të paktën 12 karaktere)",
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            MiuixDialogActions(
                modifier = Modifier.padding(top = 12.dp),
                confirmText = "Ruaj lidhjen",
                confirmEnabled = syncEndpoint.isNotBlank() && !state.syncBusy &&
                    (state.syncHasPassphrase || syncPassphrase.length >= 12),
                onCancel = { showSyncDialog = false },
                onConfirm = {
                    onAction(AgentMemoryAction.SaveWebDav(
                        syncEndpoint, syncUsername, syncPassword, syncPassphrase,
                    ))
                    syncPassword = ""
                    syncPassphrase = ""
                    showSyncDialog = false
                },
            )
        }
    }

    EtaWindowDialog(
        show = showDisconnectDialog,
        title = "Shkëput sinkronizimin WebDAV",
        summary = "Hiq adresën dhe çelësin nga kjo pajisje; mos fshi njohuritë lokale ose skedarët e enkriptuar në server.",
        onDismissRequest = { showDisconnectDialog = false },
    ) {
        MiuixDialogActions(
            confirmText = "Shkëput",
            destructive = true,
            onCancel = { showDisconnectDialog = false },
            onConfirm = {
                showDisconnectDialog = false
                onAction(AgentMemoryAction.ClearWebDavConfig)
            },
        )
    }

    EtaWindowDialog(
        show = showMemoryConflictDialog,
        title = "Konfirmo zgjidhjen e konfliktit të kujtesës",
        onDismissRequest = { showMemoryConflictDialog = false },
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = 510.dp)) {
            Text(
                "Shqyrto kujtesën në server, bashkoje me MEMORY.md lokal dhe ruaje. Pas konfirmimit kontrollohet përsëri serveri dhe përdoret versioni lokal; njohuritë dhe përvojat bashkohen.",
                style = MiuixTheme.textStyles.body2,
            )
            state.syncRemoteConflictContent?.let { remoteText ->
                Text(
                    "MEMORY.md në server (parapamje):",
                    modifier = Modifier.padding(top = 10.dp),
                    style = MiuixTheme.textStyles.body1,
                )
                Box(
                    modifier = Modifier.weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                ) {
                    StaticMarkdown(
                        content = remoteText.take(12_000),
                        tone = MarkdownTone.Answer,
                    )
                }
                EtaTextButton(
                    text = "Kopjo kujtesën e plotë nga serveri",
                    onClick = {
                        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE)
                            as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Eta remote memory", remoteText))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            MiuixDialogActions(
                modifier = Modifier.padding(top = 10.dp),
                confirmText = "U shqyrtua; përdor versionin lokal",
                confirmEnabled = state.syncRemoteConflictContent != null &&
                    !state.hasUnsavedChanges && !state.syncBusy,
                onCancel = { showMemoryConflictDialog = false },
                onConfirm = {
                    showMemoryConflictDialog = false
                    onAction(AgentMemoryAction.ConfirmWebDavMemoryResolution)
                },
            )
        }
    }

    EtaWindowDialog(
        show = showKnowledgeList,
        title = "Baza lokale e njohurive",
        onDismissRequest = { showKnowledgeList = false },
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = "Kërko titull, burim ose etiketë",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Column(
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            ) {
                state.knowledgeDocuments.filter {
                    searchText.isBlank() || it.title.contains(searchText, true) ||
                        it.source.contains(searchText, true) ||
                        it.content.contains(searchText, true) ||
                        it.tags.any { tag -> tag.contains(searchText, true) }
                }.take(50).forEach { document ->
                    EtaPreference(
                        title = document.title,
                        summary = "${document.kind} · ${document.source.take(80)}",
                        onClick = { openKnowledgeEditor(document) },
                    )
                    EtaPreferenceDivider(hasLeading = false)
                }
            }
            knowledgeImportError?.let { message ->
                Text(message, style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.error)
            }
            EtaTextButton(
                text = "Importo Markdown / TXT",
                onClick = {
                    knowledgeImportError = null
                    knowledgeImporter.launch(arrayOf("text/*", "application/octet-stream"))
                },
                modifier = Modifier.fillMaxWidth(),
            )
            MiuixDialogActions(
                modifier = Modifier.padding(top = 12.dp),
                confirmText = "Krijo njohuri",
                onCancel = { showKnowledgeList = false },
                onConfirm = { openKnowledgeEditor(null) },
            )
        }
    }

    EtaWindowDialog(
        show = showRetainedList,
        title = "Kujtesë afatgjatë e strukturuar",
        onDismissRequest = { showRetainedList = false },
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
            Text(
                "Ruajtje me nivele e të dhënave të nxjerra nga modeli; provat nuk janë verifikuar në mënyrë të pavarur dhe lidhjet janë sugjerime.",
                style = MiuixTheme.textStyles.body2,
            )
            Column(
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            ) {
                state.retainedNodes.forEach { node ->
                    EtaPreference(
                        title = node.text.take(85),
                        summary = (if (node.active) "Aktiv" else "U zëvendësua nga të dhëna më të reja") +
                            " · ${node.layer} · ${node.category} · Sugjerime lidhjesh:${node.links.size}  elemente",
                        onClick = {
                            selectedRetainedId = node.id
                            showRetainedList = false
                        },
                    )
                    EtaPreferenceDivider(hasLeading = false)
                }
            }
            MiuixDialogActions(
                confirmText = "Pastro kujtesën e strukturuar",
                confirmEnabled = state.retainedNodes.isNotEmpty(),
                destructive = true,
                onCancel = { showRetainedList = false },
                onConfirm = {
                    showRetainedList = false
                    showRetainedClear = true
                },
            )
        }
    }

    state.retainedNodes.find { it.id == selectedRetainedId }?.let { node ->
        EtaWindowDialog(
            show = selectedRetainedId != null && selectedRetainedLink == null,
            title = "Hollësitë e nyjës së kujtesës",
            onDismissRequest = { selectedRetainedId = null; showRetainedList = true },
        ) {
            Column(Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(node.text, style = MiuixTheme.textStyles.body1)
                    Text(
                        "Gjendja:${if (node.active) "Aktiv" else "Version historik (joaktiv)"}\n" +
                            "Niveli:${node.layer} / ${node.category}\n" +
                            "Fusha:${node.scope}\nSubjekti:${node.subject}\n" +
                            "Dëshmi e përdoruesit:${node.evidence}\n" +
                            "Entitetet:${node.entities.joinToString("、")}\n" +
                            "Lidhjet:${node.links.size}  elemente (Lidhjet e konfirmuara lejojnë zgjerim deri në dy hapa;" +
                            "sugjerimet e pashqyrtuara vetëm një hap)",
                        style = MiuixTheme.textStyles.body2,
                    )
                    node.links.forEach { link ->
                        val related = state.retainedNodes.find { it.id == link.targetId }
                        EtaPreference(
                            title = "${link.type} → ${related?.text?.take(45) ?: "Nyja e lidhur nuk është më aktive"}",
                            summary = "${when(link.status) {
                                "confirmed" -> "U konfirmua"
                                "rejected" -> "U refuzua"
                                else -> "Në shqyrtim"
                            }} · Besueshmëria:${"%.2f".format(link.confidence)} · ${link.reason.take(75)}",
                            onClick = { selectedRetainedLink = link },
                        )
                        EtaPreferenceDivider(hasLeading = false)
                    }
                }
                MiuixDialogActions(
                    modifier = Modifier.padding(top = 12.dp),
                    confirmText = "Fshi këtë kujtim",
                    destructive = true,
                    onCancel = { selectedRetainedId = null; showRetainedList = true },
                    onConfirm = {
                        onAction(AgentMemoryAction.DeleteRetainedMemory(node.id))
                        selectedRetainedId = null
                        showRetainedList = true
                    },
                )
            }
        }
    }

    selectedRetainedLink?.let { relation ->
        EtaWindowDialog(
            show = true,
            title = "Shqyrto lidhjet e kujtesës",
            summary = "${relation.type} / Besueshmëria e modelit:${"%.2f".format(relation.confidence)}\n" +
                "Arsyeja:${relation.reason.ifBlank { "Sugjeruar automatikisht nga Retain; ende pa shpjegim" }}\n" +
                "Objektivi:${state.retainedNodes.find { it.id == relation.targetId }?.text ?: "Nyja e lidhur nuk është e disponueshme"}",
            onDismissRequest = { selectedRetainedLink = null },
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EtaTextButton(
                    text = "Konfirmo lidhjen (deri në dy hapa)",
                    onClick = {
                        selectedRetainedId?.let { id ->
                            onAction(AgentMemoryAction.ReviewMemoryLink(
                                id, relation.targetId, relation.type, "confirmed",
                            ))
                        }
                        selectedRetainedLink = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                EtaTextButton(
                    text = "Refuzo lidhjen (përjashto nga rikujtimi)",
                    onClick = {
                        selectedRetainedId?.let { id ->
                            onAction(AgentMemoryAction.ReviewMemoryLink(
                                id, relation.targetId, relation.type, "rejected",
                            ))
                        }
                        selectedRetainedLink = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    EtaWindowDialog(
        show = showSemanticModelDialog,
        title = "Cilësimet e modelit Embeddings",
        onDismissRequest = { showSemanticModelDialog = false },
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                "Mbështetet vetëm API HTTPS Embeddings e përputhshme me OpenAI e ofruesit të zgjedhur." +
                    "Mos përdor emrin e modelit të bisedës; vektorët ruhen lokalisht. Pas ndryshimit të modelit rindërto indeksin.",
                style = MiuixTheme.textStyles.body2,
            )
            TextField(
                value = semanticModelDraft,
                onValueChange = { semanticModelDraft = it },
                label = "Identifikuesi i modelit Embeddings",
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            MiuixDialogActions(
                modifier = Modifier.padding(top = 10.dp),
                confirmText = "Ruaj modelin",
                confirmEnabled = semanticModelDraft.matches(Regex("[a-zA-Z0-9._:/-]{1,100}")),
                onCancel = { showSemanticModelDialog = false },
                onConfirm = {
                    onAction(AgentMemoryAction.SaveSemanticModel(semanticModelDraft.trim()))
                    showSemanticModelDialog = false
                },
            )
        }
    }

    EtaWindowDialog(
        show = showRetainedClear,
        title = "Konfirmo pastrimin e kujtesës së strukturuar",
        summary = "Pastrimi sinkronizohet në pajisjet e tjera, por nuk fshin MEMORY.md, përvojat e detyrave apo njohuritë.",
        onDismissRequest = { showRetainedClear = false },
    ) {
        MiuixDialogActions(
            confirmText = "Pastro të gjitha",
            destructive = true,
            onCancel = { showRetainedClear = false; showRetainedList = true },
            onConfirm = {
                showRetainedClear = false
                onAction(AgentMemoryAction.ClearRetainedMemory)
            },
        )
    }

    EtaWindowDialog(
        show = showKnowledgeEditor,
        title = if (editDocument == null) "Shto njohuri" else "Ndrysho njohurinë",
        onDismissRequest = { showKnowledgeEditor = false },
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = 540.dp)) {
            Column(
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = knowledgeTitle,
                    onValueChange = { knowledgeTitle = it },
                    label = "Titulli",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = knowledgeContent,
                    onValueChange = { knowledgeContent = it },
                    label = "Përmbajtja Markdown",
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = knowledgeSource,
                    onValueChange = { knowledgeSource = it },
                    label = "Burimi (URL, skedar ose përdorues)",
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = knowledgeTags,
                    onValueChange = { knowledgeTags = it },
                    label = "Etiketat (të ndara me presje)",
                    modifier = Modifier.fillMaxWidth(),
                )
                if (editDocument != null) {
                    EtaPreference(
                        title = "Fshi këtë njohuri",
                        summary = "Fshirja sinkronizohet me pajisjet e tjera",
                        onClick = { showKnowledgeDelete = true },
                    )
                }
            }
            MiuixDialogActions(
                modifier = Modifier.padding(top = 12.dp),
                confirmText = "Ruaj",
                confirmEnabled = knowledgeTitle.isNotBlank() &&
                    knowledgeContent.isNotBlank() && knowledgeContent.length <= 12_000,
                onCancel = { showKnowledgeEditor = false },
                onConfirm = {
                    onAction(AgentMemoryAction.SaveKnowledge(
                        title = knowledgeTitle,
                        content = knowledgeContent,
                        kind = editDocument?.kind ?: "reference",
                        source = knowledgeSource,
                        tags = knowledgeTags.split(',').map(String::trim)
                            .filter(String::isNotBlank).take(12),
                        id = editDocument?.id,
                        revision = editDocument?.revision,
                    ))
                    showKnowledgeEditor = false
                },
            )
        }
    }

    EtaWindowDialog(
        show = showKnowledgeDelete,
        title = "Fshi njohurinë",
        summary = "Pas konfirmimit shënohet për fshirje dhe sinkronizohet me pajisjet e tjera.",
        onDismissRequest = { showKnowledgeDelete = false },
    ) {
        MiuixDialogActions(
            confirmText = "Fshi",
            destructive = true,
            onCancel = { showKnowledgeDelete = false },
            onConfirm = {
                editDocument?.let {
                    onAction(AgentMemoryAction.DeleteKnowledge(it.id, it.revision))
                }
                showKnowledgeDelete = false
                showKnowledgeEditor = false
            },
        )
    }

    state.notice?.let { notice ->
        EtaWindowDialog(
            show = true,
            title = stringResource(R.string.ui_memory_b55ff5),
            summary = notice,
            onDismissRequest = { onAction(AgentMemoryAction.DismissNotice) },
        ) {
            EtaTextButton(
                text = stringResource(R.string.ui_knew_cb63c6),
                onClick = { onAction(AgentMemoryAction.DismissNotice) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MemorySettingsGroup(
    state: AgentMemoryUiState,
    onAction: (AgentMemoryAction) -> Unit,
    onConfigureSync: () -> Unit,
    onDisconnectSync: () -> Unit,
    onOpenKnowledge: () -> Unit,
    onOpenRetained: () -> Unit,
    onConfigureSemanticModel: () -> Unit,
    onResolveMemoryConflict: () -> Unit,
) {
    EtaPreferenceGroup(
        modifier = Modifier
            .padding(horizontal = EtaPreferenceDefaults.SidePadding)
            .padding(top = 8.dp, bottom = EtaPreferenceDefaults.GroupSpacing)
            .heightIn(max = 310.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        EtaSwitchPreference(
            title = stringResource(R.string.ui_enable_memory_4b69b7),
            summary = stringResource(R.string.ui_after_closing_no_memory_will_be_injected_and_the_mod_db3d23),
            checked = state.enabled,
            enabled = !state.isLoading,
            onCheckedChange = { onAction(AgentMemoryAction.ToggleEnabled(it)) },
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaSwitchPreference(
            title = "Ruajtja automatike e përvojave të detyrave",
            summary = "Ruaj vetëm llojet e detyrave, emrat e mjeteve dhe kodet e gabimeve; jo bisedat, pamjet e ekranit apo argumentet. Çaktivizuar si parazgjedhje.",
            checked = state.autoExperienceEnabled,
            enabled = state.enabled && !state.isLoading,
            onCheckedChange = { onAction(AgentMemoryAction.ToggleAutoExperience(it)) },
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaSwitchPreference(
            title = "Kujtesë inteligjente · Retain",
            summary = "Përdor veçmas modelin aktual për kujtime afatgjata me prova. Rrit kohën dhe koston; çaktivizuar si parazgjedhje.",
            checked = state.smartRetainEnabled,
            enabled = state.enabled && !state.isLoading,
            onCheckedChange = { onAction(AgentMemoryAction.ToggleSmartRetain(it)) },
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaPreference(
            title = "Kujtesa e strukturuar (${state.retainedNodes.size}  elemente )",
            summary = "Shfaq fakte semantike, përvoja, hapa dhe sugjerime lidhjesh; mund t'i fshish individualisht",
            enabled = !state.isLoading,
            onClick = onOpenRetained,
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaSwitchPreference(
            title = "Kërkimi semantik me modelin e bisedës",
            summary = "Përdor modelin aktual për renditje semantike në MEMORY.md, njohuri dhe nyjet Retain, pa model të veçantë Embeddings",
            checked = state.semanticMemoryEnabled,
            enabled = state.enabled && !state.isLoading,
            onCheckedChange = { onAction(AgentMemoryAction.ToggleSemanticRecall(it)) },
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaPreference(
            title = "Gjendja e kërkimit semantik",
            summary = state.semanticRetrievalReport ?: (
                "Pjesë të kërkueshme të MEMORY.md:${state.semanticCoreChunks}  elemente，Kujtesë e strukturuar:" +
                    "${state.retainedNodes.count { it.active }}  elemente；Ende nuk ka kërkime"
                ),
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaPreference(
            title = if (state.chatRetrievalTestBusy) "Po testohet modeli i bisedës…" else "Testo kërkimin me modelin aktual",
            summary = state.chatRetrievalTestStatus
                ?: "Kërko renditje semantike nga modeli aktual pa konfiguruar Embeddings",
            enabled = state.semanticMemoryEnabled && !state.chatRetrievalTestBusy,
            onClick = { onAction(AgentMemoryAction.TestChatSemanticRetrieval) },
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaSwitchPreference(
            title = "Të avancuara: Embeddings i veçantë",
            summary = "Çaktivizuar si parazgjedhje; aktivizo vetëm nëse ofruesi mbështet Embeddings. Kërkon thirrje shtesë.",
            checked = state.semanticUseEmbeddings,
            enabled = state.semanticMemoryEnabled && state.enabled && !state.isLoading,
            onCheckedChange = { onAction(AgentMemoryAction.ToggleEmbeddingAccelerator(it)) },
        )
        if (state.semanticUseEmbeddings) {
            EtaPreferenceDivider(hasLeading = false)
            EtaPreference(
                title = "Modeli Embeddings:${state.semanticModel}",
                summary = "Zgjidh një model Embeddings të veçantë nga modeli i bisedës",
                onClick = onConfigureSemanticModel,
            )
            EtaPreferenceDivider(hasLeading = false)
            EtaPreference(
                title = if (state.embeddingTestBusy) "Po testohet API-ja…" else "Testo API-n Embeddings",
                summary = state.embeddingTestStatus ?: "Testo API-n Embeddings të ofruesit aktual për të marrë dimensionin e vektorit ose gabimin",
                enabled = !state.embeddingTestBusy,
                onClick = { onAction(AgentMemoryAction.TestEmbeddingProvider) },
            )
            EtaPreferenceDivider(hasLeading = false)
            EtaPreference(
                title = if (state.semanticIndexBusy) "Po krijohen vektorët…" else "Krijo / riparo indeksin vektorial lokal",
                summary = state.semanticIndexStatus ?: (
                    "Kujtesë e strukturuar:${state.retainedNodes.count { it.active }}  elemente；Janë krijuar:${state.semanticIndexedNodes}  elemente。" +
                        if (state.retainedNodes.none { it.active }) {
                            "Indeksi nuk nevojitet tani; modeli mund të kërkojë në MEMORY.md"
                        } else "Prek për të krijuar indeksin"
                    ),
                enabled = state.semanticMemoryEnabled && !state.semanticIndexBusy &&
                    state.retainedNodes.any { it.active },
                onClick = { onAction(AgentMemoryAction.BuildSemanticIndex) },
            )
        }
        EtaPreferenceDivider(hasLeading = false)
        EtaSwitchPreference(
            title = "Rikujtimi i lidhjeve të kujtesës",
            summary = "Modeli sugjeron lidhje; lidhjet e konfirmuara zgjerohen deri në dy hapa, të pashqyrtuarat vetëm një",
            checked = state.graphMemoryEnabled,
            enabled = state.enabled && !state.isLoading,
            onCheckedChange = { onAction(AgentMemoryAction.ToggleGraphRecall(it)) },
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaPreference(
            title = if (state.graphClassifierBusy) "Po analizohen lidhjet…" else "Analizo lidhjet ekzistuese të kujtesës",
            summary = state.graphClassifierStatus ?: "Riklasifiko deri në 12 kujtime të fundit (thirrje shtesë modeli dhe shqyrtim manual)",
            enabled = state.graphMemoryEnabled && !state.graphClassifierBusy && state.retainedNodes.isNotEmpty(),
            onClick = { onAction(AgentMemoryAction.ClassifyExistingRelations) },
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaPreference(
            title = "Baza e njohurive (${state.knowledgeDocuments.size}  elemente )",
            summary = "Kërko, shfaq dhe ndrysho njohuritë e ruajtura lokalisht",
            enabled = !state.isLoading,
            onClick = onOpenKnowledge,
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaPreference(
            title = "Sinkronizim i enkriptuar WebDAV",
            summary = state.syncEndpoint.ifBlank { "Nuk është konfiguruar · lidhet me çdo server HTTPS WebDAV" },
            enabled = !state.isLoading,
            onClick = onConfigureSync,
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaPreference(
            title = if (state.syncBusy) "Po sinkronizohet…" else "Sinkronizo njohuritë dhe kujtesën",
            summary = state.syncStatus ?: "Bashko njohuritë, kujtesën kryesore dhe përvojat në pajisje të ndryshme",
            enabled = state.syncHasPassphrase && !state.syncBusy,
            onClick = { onAction(AgentMemoryAction.RunWebDavSync) },
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaSwitchPreference(
            title = "Sinkronizimi automatik në sfond",
            summary = "Android e planifikon afërsisht çdo 15 minuta; kursimi i baterisë mund ta ndryshojë",
            checked = state.syncAutoEnabled,
            enabled = state.syncHasPassphrase && !state.syncBusy,
            onCheckedChange = { onAction(AgentMemoryAction.ToggleWebDavAutoSync(it)) },
        )
        if (state.syncMemoryConflict) {
            EtaPreferenceDivider(hasLeading = false)
            EtaPreference(
                title = "Zgjidh konfliktin e MEMORY.md mes pajisjeve",
                summary = "Shqyrto kopjen në konflikt, bashko përmbajtjen lokalisht dhe konfirmo versionin lokal",
                enabled = !state.syncBusy,
                onClick = onResolveMemoryConflict,
            )
        }
        if (state.syncEndpoint.isNotBlank()) {
            EtaPreferenceDivider(hasLeading = false)
            EtaPreference(
                title = "Shkëput lidhjen WebDAV",
                summary = "Hiq vetëm konfigurimin lokal; mos fshi skedarët në server",
                enabled = !state.syncBusy,
                onClick = onDisconnectSync,
            )
        }
        EtaPreferenceDivider(hasLeading = false)
        EtaPreference(
            title = "Përvoja të ruajtura:${state.experienceCount}  elemente",
            summary = "Prek për të pastruar përvojat automatike (MEMORY.md ruhet)",
            onClick = { onAction(AgentMemoryAction.ClearExperiences) },
        )
        EtaPreferenceDivider(hasLeading = false)
        EtaPreference(
            title = stringResource(R.string.ui_core_memory_injection_budget_48b5d5),
            summary = stringResource(R.string.memory_budget_summary, formatNumber(state.coreBudgetChars)),
        )
    }
}

/** 文档卡片：Titulli栏承载文件名、体积与预览/编辑切换，正文区占满卡片剩余高度并独立滚动。 */
@Composable
private fun MemoryDocumentCard(
    state: AgentMemoryUiState,
    mode: MemoryViewMode,
    onModeChange: (MemoryViewMode) -> Unit,
    onDraftChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    val overLimit = state.draftBytes > state.maxBytes
    EtaCard(modifier = modifier.fillMaxWidth(), insideMargin = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Notes,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.primary,
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MEMORY.md",
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                )
                Text(
                    text = "${formatBytes(state.draftBytes)} / ${formatBytes(state.maxBytes)}",
                    style = MiuixTheme.textStyles.footnote2,
                    color = if (overLimit) colors.error else colors.onSurfaceVariantSummary,
                )
            }
            MemoryModeSwitch(mode = mode, enabled = !state.isLoading, onModeChange = onModeChange)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.dividerLine),
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    InfiniteProgressIndicator(size = 24.dp)
                }
                else -> Crossfade(targetState = mode, label = "memory_mode") { current ->
                    when (current) {
                        MemoryViewMode.Preview -> MemoryPreview(
                            content = state.draft,
                            onStartEditing = { onModeChange(MemoryViewMode.Edit) },
                        )
                        MemoryViewMode.Edit -> MemoryEditor(
                            value = state.draft,
                            enabled = !state.isSaving,
                            onValueChange = onDraftChange,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryPreview(content: String, onStartEditing: () -> Unit) {
    if (content.isBlank()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onStartEditing)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.memory_empty_title),
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.ui_core_memory_user_name_long_term_preferences_aa6ff9),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        return
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        StaticMarkdown(content = content, tone = MarkdownTone.Answer, modifier = Modifier.fillMaxWidth())
    }
}

/** 无边框整页编辑：与卡片同色，不再嵌一层灰色输入框；点到空白处也能把光标落进正文。 */
@Composable
private fun MemoryEditor(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = null, indication = null) { focusRequester.requestFocus() }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        if (value.isEmpty()) {
            Text(
                text = stringResource(R.string.ui_core_memory_user_name_long_term_preferences_aa6ff9),
                style = MemoryTextStyle(),
                color = colors.onSurfaceVariantSummary,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = MemoryTextStyle().copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
    }
}

@Composable
private fun MemoryTextStyle() = MiuixTheme.textStyles.body2.copy(lineHeight = 22.sp)

/** 轻量分段切换：两个选项常驻可见，选中项用浮起的胶囊表示，比整行 TabRow 更贴合卡片Titulli栏。 */
@Composable
private fun MemoryModeSwitch(
    mode: MemoryViewMode,
    enabled: Boolean,
    onModeChange: (MemoryViewMode) -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    Row(
        modifier = Modifier
            .squircleSurface(color = colors.surfaceContainerHigh, cornerRadius = 12.dp)
            .padding(3.dp),
    ) {
        MemoryViewMode.entries.forEach { option ->
            val selected = option == mode
            val background by animateColorAsState(
                if (selected) colors.surface else colors.surfaceContainerHigh,
                label = "memory_mode_tab",
            )
            Box(
                modifier = Modifier
                    .squircleSurface(color = background, cornerRadius = 9.dp)
                    .clickable(enabled = enabled && !selected) { onModeChange(option) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        if (option == MemoryViewMode.Preview) R.string.memory_mode_preview else R.string.memory_mode_edit,
                    ),
                    style = MiuixTheme.textStyles.footnote1,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    color = if (selected) colors.onSurface else colors.onSurfaceVariantSummary,
                )
            }
        }
    }
}

/** 有未Ruaj修改或超限时才出现：左侧说明状态，右侧放弃与Ruaj，避免常驻一个灰掉的Ruaj按钮。 */
@Composable
private fun MemorySaveBar(
    state: AgentMemoryUiState,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
) {
    val overLimit = state.draftBytes > state.maxBytes
    AnimatedVisibility(
        visible = !state.isLoading && (state.hasUnsavedChanges || state.isSaving),
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EtaPreferenceDefaults.SidePadding)
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (overLimit) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.primary,
                            CircleShape,
                        ),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(if (overLimit) R.string.memory_over_limit else R.string.memory_unsaved_changes),
                    style = MiuixTheme.textStyles.footnote1,
                    color = if (overLimit) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 2,
                )
            }
            EtaTextButton(
                text = stringResource(R.string.memory_discard),
                enabled = !state.isSaving,
                onClick = onDiscard,
                minHeight = 40.dp,
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            )
            EtaTextButton(
                text = stringResource(if (state.isSaving) R.string.memory_saving else R.string.memory_save),
                enabled = state.canSave,
                onClick = onSave,
                minHeight = 40.dp,
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

private fun formatBytes(bytes: Int): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_024 * 1_024 -> "%.1f KiB".format(bytes / 1_024.0)
    bytes % (1_024 * 1_024) == 0 -> "${bytes / (1_024 * 1_024)} MiB"
    else -> "%.2f MiB".format(bytes / (1_024.0 * 1_024.0))
}

private fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance().format(value)
