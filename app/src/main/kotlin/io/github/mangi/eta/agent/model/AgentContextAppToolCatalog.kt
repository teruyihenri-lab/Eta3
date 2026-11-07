package io.github.mangi.eta.agent.model

import org.json.JSONArray
import org.json.JSONObject

/** 上下文、应用入口与屏幕观察工具 schema。 */
internal object AgentContextAppToolCatalog {
    fun appendTo(tools: JSONArray) {
        tools
            .put(
                AgentToolSchema.function(
                    name = "get_current_context",
                    description = "Merr orën aktuale, zonën kohore dhe vendndodhjen e fundit të sistemit. Përdore për pyetje rreth tani, sot, nesër ose vendndodhjes.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put("properties", JSONObject())
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "search_apps",
                    description = "Kërko aplikacionet Android të instaluara dhe kthe emrat e paketave. Kur nuk je i sigurt për paketën, kërko këtu para se të hapësh aplikacionin.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "query",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("description", "Emri i aplikacionit ose pjesë e paketës, për shembull QQ, WeChat, com.tencent")
                                )
                                .put(
                                    "include_system",
                                    JSONObject()
                                        .put("type", "boolean")
                                        .put("description", "Përfshi edhe aplikacionet e sistemit; parazgjedhja false")
                                )
                                .put(
                                    "limit",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "Kthe 1 deri në 20 rezultate; parazgjedhja 10")
                                )
                        )
                        .put("required", JSONArray().put("query"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "launch_app",
                    description = "Nis një aplikacion Android të instaluar. Jep fillimisht package_name; nëse jep vetëm emrin, lejohet kërkim i përafërt. Kur ka disa përputhje, kthe kandidatët dhe mos e nis aplikacionin.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "package_name",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("description", "Emri i saktë i paketës Android, për shembull com.tencent.mobileqq")
                                )
                                .put(
                                    "app_name",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("description", "Emri që shfaq aplikacioni, për shembull QQ")
                                )
                        )
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "open_uri",
                    description = "Dërgo një URI të vlefshme te një aplikacion tjetër Android, p.sh. https, tel, geo ose lidhje e thellë. Nuk përdoret për të lexuar ose kontrolluar faqe interneti. Mos shpik URI.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "uri",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("description", "URI e vlefshme që sistemi mund ta hapë")
                                )
                        )
                        .put("required", JSONArray().put("uri"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "observe_screen",
                    description = "Vëzhgo ekranin e telefonit. Si parazgjedhje kthen vetëm aplikacionin aktiv, përmasat, observation_id dhe nyjet UI të dukshme, pa pamje ekrani. Kur nyjet mungojnë, objektivi nuk identifikohet qartë ose faqja mbështetet te Canvas, harta, foto, QR, ngjyra apo pozicioni hapësinor, vendos include_screenshot=true. Kur shton pamje ekrani, mbaj include_ui_tree=true, rifresko nyjet dhe observation_id nga i njëjti vëzhgim i ri; mos përziej pamjen e re me nyjet e vjetra. Veprimet mbi nyje duhet të përmbajnë observation_id nga i njëjti vëzhgim. Kur lista ndërpritet, por nyjet janë të vlefshme, provo max_nodes=120 përpara se të kërkosh pamje.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "include_screenshot",
                                    JSONObject()
                                        .put("type", "boolean")
                                        .put("default", AgentScreenObservationContract.DEFAULT_INCLUDE_SCREENSHOT)
                                        .put("description", "Bashkëngjit fotografinë e ekranit për modelin, parazgjedhja false; aktivizoje vetëm kur nyjet UI nuk mjaftojnë")
                                )
                                .put(
                                    "include_ui_tree",
                                    JSONObject()
                                        .put("type", "boolean")
                                        .put("default", AgentScreenObservationContract.DEFAULT_INCLUDE_UI_TREE)
                                        .put("description", "Kthe listën e nyjeve UI; parazgjedhja true")
                                )
                                .put(
                                    "max_nodes",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("minimum", AgentScreenObservationContract.MIN_MAX_NODES)
                                        .put("maximum", AgentScreenObservationContract.MAX_MAX_NODES)
                                        .put("default", AgentScreenObservationContract.DEFAULT_MAX_NODES)
                                        .put("description", "Kthe 1 deri në 120 nyje UI; parazgjedhja 60")
                                )
                        )
                )
            )
    }
}
