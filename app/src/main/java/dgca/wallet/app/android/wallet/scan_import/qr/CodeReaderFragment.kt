/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-wallet-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by osarapulov on 9/12/21 12:58 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.databinding.FragmentCodeReaderBinding
import dgca.wallet.app.android.model.TicketingCheckInParcelable
import dgca.wallet.app.android.wallet.scan_import.qr.certificate.ClaimGreenCertificateModel

const val CAMERA_REQUEST_CODE = 1003

@AndroidEntryPoint
class CodeReaderFragment : BindingFragment<FragmentCodeReaderBinding>(), NavController.OnDestinationChangedListener {

    private lateinit var beepManager: BeepManager
    private var lastText: String? = null

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }
            binding.barcodeScanner.pause()

            lastText = result.text
            beepManager.playBeepSoundAndVibrate()

            navigateToModelFetcherPage(result.text)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCodeReaderBinding =
        FragmentCodeReaderBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestCameraPermission()

        val formats: Collection<BarcodeFormat> = listOf(BarcodeFormat.AZTEC, BarcodeFormat.QR_CODE)
        binding.barcodeScanner.decoderFactory = DefaultDecoderFactory(formats)
        binding.barcodeScanner.decodeContinuous(callback)
        beepManager = BeepManager(requireActivity())

        setFragmentResultListener(FETCH_MODEL_REQUEST_KEY) { _, bundle ->
            findNavController().navigateUp()
            val claimGreenCertificateModel: ClaimGreenCertificateModel? = bundle.getParcelable(CLAIM_GREEN_CERTIFICATE_RESULT_KEY)
            if (claimGreenCertificateModel != null) {
                navigateToClaimCertificatePage(claimGreenCertificateModel)
            } else {
                val ticketingCheckInParcelable: TicketingCheckInParcelable? = bundle.getParcelable(BOOKING_SYSTEM_MODEL_RESULT_KEY)
                if (ticketingCheckInParcelable != null) {
                    navigateToBookingSystemConsentPage(ticketingCheckInParcelable)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findNavController().addOnDestinationChangedListener(this)
        lastText = ""
    }

    override fun onPause() {
        super.onPause()
        findNavController().removeOnDestinationChangedListener(this)
        binding.barcodeScanner.pause()
    }

    private fun navigateToModelFetcherPage(qrCodeText: String) {
        val action =
            CodeReaderFragmentDirections.actionCodeReaderFragmentToModelFetcherDialogFragment(qrCodeText)
        findNavController().navigate(action)
    }

    private fun navigateToClaimCertificatePage(claimGreenCertificateModel: ClaimGreenCertificateModel) {
        binding.barcodeScanner.pause()
        val action =
            CodeReaderFragmentDirections.actionCodeReaderFragmentToClaimCertificateFragment(claimGreenCertificateModel)
        findNavController().navigate(action)
    }

    private fun navigateToBookingSystemConsentPage(ticketingCheckInParcelable: TicketingCheckInParcelable) {
        binding.barcodeScanner.pause()
        val action =
            CodeReaderFragmentDirections.actionCodeReaderFragmentToBookingSystemConsentFragment(ticketingCheckInParcelable)
        findNavController().navigate(action)
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination.id == R.id.codeReaderFragment) {
            binding.barcodeScanner.resume()
            lastText = ""
        }
    }
}