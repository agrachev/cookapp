plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("schema-org-generator") {
            description = "Creates Schema.org classes"
            displayName = "Schema.org Class Generator"
            id = "ru.agrachev.schema-org-generator"
            implementationClass = "ru.agrachev.plugin.SchemaOrgGenerator"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.schema.org.generator)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
