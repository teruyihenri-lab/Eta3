package io.github.mangi.eta.ui.pages.providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SearchOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.data.provider.CommunityCatalog
import io.github.mangi.eta.data.provider.CommunityCatalogProvider
import io.github.mangi.eta.data.provider.CommunityCatalogSource
import io.github.mangi.eta.ui.app.CommunityCatalogStore
import io.github.mangi.eta.ui.components.EtaCard
import io.github.mangi.eta.ui.components.EtaCheckboxPreference
import io.github.mangi.eta.ui.components.EtaPreference
import io.github.mangi.eta.ui.components.EtaPreferenceDivider
import io.github.mangi.eta.ui.components.EtaPreferenceGroupItem
import io.github.mangi.eta.ui.components.EtaPreferenceGroupTitle
import io.github.mangi.eta.ui.components.EtaTextButton
import io.github.mangi.eta.ui.components.ListEmptyState
import io.github.mangi.eta.ui.components.MiuixScaffoldPage
import io.github.mangi.eta.ui.model.formatCompactTokenCount
import io.github.mangi.eta.ui.navigation.AppRoute
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
internal fun CommunityCatalogScreen(
    store: CommunityCatalogStore,
    onNavigate: (AppRoute) -> Unit,
    onBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val catalog = store.catalog
    val providers = remember(catalog, query) {
        val term = query.trim()
        catalog?.providers.orEmpty().filter { provider ->
            term.isEmpty() ||
                provider.name.contains(term, ignoreCase = true) ||
                provider.id.contains(term, ignoreCase = true) ||
                provider.baseUrl.contains(term, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) { store.openCatalog() }

    MiuixScaffoldPage(
        title = "Shto nga katalogu",
        onBack = onBack,
        actions = {
            IconButton(
                onClick = store::refresh,
                enabled = !store.loading && !store.refreshing,
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Rifresko katalogun e modeleve")
            }
        },
    ) {
        item(key = "search") {
            InputField(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                label = "Kërko ofrues",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        item(key = "catalog_status") {
            ProviderSection(title = "Gjendja e katalogut") {
                EtaPreference(
                    title = "Katalogu i komunitetit models.dev",
                    summary = "Leximi i katalogut nuk ngarkon çelësat API të Eta-s",
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaPreference(
                    title = "Modelet janë sugjerime nga komuniteti",
                    summary = "Përdorimi varet nga lejet e llogarisë dhe API-ja e ofruesit",
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaPreference(
                    title = catalog?.sourceLabel() ?: if (store.loading) "Po lexohet katalogu lokal" else "Katalogu nuk është i disponueshëm",
                    summary = catalog?.let(::catalogSummary),
                )
                if (store.refreshing) {
                    EtaPreferenceDivider(hasLeading = false)
                    EtaPreference(title = "Po përditësohet katalogu online")
                }
                store.refreshError?.let { error ->
                    EtaPreferenceDivider(hasLeading = false)
                    EtaPreference(
                        title = "Përditësimi online dështoi",
                        summary = if (catalog != null) "$error; katalogu aktual mbetet i përdorshëm" else error,
                    )
                }
                if (store.loadError && catalog == null) {
                    EtaPreferenceDivider(hasLeading = false)
                    EtaPreference(title = "Katalogu lokal nuk u lexua", summary = "Provo përsëri ose kontrollo instalimin e aplikacionit")
                }
            }
        }

        item(key = "results_title") {
            EtaPreferenceGroupTitle("Ofrues të disponueshëm për import (${providers.size})")
        }
        if (catalog == null && store.loading) {
            item(key = "loading") { CatalogLoadingState() }
        } else if (providers.isEmpty()) {
            item(key = "empty") {
                ListEmptyState(
                    title = if (catalog == null) "Nuk ka katalog të disponueshëm" else "Nuk u gjet ofrues që përputhet",
                    summary = if (catalog == null) "Prek Rifresko lart djathtas dhe provo përsëri" else "Provo emër ose adresë tjetër",
                    icon = if (catalog == null) Icons.Rounded.CloudOff else Icons.Rounded.SearchOff,
                )
            }
        } else {
            items(providers, key = { "provider:${it.id}" }, contentType = { "provider" }) { provider ->
                EtaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = { onNavigate(AppRoute.CommunityCatalogProvider(provider.id)) },
                ) {
                    Text(
                        text = provider.name,
                        style = MiuixTheme.textStyles.body1,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = provider.baseUrl,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${provider.models.size} modele të disponueshme për import",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CommunityCatalogProviderScreen(
    catalogId: String,
    store: CommunityCatalogStore,
    onImported: (String) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var query by rememberSaveable(catalogId) { mutableStateOf("") }
    var selectedIds by rememberSaveable(catalogId) { mutableStateOf(arrayListOf<String>()) }
    var importError by remember { mutableStateOf<String?>(null) }
    var importInFlight by remember { mutableStateOf(false) }
    var selection by remember(catalogId) { mutableStateOf<CommunityCatalogProvider?>(null) }
    val provider = selection ?: store.catalog?.providers?.firstOrNull { it.id == catalogId }
    val filteredModels = remember(provider, query) {
        val term = query.trim()
        provider?.models.orEmpty().filter { model ->
            term.isEmpty() ||
                model.displayName.contains(term, ignoreCase = true) ||
                model.modelId.contains(term, ignoreCase = true)
        }
    }

    LaunchedEffect(catalogId) { store.ensureLoaded() }
    LaunchedEffect(provider) {
        if (selection == null && provider != null) selection = provider
    }

    MiuixScaffoldPage(
        title = provider?.name ?: "Zgjidh modelin",
        onBack = onBack,
        actions = {
            EtaTextButton(
                text = "Importo (${selectedIds.size})",
                enabled = provider != null && selectedIds.isNotEmpty() && !importInFlight && !store.importing,
                onClick = {
                    val chosen = provider ?: return@EtaTextButton
                    if (importInFlight) return@EtaTextButton
                    importInFlight = true
                    importError = null
                    scope.launch {
                        try {
                            val addedId = store.importProvider(chosen, selectedIds.toSet())
                            onImported(addedId)
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: IllegalArgumentException) {
                            importError = "Përzgjedhja e modeleve ka skaduar. Zgjidhi përsëri"
                        } catch (_: Throwable) {
                            importError = "Ofruesi nuk u ruajt. Provo përsëri"
                        } finally {
                            importInFlight = false
                        }
                    }
                },
                insideMargin = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            )
        },
    ) {
        if (provider == null) {
            item(key = "missing") {
                if (store.loading) {
                    CatalogLoadingState()
                } else {
                    ListEmptyState(
                        title = "Ky ofrues nuk është në katalog",
                        summary = "Katalogu mund të ketë ndryshuar. Kthehu te lista dhe zgjidh përsëri",
                    )
                }
            }
            return@MiuixScaffoldPage
        }

        item(key = "provider") {
            ProviderSection(title = "Ofruesi") {
                EtaPreference(title = "Emri", summary = provider.name)
                EtaPreferenceDivider(hasLeading = false)
                EtaPreference(title = null) {
                    Text(
                        text = "Base URL",
                        style = MiuixTheme.textStyles.body1,
                        fontWeight = FontWeight.Medium,
                    )
                    SelectionContainer {
                        Text(
                            text = provider.baseUrl,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            softWrap = true,
                        )
                    }
                }
            }
        }
        item(key = "selection_status") {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
                Text(
                    text = "U zgjodhën ${selectedIds.size} nga ${provider.models.size} modele",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = "Pas importimit shto çelësin API dhe aktivizo ofruesin; kërkesat dërgohen te URL-ja bazë më sipër",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                importError?.let { error ->
                    Text(
                        text = error,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.error,
                    )
                }
            }
        }
        item(key = "model_search") {
            InputField(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                label = "Kërko emrin ose ID-në e modelit",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item(key = "model_actions") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                EtaTextButton(
                    text = "Zgjidh të gjitha rezultatet",
                    enabled = filteredModels.isNotEmpty() && !store.importing,
                    onClick = {
                        selectedIds = ArrayList((selectedIds + filteredModels.map { it.modelId }).distinct())
                    },
                )
                EtaTextButton(
                    text = "Pastro përzgjedhjen",
                    enabled = selectedIds.isNotEmpty() && !store.importing,
                    onClick = { selectedIds = arrayListOf() },
                )
            }
        }
        item(key = "models_title") {
            EtaPreferenceGroupTitle("Modelet (${filteredModels.size})")
        }
        if (filteredModels.isEmpty()) {
            item(key = "empty_models") {
                ListEmptyState(title = "Nuk u gjet model që përputhet")
            }
        } else {
            // 与提供商自己的模型页一致：逐项组合，画成一个连续分组。
            itemsIndexed(filteredModels, key = { _, model -> "model:${model.modelId}" }, contentType = { _, _ -> "model" }) { index, model ->
                EtaPreferenceGroupItem(
                    isFirst = index == 0,
                    isLast = index == filteredModels.lastIndex,
                ) {
                    EtaCheckboxPreference(
                        title = model.displayName,
                        summary = buildString {
                            append(model.modelId)
                            append(" · ")
                            append(formatCompactTokenCount(model.contextWindow))
                            append(" tokenë konteksti")
                            model.releaseDate?.let { append(" · "); append(it) }
                        },
                        checked = model.modelId in selectedIds,
                        enabled = !store.importing,
                        onCheckedChange = { checked ->
                            selectedIds = if (checked) {
                                ArrayList(selectedIds + model.modelId)
                            } else {
                                ArrayList(selectedIds.filterNot { it == model.modelId })
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogLoadingState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        InfiniteProgressIndicator(size = 28.dp)
    }
}

private fun CommunityCatalog.sourceLabel(): String = when (source) {
    CommunityCatalogSource.SNAPSHOT -> "Katalogu i integruar"
    CommunityCatalogSource.CACHE -> "Katalogu i ruajtur lokalisht"
    CommunityCatalogSource.ONLINE -> "Katalogu online"
}

private fun catalogSummary(catalog: CommunityCatalog): String {
    val updated = catalog.fetchedAt?.let { timestamp ->
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))
    }
    return listOfNotNull("${catalog.providers.size} ofrues për import", updated?.let { "Përditësuar më $it" })
        .joinToString(" · ")
}
