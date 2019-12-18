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
import kotlin.math.abs
import kotlin.math.pow


class MainActivity : SimpleBaseGameActivity() {

    private var attackButtonSprite: Sprite? = null
    private var backgroundSprite: SpriteBackground? = null
    private var parallaxLayer: ParallaxLayer? = null
    private var parallaxPosition: Float = 1F
    private var mCharacter: Character? = null
    private var mTextures: Textures? = null
    private var mCamera: Camera? = null

    private var controllerStickSprite: Sprite? = null
    private lateinit var defaultStickPos: ArrayList<Float>

    private var isTimerActive: Boolean = false
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var characterAnimation: AnimatedSprite? = null
    private var characterPositionX: Float = 0F
    private var characterPositionY: Float = 0F

    companion object {
        private var CAMERA_WIDTH: Float = 0.0f
        private var CAMERA_HEIGHT = 0.0f

        private var enemyDefaultPosX = 0F
        private var enemyDefaultPosY = 0F
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
        characterPositionY = CAMERA_HEIGHT - 370F

        enemyDefaultPosX = CAMERA_WIDTH - 93F * 4
        enemyDefaultPosY = CAMERA_HEIGHT - 96F * 4

        // Creating parallax effect for background
        parallaxLayer = ParallaxLayer(mCamera!!, true, CAMERA_WIDTH.toInt())

        scene.attachChild(parallaxLayer)

        val treesSprite = Sprite(
            0F,
            0F,
            CAMERA_WIDTH,
            CAMERA_HEIGHT,
            mTextures!!.mTreesTextureRegion,
            vertexBufferObjectManager
        )

        parallaxLayer!!.attachParallaxEntity(
            ParallaxLayer.ParallaxEntity(
                15F,
                treesSprite,
                false,
                1
            )
        )
        parallaxLayer!!.setParallaxScrollFactor(10F)

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
            // TODO separate this event to another method
            override fun onAreaTouched(
                pSceneTouchEvent: TouchEvent?,
                pTouchAreaLocalX: Float,
                pTouchAreaLocalY: Float
            ): Boolean {
                // Change stick position
                if (pSceneTouchEvent!!.isActionMove) {

                    val xPos = pSceneTouchEvent.x
                    val yPos = pSceneTouchEvent.y


                    // Change idle condition
                    mCharacter!!.characterConditions["idle"]!!["active"] = false
                    mCharacter!!.characterConditions["idle"]!!["state"] = false

                    // To run condition
                    mCharacter!!.characterConditions["run"]!!["active"] = true

                    // TODO depends from direction of the stick change condition

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
                } else {

                    // Change the stick position
                    controllerStickSprite!!.setPosition(
                        defaultStickPos[0],
                        defaultStickPos[1]
                    )

                    // Set idle condition
                    mCharacter!!.characterConditions["idle"]!!["active"] = true
                    mCharacter!!.characterConditions["idle"]!!["state"] = true

                    mCharacter!!.characterConditions["run"]!!["active"] = false
                    mCharacter!!.characterConditions["run"]!!["state"] = false


                    return false
                }

                return true
            }
        }


        defaultStickPos = arrayListOf(
            controllerSprite.width / 2 - mTextures!!.mControllerStick!!.width / 2,
            CAMERA_HEIGHT - controllerSprite.height / 2 - mTextures!!.mControllerStick!!.height / 2
        )

        controllerStickSprite = Sprite(
            defaultStickPos[0],
            defaultStickPos[1],
            mTextures!!.mControllerStick,
            vertexBufferObjectManager
        )

        attackButtonSprite = object : Sprite(
            CAMERA_WIDTH - 200F - controllerStickSprite!!.x,
            controllerStickSprite!!.y - 200F / 4,
            200F, 200F,
            mTextures!!.attackButtonTextureRegion,
            vertexBufferObjectManager
        ) {
            override fun onAreaTouched(
                pSceneTouchEvent: TouchEvent?,
                pTouchAreaLocalX: Float,
                pTouchAreaLocalY: Float
            ): Boolean {
                return if (pSceneTouchEvent!!.isActionDown) {

                    attackButtonSprite!!.red = 0F

                    mCharacter!!.characterConditions = mCharacter!!.zeroizeConditions()
                    mCharacter!!.characterConditions["attack"]!!["active"] = true
                    true
                } else {
                    attackButtonSprite!!.red = 1F

                    mCharacter!!.characterConditions["attack"]!!["active"] = false
                    mCharacter!!.characterConditions["attack"]!!["state"] = false

                    mCharacter!!.characterConditions["idle"]!!["active"] = true
                    mCharacter!!.characterConditions["idle"]!!["state"] = true
                    false
                }
            }
        }

