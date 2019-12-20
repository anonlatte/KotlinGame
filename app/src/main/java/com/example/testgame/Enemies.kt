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
    override val attackFrameDuration = longArrayOf(125, 125, 125, 125, 125, 125, 125, 125, 125)
    override val runFrameDuration = longArrayOf(125, 125, 125, 125, 125, 125, 125, 125)
    override val dieFrameDuration = longArrayOf(125, 125, 125, 125, 125, 125)

    init {
        this.mActivity = activity
        this.engine = engine

        val minotaurTexture =
            BitmapTextureAtlas(engine.textureManager, 837, 288, TextureOptions.BILINEAR)

        minotaurRunTextureRegion =
            BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                minotaurTexture, activity, "minotaur/minotaur_run.png", 0, 0, 8, 1
            )
        minotaurAttackTextureRegion =
            BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                minotaurTexture, activity, "minotaur/minotaur_attack.png", 0, 96, 9, 1
            )
        minotaurDieTextureRegion =
            BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                minotaurTexture, activity, "minotaur/minotaur_die.png", 0, 192, 6, 1
            )

        minotaurTexture.load()
    }

    fun spawnEnemy(
        screenWidth: Float,
        screenHeight: Float
    ): AnimatedSprite {
        val mEnemy = Enemies(this.mActivity!!, this.engine!!)

        val enemyWidth = screenWidth / 96F * 14
        val enemyHeight = screenHeight / 93F * 21
        val enemyPositionX = screenWidth - enemyWidth
        val enemyPositionY = screenHeight - enemyHeight * 1.125F
        val enemyAnimation = mEnemy.setRunAnimation(
            enemyPositionX,
            enemyPositionY,
            enemyWidth,
            enemyHeight
        )
        enemyAnimation.animate(runFrameDuration)
        return enemyAnimation
    }

    override fun setRunAnimation(
        xPosition: Float,
        yPosition: Float,
        spriteWidth: Float,
        spriteHeight: Float
    ): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            spriteWidth,
            spriteHeight,
            minotaurRunTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    override fun setAttackAnimation(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            enemyWidth,
            enemyHeight,
            minotaurAttackTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    override fun setDieAnimation(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            enemyWidth,
            enemyHeight,
            minotaurDieTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

    companion object {
        // Sizes of the character
        var enemyWidth = 93F
        var enemyHeight = 96F
    }
}