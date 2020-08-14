import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
	repositories {
		mavenCentral()
		maven("https://dl.bintray.com/jetbrains/intellij-plugin-service")
	}
}

plugins {
	id("org.jetbrains.intellij") version "0.4.21"
	kotlin("jvm") version "1.3.72"
	idea
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly(kotlin("stdlib-jdk8"))
}

allprojects {
	group = "LastExceed"
	version = "1.0-SNAPSHOT"

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
			jvmTarget = "13"
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

defaultTasks("clean", "buildPlugin")
