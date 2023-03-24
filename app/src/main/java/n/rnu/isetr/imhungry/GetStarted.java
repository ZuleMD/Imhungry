package n.rnu.isetr.imhungry;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class GetStarted extends AppCompatActivity {

    TextView tvSplash, tvSubSplash;
    Button btnget, btngo;
    Animation atg, btgone, btgtwo;
    ImageView ivSplash;
    String btnGoText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         loadLocale();
        setContentView(R.layout.get_started_screen);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        tvSplash = findViewById(R.id.tvSplash);
        tvSubSplash = findViewById(R.id.tvSubSplash);
        btnget = findViewById(R.id.btnget);
        btngo = findViewById(R.id.btngo);
        btnGoText = btngo.getText().toString();

        ivSplash = findViewById(R.id.ivSplash);

        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        btgone = AnimationUtils.loadAnimation(this, R.anim.btgone);
        btgtwo = AnimationUtils.loadAnimation
                (this, R.anim.btgtwo);
        ivSplash.startAnimation(atg);
        tvSplash.startAnimation(btgone);
        tvSubSplash.startAnimation(btgtwo);
        btnget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChooseLanguageDialog();
            }
        });

        btngo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GetStarted.this, MapActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showChooseLanguageDialog() {
        final String[] listItems = {"French", "العربية", "English", "Spanish", "German"};
        AlertDialog.Builder builder = new AlertDialog.Builder(GetStarted.this);
        builder.setTitle("Choose Language...");
        builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) { // French
                    setLocale("fr");
                } else if (i == 1) { // Arabic
                    setLocale("ar");
                } else if (i == 2) { // English
                    setLocale("en");
                } else if (i == 3) { // Spanish
                    setLocale("es");
                } else if (i == 4) { // German
                    setLocale("de");
                }
                dialogInterface.dismiss();
                updateUI();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        //save data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    private String loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
        return language;
    }

    private void updateUI() {
        tvSplash.setText(R.string.good);
        tvSubSplash.setVisibility(View.INVISIBLE);
        btngo.setVisibility(View.VISIBLE);
        btngo.setText(R.string.gobtn);
        btnget.setVisibility(View.INVISIBLE);
    }

}