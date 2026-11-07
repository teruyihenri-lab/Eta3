package io.github.mangi.eta.agent.context

import java.math.BigInteger
import java.time.Instant

internal enum class PersonalContextFieldKind { TEXT, INTEGER, DECIMAL }

internal data class PersonalContextTimeBounds(val startInclusive: Long?, val endExclusive: Long?)

internal enum class PersonalContextTimeUnit(val unitsPerSecond: Long, val requiresNumericCast: Boolean = false) {
    MILLISECONDS(1_000),
    SECONDS(1),
    TEXT_MILLISECONDS(1_000, requiresNumericCast = true);

    /** 整数时间戳的左右边界都取 ceil，才能精确保留原始左闭右开区间。 */
    fun toBounds(start: Instant?, end: Instant?): PersonalContextTimeBounds = try {
        PersonalContextTimeBounds(start?.let(::ceiling), end?.let(::ceiling))
    } catch (_: ArithmeticException) {
        throw PersonalContextArgumentException("Intervali kohor tejkalon kufirin e burimit")
    }

    private fun ceiling(value: Instant): Long {
        val nanosPerUnit = 1_000_000_000L / unitsPerSecond
        val fractional = (value.nano.toLong() + nanosPerUnit - 1) / nanosPerUnit
        return BigInteger.valueOf(value.epochSecond).multiply(BigInteger.valueOf(unitsPerSecond))
            .add(BigInteger.valueOf(fractional)).longValueExact()
    }
}

internal data class PersonalContextField(
    val logicalName: String,
    val physicalCandidates: List<String>,
    val kind: PersonalContextFieldKind = PersonalContextFieldKind.TEXT,
    val required: Boolean = false,
    val exposed: Boolean = true,
    val timeUnit: PersonalContextTimeUnit? = null,
    val description: String? = null,
)

internal enum class PersonalContextFilterOperator { EQUALS, NULL_OR_EQUALS }

internal data class PersonalContextFixedFilter(
    val fieldName: String,
    val operator: PersonalContextFilterOperator,
    val value: Long,
)

internal data class PersonalContextSource(
    val id: String,
    val label: String,
    val resource: String,
    val fields: List<PersonalContextField>,
    val searchFields: List<String>,
    val timeField: String?,
    val timeUnit: PersonalContextTimeUnit?,
    val timeDescription: String?,
    val fixedFilters: List<PersonalContextFixedFilter>,
) {
    init {
        require(fields.map { it.logicalName }.distinct().size == fields.size)
        require(searchFields.all { name -> fields.any { it.logicalName == name && it.exposed } })
        require((timeField == null) == (timeUnit == null))
        require(timeField == null || field(timeField)?.timeUnit == timeUnit)
        require(fixedFilters.all { field(it.fieldName)?.required == true })
    }

    fun field(logicalName: String): PersonalContextField? = fields.firstOrNull { it.logicalName == logicalName }

    /** 所有 action 都必须先确认这些列，不能因保护列缺失而移除过滤条件。 */
    val probeRequired: List<PersonalContextField> get() = fields.filter { it.required }
    val exposedFields: List<PersonalContextField> get() = fields.filter { it.exposed }
    val sortField: String? get() = timeField ?: field("indexed_time")?.logicalName
    val sortTimeKind: String get() = if (timeField == null) "indexed" else "business"
}

internal object PersonalContextSources {
    const val BILL_CURRENCY = "CNY"
    const val BILL_AMOUNT_UNIT = "yuan"
    const val BILL_EXPENSES = "expenses"
    const val BILL_INCOME = "income"

    private val valid = PersonalContextField("_valid", listOf("valid"), PersonalContextFieldKind.INTEGER, required = true, exposed = false)
    private val stableId = PersonalContextField("id", listOf("docID"), PersonalContextFieldKind.INTEGER, required = true)
    private val validFilter = PersonalContextFixedFilter("_valid", PersonalContextFilterOperator.EQUALS, 1)

