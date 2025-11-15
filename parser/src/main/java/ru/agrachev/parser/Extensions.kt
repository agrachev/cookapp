package ru.agrachev.parser

import ru.agrachev.parser.builder.JsonLdValue
import ru.agrachev.parser.transformer.CustomSchemaDataProvider
import com.weedow.schemaorg.commons.model.JsonLdDataType
import com.weedow.schemaorg.commons.model.JsonLdNode
import org.schema.model.HowToStep
import org.schema.model.ImageObject
import org.schema.model.ItemList
import org.schema.model.PropertyValue
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KFunction

internal typealias JsonLdTypeMapper = Map<String, Class<out JsonLdNode>>

internal inline val <T> Class<T>.isSchemaNode
    get() = JsonLdNode::class.java.isAssignableFrom(this)
internal inline val <T> Class<T>.isSchemaData
    get() = JsonLdDataType::class.java.isAssignableFrom(this)

@Suppress("UNCHECKED_CAST")
internal fun <T> Class<T>.toSchemaNode() = this as Class<out JsonLdNode>

@Suppress("UNCHECKED_CAST")
internal fun <T> Class<T>.toSchemaData() = this as Class<out JsonLdValue>

typealias CompletionCondition<T> = T.() -> Boolean

val a = buildMap {
    completionCondition<HowToStep> {
        text != null && getImage<Any?>().isCompleted()
    }
    completionCondition<ImageObject> {
        url != null
    }
    completionCondition<PropertyValue> {
        name != null && getValue<Any?>().isCompleted()
    }
    completionCondition<ItemList> {
        name != null
    }
}

inline fun <reified K : JsonLdNode> MutableMap<Class<out JsonLdNode>, CompletionCondition<*>>.completionCondition(
    noinline condition: CompletionCondition<K>,
) {
    put(K::class.java, condition)
}

@Suppress("UNCHECKED_CAST")
internal inline fun <T : JsonLdNode> T.isCompleted(): Boolean =
    (a[this::class.java.interfaces[0]] as? CompletionCondition<T>)?.let { this.it() } ?: true

internal inline fun <T> JsonLdDataType<T>.isCompleted() = value != null

internal inline fun <E> Collection<E>.isCompleted() =
    !isNullOrEmpty() and all { it.isCompleted() }

internal inline fun <E> E.isCompleted() =
    when (this) {
        is JsonLdDataType<*> -> this.isCompleted()
        is JsonLdNode -> this.isCompleted()
        else -> this != null
    }

internal inline val Field.typeClass
    get() = genericType.typeClass

private inline val Type.typeClass
    get() = when (this) {
        is ParameterizedType -> this.actualTypeArguments[0]
        else -> this
    } as Class<*>

internal typealias ResolverCreator<T> = () -> KFunction<T>
internal typealias SchemaDataProvider<T> = () -> KFunction<CustomSchemaDataProvider<T>>

@Suppress("UNCHECKED_CAST")
internal inline fun <T : JsonLdNode> SchemaDataProvider<T>?.getOrDefault(entity: T) =
    this?.invoke()?.call(entity) as? T ?: entity
