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

open class Textures(var activity: BaseGameActivity, engine: Engine) {

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

            // 2 - Load bitmap textures into VRAM
            backgroundTexture.load()
            controllerFrameTexture.load()
            controllerStickTexture.load()

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