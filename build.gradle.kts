plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "2.25.0"
}

group = "app.swiper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"
// Definimos la versión de Jackson para mantener consistencia si añadimos más módulos
val jacksonVersion = "2.18.2"

java {
    toolchain {
        // Nota: Java 25 es una versión futura/Early Access. Asegúrate de tener el JDK instalado.
        languageVersion = JavaLanguageVersion.of(25)
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
    version = "24.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}