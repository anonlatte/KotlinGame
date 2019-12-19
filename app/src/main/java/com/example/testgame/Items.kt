package com.example.testgame

import org.andengine.engine.Engine
import org.andengine.entity.sprite.Sprite
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory
import org.andengine.opengl.texture.region.ITextureRegion
import org.andengine.ui.activity.BaseGameActivity

class Items(activity: BaseGameActivity, engine: Engine) : Character(activity, engine) {
    var coinTextureRegion: ITextureRegion? = null

    init {

        val itemsTexture =
            BitmapTextureAtlas(engine.textureManager, 16, 16)

        coinTextureRegion =
            BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                itemsTexture, activity, "itemPack/coinSprite.png", 0, 0
            )
        itemsTexture.load()

    }

    fun dropCoin(positionX: Float, positionY: Float): Sprite {
        return Sprite(
            positionX,
            positionY,
            128f,
            128f,
            coinTextureRegion,
            engine!!.vertexBufferObjectManager
        )
    }

}