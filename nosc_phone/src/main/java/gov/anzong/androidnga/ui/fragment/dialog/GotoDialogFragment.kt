package gov.anzong.androidnga.ui.fragment.dialog

import gov.anzong.androidnga.R
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.os.Bundle
import android.content.Intent
import sp.phone.util.ActivityUtils
import android.app.Activity
import android.app.Dialog
import android.view.View
import androidx.appcompat.app.AlertDialog

class GotoDialogFragment : NoframeDialogFragment() {

    var mNumberPicker: NumberPicker? = null
    var mRadioGroup: RadioGroup? = null
    private var mMaxFloor = 0
    private var currPage = 0
    private var mMaxPage = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        val bundle = arguments
        mMaxPage = bundle?.getInt("page", 1) ?: 1
        mMaxFloor = bundle?.getInt("floor", mMaxPage) ?: mMaxPage
        currPage = bundle?.getInt("currPage",1) ?: 1
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.goto_floor_description)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                mNumberPicker?.apply {
                    clearFocus()
                    val intent = Intent()
                    dismiss()
                    if (mRadioGroup?.checkedRadioButtonId == R.id.page) {
                        intent.putExtra("page", value - 1)
                    } else {
                        intent.putExtra("floor", value)
                    }
                    targetFragment?.onActivityResult(
                        ActivityUtils.REQUEST_CODE_JUMP_PAGE,
                        Activity.RESULT_OK,
                        intent
                    )
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private val dialogView: View
        get() {
            val view = requireActivity().layoutInflater.inflate(R.layout.dialog_page_picker, null, false)
            mNumberPicker = view.findViewById(R.id.numberPicker)
            mRadioGroup = view.findViewById(R.id.radioGroup)
            mRadioGroup?.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId == R.id.page) {
                    initPagePicker()
                } else {
                    initFloorPicker()
                }
            }
            initPagePicker()
            return view
        }

    private fun initPagePicker() {
        mNumberPicker?.maxValue = mMaxPage
        mNumberPicker?.minValue = 1
        mNumberPicker?.value = currPage + 1
    }

    private fun initFloorPicker() {
        mNumberPicker?.maxValue = mMaxFloor - 1
        mNumberPicker?.minValue = 0
        mNumberPicker?.value = mMaxFloor - 1
    }
}