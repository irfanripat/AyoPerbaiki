package com.dicoding.picodiploma.myalarmmanager.utils


import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.capstone.ayoperbaiki.R
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var mListener: DialogDateListener? = null

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: isi context DatePicker $context")
        mListener = context as DialogDateListener?

        super.onAttach(context)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        Log.d(TAG, "onDataSet: akan terpanggil ketika memilih tanggal $tag")
        mListener?.onDialogDateSet(tag, year, month, dayOfMonth)
    }

    override fun onDetach() {
        if(mListener != null) mListener = null
        super.onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)

        return DatePickerDialog(activity as Context, this, year, month, date)

    }

    interface DialogDateListener {
        fun onDialogDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int)
    }

    companion object{
        val TAG: String = DatePickerFragment::class.java.simpleName
    }
}
