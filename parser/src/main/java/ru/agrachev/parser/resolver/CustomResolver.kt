package ru.agrachev.parser.resolver

import com.weedow.schemaorg.commons.model.JsonLdNode
import org.jsoup.nodes.Element

internal fun interface CustomResolver<P : JsonLdNode> {

    operator fun invoke(entity: P, element: Element): P
}
