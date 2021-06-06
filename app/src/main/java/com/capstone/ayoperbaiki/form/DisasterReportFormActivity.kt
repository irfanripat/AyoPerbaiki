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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Address
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.databinding.ActivityDisasterReportFormBinding
import com.capstone.ayoperbaiki.databinding.DisasterReportFormBinding
import com.dicoding.picodiploma.myalarmmanager.utils.DatePickerFragment
import java.io.File
import java.io.IOException
import org.tensorflow.lite.support.image.TensorImage
import com.capstone.ayoperbaiki.ml.BlurImageModel
import com.capstone.ayoperbaiki.utils.Disaster
import com.capstone.ayoperbaiki.utils.GridPhotoAdapter
import com.capstone.ayoperbaiki.utils.Utils.EXTRA_DATA_ADDRESS
import com.capstone.ayoperbaiki.utils.Utils.IMAGE_FILE_FORMAT
import com.capstone.ayoperbaiki.utils.Utils.PERMISSION_REQUEST_CODE
import com.capstone.ayoperbaiki.utils.Utils.REQUIRED_PERMISSION
import com.capstone.ayoperbaiki.utils.Utils.hide
import com.capstone.ayoperbaiki.utils.Utils.roundOffDecimal
import com.capstone.ayoperbaiki.utils.Utils.show
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
@AndroidEntryPoint
class DisasterReportFormActivity : AppCompatActivity(), DatePickerFragment.DialogDateListener, View.OnClickListener{

    private lateinit var binding: ActivityDisasterReportFormBinding
    private lateinit var bindingForm: DisasterReportFormBinding
    private lateinit var outputImagePath: String
    private lateinit var address: Address
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

        val 

        binding.btnBack.setOnClickListener(this)
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
        val arrayAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, disasterList)
        bindingForm.inputKategoriBencana.setAdapter(arrayAdapter)

        val typeOfDamageList = Disaster.generateListTypeOfDamage() as ArrayList<String>
        val arrayAdapter2 = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, typeOfDamageList)
        bindingForm.inputJenisKerusakan.setAdapter(arrayAdapter2)
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
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_FROM_CAMERA_REQUEST_CODE)
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
                if(resultCode == RESULT_OK){
                    val imgFile = File(outputImagePath)
                    if (imgFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(outputImagePath)
                        if(validateImage(bitmap)){
                            val resultPhoto = Uri.fromFile(imgFile)
                            gridPhotoAdapter.addData(resultPhoto.toString())
                        } else {
                            Toast.makeText(this@DisasterReportFormActivity, "Gambar tampak blur. Mohon ambil gambar kembali!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.image_not_captured), Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(applicationContext, getString(R.string.failed_save_image), Toast.LENGTH_LONG).show()
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
//        bindingForm.edtWaktuBe ncana.setText(dateFormat.format(calendar.time))
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

    companion object{
        private val TAG = DisasterReportFormActivity::class.java.simpleName
        private const val CAPTURE_IMAGE_FROM_CAMERA_REQUEST_CODE = 100
    }

    override fun onClick(view: View?) {
        when(view) {
            binding.btnBack -> confirmBack()
        }
    }

    private fun confirmBack() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.bottom_to_top)
    }

    private fun processToSubmitData() {
        var tipeBencana: String = bindingForm.inputKategoriBencana.text.toString()
        var jenisKerusakan: String = bindingForm.inputJenisKerusakan.text.toString()
        var waktu: String = bindingForm.inputWaktu.text.toString()
        var description: String = bindingForm.inputKeterangan.text.toString()

        bindingForm.inputKategoriBencana.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            when {
                item == "Lainnya" -> {
                    bindingForm.inputLayoutKategoriBencanaLainnya.show()
                    bindingForm.inputKategoriBencanaLainnya.doOnTextChanged { text, _, _, _ ->
                        if(text != null) when {
                            text.isEmpty() -> {
                                bindingForm.inputLayoutKategoriBencanaLainnya.apply {
                                    error = getString(R.string.required_text_field)
                                    isHelperTextEnabled = true
                                    isErrorEnabled = true
                                    errorIconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_error, null)
                                }
                            }
                            text.isNotEmpty() -> {
                                bindingForm.inputLayoutKategoriBencanaLainnya.apply {
                                    isErrorEnabled = false
                                    isHelperTextEnabled = false
                                }
                            }
                        }
                        tipeBencana = bindingForm.inputKategoriBencanaLainnya.text.toString().trim()
                    }
                }
                item != "Lainnya" -> {
                    bindingForm.inputLayoutKategoriBencanaLainnya.hide()
                    tipeBencana = bindingForm.inputKategoriBencana.text.toString().trim()
                }
            }
        }

        bindingForm.inputJenisKerusakan.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            when {
                item == "Lainnya" -> {
                    bindingForm.inputLayoutJenisKerusakanLainnya.show()
                    bindingForm.inputJenisKerusakanLainnya.doOnTextChanged { text, _, _, _ ->
                        if(text != null) when {
                            text.isEmpty() -> {
                                bindingForm.inputLayoutJenisKerusakanLainnya.apply {
                                    error = getString(R.string.required_text_field)
                                    isHelperTextEnabled = true
                                    isErrorEnabled = true
                                    errorIconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_error, null)
                                }
                            }
                            text.isNotEmpty() -> {
                                bindingForm.inputLayoutJenisKerusakanLainnya.apply {
                                    isErrorEnabled = false
                                    isHelperTextEnabled = false
                                }
                            }
                        }
                        jenisKerusakan = bindingForm.inputJenisKerusakanLainnya.text.toString().trim()
                    }
                }
                item != "Lainnya" -> {
                    bindingForm.inputLayoutJenisKerusakanLainnya.hide()
                    jenisKerusakan = bindingForm.inputJenisKerusakanLainnya.text.toString().trim()
                }
            }
        }

//        bindingForm.actvWaktuBencana.setEndIconOnClickListener {
//            Toast.makeText(this, "TestEndIcon", Toast.LENGTH_SHORT).show()
//            val datePickerFragment = DatePickerFragment()
//            datePickerFragment.show(supportFragmentManager, DATE_PICKER_TAG)
//
//            waktu = bindingForm.edtWaktuBencana.text.toString().trim()
//        }
//
        bindingForm.inputKeterangan.doOnTextChanged { text, _, _, _ ->
            if(text != null) when {
                text.isEmpty() -> {
                    bindingForm.inputLayoutKeterangan.apply {
                        isHelperTextEnabled = true
                        isErrorEnabled = true
                        error = getString(R.string.required_text_field)
                        errorIconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_error, null)
                    }
                }
                text.isNotEmpty() -> {
                    bindingForm.inputLayoutKeterangan.apply {
                        isErrorEnabled = false
                        isHelperTextEnabled = false
                    }

                    description = bindingForm.inputKeterangan.text.toString().trim()
                }
            }
        }

//        bindingForm.btnSubmitForm.setOnClickListener {
//            if (tipeBencana.isNotBlank() && jenisKerusakan.isNotBlank() && waktu.isNotBlank() && description.isNotBlank() && linkDonasi.isNotBlank() && capturedImage.isNotBlank() && capturedImage2.isNotBlank() && capturedImage3.isNotBlank()) {
//                //convert string to timestamp
//                //submit the data
//            }
//        }
    }
}