    val all: List<PersonalContextSource> = listOf(
        source(
            "photos", "Fotografi dhe pamje ekrani", "gallery",
            listOf(
                integer("origin_id", "media_idCom", required = true), text("uri", "uriCom"),
                text("title", "titleCom"), text("text", "ocrCom"),
                text("caption", "caption_content0Com", "caption_contentCom"), text("tags", "tagsCom"),
                text("album", "bucket_nameCom"), text("location", "locationCom"),
                protection("_media_type", "typeCom"), time("time", "date_modifiedCom", PersonalContextTimeUnit.SECONDS),
                time("taken_time", "datetakenCom"),
            ), listOf("text", "caption", "tags", "location", "album"), "time", PersonalContextTimeUnit.SECONDS,
            "Koha e ndryshimit në indeks në sekonda; data e fotografimit është te taken_time dhe nuk tregon gjendjen aktuale", listOf(equal("_media_type", 1)),
        ),
        source(
            "notes", "Shënimet", "com.coloros.note#note",
            listOf(
                text("origin_id", "local_idCom"), text("title", "titleCom"), text("text", "textCom"),
                text("folder", "folder_nameCom"), time("created_time", "create_timeCom"), time("time", "update_timeCom"),
                protection("_recycled", "recycle_timeCom"), protection("_encrypted", "encryptedCom"),
            ), listOf("title", "text", "folder"), "time", PersonalContextTimeUnit.MILLISECONDS, "Koha e fundit e ndryshimit të shënimit",
            listOf(equal("_recycled", 0), equal("_encrypted", 0)),
        ),
        source(
            "recordings", "Regjistrimet", "recorder",
            listOf(
                integer("origin_id", "idCom"), text("title", "display_nameCom"), text("audio_path", "media_pathCom"),
                text("transcript_path", "text_pathCom"), integer("duration_ms", "durationCom"), text("folder", "bucketCom"),
                integer("has_transcript", "supportContentCom"), integer("has_summary", "summary_text_flagCom"),
                time("time", "date_modifiedCom"),
            ), listOf("title", "folder"), "time", PersonalContextTimeUnit.MILLISECONDS, "Koha e fundit e ndryshimit; rruga e transkriptimit nuk është vetë teksti",
        ),
        source(
            "calendar", "Kalendari", "calendar",
            listOf(
                text("origin_id", "idCom"), text("title", "nameCom"), text("text", "descriptionCom"), text("location", "locationCom"),
                time("time", "startTimeCom"), time("end_time", "endTimeCom"), text("timezone", "timezoneCom"),
                text("recurrence", "isRepeatCom"), integer("all_day", "allDayIndexCom"),
            ), listOf("title", "text", "location"), "time", PersonalContextTimeUnit.MILLISECONDS,
            "Koha e fillimit; ngjarjet e përsëritura varen nga indeksi i sistemit. instance_id identifikon një rast; leximi sipas id nuk garanton të gjitha përsëritjet",
        ),
        source(
            "calendar_todos", "Detyrat e kalendarit", "calendarTodo",
            listOf(
                text("origin_id", "idCom"), text("text", "contentCom"), text("source_package", "create_packageCom"),
                time("time", "start_timeCom", PersonalContextTimeUnit.TEXT_MILLISECONDS),
                time("planned_end_time", "finish_timeCom", PersonalContextTimeUnit.TEXT_MILLISECONDS),
                text("recurrence", "rruleCom"), text("reminders", "remindTimesCom"), integer("starred", "starCom"),
                time("source_created_time", "create_timeCom", PersonalContextTimeUnit.TEXT_MILLISECONDS, "Koha e krijimit nga burimi, jo për filtrimin sipas fillimit të planit"),
            ), listOf("text", "source_package"), "time", PersonalContextTimeUnit.TEXT_MILLISECONDS,
            "Fillimi i planifikuar i detyrës; përsëritjet mund të mos kenë ID. Fundi i planifikuar nuk do të thotë se detyra u përfundua",
        ),
        source(
            "todos", "Detyrat", "com.oplus.deepthinker#donate_todo_record",
            listOf(
                text("origin_id", "idCom"), text("title", "todoTitleCom"), text("text", "todoDescriptionCom"),
                text("context", "descriptionCom"), text("original_text", "originalTodoCom"), text("status", "statusCom"),
                text("origin", "sourceCom"), time("time", "eventStartTimeMillisCom", description = "0 tregon se nuk është njohur koha e fillimit"),
                time("deadline", "todoDeadlineTimeCom", description = "Vlera deri në 0 tregon se afati nuk është caktuar"),
                time("indexed_time", "creationTimestampMillisCom", description = "Koha e krijimit të regjistrit, jo koha e ngjarjes"),
            ), listOf("title", "text", "context", "original_text"), "time", PersonalContextTimeUnit.MILLISECONDS,
            "Fillimi i njohur i detyrës; 0 do të thotë i panjohur. Filtri nuk kërkon data të shkruara vetëm në tekst",
        ),
        source(
            "memories", "Kujtesa e sistemit", "com.oplus.aimemory#donate_AIMemory",
            listOf(
                text("origin_id", "memoryIdCom"), text("title", "titleCom"), text("text", "dataTextCom"),
                text("summary", "summaryCom", "titleSummaryCom"), text("description", "descriptionCom"),
                text("notes", "notesCom"), text("memory_type", "memoryTypeCom"), text("source_app", "appNameCom"),
                time("time", "createdTimeCom"), time("updated_time", "updateTimeCom"),
                time("indexed_time", "creationTimestampMillisCom", description = "Koha e indeksimit në sistem, jo e krijimit të kujtimit"),
                protection("_recycled", "recycleTimeCom"),
            ), listOf("title", "text", "summary", "description", "notes", "source_app"), "time", PersonalContextTimeUnit.MILLISECONDS,
            "Koha e krijimit të kujtimit", listOf(nullOrEqual("_recycled", 0)),
        ),
        source(
            "bills", "Faturat", "com.oplus.aimemory#billsDonate_Bills",
            listOf(
                text("origin_id", "billIdCom"), text("title", "productNameCom"), text("merchant", "merchantNameCom"),
                PersonalContextField("amount", listOf("amountCom"), PersonalContextFieldKind.DECIMAL, description = "Shuma është në juanë kinezë"),
                text("category", "categoryCom"), text("transaction_type", "transactionTypeCom"), text("transaction_status", "transactionStatusCom"),
                time("transaction_time", "transactionTimeCom"), text("payment_source", "paymentSourceCom"),
                protection("_recycled", "recycleTimeCom"),
            ), listOf("title", "merchant", "category", "transaction_status", "payment_source"), "transaction_time", PersonalContextTimeUnit.MILLISECONDS,
            "Koha e transaksionit; gjendja shfaqet siç është, pa llogaritur rimbursime të supozuara", listOf(nullOrEqual("_recycled", 0)),
        ),
        source(
            "collections", "Koleksioni i kujtimeve", "com.oplus.aimemory#collectionDonate_Collection",
            listOf(
                text("origin_id", "collectionIdCom"), text("title", "collectionNameCom"), text("text", "collectionSummaryCom"),
                text("summary_title", "collectionSummaryTitleCom"), text("member_ids", "memoryIdsCom"),
                time("indexed_time", "creationTimestampMillisCom", description = "Vetëm koha e indeksimit, jo e krijimit të koleksionit"),
            ), listOf("title", "text", "summary_title"), null, null,
            "Nuk ka datë biznesi të konfirmuar; filtrimi sipas saj nuk mbështetet",
        ),
        source(
            "events", "Ngjarjet e jetës", "com.oplus.deepthinker#donate_daily_event",
            listOf(text("title", "eventNameCom"), text("text", "eventDescriptionCom"), time("time", "startTimeMillisCom"),
                time("end_time", "endTimeMillisCom", description = "Vlera bosh ose 0 mund të tregojë se ngjarja vazhdon")),
            listOf("title", "text"), "time", PersonalContextTimeUnit.MILLISECONDS, "Koha e fillimit; ngjarjet e nxjerra nuk janë fakte të konfirmuara",
        ),
        source(
            "notifications", "Historiku i njoftimeve", "com.oplus.deepthinker#donate_notification",
            listOf(
                text("origin_id", "notificationKeyCom"), text("title", "titleCom"), text("text", "descriptionCom"),
                text("subtext", "subTextCom"), text("source_package", "packageNameCom"), text("channel", "channelIdCom"),
                time("time", "eventTimeCom"), time("indexed_time", "createTimeCom", description = "Koha e krijimit të indeksit, jo e njoftimit"),
            ), listOf("title", "text", "subtext", "source_package"), "time", PersonalContextTimeUnit.MILLISECONDS,
            "Koha e njoftimit historik, jo e panelit aktual ose e statusit të palexuar",
        ),
        source(
            "files", "Skedarët lokalë", "file",
            listOf(
                text("origin_id", "idCom"), text("name", "filenameCom"), text("title", "titleCom"), text("text", "highlight"),
                text("path", "absolutePathCom"), integer("size_bytes", "sizeCom"), integer("file_type", "typeCom"), time("time", "lastModifiedCom"),
            ), listOf("name", "title", "text"), "time", PersonalContextTimeUnit.MILLISECONDS,
            "Koha e fundit e ndryshimit në indeks; rruga nuk garanton që skedari ekziston ende",
        ),
        source(
            "app_files", "Skedarët e aplikacioneve", "com.coloros.filemanager#third_app_file",
            listOf(text("name", "nameCom"), text("source_package", "packageCom"), text("source_app", "source_nameCom"),
                integer("size_bytes", "sizeCom"), time("time", "file_timeCom")),
            listOf("name", "source_package", "source_app"), "time", PersonalContextTimeUnit.MILLISECONDS, "Koha e skedarit nga burimi",
        ),
        source(
            "flights", "Fluturimet", "standard_data#flight",
            listOf(
                text("origin_id", "idCom"), text("title", "titleCom"), text("text", "descriptionCom"), text("number", "flightNumberCom"),
                text("carrier", "airlineNameCom"), text("departure", "departureAirportNameCom"), text("arrival", "arrivalAirportNameCom"),
                text("status", "flightStatusCom"), time("time", "planDepartureTimestampCom"), time("arrival_time", "planArrivalTimestampCom"),
            ), listOf("title", "text", "number", "carrier", "departure", "arrival"), "time", PersonalContextTimeUnit.MILLISECONDS, "Koha e planifikuar e fluturimit",
        ),
        source(
            "hotels", "Rezervimet e hoteleve", "standard_data#hotel",
            listOf(
                text("origin_id", "idCom"), text("title", "hotelNameCom"), text("text", "descriptionCom"), text("address", "addressCom"),
                text("order_number", "orderNumberCom"), text("room_type", "room_typeCom"), text("status", "statusCom"),
                time("time", "checkInTimestampCom"), time("checkout_time", "checkoutTimestampCom"),
            ), listOf("title", "text", "address", "order_number"), "time", PersonalContextTimeUnit.MILLISECONDS, "Koha e rezervimit të hyrjes",
        ),
        source(
            "trains", "Udhëtimet me tren", "standard_data#train",
            listOf(
                text("origin_id", "idCom"), text("title", "titleCom"), text("text", "descriptionCom"), text("number", "trainNumberCom"),
                text("departure", "departureStationNameCom"), text("arrival", "arrivalStationNameCom"), integer("status", "trainStatusCom"),
                time("time", "planDepartureTimestampCom"), time("arrival_time", "planArrivalTimestampCom"),
            ), listOf("title", "text", "number", "departure", "arrival"), "time", PersonalContextTimeUnit.MILLISECONDS, "Koha e planifikuar e nisjes",
        ),
    )

