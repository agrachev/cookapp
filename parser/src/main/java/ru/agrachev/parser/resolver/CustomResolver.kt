package ru.agrachev.parser.resolver

import com.weedow.schemaorg.commons.model.JsonLdNode
import org.jsoup.nodes.Element

internal interface CustomResolver<P : JsonLdNode> {
    val parent: P
    val entity: Any
    val element: Element
}
