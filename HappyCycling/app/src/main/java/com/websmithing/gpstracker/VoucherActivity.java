
package com.websmithing.gpstracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by Dimitar on 3/5/2015.
 */
public class VoucherActivity extends Activity {
    private static final String TAG = "GpsTrackerActivity";
    private static Button buy1;
    private static Button buy2;
    private static Button buy3;
    private static TextView txt1;
    private static TextView txt2;
    private static TextView txt3;
    private static TextView txt4;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher);
        txt1 = (TextView) findViewById(R.id.textView);
        txt2 = (TextView) findViewById(R.id.textView2);
        txt3 = (TextView) findViewById(R.id.textView3);
        txt4 = (TextView) findViewById(R.id.textView4);
        buy1 = (Button) findViewById(R.id.button);
        buy2 = (Button) findViewById(R.id.button2);
        buy3 = (Button) findViewById(R.id.button3);

        buy1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                txt4.setText("You don't have enough points for the S Market voucher!");
            }
        });
        buy2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                txt4.setText("You don't have enough points for the Alko voucher!");
            }
        });
        buy3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                buy3.setVisibility(View.GONE);
                txt3.setVisibility(View.GONE);
                Random r = new Random();
                int i1 = r.nextInt(123456789 - 12345678) + 12345678;
                txt4.setText("You have bought Sodexo voucher. Show the code " +i1+" when using the voucher");

            }
        });

    }


}

