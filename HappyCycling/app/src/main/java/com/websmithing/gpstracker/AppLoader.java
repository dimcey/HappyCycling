package com.websmithing.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


public class AppLoader extends ActionBarActivity {
    private static Button LoginBtn;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apploader);

        LoginBtn = (Button)findViewById(R.id.btnLogin);

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //LoginBtn(view.);
                Intent intent = new Intent(getBaseContext(), GpsTrackerActivity.class);
                startActivity(intent);
            }
        });
    }
}
