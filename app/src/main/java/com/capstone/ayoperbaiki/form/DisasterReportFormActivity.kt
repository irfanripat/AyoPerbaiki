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
import com.capstone.ayoperbaiki.core.domain.model.Disaster
import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.capstone.ayoperbaiki.databinding.ActivityDisasterReportFormBinding
import com.capstone.ayoperbaiki.databinding.DisasterReportFormBinding
import com.capstone.ayoperbaiki.ml.BlurImageModel
import com.capstone.ayoperbaiki.utils.Utils.DATE_PICKER_TAG
import com.capstone.ayoperbaiki.utils.Utils.EXTRA_DATA_ADDRESS
import com.capstone.ayoperbaiki.utils.Utils.IMAGE_FILE_FORMAT
import com.capstone.ayoperbaiki.utils.Utils.PERMISSION_REQUEST_CODE
import com.capstone.ayoperbaiki.utils.Utils.REQUIRED_PERMISSION
import com.capstone.ayoperbaiki.utils.gone
import com.capstone.ayoperbaiki.utils.visible
import com.dicoding.picodiploma.myalarmmanager.utils.DatePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.support.image.TensorImage
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

typealias DisasterUtils = com.capstone.ayoperbaiki.utils.Disaster

@AndroidEntryPoint
class DisasterReportFormActivity
    : AppCompatActivity(), DatePickerFragment.DialogDateListener{

    private lateinit var binding: ActivityDisasterReportFormBinding
    private lateinit var bindingForm: DisasterReportFormBinding
    private lateinit var outputImagePath: String
    private lateinit var address: Address
    private val viewModel : ReportViewModel by viewModels()
    private var capturedImage: String? = null
    private var capturedImage2: String? = null
    private var capturedImage3: String? = null
    private var waktuKejadian: String? = null
    private var isImageValid = false

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

        //Init dropdown kategori bencana
        val disasterList = DisasterUtils.generateDisaster() as ArrayList<String>
        disasterList.add("Lainnya")
        Log.d(TAG, "onResume: list disaster $disasterList")
        val arrayAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, disasterList)
        bindingForm.edtTipeBencana.setAdapter(arrayAdapter)

        //Init dropdown tipe kerusakan infrastruktur
        val kerusakanList = DisasterUtils.generateKerusakanInfrastruktur2() as ArrayList<String>
        kerusakanList.add("Lainnya")
        Log.d(TAG, "onResume: list disaster $kerusakanList")
        val arrayAdapter2 = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, kerusakanList)
        bindingForm.edtTipeKerusakanInfrastruktur.setAdapter(arrayAdapter2)
    }

    private fun initForm() {
        var tipeBencana: String = bindingForm.edtTipeBencana.text.toString()
        var idTipeBencana: Int = 0
        var jenisKerusakan: String = bindingForm.edtTipeKerusakanInfrastruktur.text.toString()
        var idJenisKerusakan: Int = 0
        val addressDetail = "${address.knownName}, ${address.city}, ${address.country}, Kode Pos ${address.postalCode} (Latitude: ${address.latitude}, Longitude ${address.longitude})"
        var description: String = bindingForm.edtKeteranganBencana.text.toString()
        var linkDonasi: String = bindingForm.edtLinkDonasi.text.toString()

        bindingForm.edtTipeBencana.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            idTipeBencana = position + 1
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
            idJenisKerusakan = position + 1
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
                    jenisKerusakan = bindingForm.edtTipeKerusakanInfrastruktur.text.toString().trim()
                }
            }
        }

        //data belum terambil dengan baik
        bindingForm.actvWaktuBencana.setEndIconOnClickListener {
            Toast.makeText(this, "TestEndIcon", Toast.LENGTH_SHORT).show()
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.show(supportFragmentManager, DATE_PICKER_TAG)
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
            when{
//                capturedImage.isNullOrEmpty() && capturedImage2.isNullOrEmpty() && capturedImage3.isNullOrEmpty() -> {
                capturedImage.isNullOrEmpty() -> {
                    Toast.makeText(
                        this@DisasterReportFormActivity,
                        "Mohon lengkapi data gambar infrastruktur yang rusak",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                !isImageValid -> {
                    Toast.makeText(
                        this@DisasterReportFormActivity,
                        "Gambar masih tampak blur. Mohon ambil gambar kembali!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                tipeBencana.isEmpty() -> {
                    Toast.makeText(this@DisasterReportFormActivity, "Form data belum lengkap 1", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                jenisKerusakan.isEmpty() -> {
                    Toast.makeText(this@DisasterReportFormActivity, "Form data belum lengkap 2", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                waktuKejadian.isNullOrEmpty() -> {
                    Toast.makeText(this@DisasterReportFormActivity, "Form data belum lengkap 3", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                addressDetail.isEmpty() -> {
                    Toast.makeText(this@DisasterReportFormActivity, "Form data belum lengkap 4", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                description.isEmpty() -> {
                    Toast.makeText(this@DisasterReportFormActivity, "Form data belum lengkap 5", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else -> {
                    Toast.makeText(this@DisasterReportFormActivity, "Form data sudah lengkap", Toast.LENGTH_SHORT).show()

                    val feedback = Feedback(status = false, description = description)
                    val disaster = Disaster(
                        id = idTipeBencana,
                        disasterName = tipeBencana
                    )
/*
                    val resultForm = Report(

                    )

                    viewModel.setResultOfReportForm(resultForm)
                    val statusInsertingReport = viewModel.insertFormReport()*/

                    //Upload image
//                    val listUriImage = listOf(capturedImage!!, capturedImage2!!, capturedImage3!!)
                    //test upload one image
                    val listUriImage = listOf(capturedImage!!)
                    viewModel.uploadImage(listUriImage)
                }
            }
        }

        Log.d(TAG, "initEditText: Isi data form\n$tipeBencana $jenisKerusakan $waktuKejadian $addressDetail $description $linkDonasi")
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
                    Log.d(TAG, "onActivityResult: isi outputimagepath $outputImagePath lenght ${imgFile.length()}")
                    if (imgFile.exists()) {
                        val resultPhoto = Uri.fromFile(imgFile)
                        Log.d(TAG, "onActivityResult: isi Uri image 1 $resultPhoto")

                        capturedImage = resultPhoto.toString()
                        bindingForm.imgKerusakanInfrastruktur.setImageURI(resultPhoto)

                        //validasi gambar blur atau tidak
                        val bitmap = BitmapFactory.decodeFile(outputImagePath)
                        Log.d(TAG, "onActivityResult: ukuran foto fullnya width ${bitmap.height} height ${bitmap.width}")
                        setValueImageIsValidOrNot(bitmap)

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
                        val resultPhoto = Uri.fromFile(imgFile)
                        capturedImage2 = resultPhoto.toString()
                        bindingForm.imgKerusakanInfrastruktur2.setImageURI(resultPhoto)

                        //validasi gambar blur atau tidak
                        val bitmap = BitmapFactory.decodeFile(outputImagePath)
                        setValueImageIsValidOrNot(bitmap)

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
                        val resultPhoto = Uri.fromFile(imgFile)
                        capturedImage3 = resultPhoto.toString()
                        bindingForm.imgKerusakanInfrastruktur3.setImageURI(resultPhoto)

                        //validasi gambar blur atau tidak
                        val bitmap = BitmapFactory.decodeFile(outputImagePath)
                        setValueImageIsValidOrNot(bitmap)

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
        val time = dateFormat.format(calendar.time)
        bindingForm.edtWaktuBencana.setText(time)
        waktuKejadian = time
    }

    private fun setValueImageIsValidOrNot(bitmap: Bitmap){
        if(validateImage(bitmap)){
            isImageValid = true
        } else {
            isImageValid = false
            Toast.makeText(this@DisasterReportFormActivity, "Gambar tampak blur. Mohon ambil gambar kembali!", Toast.LENGTH_SHORT).show()
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

    companion object{
        private val TAG = DisasterReportFormActivity::class.java.simpleName
        private const val CAPTURE_IMAGE_REQUEST_CODE = 101
        private const val CAPTURE_IMAGE2_REQUEST_CODE = 102
        private const val CAPTURE_IMAGE3_REQUEST_CODE = 103
    }
}