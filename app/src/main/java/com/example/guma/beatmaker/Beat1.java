package com.example.guma.beatmaker;

import java.io.*;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class Beat1 extends Activity {

	final static String mytag = "MyTag";
	final static String str_Path = Environment.getExternalStorageDirectory()
			.getAbsolutePath();
	final static int NUM_INST = 8;
	final static int NUM_PTNS = 10;
	final static int NUM_DRUMS = 6;
	final static int BPM_MAX = 180;
	final static int BPM_MIN = 60;

	private SoundPool beatPlayer;
	private int[][] soundInst = new int[NUM_DRUMS][NUM_INST];
	private boolean playFlag;
	private boolean loopFlag = true;

	private Pattern[] ptn = new Pattern[NUM_PTNS];
	private int currentPtn = 0;
	private int currentDrum = 0;
	private int currentBPM = 120;

	
	private CheckBox[][] mCheck = new CheckBox[NUM_INST][16];
	private CheckBox checkBoxLoop;

	private NumberPicker bpmPicker;

	private Spinner ptnSpinner, drumSpinner;
	private Button btnPlay, btnStop, btnSave, btnLoad;
	private Button[] btnInst = new Button[NUM_INST];
	private Button[] btnWav = new Button[4];
	private Button btnWavLoad;


	private EditText ptnField;

	private int numOfPtns = 0;
	private int[] ptnStream = new int[500];

	private MediaPlayer[] wav_play = new MediaPlayer[4];
	public static String[] wavPath= new String[4];
	

	OnValueChangeListener bpmChangeListener = new OnValueChangeListener() {

		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			// TODO Auto-generated method stub
			currentBPM = newVal;
		}
	};
	
	OnClickListener mClickListener2 = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}};

	OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			for (int i = 0; i < NUM_INST; i++) {
				for (int j = 0; j < 16; j++) {
					if (v.getId() == mCheck[i][j].getId()) {
						ptn[currentPtn].flags[i][j] = mCheck[i][j].isChecked();
					}
				}
			}

			if (v.getId() ==checkBoxLoop.getId()) {
				loopFlag = checkBoxLoop.isChecked();
			}

			if (v.getId() == btnPlay.getId()) {
				playFlag = false;
				getPatternStream();
				refresh();
				(new PlayThread()).start();

			}

			if (v.getId() == btnStop.getId())
				stop();

			if (v.getId() == btnLoad.getId()) {

				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

				startActivityForResult(intent, 0);

			}
			if (v.getId() == btnSave.getId()) {
				

			}

			for (int i = 0; i < NUM_INST; i++) {
				if (v.getId() == btnInst[i].getId())
					playBeat(i);
			}

			for(int i=0;i<4;i++){
			if (v.getId() == btnWav[i].getId()) {
				wav_play[i].start();
			}
			}			
			if(v.getId()==btnWavLoad.getId()){
				wavLoad();
			}

		}
	};

	protected void wavLoad() {
		startActivityForResult(new Intent(this, WavLoader.class), 1);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent inData) {
		super.onActivityResult(requestCode, resultCode, inData);
		if(requestCode==1){
			prepareWav();
		}
		else{

		if (resultCode == RESULT_OK) {
			if (inData != null) {
				String filePath = inData.getDataString();
				filePath = filePath.substring(7);

				// System.out.println("path" + filePath); // logCat���� ���Ȯ��.
				Log.i("MyTag2", "path" + filePath);
				try {
					load(filePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beat1);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		startActivity(new Intent(this, SplashActivity.class));

		initialize();

	}

	public void refreshPatternBox() {
		for (int i = 0; i < NUM_INST; i++) {
			for (int j = 0; j < 16; j++) {
				mCheck[i][j].setChecked(ptn[currentPtn].flags[i][j]);

			}
		}

	}

	public void refresh() {
		refreshPatternBox();
		ptnSpinner.setSelection(currentPtn);
		for (int i = 0; i < NUM_INST; i++) {
			for(int j=0;j<16;j++){
				mCheck[i][j].setBackgroundResource(R.drawable.on_off_checked);
			}
		}
	}

	public void soundSettings() {
		beatPlayer = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		soundInst[0][0] = beatPlayer.load(getBaseContext(), R.raw.inst0_1, 1);
		soundInst[0][1] = beatPlayer.load(getBaseContext(), R.raw.inst0_2, 1);
		soundInst[0][2] = beatPlayer.load(getBaseContext(), R.raw.inst0_3, 1);
		soundInst[0][3] = beatPlayer.load(getBaseContext(), R.raw.inst0_4, 1);
		soundInst[0][4] = beatPlayer.load(getBaseContext(), R.raw.inst0_5, 1);
		soundInst[0][5] = beatPlayer.load(getBaseContext(), R.raw.inst0_6, 1);
		soundInst[0][6] = beatPlayer.load(getBaseContext(), R.raw.inst0_7, 1);
		soundInst[0][7] = beatPlayer.load(getBaseContext(), R.raw.inst0_8, 1);

		soundInst[1][0] = beatPlayer.load(getBaseContext(), R.raw.inst1_1, 1);
		soundInst[1][1] = beatPlayer.load(getBaseContext(), R.raw.inst1_2, 1);
		soundInst[1][2] = beatPlayer.load(getBaseContext(), R.raw.inst1_3, 1);
		soundInst[1][3] = beatPlayer.load(getBaseContext(), R.raw.inst1_4, 1);
		soundInst[1][4] = beatPlayer.load(getBaseContext(), R.raw.inst1_5, 1);
		soundInst[1][5] = beatPlayer.load(getBaseContext(), R.raw.inst1_6, 1);
		soundInst[1][6] = beatPlayer.load(getBaseContext(), R.raw.inst1_7, 1);
		soundInst[1][7] = beatPlayer.load(getBaseContext(), R.raw.inst1_8, 1);

		soundInst[2][0] = beatPlayer.load(getBaseContext(), R.raw.inst2_1, 1);
		soundInst[2][1] = beatPlayer.load(getBaseContext(), R.raw.inst2_2, 1);
		soundInst[2][2] = beatPlayer.load(getBaseContext(), R.raw.inst2_3, 1);
		soundInst[2][3] = beatPlayer.load(getBaseContext(), R.raw.inst2_4, 1);
		soundInst[2][4] = beatPlayer.load(getBaseContext(), R.raw.inst2_5, 1);
		soundInst[2][5] = beatPlayer.load(getBaseContext(), R.raw.inst2_6, 1);
		soundInst[2][6] = beatPlayer.load(getBaseContext(), R.raw.inst2_7, 1);
		soundInst[2][7] = beatPlayer.load(getBaseContext(), R.raw.inst2_8, 1);

		soundInst[3][0] = beatPlayer.load(getBaseContext(), R.raw.inst3_1, 1);
		soundInst[3][1] = beatPlayer.load(getBaseContext(), R.raw.inst3_2, 1);
		soundInst[3][2] = beatPlayer.load(getBaseContext(), R.raw.inst3_3, 1);
		soundInst[3][3] = beatPlayer.load(getBaseContext(), R.raw.inst3_4, 1);
		soundInst[3][4] = beatPlayer.load(getBaseContext(), R.raw.inst3_5, 1);
		soundInst[3][5] = beatPlayer.load(getBaseContext(), R.raw.inst3_6, 1);
		soundInst[3][6] = beatPlayer.load(getBaseContext(), R.raw.inst3_7, 1);
		soundInst[3][7] = beatPlayer.load(getBaseContext(), R.raw.inst3_8, 1);

		soundInst[4][0] = beatPlayer.load(getBaseContext(), R.raw.inst4_1, 1);
		soundInst[4][1] = beatPlayer.load(getBaseContext(), R.raw.inst4_2, 1);
		soundInst[4][2] = beatPlayer.load(getBaseContext(), R.raw.inst4_3, 1);
		soundInst[4][3] = beatPlayer.load(getBaseContext(), R.raw.inst4_4, 1);
		soundInst[4][4] = beatPlayer.load(getBaseContext(), R.raw.inst4_5, 1);
		soundInst[4][5] = beatPlayer.load(getBaseContext(), R.raw.inst4_6, 1);
		soundInst[4][6] = beatPlayer.load(getBaseContext(), R.raw.inst4_7, 1);
		soundInst[4][7] = beatPlayer.load(getBaseContext(), R.raw.inst4_8, 1);

		soundInst[5][0] = beatPlayer.load(getBaseContext(), R.raw.inst5_1, 1);
		soundInst[5][1] = beatPlayer.load(getBaseContext(), R.raw.inst5_2, 1);
		soundInst[5][2] = beatPlayer.load(getBaseContext(), R.raw.inst5_3, 1);
		soundInst[5][3] = beatPlayer.load(getBaseContext(), R.raw.inst5_4, 1);
		soundInst[5][4] = beatPlayer.load(getBaseContext(), R.raw.inst5_5, 1);
		soundInst[5][5] = beatPlayer.load(getBaseContext(), R.raw.inst5_6, 1);
		soundInst[5][6] = beatPlayer.load(getBaseContext(), R.raw.inst5_7, 1);
		soundInst[5][7] = beatPlayer.load(getBaseContext(), R.raw.inst5_8, 1);
	}

	public void initialize() {
		soundSettings();

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		for (int i = 0; i < NUM_PTNS; i++) {
			ptn[i] = new Pattern(NUM_INST);
		}

		/*
		 * PowerManager.WakeLock wakeLock=null; if(wakeLock==null){ PowerManager
		 * powerManager= (PowerManager)getSystemService(Context.POWER_SERVICE);
		 * wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
		 * "wakelock"); wakeLock.acquire(); }
		 */
		bpmPicker = (NumberPicker) findViewById(R.id.bpmPicker);

		bpmPicker.setMaxValue(BPM_MAX);
		bpmPicker.setMinValue(BPM_MIN);
		bpmPicker.setValue(currentBPM);
		bpmPicker.setOnValueChangedListener(bpmChangeListener);
		bpmPicker.setWrapSelectorWheel(false);


		ptnField = (EditText) findViewById(R.id.ptnField);

		ptnSpinner = (Spinner) findViewById(R.id.ptnSpinner);
		ArrayAdapter<?> adapter1 = ArrayAdapter.createFromResource(this,
				R.array.Patterns, android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ptnSpinner.setAdapter(adapter1);
		ptnSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				currentPtn = position;
				refreshPatternBox();
			}

			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		drumSpinner = (Spinner) findViewById(R.id.drumSpinner);
		ArrayAdapter<?> adapter2 = ArrayAdapter.createFromResource(this,
				R.array.Drums, android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		drumSpinner.setAdapter(adapter2);
		drumSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				currentDrum = position;
			}

			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		
		checkBoxLoop = (CheckBox) findViewById(R.id.checkBoxLoop);
		checkBoxLoop.setOnClickListener(mClickListener);
		checkBoxLoop.setChecked(true);
		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(mClickListener);
		btnStop = (Button) findViewById(R.id.btnStop);
		btnStop.setOnClickListener(mClickListener);
		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(mClickListener);
		btnLoad = (Button) findViewById(R.id.btnLoad);
		btnLoad.setOnClickListener(mClickListener);

		btnInst[0] = (Button) findViewById(R.id.btnWL1);
		btnInst[1] = (Button) findViewById(R.id.button2);
		btnInst[2] = (Button) findViewById(R.id.button3);
		btnInst[3] = (Button) findViewById(R.id.button4);
		btnInst[4] = (Button) findViewById(R.id.button5);
		btnInst[5] = (Button) findViewById(R.id.button6);
		btnInst[6] = (Button) findViewById(R.id.button7);
		btnInst[7] = (Button) findViewById(R.id.button8);

		chkBtnInitialize();

		btnWav[0] = (Button) findViewById(R.id.btnWav1);
		btnWav[1] = (Button) findViewById(R.id.btnWav2);
		btnWav[2] = (Button) findViewById(R.id.btnWav3);
		btnWav[3] = (Button) findViewById(R.id.btnWav4);
		btnWavLoad = (Button) findViewById(R.id.btnWavLoad);

		for (int i = 0; i < NUM_INST; i++) {
			btnInst[i].setOnClickListener(mClickListener);
			for (int j = 0; j < 16; j++) {
				mCheck[i][j].setOnClickListener(mClickListener);
			}
		}
		for (int i = 0; i < 4; i++) {
			wav_play[i]=new MediaPlayer();
			btnWav[i].setOnClickListener(mClickListener);
		}
		btnWavLoad.setOnClickListener(mClickListener);

		prepareWav();

	}

	public void prepareWav() {
		try {
			for(int i=0;i<4;i++){
				if(wavPath[i]!=null){
				wav_play[i].setDataSource(wavPath[i]);
				wav_play[i].prepare();
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void chkBtnInitialize() {
		mCheck[0][0] = (CheckBox) findViewById(R.id.checkBox0_0);
		mCheck[0][1] = (CheckBox) findViewById(R.id.checkBox0_1);
		mCheck[0][2] = (CheckBox) findViewById(R.id.checkBox0_2);
		mCheck[0][3] = (CheckBox) findViewById(R.id.checkBox0_3);
		mCheck[0][4] = (CheckBox) findViewById(R.id.checkBox0_4);
		mCheck[0][5] = (CheckBox) findViewById(R.id.checkBox0_5);
		mCheck[0][6] = (CheckBox) findViewById(R.id.checkBox0_6);
		mCheck[0][7] = (CheckBox) findViewById(R.id.checkBox0_7);
		mCheck[0][8] = (CheckBox) findViewById(R.id.checkBox0_8);
		mCheck[0][9] = (CheckBox) findViewById(R.id.checkBox0_9);
		mCheck[0][10] = (CheckBox) findViewById(R.id.checkBox0_10);
		mCheck[0][11] = (CheckBox) findViewById(R.id.checkBox0_11);
		mCheck[0][12] = (CheckBox) findViewById(R.id.checkBox0_12);
		mCheck[0][13] = (CheckBox) findViewById(R.id.checkBox0_13);
		mCheck[0][14] = (CheckBox) findViewById(R.id.checkBox0_14);
		mCheck[0][15] = (CheckBox) findViewById(R.id.checkBox0_15);

		mCheck[1][0] = (CheckBox) findViewById(R.id.checkBox1_0);
		mCheck[1][1] = (CheckBox) findViewById(R.id.checkBox1_1);
		mCheck[1][2] = (CheckBox) findViewById(R.id.checkBox1_2);
		mCheck[1][3] = (CheckBox) findViewById(R.id.checkBox1_3);
		mCheck[1][4] = (CheckBox) findViewById(R.id.checkBox1_4);
		mCheck[1][5] = (CheckBox) findViewById(R.id.checkBox1_5);
		mCheck[1][6] = (CheckBox) findViewById(R.id.checkBox1_6);
		mCheck[1][7] = (CheckBox) findViewById(R.id.checkBox1_7);
		mCheck[1][8] = (CheckBox) findViewById(R.id.checkBox1_8);
		mCheck[1][9] = (CheckBox) findViewById(R.id.checkBox1_9);
		mCheck[1][10] = (CheckBox) findViewById(R.id.checkBox1_10);
		mCheck[1][11] = (CheckBox) findViewById(R.id.checkBox1_11);
		mCheck[1][12] = (CheckBox) findViewById(R.id.checkBox1_12);
		mCheck[1][13] = (CheckBox) findViewById(R.id.checkBox1_13);
		mCheck[1][14] = (CheckBox) findViewById(R.id.checkBox1_14);
		mCheck[1][15] = (CheckBox) findViewById(R.id.checkBox1_15);

		mCheck[2][0] = (CheckBox) findViewById(R.id.checkBox2_0);
		mCheck[2][1] = (CheckBox) findViewById(R.id.checkBox2_1);
		mCheck[2][2] = (CheckBox) findViewById(R.id.checkBox2_2);
		mCheck[2][3] = (CheckBox) findViewById(R.id.checkBox2_3);
		mCheck[2][4] = (CheckBox) findViewById(R.id.checkBox2_4);
		mCheck[2][5] = (CheckBox) findViewById(R.id.checkBox2_5);
		mCheck[2][6] = (CheckBox) findViewById(R.id.checkBox2_6);
		mCheck[2][7] = (CheckBox) findViewById(R.id.checkBox2_7);
		mCheck[2][8] = (CheckBox) findViewById(R.id.checkBox2_8);
		mCheck[2][9] = (CheckBox) findViewById(R.id.checkBox2_9);
		mCheck[2][10] = (CheckBox) findViewById(R.id.checkBox2_10);
		mCheck[2][11] = (CheckBox) findViewById(R.id.checkBox2_11);
		mCheck[2][12] = (CheckBox) findViewById(R.id.checkBox2_12);
		mCheck[2][13] = (CheckBox) findViewById(R.id.checkBox2_13);
		mCheck[2][14] = (CheckBox) findViewById(R.id.checkBox2_14);
		mCheck[2][15] = (CheckBox) findViewById(R.id.checkBox2_15);

		mCheck[3][0] = (CheckBox) findViewById(R.id.checkBox3_0);
		mCheck[3][1] = (CheckBox) findViewById(R.id.checkBox3_1);
		mCheck[3][2] = (CheckBox) findViewById(R.id.checkBox3_2);
		mCheck[3][3] = (CheckBox) findViewById(R.id.checkBox3_3);
		mCheck[3][4] = (CheckBox) findViewById(R.id.checkBox3_4);
		mCheck[3][5] = (CheckBox) findViewById(R.id.checkBox3_5);
		mCheck[3][6] = (CheckBox) findViewById(R.id.checkBox3_6);
		mCheck[3][7] = (CheckBox) findViewById(R.id.checkBox3_7);
		mCheck[3][8] = (CheckBox) findViewById(R.id.checkBox3_8);
		mCheck[3][9] = (CheckBox) findViewById(R.id.checkBox3_9);
		mCheck[3][10] = (CheckBox) findViewById(R.id.checkBox3_10);
		mCheck[3][11] = (CheckBox) findViewById(R.id.checkBox3_11);
		mCheck[3][12] = (CheckBox) findViewById(R.id.checkBox3_12);
		mCheck[3][13] = (CheckBox) findViewById(R.id.checkBox3_13);
		mCheck[3][14] = (CheckBox) findViewById(R.id.checkBox3_14);
		mCheck[3][15] = (CheckBox) findViewById(R.id.checkBox3_15);

		mCheck[4][0] = (CheckBox) findViewById(R.id.checkBox4_0);
		mCheck[4][1] = (CheckBox) findViewById(R.id.checkBox4_1);
		mCheck[4][2] = (CheckBox) findViewById(R.id.checkBox4_2);
		mCheck[4][3] = (CheckBox) findViewById(R.id.checkBox4_3);
		mCheck[4][4] = (CheckBox) findViewById(R.id.checkBox4_4);
		mCheck[4][5] = (CheckBox) findViewById(R.id.checkBox4_5);
		mCheck[4][6] = (CheckBox) findViewById(R.id.checkBox4_6);
		mCheck[4][7] = (CheckBox) findViewById(R.id.checkBox4_7);
		mCheck[4][8] = (CheckBox) findViewById(R.id.checkBox4_8);
		mCheck[4][9] = (CheckBox) findViewById(R.id.checkBox4_9);
		mCheck[4][10] = (CheckBox) findViewById(R.id.checkBox4_10);
		mCheck[4][11] = (CheckBox) findViewById(R.id.checkBox4_11);
		mCheck[4][12] = (CheckBox) findViewById(R.id.checkBox4_12);
		mCheck[4][13] = (CheckBox) findViewById(R.id.checkBox4_13);
		mCheck[4][14] = (CheckBox) findViewById(R.id.checkBox4_14);
		mCheck[4][15] = (CheckBox) findViewById(R.id.checkBox4_15);

		mCheck[5][0] = (CheckBox) findViewById(R.id.checkBox5_0);
		mCheck[5][1] = (CheckBox) findViewById(R.id.checkBox5_1);
		mCheck[5][2] = (CheckBox) findViewById(R.id.checkBox5_2);
		mCheck[5][3] = (CheckBox) findViewById(R.id.checkBox5_3);
		mCheck[5][4] = (CheckBox) findViewById(R.id.checkBox5_4);
		mCheck[5][5] = (CheckBox) findViewById(R.id.checkBox5_5);
		mCheck[5][6] = (CheckBox) findViewById(R.id.checkBox5_6);
		mCheck[5][7] = (CheckBox) findViewById(R.id.checkBox5_7);
		mCheck[5][8] = (CheckBox) findViewById(R.id.checkBox5_8);
		mCheck[5][9] = (CheckBox) findViewById(R.id.checkBox5_9);
		mCheck[5][10] = (CheckBox) findViewById(R.id.checkBox5_10);
		mCheck[5][11] = (CheckBox) findViewById(R.id.checkBox5_11);
		mCheck[5][12] = (CheckBox) findViewById(R.id.checkBox5_12);
		mCheck[5][13] = (CheckBox) findViewById(R.id.checkBox5_13);
		mCheck[5][14] = (CheckBox) findViewById(R.id.checkBox5_14);
		mCheck[5][15] = (CheckBox) findViewById(R.id.checkBox5_15);

		mCheck[6][0] = (CheckBox) findViewById(R.id.checkBox6_0);
		mCheck[6][1] = (CheckBox) findViewById(R.id.checkBox6_1);
		mCheck[6][2] = (CheckBox) findViewById(R.id.checkBox6_2);
		mCheck[6][3] = (CheckBox) findViewById(R.id.checkBox6_3);
		mCheck[6][4] = (CheckBox) findViewById(R.id.checkBox6_4);
		mCheck[6][5] = (CheckBox) findViewById(R.id.checkBox6_5);
		mCheck[6][6] = (CheckBox) findViewById(R.id.checkBox6_6);
		mCheck[6][7] = (CheckBox) findViewById(R.id.checkBox6_7);
		mCheck[6][8] = (CheckBox) findViewById(R.id.checkBox6_8);
		mCheck[6][9] = (CheckBox) findViewById(R.id.checkBox6_9);
		mCheck[6][10] = (CheckBox) findViewById(R.id.checkBox6_10);
		mCheck[6][11] = (CheckBox) findViewById(R.id.checkBox6_11);
		mCheck[6][12] = (CheckBox) findViewById(R.id.checkBox6_12);
		mCheck[6][13] = (CheckBox) findViewById(R.id.checkBox6_13);
		mCheck[6][14] = (CheckBox) findViewById(R.id.checkBox6_14);
		mCheck[6][15] = (CheckBox) findViewById(R.id.checkBox6_15);

		mCheck[7][0] = (CheckBox) findViewById(R.id.checkBox7_0);
		mCheck[7][1] = (CheckBox) findViewById(R.id.checkBox7_1);
		mCheck[7][2] = (CheckBox) findViewById(R.id.checkBox7_2);
		mCheck[7][3] = (CheckBox) findViewById(R.id.checkBox7_3);
		mCheck[7][4] = (CheckBox) findViewById(R.id.checkBox7_4);
		mCheck[7][5] = (CheckBox) findViewById(R.id.checkBox7_5);
		mCheck[7][6] = (CheckBox) findViewById(R.id.checkBox7_6);
		mCheck[7][7] = (CheckBox) findViewById(R.id.checkBox7_7);
		mCheck[7][8] = (CheckBox) findViewById(R.id.checkBox7_8);
		mCheck[7][9] = (CheckBox) findViewById(R.id.checkBox7_9);
		mCheck[7][10] = (CheckBox) findViewById(R.id.checkBox7_10);
		mCheck[7][11] = (CheckBox) findViewById(R.id.checkBox7_11);
		mCheck[7][12] = (CheckBox) findViewById(R.id.checkBox7_12);
		mCheck[7][13] = (CheckBox) findViewById(R.id.checkBox7_13);
		mCheck[7][14] = (CheckBox) findViewById(R.id.checkBox7_14);
		mCheck[7][15] = (CheckBox) findViewById(R.id.checkBox7_15);

	}

	public void getPatternStream() {

		char temp;
		String temp2 = ptnField.getText().toString();
		numOfPtns = temp2.length();
		for (int i = 0; i < numOfPtns; i++) {
			temp = temp2.charAt(i);
			ptnStream[i] = (int) temp - 48;
		}

	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			refresh(); // ���ڰ� ���ߴ� UI ������Ʈ �۾�
		}
	};
	final Handler handler2 = new Handler() {
		public void handleMessage(Message msg) {
			int i=msg.what;
			for (int j = 0; j < NUM_INST; j++) {
				if(i>0){
					mCheck[j][i-1].setBackgroundResource(R.drawable.on_off_checked);
					mCheck[j][i].setBackgroundResource(R.drawable.on_off_checked_play);
				}
				else{
					mCheck[j][15].setBackgroundResource(R.drawable.on_off_checked);
					mCheck[j][0].setBackgroundResource(R.drawable.on_off_checked_play);
				}
			}
			
		}
	};

	public void playBeat(int inst) {
		beatPlayer.play(soundInst[currentDrum][inst], 1.0f, 1.0f, 0, 0, 1.0f);
	}

	class PlayThread extends Thread // �ð�üũ�� ����� �˰?���� ����ִ� ����Ŭ����
	{

		private long startTime;
		private long elapTime;
		private long cntTime;
		private long oneLoopTime;
		private int i;
		private int count;

		public void run() {

			playFlag = true; // ����Ұ��̹Ƿ� ó���� playFlag�� true�� �ٲ�
			count = 0;

			while (playFlag == true && (count < numOfPtns || loopFlag == true)) {

				i = 0;
				startTime = System.currentTimeMillis(); // �� �ֱ⿡�� ���۽ð�������
				elapTime = 0; // ���ð� �ʱ�ȭ
				cntTime = startTime; // ����ð��� ������ ���� ����
				oneLoopTime = 240000 / currentBPM;
				if (loopFlag == false) // song����϶��� �� �ֱ⸶�� ���� �������� �Ѿ�鼭 ���
				{

					currentPtn = ptnStream[count];
					Log.i(mytag, "before");
					Message msg = handler.obtainMessage();
					handler.sendMessage(msg);
					Log.i(mytag, "fin3!");
					count++;

				}

				while (elapTime <= oneLoopTime && playFlag == true) {

					cntTime = System.currentTimeMillis();
					elapTime = cntTime - startTime;// ����ð�-���۽ð� �ؼ� ���ð��� ����
					if (elapTime == (oneLoopTime / 16) * (i + 1))// ���ð���
																	// 1�ֱ�*(i/16)��
																	// ��Ȯ�� ���� ���
																	// if������
																	// ����
					{

						
						handler2.sendMessage(handler2.obtainMessage(i));
						for (int j = 0; j < NUM_INST; j++) {

							
							
							
							if (ptn[currentPtn].flags[j][i] == true) {
								playBeat(j);
							}

						}

						i++;// i��° üũ�ڽ��� �о����Ƿ� ���� ���� üũ�ڽ����� �б� ���ؼ� i���� �ϳ� �÷���
					}
				}
			}

		}
	}

	public void stop() {
		playFlag = false;
		for(int i=0;i<4;i++){
			wav_play[i].stop();
			wav_play[i].reset();
		}
		prepareWav();
	}

	public void load(String filePath) throws IOException {
		playFlag = false;

		File file = new File(filePath);

		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader bufferReader = new BufferedReader(
					new InputStreamReader(fis));

			try {

				char temp;
				String str = null;

				str = bufferReader.readLine();
				currentDrum = Integer.parseInt(str);
				drumSpinner.setSelection(currentDrum);
				str = bufferReader.readLine();

				currentBPM = Integer.parseInt(str);
				bpmPicker.setValue(currentBPM);

				str = bufferReader.readLine();
				ptnField.setText(str);
				getPatternStream();

				int[][][] numPtns = new int[10][8][16];

				int i = 0, j = 0, k = 0;
				while (i < 10) {
					str = bufferReader.readLine();
					j = 0;
					while (j < 16) {
						temp = str.charAt(j);
						numPtns[i][0][j] = (int) temp - 48;
						j++;
					}
					j++;
					while (j < 33) {
						temp = str.charAt(j);
						numPtns[i][1][j - 17] = (int) temp - 48;
						j++;
					}
					j++;
					while (j < 50) {
						temp = str.charAt(j);
						numPtns[i][2][j - 34] = (int) temp - 48;
						j++;
					}
					j++;
					while (j < 67) {
						temp = str.charAt(j);
						numPtns[i][3][j - 51] = (int) temp - 48;
						j++;
					}
					j++;
					while (j < 84) {
						temp = str.charAt(j);
						numPtns[i][4][j - 68] = (int) temp - 48;
						j++;
					}
					j++;
					while (j < 101) {
						temp = str.charAt(j);
						numPtns[i][5][j - 85] = (int) temp - 48;
						j++;
					}
					j++;
					while (j < 118) {
						temp = str.charAt(j);
						numPtns[i][6][j - 102] = (int) temp - 48;
						j++;
					}
					j++;
					while (j < 135) {
						temp = str.charAt(j);
						numPtns[i][7][j - 119] = (int) temp - 48;
						j++;
					}
					i++;
				}

				for (i = 0; i < 10; i++) {
					for (j = 0; j < 8; j++) {
						for (k = 0; k < 16; k++) {

							if (numPtns[i][j][k] == 1)
								ptn[i].flags[j][k] = true;
							else
								ptn[i].flags[j][k] = false;
						}

					}
				}
				
				currentPtn = ptnStream[0];
				refresh();
				bufferReader.close();

			} catch (IOException e) {
				(Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT))
						.show();
				Log.i(mytag, "Failed to load");
			}
		} catch (FileNotFoundException e) {
			(Toast.makeText(this, "File not found", Toast.LENGTH_SHORT)).show();
			Log.i(mytag, "File not found");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.beat1, menu);
		return true;
	}

}

class Pattern {

	public boolean[][] flags;

	public Pattern(int n) {
		flags = new boolean[n][16];
	}
}
