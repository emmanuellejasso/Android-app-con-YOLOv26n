package org.tensorflow.lite.examples.objectdetection.detectors

import android.content.Context
import android.graphics.RectF
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.util.LinkedList

class TaskVisionDetector(
    var options: ObjectDetector.ObjectDetectorOptions,
    var currentModel: Int = 0,
    val context: Context
): org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetector {

    private var objectDetector: ObjectDetector

    init {
        val modelName = "mobilenetv1.tflite"
        objectDetector = ObjectDetector.createFromFileAndOptions(context, modelName, options)
    }

    override fun detect(tensorImage: TensorImage, imageRotation: Int): DetectionResult {
        val tvDetections = objectDetector.detect(tensorImage)

        val imageWidth = tensorImage.width.toFloat()
        val imageHeight = tensorImage.height.toFloat()

        val detections = LinkedList<ObjectDetection>()
        for (tvDetection: Detection in tvDetections) {
            val cat = tvDetection.categories[0]
            val rawBbox = tvDetection.boundingBox

            // Normalizar coordenadas (0.0f .. 1.0f) relativas a la imagen
            val normBbox = RectF(
                rawBbox.left / imageWidth,
                rawBbox.top / imageHeight,
                rawBbox.right / imageWidth,
                rawBbox.bottom / imageHeight
            )

            val objDet = ObjectDetection(
                boundingBox = normBbox,
                category = Category(
                    cat.label,
                    cat.score
                )
            )
            detections.add(objDet)
        }
        val results = DetectionResult(
            tensorImage.bitmap,
            detections
        )

        return results
    }
}
