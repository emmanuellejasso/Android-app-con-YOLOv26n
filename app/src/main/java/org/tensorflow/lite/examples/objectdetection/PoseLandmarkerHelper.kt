package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkerHelper(
    val context: Context,
    val poseLandmarkerListener: LandmarkerListener? = null
) {
    private var poseLandmarker: PoseLandmarker? = null

    init {
        setupPoseLandmarker()
    }

    fun setupPoseLandmarker() {
        try {
            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker.task")
                .setDelegate(Delegate.CPU)

            val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinPoseDetectionConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setNumPoses(3)
                .setRunningMode(RunningMode.IMAGE)

            poseLandmarker = PoseLandmarker.createFromOptions(context, optionsBuilder.build())
        } catch (e: Exception) {
            poseLandmarkerListener?.onError("Error al inicializar MediaPipe Pose: ${e.message}")
        }
    }

    fun detect(bitmap: Bitmap): PoseLandmarkerResult? {
        if (poseLandmarker == null) {
            setupPoseLandmarker()
        }
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            poseLandmarker?.detect(mpImage)
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    interface LandmarkerListener {
        fun onError(error: String)
    }
}
