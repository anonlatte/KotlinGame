package com.example.testgame

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import androidx.appcompat.app.AlertDialog
import org.andengine.engine.Engine
import org.andengine.engine.LimitedFPSEngine
import org.andengine.engine.camera.SmoothCamera
import org.andengine.engine.camera.hud.HUD
import org.andengine.engine.options.EngineOptions
import org.andengine.engine.options.ScreenOrientation
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy
import org.andengine.entity.scene.Scene
import org.andengine.entity.scene.background.ParallaxBackground
import org.andengine.entity.sprite.AnimatedSprite
import org.andengine.entity.sprite.Sprite
import org.andengine.entity.text.Text
import org.andengine.entity.util.FPSLogger
import org.andengine.input.touch.TouchEvent
import org.andengine.opengl.font.Font
import org.andengine.opengl.font.FontFactory
import org.andengine.opengl.texture.TextureOptions
import org.andengine.ui.activity.SimpleBaseGameActivity
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.system.exitProcess


class MainActivity : SimpleBaseGameActivity() {

    private var mCharacter: Character? = null
    private var mFont: Font? = null
    private var mTextures: Textures? = null
    private var mCamera: SmoothCamera? = null

    private var isTimerActive: Boolean = false
    private var characterTimer: Timer? = null
    private var characterTimerTask: TimerTask? = null

    private var fpsText: Text? = null
    private var hudLayer: HUD? = null
    private var controllerSprite: Sprite? = null
    private var controllerStickSprite: Sprite? = null
    private lateinit var defaultStickPos: ArrayList<Float>
    private var healthBarSprite: Sprite? = null
    private var healthBarSpriteFilling: Sprite? = null
    private var attackButtonSprite: Sprite? = null
    private var coinsCounter: Text? = null

    private var aspectRatio: Float = 0F

    private var backgroundSprite: Sprite? = null
    private var parallaxBackground: ParallaxBackground? = null
    private var parallaxPosition: Float = -1F

    private var characterAnimation: AnimatedSprite? = null
    private var characterPositionX: Float = 0F
    private var characterPositionY: Float = 0F

    private var enemyTimer: Timer? = null
    private var enemyTimerTask: TimerTask? = null

    companion object {
        private var CAMERA_WIDTH: Float = 0.0f
        private var CAMERA_HEIGHT = 0.0f

    }

    override fun onCreateEngine(pEngineOptions: EngineOptions?): Engine {
        return LimitedFPSEngine(pEngineOptions, 60)
    }

    override fun onCreateEngineOptions(): EngineOptions {

        val deviceScreenInfo = windowManager.defaultDisplay
        val displaySize = Point()
        deviceScreenInfo.getSize(displaySize)
        CAMERA_WIDTH = displaySize.x.toFloat()
        CAMERA_HEIGHT = displaySize.y.toFloat()
        aspectRatio = CAMERA_WIDTH / CAMERA_HEIGHT

        this.mCamera = SmoothCamera(0F, 0F, CAMERA_WIDTH, CAMERA_HEIGHT, CAMERA_WIDTH, 150F, 1.3F)
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
        mCamera!!.setCenter(characterPositionX, mCamera!!.targetCenterY)
        mFont = FontFactory.createFromAsset(
            this.fontManager,
            this.textureManager,
            (CAMERA_WIDTH * 0.7).toInt(),
            (CAMERA_HEIGHT * 0.9).toInt(),
            TextureOptions.BILINEAR,
            this.assets,
            "fonts/ARCADECLASSIC.TTF",
            CAMERA_WIDTH * 0.05F,
            true,
            Color.YELLOW
        )

        mFont!!.load()

        hudLayer = HUD()
        fpsText = Text(
            0F,
            0F,
            this.mFont,
            "Fps: ?",
            "Fps: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX".length,
            vertexBufferObjectManager
        )


        //Attach the FPSLogger
        engine.registerUpdateHandler(object : FPSLogger(0.5f) {
            override fun onLogFPS() {
                val fpsString = String.format(
                    "FPS: %.2f",
                    this.mFrames / this.mSecondsElapsed
                )
                fpsText!!.text = fpsString
            }
        })

        coinsCounter = Text(
            CAMERA_WIDTH - CAMERA_WIDTH * 0.15F,
            CAMERA_HEIGHT * 0.1F,
            mFont,
            "0",
            "xxxxxxxxxx".length,
            vertexBufferObjectManager
        )
    }

