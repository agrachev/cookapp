package ru.agrachev.cookapp.data

import android.content.Context
import ru.agrachev.parser.data.PackageContentRepository
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexFile
import java.lang.reflect.Field

class AndroidPackageContentRepository(
    private val context: Context,
) : PackageContentRepository {

    override fun getClassesInPackage(packageName: String): List<Class<*>> {
        return getDexFiles()
            .flatMap { it.entries().asSequence() }
            .filter { it.startsWith(packageName) }
            .map { context.classLoader.loadClass(it) }
            .toList()
    }

    private fun getDexFiles(): Sequence<DexFile> {
        // Here we do some reflection to access the dex files from the class loader. These implementation details vary by platform version,
        // so we have to be a little careful, but not a huge deal since this is just for testing. It should work on 21+.
        // The source for reference is at:
        // https://android.googlesource.com/platform/libcore/+/oreo-release/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
        val classLoader = context.classLoader as BaseDexClassLoader

        val pathListField = field("dalvik.system.BaseDexClassLoader", "pathList")
        val pathList = pathListField.get(classLoader) // Type is DexPathList

        val dexElementsField = field("dalvik.system.DexPathList", "dexElements")

        @Suppress("UNCHECKED_CAST")
        val dexElements =
            dexElementsField.get(pathList) as Array<Any> // Type is Array<DexPathList.Element>

        val dexFileField = field("dalvik.system.DexPathList\$Element", "dexFile")
        return dexElements
            .map { dexFileField.get(it) as DexFile }
            .asSequence()
    }

    private fun field(className: String, fieldName: String): Field {
        val clazz = Class.forName(className)
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        return field
    }
}
