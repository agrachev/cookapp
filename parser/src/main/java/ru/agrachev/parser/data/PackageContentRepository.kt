package ru.agrachev.parser.data

interface PackageContentRepository {
    fun getClassesInPackage(packageName: String): List<Class<*>>
}
