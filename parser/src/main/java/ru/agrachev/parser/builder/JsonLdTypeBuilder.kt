package ru.agrachev.parser.builder

import org.jsoup.nodes.Element

internal interface JsonLdTypeBuilder<R, T> {

    fun build(element: Element, entity: T): R?
}
