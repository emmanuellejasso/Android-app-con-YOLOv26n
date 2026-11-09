package org.tensorflow.lite.examples.objectdetection

import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetection
import kotlin.math.max

/**
 * Estimador de distancia para personas detectadas con estatura promedio estándar de 1.65 m.
 */
data class PersonDetection(
    val originalDetection: ObjectDetection,
    val estimatedDistanceMeters: Float
)

class PersonDistanceEstimator {

    companion object {
        // Factor de escala focal vertical para la cámara del dispositivo
        private const val FOCAL_FACTOR = 1.15f
        private const val DEFAULT_HEIGHT_METERS = 1.65f // Estatura promedio 1.65 m

        /**
         * Determina si la etiqueta corresponde a una persona.
         */
        fun isPerson(label: String): Boolean {
            val lowercaseLabel = label.lowercase()
            return lowercaseLabel == "persona" || lowercaseLabel == "person"
        }

        /**
         * Calcula la distancia estimada en metros a partir de la altura del bounding box en la imagen.
         * Distancia D = (f_y * H_real) / h_bbox_px
         */
        fun processDetection(
            detection: ObjectDetection,
            imageHeight: Int
        ): PersonDetection {
            val bboxHeightPx = max(1f, detection.boundingBox.height())
            val normalizedHeight = bboxHeightPx / imageHeight

            // Fórmula de distancia óptica estenopeica: D = (focalFactor * 1.70m) / heightNorm
            val estimatedDistance = (FOCAL_FACTOR * DEFAULT_HEIGHT_METERS) / normalizedHeight

            return PersonDetection(
                originalDetection = detection,
                estimatedDistanceMeters = estimatedDistance
            )
        }
    }
}
