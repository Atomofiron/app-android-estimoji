package app.atomofiron.estimoji.screen.scan

import android.content.pm.PackageManager
import android.os.Bundle
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ToggleButton
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.*
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.screen.base.BaseFragment
import app.atomofiron.estimoji.util.Knife

class ScanFragment : BaseFragment<ScanViewModel>(), DecoratedBarcodeView.TorchListener {

    override val layoutId: Int = R.layout.fragment_scan

    private val barcodeScannerView = Knife<DecoratedBarcodeView>(this, R.id.zxing_barcode_scanner)
    private val statusView = Knife<View>(this, R.id.zxing_status_view)
    private val flashlightToggle = Knife<ToggleButton>(this, R.id.switch_flashlight)

    private lateinit var capture: CaptureManager

    private lateinit var beepManager: BeepManager
    private val barcodeCallback = BarcodeCallbackImpl()

    override val viewModelClass = ScanViewModel::class

    private val hasFlash: Boolean get() = context!!.packageManager
        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeScannerView.view.setTorchListener(this)

        statusView.view.layoutParams = (statusView.view.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        if (hasFlash) {
            flashlightToggle.view.setOnCheckedChangeListener { buttonView, isChecked ->
                buttonView.isEnabled = false
                if (isChecked) {
                    barcodeScannerView.view.setTorchOn()
                } else {
                    barcodeScannerView.view.setTorchOff()
                }
            }
        } else {
            flashlightToggle.view.visibility = View.GONE
        }

        val intent = Intent(Intents.Scan.ACTION)
        intent.putExtra(Intents.Scan.PROMPT_MESSAGE, "")
        capture = CaptureManager(activity, barcodeScannerView.view)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()

        barcodeScannerView.view.decodeContinuous(barcodeCallback)
        beepManager = BeepManager(activity)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        barcodeCallback.clearLastResult()
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    /*override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeScannerView.view.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }*/

    override fun onTorchOn() {
        flashlightToggle.view.isEnabled = true
    }

    override fun onTorchOff() {
        flashlightToggle.view.isEnabled = true
    }

    private inner class BarcodeCallbackImpl : BarcodeCallback {
        private var lastResult: String? = null

        fun clearLastResult() {
            lastResult = null
            barcodeScannerView.view.setStatusText(null)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) = Unit

        override fun barcodeResult(result: BarcodeResult) {
            when (lastResult) {
                result.text -> return
                else -> lastResult = result.text
            }

            barcodeScannerView.view.setStatusText(result.text)
            beepManager.playBeepSound()

            viewModel.onScanResult(result.text)
        }
    }
}