plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("dev.vex.client.ClientMainKt")
}

dependencies {
    // Use LWJGL BOM to align versions
    implementation(platform("org.lwjgl:lwjgl-bom:3.3.6"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")   // optional for GL demo; later switch to Vulkan modules

    // natives - here example for Windows. Replace classifier with 'natives-linux' or 'natives-macos' as needed.
    // The empty version uses the BOM version (3.3.6)
    runtimeOnly("org.lwjgl:lwjgl::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-windows")

    implementation(project(":common"))
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

kotlin {
    jvmToolchain(21)
}
