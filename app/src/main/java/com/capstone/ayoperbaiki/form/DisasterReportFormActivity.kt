package com.capstone.ayoperbaiki.form

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.databinding.ActivityDisasterReportFormBinding
import com.capstone.ayoperbaiki.databinding.DisasterReportFormBinding
import com.capstone.ayoperbaiki.utils.Disaster
import com.capstone.ayoperbaiki.utils.gone
import com.capstone.ayoperbaiki.utils.visible
import com.dicoding.picodiploma.myalarmmanager.utils.DatePickerFragment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DisasterReportFormActivity
    : AppCompatActivity(), DatePickerFragment.DialogDateListener{

    private lateinit var binding: ActivityDisasterReportFormBinding
    private lateinit var bindingForm: DisasterReportFormBinding
    private lateinit var outputImagePath: String
    private lateinit var capturedImage: String
    private lateinit var capturedImage1: String
    private lateinit var capturedImage2: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisasterReportFormBinding.inflate(layoutInflater)
        bindingForm = binding.detailContent
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Formulir Kerusakan Infrastruktur"
            elevation = 4f
        }

        if(isPermissionGranted()){
            initCamera()
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE)
        }

        initForm()
    }

    override fun onResume() {
        super.onResume()

        //Init dropdown kategori bencana
        val disasterList = Disaster.generateDisaster() as ArrayList<String>
        disasterList.add("Lainnya")
        Log.d(TAG, "onResume: list disaster $disasterList")
        val arrayAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, disasterList)
        bindingForm.edtTipeBencana.setAdapter(arrayAdapter)

        //Init dropdown tipe kerusakan infrastruktur
        val kerusakanList = Disaster.generateKerusakanInfrastruktur2() as ArrayList<String>
        kerusakanList.add("Lainnya")
        Log.d(TAG, "onResume: list disaster $kerusakanList")
        val arrayAdapter2 = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, kerusakanList)
        bindingForm.edtTipeKerusakanInfrastruktur.setAdapter(arrayAdapter2)
    }

    private fun initForm() {
        var tipeBencana: String = bindingForm.edtTipeBencana.text.toString() //sudah
        var tipeKerusakanInfrastruktur: String = bindingForm.edtTipeKerusakanInfrastruktur.text.toString() //sudah
        var waktuBencana: String = bindingForm.edtWaktuBencana.text.toString() //sudah
        var lokasiBencana: String = bindingForm.edtLokasiBencana.text.toString() //sudah
        var keteranganBencana: String = bindingForm.edtKeteranganBencana.text.toString() //sudah
        var linkDonasi: String = bindingForm.edtLinkDonasi.text.toString()

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
                        tipeKerusakanInfrastruktur = bindingForm.edtTipeKerusakanInfrastruktur2.text.toString().trim()
                    }
                }
                item != "Lainnya" -> {
                    bindingForm.actvTipeKerusakanInfrastruktur2.gone()
                    tipeKerusakanInfrastruktur = bindingForm.edtTipeKerusakanInfrastruktur.text.toString().trim()
                }
            }
        }

        bindingForm.actvWaktuBencana.setEndIconOnClickListener {
            Toast.makeText(this, "TestEndIcon", Toast.LENGTH_SHORT).show()
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.show(supportFragmentManager, DATE_PICKER_TAG)

            waktuBencana = bindingForm.edtWaktuBencana.text.toString().trim()
        }

        bindingForm.edtLokasiBencana.doOnTextChanged { text, _, _, _ ->
            if(text != null) when {
                text.isEmpty() -> {
                    bindingForm.actvLokasiBencana.apply {
                        isErrorEnabled = true
                        isHelperTextEnabled = true
                        error = getString(R.string.required_text_field)
                        errorIconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_error, null)
                    }
                }
                text.isNotEmpty() -> {
                    bindingForm.actvLokasiBencana.apply {
                        isErrorEnabled = false
                        isHelperTextEnabled = false
                    }
                    lokasiBencana = bindingForm.edtLokasiBencana.text.toString().trim()
                }
            }
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

                    keteranganBencana = bindingForm.edtKeteranganBencana.text.toString().trim()
                }
            }
        }

        bindingForm.edtLinkDonasi.doOnTextChanged { text, _, _, _ ->
            if(text != null) {
                linkDonasi = bindingForm.edtLinkDonasi.text.toString().trim()
            }
        }

        bindingForm.btnSubmitForm.setOnClickListener {

        }

        Log.d(TAG, "initEditText: Isi data form\n$tipeBencana $tipeKerusakanInfrastruktur $waktuBencana $lokasiBencana $keteranganBencana $linkDonasi")
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
                    Toast.makeText(applicationContext, "Error while saving picture.", Toast.LENGTH_LONG).show();
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
                    val imgFile: File = File(outputImagePath)
                    if (imgFile.exists()) {
                        bindingForm.imgKerusakanInfrastruktur.setImageURI(Uri.fromFile(imgFile))
                    }

                } else {
                    bindingForm.imgKerusakanInfrastruktur.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.placeholder_image, null))
                }
            }
            CAPTURE_IMAGE2_REQUEST_CODE -> {

            }
            CAPTURE_IMAGE3_REQUEST_CODE -> {

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


    companion object{
        private val TAG = DisasterReportFormActivity::class.java.simpleName
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private const val DATE_PICKER_TAG = "DatePicker"
        private const val IMAGE_FILE_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
        private const val PERMISSION_REQUEST_CODE = 100
        private const val CAPTURE_IMAGE_REQUEST_CODE = 101
        private const val CAPTURE_IMAGE2_REQUEST_CODE = 102
        private const val CAPTURE_IMAGE3_REQUEST_CODE = 103
    }

}