package com.happiness.lockscreenlibrary.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.happiness.lockscreenlibrary.R;
import com.happiness.lockscreenlibrary.util.LockHelper;

/**
 * Created by KyleCe on 2016/5/25.
 *
 * @author: KyleCe
 */
public class LockView extends FrameLayout {

  private Context mContext;

  Button btnUnlock;

  public LockView(Context context) {
    this(context, null);
  }

  public LockView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LockView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    init(context);
  }

  private void init(Context context) {
    mContext = context;
    View view = inflate(context,R.layout.activity_screen_lock,null);

    btnUnlock = (Button) view.findViewById(R.id.unlock);

    btnUnlock.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        LockHelper.getLockLayer().unlock();
      }
    });

    addView(view);
  }

  public void showLockHome(){

  }
}

