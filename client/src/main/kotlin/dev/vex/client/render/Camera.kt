package dev.vex.client.render

import org.joml.Vector3f
import org.joml.Matrix4f

class Camera(
    var position: Vector3f = Vector3f(0f, 2f, 5f),
    var pitch: Float = 0f,
    var yaw: Float = -90f
) {
    private val front = Vector3f(0f, 0f, -1f)
    private val up = Vector3f(0f, 1f, 0f)
    private val right = Vector3f()
    private val worldUp = Vector3f(0f, 1f, 0f)

    var speed = 5f
    var sensitivity = 0.1f

    fun getViewMatrix(): Matrix4f {
        val center = Vector3f(position).add(front)
        return Matrix4f().lookAt(position, center, up)
    }

    fun processKeyboard(direction: String, deltaTime: Float) {
        val velocity = speed * deltaTime
        when (direction) {
            "FORWARD" -> position.add(Vector3f(front).mul(velocity))
            "BACKWARD" -> position.sub(Vector3f(front).mul(velocity))
            "LEFT" -> position.sub(Vector3f(right).mul(velocity))
            "RIGHT" -> position.add(Vector3f(right).mul(velocity))
        }
    }

    fun processMouseMovement(xoffset: Float, yoffset: Float) {
        yaw += xoffset * sensitivity
        pitch += yoffset * sensitivity

        if (pitch > 89f) pitch = 89f
        if (pitch < -89f) pitch = -89f

        updateCameraVectors()
    }

    private fun updateCameraVectors() {
        front.x = Math.cos(Math.toRadians(yaw.toDouble())).toFl
