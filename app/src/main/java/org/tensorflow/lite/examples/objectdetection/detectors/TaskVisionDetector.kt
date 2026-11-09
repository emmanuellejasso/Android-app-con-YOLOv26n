package org.tensorflow.lite.examples.objectdetection.detectors

import android.content.Context
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

        val detections = LinkedList<ObjectDetection>()
        for (tvDetection: Detection in tvDetections) {
            val cat = tvDetection.categories[0]

            val objDet = ObjectDetection(
                boundingBox = tvDetection.boundingBox,
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
