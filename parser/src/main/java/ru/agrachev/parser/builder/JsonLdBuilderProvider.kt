package ru.agrachev.parser.builder

import com.weedow.schemaorg.commons.model.JsonLdNode

internal interface JsonLdBuilderProvider {

    fun provideValueBuilder(): JsonLdValueBuilder

    fun <T : JsonLdNode> provideNodeBuilder(clazz: Class<out T>): JsonLdNodeBuilder<T>

    fun <T : JsonLdNode> provideImplementation(clazz: Class<out T>): T
}
