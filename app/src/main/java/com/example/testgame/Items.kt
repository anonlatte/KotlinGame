package com.example.testgame

import org.andengine.engine.Engine
import org.andengine.entity.sprite.AnimatedSprite
import org.andengine.opengl.texture.TextureOptions
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory
import org.andengine.opengl.texture.region.ITiledTextureRegion
import org.andengine.ui.activity.BaseGameActivity

class Items(activity: BaseGameActivity, engine: Engine) : Character(activity, engine) {
    var coinTextureRegion: ITiledTextureRegion? = null

    val coinFrameDuration = longArrayOf(125, 125, 125, 125, 125)

    init {
        val coinTexture =
            BitmapTextureAtlas(engine.textureManager, 80, 16, TextureOptions.BILINEAR)
        coinTextureRegion =
            BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                coinTexture, activity, "itemPack/MonedaD.png", 0, 0, 5, 1
            )

        coinTexture.load()

    }

    fun dropCoin(enemyPositionX: Float, enemyPositionY: Float): AnimatedSprite {
        val coin = Items(this.mActivity!!, this.engine!!)

        val coinAnimation = coin.flipCoin(
            enemyPositionX,
            enemyPositionY
        )
        coinAnimation.animate(coinFrameDuration)
        return coinAnimation
    }

    private fun flipCoin(xPosition: Float, yPosition: Float): AnimatedSprite {
        return AnimatedSprite(
            xPosition,
            yPosition,
            128f,
            128f,
            coinTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

}