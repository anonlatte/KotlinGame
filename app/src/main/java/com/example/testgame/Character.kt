package com.example.testgame

import org.andengine.engine.Engine
import org.andengine.entity.sprite.AnimatedSprite
import org.andengine.opengl.texture.TextureOptions
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory
import org.andengine.opengl.texture.region.ITiledTextureRegion
import org.andengine.ui.activity.SimpleBaseGameActivity
import org.andengine.util.debug.Debug
import java.io.IOException


open class Character(activity: SimpleBaseGameActivity, engine: Engine) :
    Textures(activity, engine) {

    var spriteDirection: Boolean = true
    private var adventurerIdleTextureRegion: ITiledTextureRegion? = null
    private var adventurerRunTextureRegion: ITiledTextureRegion? = null
    private var adventurerJumpTextureRegion: ITiledTextureRegion? = null
    private var adventurerFallTextureRegion: ITiledTextureRegion? = null
    private var adventurerAttackTextureRegion: ITiledTextureRegion? = null
    private var adventurerDieTextureRegion: ITiledTextureRegion? = null


    protected var mActivity: SimpleBaseGameActivity? = null
    protected var engine: Engine? = null

    var characterConditions = mutableMapOf(
        "run" to mutableMapOf("active" to false, "state" to false),
        "attack" to mutableMapOf("active" to false, "state" to false),
        "die" to mutableMapOf("active" to false, "state" to false)
    )
    var hasCondition = false
    var healthPoints = 100F
    //    var manaPoints = 100F
    val idleFrameDuration = longArrayOf(125, 125, 125, 125)
    open val attackFrameDuration = longArrayOf(125, 125, 125, 125, 125)
    open val runFrameDuration = longArrayOf(125, 125, 125, 125, 125, 125)
    open val dieFrameDuration = longArrayOf(125, 125, 125, 125, 125, 125, 125)

    // Sizes of the character
    var characterWidth = 0F
    var characterHeight = 0F

    init {
        this.mActivity = activity
        this.engine = engine
        try {
            // Setting up sprite sheets
            val adventurerTexture =
                BitmapTextureAtlas(engine.textureManager, 350, 185, TextureOptions.BILINEAR)


            // Setting up frames
            adventurerIdleTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerTexture, activity, "idle1.png", 0, 0, 4, 1
                )
            adventurerRunTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerTexture, activity, "run.png", 0, 37, 6, 1
                )
            adventurerJumpTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerTexture, activity, "jump.png", 0, 74, 4, 1
                )
            adventurerAttackTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerTexture, activity, "attack.png", 0, 111, 5, 1
                )
            adventurerFallTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerTexture, activity, "fall.png", 250, 111, 2, 1
                )
            adventurerDieTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerTexture, activity, "die.png", 0, 148, 7, 1
                )

            // Load bitmap textures into VRAM
            adventurerTexture.load()

        } catch (e: IOException) {
            Debug.e(e)
        }

    }

    fun setIdleAnimation(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            characterWidth,
            characterHeight,
            adventurerIdleTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    open fun setRunAnimation(
        xPosition: Float,
        yPosition: Float
    ): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            characterWidth,
            characterHeight,
            adventurerRunTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    open fun setRunAnimation(
        xPosition: Float,
        yPosition: Float,
        spriteWidth: Float,
        spriteHeight: Float
    ): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            characterWidth,
            characterHeight,
            adventurerRunTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    open fun setAttackAnimation(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            characterWidth,
            characterHeight,
            adventurerAttackTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    open fun setDieAnimation(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            characterWidth,
            characterHeight,
            adventurerDieTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }


    fun zeroizeConditions(): MutableMap<String, MutableMap<String, Boolean>> {
        return mutableMapOf(
            "idle" to mutableMapOf("active" to false, "state" to false),
            "run" to mutableMapOf("active" to false, "state" to false),
            "attack" to mutableMapOf("active" to false, "state" to false),
            "die" to mutableMapOf("active" to false, "state" to false)
        )
    }
}