    override fun onCreateScene(): Scene {
        // 1 - Create new scene
        val scene = Scene()

        // Creating parallax effect for background
        parallaxBackground = ParallaxBackground(0F, 0F, 0F)

        val treesSprite = Sprite(
            0F,
            0F,
            CAMERA_WIDTH,
            CAMERA_HEIGHT,
            mTextures!!.mTreesTextureRegion,
            vertexBufferObjectManager
        )

        backgroundSprite = Sprite(
            0F,
            0F,
            CAMERA_WIDTH,
            CAMERA_HEIGHT,
            mTextures!!.mBackgroundTextureRegion,
            vertexBufferObjectManager
        )

        parallaxBackground!!.attachParallaxEntity(
            ParallaxBackground.ParallaxEntity(
                5F,
                treesSprite
            )
        )
        parallaxBackground!!.attachParallaxEntity(
            ParallaxBackground.ParallaxEntity(
                15F,
                backgroundSprite!!
            )
        )

        scene.background = parallaxBackground

        controllerSprite = object : Sprite(
            0F,
            CAMERA_HEIGHT - CAMERA_HEIGHT * 0.3F,
            CAMERA_HEIGHT * 0.3F,
            CAMERA_HEIGHT * 0.3F,
            mTextures!!.controllerFrameTextureRegion,
            vertexBufferObjectManager
        ) {

            override fun onAreaTouched(
                pSceneTouchEvent: TouchEvent?,
                pTouchAreaLocalX: Float,
                pTouchAreaLocalY: Float
            ): Boolean {

                // Change stick position
                if (pSceneTouchEvent!!.isActionMove) {

                    val xPos = pSceneTouchEvent.x
                    val yPos = pSceneTouchEvent.y

                    mCharacter!!.hasCondition = true

                    // To run condition
                    mCharacter!!.characterConditions["run"]!!["active"] = true

                    // If stick is to the right side then change direction
                    mCharacter!!.spriteDirection =
                        xPos >= abs(controllerSprite!!.width * 0.5F) && xPos >= 0

                    // Stick position checking
                    val displacement =
                        (controllerSprite!!.width / 2 - xPos - controllerStickSprite!!.width / 2).toDouble().pow(
                            2.0
                        ) +
                                (CAMERA_HEIGHT - controllerSprite!!.height / 2 - yPos).toDouble().pow(
                                    2.0
                                )
                    val baseRadius =
                        (controllerSprite!!.width / 2 + controllerStickSprite!!.width / 2).toDouble()
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

                    mCharacter!!.characterConditions["run"]!!["active"] = false
                    mCharacter!!.characterConditions["run"]!!["state"] = false
                    mCharacter!!.hasCondition = false
                    return false
                }

                return true
            }
        }

        controllerStickSprite = Sprite(
            controllerSprite!!.width / 4,
            CAMERA_HEIGHT - controllerSprite!!.height / 2 - controllerSprite!!.width * 0.5F / 2,
            controllerSprite!!.width * 0.5F,
            controllerSprite!!.height * 0.5F,
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
                return if (pSceneTouchEvent!!.isActionMove || pSceneTouchEvent.isActionDown) {

                    attackButtonSprite!!.red = 0F
                    mCharacter!!.characterConditions["attack"]!!["active"] = true
                    mCharacter!!.hasCondition = true
                    true
                } else {
                    mCharacter!!.characterConditions["attack"]!!["active"] = false
                    attackButtonSprite!!.red = 1F
                    mCharacter!!.hasCondition = false
                    false
                }
            }
        }

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

