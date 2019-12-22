package com.example.testgame

import org.andengine.engine.Engine
import org.andengine.entity.sprite.Sprite
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory
import org.andengine.opengl.texture.region.ITextureRegion
import org.andengine.ui.activity.SimpleBaseGameActivity

class Items(activity: SimpleBaseGameActivity, engine: Engine) : Character(activity, engine) {
    private var coinTextureRegion: ITextureRegion? = null

    init {

        val itemsTexture =
            BitmapTextureAtlas(engine.textureManager, 16, 16)

        coinTextureRegion =
            BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                itemsTexture, activity, "itemPack/coinSprite.png", 0, 0
            )
        itemsTexture.load()

    }

    fun dropCoin(screenWidth: Float, screenHeight: Float): Sprite {

        val itemWidth = screenWidth * 0.05F
        val itemHeight = screenWidth * 0.05F
        val itemPositionX = screenWidth - itemWidth
        val itemPositionY = 10F
        return Sprite(
            itemPositionX,
            itemPositionY,
            itemWidth,
            itemHeight,
            coinTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

}