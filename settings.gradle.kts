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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Garoon_pre"
include(
    ":app",
    ":sync",
    ":core:common",
    ":core:designsystem",
    ":core:model",
    ":core:session",
    ":core:network",
    ":feature:user:data",
    ":feature:auth:domain",
    ":feature:auth:data",
    ":feature:auth:ui",
    ":feature:board:domain",
    ":feature:board:data",
    ":feature:board:ui",
    ":feature:schedule:domain",
    ":feature:schedule:data",
    ":feature:schedule:ui",
    ":feature:availability:domain",
    ":feature:availability:data",
    ":feature:availability:ui",
    ":feature:home:ui",
)