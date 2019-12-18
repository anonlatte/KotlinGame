package com.example.testgame

import org.andengine.engine.camera.Camera
import org.andengine.entity.Entity
import org.andengine.entity.shape.IAreaShape
import org.andengine.opengl.util.GLState


class ParallaxLayer : Entity {
    // ===========================================================
// Constants
// ===========================================================
// ===========================================================
// Fields
// ===========================================================
    private val mParallaxEntities = ArrayList<ParallaxEntity>()
    private var mParallaxEntityCount = 0
    protected var mParallaxValue = 0f
    protected var mParallaxScrollValue = 0f
    protected var mParallaxChangePerSecond = 0f
    protected var mParallaxScrollFactor = 0.2f
    private var mCamera: Camera? = null
    private var mCameraPreviousX = 0f
    private var mCameraOffsetX = 0f
    private var mLevelWidth = 0f
    private var mIsScrollable = false

    // ===========================================================
// Constructors
// ===========================================================
    constructor() {}

    constructor(camera: Camera, mIsScrollable: Boolean) {
        mCamera = camera
        this.mIsScrollable = mIsScrollable
        mCameraPreviousX = camera.getCenterX()
    }

    constructor(camera: Camera, mIsScrollable: Boolean, mLevelWidth: Int) {
        mCamera = camera
        this.mIsScrollable = mIsScrollable
        this.mLevelWidth = mLevelWidth.toFloat()
        mCameraPreviousX = camera.getCenterX()
    }

    // ===========================================================
// Getter & Setter
// ===========================================================
    fun setParallaxValue(pParallaxValue: Float) {
        mParallaxValue = pParallaxValue
    }

    fun setParallaxChangePerSecond(pParallaxChangePerSecond: Float) {
        mParallaxChangePerSecond = pParallaxChangePerSecond
    }

    fun setParallaxScrollFactor(pParallaxScrollFactor: Float) {
        mParallaxScrollFactor = pParallaxScrollFactor
    }

    // ===========================================================
// Methods for/from SuperClass/Interfaces
// ===========================================================
    override fun onManagedDraw(pGLState: GLState, pCamera: Camera) {
        super.preDraw(pGLState, pCamera)
        val parallaxValue = mParallaxValue
        val parallaxScrollValue = mParallaxScrollValue
        val parallaxEntities = mParallaxEntities
        for (i in 0 until mParallaxEntityCount) {
            if (parallaxEntities[i].mIsScrollable) {
                parallaxEntities[i].onDraw(pGLState, pCamera, parallaxScrollValue, mLevelWidth)
            } else {
                parallaxEntities[i].onDraw(pGLState, pCamera, parallaxValue, mLevelWidth)
            }
        }
    }

    override fun onManagedUpdate(pSecondsElapsed: Float) {
        if (mIsScrollable && mCameraPreviousX != mCamera!!.getCenterX()) {
            mCameraOffsetX = mCameraPreviousX - mCamera!!.getCenterX()
            mCameraPreviousX = mCamera!!.getCenterX()
            mParallaxScrollValue += mCameraOffsetX * mParallaxScrollFactor
            mCameraOffsetX = 0f
        }
        mParallaxValue += mParallaxChangePerSecond * pSecondsElapsed
        super.onManagedUpdate(pSecondsElapsed)
    }

    // ===========================================================
// Methods
// ===========================================================
    fun attachParallaxEntity(parallaxEntity: ParallaxEntity) {
        mParallaxEntities.add(parallaxEntity)
        mParallaxEntityCount++
    }

    fun detachParallaxEntity(pParallaxEntity: ParallaxEntity?): Boolean {
        mParallaxEntityCount--
        val success = mParallaxEntities.remove(pParallaxEntity)
        if (!success) {
            mParallaxEntityCount++
        }
        return success
    }

    // ===========================================================
// Inner and Anonymous Classes
// ===========================================================
    class ParallaxEntity {
        // ===========================================================
// Constants
// ===========================================================
// ===========================================================
// Fields
// ===========================================================
        val mParallaxFactor: Float
        val mAreaShape: IAreaShape
        val mIsScrollable: Boolean
        val shapeWidthScaled: Float

        // ===========================================================
// Constructors
// ===========================================================
        constructor(pParallaxFactor: Float, pAreaShape: IAreaShape) {
            mParallaxFactor = pParallaxFactor
            mAreaShape = pAreaShape
            mIsScrollable = false
            shapeWidthScaled = mAreaShape.widthScaled
        }

        constructor(
            pParallaxFactor: Float,
            pAreaShape: IAreaShape,
            mIsScrollable: Boolean
        ) {
            mParallaxFactor = pParallaxFactor
            mAreaShape = pAreaShape
            this.mIsScrollable = mIsScrollable
            shapeWidthScaled = mAreaShape.widthScaled
        }

        constructor(
            pParallaxFactor: Float,
            pAreaShape: IAreaShape,
            mIsScrollable: Boolean,
            mReduceFrequency: Int
        ) {
            mParallaxFactor = pParallaxFactor
            mAreaShape = pAreaShape
            this.mIsScrollable = mIsScrollable
            shapeWidthScaled = mAreaShape.widthScaled * mReduceFrequency
        }

        // ===========================================================
// Getter & Setter
// ===========================================================
// ===========================================================
// Methods for/from SuperClass/Interfaces
// ===========================================================
// ===========================================================
// Methods
// ===========================================================
        fun onDraw(
            pGLState: GLState,
            pCamera: Camera,
            pParallaxValue: Float,
            mLevelWidth: Float
        ) {
            pGLState.pushModelViewGLMatrix()
            run {
                val widthRange: Float
                widthRange = if (mLevelWidth != 0f) {
                    mLevelWidth
                } else {
                    pCamera.getWidth()
                }
                var baseOffset =
                    pParallaxValue * this.mParallaxFactor % shapeWidthScaled
                while (baseOffset > 0) {
                    baseOffset -= shapeWidthScaled
                }
                pGLState.translateModelViewGLMatrixf(baseOffset, 0f, 0f)
                var currentMaxX = baseOffset
                do {
                    this.mAreaShape.onDraw(pGLState, pCamera)
                    pGLState.translateModelViewGLMatrixf(shapeWidthScaled - 1, 0f, 0f)
                    currentMaxX += shapeWidthScaled
                } while (currentMaxX < widthRange)
            }
            pGLState.popModelViewGLMatrix()
        } // ===========================================================
// Inner and Anonymous Classes
// ===========================================================
    }
}