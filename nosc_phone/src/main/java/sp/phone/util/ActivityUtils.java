package sp.phone.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import gov.anzong.androidnga.activity.LauncherSubActivity;
import gov.anzong.androidnga.fragment.dialog.SearchDialogFragment;
import sp.phone.common.PhoneConfiguration;
import sp.phone.common.UserManagerImpl;
import gov.anzong.androidnga.fragment.TopicHistoryFragment;


public class ActivityUtils {

    public static final String dialogTag = "saying";
    static final String TAG = ActivityUtils.class.getSimpleName();
    static ActivityUtils instance;
    static final Object lock = new Object();
    private DialogFragment df = null;

    public static final int REQUEST_CODE_LOGIN = 1;

    public static final int REQUEST_CODE_SETTING = 2;

    public static final int REQUEST_CODE_TOPIC_POST = 3;

    public static final int REQUEST_CODE_JUMP_PAGE = 4;

    public static final int REQUEST_CODE_SUB_BOARD = 4;

    private ActivityUtils() {
    }


    public static ActivityUtils getInstance() {
        if (instance == null) {
            instance = new ActivityUtils();
        }
        return instance;//instance;

    }

    public void noticeSaying(Context context) {
        String str = StringUtils.getSaying();
        notice("", str, context);
    }

    public void noticeSayingWithProgressBar(Context context) {
        String str = StringUtils.getSaying();
        noticebar("", str, context);
    }

    private void notice(String title, String content, Context c) {

        if (c == null)
            return;
        NLog.d(TAG, "saying dialog");
        Bundle b = new Bundle();
        b.putString("title", title);
        b.putString("content", content);
        synchronized (lock) {
            try {

                DialogFragment df = new SayingDialogFragment();
                df.setArguments(b);

                FragmentActivity fa = (FragmentActivity) c;
                FragmentManager fm = fa.getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                Fragment prev = fm.findFragmentByTag(dialogTag);
                if (prev != null) {
                    ft.remove(prev);
                }

                ft.commit();
                df.show(fm, dialogTag);
                this.df = df;
            } catch (Exception e) {
                NLog.e(this.getClass().getSimpleName(), NLog.getStackTraceString(e));

            }

        }

    }

    private void noticebar(String title, String content, Context c) {

        if (c == null)
            return;
        NLog.d(TAG, "saying dialog");
        Bundle b = new Bundle();
        b.putString("title", title);
        b.putString("content", content);
        synchronized (lock) {
            try {

                DialogFragment df = new SayingDialogFragmentWithProgressBar();
                df.setArguments(b);

                FragmentActivity fa = (FragmentActivity) c;
                FragmentManager fm = fa.getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                Fragment prev = fm.findFragmentByTag(dialogTag);
                if (prev != null) {
                    ft.remove(prev);
                }

                ft.commit();
                df.show(fm, dialogTag);
                this.df = df;
            } catch (Exception e) {
                NLog.e(this.getClass().getSimpleName(), NLog.getStackTraceString(e));

            }

        }

    }

    public void noticebarsetprogress(int i) {
        NLog.d(TAG, "trying setprocess" + i);
        if (df != null && df.getActivity() != null) {
            if (df instanceof SayingDialogFragmentWithProgressBar) {
                ((SayingDialogFragmentWithProgressBar) df).setProgress(i);
            }
        }
    }

    public void clear() {
        synchronized (lock) {
            this.df = null;
        }
    }

    public void dismiss() {

        synchronized (lock) {
            NLog.d(TAG, "trying dissmiss dialog");


            if (df != null && df.getActivity() != null) {
                NLog.d(TAG, "dissmiss dialog");

                try {
                    FragmentActivity fa = (FragmentActivity) (df.getActivity());
                    FragmentManager fm = fa.getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    Fragment prev = fm.findFragmentByTag(dialogTag);
                    if (prev != null) {
                        ft.remove(prev);

                    }

                    ft.commit();
                } catch (Exception e) {
                    NLog.e(this.getClass().getSimpleName(), NLog.getStackTraceString(e));
                }

                df = null;


            } else {
                df = null;
            }

        }
    }

    public static class SayingDialogFragmentWithProgressBar extends DialogFragment {

        ProgressDialog dialog;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            dialog = new ProgressDialog(getActivity());
            //
            Bundle b = getArguments();
            if (b != null) {
                String title = b.getString("title");
                String content = b.getString("content");
                dialog.setTitle(title);
                if (StringUtils.isEmpty(content))
                    content = StringUtils.getSaying();
                dialog.setMessage(content);
            }


            dialog.setCanceledOnTouchOutside(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setCancelable(true);


            // etc...
            this.setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme);
            return dialog;
        }

        public void setProgress(int i) {
            if (dialog != null) {
                dialog.setProgress(i);
            }
        }

    }

    public static class SayingDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            //
            Bundle b = getArguments();
            if (b != null) {
                String title = b.getString("title");
                String content = b.getString("content");
                dialog.setTitle(title);
                if (StringUtils.isEmpty(content))
                    content = StringUtils.getSaying();
                dialog.setMessage(content);
            }


            dialog.setCanceledOnTouchOutside(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);


            // etc...
            this.setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme);
            return dialog;
        }


    }

    public static void startLoginActivity(Context context) {
        Intent intent = new Intent(context, PhoneConfiguration.loginActivityClass);
        context.startActivity(intent);
    }

    public static void startFavoriteTopicActivity(Context context) {
        if (UserManagerImpl.getInstance().getActiveUser() == null) {
            startLoginActivity(context);
        } else {
            Intent intent = new Intent(context, PhoneConfiguration.topicActivityClass);
            intent.putExtra("favor", 1);
            context.startActivity(intent);
        }
    }

    public static void startRecommendTopicActivity(Context context, Intent intent) {
        if (UserManagerImpl.getInstance().getActiveUser() == null) {
            startLoginActivity(context);
        } else {
            intent.setClass(context, PhoneConfiguration.topicActivityClass);
            context.startActivity(intent);
        }
    }

    public static void startTwentyFourActivity(Context context, Intent intent) {
        if (UserManagerImpl.getInstance().getActiveUser() == null) {
            startLoginActivity(context);
        } else {
            intent.setClass(context, PhoneConfiguration.topicActivityClass);
            context.startActivity(intent);
        }
    }

    public static void startHistoryTopicActivity(Context context) {
        Intent intent = new Intent(context, LauncherSubActivity.class);
        intent.putExtra("fragment", TopicHistoryFragment.class.getName());
        context.startActivity(intent);
    }
}
