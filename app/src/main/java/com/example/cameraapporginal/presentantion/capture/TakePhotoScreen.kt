package com.example.cameraapporginal.presentantion.capture

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff.*
import android.graphics.RectF
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.cameraapporginal.R
import com.example.cameraapporginal.data.SharedPref
import com.example.cameraapporginal.databinding.FragmentTakePhotoBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.cameraapporginal.util.MyEventBus
import com.example.cameraapporginal.util.appSettingOpen
import com.example.cameraapporginal.util.checkPermission
import com.example.cameraapporginal.util.gone
import com.example.cameraapporginal.util.setOnSingleClickListener
import com.example.cameraapporginal.util.visible
import com.example.cameraapporginal.util.warningPermissionDialog
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class TakePhotoScreen : Fragment(R.layout.fragment_take_photo) {
    companion object {
        private const val FILENAME_FORMAT = "yyyyMMddHHmmss"
        private val REQUIRED_PERMISSION = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    init {
        MyEventBus.connectedEvent = {
            if (permissionGranted && shared.volumePress) {
                takePhoto()
            }
        }
    }

    private var timerPos: Int = 0
    private var cameraType = 0
    private var uri: String? = null
    private var duration = 0L
    private val shared = SharedPref.getInstance()
    private var jobProgress: Job? = null
    private var jobTask: Job? = null
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSION && !it.value) {
                    permissionGranted = false
                }
                if (!permissionGranted) {
                    //todo
                } else {
                    startCamera()
                }
            }
        }

    private val binding by viewBinding(FragmentTakePhotoBinding::bind)
    private var imageCapture: ImageCapture? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var isPhoto = true
    private var clickMP: MediaPlayer? = null
    private var permissionGranted = true
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var cameraSelector: CameraSelector
    private var orientationEventListener: OrientationEventListener? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var aspectRatio = AspectRatio.RATIO_16_9
    private val multiplePermissionId = 14
    private val navController by lazy { findNavController() }
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {

        arrayListOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)

    } else {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initActions()

        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#FF000000")
        window.navigationBarColor = Color.parseColor("#FF000000")
//
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.statusBarColor = Color.parseColor("#FFFCF5FD")
        configureTimer(shared.timerType)
        uri = shared.numbers
        uri?.let {
            binding.images.setImageURI(it.toUri())
        }
        requireContext().checkPermission(multiplePermissionNameList) {
            startCamera()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        when (shared.cameraType) {
            1 -> {
                isPhoto = false
                binding.btnTakePhoto.gone()
            }

            else -> {
                binding.btnTakePhoto.visible()
                isPhoto = true
            }
        }
    }


    private fun initCameraRatio() {
        binding.apply {
            when (shared.aspectRatio) {
                1 -> {
                    aspectRatio = AspectRatio.RATIO_4_3
                    shared.aspectRatio = 1
                    setAspectRatio("H,1:1")
                    goneRatioBar()
                    oneOne.setTextColor(Color.parseColor("#FFBF00"))
                    threeFour.setTextColor(Color.parseColor("#FFFFFFFF"))
                    nineSixteen.setTextColor(Color.parseColor("#FFFFFFFF"))
                    aspectRatioTxt.text = "1:1"
                }

                3 -> {
                    aspectRatio = AspectRatio.RATIO_4_3
                    setAspectRatio("H,3:4")
                    shared.aspectRatio = 3
                    goneRatioBar()
                    oneOne.setTextColor(Color.parseColor("#FFFFFFFF"))
                    threeFour.setTextColor(Color.parseColor("#FFBF00"))
                    nineSixteen.setTextColor(Color.parseColor("#FFFFFFFF"))
                    aspectRatioTxt.text = "3:4"
                }

                else -> {
                    aspectRatio = AspectRatio.RATIO_16_9
                    setAspectRatio("H,0:0")
                    aspectRatioTxt.text = "9:16"
                    shared.aspectRatio = 9
                    goneRatioBar()
                    oneOne.setTextColor(Color.parseColor("#FFFFFFFF"))
                    threeFour.setTextColor(Color.parseColor("#FFFFFFFF"))
                    nineSixteen.setTextColor(Color.parseColor("#FFBF00"))
                }
            }
        }

    }
    private fun playClickSound() {
        if (clickMP != null) {
            clickMP!!.release()
        }
        clickMP = MediaPlayer.create(requireContext(), R.raw.capture)
        clickMP!!.start()
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun initActions() {
        binding.apply {
            btnTakePhoto.setOnSingleClickListener {
                takePhoto()
            }
            btnChangeCamera.setOnSingleClickListener {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
                bindCameraUserCases()
            }
            uri?.let {
                images.setImageURI(it.toUri())
            }
            btnTimer2.setOnSingleClickListener {
                shared.timerType = 1
                configureTimer(1)
            }
            btnTimer5.setOnSingleClickListener {
                shared.timerType = 2
                configureTimer(2)
            }
            btnTimer10.setOnSingleClickListener {
                shared.timerType = 3
                configureTimer(3)
            }
            btnTimerNone.setOnSingleClickListener {
                shared.timerType = 0
                configureTimer(0)
            }

            btnStopProgress.setOnSingleClickListener {
                jobProgress?.cancel()
                jobTask?.cancel()
                mainState.visible()
                progressState.gone()
                progressBar.gone()
                proressText.gone()
            }

            btnPreview.setOnSingleClickListener {
                uri?.let { it1 ->
                    findNavController().navigate(
                        TakePhotoScreenDirections.actionTakePhotoScreenToPhotoPreviewScreen(
                            it1
                        )
                    )
                }
            }
            btnSettings.setOnSingleClickListener {
                navController.navigate(TakePhotoScreenDirections.actionTakePhotoScreenToSettingsScreen())
            }
            aspectRatioTxt.setOnSingleClickListener {
                binding.ratioBar.visible()
                binding.topBar.gone()
            }
            oneOne.setOnSingleClickListener {
                aspectRatio = AspectRatio.RATIO_4_3
                shared.aspectRatio = 1
                setAspectRatio("H,1:1")
                goneRatioBar()
                oneOne.setTextColor(Color.parseColor("#FFBF00"))
                threeFour.setTextColor(Color.parseColor("#FFFFFFFF"))
                nineSixteen.setTextColor(Color.parseColor("#FFFFFFFF"))
                aspectRatioTxt.text = "1:1"
                bindCameraUserCases()
            }
            threeFour.setOnSingleClickListener {
                aspectRatio = AspectRatio.RATIO_4_3
                setAspectRatio("H,3:4")
                shared.aspectRatio = 3
                goneRatioBar()
                oneOne.setTextColor(Color.parseColor("#FFFFFFFF"))
                threeFour.setTextColor(Color.parseColor("#FFBF00"))
                nineSixteen.setTextColor(Color.parseColor("#FFFFFFFF"))
                aspectRatioTxt.text = "3:4"
                bindCameraUserCases()
            }
            nineSixteen.setOnSingleClickListener {
                aspectRatio = AspectRatio.RATIO_16_9
                setAspectRatio("H,0:0")
                aspectRatioTxt.text = "9:16"
                shared.aspectRatio = 9
                goneRatioBar()
                oneOne.setTextColor(Color.parseColor("#FFFFFFFF"))
                threeFour.setTextColor(Color.parseColor("#FFFFFFFF"))
                nineSixteen.setTextColor(Color.parseColor("#FFBF00"))
                bindCameraUserCases()
            }
//            btnVideo.setOnSingleClickListener {
//                if (!isPhoto) return@setOnSingleClickListener
//                shared.cameraType = 1
//                isPhoto = false
//                btnTakePhoto.gone()
//                bindCameraUserCases()
//            }
//            btnPhoto.setOnSingleClickListener {
//                if (isPhoto) return@setOnSingleClickListener
//                shared.cameraType = 0
//                isPhoto = true
//                btnTakePhoto.visible()
//                bindCameraUserCases()
//            }
            btnTimer.setOnSingleClickListener {
                visibleTimerBar()
            }
            btnCapture.setOnSingleClickListener {
                if (isPhoto) {
                    takePhoto()
                } else {
                    var maxProgress =
                        if (duration == 10000L) 10 else duration.toString()[0].toString().toInt()
                    binding.progressBar.max = maxProgress
                    binding.progressBar.progress = maxProgress
                    binding.proressText.text = maxProgress.toString()


                    if (timerPos != 0) {
                        binding.progressBar.visible()
                        binding.proressText.visible()
                        binding.mainState.gone()
                        binding.progressState.visible()
                    }
                    jobProgress = lifecycleScope.launch {
                        for (i in 0 until maxProgress) {
                            delay(1000)
                            maxProgress--
                            binding.progressBar.progress = maxProgress
                            binding.proressText.text = maxProgress.toString()
                        }
                    }
                    jobTask = lifecycleScope.launch {
                        delay(duration)
                        binding.progressBar.gone()
                        binding.proressText.gone()
                        binding.mainState.visible()
                        binding.progressState.gone()
                        startCaptureVideo()
                    }

                }
            }
            flashToggleIB.setOnSingleClickListener {
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    "Flash is disable this mode".toast(requireContext())
                    return@setOnSingleClickListener
                }
                setFlashIcon(camera)
            }
            squareImg.setOnSingleClickListener {
                recording?.stop()
                recording = null
                squareImg.gone()
                stopRecording()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        jobTask?.cancel()
        jobProgress?.cancel()
    }

    private fun goneRatioBar() {
        binding.ratioBar.gone()
        binding.topBar.visible()
    }

    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }


    fun configureCameraFilters(camera: Camera) {

    }

    @SuppressLint("ResourceAsColor")
    private fun configureTimer(pos: Int) {
        timerPos = pos
        goneTimerBar()
        binding.apply {
            when (pos) {
                0 -> {
                    duration = 0
                    btnTimer.setImageResource(R.drawable.clock_icon)
                    btnTimerNone.setTint(R.color.gold)
                    btnTimer.setTint(R.color.white)
                    btnTimer2.setTint(R.color.white)
                    btnTimer5.setTint(R.color.white)
                    btnTimer10.setTint(R.color.white)
                }

                1 -> {
                    duration = 2000
                    btnTimer.setImageResource(R.drawable.two_times_icon)
                    btnTimer.setTint(R.color.gold)
                    btnTimerNone.setTint(R.color.white)
                    btnTimer2.setTint(R.color.gold)
                    btnTimer5.setTint(R.color.white)
                    btnTimer10.setTint(R.color.white)
                }

                2 -> {
                    duration = 5000
                    btnTimer.setImageResource(R.drawable.five_times_icon)
                    btnTimer.setTint(R.color.gold)
                    btnTimerNone.setTint(R.color.white)
                    btnTimer2.setTint(R.color.white)
                    btnTimer5.setTint(R.color.gold)
                    btnTimer10.setTint(R.color.white)
                }

                else -> {
                    duration = 10000
                    btnTimer.setImageResource(R.drawable.ten_times_icon)
                    btnTimer.setTint(R.color.gold)
                    btnTimerNone.setTint(R.color.white)
                    btnTimer2.setTint(R.color.white)
                    btnTimer5.setTint(R.color.white)
                    btnTimer10.setTint(R.color.gold)
                }
            }
        }
    }

    @SuppressLint("ResourceType")
    fun ImageButton.setTint(@ColorInt color: Int) {
        this.setColorFilter(ContextCompat.getColor(requireContext(), color), Mode.SRC_IN);
    }

    private fun visibleTimerBar() {
        binding.timerBar.visible()
        binding.topBar.gone()
    }

    private fun goneTimerBar() {
        binding.timerBar.gone()
        binding.topBar.visible()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully
                    startCamera()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(),
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(requireContext())
                    } else {
                        // here warning permission show
                        warningPermissionDialog(requireContext()) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUserCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setAspectRatio(ratio: String) {
        binding.cameraPreview.layoutParams = binding.cameraPreview.layoutParams.apply {
            if (this is ConstraintLayout.LayoutParams) {
                dimensionRatio = ratio
            }
        }
    }


    private fun bindCameraUserCases() {
        val rotation = binding.cameraPreview.display.rotation

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.HIGHEST,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                )
            )
            .setAspectRatio(aspectRatio)
            .build()

        videoCapture = VideoCapture.withOutput(recorder).apply {
            targetRotation = rotation
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        orientationEventListener = object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val myRotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = myRotation
                videoCapture.targetRotation = myRotation
            }
        }
        orientationEventListener?.enable()

        try {
            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, videoCapture
            )
            setUpZoomTapToFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpZoomTapToFocus() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                val delta = detector.scaleFactor
                camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(requireContext(), listener)

        binding.cameraPreview.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN) {
                val factory = binding.cameraPreview.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS)
                    .build()

                val x = event.x
                val y = event.y

                val focusCircle = RectF(x - 50, y - 50, x + 50, y + 50)

                binding.focusCircleView.focusCircle = focusCircle
                binding.focusCircleView.invalidate()

                camera.cameraInfo.zoomState.observe(viewLifecycleOwner, zoomObserver)
                camera.cameraControl.startFocusAndMetering(action)

                view.performClick()
            }
            true
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setFlashIcon(camera: Camera) {

        if (camera.cameraInfo.hasFlashUnit()) {
            if (camera.cameraInfo.torchState.value == 0) {
                camera.cameraControl.enableTorch(true)
                binding.flashToggleIB.setImageResource(R.drawable.flash_on)
                binding.flashToggleIB.setTint(R.color.gold)
            } else {
                camera.cameraControl.enableTorch(false)
                binding.flashToggleIB.setImageResource(R.drawable.flash_off)
                binding.flashToggleIB.setTint(R.color.white)
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Flash is Not Available",
                Toast.LENGTH_LONG
            ).show()
            binding.flashToggleIB.isEnabled = false
        }
    }

    private val zoomObserver = androidx.lifecycle.Observer<ZoomState> {
//        Toast.makeText(requireContext(), it.zoomRatio.toString(), Toast.LENGTH_SHORT).show()
    }


    @SuppressLint("SimpleDateFormat")
    private fun takePhoto() {
        val imageName = SimpleDateFormat(FILENAME_FORMAT)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues()
            .apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures")
                }
            }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireActivity().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
            .build()

