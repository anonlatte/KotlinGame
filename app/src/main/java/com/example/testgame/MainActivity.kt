package com.example.testgame

import android.graphics.Color
import android.graphics.Point
import org.andengine.engine.camera.Camera
import org.andengine.engine.options.EngineOptions
import org.andengine.engine.options.ScreenOrientation
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy
import org.andengine.entity.scene.Scene
import org.andengine.entity.scene.background.SpriteBackground
import org.andengine.entity.sprite.AnimatedSprite
import org.andengine.entity.sprite.Sprite
import org.andengine.entity.text.Text
import org.andengine.input.touch.TouchEvent
import org.andengine.opengl.font.Font
import org.andengine.opengl.font.FontFactory
import org.andengine.opengl.texture.TextureOptions
import org.andengine.ui.activity.SimpleBaseGameActivity
import java.util.*
import kotlin.math.abs
import kotlin.math.pow


class MainActivity : SimpleBaseGameActivity() {

    private var aspectRatio: Float = 0F

    private var mCharacter: Character? = null
    private var mFont: Font? = null
    private var mTextures: Textures? = null
    private var mItems: Items? = null
    private var mCamera: Camera? = null

    private var enemyTimer: Timer? = null
    private var enemyTimerTask: TimerTask? = null
    private var attackButtonSprite: Sprite? = null
    private var backgroundSprite: SpriteBackground? = null
    private var parallaxLayer: ParallaxLayer? = null
    private var parallaxPosition: Float = 1F

    private var controllerStickSprite: Sprite? = null
    private lateinit var defaultStickPos: ArrayList<Float>
    private var healthBarSprite: Sprite? = null
    private var healthBarSpriteFilling: Sprite? = null

    private var isTimerActive: Boolean = false
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var characterAnimation: AnimatedSprite? = null
    private var characterPositionX: Float = 0F
    private var characterPositionY: Float = 0F
    private var coinsCounter: Text? = null

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
        aspectRatio = CAMERA_WIDTH / CAMERA_HEIGHT

