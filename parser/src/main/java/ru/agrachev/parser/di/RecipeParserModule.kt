package ru.agrachev.parser.di

import com.weedow.schemaorg.commons.model.JsonLdNode
import com.weedow.schemaorg.serializer.deserialization.JsonLdDeserializer
import com.weedow.schemaorg.serializer.deserialization.JsonLdDeserializerImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.schema.model.HowToStep
import org.schema.model.Recipe
import ru.agrachev.parser.ElementToJsonLdDataConverter
import ru.agrachev.parser.JsonLdTypeMapper
import ru.agrachev.parser.ResolverCreator
import ru.agrachev.parser.SchemaParser
import ru.agrachev.parser.builder.JsonLdBuilderProvider
import ru.agrachev.parser.builder.JsonLdNodeBuilder
import ru.agrachev.parser.builder.JsonLdValueBuilder
import ru.agrachev.parser.data.PackageContentRepository
import ru.agrachev.parser.resolver.recipe.TestHowToStepResolver
import ru.agrachev.parser.resolver.strategy.CustomResolutionStrategy
import ru.agrachev.parser.resolver.strategy.ParsingResolutionStrategy
import ru.agrachev.parser.resolver.strategy.ResolutionContainer
import ru.agrachev.parser.transformer.RecipeSchemaDataProvider
import ru.agrachev.parser.validator.JsonLdTreeBreakdownValidator
import ru.agrachev.parser.validator.JsonLdValidator

fun recipeParserModule(packageContentRepositoryModule: Module) = module {

    includes(
        packageContentRepositoryModule
    )

    // The parser itself
    single {
        SchemaParser(
            get(named(VALIDATOR_CUSTOM)),
            get(),
            get(),
        )
    }

    // Type mapping
    single {
        provideObjectTypeMapper(get())
    }

    // Validators
    single<JsonLdValidator>(qualifier = named(VALIDATOR_DEFAULT)) {
        JsonLdValidator.Default
    }
    single<JsonLdValidator>(qualifier = named(VALIDATOR_CUSTOM)) {
        JsonLdTreeBreakdownValidator(get())
    }

    // Json LD deserializer
    single<JsonLdDeserializer> {
        JsonLdDeserializerImpl(get<JsonLdTypeMapper>())
    }

    singleOf(::ParsingResolutionStrategy)
    single(defaultResolutionContainerQualifier) {
        ResolutionContainer(listOf(get<ParsingResolutionStrategy>()))
    }

    // Builder provider
    single<JsonLdBuilderProvider> {
        object : JsonLdBuilderProvider {

            override fun provideValueBuilder(): JsonLdValueBuilder =
                get<JsonLdValueBuilder>()

            override fun <T : JsonLdNode> provideNodeBuilder(clazz: Class<out T>) =
                getOrNull<JsonLdNodeBuilder<T>>() ?: provideJsonLdNodeBuilder<T>().build()

            @Suppress("UNCHECKED_CAST")
            override fun <T : JsonLdNode> provideImplementation(clazz: Class<out T>) =
                get<JsonLdTypeMapper>()[clazz.simpleName]!!
                    .getConstructor().newInstance() as T
        }
    }

    // Json LD Value builder
    singleOf(::ElementToJsonLdDataConverter)
    singleOf(::JsonLdValueBuilder)

    // Json LD Node builders / delegates
    nodeBuilder<Recipe> {
        transformer { ::RecipeSchemaDataProvider }
    }
    nodeBuilder<HowToStep> {
        customResolver(::TestHowToStepResolver)
    }
}

internal typealias ListOfResolverCreators<E> = List<ResolverCreator<E>>

private inline fun <reified T : JsonLdNode> Module.nodeBuilder(
    crossinline initializerBlock: JsonLdNodeBuilder.Builder<T>.() -> Unit,
) = single {
    provideJsonLdNodeBuilder<T>()
        .run {
            initializerBlock()
            build()
        }
}

private inline fun <T : JsonLdNode> Scope.provideJsonLdNodeBuilder() =
    JsonLdNodeBuilder.Builder { resolverInitializers ->
        if (resolverInitializers.isEmpty()) {
            get<ResolutionContainer>(defaultResolutionContainerQualifier)
        } else {
            ResolutionContainer(
                resolutionStrategies = listOf(
                    get<ParsingResolutionStrategy>(),
                    CustomResolutionStrategy<T>(get(), resolverInitializers),
                ),
            )
        }
    }

private fun provideObjectTypeMapper(packageContentRepository: PackageContentRepository) = buildMap {
    packageContentRepository.getClassesInPackage("org.schema.model.impl")
        .forEach { clazz ->
            put(clazz.simpleName.trimImpl(), clazz)
        }
}

private fun String.trimImpl() = substring(0..<length - 4)

private val defaultResolutionContainerQualifier = named(DEFAULT_RESOLUTION_CONTAINER_NAME)

private const val VALIDATOR_DEFAULT = "default"
private const val VALIDATOR_CUSTOM = "custom"
private const val DEFAULT_RESOLUTION_CONTAINER_NAME = "aaa"
