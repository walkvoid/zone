rootProject.name = "zone"

// mavenCentral 在前：本地 file 仓库若存在不完整构件（仅有 POM 无 JAR）时仍可解析；仍可使用本地目录中的私服/缓存
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

include(
    ":zone-ai-api",
    ":zone-ai-model",
    ":zone-ai-business",
    ":zone-common-api",
    ":zone-common-model",
    ":zone-common-business",
    ":zone-finance-api",
    ":zone-finance-model",
    ":zone-finance-business",
    ":zone-gateway",
    ":zone-tools-api",
    ":zone-tools-model",
    ":zone-tools-business",
    ":zone-user-api",
    ":zone-user-model",
    ":zone-user-business",
    ":zone-system-api",
    ":zone-system-model",
    ":zone-system-business",
)

mapOf(
    ":zone-ai-api" to "zone-ai/zone-ai-api",
    ":zone-ai-model" to "zone-ai/zone-ai-model",
    ":zone-ai-business" to "zone-ai/zone-ai-business",
    ":zone-common-api" to "zone-common/zone-common-api",
    ":zone-common-model" to "zone-common/zone-common-model",
    ":zone-common-business" to "zone-common/zone-common-business",
    ":zone-finance-api" to "zone-finance/zone-finance-api",
    ":zone-finance-model" to "zone-finance/zone-finance-model",
    ":zone-finance-business" to "zone-finance/zone-finance-business",
    ":zone-gateway" to "zone-gateway",
    ":zone-tools-api" to "zone-tools/zone-tools-api",
    ":zone-tools-model" to "zone-tools/zone-tools-model",
    ":zone-tools-business" to "zone-tools/zone-tools-business",
    ":zone-user-api" to "zone-user/zone-user-api",
    ":zone-user-model" to "zone-user/zone-user-model",
    ":zone-user-business" to "zone-user/zone-user-business",
    ":zone-system-api" to "zone-system/zone-system-api",
    ":zone-system-model" to "zone-system/zone-system-model",
    ":zone-system-business" to "zone-system/zone-system-business",
).forEach { (name, path) ->
    project(name).projectDir = file(path)
}