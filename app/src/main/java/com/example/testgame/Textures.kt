package com.example.testgame

import org.andengine.engine.Engine
import org.andengine.opengl.texture.ITexture
import org.andengine.opengl.texture.bitmap.BitmapTexture
import org.andengine.opengl.texture.region.ITextureRegion
import org.andengine.opengl.texture.region.TextureRegionFactory
import org.andengine.ui.activity.BaseGameActivity
import org.andengine.util.adt.io.`in`.IInputStreamOpener
import org.andengine.util.debug.Debug
import java.io.IOException

open class Textures(private var activity: BaseGameActivity, engine: Engine) {

    var mBackgroundTextureRegion: ITextureRegion? = null
    var mTreesTextureRegion: ITextureRegion? = null
    var mControllerFrame: ITextureRegion? = null
    var mControllerStick: ITextureRegion? = null
    var attackButtonTextureRegion: ITextureRegion? = null

    init {

        try {
            // Set up bitmap textures
            val backgroundTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("background.png") })
            val treesTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("background/parallax.png") })
            val controllerFrameTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("VirtualJoystickPack/Joystick.png") })
            val controllerStickTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("VirtualJoystickPack/SmallHandleFilled.png") })

            val attackButtonTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("itemPack/Item__07.png") })

            // Load bitmap textures into VRAM
            backgroundTexture.load()
            treesTexture.load()
            controllerFrameTexture.load()
            controllerStickTexture.load()
            attackButtonTexture.load()

            // Set up texture regions
            this.mBackgroundTextureRegion =
                TextureRegionFactory.extractFromTexture(backgroundTexture)
            this.mTreesTextureRegion =
                TextureRegionFactory.extractFromTexture(backgroundTexture)

            // Joystick
            this.mControllerFrame =
                TextureRegionFactory.extractFromTexture(controllerFrameTexture)
            this.mControllerStick =
                TextureRegionFactory.extractFromTexture(controllerStickTexture)

            this.attackButtonTextureRegion =
                TextureRegionFactory.extractFromTexture(attackButtonTexture)

        } catch (e: IOException) {
            Debug.e(e)
        }
    }

}