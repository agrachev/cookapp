package ru.agrachev.parser

import org.joda.time.DateTime
import org.jsoup.nodes.Element
import java.net.URI
import java.net.URL
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

internal class ElementToJsonLdDataConverter {

    private val numberFormat by lazy {
        NumberFormat.getInstance()
    }
    private val defaultZoneId by lazy {
        ZoneId.of("UTC")
    }


    fun asInteger(element: Element): Int = try {
        numberFormat.parse(asText(element)) as Int
    } catch (_: Exception) {
        0
    }

    fun asNumber(element: Element): Number = asInteger(element)

    fun asText(element: Element): String = element.attribute("content")?.value ?: element.text()

    fun asUrl(element: Element): URL? = element.attributes()
        .firstOrNull { it.key in URL_ATTRS }?.value?.let { href ->
            try {
                URI.create(
                    when {
                        href.startsWith("//") -> "https:$href"
                        else -> href
                    }
                ).toURL()
            } catch (_: Exception) {
                null
            }
        }

    fun asBoolean(element: Element): Boolean = asText(element).toBoolean()

    fun asDate(element: Element): LocalDate = toLocalDateTime(element).toLocalDate()

    fun asTime(element: Element): LocalTime = toLocalDateTime(element).toLocalTime()

    fun asDateTime(element: Element): LocalDateTime = toLocalDateTime(element)

    private fun toLocalDateTime(element: Element) = try {
        DateTime.parse(asText(element)).toDate().run {
            LocalDateTime.ofInstant(this.toInstant(), defaultZoneId)
        }
    } catch (_: Exception) {
        LocalDateTime.now()
    }
}

private val URL_ATTRS = setOf("href", "src")