    private val byId = all.associateBy { it.id }
    val ids: Set<String> get() = byId.keys
    fun find(id: String): PersonalContextSource? = byId[id]

    private fun source(
        id: String,
        label: String,
        resource: String,
        fields: List<PersonalContextField>,
        searchFields: List<String>,
        timeField: String?,
        timeUnit: PersonalContextTimeUnit?,
        timeDescription: String?,
        extraFilters: List<PersonalContextFixedFilter> = emptyList(),
    ) = PersonalContextSource(id, label, resource, listOf(stableId, valid) + fields, searchFields, timeField, timeUnit, timeDescription,
        listOf(validFilter) + extraFilters)

    private fun text(name: String, vararg physical: String) = PersonalContextField(name, physical.toList())
    private fun integer(name: String, physical: String, required: Boolean = false) =
        PersonalContextField(name, listOf(physical), PersonalContextFieldKind.INTEGER, required = required)
    private fun time(name: String, physical: String, unit: PersonalContextTimeUnit = PersonalContextTimeUnit.MILLISECONDS, description: String? = null) =
        PersonalContextField(name, listOf(physical), PersonalContextFieldKind.INTEGER, timeUnit = unit, description = description)
    private fun protection(name: String, physical: String) =
        PersonalContextField(name, listOf(physical), PersonalContextFieldKind.INTEGER, required = true, exposed = false)
    private fun equal(name: String, value: Long) = PersonalContextFixedFilter(name, PersonalContextFilterOperator.EQUALS, value)
    private fun nullOrEqual(name: String, value: Long) = PersonalContextFixedFilter(name, PersonalContextFilterOperator.NULL_OR_EQUALS, value)
}
