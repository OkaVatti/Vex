plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("dev.vex.server.ServerMainKt")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Ktor server core (pick version compatible with Kotlin 2.2 — 2.x Ktor typically works)
    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("io.ktor:ktor-server-websockets:2.3.5")
    implementation("ch.qos.logback:logback-classic:1.4.6")

    implementation(project(":common"))
}

kotlin {
    jvmToolchain(21)
}
