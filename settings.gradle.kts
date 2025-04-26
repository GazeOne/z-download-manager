pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "z-download-manager"
include(":app")
include(":nativelib")
include(":flutter_module")
project(":flutter_module").projectDir = file("../flutter_module")
apply(from = "./flutter_module/.android/include_flutter.groovy")