        // Adds fpsText to the hud
        hudLayer!!.attachChild(controllerStickSprite)
        hudLayer!!.attachChild(controllerSprite)
        hudLayer!!.attachChild(healthBarSpriteFilling)
        hudLayer!!.attachChild(healthBarSprite)
        hudLayer!!.attachChild(attackButtonSprite)
        hudLayer!!.attachChild(coinsCounter)
        hudLayer!!.attachChild(fpsText)

        // Register touch areas
        hudLayer!!.registerTouchArea(controllerSprite)
        hudLayer!!.registerTouchArea(attackButtonSprite)

        // Adds the HUD to your camera
        this.mCamera!!.hud = hudLayer

        // Start globalTimer
        startGlobalFrameTimer(scene)
        return scene
    }

    private fun startGlobalFrameTimer(scene: Scene) {
        if (!isTimerActive) {
            val enemiesList = mutableMapOf<Enemies, AnimatedSprite>()
            val itemsList = mutableMapOf<Items, Sprite>()
            characterTimer = Timer()
            // Character Conditions
            characterTimerTask = object : TimerTask() {
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
                                mCamera!!.hud.isVisible = false

                                with(enemiesList.iterator()) {
                                    forEach {
                                        scene.detachChild(it.value)
                                        remove()
                                    }
                                }

                                runOnUiThread {

                                    val builder = AlertDialog.Builder(this@MainActivity)

                                    // Set the alert dialog title
                                    builder.setTitle("You're dead")

                                    // Display a message on alert dialog
                                    builder.setMessage("Wanna restart?")

                                    // Display a negative button on alert dialog
                                    builder.setPositiveButton("Yes") { _, _ ->
                                        val mRestartActivity = Intent(
                                            this@MainActivity, MainActivity::class.java
                                        )
                                        val mPendingIntentId = 123456
                                        val mPendingIntent = PendingIntent.getActivity(
                                            this@MainActivity,
                                            mPendingIntentId,
                                            mRestartActivity,
                                            PendingIntent.FLAG_CANCEL_CURRENT
                                        )
                                        val mgr: AlarmManager =
                                            this@MainActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        mgr.set(
                                            AlarmManager.RTC,
                                            System.currentTimeMillis(),
                                            mPendingIntent
                                        )
                                        exitProcess(0)
                                    }

                                    // Display a neutral button on alert dialog
                                    builder.setNegativeButton("No") { _, _ -> }

                                    // Finally, make the alert dialog using builder
                                    val dialog: AlertDialog = builder.create()

                                    // Display the alert dialog on app interface
                                    dialog.show()
                                }


                                this.cancel()
                                enemyTimerTask!!.cancel()
                            }

                        } else {

                            // If running
                            if (mCharacter!!.characterConditions["run"]!!["active"]!! && !mCharacter!!.characterConditions["attack"]!!["active"]!!) {

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

                                characterAnimation!!.setPosition(
                                    characterPositionX,
                                    characterPositionY
                                )

//                                if (characterAnimation!!.isFlippedHorizontal) {} else {}

                                if (characterAnimation!!.isFlippedHorizontal) {

                                    // Decrement characters position each tick
                                    characterPositionX -= characterAnimation!!.width * 0.05F
                                    mCamera!!.setCenter(
                                        characterPositionX + characterAnimation!!.width * .5F,
                                        mCamera!!.targetCenterY
                                    )

                                    // Background negative shift
                                    parallaxPosition += 1
                                    parallaxBackground!!.setParallaxValue(parallaxPosition)
                                } else {
                                    // Increment characters position each tick
                                    characterPositionX += characterAnimation!!.width * 0.05F
                                    mCamera!!.setCenter(
                                        characterPositionX + characterAnimation!!.width * .5F,
                                        mCamera!!.targetCenterY
                                    )
                                    // Background negative shift
                                    parallaxPosition -= 1
                                    parallaxBackground!!.setParallaxValue(parallaxPosition)
                                }
                            }
                            if (mCharacter!!.characterConditions["attack"]!!["active"]!!) {

                                if (!mCharacter!!.characterConditions["attack"]!!["state"]!!) {
                                    mCharacter!!.characterConditions["attack"]!!["state"] = true
                                    scene.detachChild(characterAnimation)
                                    characterAnimation = mCharacter!!.setAttackAnimation(
                                        characterPositionX,
                                        characterPositionY
                                    )

                                    if (!characterAnimation!!.isAnimationRunning) {
                                        characterAnimation!!.animate(mCharacter!!.attackFrameDuration)
                                        scene.attachChild(characterAnimation)
                                    }
                                }
                                if (characterAnimation!!.currentTileIndex >= characterAnimation!!.tileCount - 1) {
//                                    mCharacter!!.characterConditions["attack"]!!["active"] = false
                                    mCharacter!!.characterConditions["attack"]!!["state"] = false
                                }
                            }

                            // Else default idle
                            else {
                                if (!mCharacter!!.hasCondition) {
                                    scene.detachChild(characterAnimation)
                                    mCharacter!!.hasCondition = true

                                    characterAnimation = mCharacter!!.setIdleAnimation(
                                        characterPositionX,
                                        characterPositionY
                                    )
                                    characterAnimation!!.animate(mCharacter!!.idleFrameDuration)
                                    scene.attachChild(characterAnimation)
                                }
                            }
                        }

                        // Flip sprite horizontally by current direction
                        if (!mCharacter!!.spriteDirection) {
                            characterAnimation!!.setFlipped(true, false)
                        } else {
                            characterAnimation!!.setFlipped(false, false)
                        }
                    }
                }
            }
            enemyTimer = Timer()
            enemyTimerTask = object : TimerTask() {
                override fun run() {
                    // Checking collision with enemies
                    with(enemiesList.iterator()) {
                        forEach {
                            // Move enemy on each tick
                            it.value.x -= it.value.width * 0.04F
                            if (characterAnimation!!.collidesWith(it.value)) {
                                if (mCharacter!!.characterConditions["attack"]!!["active"] == true ||
                                    mCharacter!!.characterConditions["attack"]!!["state"] == true
                                ) {
                                    // TODO On collides change enemy animation to attack
                                    // TODO The enemy must follow the player

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
                                        val mItems = Items(this@MainActivity, engine)
                                        val coinSprite = mItems.dropCoin(
                                            CAMERA_WIDTH, CAMERA_HEIGHT
                                        )
                                        coinSprite.x -= itemsList.size * coinSprite.width * .5F
                                        itemsList[mItems] = coinSprite
                                        coinsCounter!!.text = itemsList.size.toString()
                                        if (itemsList.size < 10) {
                                            hudLayer!!.attachChild(coinSprite)
                                        }
                                        remove()
                                    } else {
                                        // enemy attack animation
                                        it.key.characterConditions["attack"]!!["active"] = true
                                        if (!it.key.characterConditions["attack"]!!["active"]!!) {
                                            scene.detachChild(it.value)
                                            val tempPositionX = it.value.x
                                            it.setValue(
                                                it.key.spawnEnemy(
                                                    CAMERA_WIDTH,
                                                    CAMERA_HEIGHT
                                                )
                                            )
                                            it.value.x = tempPositionX
                                            it.value.animate(it.key.attackFrameDuration)
                                            scene.attachChild(it.value)
                                        }

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

                    // spawn enemy with 2% chance
                    if (Random().nextInt(100) == 10) {
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
                        enemySprite.x -= enemySprite.width * 0.04F

                        enemiesList[enemy] = enemySprite
                        enemySprite.x =
                            mCamera!!.centerX + CAMERA_WIDTH / 2 - (enemySprite.width)
                        scene.attachChild(enemySprite)
                    }
                }
            }

            characterTimer!!.scheduleAtFixedRate(characterTimerTask, 0, 100)
            enemyTimer!!.scheduleAtFixedRate(enemyTimerTask, 0, 100)
            isTimerActive = true
        }
    }
}
