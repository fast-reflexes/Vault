import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.8.0"
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

javafx {
    // will pull in transitive modules
    modules("javafx.controls", "javafx.fxml") // replace with what you modules need

    // another option is to use:
    // modules = listOf("javafx.controls", "javafx.fxml")

    version = "19" // or whatever version you're using
}

group = "com.lousseief"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:1.7.20")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("de.jensd:fontawesomefx-commons:9.1.2")
    implementation("de.jensd:fontawesomefx-controls:9.1.2")
    implementation("de.jensd:fontawesomefx-emojione:3.1.1-9.1.2")
    implementation("de.jensd:fontawesomefx-fontawesome:4.7.0-9.1.2")
    implementation("de.jensd:fontawesomefx-materialdesignfont:2.0.26-9.1.2")
    implementation("de.jensd:fontawesomefx-materialicons:2.2.0-9.1.2")
    implementation("de.jensd:fontawesomefx-octicons:4.3.0-9.1.2")
    implementation("de.jensd:fontawesomefx-icons525:4.2.0-9.1.2")
    implementation("de.jensd:fontawesomefx-weathericons:2.0.10-9.1.2")
    implementation("org.openjfx:javafx-graphics:11")
    implementation("org.controlsfx:controlsfx:9.0.0")
    implementation("commons-codec:commons-codec:1.14")

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

application {
    mainClassName = "com.lousseief.vault.MainKt"

    //https://github.com/edvin/tornadofx/issues/899
    //As a response to InaccessibeObjectException: module javafx.graphics does not "opens javafx.scene" to unnamed module, problems occurring when using newer versions of Java or JavaFX
    applicationDefaultJvmArgs = listOf(
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED"
    )
}

