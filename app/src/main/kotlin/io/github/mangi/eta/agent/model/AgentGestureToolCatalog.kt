package io.github.mangi.eta.agent.model

import org.json.JSONArray
import org.json.JSONObject

/** 屏幕手势与节点交互工具 schema。 */
internal object AgentGestureToolCatalog {
    fun appendTo(tools: JSONArray) {
        tools
            .put(
                AgentToolSchema.function(
                    name = "tap",
                    description = "Prek një koordinatë. Kur ka nyje UI përdor fillimisht tap_element; për koordinata nga pamja përdor coordinate_space=normalized (0–999), kurse nga ui_nodes përdor screen.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("x", JSONObject().put("type", "integer"))
                                .put("y", JSONObject().put("type", "integer"))
                                .put("coordinate_space", AgentToolSchema.coordinateSpace())
                        )
                        .put("required", JSONArray().put("x").put("y").put("coordinate_space"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "tap_area",
                    description = "Prek qendrën e një zone drejtkëndore. Përdore për butona të mëdhenj, elemente liste dhe tekst të dukshëm. Si te tap, jep shprehimisht coordinate_space.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("x1", JSONObject().put("type", "integer"))
                                .put("y1", JSONObject().put("type", "integer"))
                                .put("x2", JSONObject().put("type", "integer"))
                                .put("y2", JSONObject().put("type", "integer"))
                                .put("coordinate_space", AgentToolSchema.coordinateSpace())
                        )
                        .put("required", JSONArray().put("x1").put("y1").put("x2").put("y2").put("coordinate_space"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "tap_element",
                    description = "Prek nyjën UI të vëzhguar. index dhe observation_id duhet të vijnë nga i njëjti vëzhgim më i fundit observe_screen; nëse ka skaduar, vëzhgo përsëri. Runtime kontrollon lidhjen me shërbimin e aksesueshmërisë Eta.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "index",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "index i nyjës UI nga i njëjti vëzhgim observe_screen.")
                                )
                                .put(
                                    "observation_id",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("description", "observation_id që i përket index nga i njëjti vëzhgim më i fundit observe_screen.")
                                )
                        )
                        .put("required", JSONArray().put("index").put("observation_id"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "long_press",
                    description = "Mbaj shtypur koordinatën. Kur ka nyje UI përdor long_press_element; si te tap, jep shprehimisht coordinate_space.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("x", JSONObject().put("type", "integer"))
                                .put("y", JSONObject().put("type", "integer"))
                                .put(
                                    "duration_ms",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "Kohëzgjatja e mbajtjes shtypur, 300 deri në 3000 ms; parazgjedhja 800")
                                )
                                .put("coordinate_space", AgentToolSchema.coordinateSpace())
                        )
                        .put("required", JSONArray().put("x").put("y").put("coordinate_space"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "long_press_element",
                    description = "Mbaj shtypur nyjën UI të vëzhguar. index dhe observation_id duhet të vijnë nga i njëjti vëzhgim më i fundit observe_screen; nëse ka skaduar, vëzhgo përsëri. Runtime kontrollon lidhjen me aksesueshmërinë Eta.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "index",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "index i nyjës UI nga i njëjti vëzhgim observe_screen.")
                                )
                                .put(
                                    "observation_id",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("description", "observation_id që i përket index nga i njëjti vëzhgim më i fundit observe_screen.")
                                )
                                .put(
                                    "duration_ms",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "Kohëzgjatja e mbajtjes shtypur, 300 deri në 3000 ms; parazgjedhja 800")
                                )
                        )
                        .put("required", JSONArray().put("index").put("observation_id"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "swipe",
                    description = "Rrëshqit nga një koordinatë në tjetrën, sipas drejtimit të gishtit. Përdore për rrëshqitës, karusele dhe harta; për lista përdor scroll. Jep shprehimisht coordinate_space.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("x1", JSONObject().put("type", "integer"))
                                .put("y1", JSONObject().put("type", "integer"))
                                .put("x2", JSONObject().put("type", "integer"))
                                .put("y2", JSONObject().put("type", "integer"))
                                .put(
                                    "duration_ms",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "Kohëzgjatja e rrëshqitjes, 100 deri në 2000 ms; parazgjedhja 500")
                                )
                                .put("coordinate_space", AgentToolSchema.coordinateSpace())
                        )
                        .put("required", JSONArray().put("x1").put("y1").put("x2").put("y2").put("coordinate_space"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "scroll",
                    description = "Lëviz listën kryesore sipas përmbajtjes: down shfaq përmbajtjen më poshtë, up shfaq përmbajtjen më sipër, njësoj për left/right. Kur moved=false dhe at_boundary=true është arritur kufiri; ndrysho drejtim ose ndalo. Për disa lista, përdor scroll_element.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "direction",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("enum", JSONArray().put("up").put("down").put("left").put("right"))
                                )
                                .put(
                                    "amount",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("enum", JSONArray().put("page").put("small"))
                                        .put("description", "page lëviz afërsisht një ekran (parazgjedhja); small lëviz pak për ta sjellë objektivin në pamje."),
                                )
                        )
                        .put("required", JSONArray().put("direction"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "scroll_element",
                    description = "Lëviz nyjën UI të vëzhguar: down shfaq përmbajtjen më poshtë, up shfaq përmbajtjen më sipër, left majtas dhe right djathtas. index dhe observation_id duhet të vijnë nga i njëjti vëzhgim më i fundit observe_screen; nëse ka skaduar, vëzhgo përsëri. Runtime verifikon lidhjen me aksesueshmërinë Eta.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "index",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "index i nyjës UI të lëvizshme nga i njëjti vëzhgim observe_screen.")
                                )
                                .put(
                                    "observation_id",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("description", "observation_id që i përket index nga i njëjti vëzhgim më i fundit observe_screen.")
                                )
                                .put(
                                    "direction",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("enum", JSONArray().put("up").put("down").put("left").put("right"))
                                        .put("description", "Drejtimi i lëvizjes; down shfaq përmbajtjen më poshtë, up shfaq përmbajtjen më sipër.")
                                )
                                .put(
                                    "amount",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("enum", JSONArray().put("page").put("small"))
                                        .put("description", "page lëviz afërsisht një ekran (parazgjedhja); small lëviz pak për ta sjellë objektivin në pamje."),
                                )
                        )
                        .put("required", JSONArray().put("index").put("observation_id").put("direction"))
                )
            )
    }
}
