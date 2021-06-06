package com.capstone.ayoperbaiki.form

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Address
import com.capstone.ayoperbaiki.databinding.ActivityDisasterReportFormBinding
import com.capstone.ayoperbaiki.databinding.DisasterReportFormBinding
import com.capstone.ayoperbaiki.ml.BlurImageModel
import com.capstone.ayoperbaiki.utils.Disaster
import com.capstone.ayoperbaiki.utils.GridPhotoAdapter
import com.capstone.ayoperbaiki.utils.Utils.EXTRA_DATA_ADDRESS
import com.capstone.ayoperbaiki.utils.Utils.IMAGE_FILE_FORMAT
import com.capstone.ayoperbaiki.utils.Utils.PERMISSION_REQUEST_CODE
import com.capstone.ayoperbaiki.utils.Utils.REQUIRED_PERMISSION
import com.capstone.ayoperbaiki.utils.Utils.getDateTime
import com.capstone.ayoperbaiki.utils.Utils.hide
import com.capstone.ayoperbaiki.utils.Utils.roundOffDecimal
import com.capstone.ayoperbaiki.utils.Utils.show
import com.google.firebase.Timestamp
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
@AndroidEntryPoint
class DisasterReportFormActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var binding: ActivityDisasterReportFormBinding
    private lateinit var bindingForm: DisasterReportFormBinding
    private lateinit var outputImagePath: String
    private lateinit var address: Address
    private var timeStamp : Timestamp? = null
    private val viewModel: ReportViewModel by viewModels()
    private lateinit var gridPhotoAdapter: GridPhotoAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisasterReportFormBinding.inflate(layoutInflater)
        bindingForm = binding.detailContent
        setContentView(binding.root)
        address = intent.extras?.getParcelable<Address>(EXTRA_DATA_ADDRESS) as Address
        initFormInputAdapter()
        showDataAddress()
        showRecyclerGrid()
        checkCameraPermission()
        setUpViewBehavior()
        observeSubmitReportProgressStatus()
        binding.btnBack.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
    }

    @SuppressLint("CheckResult")
    private fun setUpViewBehavior() {
        val descriptionStream = RxTextView.textChanges(bindingForm.inputKeterangan)
            .skipInitialValue()
            .map { description ->
                description.isEmpty()
            }
        descriptionStream.subscribe {
            showDescriptionExistAlert(it)
        }

        val disasterTypeStream = RxTextView.textChanges(bindingForm.inputKategoriBencana)
            .skipInitialValue()
            .map { disasterType ->
                Disaster.mapDisaster.getValue(7) == disasterType.toString()
            }
        disasterTypeStream.subscribe {
            showCustomDisasterType(it)
        }

        val damageTypeStream = RxTextView.textChanges(bindingForm.inputJenisKerusakan)
            .skipInitialValue()
            .map { damageType ->
                Disaster.mapTypeOfDamage.getValue(13) == damageType.toString()
            }
        damageTypeStream.subscribe{
            showCustomDamageType(it)
        }

        val customDisasterTypeStream = RxTextView.textChanges(bindingForm.inputKategoriBencanaLainnya)
            .skipInitialValue()
            .map { disasterType ->
                bindingForm.inputLayoutKategoriBencanaLainnya.isVisible && disasterType.isEmpty()
            }
        customDisasterTypeStream.subscribe {
            showCustomDisasterExistAlert(it)
        }

        val customDamageTypeStream = RxTextView.textChanges(bindingForm.inputJenisKerusakanLainnya)
            .skipInitialValue()
            .map { damageType ->
                bindingForm.inputLayoutJenisKerusakanLainnya.isVisible && damageType.isEmpty()
            }
        customDamageTypeStream.subscribe {
            showCustomDamageTypeExistAlert(it)
        }

        val disasterTimeStream = RxTextView.textChanges(bindingForm.inputWaktu)
            .skipInitialValue()
            .map { damageTime ->
                damageTime.isEmpty()
            }
        disasterTimeStream.subscribe {
            showDisasterTimeExistAlert(it)
        }

        val validDisasterTypeStream = Observable.merge(
            disasterTypeStream.map {!it},
            customDisasterTypeStream.map {!it}
        )

        val validDamageTypeStream = Observable.merge(
            damageTypeStream.map {!it},
            customDamageTypeStream.map {!it}
        )

        Observable.combineLatest(validDisasterTypeStream, validDamageTypeStream, disasterTimeStream, descriptionStream) {a, b, c, d ->
            a && b && !c && !d
        }.subscribe { isValid ->
            binding.btnSubmit.apply {
                if (isValid) {
                    isEnabled = true
                    setBackgroundColor(ContextCompat.getColor(this@DisasterReportFormActivity, R.color.ss_top))
                } else {
                    isEnabled = false
                    setBackgroundColor(ContextCompat.getColor(this@DisasterReportFormActivity, R.color.quantum_grey400))
                }
            }
        }
    }

    private fun showCustomDisasterType(isAnother: Boolean) {
        bindingForm.inputLayoutKategoriBencanaLainnya.apply {
            if (isAnother)
                show()
            else
                hide()
        }
    }

    private fun showCustomDamageType(isAnother: Boolean) {
        bindingForm.inputLayoutJenisKerusakanLainnya.apply {
            if (isAnother)
                show()
            else
                hide()
        }
    }

    private fun showDescriptionExistAlert(isNotValid: Boolean) {
        bindingForm.inputLayoutKeterangan.error = if (isNotValid) getString(R.string.required_text_field) else null
    }

    private fun showCustomDisasterExistAlert(isNotValid: Boolean) {
        bindingForm.inputLayoutKategoriBencanaLainnya.apply {
            if (isNotValid) {
                error = getString(R.string.required_text_field)
                isHelperTextEnabled = true
                isErrorEnabled = true
                errorIconDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_error,
                    null
                )
            } else {
                isErrorEnabled = false
                isHelperTextEnabled = false
            }
        }
    }

    private fun showCustomDamageTypeExistAlert(isNotValid: Boolean) {
        bindingForm.inputLayoutJenisKerusakanLainnya.apply {
            if (isNotValid) {
                error = getString(R.string.required_text_field)
                isHelperTextEnabled = true
                isErrorEnabled = true
                errorIconDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_error,
                    null
                )
            } else {
                isErrorEnabled = false
                isHelperTextEnabled = false
            }
        }
    }

    private fun showDisasterTimeExistAlert(isNotValid: Boolean) {
        bindingForm.inputLayoutWaktu.apply {
            if (isNotValid) {
                error = getString(R.string.required_text_field)
                isHelperTextEnabled = true
                isErrorEnabled = true
                errorIconDrawable = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_error,
                    null
                )
            } else {
                isErrorEnabled = false
                isHelperTextEnabled = false
            }
        }
    }

    private fun checkCameraPermission() {
        if(isPermissionGranted()){
            initAdapterListener()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE)
        }
    }

    private fun showDataAddress() {
        with(address) {
            bindingForm.inputAlamat.setText(address)
            bindingForm.inputKota.setText(city)
            bindingForm.inputProvinsi.setText(state)
            bindingForm.inputLatitude.setText(String.format("${latitude.roundOffDecimal()}°"))
            bindingForm.inputLongitude.setText(String.format("${longitude.roundOffDecimal()}°"))
        }
    }

    private fun initFormInputAdapter() {
        val disasterList = Disaster.generateDisaster() as ArrayList<String>
        val arrayAdapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            disasterList
        )
        bindingForm.inputKategoriBencana.setAdapter(arrayAdapter)

        val typeOfDamageList = Disaster.generateListTypeOfDamage() as ArrayList<String>
        val arrayAdapter2 = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            typeOfDamageList
        )
        bindingForm.inputJenisKerusakan.setAdapter(arrayAdapter2)

        bindingForm.inputWaktu.setOnClickListener {
            pickDateTime()
        }
    }

    private fun showRecyclerGrid() {
        bindingForm.rvImage.layoutManager = GridLayoutManager(this, 3)
        gridPhotoAdapter = GridPhotoAdapter()
        gridPhotoAdapter.setData(emptyList())
        initAdapterListener()
        bindingForm.rvImage.adapter = gridPhotoAdapter
    }

    private fun initAdapterListener() {
        gridPhotoAdapter.onButtonClick = {
            startCamera()
        }
        gridPhotoAdapter.onItemClick = {
            gridPhotoAdapter.deleteDataAtIndex(it)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return REQUIRED_PERMISSION.all {
            ContextCompat.checkSelfPermission(baseContext, it) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun startCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(
                        applicationContext,
                        "Error while saving picture.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d(TAG, "startCamera: Error create Image File : \n${ex.message}")
                    null
                }

                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.capstone.ayoperbaiki.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(
                        takePictureIntent,
                        CAPTURE_IMAGE_FROM_CAMERA_REQUEST_CODE
                    )
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat(IMAGE_FILE_FORMAT, Locale.getDefault()).format(
            System.currentTimeMillis()
        )
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            outputImagePath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(isPermissionGranted()){
            initAdapterListener()
        } else {
            Toast.makeText(this, getString(R.string.permission_denies_msg), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAPTURE_IMAGE_FROM_CAMERA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    val imgFile = File(outputImagePath)
                    if (imgFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(outputImagePath)
                        if (validateImage(bitmap)) {
                            val resultPhoto = Uri.fromFile(imgFile)
                            gridPhotoAdapter.addData(resultPhoto.toString())
                        } else {
                            Toast.makeText(
                                this@DisasterReportFormActivity,
                                "Gambar tampak blur. Mohon ambil gambar kembali!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.image_not_captured),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.failed_save_image),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun validateImage(bitmap: Bitmap?) : Boolean {
        val model = BlurImageModel.newInstance(this)
        val image = TensorImage.fromBitmap(bitmap)
        val outputs = model.process(image)
        val probability = outputs.probabilityAsCategoryList
        val output = probability.single { category ->  category.score > 0.5}
        model.close()

        // return true if image is valid, and false if not valid
        return when(output.label) {
            "Valid" -> true
            else -> false
        }
    }

    private fun submitReport() {
        var selectedDisasterType: String
        var selectedDamageType: String
        with(bindingForm) {
            val disasterType = inputKategoriBencana.text.toString().trim()
            val customDisasterType = inputKategoriBencanaLainnya.text.toString().trim()
            val damageType = inputJenisKerusakan.text.toString().trim()
            val customDamageType = inputJenisKerusakanLainnya.text.toString().trim()
            val disasterTime = timeStamp
            val disasterAddress = address
            val disasterDescription = inputKeterangan.text.toString().trim()

            selectedDisasterType = if (Disaster.mapDisaster.getValue(7) == disasterType) customDisasterType else disasterType
            selectedDamageType = if (Disaster.mapTypeOfDamage.getValue(13) == damageType) customDamageType else damageType

            Toast.makeText(this@DisasterReportFormActivity, "$selectedDisasterType \n$selectedDamageType \n$disasterTime $disasterAddress $disasterDescription", Toast.LENGTH_SHORT).show()

            //iterate upload image to storage one by one
            //save the return to a variable
            //make report object
            //upload to firebase storage
        }
    }

    private fun observeSubmitReportProgressStatus() {
        viewModel.submitReportStatus.observe(this, { status ->
            if (status != null) {
                when (status) {
                    is Resource.Failure -> {
                        // show the failed notification to user
                        // ask user to resubmit the data
                    }
                    is Resource.Loading -> {
                        // show the loading bar
                    }
                    is Resource.Success -> {
                        // show the success notification to user
                        // back to maps activity
                    }
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when(view) {
            binding.btnBack -> confirmBack()
            binding.btnSubmit -> {
                submitReport()
            }
        }
    }

    private fun confirmBack() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.bottom_to_top)
    }

    private fun pickDateTime() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day, hour, minute)
                timeStamp = Timestamp(Date(pickedDateTime.timeInMillis))
                bindingForm.inputWaktu.setText(getDateTime(timeStamp!!))
            }, startHour, startMinute, false).show()
        }, startYear, startMonth, startDay).show()
    }

    companion object{
        private val TAG = DisasterReportFormActivity::class.java.simpleName
        private const val CAPTURE_IMAGE_FROM_CAMERA_REQUEST_CODE = 100
    }
}