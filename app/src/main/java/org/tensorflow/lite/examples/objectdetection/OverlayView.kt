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
    private var boxPaint = Paint()
    private var boxFillPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    // Estimación de pose con MediaPipe
    var isPoseEstimationEnabled: Boolean = false
    var poseLandmarkerResult: PoseLandmarkerResult? = null

    private var skeletonPaint = Paint()
    private var jointPaint = Paint()
    private var headJointPaint = Paint()
    private var armJointPaint = Paint()
    private var waistLinePaint = Paint()

    private var scaleFactor: Float = 1f
    private var bounds = Rect()

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

        // Esqueleto MediaPipe Pose (Líneas)
        skeletonPaint.color = Color.parseColor("#00FF66") // Verde neón
        skeletonPaint.strokeWidth = 7f
        skeletonPaint.style = Paint.Style.STROKE
        skeletonPaint.isAntiAlias = true

        // Puntos Clave generales
        jointPaint.color = Color.parseColor("#FF0055") // Magenta neón
        jointPaint.style = Paint.Style.FILL
        jointPaint.isAntiAlias = true

        // Puntos Clave de Cabeza y Rostro
        headJointPaint.color = Color.parseColor("#00E5FF") // Cian Neón
        headJointPaint.style = Paint.Style.FILL
        headJointPaint.isAntiAlias = true

        // Puntos Clave de Hombros, Codos, Muñecas y Manos
        armJointPaint.color = Color.parseColor("#FFD700") // Amarillo Neón
        armJointPaint.style = Paint.Style.FILL
        armJointPaint.isAntiAlias = true

        // Línea de Cintura (Landmarks 23-24)
        waistLinePaint.color = Color.parseColor("#FFD700")
        waistLinePaint.strokeWidth = 6f
        waistLinePaint.style = Paint.Style.STROKE
        waistLinePaint.pathEffect = DashPathEffect(floatArrayOf(12f, 12f), 0f)
        waistLinePaint.isAntiAlias = true
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

            // Si la estimación de pose está habilitada, mapear los puntos clave de MediaPipe exactamente DENTRO de drawableRect
            if (isPoseEstimationEnabled) {
                drawPoseInsideRect(canvas, drawableRect)
            }

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

    private fun drawPoseInsideRect(canvas: Canvas, rect: RectF) {
        val landmarksList = poseLandmarkerResult?.landmarks()
        if (landmarksList.isNullOrEmpty()) return

        val boxLeft = rect.left
        val boxTop = rect.top
        val boxWidth = rect.width()
        val boxHeight = rect.height()

        for (poseLandmarks in landmarksList) {
            // Dibujar conexiones entre puntos clave proyectados exactamente sobre la caja azul
            for (connection in POSE_CONNECTIONS) {
                val startIdx = connection.first
                val endIdx = connection.second

                if (startIdx < poseLandmarks.size && endIdx < poseLandmarks.size) {
                    val startLm = poseLandmarks[startIdx]
                    val endLm = poseLandmarks[endIdx]

                    val startX = boxLeft + startLm.x() * boxWidth
                    val startY = boxTop + startLm.y() * boxHeight
                    val endX = boxLeft + endLm.x() * boxWidth
                    val endY = boxTop + endLm.y() * boxHeight

                    val paintToUse = if (startIdx == 23 && endIdx == 24) waistLinePaint else skeletonPaint
                    canvas.drawLine(startX, startY, endX, endY, paintToUse)
                }
            }

            // Dibujar puntos clave (Landmarks) para cabeza, hombros, codos, manos, cadera y piernas
            for ((idx, lm) in poseLandmarks.withIndex()) {
                val x = boxLeft + lm.x() * boxWidth
                val y = boxTop + lm.y() * boxHeight

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

    fun setResults(
        detectionResults: List<ObjectDetection>,
        imageHeight: Int,
        imageWidth: Int
    ) {
        results = detectionResults

        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }
}
