package io.github.mangi.eta.ui.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.mangi.eta.R
import io.github.mangi.eta.agent.memory.AgentMemoryContextBuilder
import io.github.mangi.eta.agent.memory.AgentMemoryRelationClassifier
import io.github.mangi.eta.agent.memory.AgentChatMemoryCandidates
import io.github.mangi.eta.agent.memory.AgentChatMemoryReranker
import io.github.mangi.eta.agent.memory.AgentMemoryRetrievalHealthStore
import io.github.mangi.eta.agent.memory.AgentMemoryRetrievalReport
import io.github.mangi.eta.agent.memory.EtaOpenAiEmbeddingProvider
import io.github.mangi.eta.agent.memory.AgentSemanticMemorySearch
import io.github.mangi.eta.agent.memory.EtaEmbeddingEndpoint
import io.github.mangi.eta.agent.memory.EtaMemoryVectorIndex
import io.github.mangi.eta.agent.runtime.AgentRunController
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.core.safeLogType
import io.github.mangi.eta.data.repository.AgentMemoryRepository
import io.github.mangi.eta.data.repository.AgentExperienceRepository
import io.github.mangi.eta.data.repository.AgentKnowledgeRepository
import io.github.mangi.eta.data.repository.AgentRetainedMemoryRepository
import io.github.mangi.eta.data.repository.EtaWebDavSyncRepository
import io.github.mangi.eta.data.repository.EtaWebDavSyncScheduler
import io.github.mangi.eta.data.datastore.SettingsDataStore
import io.github.mangi.eta.data.repository.RuntimeConfigRepository
import io.github.mangi.eta.ui.model.AgentMemoryUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 记忆页的读取、编辑与保存状态；与会话和 Agent run 无共享状态。 */
internal class AgentMemoryStore(
    context: Context,
    private val scope: CoroutineScope,
) {
    private val appContext = context.applicationContext

    var memoryState by mutableStateOf(AgentMemoryUiState())
        private set

    fun refreshMemory() {
        memoryState = memoryState.copy(isLoading = true, notice = null)
        scope.launch(Dispatchers.IO) {
            runCatching {
                val snapshot = AgentMemoryRepository.snapshot()
                val enabled = AgentMemoryRepository.isEnabled()
                val contextWindow = RuntimeConfigRepository.currentRuntimeConfig()?.contextWindow
                Triple(snapshot, enabled, AgentMemoryContextBuilder.coreBudgetChars(contextWindow))
            }.fold(
                onSuccess = { (snapshot, enabled, coreBudget) ->
                    val autoExperienceEnabled = SettingsDataStore.autoExperienceEnabled()
                    val smartRetainEnabled = SettingsDataStore.smartRetainEnabled()
                    val semanticEnabled = SettingsDataStore.semanticMemoryEnabled()
                    val embeddingEnabled = SettingsDataStore.semanticMemoryUseEmbeddings()
                    val semanticModel = SettingsDataStore.semanticMemoryModel()
                    val graphEnabled = SettingsDataStore.graphMemoryEnabled()
                    val experienceCount = AgentExperienceRepository.count()
                    val retainedNodes = AgentRetainedMemoryRepository.visible(400)
                    val activeNodes = retainedNodes.filter { it.active }
                    val knowledgeDocuments = AgentKnowledgeRepository.snapshot().filter { !it.deleted }
                    val coreChunks = AgentChatMemoryCandidates.coreChunks(snapshot.content).size
                    val report = runCatching {
                        AgentMemoryRetrievalHealthStore(appContext.filesDir).read()?.shortSummary()
                    }.getOrNull()
                    val indexed = runCatching {
                        val config = RuntimeConfigRepository.currentRuntimeConfig()
                        if (config != null) {
                            val endpoint = EtaEmbeddingEndpoint.fromConfig(config, semanticModel)
                            EtaMemoryVectorIndex(appContext.filesDir).readValid(
                                EtaMemoryVectorIndex.fingerprint(endpoint), activeNodes,
                            ).vectors.size
                        } else 0
                    }.getOrDefault(0)
                    val syncConfig = EtaWebDavSyncRepository.config()
                    val autoSync = SettingsDataStore.autoWebDavSyncEnabled()
                    val memoryConflict = EtaWebDavSyncRepository.hasMemoryConflict()
                    val remoteConflictContent = if (memoryConflict) {
                        EtaWebDavSyncRepository.remoteConflictContent()
                    } else null
                    withContext(Dispatchers.Main) {
                        memoryState = AgentMemoryUiState(
                            enabled = enabled,
                            autoExperienceEnabled = autoExperienceEnabled,
                            smartRetainEnabled = smartRetainEnabled,
                            semanticMemoryEnabled = semanticEnabled,
                            semanticUseEmbeddings = embeddingEnabled,
                            semanticModel = semanticModel,
                            semanticCoreChunks = coreChunks,
                            semanticIndexedNodes = indexed,
                            semanticRetrievalReport = report,
                            graphMemoryEnabled = graphEnabled,
                            retainedNodes = retainedNodes,
                            experienceCount = experienceCount,
                            knowledgeDocuments = knowledgeDocuments,
                            syncEndpoint = syncConfig.endpoint,
                            syncUsername = syncConfig.username,
                            syncHasPassword = syncConfig.hasPassword,
                            syncHasPassphrase = syncConfig.hasPassphrase,
                            syncAutoEnabled = autoSync,
                            syncMemoryConflict = memoryConflict,
                            syncRemoteConflictContent = remoteConflictContent,
                            isLoading = false,
                            draft = snapshot.content,
                            savedContent = snapshot.content,
                            draftBytes = snapshot.byteSize,
                            coreBudgetChars = coreBudget,
                        )
                    }
                },
                onFailure = { throwable ->
                    AndroidAgentLogger.warnThrottled("agent_memory_ui_load_failed") {
                        "Agent memory UI load failed: type=${throwable.safeLogType()}"
                    }
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(
                            isLoading = false,
                            notice = appContext.getString(R.string.state_ui_failed_to_read_memory_please_try_again_later_caeaa6),
                        )
                    }
                },
            )
        }
    }

    fun updateMemoryDraft(content: String) {
        memoryState = memoryState.copy(
            draft = content,
            draftBytes = content.toByteArray(Charsets.UTF_8).size,
            notice = null,
        )
    }

    fun setMemoryEnabled(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            runCatching { AgentMemoryRepository.setEnabled(enabled) }
                .fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            memoryState = memoryState.copy(enabled = enabled, notice = null)
                        }
                    },
                    onFailure = { throwable ->
                        AndroidAgentLogger.warnThrottled("agent_memory_toggle_failed") {
                            "Agent memory setting update failed: type=${throwable.safeLogType()}"
                        }
                        withContext(Dispatchers.Main) {
                            memoryState = memoryState.copy(notice = appContext.getString(R.string.state_ui_memory_switch_failed_to_save_83b5d6))
                        }
                    },
                )
        }
    }

    fun setAutoExperienceEnabled(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            runCatching { SettingsDataStore.setAutoExperienceEnabled(enabled) }
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(autoExperienceEnabled = enabled)
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = "Aktivizimi i kujtesës automatike të përvojës nuk u ruajt")
                    }
                }
        }
    }

    fun clearExperiences() {
        scope.launch(Dispatchers.IO) {
            runCatching { AgentExperienceRepository.clear() }
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(experienceCount = 0, notice = "Përvoja e detyrave u pastrua")
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = "Përvoja e detyrave nuk u pastrua")
                    }
                }
        }
    }

    fun setSmartRetainEnabled(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            runCatching { SettingsDataStore.setSmartRetainEnabled(enabled) }
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(smartRetainEnabled = enabled)
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = "Aktivizimi i kujtesës inteligjente nuk u ruajt")
                    }
                }
        }
    }

    fun setSemanticMemoryEnabled(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            runCatching { SettingsDataStore.setSemanticMemoryEnabled(enabled) }.fold(
                onSuccess = {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(semanticMemoryEnabled = enabled)
                    }
                },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = "Cilësimi i rikujtimit semantik nuk u përditësua")
                    }
                },
            )
        }
    }

    fun testChatSemanticRetrieval() {
        if (memoryState.chatRetrievalTestBusy) return
        memoryState = memoryState.copy(
            chatRetrievalTestBusy = true,
            chatRetrievalTestStatus = "Po testohet kërkimi semantik me modelin aktual…",
        )
        scope.launch(Dispatchers.IO) {
            val result = runCatching {
                val config = RuntimeConfigRepository.currentRuntimeConfig()
                    ?: error("Zgjidh fillimisht një model bisede të disponueshëm")
                val core = AgentMemoryRepository.snapshot().content
                val nodes = AgentRetainedMemoryRepository.active(400)
                val docs = AgentKnowledgeRepository.snapshot().filter { !it.deleted }
                val query = "Cilat projekte, preferenca afatgjata dhe dokumente të rëndësishme kam ruajtur më parë?"
                val chunks = AgentChatMemoryCandidates.coreChunks(core).size
                val candidates = AgentChatMemoryCandidates.select(query, core, docs, nodes)
                if (candidates.isEmpty()) error("MEMORY.md, baza e njohurive dhe kujtesa e strukturuar janë bosh")
                val selection = AgentChatMemoryReranker().rerank(
                    query, candidates, config, AgentRunController(),
                )
                val health = AgentMemoryRetrievalReport(
                    "chat", candidates.size, selection.selected.size,
                    chunks, nodes.size, "Testi manual kaloi",
                )
                AgentMemoryRetrievalHealthStore(appContext.filesDir).write(health)
                val message = "Modeli aktual është i disponueshëm; u kontrolluan ${candidates.size} kandidatë dhe u gjetën ${selection.selected.size}." +
                    (if (selection.selected.isEmpty()) {
                        "Pyetja e testit nuk dha rezultat. Provo një pyetje më konkrete gjatë bisedës"
                    } else " Burimi: ${selection.selected.map { it.source }.distinct().joinToString(", ")}")
                message to health.shortSummary()
            }
            withContext(Dispatchers.Main) {
                memoryState = memoryState.copy(
                    chatRetrievalTestBusy = false,
                    chatRetrievalTestStatus = result.fold(
                        onSuccess = { it.first },
                        onFailure = { "Kërkimi me modelin aktual dështoi: ${it.message ?: it.javaClass.simpleName}" },
                    ),
                    semanticRetrievalReport = result.getOrNull()?.second
                        ?: memoryState.semanticRetrievalReport,
                )
            }
        }
    }

    fun setEmbeddingAcceleratorEnabled(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            runCatching { SettingsDataStore.setSemanticMemoryUseEmbeddings(enabled) }.fold(
                onSuccess = {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(
                            semanticUseEmbeddings = enabled,
                            embeddingTestStatus = if (enabled) {
                                "API-ja Embeddings nuk është verifikuar. Prek Testo"
                            } else "Kërkimi përdor modelin aktual të bisedës; API-ja e veçantë e vektorëve është e çaktivizuar",
                        )
                    }
                },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = "Cilësimi i modelit Embeddings nuk u ruajt")
                    }
                },
            )
        }
    }

    fun testEmbeddingProvider() {
        if (memoryState.embeddingTestBusy) return
        memoryState = memoryState.copy(
            embeddingTestBusy = true,
            embeddingTestStatus = "Po testohet realisht API-ja Embeddings…",
        )
        scope.launch(Dispatchers.IO) {
            val outcome = runCatching {
                val config = RuntimeConfigRepository.currentRuntimeConfig()
                    ?: error("Ofruesi i modelit aktual nuk është konfiguruar")
                val endpoint = EtaEmbeddingEndpoint.fromConfig(
                    config, SettingsDataStore.semanticMemoryModel(),
                )
                val embedding = EtaOpenAiEmbeddingProvider(endpoint)
                    .embed(listOf("Eta embedding compatibility test"), AgentRunController())
                "Lidhja u krye; dimensioni i vektorit është ${embedding.first().size}; ofruesi mbështet ${endpoint.model}"
            }
            withContext(Dispatchers.Main) {
                memoryState = memoryState.copy(
                    embeddingTestBusy = false,
                    embeddingTestStatus = outcome.fold(
                        onSuccess = { it },
                        onFailure = {
                            "Nuk është e disponueshme: ${it.message ?: it.javaClass.simpleName}. Kërkimi me modelin e bisedës mbetet aktiv"
                        },
                    ),
                )
            }
        }
    }

    fun setSemanticMemoryModel(model: String) {
        scope.launch(Dispatchers.IO) {
            runCatching { SettingsDataStore.setSemanticMemoryModel(model.trim()) }.fold(
                onSuccess = {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(
                            semanticModel = model.trim(),
                            semanticIndexStatus = "Modeli Embeddings ndryshoi; indeksi lokal duhet rindërtuar",
                        )
                    }
                },
                onFailure = { failure ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(
                            notice = failure.message ?: "Emri i modelit ka format të pavlefshëm",
                        )
                    }
                },
            )
        }
    }

    fun buildSemanticIndex() {
        if (memoryState.semanticIndexBusy || !memoryState.semanticMemoryEnabled ||
            !memoryState.semanticUseEmbeddings) return
        if (memoryState.retainedNodes.none { it.active }) {
            memoryState = memoryState.copy(
                semanticIndexStatus = "Kujtesa e strukturuar është bosh; indeksi vektorial nuk mund të krijohet, por MEMORY.md mund të kërkohet me modelin aktual",
            )
            return
        }
        memoryState = memoryState.copy(
            semanticIndexBusy = true, semanticIndexStatus = "Po krijohen vektorët semantikë lokalë…",
        )
        scope.launch(Dispatchers.IO) {
            val result = runCatching {
                val model = RuntimeConfigRepository.currentRuntimeConfig()
                    ?: error("Konfiguro te cilësimet një ofrues që mbështet Embeddings")
                val endpoint = EtaEmbeddingEndpoint.fromConfig(
                    model, SettingsDataStore.semanticMemoryModel(),
                )
                AgentSemanticMemorySearch(EtaMemoryVectorIndex(appContext.filesDir))
                    .buildIndex(
                        AgentRetainedMemoryRepository.active(400), endpoint, AgentRunController(),
                        maximumBatches = 4,
                    )
            }
            withContext(Dispatchers.Main) {
                memoryState = memoryState.copy(
                    semanticIndexBusy = false,
                    semanticIndexedNodes = result.getOrNull()?.first ?: memoryState.semanticIndexedNodes,
                    semanticIndexStatus = result.fold(
                        onSuccess = { (done, total) ->
                            "U indeksuan ${done}/${total} kujtime; ${if (done < total) "preke përsëri për të vazhduar" else "indeksimi përfundoi"}. Ruhen vetëm në pajisje"
                        },
                        onFailure = { "Indeksimi semantik dështoi: ${it.message ?: it.javaClass.simpleName} (kërkimi tekstual mbetet aktiv)" },
                    ),
                )
            }
        }
    }

    fun setGraphMemoryEnabled(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            runCatching { SettingsDataStore.setGraphMemoryEnabled(enabled) }.fold(
                onSuccess = {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(graphMemoryEnabled = enabled)
                    }
                },
                onFailure = {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = "Aktivizimi i kujtesës së ndërlidhur nuk u ruajt")
                    }
                },
            )
        }
    }

    fun classifyExistingRelations() {
        if (memoryState.graphClassifierBusy || !memoryState.graphMemoryEnabled) return
        memoryState = memoryState.copy(
            graphClassifierBusy = true,
            graphClassifierStatus = "Po klasifikohen lidhjet e kujtimeve të fundit…",
        )
        scope.launch(Dispatchers.IO) {
            val result = runCatching {
                val config = RuntimeConfigRepository.currentRuntimeConfig()
                    ?: error("Konfiguro fillimisht një model bisede")
                val nodes = AgentRetainedMemoryRepository.active(400)
                val sources = nodes.sortedByDescending { it.updatedAt }
                    .filter { it.links.none { relation -> relation.status == "confirmed" } }
                    .take(12)
                var linked = 0
                sources.chunked(3).forEach { chunk ->
                    val remaining = nodes.filterNot { node -> chunk.any { it.id == node.id } }
                    val results = AgentMemoryRelationClassifier().classify(
                        config, chunk, remaining, AgentRunController(),
                    )
                    results.forEach { (id, links) ->
                        linked += AgentRetainedMemoryRepository.proposeLinks(id, links)
                    }
                }
                linked to AgentRetainedMemoryRepository.visible(400)
            }
            withContext(Dispatchers.Main) {
                memoryState = memoryState.copy(
                    graphClassifierBusy = false,
                    retainedNodes = result.getOrNull()?.second ?: memoryState.retainedNodes,
                    graphClassifierStatus = result.fold(
                        onSuccess = { (count, _) ->
                            "U shtuan ose ndryshuan ${count} sugjerime lidhjesh; kërkohet shqyrtim manual"
                        },
                        onFailure = { "Klasifikimi i lidhjeve dështoi: ${it.message ?: it.javaClass.simpleName}" },
                    ),
                )
            }
        }
    }

    fun reviewMemoryLink(sourceId: String, targetId: String, type: String, status: String) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                check(AgentRetainedMemoryRepository.reviewLink(sourceId, targetId, type, status)) {
                    "Lidhja nuk ekziston ose kujtimi përkatës ka skaduar"
                }
                AgentRetainedMemoryRepository.visible(400)
            }.fold(
                onSuccess = { nodes ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(
                            retainedNodes = nodes,
                            notice = if (status == "confirmed") "Lidhja u konfirmua" else "Lidhja u refuzua",
                        )
                    }
                },
                onFailure = { failure ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = failure.message ?: "Shqyrtimi i lidhjes dështoi")
                    }
                },
            )
        }
    }

    fun deleteRetainedMemory(id: String) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                AgentRetainedMemoryRepository.delete(id)
                AgentRetainedMemoryRepository.visible(400)
            }.fold(
                onSuccess = { nodes ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(retainedNodes = nodes, notice = "Nyja e kujtesës u fshi")
                    }
                },
                onFailure = { error ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = error.message ?: "Fshirja e nyjës së kujtesës dështoi")
                    }
                },
            )
        }
    }

    fun clearRetainedMemory() {
        scope.launch(Dispatchers.IO) {
            runCatching {
                AgentRetainedMemoryRepository.clear()
                AgentRetainedMemoryRepository.visible(400)
            }.fold(
                onSuccess = { nodes ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(retainedNodes = nodes, notice = "Kujtesa e strukturuar u pastrua")
                    }
                },
                onFailure = { error ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = error.message ?: "Pastrimi dështoi")
                    }
                },
            )
        }
    }

    fun saveWebDav(endpoint: String, username: String, password: String, passphrase: String) {
        if (memoryState.syncBusy) return
        memoryState = memoryState.copy(syncBusy = true)
        scope.launch(Dispatchers.IO) {
            runCatching { EtaWebDavSyncRepository.configure(endpoint, username, password, passphrase) }
                .fold(
                    onSuccess = { config ->
                        withContext(Dispatchers.Main) {
                            memoryState = memoryState.copy(
                                syncBusy = false, syncEndpoint = config.endpoint,
                                syncUsername = config.username,
                                syncHasPassword = config.hasPassword,
                                syncHasPassphrase = config.hasPassphrase,
                                syncStatus = "Konfigurimi WebDAV u ruajt; prek Sinkronizo tani",
                            )
                        }
                    },
                    onFailure = { failure ->
                        withContext(Dispatchers.Main) {
                            memoryState = memoryState.copy(
                                syncBusy = false,
                                notice = failure.message ?: "Konfigurimi WebDAV nuk u ruajt",
                            )
                        }
                    },
                )
        }
    }

    fun runWebDavSync(resolveReviewedMemoryConflict: Boolean = false) {
        if (memoryState.syncBusy || !memoryState.syncHasPassphrase) return
        if (memoryState.hasUnsavedChanges) {
            memoryState = memoryState.copy(notice = "Ruaj fillimisht ndryshimet e MEMORY.md përpara sinkronizimit")
            return
        }
        memoryState = memoryState.copy(syncBusy = true, syncStatus = "Po sinkronizohet…")
        scope.launch(Dispatchers.IO) {
            val outcome = runCatching {
                EtaWebDavSyncRepository.sync(resolveReviewedMemoryConflict)
            }
            val memoryConflict = EtaWebDavSyncRepository.hasMemoryConflict()
            val remoteConflictContent = if (memoryConflict) {
                EtaWebDavSyncRepository.remoteConflictContent()
            } else null
            val refreshedMemory = runCatching { AgentMemoryRepository.snapshot() }.getOrNull()
            val refreshedKnowledge = runCatching { AgentKnowledgeRepository.snapshot() }.getOrNull()
            val experiences = runCatching { AgentExperienceRepository.count() }.getOrNull()
            val retained = runCatching { AgentRetainedMemoryRepository.visible(400) }.getOrNull()
            withContext(Dispatchers.Main) {
                val status = outcome.fold(
                    onSuccess = { result ->
                        if (result.success) {
                            "${result.message}: ${result.knowledgeCount} njohuri, ${result.experienceCount} përvoja detyrash, " +
                                "${result.retainedCount} kujtime të strukturuara, ${result.knowledgeConflicts} kopje konfliktesh"
                        } else result.message
                    },
                    onFailure = { failure -> failure.message ?: "Sinkronizimi WebDAV dështoi" },
                )
                memoryState = memoryState.copy(
                    syncBusy = false,
                    syncMemoryConflict = memoryConflict,
                    syncRemoteConflictContent = remoteConflictContent,
                    syncStatus = status,
                    savedContent = refreshedMemory?.content ?: memoryState.savedContent,
                    draft = if (!memoryState.hasUnsavedChanges) {
                        refreshedMemory?.content ?: memoryState.draft
                    } else memoryState.draft,
                    draftBytes = if (!memoryState.hasUnsavedChanges) {
                        refreshedMemory?.byteSize ?: memoryState.draftBytes
                    } else memoryState.draftBytes,
                    knowledgeDocuments = refreshedKnowledge?.filter { !it.deleted }
                        ?: memoryState.knowledgeDocuments,
                    experienceCount = experiences ?: memoryState.experienceCount,
                    retainedNodes = retained ?: memoryState.retainedNodes,
                )
            }
        }
    }

    fun setWebDavAutoSyncEnabled(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                SettingsDataStore.setAutoWebDavSyncEnabled(enabled)
                EtaWebDavSyncScheduler.configure(appContext, enabled)
            }.fold(
                onSuccess = {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(syncAutoEnabled = enabled)
                    }
                },
                onFailure = { failure ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(
                            notice = failure.message ?: "Sinkronizimi automatik nuk u konfigurua",
                        )
                    }
                },
            )
        }
    }

    fun clearWebDavConfig() {
        if (memoryState.syncBusy) return
        scope.launch(Dispatchers.IO) {
            runCatching {
                SettingsDataStore.setAutoWebDavSyncEnabled(false)
                EtaWebDavSyncScheduler.configure(appContext, false)
                EtaWebDavSyncRepository.clearConfig()
            }.fold(
                onSuccess = {
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(
                            syncEndpoint = "", syncUsername = "", syncHasPassword = false,
                            syncHasPassphrase = false, syncAutoEnabled = false,
                            syncMemoryConflict = false,
                            syncRemoteConflictContent = null,
                            syncStatus = "Lidhja WebDAV u hoq; përmbajtja lokale u ruajt",
                        )
                    }
                },
                onFailure = { failure ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = failure.message ?: "Konfigurimi WebDAV nuk u hoq")
                    }
                },
            )
        }
    }

    fun saveKnowledge(
        title: String, content: String, kind: String, source: String, tags: List<String>,
        id: String?, revision: String?,
    ) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                AgentKnowledgeRepository.save(title, content, kind, source, tags, id, revision)
                AgentKnowledgeRepository.snapshot().filter { !it.deleted }
            }.fold(
                onSuccess = { records ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(knowledgeDocuments = records, notice = "Njohuria u ruajt")
                    }
                },
                onFailure = { failure ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = failure.message ?: "Njohuria nuk u ruajt")
                    }
                },
            )
        }
    }

    fun deleteKnowledge(id: String, revision: String) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                AgentKnowledgeRepository.delete(id, revision)
                AgentKnowledgeRepository.snapshot().filter { !it.deleted }
            }.fold(
                onSuccess = { records ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(knowledgeDocuments = records, notice = "Njohuria u fshi")
                    }
                },
                onFailure = { failure ->
                    withContext(Dispatchers.Main) {
                        memoryState = memoryState.copy(notice = failure.message ?: "Fshirja dështoi")
                    }
                },
            )
        }
    }

    fun saveMemory() {
        if (!memoryState.canSave) return
        val target = memoryState.draft
        memoryState = memoryState.copy(isSaving = true, notice = null)
        scope.launch(Dispatchers.IO) {
            runCatching { AgentMemoryRepository.replaceAll(target) }
                .fold(
                    onSuccess = { snapshot ->
                        withContext(Dispatchers.Main) {
                            memoryState = memoryState.copy(
                                isSaving = false,
                                savedContent = snapshot.content,
                                draft = if (memoryState.draft == target) {
                                    snapshot.content
                                } else {
                                    memoryState.draft
                                },
                                draftBytes = memoryState.draft.toByteArray(Charsets.UTF_8).size,
                                notice = appContext.getString(R.string.state_ui_memory_saved_a2c61c),
                            )
                        }
                    },
                    onFailure = { throwable ->
                        AndroidAgentLogger.warnThrottled("agent_memory_ui_save_failed") {
                            "Agent memory UI save failed: type=${throwable.safeLogType()}"
                        }
                        withContext(Dispatchers.Main) {
                            memoryState = memoryState.copy(
                                isSaving = false,
                                notice = throwable.message ?: appContext.getString(R.string.state_ui_memory_save_failed_1f501e),
                            )
                        }
                    },
                )
        }
    }

    fun clearMemory() {
        if (memoryState.isSaving) return
        memoryState = memoryState.copy(isSaving = true, notice = null)
        scope.launch(Dispatchers.IO) {
            runCatching { AgentMemoryRepository.replaceAll("") }
                .fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            memoryState = memoryState.copy(
                                isSaving = false,
                                draft = "",
                                savedContent = "",
                                draftBytes = 0,
                                notice = appContext.getString(R.string.state_ui_memory_cleared_b415bb),
                            )
                        }
                    },
                    onFailure = { throwable ->
                        AndroidAgentLogger.warnThrottled("agent_memory_ui_clear_failed") {
                            "Agent memory UI clear failed: type=${throwable.safeLogType()}"
                        }
                        withContext(Dispatchers.Main) {
                            memoryState = memoryState.copy(
                                isSaving = false,
                                notice = throwable.message ?: appContext.getString(R.string.state_ui_memory_clearing_failed_7f0aba),
                            )
                        }
                    },
                )
        }
    }

    fun dismissMemoryNotice() {
        memoryState = memoryState.copy(notice = null)
    }
}
