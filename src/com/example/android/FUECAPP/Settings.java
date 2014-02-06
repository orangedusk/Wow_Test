package com.example.android.FUECAPP;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class Settings extends Activity {
	private EditText idlingTimeEditText;
	private Button saveButton;
	private final String PREFNAME= "idlingTime";
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		idlingTimeEditText=  (EditText) findViewById(R.id.idlingEditText);
		SharedPreferences pref= getSharedPreferences(PREFNAME,MODE_PRIVATE);
		int idlingTime= pref.getInt("idlingTime", 20);
		idlingTimeEditText.setText(""+idlingTime);
		saveButton= (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int idlingTime=Integer.parseInt(idlingTimeEditText.getText().toString());
				SharedPreferences pref= getSharedPreferences(PREFNAME,MODE_PRIVATE);
				SharedPreferences.Editor editor= pref.edit();
				editor.putInt("idlingTime", idlingTime);
				editor.commit();
			}
		});
	}

}
