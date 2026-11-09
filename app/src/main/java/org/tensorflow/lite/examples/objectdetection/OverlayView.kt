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

    // Estimación de pose con MediaPipe
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
    private var currentImageRotation: Int = 90

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

        subTextPaint.color = Color.parseColor("#FFD700") // Amarillo neón para la distancia
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

            canvas.drawRoundRect(drawableRect, 16f, 16f, boxFillPaint)
            canvas.drawRoundRect(drawableRect, 16f, 16f, boxPaint)

            // Si la estimación de pose está habilitada, renderizar la pose ÚNICAMENTE que corresponda a esta persona ajustada dentro de rect
            if (isPoseEstimationEnabled) {
                drawPoseForPerson(canvas, drawableRect)
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

    private fun drawPoseForPerson(canvas: Canvas, rect: RectF) {
        val landmarksList = poseLandmarkerResult?.landmarks()
        if (landmarksList.isNullOrEmpty()) return

        // Emparejar el esqueleto de MediaPipe que le pertenece ÚNICAMENTE a esta persona (cuya cabeza/cadera cae dentro de su caja)
        val matchedPose = landmarksList.find { poseLandmarks ->
            if (poseLandmarks.size > 24) {
                val nose = poseLandmarks[0]
                val leftHip = poseLandmarks[23]
                val rightHip = poseLandmarks[24]

                val noseX = (1.0f - nose.y()) * width
                val noseY = nose.x() * height

                val hipX = (1.0f - (leftHip.y() + rightHip.y()) / 2f) * width
                val hipY = ((leftHip.x() + rightHip.x()) / 2f) * height

                val margin = 80f
                (noseX in (rect.left - margin)..(rect.right + margin) && noseY in (rect.top - margin)..(rect.bottom + margin)) ||
                (hipX in (rect.left - margin)..(rect.right + margin) && hipY in (rect.top - margin)..(rect.bottom + margin))
            } else {
                false
            }
        } ?: return

        // Calcular los límites del esqueleto detectado
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE

        val rawPoints = ArrayList<Pair<Float, Float>>(matchedPose.size)
        for (lm in matchedPose) {
            val rx = (1.0f - lm.y()) * width
            val ry = lm.x() * height
            rawPoints.add(Pair(rx, ry))

            if (rx < minX) minX = rx
            if (rx > maxX) maxX = rx
            if (ry < minY) minY = ry
            if (ry > maxY) maxY = ry
        }

        val poseW = max(1f, maxX - minX)
        val poseH = max(1f, maxY - minY)

        // Definir el área objetivo con un margen interno (5%) dentro del recuadro azul (rect)
        val paddingH = rect.width() * 0.05f
        val paddingV = rect.height() * 0.05f
        val targetLeft = rect.left + paddingH
        val targetTop = rect.top + paddingV
        val targetW = rect.width() - paddingH * 2f
        val targetH = rect.height() - paddingV * 2f

        fun fitPoint(idx: Int): Pair<Float, Float> {
            val (rx, ry) = rawPoints[idx]
            val relX = (rx - minX) / poseW
            val relY = (ry - minY) / poseH
            val fitX = targetLeft + relX * targetW
            val fitY = targetTop + relY * targetH
            return Pair(fitX, fitY)
        }

        for (connection in POSE_CONNECTIONS) {
            val startIdx = connection.first
            val endIdx = connection.second

            if (startIdx < rawPoints.size && endIdx < rawPoints.size) {
                val (startX, startY) = fitPoint(startIdx)
                val (endX, endY) = fitPoint(endIdx)

                val paintToUse = if (startIdx == 23 && endIdx == 24) waistLinePaint else skeletonPaint
                canvas.drawLine(startX, startY, endX, endY, paintToUse)
            }
        }

        for (idx in rawPoints.indices) {
            val (x, y) = fitPoint(idx)

            val paint = when (idx) {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> headJointPaint // Cabeza / Rostro (Cian Neón)
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22 -> armJointPaint // Hombros, Codos, Muñecas y Manos (Amarillo)
                else -> jointPaint // Caderas, Rodillas, Tobillos (Magenta Neón)
            }
            val radius = if (idx in 15..22) 13f else 10f

            canvas.drawCircle(x, y, radius, paint)
        }
    }

    fun setResults(
        detectionResults: List<ObjectDetection>,
        imageHeight: Int,
        imageWidth: Int,
        imageRotation: Int = 90
    ) {
        // Filtrar estricta y únicamente detecciones de tipo Persona
        val personOnlyResults = detectionResults.filter {
            PersonDistanceEstimatorHelper.isPerson(it.category.label)
        }

        results = personOnlyResults
        currentImageHeight = imageHeight
        currentImageWidth = imageWidth
        currentImageRotation = if (imageRotation == 0) 90 else imageRotation

        personDetections = personOnlyResults
            .map { PersonDistanceEstimatorHelper.processDetection(it, imageHeight) }

        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }
}
