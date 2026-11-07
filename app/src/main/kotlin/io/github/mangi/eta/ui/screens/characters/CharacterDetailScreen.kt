package io.github.mangi.eta.ui.screens.characters

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.mangi.eta.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.agent.roleplay.CharacterCardFormat
import io.github.mangi.eta.agent.roleplay.CharacterMacros
import io.github.mangi.eta.agent.roleplay.RoleplayBinding
import io.github.mangi.eta.ui.app.CharacterLibraryStore
import io.github.mangi.eta.ui.components.EtaArrowPreference
import io.github.mangi.eta.ui.components.EtaPreferenceDivider
import io.github.mangi.eta.ui.components.EtaPreferenceGroup
import io.github.mangi.eta.ui.components.EtaPreferenceGroupTitle
import io.github.mangi.eta.ui.components.EtaSwitchPreference
import io.github.mangi.eta.ui.components.EtaWindowDialog
import io.github.mangi.eta.ui.components.ItemDescriptionDialog
import io.github.mangi.eta.ui.components.MiuixDialogActions
import io.github.mangi.eta.ui.components.MiuixScaffoldPage
import io.github.mangi.eta.ui.navigation.AppRoute
import top.yukonga.miuix.kmp.basic.BasicComponentColors
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme


internal fun greetingTitle(index: Int) = if (index == 0) "默认开场白" else "开场白 ${index + 1}"

private data class CharacterTextPreview(val title: String, val text: String)

