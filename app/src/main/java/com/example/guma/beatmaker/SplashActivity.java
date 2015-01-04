package com.example.guma.beatmaker;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;

public class SplashActivity extends Activity {
	
	
	
	
	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.splash);
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        Handler handler = new Handler(){
	        	public void handleMessage(Message msg){
	        		finish();
	        	}
	        };
	        
	        
	        
	        handler.sendEmptyMessageDelayed(0, 3000);
	        
	        
	    }
	 
	 

}