        this.mCamera = Camera(0F, 0F, CAMERA_WIDTH, CAMERA_HEIGHT)
        return EngineOptions(
            true, ScreenOrientation.LANDSCAPE_FIXED,
            RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera
        )
    }

    override fun onCreateResources() {
        mTextures = Textures(this, engine)
        mCharacter = Character(this, engine)
        mCharacter!!.characterWidth = CAMERA_WIDTH / 50 * 6
        mCharacter!!.characterHeight = CAMERA_HEIGHT / 37 * 6
        characterPositionX = CAMERA_HEIGHT * 0.3F
        characterPositionY = CAMERA_HEIGHT - mCharacter!!.characterHeight * 1.625F

        mFont = FontFactory.createFromAsset(
            this.fontManager,
            this.textureManager,
            256,
            128,
            TextureOptions.BILINEAR,
            this.assets,
            "fonts/ARCADECLASSIC.TTF",
            aspectRatio * 32F,
            true,
            Color.YELLOW
        )
        mFont!!.load()
        coinsCounter = Text(
            CAMERA_WIDTH - mFont!!.texture.width / 8 - mFont!!.texture.width / 2,
            128F,
            mFont,
            "0",
            10,
            vertexBufferObjectManager
        )
    }

    override fun onCreateScene(): Scene {
        // 1 - Create new scene
        val scene = Scene()

        // Initialize start position

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
            CAMERA_HEIGHT - CAMERA_HEIGHT * 0.3F,
            CAMERA_HEIGHT * 0.3F,
            CAMERA_HEIGHT * 0.3F,
            mTextures!!.controllerFrameTextureRegion,
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
                        (mTextures!!.controllerFrameTextureRegion!!.width / 2 - xPos - mTextures!!.controllerStickTextureRegion!!.width / 2).toDouble().pow(
                            2.0
                        ) +
                                (CAMERA_HEIGHT - mTextures!!.controllerFrameTextureRegion!!.height / 2 - yPos).toDouble().pow(
                                    2.0
                                )
                    val baseRadius =
                        (mTextures!!.controllerFrameTextureRegion!!.width / 2 + mTextures!!.controllerStickTextureRegion!!.width / 2).toDouble()
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

                    // On cancel set stick to the default position
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



        controllerStickSprite = Sprite(
            controllerSprite.width / 4,
            CAMERA_HEIGHT - controllerSprite.height / 2 - controllerSprite.width * 0.5F / 2,
            controllerSprite.width * 0.5F,
            controllerSprite.height * 0.5F,
            mTextures!!.controllerStickTextureRegion,
            vertexBufferObjectManager
        )

        defaultStickPos = arrayListOf(
            controllerStickSprite!!.x
            , controllerStickSprite!!.y
        )

        attackButtonSprite = object : Sprite(
            CAMERA_WIDTH - CAMERA_HEIGHT / 16 * 4,
            CAMERA_HEIGHT - CAMERA_HEIGHT / 16 * 4,
            CAMERA_HEIGHT / 16 * 3,
            CAMERA_HEIGHT / 16 * 3,
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

        // Drawing health bar

        healthBarSprite = Sprite(
            0F,
            0F,
            CAMERA_WIDTH / 4,
            CAMERA_HEIGHT / 9,
            mTextures!!.healthBarTextureRegion,
            vertexBufferObjectManager
        )

        // Drawing rectangle inside the health bar
        healthBarSpriteFilling = Sprite(
            healthBarSprite!!.width / 4,
            healthBarSprite!!.height / 2.5F,
            CAMERA_WIDTH / 5.5F,
            CAMERA_HEIGHT / 35,
            mTextures!!.healthBarFillingTextureRegion,
            vertexBufferObjectManager
        )

        scene.attachChild(healthBarSpriteFilling)
        scene.attachChild(healthBarSprite)

        // Set text
        scene.attachChild(coinsCounter)

        // Start globalTimer
        startGlobalFrameTimer(scene)
        return scene
    }

    private fun startGlobalFrameTimer(scene: Scene) {
        if (!isTimerActive) {
            val enemiesList = mutableMapOf<Enemies, AnimatedSprite>()
            val itemsList = mutableMapOf<Items, Sprite>()
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {

                    runOnUpdateThread {

                        if (mCharacter!!.healthPoints <= 0) {
                            // Identify death state

                            if (mCharacter!!.characterConditions["die"]!!["state"] == false) {
                                mCharacter!!.characterConditions["die"]!!["state"] = true
                                // Clear previous animation
                                characterAnimation!!.stopAnimation()

                                // If animation of the death is not running
                                if (!characterAnimation!!.isAnimationRunning) {

                                    scene.detachChild(characterAnimation)

                                    // Set new animation
                                    characterAnimation = mCharacter!!.setDieAnimation(
                                        characterPositionX,
                                        characterPositionY
                                    )
                                    characterAnimation!!.animate(mCharacter!!.dieFrameDuration)
                                    scene.attachChild(characterAnimation)

                                }
                            }

                            if (characterAnimation!!.currentTileIndex >= characterAnimation!!.tileCount - 1) {

                                // TODO Show death screen
                                isTimerActive = false
                                scene.detachChild(characterAnimation)

                                with(enemiesList.iterator()) {
                                    forEach {
                                        scene.detachChild(it.value)
                                        remove()
                                    }
                                }

                                this.cancel()
                                enemyTimerTask!!.cancel()
                            }

                            // Start running animation
                        } else {
                            if (mCharacter!!.characterConditions["idle"]!!["active"]!! && mCharacter!!.characterConditions["idle"]!!["state"]!!) {

                                mCharacter!!.characterConditions["idle"]!!["state"] = false

                                scene.detachChild(characterAnimation)

                                characterAnimation = mCharacter!!.setIdleAnimation(
                                    characterPositionX,
                                    characterPositionY
                                )
                                characterAnimation!!.animate(mCharacter!!.idleFrameDuration)
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
                                    characterAnimation!!.animate(mCharacter!!.runFrameDuration)
                                    scene.attachChild(characterAnimation)
                                }

                                if (scene.x <= 500) {

                                    // If middle of the screen hasn't been reached
                                    if (characterPositionX < CAMERA_WIDTH / 2 - mTextures!!.controllerFrameTextureRegion!!.width / 2) {
                                        characterAnimation!!.setPosition(
                                            characterPositionX,
                                            characterPositionY
                                        )
                                        // TODO increment position by screen resolution
                                        // Increment characters position each tick
                                        characterPositionX += 15

                                    } else {
                                        parallaxPosition = parallaxPosition.minus(1F)
                                        parallaxLayer!!.setParallaxValue(parallaxPosition)

                                        if (abs(parallaxPosition.toInt()) % (Random().nextInt(41) + 40) == 0) {
                                            val enemy = Enemies(
                                                this@MainActivity,
                                                engine
                                            )

                                            // Remove enemies for saving a phone performance
                                            if (enemiesList.size > 3) {
                                                runOnUpdateThread {
                                                    scene.detachChild(enemiesList.values.first())
                                                }
                                                enemiesList.remove(enemiesList.keys.first())
                                            }
                                            val enemySprite =
                                                enemy.spawnEnemy(CAMERA_WIDTH, CAMERA_HEIGHT)

                                            // Timer for changing position of the animation
                                            enemyTimer = Timer()
                                            enemyTimerTask = object : TimerTask() {
                                                override fun run() {
                                                    // TODO decrement position by screen resolution
                                                    enemySprite.x -= 10
                                                }
                                            }
                                            enemyTimer!!.scheduleAtFixedRate(enemyTimerTask, 0, 100)

                                            enemiesList[enemy] = enemySprite

                                            scene.attachChild(enemySprite)
                                        }

                                    }

                                }
                            }
                        }
                        if (mCharacter!!.characterConditions["attack"]!!["active"]!!) {
                            if (!mCharacter!!.characterConditions["attack"]!!["state"]!!) {
                                scene.detachChild(characterAnimation)
                                characterAnimation = mCharacter!!.setAttackAnimation(
                                    characterPositionX,
                                    characterPositionY
                                )
                                characterAnimation!!.animate(mCharacter!!.attackFrameDuration)
                                scene.attachChild(characterAnimation)
                                mCharacter!!.characterConditions["attack"]!!["state"] = true
                            }
                        }
                    }

                    // Checking collision with enemies
                    with(enemiesList.iterator()) {
                        forEach {
                            if (characterAnimation!!.collidesWith(it.value)) {
                                if (mCharacter!!.characterConditions["attack"]!!["state"] == true) {
                                    if (it.key.healthPoints <= 0) {
                                        // Make heal after kill
                                        mCharacter!!.healthPoints += 20F
                                        // barHP = (HP / maxHP) * barMaxHP.Width
                                        healthBarSpriteFilling!!.width =
                                            (mCharacter!!.healthPoints / 100) * (CAMERA_WIDTH / 5.5F)

                                        runOnUpdateThread {
                                            scene.detachChild(it.value)
                                        }

                                        // Drop a coin
                                        // TODO set coin size by resolution
                                        val mItems = Items(this@MainActivity, engine)
                                        val coinSprite = mItems.dropCoin(
                                            CAMERA_WIDTH - 128F - itemsList.size * 32,
                                            10F
                                        )
                                        itemsList[mItems] = coinSprite
                                        coinsCounter!!.text = itemsList.size.toString()
                                        if (itemsList.size < 10) {
                                            scene.attachChild(coinSprite)
                                        }

                                        remove()
                                    } else {
                                        it.value.green = 0.5F
                                        it.value.blue = 0.5F
                                        it.key.healthPoints -= 5
                                    }
                                }
                                mCharacter!!.healthPoints -= 1F
                                // Every tick event set width of barHP = (HP / maxHP) * barMaxHP.Width
                                healthBarSpriteFilling!!.width =
                                    (mCharacter!!.healthPoints / 100) * (CAMERA_WIDTH / 5.5F)
                            }

                        }
                    }
                }
            }
            timer!!.scheduleAtFixedRate(timerTask, 0, 100)
            isTimerActive = true
        }
    }
}
