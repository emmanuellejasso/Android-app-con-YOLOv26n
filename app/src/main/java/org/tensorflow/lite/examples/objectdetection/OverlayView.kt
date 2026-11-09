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
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetection
import java.util.LinkedList
import java.util.Locale
import kotlin.math.max

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<ObjectDetection> = LinkedList<ObjectDetection>()
    private var personDetections: List<PersonDetection> = LinkedList<PersonDetection>()

    var isPoseEstimationEnabled: Boolean = false
    var poseLandmarkerResult: PoseLandmarkerResult? = null

    private var boxPaint = Paint()
    private var boxFillPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var subTextPaint = Paint()
    private var skeletonPaint = Paint()
    private var jointPaint = Paint()
    private var headJointPaint = Paint()
    private var armJointPaint = Paint()
    private var waistLinePaint = Paint()

    private var scaleFactor: Float = 1f
    private var bounds = Rect()
    private var subBounds = Rect()

    private var currentImageHeight: Int = 1
    private var currentImageWidth: Int = 1
    private var rotatedWidth: Int = 1
    private var rotatedHeight: Int = 1

    companion object {
        // Conexiones de los 33 puntos clave (landmarks) de MediaPipe Pose
        private val POSE_CONNECTIONS = listOf(
            Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(0, 4), Pair(4, 5), Pair(5, 6), Pair(9, 10), // Rostro / Cabeza
            Pair(11, 12), // Hombros
            Pair(11, 13), Pair(13, 15), Pair(15, 17), Pair(15, 19), Pair(15, 21), // Brazo Izquierdo y Mano
            Pair(12, 14), Pair(14, 16), Pair(16, 18), Pair(16, 20), Pair(16, 22), // Brazo Derecho y Mano
            Pair(11, 23), Pair(12, 24), // Torso
            Pair(23, 24), // Cintura / Caderas (38% línea de cintura)
            Pair(23, 25), Pair(25, 27), Pair(27, 29), Pair(29, 31), // Pierna Izquierda
            Pair(24, 26), Pair(26, 28), Pair(28, 30), Pair(30, 32)  // Pierna Derecha
        )
    }

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
        jointPaint.reset()
        headJointPaint.reset()
        armJointPaint.reset()
        waistLinePaint.reset()
        poseLandmarkerResult = null
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

        // Esqueleto MediaPipe Pose (Líneas)
        skeletonPaint.color = Color.parseColor("#00FF66") // Verde neón
        skeletonPaint.strokeWidth = 7f
        skeletonPaint.style = Paint.Style.STROKE
        skeletonPaint.isAntiAlias = true

        // Articulaciones / Puntos Clave generales (Caderas, Rodillas, Tobillos)
        jointPaint.color = Color.parseColor("#FF0055") // Magenta neón
        jointPaint.style = Paint.Style.FILL
        jointPaint.isAntiAlias = true

        // Puntos Clave de Cabeza y Rostro (Cian Neón)
        headJointPaint.color = Color.parseColor("#00E5FF")
        headJointPaint.style = Paint.Style.FILL
        headJointPaint.isAntiAlias = true

        // Puntos Clave de Hombros, Codos, Muñecas y Manos (Amarillo/Verde Neón)
        armJointPaint.color = Color.parseColor("#FFD700")
        armJointPaint.style = Paint.Style.FILL
        armJointPaint.isAntiAlias = true

        // Línea de Cintura (Landmarks 23-24)
        waistLinePaint.color = Color.parseColor("#FFD700") // Dorado
        waistLinePaint.strokeWidth = 6f
        waistLinePaint.style = Paint.Style.STROKE
        waistLinePaint.pathEffect = DashPathEffect(floatArrayOf(12f, 12f), 0f)
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

            canvas.drawRoundRect(drawableRect, 16f, 16f, boxFillPaint)
            canvas.drawRoundRect(drawableRect, 16f, 16f, boxPaint)

            // Si la estimación de pose está habilitada, renderizar MediaPipe Pose Landmarks
            if (isPoseEstimationEnabled) {
                if (poseLandmarkerResult != null && poseLandmarkerResult!!.landmarks().isNotEmpty()) {
                    drawMediaPipeLandmarks(canvas)
                } else {
                    drawPoseSkeletonFallback(canvas, left, top, widthPx, heightPx)
                }
            }

            val confidencePct = (personDet.originalDetection.category.confidence * 100).toInt()
            val line1Text = "👤 Persona ($confidencePct%)"
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

    private fun drawMediaPipeLandmarks(canvas: Canvas) {
        val landmarksList = poseLandmarkerResult?.landmarks() ?: return

        for (poseLandmarks in landmarksList) {
            // Dibujar conexiones entre puntos clave
            for (connection in POSE_CONNECTIONS) {
                val startIdx = connection.first
                val endIdx = connection.second

                if (startIdx < poseLandmarks.size && endIdx < poseLandmarks.size) {
                    val startLm = poseLandmarks[startIdx]
                    val endLm = poseLandmarks[endIdx]

                    val startX = startLm.x() * rotatedWidth * scaleFactor
                    val startY = startLm.y() * rotatedHeight * scaleFactor
                    val endX = endLm.x() * rotatedWidth * scaleFactor
                    val endY = endLm.y() * rotatedHeight * scaleFactor

                    val paintToUse = if (startIdx == 23 && endIdx == 24) waistLinePaint else skeletonPaint
                    canvas.drawLine(startX, startY, endX, endY, paintToUse)
                }
            }

            // Dibujar puntos clave (Landmarks) para cabeza, hombros, codos, manos, cadera y piernas
            for ((idx, lm) in poseLandmarks.withIndex()) {
                val x = lm.x() * rotatedWidth * scaleFactor
                val y = lm.y() * rotatedHeight * scaleFactor

                val paint = when (idx) {
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> headJointPaint // Cabeza / Rostro (Cian Neón)
                    11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22 -> armJointPaint // Hombros, Codos, Muñecas y Manos (Amarillo/Verde)
                    else -> jointPaint // Caderas, Rodillas, Tobillos (Magenta Neón)
                }
                val radius = if (idx in 15..22) 13f else 10f // Manos y dedos resaltados

                canvas.drawCircle(x, y, radius, paint)
            }
        }
    }

    private fun drawPoseSkeletonFallback(
        canvas: Canvas,
        left: Float,
        top: Float,
        widthPx: Float,
        heightPx: Float
    ) {
        val centerX = left + widthPx / 2f

        val headY = top + heightPx * 0.08f
        val neckY = top + heightPx * 0.18f
        val shoulderLeftX = left + widthPx * 0.25f
        val shoulderRightX = left + widthPx * 0.75f

        val waistY = top + heightPx * 0.38f // Línea de Cintura (38%)
        val hipLeftX = left + widthPx * 0.35f
        val hipRightX = left + widthPx * 0.65f

        val kneeY = top + heightPx * 0.70f
        val ankleY = top + heightPx * 0.92f

        canvas.drawCircle(centerX, headY, 12f, headJointPaint)

        canvas.drawLine(centerX, headY + 12f, centerX, neckY, skeletonPaint)
        canvas.drawLine(centerX, neckY, centerX, waistY, skeletonPaint)

        canvas.drawLine(shoulderLeftX, neckY, shoulderRightX, neckY, skeletonPaint)
        canvas.drawCircle(shoulderLeftX, neckY, 10f, armJointPaint)
        canvas.drawCircle(shoulderRightX, neckY, 10f, armJointPaint)

        // Codos y Manos
        val elbowLeftX = left + widthPx * 0.18f
        val elbowRightX = left + widthPx * 0.82f
        val handLeftX = left + widthPx * 0.12f
        val handRightX = left + widthPx * 0.88f
        val armY = top + heightPx * 0.42f

        canvas.drawLine(shoulderLeftX, neckY, elbowLeftX, armY, skeletonPaint)
        canvas.drawLine(shoulderRightX, neckY, elbowRightX, armY, skeletonPaint)
        canvas.drawCircle(elbowLeftX, armY, 10f, armJointPaint)
        canvas.drawCircle(elbowRightX, armY, 10f, armJointPaint)

        canvas.drawLine(elbowLeftX, armY, handLeftX, armY + 20f, skeletonPaint)
        canvas.drawLine(elbowRightX, armY, handRightX, armY + 20f, skeletonPaint)
        canvas.drawCircle(handLeftX, armY + 20f, 12f, armJointPaint)
        canvas.drawCircle(handRightX, armY + 20f, 12f, armJointPaint)

        canvas.drawLine(left, waistY, left + widthPx, waistY, waistLinePaint)
        canvas.drawCircle(hipLeftX, waistY, 10f, jointPaint)
        canvas.drawCircle(hipRightX, waistY, 10f, jointPaint)

        canvas.drawLine(hipLeftX, waistY, left + widthPx * 0.35f, kneeY, skeletonPaint)
        canvas.drawLine(hipRightX, waistY, left + widthPx * 0.65f, kneeY, skeletonPaint)
        canvas.drawCircle(left + widthPx * 0.35f, kneeY, 10f, jointPaint)
        canvas.drawCircle(left + widthPx * 0.65f, kneeY, 10f, jointPaint)

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
        imageWidth: Int,
        imageRotation: Int = 90
    ) {
        val personOnlyResults = detectionResults.filter {
            PersonDistanceEstimator.isPerson(it.category.label)
        }

        results = personOnlyResults
        currentImageHeight = imageHeight
        currentImageWidth = imageWidth

        if (imageRotation == 90 || imageRotation == 270) {
            rotatedWidth = imageHeight
            rotatedHeight = imageWidth
        } else {
            rotatedWidth = imageWidth
            rotatedHeight = imageHeight
        }

        personDetections = personOnlyResults
            .map { PersonDistanceEstimator.processDetection(it, rotatedHeight) }

        scaleFactor = max(width * 1f / rotatedWidth, height * 1f / rotatedHeight)
    }
}
