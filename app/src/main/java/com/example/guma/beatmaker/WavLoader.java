package com.example.guma.beatmaker;

import java.io.IOException;

import com.example.guma.beatmaker.Beat1.PlayThread;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class WavLoader extends Activity {

	private EditText[] pathWindow = new EditText[4];
	private Button[] btnLoad = new Button[4];
	private Button btnClose;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wav_loader);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				finish();
			}

		};

		initial();
	}

	public void initial() {
		btnLoad[0] = (Button) findViewById(R.id.btnWL1);
		btnLoad[1] = (Button) findViewById(R.id.btnWL2);
		btnLoad[2] = (Button) findViewById(R.id.btnWL3);
		btnLoad[3] = (Button) findViewById(R.id.btnWL4);
		pathWindow[0] = (EditText) findViewById(R.id.editText1);
		pathWindow[1] = (EditText) findViewById(R.id.editText2);
		pathWindow[2] = (EditText) findViewById(R.id.editText3);
		pathWindow[3] = (EditText) findViewById(R.id.editText4);

		btnClose = (Button) findViewById(R.id.btnWL5);
		for (int i = 0; i < 4; i++) {
			btnLoad[i].setOnClickListener(mClickListener2);
			pathWindow[i].setText(Beat1.wavPath[i]);

		}
		btnClose.setOnClickListener(mClickListener2);
	}

	OnClickListener mClickListener2 = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (v.getId() == btnClose.getId()) {
				finish();
			}
			for(int i=0;i<4;i++){
				if(v.getId()==btnLoad[i].getId()){
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intent,i);
				}
			}

		}
	};

	
	protected void onActivityResult(int requestCode, int resultCode,
			Intent inData) {
		super.onActivityResult(requestCode, resultCode, inData);
		String filePath = inData.getDataString();
		filePath = new Beat1().getFilePath(filePath);
		
		
		switch (requestCode){
		case 0:
		{
			Beat1.wavPath[0]=filePath;
			pathWindow[0].setText(filePath);
			break;
		}
		case 1:
		{
			Beat1.wavPath[1]=filePath;
			pathWindow[1].setText(filePath);
			break;
		}
		case 2:
		{
			Beat1.wavPath[2]=filePath;
			pathWindow[2].setText(filePath);
			break;
		}
		case 3:
		{
			Beat1.wavPath[3]=filePath;
			pathWindow[3].setText(filePath);
			break;
		}
		}
		
		
		
		if (resultCode == RESULT_OK) {
			if (inData != null) {
				

				// System.out.println("path" + filePath); // logCat���� ���Ȯ��.
				Log.i("MyTag2", "path" + filePath);
				
			}
		}
	}

}
