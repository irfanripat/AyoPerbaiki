package com.capstone.ayoperbaiki.form

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.core.domain.model.Address
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.databinding.ActivityDisasterReportFormBinding
import com.capstone.ayoperbaiki.databinding.DisasterReportFormBinding
import com.capstone.ayoperbaiki.utils.gone
import com.capstone.ayoperbaiki.utils.visible
import com.dicoding.picodiploma.myalarmmanager.utils.DatePickerFragment
import java.io.File
import java.io.IOException
import org.tensorflow.lite.support.image.TensorImage
import com.capstone.ayoperbaiki.ml.BlurImageModel
import com.capstone.ayoperbaiki.utils.Utils.DATE_PICKER_TAG
import com.capstone.ayoperbaiki.utils.Utils.EXTRA_DATA_ADDRESS
import com.capstone.ayoperbaiki.utils.Utils.IMAGE_FILE_FORMAT
import com.capstone.ayoperbaiki.utils.Utils.PERMISSION_REQUEST_CODE
import com.capstone.ayoperbaiki.utils.Utils.REQUIRED_PERMISSION
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DisasterReportFormActivity
    : AppCompatActivity(), DatePickerFragment.DialogDateListener{

    private lateinit var binding: ActivityDisasterReportFormBinding
    private lateinit var bindingForm: DisasterReportFormBinding
    private lateinit var outputImagePath: String
    private lateinit var address: Address
    private lateinit var capturedImage: String
    private lateinit var capturedImage2: String
    private lateinit var capturedImage3: String
    private val viewModel: ReportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisasterReportFormBinding.inflate(layoutInflater)
        bindingForm = binding.detailContent
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Disaster Report Form"
            elevation = 4f
        }

        if(isPermissionGranted()){
            initCamera()
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE)
        }

        val data = intent.extras
        if(data != null){
            address = data.getParcelable<Address>(EXTRA_DATA_ADDRESS) as Address
        }

        initForm()
    }

    override fun onResume() {
        super.onResume()
        val disasterList = resources.getStringArray(R.array.list_disaster)
        val arrayAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, disasterList)
        bindingForm.edtTipeBencana.setAdapter(arrayAdapter)
    }

    private fun initForm() {
        var tipeBencana: String = bindingForm.edtTipeBencana.text.toString()
        var jenisKerusakan: String = bindingForm.edtTipeKerusakanInfrastruktur.text.toString()
        var waktu: String = bindingForm.edtWaktuBencana.text.toString()
        var description: String = bindingForm.edtKeteranganBencana.text.toString()
        var linkDonasi: String = bindingForm.edtLinkDonasi.text.toString()
        val addressDetail = "${address.city}, ${address.country}, Kode Pos ${address.postalCode} (Latitude: ${address.latitude}, Longitude ${address.longitude})"

        bindingForm.edtTipeBencana.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            when {
                item == "Lainnya" -> {
                    bindingForm.actvTipeBencana2.visible()
                    bindingForm.edtTipeBencana2.doOnTextChanged { text, _, _, _ ->
                        if(text != null) when {
                            text.isEmpty() -> {
                                bindingForm.actvTipeBencana2.apply {
                                    error = getString(R.string.required_text_field)
                                    isHelperTextEnabled = true
                                    isErrorEnabled = true
                                    errorIconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_error, null)
                                }
                            }
                            text.isNotEmpty() -> {
                                bindingForm.actvTipeBencana2.apply {
                                    isErrorEnabled = false
                                    isHelperTextEnabled = false
                                }
                            }
                        }
                        tipeBencana = bindingForm.edtTipeBencana2.text.toString().trim()
                    }
                }
                item != "Lainnya" -> {
                    bindingForm.actvTipeBencana2.gone()
                    tipeBencana = bindingForm.edtTipeBencana.text.toString().trim()
                }
            }
        }

        bindingForm.edtTipeKerusakanInfrastruktur.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            when {
                item == "Lainnya" -> {
                    bindingForm.actvTipeKerusakanInfrastruktur2.visible()
                    bindingForm.edtTipeKerusakanInfrastruktur2.doOnTextChanged { text, _, _, _ ->
                        if(text != null) when {
                            text.isEmpty() -> {
                                bindingForm.actvTipeKerusakanInfrastruktur2.apply {
                                    error = getString(R.string.required_text_field)
                                    isHelperTextEnabled = true
                                    isErrorEnabled = true
                                    errorIconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_error, null)
                                }
                            }
                            text.isNotEmpty() -> {
                                bindingForm.actvTipeKerusakanInfrastruktur2.apply {
                                    isErrorEnabled = false
                                    isHelperTextEnabled = false
                                }
                            }
                        }
                        jenisKerusakan = bindingForm.edtTipeKerusakanInfrastruktur2.text.toString().trim()
                    }
                }
                item != "Lainnya" -> {
                    bindingForm.actvTipeKerusakanInfrastruktur2.gone()
                    jenisKerusakan = bindingForm.edtTipeKerusakanInfrastruktur2.text.toString().trim()
                }
            }
        }

        bindingForm.actvWaktuBencana.setEndIconOnClickListener {
            Toast.makeText(this, "TestEndIcon", Toast.LENGTH_SHORT).show()
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.show(supportFragmentManager, DATE_PICKER_TAG)

            waktu = bindingForm.edtWaktuBencana.text.toString().trim()
        }

        bindingForm.edtLokasiBencana.apply {
            setText(addressDetail)
        }
        
        bindingForm.edtKeteranganBencana.doOnTextChanged { text, _, _, _ ->
            if(text != null) when {
                text.isEmpty() -> {
                    bindingForm.actvKeteranganBencana.apply {
                        isHelperTextEnabled = true
                        isErrorEnabled = true
                        error = getString(R.string.required_text_field)
                        errorIconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_error, null)
                    }
                }
                text.isNotEmpty() -> {
                    bindingForm.actvKeteranganBencana.apply {
                        isErrorEnabled = false
                        isHelperTextEnabled = false
                    }

                    description = bindingForm.edtKeteranganBencana.text.toString().trim()
                }
            }
        }

        bindingForm.edtLinkDonasi.doOnTextChanged { text, _, _, _ ->
            if(text != null) {
                linkDonasi = bindingForm.edtLinkDonasi.text.toString().trim()
            }
        }

        bindingForm.btnSubmitForm.setOnClickListener {
            // TODO: 04/06/2021 Membuat validasi form dan kirim data ke firebase

//            val resultForm = Report(
//                address = address,
//                description = description,
//                photoUri = capturedImage
//            )
        }

        Log.d(TAG, "initEditText: Isi data form\n$tipeBencana $jenisKerusakan $waktu $addressDetail $description $linkDonasi")
    }

    private fun initCamera() {
        bindingForm.btnOpenCamera.setOnClickListener {
            startCamera(CAPTURE_IMAGE_REQUEST_CODE)
        }
        bindingForm.btnOpenCamera2.setOnClickListener {
            startCamera(CAPTURE_IMAGE2_REQUEST_CODE)
        }
        bindingForm.btnOpenCamera3.setOnClickListener {
            startCamera(CAPTURE_IMAGE3_REQUEST_CODE)
        }
    }


    private fun isPermissionGranted(): Boolean {
        return REQUIRED_PERMISSION.all {
            ContextCompat.checkSelfPermission(baseContext, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startCamera(requestCode: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {

                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(applicationContext, "Error while saving picture.", Toast.LENGTH_LONG).show()
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
                    startActivityForResult(takePictureIntent, requestCode)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat(IMAGE_FILE_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(isPermissionGranted()){
            initCamera()
        } else {
            Toast.makeText(this, "Permission camera has been denied by user", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAPTURE_IMAGE_REQUEST_CODE -> {
                if(resultCode == RESULT_OK){
                    //Untuk mengambil hasil gambar full berdasarkan path
                    val imgFile = File(outputImagePath)
                    if (imgFile.exists()) {

                        //validasi gambar blue atau tidak
                        val bitmap = BitmapFactory.decodeFile(outputImagePath)
                        if(validateImage(bitmap)){
                            val resultPhoto = Uri.fromFile(imgFile)
                            capturedImage = resultPhoto.toString()
                            bindingForm.imgKerusakanInfrastruktur.setImageURI(resultPhoto)
                        } else {
                            Toast.makeText(this@DisasterReportFormActivity, "Gambar tampak blur. Mohon ambil gambar kembali!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Error: Image not captured", Toast.LENGTH_LONG).show()
                    }

                } else {
                    bindingForm.imgKerusakanInfrastruktur.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.placeholder_image, null))
                    Toast.makeText(applicationContext, "Error while saving picture.", Toast.LENGTH_LONG).show()
                }
            }
            CAPTURE_IMAGE2_REQUEST_CODE -> {
                if(resultCode == RESULT_OK){
                    //Untuk mengambil hasil gambar full berdasarkan path
                    val imgFile = File(outputImagePath)
                    if (imgFile.exists()) {
                        //validasi gambar blue atau tidak
                        val bitmap = BitmapFactory.decodeFile(outputImagePath)
                        if(validateImage(bitmap)){
                            val resultPhoto = Uri.fromFile(imgFile)
                            capturedImage2 = resultPhoto.toString()
                            bindingForm.imgKerusakanInfrastruktur2.setImageURI(resultPhoto)
                        } else {
                            Toast.makeText(this@DisasterReportFormActivity, "Gambar tampak blur. Mohon ambil gambar kembali!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Error: Image not captured", Toast.LENGTH_LONG).show()
                    }
                } else {
                    bindingForm.imgKerusakanInfrastruktur2.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.placeholder_image, null))
                }
            }
            CAPTURE_IMAGE3_REQUEST_CODE -> {
                if(resultCode == RESULT_OK){
                    //Untuk mengambil hasil gambar full berdasarkan path
                    val imgFile = File(outputImagePath)
                    if (imgFile.exists()) {
                        //validasi gambar blue atau tidak
                        val bitmap = BitmapFactory.decodeFile(outputImagePath)
                        if(validateImage(bitmap)){
                            val resultPhoto = Uri.fromFile(imgFile)
                            capturedImage3 = resultPhoto.toString()
                            bindingForm.imgKerusakanInfrastruktur3.setImageURI(resultPhoto)
                        } else {
                            Toast.makeText(this@DisasterReportFormActivity, "Gambar tampak blur. Mohon ambil gambar kembali!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Error: Image not captured", Toast.LENGTH_LONG).show()
                    }

                } else {
                    bindingForm.imgKerusakanInfrastruktur3.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.placeholder_image, null))
                    Toast.makeText(applicationContext, "Error while saving picture.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    override fun onDialogDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)

        //Tuesday, 01 January 2021 12:30:00
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm:ss", Locale.getDefault())

        //Set text dari textview once
        bindingForm.edtWaktuBencana.setText(dateFormat.format(calendar.time))
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

    private fun submitReport(report: Report) {
        viewModel.submitReport(report)

    }

    companion object{
        private val TAG = DisasterReportFormActivity::class.java.simpleName
        private const val CAPTURE_IMAGE_REQUEST_CODE = 101
        private const val CAPTURE_IMAGE2_REQUEST_CODE = 102
        private const val CAPTURE_IMAGE3_REQUEST_CODE = 103
    }
}