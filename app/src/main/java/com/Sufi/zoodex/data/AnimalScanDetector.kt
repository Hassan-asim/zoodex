package com.Sufi.zoodex.data

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device detection: ML Kit object detection (regions) + image labeling on crops and full frame.
 * Same practical role as a small YOLO + classifier without bundling a large .tflite.
 */
object AnimalScanDetector {

    private const val TAG = "AnimalScanDetector"

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result -> if (cont.isActive) cont.resume(result) }
        addOnFailureListener { e -> if (cont.isActive) cont.resumeWithException(e) }
    }

    private suspend fun detectObjects(bitmap: Bitmap): List<DetectedObject> {
        val opts = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
        val detector = ObjectDetection.getClient(opts)
        val image = InputImage.fromBitmap(bitmap, 0)
        return detector.process(image).awaitTask()
    }

    private suspend fun labelBitmap(bitmap: Bitmap, minConfidence: Float): List<ImageLabel> {
        val opts = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(minConfidence)
            .build()
        val labeler = ImageLabeling.getClient(opts)
        val image = InputImage.fromBitmap(bitmap, 0)
        return labeler.process(image).awaitTask()
    }

    private fun crop(bitmap: Bitmap, box: Rect): Bitmap? {
        val left = box.left.coerceIn(0, bitmap.width - 1)
        val top = box.top.coerceIn(0, bitmap.height - 1)
        val right = box.right.coerceIn(left + 1, bitmap.width)
        val bottom = box.bottom.coerceIn(top + 1, bitmap.height)
        val w = right - left
        val h = bottom - top
        if (w < 32 || h < 32) return null
        return try {
            Bitmap.createBitmap(bitmap, left, top, w, h)
        } catch (e: Exception) {
            Log.w(TAG, "crop failed: ${e.message}")
            null
        }
    }

    private fun hintsFromLabelText(text: String): Set<String> {
        val key = text.lowercase()
        return buildSet {
            when {
                key.contains("dog") || key.contains("canine") || key.contains("puppy") || key.contains("wolf") || key.contains("canidae") || key.contains("mammal") || key.contains("pet") || key.contains("carnivore") || key.contains("snout") || key.contains("husky") || key.contains("terrier") || key.contains("shepherd") || key.contains("coyote") || key.contains("fox") || key.contains("dingo") || key.contains("jackal") -> add("CANINE")
                key.contains("cat") || key.contains("feline") || key.contains("tiger") || key.contains("leopard") || key.contains("kitten") || key.contains("lion") || key.contains("cheetah") || key.contains("lynx") || key.contains("panther") || key.contains("jaguar") || key.contains("cougar") || key.contains("puma") || key.contains("felidae") || key.contains("whiskers") || key.contains("tabby") || key.contains("siamese") -> add("FELINE")
                key.contains("bird") || key.contains("eagle") || key.contains("hawk") || key.contains("owl") || key.contains("sparrow") || key.contains("crow") || key.contains("duck") || key.contains("swan") || key.contains("goose") || key.contains("falcon") || key.contains("beak") || key.contains("feather") || key.contains("wing") || key.contains("predator") || key.contains("passerine") || key.contains("pheasant") || key.contains("hornbill") || key.contains("peacock") || key.contains("peafowl") || key.contains("crane") || key.contains("avian") -> add("AVIAN")
                key.contains("snake") || key.contains("lizard") || key.contains("cobra") || key.contains("reptile") || key.contains("crocodile") || key.contains("turtle") || key.contains("python") || key.contains("viper") || key.contains("scaled reptile") || key.contains("serpent") || key.contains("gecko") || key.contains("iguana") || key.contains("alligator") || key.contains("chameleon") || key.contains("monitor lizard") -> add("REPTILE")
                key.contains("fish") || key.contains("dolphin") || key.contains("otter") || key.contains("aquatic") || key.contains("frog") || key.contains("shark") || key.contains("whale") || key.contains("seal") || key.contains("walrus") || key.contains("marine biology") || key.contains("water") || key.contains("marine mammal") || key.contains("fin") || key.contains("cetacean") || key.contains("catfish") || key.contains("coral") -> add("AQUATIC")
                key.contains("deer") || key.contains("goat") || key.contains("boar") || key.contains("buffalo") || key.contains("ungulate") || key.contains("ibex") || key.contains("horse") || key.contains("sheep") || key.contains("cow") || key.contains("bull") || key.contains("livestock") || key.contains("cattle") || key.contains("pasture") || key.contains("antelope") || key.contains("rhino") || key.contains("rhinoceros") || key.contains("markhor") || key.contains("tusker") -> add("UNGULATE")
                key.contains("monkey") || key.contains("gorilla") || key.contains("primate") || key.contains("ape") || key.contains("chimpanzee") || key.contains("orangutan") || key.contains("baboon") || key.contains("gibbon") -> add("PRIMATE")
                key.contains("squirrel") || key.contains("rodent") || key.contains("mouse") || key.contains("rabbit") || key.contains("rat") || key.contains("hamster") || key.contains("porcupine") || key.contains("hare") || key.contains("guinea pig") -> add("RODENT")
                key.contains("mongoose") || key.contains("badger") || key.contains("mustelid") || key.contains("weasel") || key.contains("ferret") || key.contains("mink") || key.contains("civet") -> add("MUSTELID")
                key.contains("bear") || key.contains("ursidae") || key.contains("koala") || key.contains("panda") || key.contains("grizzly") -> add("URSIDAE")
                key.contains("elephant") || key.contains("proboscidean") || key.contains("trunk") -> add("PROBOSCIDEAN")
                key.contains("phantom") || key.contains("ghost") || key.contains("specter") || key.contains("dark") || key.contains("shadow") -> add("PHANTOM")
                else -> Unit
            }
        }
    }

    suspend fun analyzeStrict(bitmap: Bitmap): Triple<List<Int>, String, Boolean> = withContext(Dispatchers.Default) {
        Log.d(TAG, "Starting analyze process for bitmap ${bitmap.width}x${bitmap.height}")
        try {
            val classHints = linkedSetOf<String>()
            val allLabelTexts = mutableListOf<String>()

            val objects = detectObjects(bitmap)
            for (obj in objects) {
                for (lbl in obj.labels) {
                    allLabelTexts.add(lbl.text)
                    classHints.addAll(hintsFromLabelText(lbl.text))
                }
                val box = obj.boundingBox
                if (box != null) {
                    val cropped = crop(bitmap, box) ?: continue
                    val cropLabels = labelBitmap(cropped, 0.22f)
                    for (l in cropLabels) {
                        allLabelTexts.add(l.text)
                        classHints.addAll(hintsFromLabelText(l.text))
                    }
                }
            }

            val fullLabels = labelBitmap(bitmap, 0.22f)
            for (l in fullLabels) {
                allLabelTexts.add(l.text)
                classHints.addAll(hintsFromLabelText(l.text))
            }

            val matched = classHints
                .flatMap { AnimalDatabase.getAnimalsByClass(it) }
                .distinctBy { it.id }
                .take(3)

            val headline = allLabelTexts.firstOrNull()?.uppercase() ?: "UNKNOWN_SIGNAL"
            
            // Strictly check if any detected hint actually belongs to the encyclopedia
            val existsInEncyclopedia = classHints.any { hint -> 
                AnimalDatabase.allAnimals.any { it.encyclopediaClass.equals(hint, true) }
            }

            if (existsInEncyclopedia && matched.isNotEmpty()) {
                return@withContext Triple(matched.map { it.id }, headline, true)
            }
            
            Triple(emptyList(), headline, false)
        } catch (e: Exception) {
            Log.e(TAG, "analyze error: ${e.message}", e)
            Triple(emptyList(), "SCAN_RECOVERY", false)
        }
    }
}
