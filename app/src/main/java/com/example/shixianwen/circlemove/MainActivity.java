package com.example.shixianwen.circlemove;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private CircleView1 circleView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       //set full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        circleView = (CircleView1) this.findViewById(R.id.circlemove);
        circleView.setBackgroundColor(getResources().getColor(android.R.color.white));
        circleView.setHandeler(new Handler());
        circleView.init();
        String msg = "Attempting to connect";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(circleView != null){
            circleView.onclose();
        }
    }
}
