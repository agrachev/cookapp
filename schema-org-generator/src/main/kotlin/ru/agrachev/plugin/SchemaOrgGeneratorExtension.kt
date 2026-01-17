package ru.agrachev.plugin

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class SchemaOrgGeneratorExtension(project: Project) {

    private val objects = project.objects

    private val schemaVersionProperty = objects.property(String::class)
        .convention(null)
    private val outputFolderProperty = objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("src-gen"))
    private val modelsProperty = objects.listProperty(String::class)
        .convention(null)
    private val modelPackageProperty = objects.property(String::class)
        .convention(null)
    private val modelImplPackageProperty = objects.property(String::class)
        .convention(null)
    private val dataTypePackageProperty = objects.property(String::class)
        .convention(null)
    private val verboseProperty = objects.property(Boolean::class)
        .convention(false)

    var schemaVersion: String? by propertyDelegate(schemaVersionProperty)
    var outputFolder: Directory? by propertyDelegate(outputFolderProperty)
    var models: List<String>? by propertyListDelegate(modelsProperty)
    var modelPackage: String? by propertyDelegate(modelPackageProperty)
    var modelImplPackage: String? by propertyDelegate(modelImplPackageProperty)
    var dataTypePackage: String? by propertyDelegate(dataTypePackageProperty)
    var verbose: Boolean? by propertyDelegate(verboseProperty)

    private fun <V : Any> propertyDelegate(delegateProperty: Property<V>):
            PropertyDelegate<V> = object : PropertyDelegate<V> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): V? =
            delegateProperty.run { if (isPresent) get() else null }

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: V?,
        ) =
            delegateProperty.set(value)
    }

    private fun <V : Any> propertyListDelegate(delegateProperty: ListProperty<V>):
            PropertyDelegate<List<V>> = object : PropertyDelegate<List<V>> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): List<V>? =
            delegateProperty.run { if (isPresent) get() else null }

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: List<V>?,
        ) =
            delegateProperty.set(value)
    }
}

private typealias PropertyDelegate<V> = ReadWriteProperty<Any?, V?>
