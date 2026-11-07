package io.github.mangi.eta.ui.screens.characters

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


internal fun greetingTitle(index: Int) = if (index == 0) "Përshëndetja fillestare" else "Përshëndetja ${index + 1}"

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
    val profile = store.selected?.takeIf { it.id == id }
    MiuixScaffoldPage(title = "", onBack = onBack, compactTitle = true) {
        if (profile == null) {
            item { CharacterPageMessage(if (store.busy) "Po lexohet personazhi…" else "Personazhi nuk u lexua. Kthehu dhe provo përsëri") }
            return@MiuixScaffoldPage
        }
        val userName = if (store.usePersona) store.persona.name.ifBlank { "Përdoruesi" } else "Përdoruesi"
        item(key = "hero") {
            CharacterHero(
                profile = profile,
                busy = store.busy,
                onChangeAvatar = { avatarPicker.launch("image/*") },
                onToggleFavorite = { store.toggleFavorite(id) },
                onStart = { store.startConversation(id, onStart) },
                onReadDescription = {
                    preview = CharacterTextPreview(
                        "Profili i personazhit",
                        CharacterMacros.expand(profile.card.description, profile.card, userName),
                    )
                },
                userName = userName,
            )
        }
        item(key = "persona") {
            EtaPreferenceGroup {
                EtaSwitchPreference(
                    title = "Përdor profilin tim",
                    summary = store.persona.name.ifBlank { "Nuk është vendosur emër" },
                    checked = store.usePersona,
                    onCheckedChange = { store.usePersona = it },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(title = "Ndrysho profilin tim", onClick = { onNavigate(AppRoute.CharacterPersona) })
            }
        }
        item(key = "greetings-title") { EtaPreferenceGroupTitle("Përshëndetja") }
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
                onRead = { index -> preview = CharacterTextPreview(greetingTitle(index), greetings[index]) },
            )
        }
        item(key = "management-title") { EtaPreferenceGroupTitle("Menaxhimi") }
        item(key = "management") {
            EtaPreferenceGroup {
                EtaArrowPreference(title = "Ndrysho personazhin", enabled = !store.busy, onClick = {
                    store.discardEditor()
                    onNavigate(AppRoute.CharacterEditor(id))
                })
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(title = "Kujtesa e historisë", summary = "Historia dhe marrëdhëniet ruhen në të gjitha bisedat e këtij personazhi", onClick = {
                    onNavigate(AppRoute.CharacterMemory(id))
                })
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(title = "Kopjo personazhin", enabled = !store.busy, onClick = {
                    store.duplicate(id) { onNavigate(AppRoute.CharacterDetail(it)) }
                })
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = "Eksporto kartën PNG",
                    enabled = !store.busy,
                    onClick = { pngExporter.launch(characterExportName(profile.card.name, "png")) },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = "Eksporto kartën JSON",
                    enabled = !store.busy,
                    onClick = { jsonExporter.launch(characterExportName(profile.card.name, "json")) },
                )
            }
        }
        if (store.compatibilityWarnings.isNotEmpty()) {
            item(key = "compatibility") {
                EtaPreferenceGroup {
                    EtaArrowPreference(
                        title = "Shënime për përputhshmërinë",
                        summary = "${store.compatibilityWarnings.size}  elemente trajtohen sipas përputhshmërisë",
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
        // 破坏性操作单独成组并放在页面末尾，与日常Menaxhimi操作拉开距离。
        item(key = "danger") {
            EtaPreferenceGroup {
                EtaArrowPreference(
                    title = "Fshi personazhin",
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
            title = "Fshi personazhin",
            summary = "「${profile.card.name}」do të hiqet nga biblioteka. Fotografia dhe kujtesa e historisë fshihen; bisedat ruhen. Ky veprim nuk mund të zhbëhet.",
            onDismissRequest = { showDeleteConfirm = false },
        ) {
            MiuixDialogActions(
                confirmText = "Fshi",
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

private fun characterExportName(name: String, extension: String): String {
    val basename = name.map { if (it.isISOControl() || it in "\\/:*?\"<>|") '_' else it }
        .joinToString("").trim().trimEnd('.').take(64).ifBlank { "Personazhi" }
    return "$basename.$extension"
}
