package io.github.mangi.eta.ui.screens.characters

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.agent.roleplay.CharacterBookEntryDraft
import io.github.mangi.eta.agent.roleplay.CharacterWorldbook
import io.github.mangi.eta.ui.app.CharacterLibraryStore
import io.github.mangi.eta.ui.components.EtaArrowPreference
import io.github.mangi.eta.ui.components.EtaCard
import io.github.mangi.eta.ui.components.EtaPreferenceGroup
import io.github.mangi.eta.ui.components.EtaPreferenceGroupTitle
import io.github.mangi.eta.ui.components.EtaSwitch
import io.github.mangi.eta.ui.components.EtaSwitchPreference
import io.github.mangi.eta.ui.components.EtaTextButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal fun LazyListScope.characterWorldbookEditor(
    store: CharacterLibraryStore,
    expanded: Boolean,
    expandedEntry: Int?,
    onToggleExpanded: () -> Unit,
    onExpandEntry: (Int?) -> Unit,
) {
    val card = store.draft ?: return
    val book = card.worldbookDraft()
    val enabled = !store.busy
    item(key = "worldbook-title") { EtaPreferenceGroupTitle("Libri i botës") }
    item(key = "worldbook-header") {
        EtaPreferenceGroup(modifier = Modifier.padding(horizontal = CharacterCardPadding)) {
            EtaArrowPreference(
                title = "Libri i botës i integruar",
                summary = "${book.entries.size}  Hyrje · ${if (expanded) "Mblidh" else "Zgjero redaktimin"}",
                onClick = onToggleExpanded,
            )
        }
    }
    if (!expanded) return
    item(key = "worldbook-name") {
        CharacterTextField("Emri i librit të botës", book.name, { value -> store.updateWorldbook { it.copy(name = value) } }, enabled, singleLine = true)
    }
    item(key = "worldbook-depth") {
        CharacterTextField("Mesazhet e fundit për skanim (bosh = parazgjedhje)", book.scanDepth?.toString().orEmpty(), { value ->
            optionalNonNegativeInt(value) { number -> store.updateWorldbook { it.copy(scanDepth = number) } }
        }, enabled, singleLine = true)
    }
    item(key = "worldbook-budget") {
        CharacterTextField("Kufiri i tokenëve (bosh = automatik)", book.tokenBudget?.toString().orEmpty(), { value ->
            optionalNonNegativeInt(value) { number -> store.updateWorldbook { it.copy(tokenBudget = number) } }
        }, enabled, singleLine = true)
    }
    item(key = "worldbook-recursive") {
        EtaPreferenceGroup(modifier = Modifier.padding(horizontal = CharacterCardPadding, vertical = 8.dp)) {
            EtaSwitchPreference(
                title = "Përputhje rekursive",
                summary = "Përdor përmbajtjen e përputhur për të gjetur hyrje të tjera",
                checked = book.recursiveScanning == true,
                enabled = enabled,
                onCheckedChange = { value -> store.updateWorldbook { it.copy(recursiveScanning = value) } },
            )
        }
    }
    item(key = "worldbook-entries-title") { EtaPreferenceGroupTitle("Hyrje") }
    val unsupported = CharacterWorldbook.unsupportedEntries(card).associate { it.index to it.reasons }
    itemsIndexed(book.entries, key = { index, _ -> "worldbook-entry-$index" }) { index, entry ->
        EtaPreferenceGroup(
            modifier = Modifier
                .padding(horizontal = CharacterCardPadding, vertical = 8.dp),
        ) {
            EtaArrowPreference(
                title = entry.name.take(120).ifBlank { "Hyrje ${index + 1}" },
                summary = when {
                    !entry.enabled -> "Çaktivizuar"
                    !unsupported[index].isNullOrEmpty() -> "U anashkalua:${unsupported[index].orEmpty().joinToString("；")}"
                    entry.constant -> "Përfshi gjithmonë në kontekst"
                    else -> entry.keys.take(4).joinToString("、").take(160).ifBlank { "Nuk janë vendosur fjalë kyçe" }
                },
                onClick = { onExpandEntry(if (expandedEntry == index) null else index) },
                endActions = {
                    EtaSwitch(
                        checked = entry.enabled,
                        enabled = enabled,
                        onCheckedChange = { value ->
                            store.updateWorldbook { current ->
                                current.copy(entries = current.entries.toMutableList().apply { this[index] = entry.copy(enabled = value) })
                            }
                        },
                    )
                },
            )
            if (expandedEntry == index) {
                CharacterWorldbookEntryEditor(
                    entry = entry,
                    enabled = enabled,
                    unsupported = unsupported[index].orEmpty(),
                    onChange = { replacement ->
                        store.updateWorldbook { current -> current.copy(entries = current.entries.toMutableList().apply { this[index] = replacement }) }
                    },
                    onDelete = {
                        onExpandEntry(null)
                        store.updateWorldbook { current -> current.copy(entries = current.entries.filterIndexed { i, _ -> i != index }) }
                    },
                )
            }
        }
    }
    item(key = "worldbook-add-entry") {
        EtaTextButton(
            text = "Shto hyrje",
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CharacterCardPadding, vertical = 8.dp),
            onClick = {
                onExpandEntry(book.entries.size)
                store.updateWorldbook { it.copy(entries = it.entries + CharacterBookEntryDraft(insertionOrder = it.entries.size)) }
            },
        )
    }
}

