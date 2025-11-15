package ru.agrachev.parser.builder

import com.weedow.schemaorg.commons.model.JsonLdDataType
import org.jsoup.nodes.Element
import org.schema.model.datatype.Boolean
import org.schema.model.datatype.Date
import org.schema.model.datatype.DateTime
import org.schema.model.datatype.Integer
import org.schema.model.datatype.Number
import org.schema.model.datatype.Text
import org.schema.model.datatype.Time
import org.schema.model.datatype.URL
import ru.agrachev.parser.ElementToJsonLdDataConverter
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.isAccessible

typealias JsonLdValue = JsonLdDataType<*>

internal class JsonLdValueBuilder(
    dataConverter: ElementToJsonLdDataConverter,
) : JsonLdTypeBuilder<JsonLdValue, Class<out JsonLdValue>> {

    private val mapper = mapOf<Class<out JsonLdValue>, KFunction<*>>(
        classOf<Number>() to dataConverter::asNumber,
        classOf<Integer>() to dataConverter::asInteger,
        classOf<Text>() to dataConverter::asText,
        classOf<URL>() to dataConverter::asUrl,
        classOf<Boolean>() to dataConverter::asBoolean,
        classOf<Date>() to dataConverter::asDate,
        classOf<Time>() to dataConverter::asTime,
        classOf<DateTime>() to dataConverter::asDateTime,
    )

    override fun build(
        element: Element, entity: Class<out JsonLdValue>,
    ): JsonLdValue? =
        mapper[entity]?.let { converter ->
            converter.isAccessible = true
            converter.call(element)
                ?.let { converterData ->
                    entity.getMethod(
                        BUILDER_METHOD_NAME,
                        converterData::class.java
                    )
                        .invoke(null, converterData) as JsonLdValue
                }
        }

    private inline fun <reified T : JsonLdValue> classOf(): Class<T> = T::class.java

    companion object {
        private const val BUILDER_METHOD_NAME = "of"
    }
}
