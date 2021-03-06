package com.happiness.lockscreenlibrary;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.happiness.lockscreenlibrary.inter.PhoneStateChange;
import com.happiness.lockscreenlibrary.util.CoreIntent;
import com.happiness.lockscreenlibrary.util.LockHelper;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by KyleCe on 2016/5/25.
 *
 * @author: KyleCe
 */

final public class LockBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = LockBroadcastReceiver.class.getSimpleName();

  private volatile boolean bInterruptSupervisor = false;

  private ScheduledThreadPoolExecutor mExecutor;
  private FutureRunnable mSupervisorRunnable;

  private static final int SCHEDULE_TASK_NUMBER = 3;

  private PhoneStateChange mPhoneStateChangeCallback;

  public void assignPhoneStateChangeCallback(PhoneStateChange phoneStateChangeCallback) {
    mPhoneStateChangeCallback = phoneStateChangeCallback;
  }

  @Override public void onReceive(Context context, Intent intent) {
    String mAction = intent.getAction();
    //DU.sd("broadcast -----The Intent Action is: ", "" + mAction);

    switch (mAction) {
      case LockHelper.INIT_VIEW_FILTER:
        LockHelper.INSTANCE.initLockViewInBackground(context);
        break;
      case Intent.ACTION_SCREEN_ON:
        refreshBatteryInfo();
        bringLockViewBackTopIfNot();
        break;
      case CoreIntent.ACTION_SCREEN_LOCKER_UNLOCK:
        shutdownScheduleExecutor();
        break;
      case LockHelper.START_SUPERVISE:
        bInterruptSupervisor = false;
        supervise(context.getApplicationContext());
        break;
      case LockHelper.STOP_SUPERVISE:
        bInterruptSupervisor = true;
        break;
      case LockHelper.SHOW_SCREEN_LOCKER:
        //DU.sd("broadcast", "locker received");
      case Intent.ACTION_SCREEN_OFF:
        LockHelper.INSTANCE.initialize(context);
        LockHelper.INSTANCE.getLockLayer().lock();
        bInterruptSupervisor = true;
        break;
      case Intent.ACTION_POWER_CONNECTED:
        //LockHelper.INSTANCE.getLockView().batteryChargingAnim();
        break;
      case Intent.ACTION_POWER_DISCONNECTED:
        //LockHelper.INSTANCE.getLockView().batteryChargingAnim();
        break;
      case Intent.ACTION_SHUTDOWN:
        break;
      case "android.intent.action.PHONE_STATE":
        TelephonyManager tm =
            (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

        switch (tm.getCallState()) {
          case TelephonyManager.CALL_STATE_RINGING:
            mPhoneStateChangeCallback.ringing();
            Log.i(TAG, "RINGING :" + intent.getStringExtra("incoming_number"));
            break;
          case TelephonyManager.CALL_STATE_OFFHOOK:
            mPhoneStateChangeCallback.offHook();
            //DU.sd(TAG, "off hook");
            break;
          case TelephonyManager.CALL_STATE_IDLE:
            mPhoneStateChangeCallback.idle();
            Log.i(TAG, "incoming IDLE");
            break;
        }
        break;
      default:
        break;
    }
  }

  abstract class FutureRunnable implements Runnable {

    private Future<?> future;

    public Future<?> getFuture() {
      return future;
    }

    public void setFuture(Future<?> future) {
      this.future = future;
    }
  }

  public void supervise(final Context context) {
    //DU.sd("service", "supervise");

    initScheduleExecutor();

    if (mSupervisorRunnable == null) {
      mSupervisorRunnable = new FutureRunnable() {
        public void run() {
          if (bInterruptSupervisor) getFuture().cancel(true);

          boolean cameraRunning = false;
          Camera _camera = null;
          try {
            _camera = Camera.open();
            cameraRunning = _camera == null;
          } catch (Exception e) {
            // fail to open camera, secure to ignore exception
            //DU.sd("camera exception on supervise");
            cameraRunning = true;
          } finally {
            if (_camera != null) {
              _camera.release();
              getFuture().cancel(true);
              context.sendBroadcast(new Intent(LockHelper.SHOW_SCREEN_LOCKER));
            }
          }

          if (!cameraRunning) context.sendBroadcast(new Intent(LockHelper.SHOW_SCREEN_LOCKER));
        }
      };
    }
    Future<?> future =
        mExecutor.scheduleAtFixedRate(mSupervisorRunnable, 2000, 500, TimeUnit.MILLISECONDS);
    mSupervisorRunnable.setFuture(future);
  }

  private void bringLockViewBackTopIfNot() {
    initScheduleExecutor();
    mExecutor.scheduleAtFixedRate(new Runnable() {
      @Override public void run() {
        LockHelper.INSTANCE.getLockLayer().requestFullScreen();
      }
    }, 1000, 1000, TimeUnit.MILLISECONDS);
  }

  private void refreshBatteryInfo() {
    initScheduleExecutor();
    mExecutor.scheduleAtFixedRate(new Runnable() {
      @Override public void run() {
        //LockHelper.INSTANCE.getLockView().refreshBattery();
      }
    }, 2, 2, TimeUnit.MINUTES);
  }

  private void initScheduleExecutor() {
    if (mExecutor == null) {
      synchronized (this) {
        if (mExecutor == null) mExecutor = new ScheduledThreadPoolExecutor(SCHEDULE_TASK_NUMBER);
      }
    }
  }

  public synchronized void shutdownScheduleExecutor() {
    if (mExecutor == null) return;

    mExecutor.shutdown();
    mExecutor = null;
  }
}
