package com.example.testgame

import org.andengine.engine.Engine
import org.andengine.entity.sprite.AnimatedSprite
import org.andengine.opengl.texture.TextureOptions
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory
import org.andengine.opengl.texture.region.ITiledTextureRegion
import org.andengine.ui.activity.BaseGameActivity

class Enemies(activity: BaseGameActivity, engine: Engine) : Character(activity, engine) {
    private var minotaurRunTextureRegion: ITiledTextureRegion? = null
    private var minotaurAttackTextureRegion: ITiledTextureRegion? = null
    private var minotaurDieTextureRegion: ITiledTextureRegion? = null
    var characterPositionX = 0F
    var characterPositionY = 0F

    init {
        this.mActivity = activity
        this.engine = engine

        val minotaurRunTexture =
            BitmapTextureAtlas(engine.textureManager, 744, 96, TextureOptions.BILINEAR)
        val minotaurAttackTexture =
            BitmapTextureAtlas(engine.textureManager, 837, 96, TextureOptions.BILINEAR)
        val minotaurDieTexture =
            BitmapTextureAtlas(engine.textureManager, 558, 96, TextureOptions.BILINEAR)

        minotaurRunTextureRegion =
            BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                minotaurRunTexture, activity, "minotaur/minotaur_run.png", 0, 0, 8, 1
            )
        minotaurAttackTextureRegion =
            BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                minotaurAttackTexture, activity, "minotaur/minotaur_attack.png", 0, 0, 9, 1
            )
        minotaurDieTextureRegion =
            BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                minotaurDieTexture, activity, "minotaur/minotaur_die.png", 0, 0, 6, 1
            )

        minotaurRunTexture.load()
        minotaurAttackTexture.load()
        minotaurDieTexture.load()
    }

    fun spawnEnemy(enemyPositionX: Float, enemyPositionY: Float): AnimatedSprite {
        val mEnemy = Enemies(this.mActivity!!, this.engine!!)

        val enemyAnimation = mEnemy.setRunAnimation(
            enemyPositionX,
            enemyPositionY
        )
        enemyAnimation.animate(frameDuration)
        return enemyAnimation
    }

    override fun setRunAnimation(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            characterWidth,
            characterHeight,
            minotaurRunTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    override fun setAttackAnimation(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            characterWidth,
            characterHeight,
            minotaurAttackTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    override fun setDieAnimation(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            characterWidth,
            characterHeight,
            minotaurDieTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    companion object {
        // Sizes of the character
        var characterWidth = 93F * 4
        var characterHeight = 96F * 4
    }
}