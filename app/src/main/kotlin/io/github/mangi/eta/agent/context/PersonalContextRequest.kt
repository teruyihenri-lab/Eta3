package io.github.mangi.eta.agent.context

import java.math.BigDecimal
import java.time.DateTimeException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.json.JSONObject

internal class PersonalContextArgumentException(message: String) : IllegalArgumentException(message) {
    val code: String = "INVALID_ARGUMENT"
}

internal enum class PersonalContextAction(val wireName: String) {
    SOURCES("sources"), SEARCH("search"), READ("read"), COUNT("count"), BILL_SUMMARY("bill_summary");
}

internal enum class PersonalContextSort(val wireName: String) { NEWEST("newest"), OLDEST("oldest") }

internal data class PersonalContextRequest(
    val action: PersonalContextAction,
    val source: PersonalContextSource?,
    val query: String? = null,
    val start: Instant? = null,
    val end: Instant? = null,
    val limit: Int = 10,
    val offset: Int = 0,
    val sort: PersonalContextSort = PersonalContextSort.NEWEST,
    val id: String? = null,
) {
    companion object {
        const val MAX_QUERY_CHARACTERS = 200
        const val MAX_LIMIT = 30
        const val MAX_OFFSET = 10_000

        private val timePattern = Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}(?::[0-9]{2}(?:\\.[0-9]{1,9})?)?(?:Z|[+-][0-9]{2}:[0-9]{2})$")
        private val idPattern = Regex("^[0-9]{1,19}$")

        fun parse(arguments: JSONObject): PersonalContextRequest {
            val actionName = requiredString(arguments, "action", "Kërkohet action i vlefshëm")
            val action = PersonalContextAction.entries.firstOrNull { it.wireName == actionName }
                ?: invalid("Ky veprim i kontekstit personal nuk mbështetet")
            val source = if (arguments.has("source")) {
                PersonalContextSources.find(requiredString(arguments, "source", "source duhet të jetë burim i vlefshëm"))
                    ?: invalid("Ky burim i kontekstit personal nuk mbështetet")
            } else null
            if (action != PersonalContextAction.SOURCES && source == null) invalid("Ky veprim kërkon source")
            if (action == PersonalContextAction.BILL_SUMMARY && source?.id != "bills") invalid("bill_summary mbështet vetëm burimin bills")

            val query = optionalString(arguments, "query", "query duhet të jetë tekst")?.let { value ->
                if (value.codePointCount(0, value.length) > MAX_QUERY_CHARACTERS) invalid("query tejkalon 200 karaktere")
                if ('\u0000' in value || !Charsets.UTF_8.newEncoder().canEncode(value)) invalid("query përmban karaktere të pavlefshme")
                value.trim().takeIf(String::isNotEmpty)
            }
            val start = parseTime(arguments, "start_time")
            val end = parseTime(arguments, "end_time")
            if (start != null && end != null && start >= end) invalid("start_time duhet të jetë përpara end_time")
            if ((start != null || end != null) && source?.timeField == null) invalid("Ky burim nuk ka kohë të verifikuar për filtrimin sipas datës")
            source?.timeUnit?.toBounds(start, end)
            val limit = optionalInteger(arguments, "limit", 10, 1, MAX_LIMIT)
            val offset = optionalInteger(arguments, "offset", 0, 0, MAX_OFFSET)
            val sort = optionalString(arguments, "sort", "sort duhet të jetë newest ose oldest")?.let { value ->
                PersonalContextSort.entries.firstOrNull { it.wireName == value } ?: invalid("sort duhet të jetë newest ose oldest")
            } ?: PersonalContextSort.NEWEST
            val id = if (action == PersonalContextAction.READ) {
                val value = requiredString(arguments, "id", "read kërkon id numerike")
                if (!idPattern.matches(value)) invalid("id duhet të jetë numër jo negativ deri në 19 shifra")
                value.toLongOrNull()?.toString() ?: invalid("id tejkalon kufirin numerik")
            } else null
            return PersonalContextRequest(action, source, query, start, end, limit, offset, sort, id)
        }

        private fun parseTime(arguments: JSONObject, name: String): Instant? {
            val value = optionalString(arguments, name, "Koha duhet të jetë në format ISO 8601 me zonë kohore") ?: return null
            if (!timePattern.matches(value)) invalid("Koha duhet të ketë datë, orë dhe Z ose zhvendosje të zonës")
            return try {
                OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
            } catch (_: DateTimeException) {
                invalid("Data ose zona kohore është e pavlefshme")
            }
        }

        private fun optionalInteger(arguments: JSONObject, name: String, default: Int, minimum: Int, maximum: Int): Int {
            if (!arguments.has(name)) return default
            val value = arguments.opt(name)
            if (value !is Number) invalid("Parametri i faqeve duhet të jetë numër i plotë JSON")
            val integer = try { BigDecimal(value.toString()).intValueExact() } catch (_: NumberFormatException) {
                invalid("Parametri i faqeve duhet të jetë numër i plotë i kufizuar")
            } catch (_: ArithmeticException) {
                invalid("Parametri i faqeve duhet të jetë brenda intervalit")
            }
            if (integer !in minimum..maximum) invalid("Parametri i faqeve tejkalon kufirin")
            return integer
        }

        private fun optionalString(arguments: JSONObject, name: String, message: String): String? =
            if (arguments.has(name)) arguments.opt(name) as? String ?: invalid(message) else null

        private fun requiredString(arguments: JSONObject, name: String, message: String): String =
            arguments.opt(name) as? String ?: invalid(message)

        private fun invalid(message: String): Nothing = throw PersonalContextArgumentException(message)
    }
}
