package gov.anzong.androidnga.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import android.view.View;
import android.widget.Button;


import nosc.utils.uxUtils.ToastUtils;
import sp.phone.util.NLog;

/**
 * Created by Justwen on 2018/2/16.
 */

public abstract class BaseDialogFragment extends DialogFragment implements View.OnClickListener {

    public void showToast(String toast) {
        if (getContext() != null) {
            ToastUtils.flat(toast);
        }
    }

    public void show(FragmentManager fm) {
        show(fm, getClass().getSimpleName());
    }

    public static void show(FragmentManager fm, Bundle args, Class<?> target) {
        try {
            DialogFragment df = (DialogFragment) target.newInstance();
            df.setArguments(args);
            df.show(fm, target.getSimpleName());
        } catch (IllegalAccessException | java.lang.InstantiationException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    protected boolean onPositiveClick() {
        return false;
    }

    @Override
    public void onResume() {
        Dialog dialog = getDialog();
        if (dialog instanceof AlertDialog) {
            Button btn = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
            if (btn != null) {
                btn.setOnClickListener(this);
            }
        }
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (onPositiveClick()) {
            dismiss();
        }
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
        } catch (IllegalStateException e) {
            NLog.d(e.getMessage());
        }
    }
}
