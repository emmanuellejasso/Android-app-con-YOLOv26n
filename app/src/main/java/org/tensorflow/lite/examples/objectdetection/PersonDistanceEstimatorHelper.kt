package org.tensorflow.lite.examples.objectdetection

import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetection
import kotlin.math.max

/**
 * Estimador de distancia óptica para peatones basado en la estatura promedio (1.65m).
 */
data class PersonDetection(
    val originalDetection: ObjectDetection,
    val estimatedDistanceMeters: Float
)

class PersonDistanceEstimatorHelper {

    companion object {
        private const val FOCAL_FACTOR = 1.15f
        private const val AVERAGE_PERSON_HEIGHT_METERS = 1.65f // Estatura promedio (1.65m)

        /**
         * Comprueba si una etiqueta de clase corresponde a una persona.
         */
        fun isPerson(label: String): Boolean {
            val lowercaseLabel = label.lowercase()
            return lowercaseLabel == "persona" || lowercaseLabel == "person"
        }

        /**
         * Calcula la distancia en metros hacia la persona detectada.
         */
        fun processDetection(
            detection: ObjectDetection,
            imageHeight: Int
        ): PersonDetection {
            val bbox = detection.boundingBox
            val bboxHeightPx = max(1f, bbox.height())
            val normalizedHeight = bboxHeightPx / imageHeight.toFloat()

            val distanceMeters = (FOCAL_FACTOR * AVERAGE_PERSON_HEIGHT_METERS) / normalizedHeight

            return PersonDetection(
                originalDetection = detection,
                estimatedDistanceMeters = distanceMeters
            )
        }
    }
}
