package ru.agrachev.parser.transformer

import com.weedow.schemaorg.commons.model.JsonLdNode

internal interface CustomSchemaDataProvider<P : JsonLdNode> {
    val parent: P
}
