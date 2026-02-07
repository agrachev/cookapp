package ru.agrachev.parser.resolver.recipe

import org.jsoup.nodes.Element
import org.schema.model.HowToStep
import org.schema.model.ImageObject
import org.schema.model.datatype.Text
import ru.agrachev.parser.resolver.CustomResolver

internal class TestHowToStepResolver : CustomResolver<HowToStep> {

    override fun invoke(entity: HowToStep, element: Element): HowToStep {
        when {
            entity.text == null -> {
                entity.addText(Text.of(entity.getImage<ImageObject>()?.url?.let {
                    element.selectFirst(SRC_ATTRIBUTE_SELECTOR.format(it.value))
                        ?.parent()?.text().orEmpty()
                } ?: null.orEmpty()))
            }

            entity.getImage<Any?>() == null -> {

            }
        }
        return entity
    }
}

private const val SRC_ATTRIBUTE_SELECTOR = "[src=$%s]"
