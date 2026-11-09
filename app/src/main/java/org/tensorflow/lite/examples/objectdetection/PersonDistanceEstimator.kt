package org.tensorflow.lite.examples.objectdetection

import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetection
import kotlin.math.abs
import kotlin.math.max

/**
 * Estimador de distancia adaptativo para personas:
 * - Si se detecta Pose (MediaPipe Landmarks): Mide el segmento real entre cadera y cabeza (38% = 0.627m).
 * - Si NO se detecta Pose: Usa la caja delimitadora de la persona y la estatura promedio estándar (1.65m).
 */
data class PersonDetection(
    val originalDetection: ObjectDetection,
    val estimatedDistanceMeters: Float,
    val isPoseBasedDistance: Boolean
)

class PersonDistanceEstimator {

    companion object {
        private const val FOCAL_FACTOR = 1.15f
        private const val TOTAL_HEIGHT_METERS = 1.65f     // Estatura promedio total (1.65m)
        private const val WAIST_HEAD_RATIO = 0.38f       // 38% de la altura total (Cintura a Cabeza)
        private const val WAIST_HEAD_HEIGHT_METERS = TOTAL_HEIGHT_METERS * WAIST_HEAD_RATIO // 0.627m

        /**
         * Determina si la etiqueta corresponde a una persona.
         */
        fun isPerson(label: String): Boolean {
            val lowercaseLabel = label.lowercase()
            return lowercaseLabel == "persona" || lowercaseLabel == "person"
        }

        /**
         * Calcula la distancia estimada adaptativamente.
         */
        fun processDetection(
            detection: ObjectDetection,
            imageHeight: Int,
            imageWidth: Int,
            poseResult: PoseLandmarkerResult? = null
        ): PersonDetection {
            val bbox = detection.boundingBox
            var poseWaistHeadPx = -1f

            // Intentar extraer el segmento cadera-cabeza directamente de los landmarks de MediaPipe
            if (poseResult != null && poseResult.landmarks().isNotEmpty()) {
                val bboxNormLeft = bbox.left / imageWidth
                val bboxNormRight = bbox.right / imageWidth
                val bboxNormTop = bbox.top / imageHeight
                val bboxNormBottom = bbox.bottom / imageHeight

                for (poseLandmarks in poseResult.landmarks()) {
                    if (poseLandmarks.size > 24) {
                        val nose = poseLandmarks[0]
                        val leftHip = poseLandmarks[23]
                        val rightHip = poseLandmarks[24]

                        val hipX = (leftHip.x() + rightHip.x()) / 2f
                        val hipY = (leftHip.y() + rightHip.y()) / 2f

                        // Verificar si los puntos clave pertenecen a esta persona
                        if (hipX in (bboxNormLeft - 0.20f)..(bboxNormRight + 0.20f) &&
                            hipY in (bboxNormTop - 0.20f)..(bboxNormBottom + 0.20f)) {

                            val waistHeadNorm = abs(hipY - nose.y())
                            val pxSeg = waistHeadNorm * imageHeight
                            if (pxSeg > 10f) {
                                poseWaistHeadPx = pxSeg
                                break
                            }
                        }
                    }
                }
            }

            return if (poseWaistHeadPx > 0f) {
                // CASO A: Pose detectada -> Usar segmento cadera-cabeza (0.627m)
                val normalizedSegment = poseWaistHeadPx / imageHeight
                val distance = (FOCAL_FACTOR * WAIST_HEAD_HEIGHT_METERS) / normalizedSegment
                PersonDetection(
                    originalDetection = detection,
                    estimatedDistanceMeters = distance,
                    isPoseBasedDistance = true
                )
            } else {
                // CASO B: Sin Pose -> Usar estatura promedio total (1.65m) sobre la caja
                val bboxHeightPx = max(1f, bbox.height())
                val normalizedHeight = bboxHeightPx / imageHeight
                val distance = (FOCAL_FACTOR * TOTAL_HEIGHT_METERS) / normalizedHeight
                PersonDetection(
                    originalDetection = detection,
                    estimatedDistanceMeters = distance,
                    isPoseBasedDistance = false
                )
            }
        }
    }
}