//        try {
//            val outputStream: OutputStream = FileOutputStream(file)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
//
//            // Tell the media scanner to scan the new image, so it will be visible in the gallery
//            MediaScannerConnection.scanFile(
//                context,
//                arrayOf(file.absolutePath),
//                null,
//                null
//            )
//
//            val ordinalNumber = MyShared.getSize() + 1
//            val img = file.absolutePath
//
//            "edit save image $ordinalNumber, $img".myLog()
//
//            MyShared.setImages(ordinalNumber = ordinalNumber, img = img)
//
//            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
//            e.printStackTrace()
//        }


        var maxProgress = if (duration == 10000L) 10 else duration.toString()[0].toString().toInt()
        binding.progressBar.max = maxProgress
        binding.progressBar.progress = maxProgress
        binding.proressText.text = maxProgress.toString()


        if (timerPos != 0) {
            binding.progressBar.visible()
            binding.proressText.visible()
            binding.mainState.gone()
            binding.progressState.visible()
        }
        jobProgress = lifecycleScope.launch {
            for (i in 0 until maxProgress) {
                delay(1000)
                maxProgress--
                binding.progressBar.progress = maxProgress
                binding.proressText.text = maxProgress.toString()
            }
        }

        jobTask = lifecycleScope.launch {
            delay(duration)

            binding.progressBar.gone()
            binding.proressText.gone()
            binding.mainState.visible()
            binding.progressState.gone()


            imageCapture!!.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        binding.images.isClickable = true
                        playClickSound()
                        binding.images.setImageURI(outputFileResults.savedUri)
                        shared.numbers = outputFileResults.savedUri.toString()
                        uri = shared.numbers

                    }

                    override fun onError(exception: ImageCaptureException) {

                    }
                }
            )
        }

    }


    @SuppressLint("SimpleDateFormat")
    private fun startCaptureVideo() {
        "Video metodi".toast(requireContext())
        if (recording != null) {
            recording!!.stop()
            recording = null
            return
        }
        startRecording()

        val videoName = SimpleDateFormat(FILENAME_FORMAT)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, videoName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutPutOptions = MediaStoreOutputOptions
            .Builder(
                requireActivity().contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutPutOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {

                        binding.squareImg.visible()
                        binding.squareImg.apply {
//                            text = getString(R.string.stop_capture)
                            setImageResource(R.drawable.ic_stop)
                            //    ContentCaptureManager.isEnabled = true
                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg =
                                "Video capture succeeded: " + "${recordEvent.outputResults.outputUri}"
                            msg.toast(requireContext())
                        } else {
                            recording?.close()
                            recording = null
                        }
                        binding.squareImg.apply {
                            setImageResource(R.drawable.ic_video)
                        }
                    }
                }
            }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSION)
    }

    private fun captureVideo() {

        binding.btnCapture.isEnabled = false

        binding.flashToggleIB.gone()
        binding.btnSettings.gone()
        binding.aspectRatioTxt.gone()


        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            stopRecording()
            recording = null
            return
        }
        startRecording()
        val fileName = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(requireActivity().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutputOptions)
            .apply {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        binding.btnCapture.setImageResource(R.drawable.ic_stop)
                        binding.btnCapture.isEnabled = true
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val message =
                                "Video Capture Succeeded: " + "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            recording?.close()
                            recording = null
                            Log.d("error", recordEvent.error.toString())
                        }
                        binding.btnCapture.setImageResource(R.drawable.ic_start)
                        binding.btnCapture.isEnabled = true

                        binding.flashToggleIB.visible()
                        binding.aspectRatioTxt.visible()
                        binding.btnSettings.visible()
                    }
                }
            }

    }


    override fun onResume() {
        super.onResume()
        initCameraRatio()
        orientationEventListener?.enable()
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()
        if (recording != null) {
            recording?.stop()
            startCaptureVideo()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimer = object : Runnable {
        override fun run() {
            val currentTime = SystemClock.elapsedRealtime() - binding.recodingTimerC.base
            val timeString = currentTime.toFormattedTime()
            binding.recodingTimerC.text = timeString
            handler.postDelayed(this, 1000)
        }
    }

    private fun Long.toFormattedTime(): String {
        val seconds = ((this / 1000) % 60).toInt()
        val minutes = ((this / (1000 * 60)) % 60).toInt()
        val hours = ((this / (1000 * 60 * 60)) % 24).toInt()

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun startRecording() {
        binding.recodingTimerC.visible()
        binding.recodingTimerC.base = SystemClock.elapsedRealtime()
        binding.recodingTimerC.start()
        handler.post(updateTimer)
    }

    private fun stopRecording() {
        binding.recodingTimerC.gone()
        binding.recodingTimerC.stop()
        handler.removeCallbacks(updateTimer)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}

fun String.toast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}
