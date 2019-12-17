package com.example.testgame

import android.graphics.Point
import org.andengine.engine.camera.Camera
import org.andengine.engine.options.EngineOptions
import org.andengine.engine.options.ScreenOrientation
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy
import org.andengine.entity.scene.IOnSceneTouchListener
import org.andengine.entity.scene.Scene
import org.andengine.entity.scene.background.SpriteBackground
import org.andengine.entity.sprite.AnimatedSprite
import org.andengine.entity.sprite.Sprite
import org.andengine.input.touch.TouchEvent
import org.andengine.ui.activity.SimpleBaseGameActivity
import kotlin.math.pow


class MainActivity : SimpleBaseGameActivity(), IOnSceneTouchListener {

    private var mCharacter: Character? = null
    private var mTextures: Textures? = null
    private var mCamera: Camera? = null

    private var controllerStickSprite: Sprite? = null
    private lateinit var defaultStickPos: ArrayList<Float>

    val frameDuration = longArrayOf(125, 125, 125, 125)
    private var characterAnimation: AnimatedSprite? = null

    companion object {
        private var CAMERA_WIDTH: Float = 0.0f
        private var CAMERA_HEIGHT = 0.0f
    }


    override fun onCreateEngineOptions(): EngineOptions {

        val deviceScreenInfo = windowManager.defaultDisplay
        val displaySize = Point()
        deviceScreenInfo.getSize(displaySize)
        CAMERA_WIDTH = displaySize.x.toFloat()
        CAMERA_HEIGHT = displaySize.y.toFloat()

        this.mCamera = Camera(0F, 0F, CAMERA_WIDTH, CAMERA_HEIGHT)
        return EngineOptions(
            true, ScreenOrientation.LANDSCAPE_FIXED,
            RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera
        )
    }

    override fun onCreateResources() {
        mTextures = Textures(this, engine)
        mCharacter = Character(this, engine)
    }

    override fun onCreateScene(): Scene {
        // 1 - Create new scene
        val scene = Scene()


        val backgroundSprite = SpriteBackground(
            Sprite(
                0F,
                0F,
                CAMERA_WIDTH,
                CAMERA_HEIGHT,
                mTextures!!.mBackgroundTextureRegion,
                vertexBufferObjectManager
            )
        )

        val controllerSprite =
            Sprite(
                0F,
                CAMERA_HEIGHT - mTextures!!.mControllerFrame!!.height,
                mTextures!!.mControllerFrame,
                vertexBufferObjectManager
            )

        defaultStickPos = arrayListOf(
            controllerSprite.width / 2 - mTextures!!.mControllerStick!!.width / 2,
            CAMERA_HEIGHT - controllerSprite.height / 2 - mTextures!!.mControllerStick!!.height / 2
        )

        controllerStickSprite =
            Sprite(
                defaultStickPos[0],
                defaultStickPos[1],
                mTextures!!.mControllerStick,
                vertexBufferObjectManager
            )


        // Set default animation
        characterAnimation = mCharacter!!.setIdleAnimation(
            300F,
            CAMERA_HEIGHT - 270
        )
        characterAnimation!!.animate(frameDuration)

        scene.background = backgroundSprite
        scene.attachChild(controllerStickSprite)
        scene.attachChild(controllerSprite)

        scene.attachChild(characterAnimation)

        // Joystick motion
        scene.onSceneTouchListener = this
        return scene
    }

    override fun onSceneTouchEvent(pScene: Scene?, pSceneTouchEvent: TouchEvent?): Boolean {
        if (pSceneTouchEvent!!.isActionMove) {
            val xPos = pSceneTouchEvent.x
            val yPos = pSceneTouchEvent.y
            // Stick position checking
            val displacement =
                (mTextures!!.mControllerFrame!!.width / 2 - xPos - mTextures!!.mControllerStick!!.width / 2).toDouble().pow(
                    2.0
                ) +
                        (CAMERA_HEIGHT - mTextures!!.mControllerFrame!!.height / 2 - yPos).toDouble().pow(
                            2.0
                        )
            val baseRadius =
                (mTextures!!.mControllerFrame!!.width / 2 + mTextures!!.mControllerStick!!.width / 2).toDouble()
                    .pow(2.0)

            if (!mCharacter!!.isAnimationChanged && mCharacter!!.isActionGoing) {
                mCharacter!!.isAnimationChanged = true
                // Clear previous animation
                pScene!!.detachChild(characterAnimation)

                // Set new animation
                characterAnimation = mCharacter!!.setRunAnimation(
                    300F,
                    CAMERA_HEIGHT - 270
                )

                characterAnimation!!.animate(frameDuration)
                pScene.attachChild(characterAnimation)
            }

            // If it's inside the joystick frame
            // set touch position
            if (displacement <= baseRadius) {
                controllerStickSprite!!.setPosition(xPos, yPos)
            }
            // If outside the frame, set position near the frame to the same direction
            else {
                val ratio = baseRadius / displacement
                val constrainedX = defaultStickPos[0] + (xPos - defaultStickPos[0]) * ratio
                val constrainedY = defaultStickPos[1] + (yPos - defaultStickPos[1]) * ratio

                controllerStickSprite!!.setPosition(
                    (constrainedX).toFloat(),
                    (constrainedY).toFloat()
                )
            }

            mCharacter!!.isActionGoing = true
            return true
        }
        // On cancel set stick to the default position
        else {
            mCharacter!!.isActionGoing = false
            mCharacter!!.isAnimationChanged = false

            // Clean run animation
            pScene!!.detachChild(characterAnimation)


            // Set default idle animation
            characterAnimation = mCharacter!!.setIdleAnimation(
                300F,
                CAMERA_HEIGHT - 270
            )
            characterAnimation!!.animate(frameDuration)
            // Set idle animation
            pScene.attachChild(characterAnimation)

            // Change the stick position
            controllerStickSprite!!.setPosition(defaultStickPos[0], defaultStickPos[1])

            return false
        }
    }

}
