package org.tensorflow.lite.examples.objectdetection.detectors

import android.content.Context
import android.graphics.RectF
import com.ultralytics.yolo.ImageProcessing
import com.ultralytics.yolo.models.LocalYoloModel
import com.ultralytics.yolo.predict.detect.DetectedObject
import com.ultralytics.yolo.predict.detect.TfliteDetector
import org.tensorflow.lite.support.image.TensorImage

class YoloDetector(
    var confidenceThreshold: Float = 0.5f,
    var iouThreshold: Float = 0.3f,
    var numThreads: Int = 2,
    var maxResults: Int = 3,
    var currentDelegate: Int = 0,
    var currentModel: Int = 0,
    val context: Context
): ObjectDetector {

    private var yolo: TfliteDetector
    private var ip: ImageProcessing

    init {
        yolo = TfliteDetector(context)
        yolo.setIouThreshold(iouThreshold)
        yolo.setConfidenceThreshold(confidenceThreshold)

        val modelPath = "yolov26n_float32.tflite"
        val metadataPath = "metadata.yaml"

        val config = LocalYoloModel(
            "detect",
            "tflite",
            modelPath,
            metadataPath
        )

        val useGPU = currentDelegate == 0
        yolo.loadModel(
            config,
            useGPU
        )

        ip = ImageProcessing()
    }

    override fun detect(image: TensorImage, imageRotation: Int): DetectionResult {
        val bitmap = image.bitmap

        val ppImage = yolo.preprocess(bitmap)
        val results = yolo.predict(ppImage)

        val detections = ArrayList<ObjectDetection>()

        for (result: DetectedObject in results) {
            val category = Category(
                result.label,
                result.confidence
            )
            // Mantener coordenadas normalizadas (0.0f .. 1.0f) para alineación perfecta con el Canvas
            val yoloBox = result.boundingBox

            val bbox = RectF(
                yoloBox.left,
                yoloBox.top,
                yoloBox.right,
                yoloBox.bottom
            )
            val detection = ObjectDetection(
                bbox,
                category
            )
            detections.add(detection)
        }

        val ret = DetectionResult(ppImage, detections)
        ret.info = yolo.stats
        return ret
    }
}
