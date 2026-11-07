package io.github.mangi.eta.agent.model

import org.json.JSONArray
import org.json.JSONObject

/** 文本输入、等待与系统操作工具 schema。 */
internal object AgentTextSystemToolCatalog {
    fun appendTo(tools: JSONArray) {
        tools
            .put(
                AgentToolSchema.function(
                    name = "type_text",
                    description = "Shkruaj tekst në një fushë dhe verifiko rezultatin duke e rilexuar. Si parazgjedhje mode=replace zëvendëson gjithë përmbajtjen me text; text bosh e pastron." +
                        "mode=append fut tekst në pozicionin e kursorit. Me index shkruan drejtpërdrejt në nyjën editable, pa e prekur; përndryshe përdor fushën aktive." +
                        "Me submit=true shtyp Enter pas shkrimit për kërkim ose dërgim. Kur submitted=false, teksti është shkruar por nuk është dërguar; prek butonin e dërgimit dhe mos e përsërit shkrimin." +
                        "Teksti në çdo gjuhë, emoji dhe tekstet e gjata futen drejtpërdrejt, pa përdorur clipboard.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("text", JSONObject().put("type", "string").put("maxLength", 4_000))
                                .put(
                                    "mode",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("enum", JSONArray().put("replace").put("append"))
                                        .put("description", "replace zëvendëson të gjithë tekstin (parazgjedhja); append fut tekst te kursori."),
                                )
                                .put(
                                    "index",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "index opsional i nyjës editable; vetëm për replace dhe duhet shoqëruar me observation_id nga i njëjti vëzhgim."),
                                )
                                .put(
                                    "observation_id",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("description", "Kërkohet kur jep index dhe duhet të vijë nga i njëjti vëzhgim më i fundit."),
                                )
                                .put(
                                    "submit",
                                    JSONObject()
                                        .put("type", "boolean")
                                        .put("description", "Dërgo me Enter pas shkrimit; parazgjedhja false."),
                                )
                        )
                        .put("required", JSONArray().put("text"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "set_clipboard",
                    description = "Kopjo tekst në clipboard-in e sistemit; përdoret për tekst të gjatë, shumëgjuhësh, emoji ose simbole të veçanta.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "text",
                                    JSONObject().put("type", "string").put("maxLength", 20_000),
                                )
                        )
                        .put("required", JSONArray().put("text"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "get_clipboard",
                    description = "Lexo tekstin nga clipboard-i i sistemit; versioni Android ose kufizimet në sfond mund ta pengojnë.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put("properties", JSONObject())
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "press_key",
                    description = "Ekzekuto taste të sistemit ose veprime globale. BACK/HOME/RECENTS/NOTIFICATIONS/QUICK_SETTINGS preferojnë veprimet globale të aksesueshmërisë; ENTER preferon tastierën.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "button",
                                    JSONObject()
                                        .put("type", "string")
                                        .put(
                                            "enum",
                                            JSONArray()
                                                .put("BACK")
                                                .put("HOME")
                                                .put("ENTER")
                                                .put("RECENTS")
                                                .put("PASTE")
                                                .put("NOTIFICATIONS")
                                                .put("QUICK_SETTINGS")
                                        )
                                )
                        )
                        .put("required", JSONArray().put("button"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "wait",
                    description = "Prit për përfundimin e animacioneve, ngarkimit në rrjet ose kalimit të faqeve. Mos e zëvendëso me këtë pritjen e verifikueshme wait_for_text/wait_for_package.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "duration_ms",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "Koha e pritjes, 100 deri në 30000 ms; parazgjedhja 1000.")
                                )
                        )
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "wait_for_text",
                    description = "Prit derisa teksti ose përshkrimi i kërkuar të shfaqet në ekran, për të verifikuar hapjen e faqes, ngarkimin e listës ose dialogun.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("text", JSONObject().put("type", "string"))
                                .put(
                                    "timeout_ms",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "Koha maksimale e pritjes, 500 deri në 60000 ms; parazgjedhja 10000.")
                                )
                                .put(
                                    "include_desc",
                                    JSONObject()
                                        .put("type", "boolean")
                                        .put("description", "Përfshi content-desc në përputhje; parazgjedhja true.")
                                )
                                .put(
                                    "match",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("enum", JSONArray().put("contains").put("exact").put("prefix").put("regex"))
                                        .put("description", "Mënyra e përputhjes; parazgjedhja contains.")
                                )
                        )
                        .put("required", JSONArray().put("text"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "wait_for_package",
                    description = "Prit derisa paketa Android e kërkuar të jetë aktive, për të verifikuar launch_app/open_uri.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("package_name", JSONObject().put("type", "string"))
                                .put(
                                    "timeout_ms",
                                    JSONObject()
                                        .put("type", "integer")
                                        .put("description", "Koha maksimale e pritjes, 500 deri në 60000 ms; parazgjedhja 10000.")
                                )
                        )
                        .put("required", JSONArray().put("package_name"))
                )
            )
            .put(
                AgentToolSchema.function(
                    name = "open_system_panel",
                    description = "Hap panelin e njoftimeve ose cilësimet e shpejta.",
                    parameters = JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put(
                                    "panel",
                                    JSONObject()
                                        .put("type", "string")
                                        .put("enum", JSONArray().put("notifications").put("quick_settings"))
                                )
                        )
                        .put("required", JSONArray().put("panel"))
                )
            )
    }
}
