package com.example.android.FUECAPP;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class FUECAPPActivity extends Activity {
	 public final static String EXTRA_MESSAGE = "com.example.myapp.MESSAGE";
	 private final int DISPLAY_LENGTH = 3000;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        /* New Handler to start the Menu-Activity
        * and close this Splash-Screen after DISPLAY_LENGTH/1000 seconds.*/
        new Handler().postDelayed(new Runnable(){

        @Override
        public void run() {
        /* Create an Intent that will start the Main-Activity. */
        Intent mainIntent = new Intent(FUECAPPActivity.this, BluetoothChat.class);//PostTrip
        FUECAPPActivity.this.startActivity(mainIntent);
        FUECAPPActivity.this.finish();
        }}, DISPLAY_LENGTH);
        }
       
    
    
    
    
    public void myClickHandler(View view) {
    	Intent intent = new Intent (this,BluetoothChat.class); //Screen2Activity
    	//EditText editText= (EditText) findViewById(R.id.TextView01);
    	//String message= editText.getText().toString();
    	//intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    	
    }
    
//    public void stopClickHandler(View view) {
//    	setContentView(R.layout.post_trip);
    	
//    }
    
}