@Composable
internal fun CharacterDetailScreen(
    id: String,
    store: CharacterLibraryStore,
    onNavigate: (AppRoute) -> Unit,
    onBack: () -> Unit,
    onStart: (RoleplayBinding, String) -> Unit,
) {
    var showCompatibility by rememberSaveable(id) { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable(id) { mutableStateOf(false) }
    var preview by remember(id) { mutableStateOf<CharacterTextPreview?>(null) }
    val pngExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) {
        if (it != null) store.export(id, CharacterCardFormat.PNG, it)
    }
    val jsonExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        if (it != null) store.export(id, CharacterCardFormat.JSON, it)
    }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) store.changeAvatar(id, uri)
    }
    val context = LocalContext.current
    val roleTitle = stringResource(R.string.character_description_title)
    val genericUser = stringResource(R.string.character_user_name)
    val unsetNickname = stringResource(R.string.character_no_nickname)
    val fallbackFileName = stringResource(R.string.character_export_default_name)
    val profile = store.selected?.takeIf { it.id == id }
    MiuixScaffoldPage(title = "", onBack = onBack, compactTitle = true) {
        if (profile == null) {
            item { CharacterPageMessage(if (store.busy) stringResource(R.string.character_detail_loading) else stringResource(R.string.character_detail_load_failed)) }
            return@MiuixScaffoldPage
        }
        val userName = if (store.usePersona) store.persona.name.ifBlank { genericUser } else genericUser
        item(key = "hero") {
            CharacterHero(
                profile = profile,
                busy = store.busy,
                onChangeAvatar = { avatarPicker.launch("image/*") },
                onToggleFavorite = { store.toggleFavorite(id) },
                onStart = { store.startConversation(id, onStart) },
                onReadDescription = {
                    preview = CharacterTextPreview(
                        roleTitle,
                        CharacterMacros.expand(profile.card.description, profile.card, userName),
                    )
                },
                userName = userName,
            )
        }
        item(key = "persona") {
            EtaPreferenceGroup {
                EtaSwitchPreference(
                    title = stringResource(R.string.character_use_persona),
                    summary = store.persona.name.ifBlank { unsetNickname },
                    checked = store.usePersona,
                    onCheckedChange = { store.usePersona = it },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(title = stringResource(R.string.character_edit_persona), onClick = { onNavigate(AppRoute.CharacterPersona) })
            }
        }
        item(key = "greetings-title") { EtaPreferenceGroupTitle(stringResource(R.string.character_greeting_title)) }
        item(key = "greetings") {
            // 预览与开始对话后看到的文本一致：宏按当前是否使用人设展开。
            val greetings = remember(profile.card, userName) {
                (listOf(profile.card.firstMessage) + profile.card.alternateGreetings)
                    .map { CharacterMacros.expand(it, profile.card, userName) }
            }
            GreetingPager(
                greetings = greetings,
                selected = store.greetingIndex,
                onSelect = { store.greetingIndex = it },
                onRead = { index ->
                    val title = if (index == 0) context.getString(R.string.character_default_greeting)
                        else context.getString(R.string.character_alt_greeting_number, index + 1)
                    preview = CharacterTextPreview(title, greetings[index])
                },
            )
        }
        item(key = "management-title") { EtaPreferenceGroupTitle(stringResource(R.string.character_management)) }
        item(key = "management") {
            EtaPreferenceGroup {
                EtaArrowPreference(title = stringResource(R.string.character_edit), enabled = !store.busy, onClick = {
                    store.discardEditor()
                    onNavigate(AppRoute.CharacterEditor(id))
                })
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(title = stringResource(R.string.character_story_memory), summary = stringResource(R.string.character_story_memory_summary), onClick = {
                    onNavigate(AppRoute.CharacterMemory(id))
                })
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(title = stringResource(R.string.character_duplicate), enabled = !store.busy, onClick = {
                    store.duplicate(id) { onNavigate(AppRoute.CharacterDetail(it)) }
                })
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = stringResource(R.string.character_export_png),
                    enabled = !store.busy,
                    onClick = { pngExporter.launch(characterExportName(profile.card.name, "png", fallbackFileName)) },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = stringResource(R.string.character_export_json),
                    enabled = !store.busy,
                    onClick = { jsonExporter.launch(characterExportName(profile.card.name, "json", fallbackFileName)) },
                )
            }
        }
        if (store.compatibilityWarnings.isNotEmpty()) {
            item(key = "compatibility") {
                EtaPreferenceGroup {
                    EtaArrowPreference(
                        title = stringResource(R.string.character_compatibility_title),
                        summary = stringResource(R.string.character_compatibility_count, store.compatibilityWarnings.size),
                        onClick = { showCompatibility = !showCompatibility },
                    )
                    if (showCompatibility) {
                        store.compatibilityWarnings.forEach { warning ->
                            Text(
                                text = warning,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
        }
        // 破坏性操作单独成组并放在页面末尾，与日常管理操作拉开距离。
        item(key = "danger") {
            EtaPreferenceGroup {
                EtaArrowPreference(
                    title = stringResource(R.string.character_delete),
                    titleColor = BasicComponentColors(
                        color = MiuixTheme.colorScheme.error,
                        disabledColor = MiuixTheme.colorScheme.disabledOnSurface,
                    ),
                    enabled = !store.busy,
                    onClick = { showDeleteConfirm = true },
                )
            }
        }
    }

    if (showDeleteConfirm && profile != null) {
        EtaWindowDialog(
            show = true,
            title = stringResource(R.string.character_delete),
            summary = stringResource(R.string.character_delete_summary, profile.card.name),
            onDismissRequest = { showDeleteConfirm = false },
        ) {
            MiuixDialogActions(
                confirmText = stringResource(R.string.character_confirm_delete),
                destructive = true,
                confirmEnabled = !store.busy,
                onCancel = { showDeleteConfirm = false },
                onConfirm = {
                    showDeleteConfirm = false
                    store.delete(id) { onBack() }
                },
            )
        }
    }

    preview?.let { current ->
        ItemDescriptionDialog(
            title = current.title,
            description = current.text,
            onDismiss = { preview = null },
        )
    }
}

private fun characterExportName(name: String, extension: String, fallbackName: String): String {
    val basename = name.map { if (it.isISOControl() || it in "\\/:*?\"<>|") '_' else it }
        .joinToString("").trim().trimEnd('.').take(64).ifBlank { fallbackName }
    return "$basename.$extension"
}
