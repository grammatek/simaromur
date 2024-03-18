package com.grammatek.simaromur;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;

public class Simaromur extends Activity {

  public Simaromur() {
    /**
    if(BuildConfig.DEBUG)
      StrictMode.enableDefaults();
    */
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    /**
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
            .detectLeakedClosableObjects()
            .build());
    */
    finish();
  }
}
