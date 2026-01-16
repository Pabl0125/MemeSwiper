plugins {
    java
    application
    // Plugins para modularidad y JavaFX
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.1.0"
    // Plugin para crear el ejecutable nativo
    id("org.beryx.jlink") version "2.25.0"
}

group = "app.swiper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Versiones de dependencias
val junitVersion = "5.10.2"
val jacksonVersion = "2.18.2"

java {
    toolchain {
        // [Image of Java Toolchain version management in Gradle]
        // Esto garantiza que Gradle busque o descargue JDK 21 automáticamente
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("app.swiper.memeswiper")
    mainClass.set("app.swiper.memeswiper.HelloApplication")
}

javafx {
    // Bajamos a la versión 21 para máxima estabilidad con el SDK 21
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // UI y Controles extra
    implementation("org.controlsfx:controlsfx:11.2.1")

    // Persistencia JSON (Jackson)
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    // Pruebas unitarias
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Aseguramos que la tarea 'run' use el mismo JDK que el toolchain
tasks.withType<JavaExec> {
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
}

// Configuración para empaquetar la APP (Genera un .zip ejecutable)
jlink {
    imageZip.set(layout.buildDirectory.file("distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "MemeSwiper"
    }
}