package com.oraycn.ovcs.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.oraycn.ovcs.MainActivity;
import com.oraycn.ovcs.OrayApplication;
import com.oraycn.ovcs.R;
import com.oraycn.ovcs.utils.GlobalConfig;
import com.oraycn.ovcs.utils.IntentUtils;

public class ConnectActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        GlobalConfig.RoomID = getIntent().getStringExtra("roomID");
    }

    public void joinGroup(View view)
    {
        IntentUtils.startActivityForString(ConnectActivity.this, MainActivity.class, "roomID", GlobalConfig.RoomID);
    }

    public void close(View view)
    {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OrayApplication.getInstance().closeEngine();
    }
}