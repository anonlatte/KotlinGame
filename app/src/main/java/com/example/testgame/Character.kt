package com.example.testgame

import org.andengine.engine.Engine
import org.andengine.entity.sprite.AnimatedSprite
import org.andengine.opengl.texture.TextureOptions
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory
import org.andengine.opengl.texture.region.ITiledTextureRegion
import org.andengine.ui.activity.BaseGameActivity
import org.andengine.util.debug.Debug
import java.io.IOException

class Character(activity: BaseGameActivity, engine: Engine) : Textures(activity, engine) {

    var adventurerIdleTextureRegion: ITiledTextureRegion? = null
    var adventurerRunTextureRegion: ITiledTextureRegion? = null
    var adventurerJumpTextureRegion: ITiledTextureRegion? = null
    var adventurerFallTextureRegion: ITiledTextureRegion? = null
    var adventurerAttackTextureRegion: ITiledTextureRegion? = null
    var adventurerDieTextureRegion: ITiledTextureRegion? = null
    private var mActivity: BaseGameActivity? = null
    private var engine: Engine? = null
    var isAnimationChanged: Boolean = false
    var isActionGoing: Boolean = false

    companion object {
        var characterWidth = 350F
        var characterHeight = 259F
    }

    init {
        this.mActivity = activity
        this.engine = engine
        try {
            // Setting up sprite sheets
            val adventurerIdleTexture =
                BitmapTextureAtlas(engine.textureManager, 200, 37, TextureOptions.BILINEAR)
            val adventurerRunTexture =
                BitmapTextureAtlas(engine.textureManager, 300, 37, TextureOptions.BILINEAR)
            val adventurerJumpTexture =
                BitmapTextureAtlas(engine.textureManager, 200, 37, TextureOptions.BILINEAR)
            val adventurerFallTexture =
                BitmapTextureAtlas(engine.textureManager, 100, 37, TextureOptions.BILINEAR)
            val adventurerAttackTexture =
                BitmapTextureAtlas(engine.textureManager, 250, 37, TextureOptions.BILINEAR)
            val adventurerDieTexture =
                BitmapTextureAtlas(engine.textureManager, 350, 37, TextureOptions.BILINEAR)

            // Setting up frames
            adventurerIdleTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerIdleTexture, activity, "idle1.png", 0, 0, 4, 1
                )
            adventurerRunTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerRunTexture, activity, "run.png", 0, 0, 6, 1
                )
            adventurerJumpTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerJumpTexture, activity, "jump.png", 0, 0, 4, 1
                )
            adventurerFallTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerFallTexture, activity, "fall.png", 0, 0, 2, 1
                )
            adventurerAttackTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerAttackTexture, activity, "attack.png", 0, 0, 5, 1
                )
            adventurerDieTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                    adventurerDieTexture, activity, "die.png", 0, 0, 7, 1
                )
            // Load bitmap textures into VRAM
            adventurerIdleTexture.load()
            adventurerRunTexture.load()
            adventurerJumpTexture.load()
            adventurerFallTexture.load()
            adventurerAttackTexture.load()
            adventurerDieTexture.load()
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

    fun setRunAnimation(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            characterWidth,
            characterHeight,
            adventurerRunTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }
}