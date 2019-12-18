package com.example.testgame

import android.graphics.Point
import org.andengine.engine.camera.Camera
import org.andengine.engine.options.EngineOptions
import org.andengine.engine.options.ScreenOrientation
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy
import org.andengine.entity.scene.Scene
import org.andengine.entity.scene.background.SpriteBackground
import org.andengine.entity.sprite.AnimatedSprite
import org.andengine.entity.sprite.Sprite
import org.andengine.input.touch.TouchEvent
import org.andengine.ui.activity.SimpleBaseGameActivity
import java.util.*
import kotlin.math.pow


class MainActivity : SimpleBaseGameActivity() {

    private var attackButtonSprite: Sprite? = null
    private var backgroundSprite: SpriteBackground? = null
    private var mCharacter: Character? = null
    private var mTextures: Textures? = null
    private var mCamera: Camera? = null

    private var controllerStickSprite: Sprite? = null
    private lateinit var defaultStickPos: ArrayList<Float>

    val frameDuration = longArrayOf(125, 125, 125, 125)
    private var isTimerActive: Boolean = false
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var characterAnimation: AnimatedSprite? = null
    private var characterPositionX: Float = 0F
    private var characterPositionY: Float = 0F

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

        // Initialize start position
        characterPositionX = 300F
        characterPositionY = CAMERA_HEIGHT - 270

        backgroundSprite = SpriteBackground(
            Sprite(
                0F,
                0F,
                CAMERA_WIDTH,
                CAMERA_HEIGHT,
                mTextures!!.mBackgroundTextureRegion,
                vertexBufferObjectManager
            )
        )

        val controllerSprite = object : Sprite(
            0F,
            CAMERA_HEIGHT - mTextures!!.mControllerFrame!!.height,
            mTextures!!.mControllerFrame,
            vertexBufferObjectManager
        ) {

            override fun onAreaTouched(
                pSceneTouchEvent: TouchEvent?,
                pTouchAreaLocalX: Float,
                pTouchAreaLocalY: Float
            ): Boolean {
                if (pSceneTouchEvent!!.isActionMove) {
                    if (pSceneTouchEvent.x <= 500) {

                        mCharacter!!.isActionGoing = true

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

                        // TODO change every second position of the character if action is going and if action is running
                        if (!mCharacter!!.isAnimationChanged && mCharacter!!.isActionGoing) {
                            mCharacter!!.isAnimationChanged = true

                            // Clear previous animation
                            scene.detachChild(characterAnimation)

                            // Set new animation
                            characterAnimation = mCharacter!!.setRunAnimation(
                                characterPositionX,
                                characterPositionY

                            )

                            characterAnimation!!.animate(frameDuration)
                            scene.attachChild(characterAnimation)
                            // Start running animation
                            startTimer()
                        }

                        // If it's inside the joystick frame
                        // set touch position
                        if (displacement <= baseRadius) {
                            controllerStickSprite!!.setPosition(xPos, yPos)
                        }
                        // If outside the frame, set position near the frame to the same direction
                        else {
                            val ratio = baseRadius / displacement
                            val constrainedX =
                                defaultStickPos[0] + (xPos - defaultStickPos[0]) * ratio
                            val constrainedY =
                                defaultStickPos[1] + (yPos - defaultStickPos[1]) * ratio

                            controllerStickSprite!!.setPosition(
                                (constrainedX).toFloat(),
                                (constrainedY).toFloat()
                            )
                        }
                        return true
                    } else {
                        return false
                    }
                }
                // On cancel set stick to the default position
                else {
                    mCharacter!!.isActionGoing = false
                    mCharacter!!.isAnimationChanged = false

                    // Change the stick position
                    controllerStickSprite!!.setPosition(defaultStickPos[0], defaultStickPos[1])

                    // Stop animation
                    stopTimer(scene)
                    return false
                }
            }
        }

        scene.registerTouchArea(controllerSprite)

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

        attackButtonSprite = Sprite(
            CAMERA_WIDTH - 200F - controllerStickSprite!!.x,
            controllerStickSprite!!.y - 200F / 4,
            200F, 200F,
            mCharacter!!.attackButtonTextureRegion,
            vertexBufferObjectManager
        )

        // Set default animation
        characterAnimation = mCharacter!!.setIdleAnimation(
            characterPositionX,
            characterPositionY
        )
        characterAnimation!!.animate(frameDuration)

        scene.background = backgroundSprite

//        scene.background = backgroundSprite
        scene.attachChild(controllerStickSprite)
        scene.attachChild(controllerSprite)
        scene.attachChild(attackButtonSprite)

        scene.attachChild(characterAnimation)

        // Joystick motion
        return scene
    }

    private fun startTimer() {
        if (!isTimerActive) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {

                    // If middle of the screen hasn't been reached
                    if (characterPositionX < CAMERA_WIDTH / 2 - mTextures!!.mControllerFrame!!.width / 2) {
                        characterAnimation!!.setPosition(characterPositionX, characterPositionY)

                        // Increment characters position each tick
                        characterPositionX += 15
                    }
                }

            }
            timer!!.scheduleAtFixedRate(timerTask, 0, 100)
            isTimerActive = true
        }
    }

    private fun stopTimer(pScene: Scene?) {
        if (isTimerActive) {
            timer!!.cancel()
            isTimerActive = false

            // Clean run animation
            pScene!!.detachChild(characterAnimation)


            // Set default idle animation
            characterAnimation = mCharacter!!.setIdleAnimation(
                characterPositionX,
                characterPositionY
            )
            characterAnimation!!.animate(frameDuration)
            // Set idle animation
            pScene.attachChild(characterAnimation)

        }
    }

}
