plugins {
    `java-library`
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.schema.org.generator)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

schemaOrgGenerationParameters {
    outputFolder = layout.buildDirectory.dir("generated/src/main/java").get()
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        freeCompilerArgs.add("-Xwarning-level=NOTHING_TO_INLINE:disabled")
    }
}

dependencies {
    api(libs.jsoup)
    api(files("libs/schema-org.jar"))
    implementation(kotlin("reflect"))
    implementation(libs.schema.org.serializer) {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    implementation(libs.slf4j.api)
    implementation(libs.logback.android)
    implementation(libs.joda.time)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.junit)
}
