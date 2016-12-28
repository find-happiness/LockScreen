package com.happiness.lockscreen;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.happiness.lockscreenlibrary.LockScreenService;

public class MainActivity extends AppCompatActivity implements PageFragment.OnFragmentInteractionListener {

   ViewPager viewPage;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();

    startService(new Intent(this, LockScreenService.class));
  }

  private void initView() {
    initViewPager();
  }

  private void initViewPager() {
    viewPage = (ViewPager) this.findViewById(R.id.viewpage);
    viewPage.setOffscreenPageLimit(3);
    viewPage.setCurrentItem(1);
    viewPage.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
      @Override public Fragment getItem(int position) {
        if (position < 2) {
          return PageFragment.newInstance("", "");
        } else {
          return new AmapFragment();
        }
      }

      @Override public int getCount() {
        return 3;
      }
    });
  }

  @Override public void onFragmentInteraction(Uri uri) {

  }
}
