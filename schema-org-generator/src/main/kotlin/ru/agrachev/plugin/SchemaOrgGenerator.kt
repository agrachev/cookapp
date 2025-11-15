package ru.agrachev.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named
import ru.agrachev.task.SchemaOrgClassGeneratorTask

@Suppress("unused")
class SchemaOrgGenerator : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create(
            "schemaOrgGenerationParameters", SchemaOrgGeneratorExtension::class.java, target
        )
        with(target.tasks) {
            val generatorTask = register(
                "schemaOrgClassGeneratorTask", SchemaOrgClassGeneratorTask::class.java
            ) {
                group = "Code generation"
                description = "Generates simple Kotlin class with single message property"

                schemaVersion.value(extension.schemaVersion)
                outputFolder.value(extension.outputFolder)
                models.value(extension.models)
                modelPackage.value(extension.modelPackage)
                modelImplPackage.value(extension.modelImplPackage)
                dataTypePackage.value(extension.dataTypePackage)
                verbose.value(extension.verbose)
            }
            val compileDestinationDir = target.layout
                .buildDirectory.dir("generated/classes")
            val compilerTask = register(
                "compileSchemaOrgSources", JavaCompile::class.java
            ) {
                val defaultCompilerTask = project.tasks.named<JavaCompile>("compileJava").get()
                description = "Compiles Schema.org Sources"

                destinationDirectory.set(compileDestinationDir)
                source += project.fileTree(generatorTask.map { it.outputFolder.get() })
                classpath = defaultCompilerTask.classpath
            }
            val libraryBuilderTask = register(
                "generateSchemaOrgLibraryJar", Jar::class.java
            ) {
                group = "build"
                description = "Generates Schema.org Jar library"

                from(compileDestinationDir)
                excludes += "com/**"
            }
            compilerTask.get().dependsOn(generatorTask)
            libraryBuilderTask.get().dependsOn(compilerTask)
        }
    }
}
