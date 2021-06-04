package com.capstone.ayoperbaiki.form

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.databinding.ActivityDisasterReportFormBinding
import com.capstone.ayoperbaiki.databinding.DisasterReportFormBinding
import com.capstone.ayoperbaiki.ml.BlurImageModel
import com.capstone.ayoperbaiki.utils.gone
import com.capstone.ayoperbaiki.utils.visible
import com.dicoding.picodiploma.myalarmmanager.utils.DatePickerFragment
import org.tensorflow.lite.support.image.TensorImage
import java.text.SimpleDateFormat
import java.util.*

class DisasterReportFormActivity : AppCompatActivity(), DatePickerFragment.DialogDateListener{

    private lateinit var binding: ActivityDisasterReportFormBinding
    private lateinit var bindingForm: DisasterReportFormBinding

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

        initForm()
        initCamera()
    }

    override fun onResume() {
        super.onResume()
        val disasterList = resources.getStringArray(R.array.list_disaster)
        val arrayAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, disasterList)
        bindingForm.edtTipeBencana.setAdapter(arrayAdapter)
    }


    private fun initForm() {
        var tipeBencana: String = bindingForm.edtTipeBencana.text.toString() //sudah
        var waktuBencana: String = bindingForm.edtWaktuBencana.text.toString() //sudah
        var lokasiBencana: String = bindingForm.edtLokasiBencana.text.toString() //sudah
        var keteranganBencana: String = bindingForm.edtKeteranganBencana.text.toString() //sudah
        var gambarBencana: Bitmap? = null
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

        Log.d(TAG, "initEditText: Isi data form\n$tipeBencana $waktuBencana $lokasiBencana $keteranganBencana $linkDonasi")
    }

    private fun initCamera() {

        bindingForm.btnOpenCamera.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, CAPTURE_IMAGE_REQUEST_CODE)

            }


            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAPTURE_IMAGE_REQUEST_CODE -> {
                val capturedImage = data?.extras?.get("data") as? Bitmap
                Log.d(TAG, "onActivityResult: hasil bitmap $capturedImage")
                if(capturedImage != null) {
                    bindingForm.imgDisaster.setImageBitmap(capturedImage)
                    val result = validateImage(capturedImage)
                    Log.d("RESULT", result.toString())
                } else {
                    bindingForm.imgDisaster.setImageDrawable(resources.getDrawable(R.drawable.placeholder_image))
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



    companion object{
        private val TAG = DisasterReportFormActivity::class.java.simpleName
        private const val DATE_PICKER_TAG = "DatePicker"
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private const val CAPTURE_IMAGE_REQUEST_CODE = 101
    }

}


