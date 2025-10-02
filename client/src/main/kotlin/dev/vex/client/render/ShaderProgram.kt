package dev.vex.client.render

import org.lwjgl.opengl.GL20.*
import org.joml.Matrix4f
import org.lwjgl.system.MemoryStack

/**
 * Manages GLSL shaders for Beta 1.7.3 style rendering.
 */
class ShaderProgram {
    private var programId = 0
    private var vertexShaderId = 0
    private var fragmentShaderId = 0

    private val uniforms = mutableMapOf<String, Int>()

    fun create(vertexSource: String, fragmentSource: String) {
        programId = glCreateProgram()
        if (programId == 0) {
            throw RuntimeException("Could not create shader program")
        }

        vertexShaderId = createShader(vertexSource, GL_VERTEX_SHADER)
        fragmentShaderId = createShader(fragmentSource, GL_FRAGMENT_SHADER)

        link()
    }

    private fun createShader(source: String, type: Int): Int {
        val shaderId = glCreateShader(type)
        if (shaderId == 0) {
            throw RuntimeException("Could not create shader of type: $type")
        }

        glShaderSource(shaderId, source)
        glCompileShader(shaderId)

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw RuntimeException("Error compiling shader: ${glGetShaderInfoLog(shaderId, 1024)}")
        }

        glAttachShader(programId, shaderId)
        return shaderId
    }

    private fun link() {
        glLinkProgram(programId)
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw RuntimeException("Error linking shader: ${glGetProgramInfoLog(programId, 1024)}")
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId)
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId)
        }

        glValidateProgram(programId)
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            println("Warning validating shader: ${glGetProgramInfoLog(programId, 1024)}")
        }
    }

    fun bind() {
        glUseProgram(programId)
    }

    fun unbind() {
        glUseProgram(0)
    }

    fun createUniform(name: String) {
        val location = glGetUniformLocation(programId, name)
        if (location < 0) {
            throw RuntimeException("Could not find uniform: $name")
        }
        uniforms[name] = location
    }

    fun setUniform(name: String, value: Matrix4f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(16)
            value.get(buffer)
            glUniformMatrix4fv(uniforms[name]!!, false, buffer)
        }
    }

    fun setUniform(name: String, value: Int) {
        glUniform1i(uniforms[name]!!, value)
    }

    fun setUniform(name: String, value: Float) {
        glUniform1f(uniforms[name]!!, value)
    }

    fun setUniform(name: String, x: Float, y: Float, z: Float) {
        glUniform3f(uniforms[name]!!, x, y, z)
    }

    fun cleanup() {
        unbind()
        if (programId != 0) {
            glDeleteProgram(programId)
        }
    }

    companion object {
        /**
         * Beta 1.7.3 style vertex shader
         */
        val VERTEX_SHADER = """
            #version 330 core
            
            layout (location = 0) in vec3 position;
            layout (location = 1) in vec2 texCoord;
            layout (location = 2) in float ao;
            layout (location = 3) in vec3 normal;
            
            out vec2 fragTexCoord;
            out float fragAO;
            out vec3 fragNormal;
            out float fragFogFactor;
            out vec3 fragWorldPos;
            
            uniform mat4 projectionMatrix;
            uniform mat4 viewMatrix;
            uniform mat4 modelMatrix;
            uniform float fogDensity;
            uniform float fogGradient;
            
            void main() {
                vec4 worldPosition = modelMatrix * vec4(position, 1.0);
                vec4 positionRelativeToCamera = viewMatrix * worldPosition;
                gl_Position = projectionMatrix * positionRelativeToCamera;
                
                fragTexCoord = texCoord;
                fragAO = ao;
                fragNormal = mat3(transpose(inverse(modelMatrix))) * normal;
                fragWorldPos = worldPosition.xyz;
                
                // Beta 1.7.3 style fog calculation
                float distance = length(positionRelativeToCamera.xyz);
                fragFogFactor = exp(-pow((distance * fogDensity), fogGradient));
                fragFogFactor = clamp(fragFogFactor, 0.0, 1.0);
            }
        """.trimIndent()

        /**
         * Beta 1.7.3 style fragment shader
         */
        val FRAGMENT_SHADER = """
            #version 330 core
            
            in vec2 fragTexCoord;
            in float fragAO;
            in vec3 fragNormal;
            in float fragFogFactor;
            in vec3 fragWorldPos;
            
            out vec4 fragColor;
            
            uniform sampler2D textureSampler;
            uniform vec3 skyColor;
            uniform vec3 sunPosition;
            uniform float ambientStrength;
            
            void main() {
                // Sample texture
                vec4 texColor = texture(textureSampler, fragTexCoord);
                
                if (texColor.a < 0.1) {
                    discard;
                }
                
                // Beta 1.7.3 style lighting
                vec3 lightDir = normalize(sunPosition - fragWorldPos);
                float diff = max(dot(fragNormal, lightDir), 0.0);
                
                // Directional lighting with Beta's characteristic stepped appearance
                float lightLevel = ambientStrength + diff * 0.5;
                lightLevel = floor(lightLevel * 15.0) / 15.0; // 16 light levels like Beta
                
                // Apply AO
                lightLevel *= fragAO;
                
                // Minimum light level (simulate torches)
                lightLevel = max(lightLevel, 0.1);
                
                vec3 result = texColor.rgb * lightLevel;
                
                // Apply fog
                result = mix(skyColor, result, fragFogFactor);
                
                fragColor = vec4(result, texColor.a);
            }
        """.trimIndent()
    }
}