package de.kruemelopment.org.drckmichspiel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BackgroundView : View {
    private var paint: Paint
    private var viewheight = 0
    private var viewwidth = 0

    constructor(context: Context?) : super(context) {
        paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
    }

    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet) {
        paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        viewheight = this.measuredHeight
        viewwidth = this.measuredWidth
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, viewwidth.toFloat(), viewheight.toFloat(), paint)
        paint.color = Color.parseColor("#000000")
        val size = viewheight / 60f
        val strichdicke = viewheight / 600f
        run {
            var o = size
            while (o < viewheight) {
                canvas.drawRect(0f, o, viewwidth.toFloat(), o + strichdicke, paint)
                o += size + strichdicke
            }
        }
        var o = size
        while (o < viewwidth) {
            canvas.drawRect(o, 0f, o + strichdicke, viewheight.toFloat(), paint)
            o += size + strichdicke
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, height)
    }
}