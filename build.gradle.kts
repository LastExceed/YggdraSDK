import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
	repositories {
		mavenCentral()
		maven("https://dl.bintray.com/jetbrains/intellij-plugin-service")
	}
}

plugins {
	java
	id("org.jetbrains.intellij") version "0.4.21"
	kotlin("jvm") version "1.3.72"
	idea
}

group = "LastExceed"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

java {
	sourceCompatibility = JavaVersion.VERSION_13
	targetCompatibility = JavaVersion.VERSION_13
}

dependencies {
	compileOnly(kotlin("stdlib-jdk8"))
}


tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = JavaVersion.VERSION_1_8.toString()
		freeCompilerArgs.plus("-progressive")
	}
}
defaultTasks("clean", "buildPlugin")
