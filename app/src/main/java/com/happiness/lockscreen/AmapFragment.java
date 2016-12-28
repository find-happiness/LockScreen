package com.happiness.lockscreen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.amap.api.maps.AMap;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.TextureSupportMapFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class AmapFragment extends Fragment {
  private AMap aMap;

  public AmapFragment() {
    // Required empty public constructor
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_amap, container, false);


    if (aMap == null) {
      aMap = ((TextureSupportMapFragment) getChildFragmentManager().findFragmentById(
          R.id.map)).getMap();
    }
    return view;
  }
}
