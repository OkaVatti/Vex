package dev.vex.client.render

import org.lwjgl.opengl.GL11.*
import org.joml.Vector3f

/**
 * Renders a Beta 1.7.3 style player character model
 * Classic Steve model with 8x8x8 head, body, arms, and legs
 */
class PlayerModel {
    private val headSize = 8f / 16f
    private val bodyWidth = 8f / 16f
    private val bodyHeight = 12f / 16f
    private val bodyDepth = 4f / 16f
    private val armWidth = 4f / 16f
    private val armHeight = 12f / 16f
    private val legWidth = 4f / 16f
    private val legHeight = 12f / 16f

    // Animation state
    private var walkAnimation = 0f
    private var armSwing = 0f

    fun render(position: Vector3f, yaw: Float, pitch: Float, isWalking: Boolean, deltaTime: Float) {
        glPushMatrix()

        // Position and rotation
        glTranslatef(position.x, position.y, position.z)
        glRotatef(-yaw, 0f, 1f, 0f)

        // Update animation
        if (isWalking) {
            walkAnimation += deltaTime * 5f
            armSwing += deltaTime * 5f
        } else {
            walkAnimation *= 0.8f
            armSwing *= 0.8f
        }

        // Render body parts
        renderHead(pitch)
        renderBody()
        renderArms(armSwing)
        renderLegs(walkAnimation)

        glPopMatrix()
    }

    private fun renderHead(pitch: Float) {
        glPushMatrix()

        // Position head on top of body
        glTranslatef(0f, 1.5f, 0f)
        glRotatef(-pitch, 1f, 0f, 0f)

        // Head color (Steve's skin tone)
        glColor3f(0.96f, 0.8f, 0.69f)

        renderCube(-headSize / 2, -headSize / 2, -headSize / 2, headSize, headSize, headSize)

        // Hair (darker overlay)
        glColor3f(0.4f, 0.26f, 0.13f)
        renderCube(-headSize / 2 - 0.01f, headSize / 4, -headSize / 2 - 0.01f,
            headSize + 0.02f, headSize / 4, headSize + 0.02f)

        glPopMatrix()
    }

    private fun renderBody() {
        glPushMatrix()

        // Body position (below head)
        glTranslatef(0f, 0.75f, 0f)

        // Shirt color (light blue)
        glColor3f(0.5f, 0.7f, 0.9f)

        renderCube(-bodyWidth / 2, -bodyHeight / 2, -bodyDepth / 2,
            bodyWidth, bodyHeight, bodyDepth)

        glPopMatrix()
    }

    private fun renderArms(swing: Float) {
        // Right arm
        glPushMatrix()
        glTranslatef(bodyWidth / 2 + armWidth / 2, 1.0f, 0f)
        glRotatef(Math.sin(swing.toDouble()).toFloat() * 30f, 1f, 0f, 0f)

        // Skin tone for arms
        glColor3f(0.96f, 0.8f, 0.69f)

        renderCube(-armWidth / 2, -armHeight / 2, -armWidth / 2,
            armWidth, armHeight, armWidth)
        glPopMatrix()

        // Left arm
        glPushMatrix()
        glTranslatef(-bodyWidth / 2 - armWidth / 2, 1.0f, 0f)
        glRotatef(-Math.sin(swing.toDouble()).toFloat() * 30f, 1f, 0f, 0f)

        renderCube(-armWidth / 2, -armHeight / 2, -armWidth / 2,
            armWidth, armHeight, armWidth)
        glPopMatrix()
    }

    private fun renderLegs(walk: Float) {
        // Right leg
        glPushMatrix()
        glTranslatef(bodyWidth / 4, 0.0f, 0f)
        glRotatef(Math.sin(walk.toDouble()).toFloat() * 30f, 1f, 0f, 0f)

        // Pants color (dark blue)
        glColor3f(0.2f, 0.2f, 0.5f)

        renderCube(-legWidth / 2, -legHeight, -legWidth / 2,
            legWidth, legHeight, legWidth)
        glPopMatrix()

        // Left leg
        glPushMatrix()
        glTranslatef(-bodyWidth / 4, 0.0f, 0f)
        glRotatef(-Math.sin(walk.toDouble()).toFloat() * 30f, 1f, 0f, 0f)

        renderCube(-legWidth / 2, -legHeight, -legWidth / 2,
            legWidth, legHeight, legWidth)
        glPopMatrix()
    }

    private fun renderCube(x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float) {
        glBegin(GL_QUADS)

        // Front face
        glNormal3f(0f, 0f, 1f)
        glVertex3f(x, y, z + depth)
        glVertex3f(x + width, y, z + depth)
        glVertex3f(x + width, y + height, z + depth)
        glVertex3f(x, y + height, z + depth)

        // Back face
        glNormal3f(0f, 0f, -1f)
        glVertex3f(x, y, z)
        glVertex3f(x, y + height, z)
        glVertex3f(x + width, y + height, z)
        glVertex3f(x + width, y, z)

        // Top face
        glNormal3f(0f, 1f, 0f)
        glVertex3f(x, y + height, z)
        glVertex3f(x, y + height, z + depth)
        glVertex3f(x + width, y + height, z + depth)
        glVertex3f(x + width, y + height, z)

        // Bottom face
        glNormal3f(0f, -1f, 0f)
        glVertex3f(x, y, z)
        glVertex3f(x + width, y, z)
        glVertex3f(x + width, y, z + depth)
        glVertex3f(x, y, z + depth)

        // Right face
        glNormal3f(1f, 0f, 0f)
        glVertex3f(x + width, y, z)
        glVertex3f(x + width, y + height, z)
        glVertex3f(x + width, y + height, z + depth)
        glVertex3f(x + width, y, z + depth)

        // Left face
        glNormal3f(-1f, 0f, 0f)
        glVertex3f(x, y, z)
        glVertex3f(x, y, z + depth)
        glVertex3f(x, y + height, z + depth)
        glVertex3f(x, y + height, z)

        glEnd()
    }
}