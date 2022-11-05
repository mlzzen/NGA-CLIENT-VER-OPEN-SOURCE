package gov.anzong.androidnga.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;

import gov.anzong.androidnga.R;
import nosc.activity.gallery.GalleryActivity;
import sp.phone.util.ImageUtils;

public class AvatarDialogFragment extends BaseDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        Bundle args = getArguments();
        if (args == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        String name = args.getString("name");
        String url = args.getString("url");

        ImageView avatarView = new ImageView(getContext());
        int padding = getResources().getDimensionPixelSize(R.dimen.material_standard);
        avatarView.setPadding(0, padding, 0, 0);
        ImageUtils.loadAvatar(avatarView, url);
        if (!TextUtils.isEmpty(url)) {
            avatarView.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.putExtra(GalleryActivity.KEY_GALLERY_URLS,new String[]{url});
                intent.setClass(getContext(), GalleryActivity.class);
                requireContext().startActivity(intent);
            });
        }

        builder.setTitle(name + "的头像")
                .setView(avatarView)
                .setPositiveButton("关闭", null);

        return builder.create();
    }

}
