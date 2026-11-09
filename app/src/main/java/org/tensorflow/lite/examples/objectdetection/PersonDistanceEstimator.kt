package org.tensorflow.lite.examples.objectdetection

import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetection
import kotlin.math.max

/**
 * Estimador de distancia para personas detectadas basado exclusivamente en el segmento
 * de la cintura a la cabeza (38% de la altura total promedio de 1.65 m = 0.627 m).
 */
data class PersonDetection(
    val originalDetection: ObjectDetection,
    val estimatedDistanceMeters: Float,
    val waistHeadHeightPx: Float
)

class PersonDistanceEstimator {

    companion object {
        // Factor focal vertical estandarizado para la cámara
        private const val FOCAL_FACTOR = 1.15f
        private const val TOTAL_HEIGHT_METERS = 1.65f // Estatura total promedio (1.65 m)
        private const val WAIST_HEAD_RATIO = 0.38f   // 38% correspondiente de Cintura a Cabeza
        private const val WAIST_HEAD_HEIGHT_METERS = TOTAL_HEIGHT_METERS * WAIST_HEAD_RATIO // 0.627 m

        /**
         * Determina si la etiqueta corresponde a una persona.
         */
        fun isPerson(label: String): Boolean {
            val lowercaseLabel = label.lowercase()
            return lowercaseLabel == "persona" || lowercaseLabel == "person"
        }

        /**
         * Calcula la distancia estimada midiendo el segmento de la cintura a la cabeza (38% de la altura total).
         * Distancia D = (f_y * H_cintura_cabeza) / h_cintura_cabeza_px
         */
        fun processDetection(
            detection: ObjectDetection,
            imageHeight: Int
        ): PersonDetection {
            val bboxHeightPx = max(1f, detection.boundingBox.height())
            // Medición del segmento de la cintura a la cabeza (38% del alto de la caja delimitadora)
            val waistHeadPx = max(1f, bboxHeightPx * WAIST_HEAD_RATIO)
            val normalizedSegmentHeight = waistHeadPx / imageHeight

            // Fórmula de distancia óptica usando el segmento de la cintura a la cabeza
            val estimatedDistance = (FOCAL_FACTOR * WAIST_HEAD_HEIGHT_METERS) / normalizedSegmentHeight

            return PersonDetection(
                originalDetection = detection,
                estimatedDistanceMeters = estimatedDistance,
                waistHeadHeightPx = waistHeadPx
            )
        }
    }
}
