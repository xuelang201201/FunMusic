package com.charles.funmusic.executor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.MusicActivity;
import com.charles.funmusic.activity.SettingActivity;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.service.PlayService;
import com.charles.funmusic.service.QuitTimer;
import com.charles.funmusic.utils.ShowDialog;
import com.charles.funmusic.utils.ToastUtil;

/**
 * 导航菜单执行器
 */
public class NaviMenuExecutor {

    private static final String DIALOG_TIME = "DialogTime";

    public static boolean onNavigationItemSelected(MenuItem item, MusicActivity activity) {
        switch (item.getItemId()) {
            case R.id.action_timer:
                timerDialog(activity);
                return true;

            case R.id.action_setting:
                startActivity(activity, SettingActivity.class);
                return true;

            case R.id.action_exit:
                exit(activity);
                return true;
        }
        return false;
    }

    private static void timerDialog(final MusicActivity activity) {
//        new AlertDialog.Builder(activity)
//                .setTitle(R.string.menu_timer)
//                .setItems(activity.getResources().getStringArray(R.array.timer_text), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        int[] times = activity.getResources().getIntArray(R.array.timer_int);
//                        startTimer(activity, times[which]);
//                    }
//                }).show();
        ShowDialog showDialog = new ShowDialog(activity).invoke(R.layout.dialog_timer);
        View dialogView = showDialog.getDialogView();
        final AlertDialog dialog = showDialog.getDialog();
        TextView no = dialogView.findViewById(R.id.dialog_timer_no);
        TextView ten = dialogView.findViewById(R.id.dialog_timer_ten);
        TextView twenty = dialogView.findViewById(R.id.dialog_timer_twenty);
        TextView thirty = dialogView.findViewById(R.id.dialog_timer_thirty);
        TextView anHour = dialogView.findViewById(R.id.dialog_timer_hour);
        TextView custom = dialogView.findViewById(R.id.dialog_timer_custom);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startTimer(activity, 0);
            }
        });

        ten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startTimer(activity, 10);
            }
        });

        twenty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startTimer(activity, 20);
            }
        });

        thirty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startTimer(activity, 30);
            }
        });

        anHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startTimer(activity, 60);
            }
        });

//        custom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//                FragmentManager manager = activity.getFragmentManager();
//                TimePickerFragment dialog = TimePickerFragment.newInstance(new Date());
//                dialog.setTargetFragment(TimerFragment.this, REQUEST_TIME);
//                dialog.show(manager, DIALOG_TIME);
//            }
//        });
    }

    private static void startTimer(Context context, int minute) {
        QuitTimer.getInstance().start(minute * 60 * 1000);
        if (minute > 0) {
            ToastUtil.show(context.getString(R.string.timer_set, String.valueOf(minute)));
        }
    }

    private static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    private static void exit(MusicActivity activity) {
        activity.finish();
        PlayService service = AppCache.getPlayService();
        if (service != null) {
            service.quit();
        }
    }
}
