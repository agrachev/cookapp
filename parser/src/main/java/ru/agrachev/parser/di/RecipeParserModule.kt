package ru.agrachev.parser.di

import ru.agrachev.parser.builder.JsonLdBuilderProvider
import ru.agrachev.parser.builder.JsonLdNodeBuilder
import ru.agrachev.parser.builder.JsonLdValueBuilder
import ru.agrachev.parser.data.PackageContentRepository
import com.weedow.schemaorg.commons.model.JsonLdNode
import com.weedow.schemaorg.serializer.deserialization.JsonLdDeserializer
import com.weedow.schemaorg.serializer.deserialization.JsonLdDeserializerImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.schema.model.Recipe
import ru.agrachev.parser.ElementToJsonLdDataConverter
import ru.agrachev.parser.JsonLdTypeMapper
import ru.agrachev.parser.RecipeParser
import ru.agrachev.parser.ResolverCreator
import ru.agrachev.parser.resolver.recipe.TestRecipeResolver
import ru.agrachev.parser.resolver.strategy.ParsingResolutionStrategy
import ru.agrachev.parser.resolver.strategy.ResolutionContainer
import ru.agrachev.parser.resolver.strategy.ResolutionStrategy
import ru.agrachev.parser.transformer.RecipeSchemaDataProvider
import ru.agrachev.parser.validator.JsonLdTreeBreakdownValidator
import ru.agrachev.parser.validator.JsonLdValidator

fun recipeParserModule(packageContentRepositoryModule: Module) = module {

    includes(
        packageContentRepositoryModule
    )

    // The parser itself
    single { RecipeParser(get(named(VALIDATOR_CUSTOM)), get(), get()) }

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

    single<Collection<ResolutionStrategy>> {
        listOf(ParsingResolutionStrategy(get()))
    }
    singleOf(::ResolutionContainer)

    // Builder provider
    single<JsonLdBuilderProvider> {
        object : JsonLdBuilderProvider {
            override fun provideValueBuilder(): JsonLdValueBuilder =
                get<JsonLdValueBuilder>()

            override fun <T : JsonLdNode> provideNodeBuilder(clazz: Class<out T>): JsonLdNodeBuilder<T> =
                getOrNull(qualifier = named(clazz.name)) ?: JsonLdNodeBuilder.Builder<T>(get())
                    .build()

            @Suppress("UNCHECKED_CAST")
            override fun <T : JsonLdNode> provideImplementation(
                clazz: Class<out T>
            ): T {
                val mapper: JsonLdTypeMapper = get()
                return mapper[clazz.simpleName]!!.getConstructor().newInstance() as T
            }

        }
    }

    // Json LD Value builder
    singleOf(::ElementToJsonLdDataConverter)
    singleOf(::JsonLdValueBuilder)

    // Json LD Node builders / delegates
    nodeBuilder<Recipe> {
        transformer { ::RecipeSchemaDataProvider }
        //resolver { ::TestRecipeResolver }
    }
}

private inline fun <reified T : JsonLdNode> Module.nodeBuilder(
//    crossinline resolverInitializer: ListOfResolverCreators<T>.() -> Unit,
    crossinline initializerBlock: JsonLdNodeBuilder.Builder<T>.() -> Unit
) {
    single(named(T::class.java.name)) {
        with(JsonLdNodeBuilder.Builder<T>(get())) {
            initializerBlock()
            build()
        }
    }
}

val list = buildList<ResolverCreator<Recipe>> {
    resolver { ::TestRecipeResolver }
}

internal typealias ListOfResolverCreators<E> = MutableList<ResolverCreator<E>>

internal inline fun <E> MutableList<ResolverCreator<E>>.resolver(
    noinline creator: ResolverCreator<E>,
) {
    this.add(creator)
}

private fun provideObjectTypeMapper(packageContentRepository: PackageContentRepository) = buildMap {
    packageContentRepository.getClassesInPackage("org.schema.model.impl")
        .forEach { clazz ->
            put(clazz.simpleName.trimImpl(), clazz)
        }
}

private fun String.trimImpl() = substring(0..<length - 4)

private const val VALIDATOR_DEFAULT = "default"
private const val VALIDATOR_CUSTOM = "custom"
