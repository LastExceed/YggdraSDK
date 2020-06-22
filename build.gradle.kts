import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.4-M2"
	id("org.openjfx.javafxplugin") version "0.0.8"
}

allprojects {
	group = "LastExceed"
	version = "1.0-SNAPSHOT"

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "13"
			freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
		}
	}
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "org.jetbrains.kotlin.jvm")

	repositories {
		mavenCentral()
		jcenter()
		maven("https://dl.bintray.com/kotlin/kotlin-eap")
		setOf(
			"kotlin-eap",
			"ktor",
			"kotlinx"
		).forEach {
			maven("https://dl.bintray.com/kotlin/$it")
		}
	}

	dependencies {
		implementation(kotlin("stdlib-jdk8"))
		implementation("io.ktor", "ktor-network", "1.3.2-1.4-M2")
		implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.7-1.4-M2")
	}
}

project(":server") {
	dependencies {
		implementation(project(":common"))
		setOf(
			"core",
			"dao",
			"jdbc",
			"java-time"
		).forEach { implementation("org.jetbrains.exposed", "exposed-$it", "0.24.1") }
		implementation("org.xerial","sqlite-jdbc","3.30.1")
		implementation("org.slf4j","slf4j-simple","1.7.25")
	}
}

project(":client") {
	apply(plugin = "org.openjfx.javafxplugin")
	repositories {
		maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
	}
	javafx {
		version = "14"
		modules(
			"javafx.controls",
			"javafx.fxml",
			"javafx.media",
			"javafx.web",
			"javafx.swing"
		)
	}
	dependencies {
		implementation(project(":common"))
		implementation("no.tornado","tornadofx","2.0.0-SNAPSHOT")
		implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-javafx", "1.3.7-1.4-M2")
	}
}
