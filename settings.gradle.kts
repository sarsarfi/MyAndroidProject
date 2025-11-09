pluginManagement {
    repositories {
        // 👈 بلوک content حذف شد تا دسترسی کامل به پلاگین KSP برقرار شود
        google()

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

rootProject.name = "MyDictionary"
include(":app")
 