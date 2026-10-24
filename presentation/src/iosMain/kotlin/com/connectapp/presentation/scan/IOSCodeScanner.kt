package com.connectapp.presentation.scan

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSSelectorFromString
import platform.UIKit.*
import platform.darwin.*

@OptIn(ExperimentalForeignApi::class)
internal class IOSCodeScanner(private val cancelLabel: String) : CodeScanner {
    private var controller: BarcodeCaptureController? = null
    private var active = false
    override fun launch(onResult: (CodeScanResult) -> Unit) {
        if (active) return
        active = true
        fun show() {
            if (!active) return
            val scene = UIApplication.sharedApplication.connectedScenes.filterIsInstance<UIWindowScene>()
                .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            val root = scene?.keyWindow?.rootViewController ?: UIApplication.sharedApplication.keyWindow?.rootViewController
            if (root == null) { active = false; onResult(CodeScanResult.Unavailable); return }
            var presenter: UIViewController = root
            while (presenter.presentedViewController != null) presenter = presenter.presentedViewController!!
            val capture = BarcodeCaptureController(cancelLabel) { result ->
                controller = null
                if (active) { active = false; onResult(result) }
            }
            controller = capture
            capture.modalPresentationStyle = UIModalPresentationFullScreen
            presenter.presentViewController(capture, animated = true, completion = null)
        }
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> show()
            AVAuthorizationStatusNotDetermined -> AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                dispatch_async(dispatch_get_main_queue()) {
                    if (granted) show() else if (active) { active = false; onResult(CodeScanResult.PermissionDenied) }
                }
            }
            else -> { active = false; onResult(CodeScanResult.PermissionDenied) }
        }
    }
    override fun close() { active = false; controller?.finish(CodeScanResult.Cancelled); controller = null }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class BarcodeCaptureController(
    private val cancelLabel: String,
    private val onResult: (CodeScanResult) -> Unit
) : UIViewController(nibName = null, bundle = null), AVCaptureMetadataOutputObjectsDelegateProtocol {
    private val session = AVCaptureSession()
    private val queue = dispatch_queue_create("mibotiquin.camera", null)
    private var preview: AVCaptureVideoPreviewLayer? = null
    private var closeButton: UIButton? = null
    private var finished = false
    private var appeared = false
    private var pendingResult: CodeScanResult? = null

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.blackColor
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, null) }
        val output = AVCaptureMetadataOutput()
        if (input == null || !session.canAddInput(input) || !session.canAddOutput(output)) {
            dispatch_async(dispatch_get_main_queue()) { finish(CodeScanResult.Unavailable) }; return
        }
        session.addInput(input); session.addOutput(output)
        output.setMetadataObjectsDelegate(this, dispatch_get_main_queue())
        val supported = listOfNotNull(AVMetadataObjectTypeEAN13Code, AVMetadataObjectTypeDataMatrixCode)
            .filter { output.availableMetadataObjectTypes.contains(it) }
        if (supported.isEmpty()) { dispatch_async(dispatch_get_main_queue()) { finish(CodeScanResult.Unavailable) }; return }
        output.metadataObjectTypes = supported
        preview = AVCaptureVideoPreviewLayer(session = session).also {
            it.videoGravity = AVLayerVideoGravityResizeAspectFill
            view.layer.addSublayer(it)
        }
        closeButton = UIButton.buttonWithType(UIButtonTypeSystem).also {
            it.setTitle(cancelLabel, UIControlStateNormal)
            it.setTitleColor(UIColor.whiteColor, UIControlStateNormal)
            it.backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.7)
            it.layer.cornerRadius = 12.0
            it.addTarget(this, NSSelectorFromString("closeScanner"), UIControlEventTouchUpInside)
            view.addSubview(it)
        }
        dispatch_async(queue) { if (!finished) session.startRunning() }
    }

    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        appeared = true
        pendingResult?.let { pendingResult = null; finish(it) }
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        preview?.frame = view.bounds
        closeButton?.setFrame(CGRectMake(16.0, view.safeAreaInsets.useContents { top } + 16.0, 140.0, 48.0))
    }

    override fun captureOutput(output: AVCaptureOutput, didOutputMetadataObjects: List<*>, fromConnection: AVCaptureConnection) {
        val code = didOutputMetadataObjects.filterIsInstance<AVMetadataMachineReadableCodeObject>().firstOrNull()?.stringValue ?: return
        finish(CodeScanResult.Read(code))
    }

    @ObjCAction
    fun closeScanner() { finish(CodeScanResult.Cancelled) }

    fun finish(result: CodeScanResult) {
        if (finished) return
        if (!appeared) { pendingResult = result; return }
        finished = true
        dispatch_async(queue) { session.stopRunning() }
        dismissViewControllerAnimated(true) { onResult(result) }
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        if (!finished) finish(CodeScanResult.Cancelled)
    }
}
