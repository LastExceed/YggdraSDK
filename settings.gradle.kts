rootProject.name = "YggdraSDK"
include(
	"server",
	"client",
	"common"
)

pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven {
			url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
		}
	}

}