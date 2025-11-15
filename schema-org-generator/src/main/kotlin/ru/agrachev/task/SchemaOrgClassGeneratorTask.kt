package ru.agrachev.task

import com.weedow.schemaorg.generator.SchemaModelGeneratorBuilder
import com.weedow.schemaorg.generator.core.GeneratorOptions
import com.weedow.schemaorg.generator.parser.ParserOptions
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import kotlin.io.path.isRegularFile

abstract class SchemaOrgClassGeneratorTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val schemaVersion: Property<String>

    @get:OutputDirectory
    @get:Optional
    abstract val outputFolder: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val models: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val modelPackage: Property<String>

    @get:Input
    @get:Optional
    abstract val modelImplPackage: Property<String>

    @get:Input
    @get:Optional
    abstract val dataTypePackage: Property<String>

    @get:Input
    @get:Optional
    abstract val verbose: Property<Boolean>

    private inline val outputFolderPath
        get() = outputFolder.orNull?.asFile?.toPath()

    @TaskAction
    fun invoke() {
        if (!isOutputDirectoryEmpty()) {
            return
        }
        also { task ->
            val parserOptions = ParserOptions().apply {
                schemaVersion =
                    task.schemaVersion.orElse(DEFAULT_SCHEMA_VERSION).get()
            }
            val generatorOptions = GeneratorOptions().apply {
                task.outputFolder.orNull?.let {
                    outputFolder = it.asFile.toPath()
                }
                task.models.orNull?.let {
                    models = it
                }
                task.modelPackage.orNull?.let {
                    modelPackage = it
                }
                task.modelImplPackage.orNull?.let {
                    modelImplPackage = it
                }
                task.dataTypePackage.orNull?.let {
                    dataTypePackage = it
                }
            }
            logger.lifecycle(
                "Generating Schema.Org models:" +
                        "\n\tVersion: ${parserOptions.schemaVersion}" +
                        "\n\tOutput folder: ${generatorOptions.outputFolder}" +
                        "\n\tModels: ${generatorOptions.models}" +
                        "\n\tModels package: ${generatorOptions.modelPackage}" +
                        "\n\tModels implementation package: ${generatorOptions.modelImplPackage}" +
                        "\n\tData type package: ${generatorOptions.dataTypePackage}"
            )
            SchemaModelGeneratorBuilder().apply {
                generatorOptions(generatorOptions)
                parserOptions(parserOptions)
                    .verbose(verbose.orElse(DEFAULT_VERBOSE).get())
                    .build()
                    .generate()
            }
            outputFolderPath?.let {
                Files.walk(it).use { stream ->
                    stream.filter { path ->
                        path.isRegularFile() && path.fileName.toString().startsWith("null")
                    }.forEach { path ->
                        val fileName = path.toString()
                        if (path.toFile().delete()) {
                            logger.lifecycle("Illegal file found, removing.\n$fileName")
                        }
                    }
                }
            }
            logger.lifecycle("Successfully generated Schema.Org models")
        }
    }

    private fun isOutputDirectoryEmpty(): Boolean =
        outputFolderPath?.let { path ->
            !(Files.exists(path) && Files.isDirectory(path)) ||
                    Files.newDirectoryStream(path).use { dir ->
                        !dir.iterator().hasNext()
                    }
        } ?: false
}

private const val DEFAULT_SCHEMA_VERSION = "latest"
private const val DEFAULT_VERBOSE = false