        // Character attack animation

        scene.background = backgroundSprite

        scene.attachChild(controllerStickSprite)
        scene.attachChild(controllerSprite)
        scene.attachChild(attackButtonSprite)

        // Register touch areas
        scene.registerTouchArea(controllerSprite)
        scene.registerTouchArea(attackButtonSprite)

        // Joystick motion

        // Start globalTimer
        startGlobalFrameTimer(scene)
        return scene
    }

    private fun startGlobalFrameTimer(scene: Scene) {
        if (!isTimerActive) {
            // TODO if collusion with Character get and give damage
            // TODO when 0 hp detach enemy by tag
            // TODO if enemies more then 10, remove 1 of them
            //
            val enemiesList = mutableMapOf<Enemies, AnimatedSprite>()
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    runOnUpdateThread {

                        if (mCharacter!!.characterConditions["idle"]!!["active"]!! && mCharacter!!.characterConditions["idle"]!!["state"]!!) {

                            mCharacter!!.characterConditions["idle"]!!["state"] = false

                            scene.detachChild(characterAnimation)

                            characterAnimation = mCharacter!!.setIdleAnimation(
                                characterPositionX,
                                characterPositionY
                            )
                            characterAnimation!!.animate(mCharacter!!.frameDuration)
                            scene.attachChild(characterAnimation)

                        } else if (mCharacter!!.characterConditions["run"]!!["active"]!!) {

                            // If animation of the running had not been run
                            if (!mCharacter!!.characterConditions["run"]!!["state"]!!) {
                                mCharacter!!.characterConditions["run"]!!["state"] = true

                                // Clear previous animation
                                scene.detachChild(characterAnimation)

                                // Set new animation
                                characterAnimation = mCharacter!!.setRunAnimation(
                                    characterPositionX,
                                    characterPositionY

                                )

                                // Start running animation
                                characterAnimation!!.animate(mCharacter!!.frameDuration)
                                scene.attachChild(characterAnimation)

                            }


                            if (scene.x <= 500) {

                                // If middle of the screen hasn't been reached
                                if (characterPositionX < CAMERA_WIDTH / 2 - mTextures!!.mControllerFrame!!.width / 2) {
                                    characterAnimation!!.setPosition(
                                        characterPositionX,
                                        characterPositionY
                                    )

                                    // Increment characters position each tick
                                    characterPositionX += 15
                                } else {
                                    parallaxPosition = parallaxPosition.minus(1F)
                                    parallaxLayer!!.setParallaxValue(parallaxPosition)
                                }

                            }

                            // On cancel set stick to the default position


                        }
                        if (mCharacter!!.characterConditions["attack"]!!["active"]!!) {
                            if (!mCharacter!!.characterConditions["attack"]!!["state"]!!) {
                                scene.detachChild(characterAnimation)
                                characterAnimation = mCharacter!!.setAttackAnimation(
                                    characterPositionX,
                                    characterPositionY
                                )
                                characterAnimation!!.animate(mCharacter!!.frameDuration)
                                scene.attachChild(characterAnimation)
                                mCharacter!!.characterConditions["attack"]!!["state"] = true
                            }
                        }
                    }

                    // Checking collision with enemies
                    enemiesList.forEach {
                        if (characterAnimation!!.collidesWith(it.value)) {

                            // TODO if collides then character can't run and take some damage
                            runOnUpdateThread {
                                scene.detachChild(it.value)
                            }
                            enemiesList.remove(it.key)
                        }
                    }

                    // Spawning enemies
                    if (abs(parallaxPosition.toInt()) % (Random().nextInt(41) + 40) == 0) {
                        val enemy = Enemies(
                            this@MainActivity,
                            engine
                        )

                        if (enemiesList.size > 3) {
                            runOnUpdateThread {
                                scene.detachChild(enemiesList.values.first())
                            }
                            enemiesList.remove(enemiesList.keys.first())
                        }
                        val enemySprite = enemy.spawnEnemy(
                            enemyDefaultPosX
                            , enemyDefaultPosY
                        )

                        // Timer for changing position of the animation
                        val enemyTimer = Timer()
                        val enemyTimerTask = object : TimerTask() {
                            override fun run() {

                                enemySprite.x -= 10
                            }
                        }
                        enemyTimer.scheduleAtFixedRate(enemyTimerTask, 0, 100)

                        enemiesList[enemy] = enemySprite

                        scene.attachChild(enemySprite)
                    }
                }
            }
            timer!!.scheduleAtFixedRate(timerTask, 0, 100)
            isTimerActive = true
        }
    }
}
