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
defaultTasks("clean", "buildPlugin")