@Composable
private fun CharacterWorldbookEntryEditor(
    entry: CharacterBookEntryDraft,
    enabled: Boolean,
    unsupported: List<String>,
    onChange: (CharacterBookEntryDraft) -> Unit,
    onDelete: () -> Unit,
) {
    if (unsupported.isNotEmpty()) {
        Text(
            "Kjo hyrje nuk merr pjesë në përputhje:${unsupported.joinToString("；")}",
            style = MiuixTheme.textStyles.body2,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
    CharacterFieldGroupLabel("Përmbajtja")
    CharacterTextField("Titulli i hyrjes", entry.name, { onChange(entry.copy(name = it)) }, enabled, singleLine = true)
    CharacterTextField("Përmbajtja", entry.content, { onChange(entry.copy(content = it)) }, enabled, minLines = 4)
    CharacterFieldGroupLabel("Aktivizimi")
    CharacterTextField("Fjalë kyçe kryesore (një për rresht)", entry.keys.joinToString("\n"), { onChange(entry.copy(keys = it.lines())) }, enabled)
    CharacterTextField("Fjalë kyçe dytësore (një për rresht)", entry.secondaryKeys.joinToString("\n"), { onChange(entry.copy(secondaryKeys = it.lines())) }, enabled)
    EtaSwitchPreference(
        title = "Përputh edhe fjalët kyçe dytësore",
        checked = entry.selective,
        enabled = enabled,
        onCheckedChange = { onChange(entry.copy(selective = it)) },
    )
    CharacterFieldGroupLabel("Futja")
    EtaSwitchPreference(
        title = "Kontekst i përhershëm",
        checked = entry.constant,
        enabled = enabled,
        onCheckedChange = { onChange(entry.copy(constant = it)) },
    )
    EtaSwitchPreference(
        title = "Vendose para profilit të personazhit",
        summary = "Kur është çaktivizuar, vendose pas profilit të personazhit",
        checked = entry.position == "before_char",
        enabled = enabled,
        onCheckedChange = { onChange(entry.copy(position = if (it) "before_char" else "after_char")) },
    )
    CharacterTextField("Renditja e futjes", entry.insertionOrder.toString(), { value ->
        value.toIntOrNull()?.takeIf { it >= 0 }?.let { onChange(entry.copy(insertionOrder = it)) }
    }, enabled, singleLine = true)
    EtaTextButton(
        text = "Hiq hyrjen",
        enabled = enabled,
        onClick = onDelete,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

private fun optionalNonNegativeInt(value: String, onValue: (Int?) -> Unit) {
    if (value.isBlank()) onValue(null)
    else value.toIntOrNull()?.takeIf { it >= 0 }?.let(onValue)
}
