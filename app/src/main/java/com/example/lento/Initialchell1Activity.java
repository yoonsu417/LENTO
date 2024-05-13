package com.example.lento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Initialchell1Activity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialchell1);

        Button chellsetbtn = (Button)findViewById(R.id.chellset);
        chellsetbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Initialchell1Activity.this, Initialchell2Activity.class);
                startActivity(intent);
            }
        });

    }
}
