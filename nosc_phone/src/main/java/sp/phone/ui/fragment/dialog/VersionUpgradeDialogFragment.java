package sp.phone.ui.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;

import gov.anzong.androidnga.NgaClientApp;
import gov.anzong.androidnga.R;


public class VersionUpgradeDialogFragment extends BaseDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String tip = null;
        if (tip == null) {
            dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.prompt)
                .setMessage(tip);
        return builder.create();
    }

    @Override
    public void onDestroy() {
//        NgaClientApp.setNewVersion(false);
        super.onDestroy();
    }

}
