package com.huolala.mockgps.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.huolala.mockgps.R
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * Created by kqw on 2016/8/30.
 * 摇杆控件
 */
class RockerView(context: Context, attrs: AttributeSet) :
    View(context, attrs) {
    private val mAreaBackgroundPaint: Paint
    private val mRockerPaint: Paint
    private var mRockerPosition: Point
    private val mCenterPoint: Point
    private var mAreaRadius = 0
    private var mRockerRadius = 0
    private var mCallBackMode = CallBackMode.CALL_BACK_MODE_MOVE
    private var mOnAngleChangeListener: OnAngleChangeListener? = null
    private var mOnShakeListener: OnShakeListener? = null
    private var mDirectionMode: DirectionMode? = null
    private var tempDirection = Direction.DIRECTION_CENTER
    private var mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT
    private var mAreaBitmap: Bitmap? = null
    private var mAreaColor = 0
    private var mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT
    private var mRockerBitmap: Bitmap? = null
    private var mRockerColor = 0
    private var currentTimeMillis: Long = 0L
    private val src = Rect()
    private val dst = Rect()

    init {

        // 获取自定义属性
        initAttribute(context, attrs)

        // 移动区域画笔
        mAreaBackgroundPaint = Paint()
        mAreaBackgroundPaint.isAntiAlias = true

        // 摇杆画笔
        mRockerPaint = Paint()
        mRockerPaint.isAntiAlias = true

        // 中心点
        mCenterPoint = Point()
        // 摇杆位置
        mRockerPosition = Point()
    }

    /**
     * 获取属性
     *
     * @param context context
     * @param attrs   attrs
     */
    private fun initAttribute(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RockerView)

        // 可移动区域背景
        val areaBackground = typedArray.getDrawable(R.styleable.RockerView_areaBackground)
        if (null != areaBackground) {
            // 设置了背景
            when (areaBackground) {
                is BitmapDrawable -> {
                    // 设置了一张图片
                    mAreaBitmap = areaBackground.bitmap
                    mAreaBackgroundMode = AREA_BACKGROUND_MODE_PIC
                }

                is GradientDrawable -> {
                    // XML
                    mAreaBitmap = drawable2Bitmap(areaBackground)
                    mAreaBackgroundMode = AREA_BACKGROUND_MODE_XML
                }

                is ColorDrawable -> {
                    // 色值
                    mAreaColor = areaBackground.color
                    mAreaBackgroundMode = AREA_BACKGROUND_MODE_COLOR
                }

                else -> {
                    // 其他形式
                    mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT
                }
            }
        } else {
            // 没有设置背景
            mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT
        }
        // 摇杆背景
        val rockerBackground = typedArray.getDrawable(R.styleable.RockerView_rockerBackground)
        if (null != rockerBackground) {
            // 设置了摇杆背景
            when (rockerBackground) {
                is BitmapDrawable -> {
                    // 图片
                    mRockerBitmap = rockerBackground.bitmap
                    mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_PIC
                }

                is GradientDrawable -> {
                    // XML
                    mRockerBitmap = drawable2Bitmap(rockerBackground)
                    mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_XML
                }

                is ColorDrawable -> {
                    // 色值
                    mRockerColor = rockerBackground.color
                    mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_COLOR
                }

                else -> {
                    // 其他形式
                    mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT
                }
            }
        } else {
            // 没有设置摇杆背景
            mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT
        }

        // 摇杆半径
        mRockerRadius = typedArray.getDimensionPixelOffset(
            R.styleable.RockerView_rockerRadius,
            DEFAULT_ROCKER_RADIUS
        )

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth: Int
        val measureHeight: Int
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        measureWidth = if (widthMode == MeasureSpec.EXACTLY) {
            // 具体的值和match_parent
            widthSize
        } else {
            // wrap_content
            DEFAULT_SIZE
        }
        measureHeight = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            DEFAULT_SIZE
        }
        setMeasuredDimension(measureWidth, measureHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val measuredWidth = measuredWidth
        val measuredHeight = measuredHeight
        val cx = measuredWidth / 2
        val cy = measuredHeight / 2
        // 中心点
        mCenterPoint.set(cx, cy)
        // 可移动区域的半径
        mAreaRadius = if (measuredWidth <= measuredHeight) cx else cy

        // 摇杆位置
        if (0 == mRockerPosition.x || 0 == mRockerPosition.y) {
            mRockerPosition.set(mCenterPoint.x, mCenterPoint.y)
        }

        // 画可移动区域
        if (AREA_BACKGROUND_MODE_PIC == mAreaBackgroundMode || AREA_BACKGROUND_MODE_XML == mAreaBackgroundMode) {
            // 图片
            src.set(0, 0, mAreaBitmap!!.width, mAreaBitmap!!.height)
            dst.set(
                mCenterPoint.x - mAreaRadius,
                mCenterPoint.y - mAreaRadius,
                mCenterPoint.x + mAreaRadius,
                mCenterPoint.y + mAreaRadius
            )
            canvas.drawBitmap(mAreaBitmap!!, src, dst, mAreaBackgroundPaint)
        } else if (AREA_BACKGROUND_MODE_COLOR == mAreaBackgroundMode) {
            // 色值
            mAreaBackgroundPaint.color = mAreaColor
            canvas.drawCircle(
                mCenterPoint.x.toFloat(),
                mCenterPoint.y.toFloat(),
                mAreaRadius.toFloat(),
                mAreaBackgroundPaint
            )
        } else {
            // 其他或者未设置
            mAreaBackgroundPaint.color = Color.GRAY
            canvas.drawCircle(
                mCenterPoint.x.toFloat(),
                mCenterPoint.y.toFloat(),
                mAreaRadius.toFloat(),
                mAreaBackgroundPaint
            )
        }

        // 画摇杆
        if (ROCKER_BACKGROUND_MODE_PIC == mRockerBackgroundMode || ROCKER_BACKGROUND_MODE_XML == mRockerBackgroundMode) {
            // 图片
            src.set(0, 0, mRockerBitmap!!.width, mRockerBitmap!!.height)
            dst.set(
                mRockerPosition.x - mRockerRadius,
                mRockerPosition.y - mRockerRadius,
                mRockerPosition.x + mRockerRadius,
                mRockerPosition.y + mRockerRadius
            )
            canvas.drawBitmap(mRockerBitmap!!, src, dst, mRockerPaint)
        } else if (ROCKER_BACKGROUND_MODE_COLOR == mRockerBackgroundMode) {
            // 色值
            mRockerPaint.color = mRockerColor
            canvas.drawCircle(
                mRockerPosition.x.toFloat(),
                mRockerPosition.y.toFloat(),
                mRockerRadius.toFloat(),
                mRockerPaint
            )
        } else {
            // 其他或者未设置
            mRockerPaint.color = Color.RED
            canvas.drawCircle(
                mRockerPosition.x.toFloat(),
                mRockerPosition.y.toFloat(),
                mRockerRadius.toFloat(),
                mRockerPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 回调 开始
                callBackStart()
//                val moveX = event.x
//                val moveY = event.y
//                mRockerPosition = getRockerPositionPoint(
//                    mCenterPoint,
//                    Point(moveX.toInt(), moveY.toInt()),
//                    mAreaRadius.toFloat(),
//                    mRockerRadius.toFloat()
//                )
                currentTimeMillis = System.currentTimeMillis()
                moveRocker(mRockerPosition.x.toFloat(), mRockerPosition.y.toFloat())
            }

            MotionEvent.ACTION_MOVE -> {
                val moveX = event.x
                val moveY = event.y
                //延迟才触发回调  防止首次方向与意图不一致问题
                System.currentTimeMillis().let {
                    if (it - currentTimeMillis >= 500) {
                        mRockerPosition = getRockerPositionPoint(
                            mCenterPoint,
                            Point(moveX.toInt(), moveY.toInt()),
                            mAreaRadius.toFloat(),
                            mRockerRadius.toFloat()
                        )
                    }
                }
                moveRocker(mRockerPosition.x.toFloat(), mRockerPosition.y.toFloat())
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 回调 结束
                callBackFinish()
                moveRocker(mCenterPoint.x.toFloat(), mCenterPoint.y.toFloat())
            }
        }
        return true
    }

    /**
     * 获取摇杆实际要显示的位置（点）
     *
     * @param centerPoint  中心点
     * @param touchPoint   触摸点
     * @param regionRadius 摇杆可活动区域半径
     * @param rockerRadius 摇杆半径
     * @return 摇杆实际显示的位置（点）
     */
    private fun getRockerPositionPoint(
        centerPoint: Point,
        touchPoint: Point,
        regionRadius: Float,
        rockerRadius: Float
    ): Point {
        // 两点在X轴的距离
        val lenX = (touchPoint.x - centerPoint.x).toFloat()
        // 两点在Y轴距离
        val lenY = (touchPoint.y - centerPoint.y).toFloat()
        // 两点距离
        val lenXY = sqrt((lenX * lenX + lenY * lenY).toDouble()).toFloat()
        // 计算弧度
        val radian =
            acos((lenX / lenXY).toDouble()) * if (touchPoint.y < centerPoint.y) -1 else 1
        // 计算角度
        val angle = radian2Angle(radian)

        // 回调 返回参数
        callBack(angle)
        return if (lenXY + rockerRadius <= regionRadius) { // 触摸位置在可活动范围内
            touchPoint
        } else { // 触摸位置在可活动范围以外
            // 计算要显示的位置
            val showPointX =
                (centerPoint.x + (regionRadius - rockerRadius) * cos(radian)).toInt()
            val showPointY =
                (centerPoint.y + (regionRadius - rockerRadius) * sin(radian)).toInt()
            Point(showPointX, showPointY)
        }
    }

    /**
     * 移动摇杆到指定位置
     *
     * @param x x坐标
     * @param y y坐标
     */
    private fun moveRocker(x: Float, y: Float) {
        mRockerPosition.set(x.toInt(), y.toInt())
        invalidate()
    }

    /**
     * 弧度转角度
     *
     * @param radian 弧度
     * @return 角度[0, 360)
     */
    private fun radian2Angle(radian: Double): Double {
        val tmp = (radian / Math.PI * 180).roundToInt().toDouble()
        return if (tmp >= 0) tmp else 360 + tmp
    }

    /**
     * Drawable 转 Bitmap
     *
     * @param drawable Drawable
     * @return Bitmap
     */
    private fun drawable2Bitmap(drawable: Drawable): Bitmap {
        // 取 drawable 的长宽
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        // 取 drawable 的颜色格式
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        // 建立对应 bitmap
        val bitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 回调
     * 开始
     */
    private fun callBackStart() {
        tempDirection = Direction.DIRECTION_CENTER
        if (null != mOnAngleChangeListener) {
            mOnAngleChangeListener!!.onStart()
        }
        if (null != mOnShakeListener) {
            mOnShakeListener!!.onStart()
        }
    }

    /**
     * 回调
     * 返回参数
     *
     * @param angle 摇动角度
     */
    private fun callBack(angle: Double) {
        if (null != mOnAngleChangeListener) {
            //调整回调  0°/360°为正上
            mOnAngleChangeListener!!.angle((angle + 90) % 360)
        }
        if (null != mOnShakeListener) {
            if (CallBackMode.CALL_BACK_MODE_MOVE == mCallBackMode) {
                when (mDirectionMode) {
                    DirectionMode.DIRECTION_2_HORIZONTAL -> if (ANGLE_0 <= angle && ANGLE_HORIZONTAL_2D_OF_0P > angle || ANGLE_HORIZONTAL_2D_OF_1P <= angle && ANGLE_360 > angle) {
                        // 右
                        mOnShakeListener!!.direction(Direction.DIRECTION_RIGHT)
                    } else if (ANGLE_HORIZONTAL_2D_OF_0P <= angle && ANGLE_HORIZONTAL_2D_OF_1P > angle) {
                        // 左
                        mOnShakeListener!!.direction(Direction.DIRECTION_LEFT)
                    }

                    DirectionMode.DIRECTION_2_VERTICAL -> if (ANGLE_VERTICAL_2D_OF_0P <= angle && ANGLE_VERTICAL_2D_OF_1P > angle) {
                        // 下
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN)
                    } else if (ANGLE_VERTICAL_2D_OF_1P <= angle && ANGLE_360 > angle) {
                        // 上
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP)
                    }

                    DirectionMode.DIRECTION_4_ROTATE_0 -> if (ANGLE_4D_OF_0P <= angle && ANGLE_4D_OF_1P > angle) {
                        // 右下
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN_RIGHT)
                    } else if (ANGLE_4D_OF_1P <= angle && ANGLE_4D_OF_2P > angle) {
                        // 左下
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN_LEFT)
                    } else if (ANGLE_4D_OF_2P <= angle && ANGLE_4D_OF_3P > angle) {
                        // 左上
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP_LEFT)
                    } else if (ANGLE_4D_OF_3P <= angle && ANGLE_360 > angle) {
                        // 右上
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP_RIGHT)
                    }

                    DirectionMode.DIRECTION_4_ROTATE_45 -> if (ANGLE_0 <= angle && ANGLE_ROTATE45_4D_OF_0P > angle || ANGLE_ROTATE45_4D_OF_3P <= angle && ANGLE_360 > angle) {
                        // 右
                        mOnShakeListener!!.direction(Direction.DIRECTION_RIGHT)
                    } else if (ANGLE_ROTATE45_4D_OF_0P <= angle && ANGLE_ROTATE45_4D_OF_1P > angle) {
                        // 下
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN)
                    } else if (ANGLE_ROTATE45_4D_OF_1P <= angle && ANGLE_ROTATE45_4D_OF_2P > angle) {
                        // 左
                        mOnShakeListener!!.direction(Direction.DIRECTION_LEFT)
                    } else if (ANGLE_ROTATE45_4D_OF_2P <= angle && ANGLE_ROTATE45_4D_OF_3P > angle) {
                        // 上
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP)
                    }

                    DirectionMode.DIRECTION_8 -> if (ANGLE_0 <= angle && ANGLE_8D_OF_0P > angle || ANGLE_8D_OF_7P <= angle && ANGLE_360 > angle) {
                        // 右
                        mOnShakeListener!!.direction(Direction.DIRECTION_RIGHT)
                    } else if (ANGLE_8D_OF_0P <= angle && ANGLE_8D_OF_1P > angle) {
                        // 右下
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN_RIGHT)
                    } else if (ANGLE_8D_OF_1P <= angle && ANGLE_8D_OF_2P > angle) {
                        // 下
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN)
                    } else if (ANGLE_8D_OF_2P <= angle && ANGLE_8D_OF_3P > angle) {
                        // 左下
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN_LEFT)
                    } else if (ANGLE_8D_OF_3P <= angle && ANGLE_8D_OF_4P > angle) {
                        // 左
                        mOnShakeListener!!.direction(Direction.DIRECTION_LEFT)
                    } else if (ANGLE_8D_OF_4P <= angle && ANGLE_8D_OF_5P > angle) {
                        // 左上
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP_LEFT)
                    } else if (ANGLE_8D_OF_5P <= angle && ANGLE_8D_OF_6P > angle) {
                        // 上
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP)
                    } else if (ANGLE_8D_OF_6P <= angle && ANGLE_8D_OF_7P > angle) {
                        // 右上
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP_RIGHT)
                    }

                    else -> {}
                }
            } else if (CallBackMode.CALL_BACK_MODE_STATE_CHANGE == mCallBackMode) {
                when (mDirectionMode) {
                    DirectionMode.DIRECTION_2_HORIZONTAL -> if ((ANGLE_0 <= angle && ANGLE_HORIZONTAL_2D_OF_0P > angle || ANGLE_HORIZONTAL_2D_OF_1P <= angle && ANGLE_360 > angle) && tempDirection != Direction.DIRECTION_RIGHT) {
                        // 右
                        tempDirection = Direction.DIRECTION_RIGHT
                        mOnShakeListener!!.direction(Direction.DIRECTION_RIGHT)
                    } else if (ANGLE_HORIZONTAL_2D_OF_0P <= angle && ANGLE_HORIZONTAL_2D_OF_1P > angle && tempDirection != Direction.DIRECTION_LEFT) {
                        // 左
                        tempDirection = Direction.DIRECTION_LEFT
                        mOnShakeListener!!.direction(Direction.DIRECTION_LEFT)
                    }

                    DirectionMode.DIRECTION_2_VERTICAL -> if (ANGLE_VERTICAL_2D_OF_0P <= angle && ANGLE_VERTICAL_2D_OF_1P > angle && tempDirection != Direction.DIRECTION_DOWN) {
                        // 下
                        tempDirection = Direction.DIRECTION_DOWN
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN)
                    } else if (ANGLE_VERTICAL_2D_OF_1P <= angle && ANGLE_360 > angle && tempDirection != Direction.DIRECTION_UP) {
                        // 上
                        tempDirection = Direction.DIRECTION_UP
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP)
                    }

                    DirectionMode.DIRECTION_4_ROTATE_0 -> if (ANGLE_4D_OF_0P <= angle && ANGLE_4D_OF_1P > angle && tempDirection != Direction.DIRECTION_DOWN_RIGHT) {
                        // 右下
                        tempDirection = Direction.DIRECTION_DOWN_RIGHT
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN_RIGHT)
                    } else if (ANGLE_4D_OF_1P <= angle && ANGLE_4D_OF_2P > angle && tempDirection != Direction.DIRECTION_DOWN_LEFT) {
                        // 左下
                        tempDirection = Direction.DIRECTION_DOWN_LEFT
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN_LEFT)
                    } else if (ANGLE_4D_OF_2P <= angle && ANGLE_4D_OF_3P > angle && tempDirection != Direction.DIRECTION_UP_LEFT) {
                        // 左上
                        tempDirection = Direction.DIRECTION_UP_LEFT
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP_LEFT)
                    } else if (ANGLE_4D_OF_3P <= angle && ANGLE_360 > angle && tempDirection != Direction.DIRECTION_UP_RIGHT) {
                        // 右上
                        tempDirection = Direction.DIRECTION_UP_RIGHT
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP_RIGHT)
                    }

                    DirectionMode.DIRECTION_4_ROTATE_45 -> if ((ANGLE_0 <= angle && ANGLE_ROTATE45_4D_OF_0P > angle || ANGLE_ROTATE45_4D_OF_3P <= angle && ANGLE_360 > angle) && tempDirection != Direction.DIRECTION_RIGHT) {
                        // 右
                        tempDirection = Direction.DIRECTION_RIGHT
                        mOnShakeListener!!.direction(Direction.DIRECTION_RIGHT)
                    } else if (ANGLE_ROTATE45_4D_OF_0P <= angle && ANGLE_ROTATE45_4D_OF_1P > angle && tempDirection != Direction.DIRECTION_DOWN) {
                        // 下
                        tempDirection = Direction.DIRECTION_DOWN
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN)
                    } else if (ANGLE_ROTATE45_4D_OF_1P <= angle && ANGLE_ROTATE45_4D_OF_2P > angle && tempDirection != Direction.DIRECTION_LEFT) {
                        // 左
                        tempDirection = Direction.DIRECTION_LEFT
                        mOnShakeListener!!.direction(Direction.DIRECTION_LEFT)
                    } else if (ANGLE_ROTATE45_4D_OF_2P <= angle && ANGLE_ROTATE45_4D_OF_3P > angle && tempDirection != Direction.DIRECTION_UP) {
                        // 上
                        tempDirection = Direction.DIRECTION_UP
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP)
                    }

                    DirectionMode.DIRECTION_8 -> if ((ANGLE_0 <= angle && ANGLE_8D_OF_0P > angle || ANGLE_8D_OF_7P <= angle && ANGLE_360 > angle) && tempDirection != Direction.DIRECTION_RIGHT) {
                        // 右
                        tempDirection = Direction.DIRECTION_RIGHT
                        mOnShakeListener!!.direction(Direction.DIRECTION_RIGHT)
                    } else if (ANGLE_8D_OF_0P <= angle && ANGLE_8D_OF_1P > angle && tempDirection != Direction.DIRECTION_DOWN_RIGHT) {
                        // 右下
                        tempDirection = Direction.DIRECTION_DOWN_RIGHT
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN_RIGHT)
                    } else if (ANGLE_8D_OF_1P <= angle && ANGLE_8D_OF_2P > angle && tempDirection != Direction.DIRECTION_DOWN) {
                        // 下
                        tempDirection = Direction.DIRECTION_DOWN
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN)
                    } else if (ANGLE_8D_OF_2P <= angle && ANGLE_8D_OF_3P > angle && tempDirection != Direction.DIRECTION_DOWN_LEFT) {
                        // 左下
                        tempDirection = Direction.DIRECTION_DOWN_LEFT
                        mOnShakeListener!!.direction(Direction.DIRECTION_DOWN_LEFT)
                    } else if (ANGLE_8D_OF_3P <= angle && ANGLE_8D_OF_4P > angle && tempDirection != Direction.DIRECTION_LEFT) {
                        // 左
                        tempDirection = Direction.DIRECTION_LEFT
                        mOnShakeListener!!.direction(Direction.DIRECTION_LEFT)
                    } else if (ANGLE_8D_OF_4P <= angle && ANGLE_8D_OF_5P > angle && tempDirection != Direction.DIRECTION_UP_LEFT) {
                        // 左上
                        tempDirection = Direction.DIRECTION_UP_LEFT
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP_LEFT)
                    } else if (ANGLE_8D_OF_5P <= angle && ANGLE_8D_OF_6P > angle && tempDirection != Direction.DIRECTION_UP) {
                        // 上
                        tempDirection = Direction.DIRECTION_UP
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP)
                    } else if (ANGLE_8D_OF_6P <= angle && ANGLE_8D_OF_7P > angle && tempDirection != Direction.DIRECTION_UP_RIGHT) {
                        // 右上
                        tempDirection = Direction.DIRECTION_UP_RIGHT
                        mOnShakeListener!!.direction(Direction.DIRECTION_UP_RIGHT)
                    }

                    else -> {}
                }
            }
        }
    }

    /**
     * 回调
     * 结束
     */
    private fun callBackFinish() {
        tempDirection = Direction.DIRECTION_CENTER
        if (null != mOnAngleChangeListener) {
            mOnAngleChangeListener!!.onFinish()
        }
        if (null != mOnShakeListener) {
            mOnShakeListener!!.onFinish()
        }
    }

    /**
     * 回调模式
     */
    enum class CallBackMode {
        // 有移动就立刻回调
        CALL_BACK_MODE_MOVE,  // 只有状态变化的时候才回调
        CALL_BACK_MODE_STATE_CHANGE
    }

    /**
     * 设置回调模式
     *
     * @param mode 回调模式
     */
    fun setCallBackMode(mode: CallBackMode) {
        mCallBackMode = mode
    }

    /**
     * 摇杆支持几个方向
     */
    enum class DirectionMode {
        DIRECTION_2_HORIZONTAL,  // 横向 左右两个方向
        DIRECTION_2_VERTICAL,  // 纵向 上下两个方向
        DIRECTION_4_ROTATE_0,  // 四个方向
        DIRECTION_4_ROTATE_45,  // 四个方向 旋转45度
        DIRECTION_8 // 八个方向
    }

    /**
     * 方向
     */
    enum class Direction {
        DIRECTION_LEFT,  // 左
        DIRECTION_RIGHT,  // 右
        DIRECTION_UP,  // 上
        DIRECTION_DOWN,  // 下
        DIRECTION_UP_LEFT,  // 左上
        DIRECTION_UP_RIGHT,  // 右上
        DIRECTION_DOWN_LEFT,  // 左下
        DIRECTION_DOWN_RIGHT,  // 右下
        DIRECTION_CENTER // 中间
    }

    /**
     * 添加摇杆摇动角度的监听
     *
     * @param listener 回调接口
     */
    fun setOnAngleChangeListener(listener: OnAngleChangeListener?) {
        mOnAngleChangeListener = listener
    }

    /**
     * 添加摇动的监听
     *
     * @param directionMode 监听的方向
     * @param listener      回调
     */
    fun setOnShakeListener(directionMode: DirectionMode?, listener: OnShakeListener?) {
        mDirectionMode = directionMode
        mOnShakeListener = listener
    }

    /**
     * 摇动方向监听接口
     */
    interface OnShakeListener {
        // 开始
        fun onStart()

        /**
         * 摇动方向
         *
         * @param direction 方向
         */
        fun direction(direction: Direction?)

        // 结束
        fun onFinish()
    }

    /**
     * 摇动角度的监听接口
     */
    interface OnAngleChangeListener {
        // 开始
        fun onStart()

        /**
         * 摇杆角度变化
         *
         * @param angle 角度[0,360)
         */
        fun angle(angle: Double)

        // 结束
        fun onFinish()
    }

    companion object {
        private const val DEFAULT_SIZE = 400
        private const val DEFAULT_ROCKER_RADIUS = DEFAULT_SIZE / 8

        // 角度
        private const val ANGLE_0 = 0.0
        private const val ANGLE_360 = 360.0

        // 360°水平方向平分2份的边缘角度
        private const val ANGLE_HORIZONTAL_2D_OF_0P = 90.0
        private const val ANGLE_HORIZONTAL_2D_OF_1P = 270.0

        // 360°垂直方向平分2份的边缘角度
        private const val ANGLE_VERTICAL_2D_OF_0P = 0.0
        private const val ANGLE_VERTICAL_2D_OF_1P = 180.0

        // 360°平分4份的边缘角度
        private const val ANGLE_4D_OF_0P = 0.0
        private const val ANGLE_4D_OF_1P = 90.0
        private const val ANGLE_4D_OF_2P = 180.0
        private const val ANGLE_4D_OF_3P = 270.0

        // 360°平分4份的边缘角度(旋转45度)
        private const val ANGLE_ROTATE45_4D_OF_0P = 45.0
        private const val ANGLE_ROTATE45_4D_OF_1P = 135.0
        private const val ANGLE_ROTATE45_4D_OF_2P = 225.0
        private const val ANGLE_ROTATE45_4D_OF_3P = 315.0

        // 360°平分8份的边缘角度
        private const val ANGLE_8D_OF_0P = 22.5
        private const val ANGLE_8D_OF_1P = 67.5
        private const val ANGLE_8D_OF_2P = 112.5
        private const val ANGLE_8D_OF_3P = 157.5
        private const val ANGLE_8D_OF_4P = 202.5
        private const val ANGLE_8D_OF_5P = 247.5
        private const val ANGLE_8D_OF_6P = 292.5
        private const val ANGLE_8D_OF_7P = 337.5

        // 摇杆可移动区域背景
        private const val AREA_BACKGROUND_MODE_PIC = 0
        private const val AREA_BACKGROUND_MODE_COLOR = 1
        private const val AREA_BACKGROUND_MODE_XML = 2
        private const val AREA_BACKGROUND_MODE_DEFAULT = 3

        // 摇杆背景
        private const val ROCKER_BACKGROUND_MODE_PIC = 4
        private const val ROCKER_BACKGROUND_MODE_COLOR = 5
        private const val ROCKER_BACKGROUND_MODE_XML = 6
        private const val ROCKER_BACKGROUND_MODE_DEFAULT = 7
    }
}