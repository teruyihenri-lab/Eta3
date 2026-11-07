package io.github.mangi.eta.agent.model

import org.json.JSONArray
import org.json.JSONObject

internal object AgentBrowserToolCatalog {
    fun appendTo(tools: JSONArray) {
        tools.put(
            AgentToolSchema.function(
                name = "browser_use",
                description = "Kontrollo shfletuesin e përbashkët të agjentit Eta pa kaluar te një shfletues i jashtëm. Një thirrje kryen vetëm një action: zakonisht navigate, më pas get_readable për përmbajtjen ose find_elements për elementet ndërvepruese. run_js ekzekuton JavaScript në faqen aktuale. Skriptet e përdoruesit menaxhohen me add_script (source ose url .user.js), list_scripts, toggle_script, remove_script dhe debug_scripts. Skriptet injektohen sipas @match/@include dhe ofrojnë GM_* API; kontrollo userscripts në get_page_info. Përgjimi i kërkesave: set_intercept, list_requests, clear_requests, add_intercept_replace (body/mime_type/status sipas pjesës së URL-së) dhe clear_intercept_replace. Përdor open_uri kur URI duhet kaluar shprehimisht te një aplikacion tjetër.",
                parameters = JSONObject()
                    .put("type", "object")
                    .put(
                        "properties",
                        JSONObject()
                            .put(
                                "action",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "Veprimi i vetëm i shfletuesit për këtë thirrje.")
                                    .put(
                                        "enum",
                                        JSONArray()
                                            .put("navigate")
                                            .put("get_readable")
                                            .put("get_text")
                                            .put("find_elements")
                                            .put("click")
                                            .put("type")
                                            .put("scroll")
                                            .put("screenshot")
                                            .put("get_page_info")
                                            .put("go_back")
                                            .put("go_forward")
                                            .put("reload")
                                            .put("wait_for_selector")
                                            .put("run_js")
                                            .put("list_scripts")
                                            .put("add_script")
                                            .put("toggle_script")
                                            .put("remove_script")
                                            .put("debug_scripts")
                                            .put("set_intercept")
                                            .put("add_intercept_replace")
                                            .put("clear_intercept_replace")
                                            .put("list_requests")
                                            .put("clear_requests")
                                    )
                            )
                            .put(
                                "url",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "URL-ja që duhet hapur me navigate.")
                            )
                            .put(
                                "selector",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "Selektori CSS për click, type, get_text, find_elements ose wait_for_selector.")
                            )
                            .put(
                                "text",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "Teksti që duhet futur me type; i dërgohet vetëm mjetit dhe nuk shfaqet te përmbledhja e ekzekutimit.")
                            )
                            .put(
                                "submit",
                                JSONObject()
                                    .put("type", "boolean")
                                    .put("description", "A duhet dërguar formulari pas type; parazgjedhja false.")
                            )
                            .put(
                                "coordinate_x",
                                JSONObject()
                                    .put("type", "integer")
                                    .put("description", "Koordinata X e zonës së shikimit për click ose type; përdore së bashku me coordinate_y.")
                            )
                            .put(
                                "coordinate_y",
                                JSONObject()
                                    .put("type", "integer")
                                    .put("description", "Koordinata Y e zonës së shikimit për click ose type; përdore së bashku me coordinate_x.")
                            )
                            .put(
                                "amount",
                                JSONObject()
                                    .put("type", "integer")
                                    .put("description", "Distanca e lëvizjes së scroll në pikselë.")
                            )
                            .put(
                                "direction",
                                JSONObject()
                                    .put("type", "string")
                                    .put("enum", JSONArray().put("up").put("down"))
                                    .put("description", "Drejtimi i lëvizjes së scroll.")
                            )
                            .put(
                                "offset",
                                JSONObject()
                                    .put("type", "integer")
                                    .put("description", "Pozicioni fillestar i tekstit për get_readable ose get_text; parazgjedhja 0.")
                            )
                            .put(
                                "max_chars",
                                JSONObject()
                                    .put("type", "integer")
                                    .put("description", "Numri maksimal i karaktereve të kthyera nga get_readable ose get_text.")
                            )
                            .put(
                                "read_image",
                                JSONObject()
                                    .put("type", "boolean")
                                    .put("description", "A t'i bashkëngjitet pamja e ekranit modelit gjatë screenshot; parazgjedhja true.")
                            )
                            .put(
                                "timeout_ms",
                                JSONObject()
                                    .put("type", "integer")
                                    .put("description", "Koha maksimale e pritjes në milisekonda për navigate ose wait_for_selector.")
                            )
                            .put(
                                "code",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "Kodi JavaScript që run_js ekzekuton në faqen aktuale; kthen vlerën e shprehjes së fundit.")
                            )
                            .put(
                                "source",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "Kodi i plotë i skriptit të përdoruesit për add_script, përfshirë metadata ==UserScript==.")
                            )
                            .put(
                                "script_id",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "ID-ja e skriptit për toggle_script ose remove_script, marrë nga list_scripts.")
                            )
                            .put(
                                "enabled",
                                JSONObject()
                                    .put("type", "boolean")
                                    .put("description", "Gjendja e synuar për toggle_script (pa vlerë ndryshon gjendjen); për set_intercept aktivizon ose çaktivizon përgjimin.")
                            )
                            .put(
                                "match",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "Pjesa e URL-së që duhet përputhur nga add_intercept_replace / clear_intercept_replace.")
                            )
                            .put(
                                "body",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "Përmbajtja zëvendësuese e përgjigjes për add_intercept_replace.")
                            )
                            .put(
                                "mime_type",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "Lloji MIME i përgjigjes për add_intercept_replace; parazgjedhja text/html.")
                            )
                            .put(
                                "encoding",
                                JSONObject()
                                    .put("type", "string")
                                    .put("description", "Kodimi i përgjigjes për add_intercept_replace; parazgjedhja utf-8.")
                            )
                            .put(
                                "status",
                                JSONObject()
                                    .put("type", "integer")
                                    .put("description", "Kodi i statusit të përgjigjes për add_intercept_replace; parazgjedhja 200.")
                            )
                    )
                    .put("required", JSONArray().put("action"))
            )
        )
    }

}
