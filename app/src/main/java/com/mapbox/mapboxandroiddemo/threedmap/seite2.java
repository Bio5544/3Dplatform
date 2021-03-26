package com.mapbox.mapboxandroiddemo.threedmap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;


// the main page
public class seite2 extends AppCompatActivity {

    private Button button;
    private TextInputEditText text1;
    private String message;
    public static TextView tex2;


// It takes the parameter from textView and reads the JSON file on the server in the background by "ReadTextTask" Class
// When it's done, all these parameters are passed to the next page (MainActivity).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seite2_layout);
        text1= (TextInputEditText) findViewById(R.id.edit_text);
        tex2=findViewById(R.id.textView);
        message="";

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text1.getText().toString().equals("")) {
                    message="";
                }
                else{
                    message="http://"+text1.getText().toString();
                }
                ReadTextTask rea= new ReadTextTask();
                if (message!=""){
                    rea.execute(message);
                }
                else{
                    // This is the standard message that is forwarded when the connection to the server is not working
                    tex2.setText("0;1;2;3;4;5;6;#00cecf;Beuth Hochschule 3D;52.5454363;13.3514886");
                }
                if (((Boolean) ((String) tex2.getText()).contains("$") ==false) & (String) tex2.getText()!=null){
                    String tt = (String) tex2.getText();
                    openSeite2(tt);
                }
            }
        });
    }

    // Function to call the MainActivity.(with parameters)
    // parameters: message is the URL of the Folder on the Server ddd is the information from json Fie on the Server .

    public  void openSeite2(String s) {
        Intent intent =new Intent(this,MainActivity.class);
        intent.putExtra("mes",message);
        intent.putExtra("ddd",s);
        startActivity(intent);
        this.finish();
        }
    }

