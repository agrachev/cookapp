package ru.agrachev.parser.validator

internal interface JsonLdValidator {
    fun validate(json: String): String = json

    object Default : JsonLdValidator
}
