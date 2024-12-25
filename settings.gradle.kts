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
<<<<<<< HEAD
        jcenter()
=======
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
<<<<<<< HEAD
        maven {
            url = uri("https://github.com/jitsi/jitsi-maven-repository/raw/master/releases")
        }
        maven {
            url = uri("https://maven.google.com")
        }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
=======
>>>>>>> b3c56b5b1afab76afaefd452be7977574aa25928
    }
}

rootProject.name = "Telemedicine"
include(":app")
