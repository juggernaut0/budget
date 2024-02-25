plugins {
    id("dev.twarner.kotlin-service")
}

dependencies {
    implementation(projects.api)
    implementation(projects.dbmigrate)
    webResource(projects.web)

    implementation(libs.config4k)
    implementation(libs.hikari)
    implementation(libs.javalin)
    implementation(libs.logback)
    implementation(libs.multiplatformUtils.javalin)
    implementation(libs.twarner.auth.plugins.javalin)
}

application {
    mainClass = "budget.MainKt"
}

tasks {
    (run) {
        environment("APP_PORT", "8080")
        environment("DB_URL", "jdbc:postgresql://localhost:5432/budget")
        environment("DB_USER", "budget")
        environment("DB_PASSWORD", "budget")
        environment("AUTH_MOCK", "true")
        environment("AUTH_URL", "http://localhost:8080")
    }
}
