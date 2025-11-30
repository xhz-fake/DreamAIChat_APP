pluginManagement {
    repositories {
        // Tencent mirrors covering Google / MavenCentral / Gradle Plugin Portal
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/gradle-plugin/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven-public/") }

        // Keep official repositories as fallback
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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven-public/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/gradle-plugin/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "My Application"
include(":app")

//pluginManagement {
//    repositories {
//        google {
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
//        mavenCentral()
//        gradlePluginPortal()
//    }
//}
//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        mavenCentral()
//    }
//}
//
//rootProject.name = "DreamAIChat_APP"
//include(":app")
