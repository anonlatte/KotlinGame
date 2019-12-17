package com.example.testgame

import org.andengine.engine.Engine
import org.andengine.opengl.texture.ITexture
import org.andengine.opengl.texture.TextureOptions
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory
import org.andengine.opengl.texture.bitmap.BitmapTexture
import org.andengine.opengl.texture.region.ITextureRegion
import org.andengine.opengl.texture.region.ITiledTextureRegion
import org.andengine.opengl.texture.region.TextureRegionFactory
import org.andengine.ui.activity.BaseGameActivity
import org.andengine.util.adt.io.`in`.IInputStreamOpener
import org.andengine.util.debug.Debug
import java.io.IOException

class Textures(var activity: BaseGameActivity, engine: Engine) {

    var adventurerTextureRegion: ITiledTextureRegion? = null
    var mBackgroundTextureRegion: ITextureRegion? = null
    var mControllerFrame: ITextureRegion? = null
    var mControllerStick: ITextureRegion? = null

    init {

        try {
            // 1 - Set up bitmap textures
            val backgroundTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("background.png") })
            val controllerFrameTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("VirtualJoystickPack/Joystick.png") })
            val controllerStickTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("VirtualJoystickPack/SmallHandleFilled.png") })


            val adventurerTexture =
                BitmapTextureAtlas(engine.textureManager, 200, 37, TextureOptions.BILINEAR)

            // Main character
            adventurerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                adventurerTexture, activity, "spritesheet.png", 0, 0, 4, 1
            )

            // 2 - Load bitmap textures into VRAM
            backgroundTexture.load()
            controllerFrameTexture.load()
            controllerStickTexture.load()
            adventurerTexture.load()

            // 3 - Set up texture regions
            this.mBackgroundTextureRegion =
                TextureRegionFactory.extractFromTexture(backgroundTexture)

            // Joystick
            this.mControllerFrame =
                TextureRegionFactory.extractFromTexture(controllerFrameTexture)
            this.mControllerStick =
                TextureRegionFactory.extractFromTexture(controllerStickTexture)

        } catch (e: IOException) {
            Debug.e(e)
        }
    }

}