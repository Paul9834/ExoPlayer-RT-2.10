package com.paul9834.exoiptv;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class HomeClass extends AppCompatActivity {


    Button actividad;
    EditText id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_class);


        id = findViewById(R.id.editText);
        actividad = findViewById(R.id.Enviar);


        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);

        if (!isFirstRun) {
            Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
            startActivity(intent);
            finish();
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();
        Layout();
    }


    public void Layout () {
        actividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String texto = id.getText().toString().trim();

                if (TextUtils.isEmpty(texto)) {
                    id.setError("Ingrese un ID de canal");

                }

                else {

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(HomeClass.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("id", texto);
                    editor.apply();

                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });
    }
}
