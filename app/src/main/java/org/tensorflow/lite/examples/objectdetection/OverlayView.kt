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
import android.graphics.DashPathEffect
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
    private var personDetections: List<PersonDetection> = LinkedList<PersonDetection>()

    var isPoseEstimationEnabled: Boolean = false

    private var boxPaint = Paint()
    private var boxFillPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var subTextPaint = Paint()
    private var skeletonPaint = Paint()
    private var waistLinePaint = Paint()

    private var scaleFactor: Float = 1f
    private var bounds = Rect()
    private var subBounds = Rect()

    private var currentImageHeight: Int = 1
    private var currentImageWidth: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        textPaint.reset()
        subTextPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        boxFillPaint.reset()
        skeletonPaint.reset()
        waistLinePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.parseColor("#EE1A202C")
        textBackgroundPaint.style = Paint.Style.FILL

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 38f
        textPaint.isFakeBoldText = true

        subTextPaint.color = Color.parseColor("#FFD700") // Amarillo neón
        subTextPaint.style = Paint.Style.FILL
        subTextPaint.textSize = 34f
        subTextPaint.isFakeBoldText = true

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 6f
        boxPaint.style = Paint.Style.STROKE
        boxPaint.isAntiAlias = true

        boxFillPaint.color = Color.parseColor("#2000E5FF")
        boxFillPaint.style = Paint.Style.FILL
        boxFillPaint.isAntiAlias = true

        // Esqueleto para estimación de pose
        skeletonPaint.color = Color.parseColor("#00FF66") // Verde neón
        skeletonPaint.strokeWidth = 5f
        skeletonPaint.style = Paint.Style.STROKE
        skeletonPaint.isAntiAlias = true

        // Línea de cintura (38% desde la cabeza)
        waistLinePaint.color = Color.parseColor("#FFD700") // Dorado
        waistLinePaint.strokeWidth = 4f
        waistLinePaint.style = Paint.Style.STROKE
        waistLinePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        waistLinePaint.isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (personDetections.isNotEmpty()) {
            drawPersonDetections(canvas)
        } else {
            drawStandardDetections(canvas)
        }
    }

    private fun drawPersonDetections(canvas: Canvas) {
        val cyanColor = Color.parseColor("#00E5FF")
        boxPaint.color = cyanColor
        boxFillPaint.color = Color.parseColor("#2500E5FF")

        for (personDet in personDetections) {
            val boundingBox = personDet.originalDetection.boundingBox

            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            val drawableRect = RectF(left, top, right, bottom)
            val widthPx = right - left
            val heightPx = bottom - top

            // Dibujar caja delimitadora con bordes redondeados
            canvas.drawRoundRect(drawableRect, 16f, 16f, boxFillPaint)
            canvas.drawRoundRect(drawableRect, 16f, 16f, boxPaint)

            // Si la estimación de pose está habilitada, dibujar el esqueleto
            if (isPoseEstimationEnabled) {
                drawPoseSkeleton(canvas, left, top, widthPx, heightPx)
            }

            // Información de línea 1 (Persona + Confianza)
            val confidencePct = (personDet.originalDetection.category.confidence * 100).toInt()
            val line1Text = "👤 Persona ($confidencePct%)"

            // Información de línea 2 (Distancia estimada basada en el segmento cintura-cabeza 38%)
            val line2Text = String.format(Locale.US, "D: %.2f m", personDet.estimatedDistanceMeters)

            textPaint.getTextBounds(line1Text, 0, line1Text.length, bounds)
            subTextPaint.getTextBounds(line2Text, 0, line2Text.length, subBounds)

            val maxTextWidth = max(bounds.width(), subBounds.width()).toFloat()
            val totalTextHeight = bounds.height() + subBounds.height() + 16f

            val paddingH = 18f
            val paddingV = 14f

            val bgRect = RectF(
                left,
                top - totalTextHeight - paddingV * 2,
                left + maxTextWidth + paddingH * 2,
                top
            )

            if (bgRect.top < 0) {
                bgRect.top = top
                bgRect.bottom = top + totalTextHeight + paddingV * 2
            }

            canvas.drawRoundRect(bgRect, 14f, 14f, textBackgroundPaint)

            canvas.drawText(
                line1Text,
                bgRect.left + paddingH,
                bgRect.top + paddingV + bounds.height(),
                textPaint
            )

            canvas.drawText(
                line2Text,
                bgRect.left + paddingH,
                bgRect.bottom - paddingV,
                subTextPaint
            )
        }
    }

    private fun drawPoseSkeleton(
        canvas: Canvas,
        left: Float,
        top: Float,
        widthPx: Float,
        heightPx: Float
    ) {
        val centerX = left + widthPx / 2f

        // Puntos de referencia anatómicos
        val headY = top + heightPx * 0.08f
        val neckY = top + heightPx * 0.18f
        val shoulderLeftX = left + widthPx * 0.25f
        val shoulderRightX = left + widthPx * 0.75f

        val waistY = top + heightPx * 0.38f // Línea de Cintura / Cadera (38% del alto total)
        val hipLeftX = left + widthPx * 0.35f
        val hipRightX = left + widthPx * 0.65f

        val kneeY = top + heightPx * 0.70f
        val ankleY = top + heightPx * 0.92f

        // Dibujar nodo Cabeza
        canvas.drawCircle(centerX, headY, 12f, skeletonPaint)

        // Columna vertebral (Cabeza -> Cuello -> Cintura)
        canvas.drawLine(centerX, headY + 12f, centerX, neckY, skeletonPaint)
        canvas.drawLine(centerX, neckY, centerX, waistY, skeletonPaint)

        // Hombros
        canvas.drawLine(shoulderLeftX, neckY, shoulderRightX, neckY, skeletonPaint)
        canvas.drawCircle(shoulderLeftX, neckY, 8f, skeletonPaint)
        canvas.drawCircle(shoulderRightX, neckY, 8f, skeletonPaint)

        // Brazos
        canvas.drawLine(shoulderLeftX, neckY, left + widthPx * 0.15f, top + heightPx * 0.45f, skeletonPaint)
        canvas.drawLine(shoulderRightX, neckY, left + widthPx * 0.85f, top + heightPx * 0.45f, skeletonPaint)

        // Línea de Cintura (Segmento de medición del 38%)
        canvas.drawLine(left, waistY, left + widthPx, waistY, waistLinePaint)
        canvas.drawCircle(hipLeftX, waistY, 8f, skeletonPaint)
        canvas.drawCircle(hipRightX, waistY, 8f, skeletonPaint)

        // Piernas
        canvas.drawLine(hipLeftX, waistY, left + widthPx * 0.35f, kneeY, skeletonPaint)
        canvas.drawLine(hipRightX, waistY, left + widthPx * 0.65f, kneeY, skeletonPaint)
        canvas.drawCircle(left + widthPx * 0.35f, kneeY, 8f, skeletonPaint)
        canvas.drawCircle(left + widthPx * 0.65f, kneeY, 8f, skeletonPaint)

        canvas.drawLine(left + widthPx * 0.35f, kneeY, left + widthPx * 0.35f, ankleY, skeletonPaint)
        canvas.drawLine(left + widthPx * 0.65f, kneeY, left + widthPx * 0.65f, ankleY, skeletonPaint)
    }

    private fun drawStandardDetections(canvas: Canvas) {
        for (result in results) {
            val boundingBox = result.boundingBox

            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            val drawableRect = RectF(left, top, right, bottom)

            canvas.drawRoundRect(drawableRect, 16f, 16f, boxFillPaint)
            canvas.drawRoundRect(drawableRect, 16f, 16f, boxPaint)

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

            if (bgRect.top < 0) {
                bgRect.top = top
                bgRect.bottom = top + textHeight + textPaddingVertical * 2
            }

            canvas.drawRoundRect(bgRect, 12f, 12f, textBackgroundPaint)

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
        // Filtrar explícitamente para incluir ÚNICAMENTE personas
        val personOnlyResults = detectionResults.filter {
            PersonDistanceEstimator.isPerson(it.category.label)
        }

        results = personOnlyResults
        currentImageHeight = imageHeight
        currentImageWidth = imageWidth

        personDetections = personOnlyResults
            .map { PersonDistanceEstimator.processDetection(it, imageHeight) }

        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }
}
