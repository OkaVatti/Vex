// dev/vex/client/util/ResourceLoader.kt

package dev.vex.client.util

import org.lwjgl.BufferUtils
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Utility object to load resources from the application's classpath.
 */
object ResourceLoader {

    private const val BUFFER_SIZE = 8 * 1024

    /**
     * Reads a classpath resource into a direct ByteBuffer.
     * @param path The path to the resource, relative to the classpath root (e.g., "assets/textures/font.png").
     * @return A filled, flipped ByteBuffer.
     * @throws RuntimeException if the resource is not found or reading fails.
     */
    fun loadResource(path: String): ByteBuffer {
        // The object {}.javaClass.getResourceAsStream(path) is often more reliable
        // for classpath resources in JVM apps than the classLoader version :cite[2].
        val inputStream: InputStream? = object {}.javaClass.getResourceAsStream(path)

        if (inputStream == null) {
            throw RuntimeException("Resource not found on classpath: '$path'")
        }

        inputStream.use { stream ->
            val bytes = stream.readAllBytes()
            val buffer = BufferUtils.createByteBuffer(bytes.size)
            buffer.put(bytes).flip()
            return buffer
        }
    }

    /**
     * Reads a classpath resource into a direct ByteBuffer - alias for loadResource for compatibility.
     * This method exists to support both naming conventions in your codebase.
     * @param path The path to the resource, relative to the classpath root.
     * @return A filled, flipped ByteBuffer.
     * @throws RuntimeException if the resource is not found or reading fails.
     */
    fun readResourceToByteBuffer(path: String): ByteBuffer {
        return loadResource(path)
    }
}