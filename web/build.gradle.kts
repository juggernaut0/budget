plugins {
    id("dev.twarner.kotlin-web")
}

dependencies {
    jsMainImplementation(projects.api)
    jsMainImplementation(libs.kui)
    jsMainImplementation(libs.asyncLite)
    jsMainImplementation(libs.twarner.auth.ui)

    jsTestImplementation(kotlin("test"))
}
