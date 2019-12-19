package com.example.testgame

import org.andengine.engine.Engine
import org.andengine.opengl.texture.ITexture
import org.andengine.opengl.texture.TextureOptions
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory
import org.andengine.opengl.texture.bitmap.BitmapTexture
import org.andengine.opengl.texture.region.ITextureRegion
import org.andengine.opengl.texture.region.TextureRegion
import org.andengine.opengl.texture.region.TextureRegionFactory
import org.andengine.ui.activity.BaseGameActivity
import org.andengine.util.adt.io.`in`.IInputStreamOpener
import org.andengine.util.debug.Debug
import java.io.IOException

open class Textures(private var activity: BaseGameActivity, engine: Engine) {

    var mBackgroundTextureRegion: ITextureRegion? = null
    var mTreesTextureRegion: ITextureRegion? = null

    var controllerFrameTextureRegion: ITextureRegion? = null
    var controllerStickTextureRegion: ITextureRegion? = null
    var attackButtonTextureRegion: ITextureRegion? = null
    var healthBarTextureRegion: TextureRegion? = null
    var healthBarFillingTextureRegion: TextureRegion? = null


    init {

        try {
            // Set up bitmap textures
            val backgroundTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("background.png") })
            val treesTexture: ITexture =
                BitmapTexture(engine.textureManager,
                    IInputStreamOpener { activity.assets.open("background/parallax.png") })

            val hudTextureRegion =
                BitmapTextureAtlas(engine.textureManager, 300, 400, TextureOptions.BILINEAR)

            // Setting up frames
            controllerFrameTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                    hudTextureRegion, activity, "hud/Joystick.png", 0, 0
                )
            controllerStickTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                    hudTextureRegion, activity, "hud/SmallHandleFilled.png", 0, 300
                )
            attackButtonTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                    hudTextureRegion, activity, "hud/Item__07.png", 100, 300
                )
            healthBarTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                    hudTextureRegion, activity, "hud/healthBar.png", 116, 300
                )
            healthBarFillingTextureRegion =
                BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                    hudTextureRegion, activity, "hud/healthBarFilling.png", 192, 300
                )


            // Load bitmap textures into VRAM
            backgroundTexture.load()
            treesTexture.load()
            hudTextureRegion.load()

            // Set up texture regions
            this.mBackgroundTextureRegion =
                TextureRegionFactory.extractFromTexture(backgroundTexture)
            this.mTreesTextureRegion =
                TextureRegionFactory.extractFromTexture(backgroundTexture)

            // Joystick


        } catch (e: IOException) {
            Debug.e(e)
        }
    }

}