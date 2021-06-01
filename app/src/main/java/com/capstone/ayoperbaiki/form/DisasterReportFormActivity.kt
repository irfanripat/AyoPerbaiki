package com.capstone.ayoperbaiki.form

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.databinding.ActivityDisasterReportFormBinding
import com.capstone.ayoperbaiki.databinding.DisasterReportFormBinding
import com.capstone.ayoperbaiki.utils.gone
import com.capstone.ayoperbaiki.utils.visible
import com.dicoding.picodiploma.myalarmmanager.utils.DatePickerFragment
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

        initEditText()
    }

    override fun onResume() {
        super.onResume()
        val disasterList = resources.getStringArray(R.array.list_disaster)
        val arrayAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, disasterList)
        bindingForm.edtTipeBencana.setAdapter(arrayAdapter)
    }


    private fun initEditText() {
        var tipeBencana: String = bindingForm.edtTipeBencana.text.toString() //sudah
        var waktuBencana: String = bindingForm.edtWaktuBencana.text.toString() //sudah
        var lokasiBencana: String = bindingForm.edtLokasiBencana.text.toString() //sudah
        var keteranganBencana: String = bindingForm.edtKeteranganBencana.text.toString() //sudah
        var gambarBencana: Bitmap? = null
        var linkDonasi: String = bindingForm.edtLinkDonasi.text.toString()

        bindingForm.edtTipeBencana.setOnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position).toString()
            when {
                item == "Lainnya" -> {
                    bindingForm.actvTipeBencana2.visible()
                    bindingForm.edtTipeBencana2.doOnTextChanged { text, start, before, count ->
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

        bindingForm.edtLokasiBencana.doOnTextChanged { text, start, before, count ->
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

        bindingForm.edtKeteranganBencana.doOnTextChanged { text, start, before, count ->
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

        bindingForm.edtLinkDonasi.doOnTextChanged { text, start, before, count ->
            if(text != null) {
                linkDonasi = bindingForm.edtLinkDonasi.text.toString().trim()
            }
        }

        bindingForm.btnSubmitForm.setOnClickListener {

        }

        Log.d(TAG, "initEditText: Isi data form\n$tipeBencana $waktuBencana $lokasiBencana $keteranganBencana $linkDonasi")
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
        private const val DATE_PICKER_TAG = "DatePicker"

    }

}