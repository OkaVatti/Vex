package dev.vex.client.render

import org.joml.Vector3f
import org.joml.Matrix4f
import kotlin.math.*

/**
 * First-person camera with Beta 1.7.3 movement physics.
 */
class Camera(
    var position: Vector3f = Vector3f(0f, 75f, 0f),
    var pitch: Float = 0f,
    var yaw: Float = -90f
) {
    private val front = Vector3f(0f, 0f, -1f)
    private val up = Vector3f(0f, 1f, 0f)
    private val right = Vector3f()
    private val worldUp = Vector3f(0f, 1f, 0f)

    var velocity = Vector3f(0f, 0f, 0f)
    private var isOnGround = false
    private var isSprinting = false
    private var isSneaking = false

    // Beta 1.7.3 movement constants
    private val walkSpeed = 4.317f
    private val sprintSpeed = 5.612f
    private val sneakSpeed = 1.31f
    private val jumpVelocity = 0.42f
    private val gravity = -0.08f
    private val airResistance = 0.91f
    private val groundFriction = 0.6f

    var fov = 70f
    var sensitivity = 0.15f
    private val maxPitch = 89.5f

    // Head bobbing
    private var bobPhase = 0f
    private val bobSpeed = 0.15f

    init {
        updateCameraVectors()
    }

    fun getViewMatrix(): Matrix4f {
        val bobOffset = if (isOnGround && velocity.lengthSquared() > 0.01f) {
            val bob = sin(bobPhase) * 0.05f
            Vector3f(0f, bob, 0f)
        } else {
            Vector3f()
        }

        val bobPosition = Vector3f(position).add(bobOffset)
        val center = Vector3f(bobPosition).add(front)
        return Matrix4f().lookAt(bobPosition, center, up)
    }

    fun getProjectionMatrix(aspectRatio: Float): Matrix4f {
        return Matrix4f().perspective(
            Math.toRadians(fov.toDouble()).toFloat(),
            aspectRatio,
            0.1f,
            1000f
        )
    }

    fun processMovement(
        forward: Boolean, backward: Boolean,
        left: Boolean, right: Boolean,
        jump: Boolean, sprint: Boolean,
        sneak: Boolean, deltaTime: Float
    ) {
        isSprinting = sprint && forward && !isSneaking
        isSneaking = sneak

        val speed = when {
            isSprinting -> sprintSpeed
            isSneaking -> sneakSpeed
            else -> walkSpeed
        } * deltaTime

        val inputDir = Vector3f()
        // project front to horizontal plane
        val horizontalFront = Vector3f(front.x, 0f, front.z).normalize()
        val horizontalRight = Vector3f(this.right.x, 0f, this.right.z).normalize()

        if (forward) inputDir.add(Vector3f(horizontalFront))
        if (backward) inputDir.sub(Vector3f(horizontalFront))
        if (left) inputDir.sub(Vector3f(horizontalRight))
        if (right) inputDir.add(Vector3f(horizontalRight))

        if (inputDir.lengthSquared() > 0f) {
            inputDir.normalize()
            val accel = Vector3f(inputDir).mul(speed * 10f)
            velocity.x += accel.x * deltaTime
            velocity.z += accel.z * deltaTime
        }

        val friction = if (isOnGround) groundFriction else airResistance
        velocity.x *= friction
        velocity.z *= friction

        if (jump && isOnGround) {
            velocity.y = jumpVelocity
            isOnGround = false
        }

        velocity.y += gravity * deltaTime
        position.add(Vector3f(velocity).mul(deltaTime * 20f))

        // Simple ground check
        if (position.y <= 64f) {
            position.y = 64f
            velocity.y = 0f
            isOnGround = true
        } else {
            isOnGround = false
        }

        // Head bobbing
        if (isOnGround && velocity.lengthSquared() > 0.01f) {
            val horizontalSpeed = sqrt(velocity.x * velocity.x + velocity.z * velocity.z)
            bobPhase += horizontalSpeed * bobSpeed
        }
    }

    fun processMouseMovement(xoffset: Float, yoffset: Float) {
        yaw += xoffset * sensitivity
        pitch += yoffset * sensitivity

        pitch = pitch.coerceIn(-maxPitch, maxPitch)

        updateCameraVectors()
    }

    private fun updateCameraVectors() {
        val pitchRad = Math.toRadians(pitch.toDouble())
        val yawRad = Math.toRadians(yaw.toDouble())

        front.x = cos(yawRad).toFloat() * cos(pitchRad).toFloat()
        front.y = sin(pitchRad).toFloat()
        front.z = sin(yawRad).toFloat() * cos(pitchRad).toFloat()
        front.normalize()

        Vector3f(front).cross(worldUp, right).normalize()
        Vector3f(right).cross(front, up).normalize()
    }
}
