rootProject.name = "DreamAIChat_backend"

pluginManagement {
    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/gradle-plugin/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven-public/") }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven-public/") }
        mavenCentral()
    }
}

