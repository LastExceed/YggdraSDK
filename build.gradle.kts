import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.3.72"
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

	buildscript {
		repositories {
			mavenCentral()
		}

		dependencies {
			classpath("org.jetbrains.kotlin","kotlin-gradle-plugin","1.3.72")
		}
	}
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "org.jetbrains.kotlin.jvm")

	repositories {
		mavenCentral()
		jcenter()
	}

	dependencies {
		implementation(kotlin("stdlib-jdk8"))
		implementation("io.ktor", "ktor-network", "1.3.1")
		implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.8")
		testImplementation(kotlin("test-junit5"))
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
		).forEach { implementation("org.jetbrains.exposed", "exposed-$it", "0.25.1") }
		implementation("org.xerial","sqlite-jdbc","3.30.1")
		implementation("org.slf4j","slf4j-simple","1.7.25")
		testImplementation("com.zaxxer","HikariCP","3.4.2")
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
		implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-javafx", "1.3.8")
	}
}
