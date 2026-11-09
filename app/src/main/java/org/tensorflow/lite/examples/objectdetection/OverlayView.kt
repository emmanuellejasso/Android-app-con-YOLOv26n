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
    private var personDetections: List<PersonDetection> = LinkedList<PersonDetection>()

    private var boxPaint = Paint()
    private var boxFillPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var subTextPaint = Paint()

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
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        // Fondo oscuro translúcido para insignias
        textBackgroundPaint.color = Color.parseColor("#EE1A202C")
        textBackgroundPaint.style = Paint.Style.FILL

        // Texto principal blanco en negrita
        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 38f
        textPaint.isFakeBoldText = true

        // Subtexto secundario (distancia)
        subTextPaint.color = Color.parseColor("#FFD700") // Amarillo oro neón
        subTextPaint.style = Paint.Style.FILL
        subTextPaint.textSize = 34f
        subTextPaint.isFakeBoldText = true

        // Trazo de cuadro delimitador
        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 6f
        boxPaint.style = Paint.Style.STROKE
        boxPaint.isAntiAlias = true

        // Relleno translúcido
        boxFillPaint.color = Color.parseColor("#2000E5FF")
        boxFillPaint.style = Paint.Style.FILL
        boxFillPaint.isAntiAlias = true
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

            // Dibujar caja delimitadora con bordes redondeados
            canvas.drawRoundRect(drawableRect, 16f, 16f, boxFillPaint)
            canvas.drawRoundRect(drawableRect, 16f, 16f, boxPaint)

            // Información de línea 1 (Persona + Confianza)
            val confidencePct = (personDet.originalDetection.category.confidence * 100).toInt()
            val line1Text = "👤 Persona ($confidencePct%)"

            // Información de línea 2 (Distancia estimada)
            val line2Text = String.format(Locale.US, "📏 Distancia: %.2f m (Talla ~1.65m)", personDet.estimatedDistanceMeters)

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

            // Ajuste si la etiqueta sobresale por la parte superior
            if (bgRect.top < 0) {
                bgRect.top = top
                bgRect.bottom = top + totalTextHeight + paddingV * 2
            }

            // Dibujar tarjeta oscura redondeada detrás del texto
            canvas.drawRoundRect(bgRect, 14f, 14f, textBackgroundPaint)

            // Dibujar línea 1 (Persona)
            canvas.drawText(
                line1Text,
                bgRect.left + paddingH,
                bgRect.top + paddingV + bounds.height(),
                textPaint
            )

            // Dibujar línea 2 (Distancia)
            canvas.drawText(
                line2Text,
                bgRect.left + paddingH,
                bgRect.bottom - paddingV,
                subTextPaint
            )
        }
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
        results = detectionResults
        currentImageHeight = imageHeight
        currentImageWidth = imageWidth

        // Procesar detecciones de personas con estimación de distancia
        personDetections = detectionResults
            .filter { PersonDistanceEstimator.isPerson(it.category.label) }
            .map { PersonDistanceEstimator.processDetection(it, imageHeight) }

        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }

    fun setPersonResults(
        personResults: List<PersonDetection>,
        imageHeight: Int,
        imageWidth: Int
    ) {
        personDetections = personResults
        currentImageHeight = imageHeight
        currentImageWidth = imageWidth

        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }
}
