/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetection
import java.util.LinkedList
import java.util.Locale
import kotlin.math.max

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<ObjectDetection> = LinkedList<ObjectDetection>()
    private var boxPaint = Paint()
    private var boxFillPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        boxFillPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        // Modern Pill Background for Label
        textBackgroundPaint.color = Color.parseColor("#E61A202C")
        textBackgroundPaint.style = Paint.Style.FILL

        // Modern White Bold Text
        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 42f
        textPaint.isFakeBoldText = true

        // Modern Neon Cyan Bounding Box Stroke
        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 6f
        boxPaint.style = Paint.Style.STROKE
        boxPaint.isAntiAlias = true

        // Semi-transparent box fill
        boxFillPaint.color = Color.parseColor("#2000E5FF")
        boxFillPaint.style = Paint.Style.FILL
        boxFillPaint.isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        for (result in results) {
            val boundingBox = result.boundingBox

            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            val drawableRect = RectF(left, top, right, bottom)

            // Draw semi-transparent box fill and rounded neon stroke
            canvas.drawRoundRect(drawableRect, 16f, 16f, boxFillPaint)
            canvas.drawRoundRect(drawableRect, 16f, 16f, boxPaint)

            // Label + Confidence percentage
            val confidencePct = (result.category.confidence * 100).toInt()
            val drawableText = "${result.category.label} ${confidencePct}%"

            textPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()

            val textPaddingHorizontal = 16f
            val textPaddingVertical = 12f

            val bgRect = RectF(
                left,
                top - textHeight - textPaddingVertical * 2,
                left + textWidth + textPaddingHorizontal * 2,
                top
            )

            // If badge goes off top of screen, show inside top of box
            if (bgRect.top < 0) {
                bgRect.top = top
                bgRect.bottom = top + textHeight + textPaddingVertical * 2
            }

            // Draw rounded pill background behind text
            canvas.drawRoundRect(bgRect, 12f, 12f, textBackgroundPaint)

            // Draw text centered inside pill badge
            canvas.drawText(
                drawableText,
                bgRect.left + textPaddingHorizontal,
                bgRect.bottom - textPaddingVertical,
                textPaint
            )
        }
    }

    fun setResults(
        detectionResults: List<ObjectDetection>,
        imageHeight: Int,
        imageWidth: Int
    ) {
        results = detectionResults

        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }
}
