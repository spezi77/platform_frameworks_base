
package com.android.systemui.statusbar.toggles;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.ImageView;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.BrightnessController;
import com.android.systemui.statusbar.policy.BrightnessController.BrightnessStateChangeCallback;
import com.android.systemui.statusbar.policy.ToggleSlider;

public class BrightnessToggle extends BaseToggle implements BrightnessStateChangeCallback {

    // get these out of here
    private Dialog mBrightnessDialog;

    BrightnessController mBrightnessController;

    private int mBrightnessDialogLongTimeout;

    private int mBrightnessDialogShortTimeout;

    @Override
    public void init(Context c, int style) {
        super.init(c, style);
        mBrightnessDialogLongTimeout =
                mContext.getResources().getInteger(
                        R.integer.quick_settings_brightness_dialog_long_timeout);
        mBrightnessDialogShortTimeout =
                mContext.getResources().getInteger(
                        R.integer.quick_settings_brightness_dialog_short_timeout);
        onBrightnessLevelChanged();
    }

    @Override
    public void onClick(View v) {
        collapseStatusBar();
        showBrightnessDialog();
    }

    @Override
    public boolean onLongClick(View v) {
        dismissKeyguard();
        collapseStatusBar();
        startActivity(new Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS));
        return super.onLongClick(v);
    }

    @Override
    protected void updateView() {
        super.updateView();
        dismissBrightnessDialog(mBrightnessDialogShortTimeout);
    }

    private void showBrightnessDialog() {
        Intent intent = new Intent(Intent.ACTION_SHOW_BRIGHTNESS_DIALOG);
        mContext.sendBroadcast(intent);
    }

    private void removeAllBrightnessDialogCallbacks() {
        mHandler.removeCallbacks(mDismissBrightnessDialogRunnable);
    }

    private Runnable mDismissBrightnessDialogRunnable = new Runnable() {
        public void run() {
            if (mBrightnessDialog != null && mBrightnessDialog.isShowing()) {
                mBrightnessDialog.dismiss();
            }
            removeAllBrightnessDialogCallbacks();
        };
    };

    private void dismissBrightnessDialog(int timeout) {
        removeAllBrightnessDialogCallbacks();
        if (mBrightnessDialog != null) {
            mHandler.postDelayed(mDismissBrightnessDialogRunnable, timeout);
        }
    }

    @Override
    public void onBrightnessLevelChanged() {
        int mode = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                UserHandle.USER_CURRENT);
        boolean autoBrightness =
                (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        int iconId = autoBrightness
                ? R.drawable.ic_qs_brightness_auto_on
                : R.drawable.ic_qs_brightness_auto_off;
        int label = R.string.quick_settings_brightness_label;

        setIcon(iconId);
        setLabel(label);
        scheduleViewUpdate();
    }

}
