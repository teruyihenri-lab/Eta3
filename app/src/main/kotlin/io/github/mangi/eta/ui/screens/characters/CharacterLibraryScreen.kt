package io.github.mangi.eta.ui.screens.characters

import androidx.compose.ui.res.stringResource
import io.github.mangi.eta.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.agent.roleplay.CharacterProfile
import io.github.mangi.eta.ui.app.CharacterLibraryStore
import io.github.mangi.eta.ui.components.EtaArrowPreference
import io.github.mangi.eta.ui.components.EtaPreferenceColors
import io.github.mangi.eta.ui.components.EtaPreferenceGroup
import io.github.mangi.eta.ui.components.EtaPreferenceGroupTitle
import io.github.mangi.eta.ui.components.EtaPreferenceIcon
import io.github.mangi.eta.ui.components.EtaTextButton
import io.github.mangi.eta.ui.components.ListEmptyState
import io.github.mangi.eta.ui.components.MiuixScaffoldPage
import io.github.mangi.eta.ui.navigation.AppRoute
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val CharacterCardPadding = 16.dp
private val CharacterGridSpacing = 12.dp

@Composable
internal fun CharacterLibraryScreen(
    store: CharacterLibraryStore,
    onNavigate: (AppRoute) -> Unit,
    onBack: () -> Unit,
) {
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) store.importCard(uri) { onNavigate(AppRoute.CharacterDetail(it)) }
    }
    val importCard = {
        importer.launch(arrayOf("image/png", "application/json", "text/plain", "application/octet-stream"))
    }
    val createCharacter = {
        store.discardEditor()
        onNavigate(AppRoute.CharacterEditor())
    }
    MiuixScaffoldPage(
        title = stringResource(R.string.character_library_title),
        onBack = onBack,
        actions = {
            IconButton(onClick = importCard, enabled = !store.busy) {
                Icon(Icons.Rounded.FileUpload, contentDescription = stringResource(R.string.character_import_card))
            }
            IconButton(onClick = createCharacter, enabled = !store.busy) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.character_create))
            }
        },
    ) {
        item(key = "search") {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CharacterCardPadding)
                    .padding(top = 4.dp, bottom = 12.dp),
                // 去掉组件自带的横向内边距，让搜索框与下方卡片左右对齐。
                insideMargin = DpSize(0.dp, 0.dp),
                expanded = false,
                onExpandedChange = {},
                inputField = {
                    InputField(
                        query = store.query,
                        onQueryChange = { store.query = it },
                        onSearch = { store.query = it },
                        expanded = false,
                        onExpandedChange = {},
                        label = stringResource(R.string.character_search),
                    )
                },
                content = {},
            )
        }
        when {
            store.busy && store.characters.isEmpty() -> {
                item(key = "loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        InfiniteProgressIndicator(size = 28.dp)
                    }
                }
            }
            store.characters.isEmpty() -> {
                item(key = "empty-library") {
                    ListEmptyState(
                        title = stringResource(R.string.character_library_empty),
                        summary = stringResource(R.string.character_library_empty_summary),
                        icon = Icons.Rounded.TheaterComedy,
                        action = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                EtaTextButton(
                                    text = stringResource(R.string.character_restore_defaults),
                                    onClick = { store.restoreDefaultCharacter() },
                                    enabled = !store.busy,
                                )
                                EtaTextButton(
                                    text = stringResource(R.string.character_create),
                                    onClick = createCharacter,
                                    colors = ButtonDefaults.textButtonColorsPrimary(),
                                )
                            }
                        },
                    )
                }
            }
            store.filteredCharacters.isEmpty() -> {
                item(key = "empty-search") {
                    ListEmptyState(
                        title = stringResource(R.string.character_no_matches),
                        summary = stringResource(R.string.character_no_matches_summary),
                        icon = Icons.Rounded.SearchOff,
                    )
                }
            }
        }
        // 两列竖版卡片：每行一个 Lazy 项，长列表仍按行组合。未搜索时末尾追加一张新建卡，
        // 既补齐单数时的空位，也让创建与导入在网格中就近可达。
        val showAddTile = store.query.isBlank() && store.characters.isNotEmpty()
        val tiles: List<CharacterProfile?> = store.filteredCharacters + if (showAddTile) listOf(null) else emptyList()
        val rows = tiles.chunked(2)
        items(rows, key = { row -> row.joinToString("|") { it?.id ?: "add" } }) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CharacterCardPadding)
                    .padding(bottom = CharacterGridSpacing),
                horizontalArrangement = Arrangement.spacedBy(CharacterGridSpacing),
            ) {
                row.forEach { profile ->
                    if (profile == null) {
                        CharacterAddTile(
                            enabled = !store.busy,
                            onCreate = createCharacter,
                            onImport = importCard,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        CharacterPortraitCard(
                            profile = profile,
                            onClick = { if (!store.busy) onNavigate(AppRoute.CharacterDetail(profile.id)) },
                            onToggleFavorite = { store.toggleFavorite(profile.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item(key = "persona-title") { EtaPreferenceGroupTitle(stringResource(R.string.character_my_section)) }
        item(key = "persona") {
            EtaPreferenceGroup {
                EtaArrowPreference(
                    title = stringResource(R.string.character_persona_title),
                    startAction = { EtaPreferenceIcon(Icons.Rounded.Face, tint = EtaPreferenceColors.Blue) },
                    summary = stringResource(R.string.character_persona_summary),
                    onClick = { onNavigate(AppRoute.CharacterPersona) },
                )
            }
        }
    }
}

@Composable
internal fun CharacterPageMessage(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.body2,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}
