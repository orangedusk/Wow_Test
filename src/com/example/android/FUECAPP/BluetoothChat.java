/*
 * Copyright (C) 2009 The Android Open Source Project

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* STORING FUNCTION IMPLEMENTATION ADDITION - RNTBCI_001
 * Storing Function has been implemented as per request.
 * All the raw data read from BTdevice is stored in the file named "BtFile.csv" and
 * the decoded car info data is stored in another file named "BtFileDecoded.csv".Both 
 * files are saved to the applications's private storage space in the external storage
 * device(SDCard).
 * 
 * */

package com.example.android.FUECAPP;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
/**
 * @author z003771
 *
 */
public class BluetoothChat extends Activity {
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;
	//pref name
	private final String PREFNAME_IDLING= "idlingTime";
	private final String PREFNAME = "device_address";
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_DISPLAY = 6; //RNTBCI_001

	// Index for columns
	public static final int VEHICLE_SPEED=7;
	public static final int FUEL_CONSUMPTION_FINE=11;
	public static final int DISTANCE_TOTALIZER=22;
	public static final int ENGINE_RPM_180=3;
	public static final int ENGINE_RPM_181=5;
	public static final int IGNITION_SUPPLY=13;
	// Definition of second values
	public static final int TWENTY_SECONDS =200;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	
	
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	//Bluetooth connection states
	private static final int BT_CONNECTED=1;
	private static final int BT_CONNECTING=2;
	private static final int BT_NOT_CONNECTED=3;
	//Variable that holds Bluetooth connection state
	private static int bluetoothState= BT_NOT_CONNECTED;
	/* START-RNTBCI_001*/
	// Constants for GearShift
	public static final int MIN_RPM_GEAR1 = 600;
	public static final int MIN_RPM_GEAR2 = 1000;
	public static final int MIN_RPM_GEAR3 = 1200;
	public static final int MIN_RPM_GEAR4 = 1400;
	public static final int MIN_RPM_GEAR5 = 1600;
	public static final int MAX_RPM_GEAR1 = 1500;
	public static final int MAX_RPM_GEAR2 = 1700;
	public static final int MAX_RPM_GEAR3 = 1800;
	public static final int MAX_RPM_GEAR4 = 1900;
	public static final int MAX_RPM_GEAR5 = 2000;

	//Constants for eco score
	public static final double  GAMMA=1;


	//Message names array is defined such that the message id can be directly hashed
	//to array index
	private String[] messageNameList={"Time",
			"Steering Angle",
			"TransmRangeEngaged_Current",
			"EngineRPM_180",
			"ClutchSwitch_180",
			"EngineRPM_181",
			"ClutchSwitch_181",
			"VehicleSpeed",
			"ExternalTemp",
			"VehicleSpeed_CLUSTER",
			"FuelConsumption",
			"FuelConsumptionFine",
			"ClimateCoolingStatus",
			"IgnitionSupplyConfirmation",
			"CompressorActivation",
			"EngineStatus",
			"IgnitionState(METER)",
			"AverageFuelEconomy",
			"DistanceToEmpty",
			"ClimateCoolingRequest",
			"ClimateCoolingRequest_CLIM",
			"A/C stroke request status(Return)",
			"DistanceTotalizer",
			"FuelLevel",
			"ExternalTemp_84",
			"RawSensor",
			"VirtualAcceleratorAngle_ForTM",
			"VehicleSpeed_CLUSTER (copy)",
			"CloseActiveBrakeSwitch_ABS",
			"AcceleratorPedalPosition for AT and ACC",
			"Brake switch signal",
			"CloseActiveBrakeSwitch_BCM"
	};
	private int[] messageLengthList={0,2,1,2,1,2,1,2,1,2,1,2,1,1,1,1,1,2,2,1,1,1,
			3,1,1,2,1,2,1,1,1,1};
	private double[] bitRate={0,0.1,1,0.125,1,0.125,1,0.01,1,0.01,80,0.3125,1,1,1,1,1,0.1,0.1,1,1,1,1,1,1,0.125,0.392,0.01,1,0.392,1,1};
	private String[] dataArray= new String[32];
	private String[][] tenSecondArray = new String [100][32];

	private long startTimeInMillis;


	private static float speedMps_GS=0;
	private static float fuelConsumptionWrongGear_GS=0;
	private static float fuelConsumptionRightGear_GS=0;
	private static float distanceCoveredWrongGear_GS=0;
	private static float distanceCoveredRightGear_GS=0;
	private static float distanceCoveredWrongGearinKiloMeters_GS=0;
	private static float distanceCoveredRightGearinKiloMeters_GS=0;
	private static float avgFuelConsumptionWrongGear_GS=0;
	private static float avgFuelConsumptionRightGear_GS=0;
	private static float fuelWastedPerKiloMeter_GS=0;
	private static float totalFuelWasted_GS=0;
	private static float totalDistanceCoveredRightGearinKiloMeters_GS ;
	private static float totalDistanceCoveredWrongGearinKiloMeters_GS ;
	private static float totalFuelConsumptionRightGear_GS ;
	private static float totalFuelConsumptionWrongGear_GS;
	private static float fuelWastedTillIgnOff_GS;



	//Intrip Variables
	private static int rpm_index_blink=0;
	private static int idlingSecondCounter=0;
	private static int scoreSecondCounter=0;
	private static int averageEcoScore=0;
	private static int currentEcoScore=0;
	private static int historyEcoScore=0;
	private static int ecoScoreCounter=0;
	private static int greaterThan2000Counter=0;
	private static boolean image1_visibility=false;
	
	
	//Trip Variables
	private static int tripAverageScore=0;
	private static float tripFuelWastageGS=0;
	private static float tripFuelWastageIdling=0;
	private static float tripTotalFuelConsumed=0;
	/*private static float tripFirstDistance=0;
	 * private static boolean firstTimeInTrip=true;
	 *private static float tripLastDistance=0;*/
	private static float tripTotalDistance=0;
	private static float tripTotalFuelWastage=0;
	// Image array to hold the images for Score meter
	private int[] scoreMeterArray={
			R.drawable.meter0,	
			R.drawable.meter1,
			R.drawable.meter2,
			R.drawable.meter3,
			R.drawable.meter4,
			R.drawable.meter5,
			R.drawable.meter6,
			R.drawable.meter7,
			R.drawable.meter8,
			R.drawable.meter9,
			R.drawable.meter10
	};
	//Image Array to hold the images for RPM Meter
	private int[] rpmMeterArray1={
			R.drawable.rpm0_1,
			R.drawable.rpm1_3,
			R.drawable.rpm2_3,
			R.drawable.rpm3_3,
			R.drawable.rpm4_3,
			R.drawable.rpm5_3,
			R.drawable.rpm6_3,
			R.drawable.rpm7_3,
			R.drawable.rpm8_3,
			R.drawable.rpm9_3,
			R.drawable.rpm10_3,
			R.drawable.rpm11_3,
			R.drawable.rpm12_3,
			R.drawable.rpm13_3,
			R.drawable.rpm14_3,
			R.drawable.rpm15_3,
			R.drawable.rpm16_3,
			R.drawable.rpm17_3,
			R.drawable.rpm18_3,
			R.drawable.rpm19_3,
			R.drawable.rpm20_3,
			R.drawable.rpm21_3,
			R.drawable.rpm22_3,
			R.drawable.rpm23_3,
			R.drawable.rpm24_3,
			R.drawable.rpm25_4,
			R.drawable.rpm26_4,
			R.drawable.rpm27_4,
			R.drawable.rpm28_4,
			R.drawable.rpm29_4,
			R.drawable.rpm30_4,
			R.drawable.rpm31_4,
			R.drawable.rpm32_4,
			R.drawable.rpm33_4,
			R.drawable.rpm34_4,
			R.drawable.rpm35_4,
			R.drawable.rpm36_4
	};

	private int[] rpmMeterArray2={
			R.drawable.rpm0_1,
			R.drawable.rpm1_2,
			R.drawable.rpm2_2,
			R.drawable.rpm3_2,
			R.drawable.rpm4_2,
			R.drawable.rpm5_2,
			R.drawable.rpm6_2,
			R.drawable.rpm7_2,
			R.drawable.rpm8_2,
			R.drawable.rpm9_2,
			R.drawable.rpm10_2,
			R.drawable.rpm11_2,
			R.drawable.rpm12_2,
			R.drawable.rpm13_2,
			R.drawable.rpm14_2,
			R.drawable.rpm15_2,
			R.drawable.rpm16_2,
			R.drawable.rpm17_2,
			R.drawable.rpm18_2,
			R.drawable.rpm19_2,
			R.drawable.rpm20_2,
			R.drawable.rpm21_2,
			R.drawable.rpm22_2,
			R.drawable.rpm23_2,
			R.drawable.rpm24_2,
			R.drawable.rpm25_3,
			R.drawable.rpm26_3,
			R.drawable.rpm27_3,
			R.drawable.rpm28_3,
			R.drawable.rpm29_3,
			R.drawable.rpm30_3,
			R.drawable.rpm31_3,
			R.drawable.rpm32_3,
			R.drawable.rpm33_3,
			R.drawable.rpm34_3,
			R.drawable.rpm35_3,
			R.drawable.rpm36_3
	};

	private int[] rpmMeterArray3={
			R.drawable.rpm0_1,
			R.drawable.rpm1_2,
			R.drawable.rpm2_2,
			R.drawable.rpm3_2,
			R.drawable.rpm4_2,
			R.drawable.rpm5_2,
			R.drawable.rpm6_2,
			R.drawable.rpm7_2,
			R.drawable.rpm8_2,
			R.drawable.rpm9_2,
			R.drawable.rpm10_2,
			R.drawable.rpm11_2,
			R.drawable.rpm12_2,
			R.drawable.rpm13_2,
			R.drawable.rpm14_2,
			R.drawable.rpm15_2,
			R.drawable.rpm16_2,
			R.drawable.rpm17_2,
			R.drawable.rpm18_2,
			R.drawable.rpm19_2,
			R.drawable.rpm20_2,
			R.drawable.rpm21_2,
			R.drawable.rpm22_2,
			R.drawable.rpm23_2,
			R.drawable.rpm24_3,
			R.drawable.rpm25_5,
			R.drawable.rpm26_5,
			R.drawable.rpm27_5,
			R.drawable.rpm28_5,
			R.drawable.rpm29_5,
			R.drawable.rpm30_5,
			R.drawable.rpm31_5,
			R.drawable.rpm32_5,
			R.drawable.rpm33_5,
			R.drawable.rpm34_5,
			R.drawable.rpm35_5,
			R.drawable.rpm36_5
	};
	private int[] idlingArray={
			R.drawable.idling_0,
			R.drawable.idling_1,
			R.drawable.idling_2,
			R.drawable.idling_3,
			R.drawable.idling_4,
			R.drawable.idling_5,
			R.drawable.idling_6,
			R.drawable.idling_7,
			R.drawable.idling_8,
			R.drawable.idling_9,
			R.drawable.idling_10,
			R.drawable.idling_11,
			R.drawable.idling_12,
			R.drawable.idling_13,
			R.drawable.idling_14,
			R.drawable.idling_15,
			R.drawable.idling_16,
			R.drawable.idling_17,
			R.drawable.idling_18,
			R.drawable.idling_19,
			R.drawable.idling_20,
			R.drawable.idling_21,
			R.drawable.idling_22,
			R.drawable.idling_23,
			R.drawable.idling_24,
			R.drawable.idling_25,
			R.drawable.idling_26,
			R.drawable.idling_27,
			R.drawable.idling_28,
			R.drawable.idling_29,
			R.drawable.idling_30,
			R.drawable.idling_31,
			R.drawable.idling_32,
			R.drawable.idling_33,
			R.drawable.idling_34,
			R.drawable.idling_35,
			R.drawable.idling_36
			
	};



	/* END-RNTBCI_001*/
	// Layout Views
	private Button mStartButton,mSettingsButton;
	private TextView mScore,mTitle;
	private MediaPlayer player;
	private ImageView image1,image2;
	

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.in_trip);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		//Keep brightness as 100% 
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = 100 / 100.0f;
		getWindow().setAttributes(lp);
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		//		mExitButton=(Button) findViewById(R.id.exitButton);
		mScore = (TextView) findViewById(R.id.scoreTextView);
		image1=(ImageView) findViewById(R.id.ImageView1);
		image2=(ImageView) findViewById(R.id.ImageView2);
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		//Initialize all variables
		ecoScoreCounter=0;
		idlingSecondCounter=0;
		scoreSecondCounter=0;	
		rpm_index_blink=0;
		averageEcoScore=0;
		currentEcoScore=0;
		historyEcoScore=0;
		//Post Trip Variables
		tripFuelWastageGS=0;
		tripFuelWastageIdling=0;
		tripTotalFuelConsumed=0;
		tripTotalDistance=0;
//		tripFirstDistance=0;
//		tripLastDistance=0;
		tripTotalFuelWastage=0;
		greaterThan2000Counter=0;
		tripAverageScore=0;
//		firstTimeInTrip=true;
		startTimeInMillis=System.currentTimeMillis();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	private void playAudio(int id)
	{
		player= MediaPlayer.create(getApplicationContext(),id);
		player.setLooping(false);
		if(player.isPlaying()){
			player.pause();
			player.start();
		}
		else{
			player.start();
		}

	}
	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the Settings button with a listener  for click events
		mSettingsButton= (Button) findViewById(R.id.settingsButton);
		mSettingsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent= new Intent (BluetoothChat.this,Settings.class);
				startActivity(intent);
				
			}
				
			});

		// Initialize the START button with a listener  for click events
		mStartButton = (Button) findViewById(R.id.startButton);
		mStartButton.setTag(1);
		mStartButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/*Button b = (Button)v;
				String text = b.getText().toString();
				if (text.equals(R.string.start))*/
				final int status =(Integer) v.getTag();
				if(status == 1) {
					
					if (bluetoothState==BT_NOT_CONNECTED){
						
						initiateBtConnection();					
						
					}
					else if (bluetoothState==BT_CONNECTED){
						send_start_command();
						
					}
					
					} 
				else {
					
				    String message = "0220DF";
					sendMessage(message);
					calculatePosttrip();
					mStartButton.setText("Start");
					mStartButton.setTag(1); //start
					mSettingsButton.setVisibility(View.VISIBLE);
					finish();
				} 			
			}
		});

				




		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

    private void initiateBtConnection(){
    	SharedPreferences pref= getSharedPreferences(PREFNAME,MODE_PRIVATE);
		String address= pref.getString("deviceAddress","null");
		if (!(address.contentEquals("null"))){
			// Get the BLuetoothDevice object
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			// Attempt to connect to the device
			mChatService.connect(device, true);
		}
		else
			{
			Toast.makeText(getBaseContext(), "First Attempt!!! Please select device",
					Toast.LENGTH_SHORT).show();
			
			}
    }
    



	/** Gear Shift post trip logic
	 * Calculates the post-trip Fuel wastage due to Gear Shift
	 * @param value
	 */

	/*private void gearShiftPostTrip(String[] value)
	{
		// local variables used for GearShift_Post-trip

		float fuelValue_GS = Float.parseFloat(value[10]);
		float speedValue_GS = Float.parseFloat(value[6]);
		float rpmValue_GS = Float.parseFloat(value[2]);
		String ignitionSupply_GS = value[12];


		if(ignitionSupply_GS.equals("Primary ignition supply confirmed"))
		{
			//for speed 0 ~ 20
			if(speedValue_GS >= 0 && speedValue_GS <= 20)
			{
				minRpm_GS = MIN_RPM_GEAR1;
				maxRpm_GS = MAX_RPM_GEAR1;
			}
			//for speed 20 ~ 40
			else if(speedValue_GS > 20 && speedValue_GS <= 40)
			{
				minRpm_GS = MIN_RPM_GEAR2;
				maxRpm_GS = MAX_RPM_GEAR2;
			}
			//for speed 40 ~ 50
			else if(speedValue_GS > 40 && speedValue_GS <= 50)
			{
				minRpm_GS = MIN_RPM_GEAR3;
				maxRpm_GS = MAX_RPM_GEAR3;
			}
			//for speed 50 ~ 60
			else if(speedValue_GS > 50 && speedValue_GS <= 60)
			{
				minRpm_GS = MIN_RPM_GEAR4;
				maxRpm_GS = MAX_RPM_GEAR4;
			}
			//for speed > 60
			else if(speedValue_GS > 60)
			{
				minRpm_GS = MIN_RPM_GEAR5;
				maxRpm_GS = MAX_RPM_GEAR5;
			}

			// if current RPM is in band, assume right gear
			if(rpmValue_GS >= minRpm_GS && rpmValue_GS <= maxRpm_GS)
			{
				speedMps_GS = speedValue_GS * 5 / 18;
				if(wrongGearFlag == 1)
				{
					wrongGearFlag = 0;
					rightGearFlag = 1;
					fuelConsumptionWrongGear_GS = fuelConsumptionWrongGear_GS + fuelValue_GS;
				}
				else
				{
					if(rightGearFlag == 0)
					{
						rightGearFlag = 1;
					}
				}
				fuelConsumptionRightGear_GS = fuelConsumptionRightGear_GS + fuelValue_GS;
				distanceCoveredRightGear_GS = distanceCoveredRightGear_GS + (float)(speedMps_GS * 0.1); // distance = speed * time; time = 100ms
			}
			// if current RPM is out of band, assume wrong gear
			else if(rpmValue_GS < minRpm_GS || rpmValue_GS > maxRpm_GS)
			{
				speedMps_GS = speedValue_GS * 5 / 18;
				if(rightGearFlag == 1)
				{
					rightGearFlag = 0;
					wrongGearFlag = 1;
					fuelConsumptionRightGear_GS = fuelConsumptionRightGear_GS + fuelValue_GS;
				}
				else
				{
					if(wrongGearFlag == 0)
					{
						wrongGearFlag = 1;
					}
				}
				fuelConsumptionWrongGear_GS = fuelConsumptionWrongGear_GS + fuelValue_GS;
				distanceCoveredWrongGear_GS = distanceCoveredWrongGear_GS + (float)(speedMps_GS * 0.1); // distance = speed * time; time = 100ms
			}
		}
		else if(ignitionSupply_GS.equals("No primary ignition supply"))
		{
			rightGearFlag = 0;
			wrongGearFlag = 0;
			distanceCoveredRightGearinKiloMeters_GS = distanceCoveredRightGear_GS / 1000;
			distanceCoveredWrongGearinKiloMeters_GS = distanceCoveredWrongGear_GS / 1000;
			fuelConsumptionRightGear_GS = fuelConsumptionRightGear_GS / 1000000;
			fuelConsumptionWrongGear_GS = fuelConsumptionWrongGear_GS / 1000000;
			if(distanceCoveredRightGearinKiloMeters_GS !=0)
			{
				avgFuelConsumptionRightGear_GS = fuelConsumptionRightGear_GS / distanceCoveredRightGearinKiloMeters_GS;
			}
			if(distanceCoveredWrongGearinKiloMeters_GS !=0)
			{
				avgFuelConsumptionWrongGear_GS = fuelConsumptionWrongGear_GS / distanceCoveredWrongGearinKiloMeters_GS;
			}

			if(avgFuelConsumptionWrongGear_GS >= avgFuelConsumptionRightGear_GS)
			{
				fuelWastedPerKiloMeter_GS = avgFuelConsumptionWrongGear_GS - avgFuelConsumptionRightGear_GS;
				fuelWastedTillIgnOff_GS = distanceCoveredWrongGearinKiloMeters_GS * fuelWastedPerKiloMeter_GS;
			}
			totalDistanceCoveredRightGearinKiloMeters_GS = totalDistanceCoveredRightGearinKiloMeters_GS + distanceCoveredRightGearinKiloMeters_GS;
			totalDistanceCoveredWrongGearinKiloMeters_GS = totalDistanceCoveredWrongGearinKiloMeters_GS + distanceCoveredWrongGearinKiloMeters_GS;
			totalFuelConsumptionRightGear_GS = totalFuelConsumptionRightGear_GS + fuelConsumptionRightGear_GS;
			totalFuelConsumptionWrongGear_GS = totalFuelConsumptionWrongGear_GS + fuelConsumptionWrongGear_GS;
			totalFuelWasted_GS = totalFuelWasted_GS + fuelWastedTillIgnOff_GS;
			fuelConsumptionWrongGear_GS = 0;
			fuelConsumptionRightGear_GS = 0;
			distanceCoveredWrongGear_GS = 0;
			distanceCoveredRightGear_GS = 0;
			distanceCoveredWrongGearinKiloMeters_GS = 0;
			distanceCoveredRightGearinKiloMeters_GS = 0;
		}	
	}
*/
	/** Method to calculate the post trip score and and start 
	 * the post trip activity
	 * 
	 */
	public void calculatePosttrip(){



//		tripTotalDistance=tripLastDistance-tripFirstDistance;
		tripAverageScore= (int)((1- ((GAMMA*tripTotalFuelWastage)/tripTotalFuelConsumed))*100);
		Intent intent= new Intent (BluetoothChat.this,PostTrip.class);
		intent.putExtra("IdlingFuelValue",tripFuelWastageIdling);
		intent.putExtra("GearShiftFuel", tripFuelWastageGS);
		intent.putExtra("TotalDistance", tripTotalDistance);
		intent.putExtra("TotalFuelConsumed",tripTotalFuelConsumed);
		intent.putExtra("TotalFuelWasted",tripTotalFuelWastage);
		intent.putExtra("TripEcoScore",tripAverageScore);

		startActivity(intent);
		String text = "Moving to Post Trip Screen";
		showMessage(text);

	}



	/**
	 * Method to display text in a toast
	 * @param text
	 */
	private void showMessage(CharSequence text)
	{
		Context context= getApplicationContext();
		int duration= Toast.LENGTH_SHORT;
		Toast toast= Toast.makeText(context,text,duration);
		toast.show();


	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
			.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);

		}
	}



	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					bluetoothState=BT_CONNECTED;
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					send_start_command();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					bluetoothState=BT_CONNECTING;
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					bluetoothState=BT_NOT_CONNECTED;
					initiateBtConnection();
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
//				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
//				String writeMessage = new String(writeBuf);



				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);


				storeMessage(readMessage); /* RNTBCI_001 */
				storeDecodedMessage(readMessage); /* RNTBCI_001 */
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;

			}
		}
	};

	/* START-RNTBCI_001 ***************************************/


	/**
	 * Sends the start command 
	 */
	private void send_start_command()
	{
		
		String message = "0210EF";
		sendMessage(message);
	}

	/** 
	 * Stores the raw message to external storage file.
	 * 
	 * @param message
	 *            A string of text to store.
	 * 
	 */

	private void storeMessage(String message)  {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try{
				File path= getExternalFilesDir(null);
				File outfile= new File(path,"BtFile.csv");
				if (!outfile.exists())
				{
					outfile.createNewFile();
				}

				BufferedWriter buf = new BufferedWriter(new FileWriter(outfile, true)); 
				buf.append(message);
				buf.newLine();
				buf.flush();
				buf.close();
			}
			catch (Exception e)
			{
				Toast.makeText(getBaseContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}


		}
		else
		{
			Toast.makeText(getBaseContext(), "External Storage Not Mounted",
					Toast.LENGTH_SHORT).show();
		}
	}


	/** 
	 * Stores the decoded message to external storage file.
	 * Each message is decoded and stored according to message id.
	 * Each message set is also  timestamped with the device's system time.
	 * @param message
	 *            A string of text to store.
	 * 
	 */

	private void storeDecodedMessage(String message)  {

		byte[] messageBytes= message.getBytes();
		char[][] messageArray= new char[messageNameList.length][];
		int i,j,length=0,serviceCode=0;
		byte messageId=0,byteValue=0,calculatedCheckSum=0,receivedCheckSum=0;
		long systemTime=System.currentTimeMillis();

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try{
				File path= getExternalFilesDir(null);
				File outfile= new File(path,"BtFileDecoded_"+startTimeInMillis+".csv");
				if (!outfile.exists())
				{
					outfile.createNewFile();
					BufferedWriter buf = new BufferedWriter(new FileWriter(outfile, true)); 
					for( i=0;i<messageNameList.length;i++ )
					{
						buf.append(messageNameList[i]);
						buf.append(",");
					}
					buf.newLine();
					buf.flush();
					buf.close();
				}

				//To find the length of the message 
				length= getCharValueOf(messageBytes[0],messageBytes[1]);

				length*=2;
				serviceCode=getCharValueOf(messageBytes[2],messageBytes[3]);
				if (serviceCode==0x50)
				{	
					//change the button name to 'stop' after receiving valid data
					
					if((Integer) mStartButton.getTag() == 1){
					mStartButton.setText("Stop");
					mStartButton.setTag(0); //stop	
					mSettingsButton.setVisibility(View.INVISIBLE);
					}
					
					//Calculate Checksum
					for(i=2;i<length;i+=2)
					{
						byteValue+=getCharValueOf(messageBytes[i],messageBytes[i+1]);
					}
					calculatedCheckSum=(byte)(byteValue^0xFF);
					receivedCheckSum=getCharValueOf(messageBytes[i],messageBytes[i+1]);
					//Verify Checksum
					if(calculatedCheckSum==receivedCheckSum)
					{
						/*	mCollecting.post(new Runnable(){
        				public void run(){
        					mCollecting.setText("Collecting data...");
        				}

        			});*/
						for(i=4;i<(length-2);)
						{
							messageId= getCharValueOf(messageBytes[i],messageBytes[i+1]);
							messageArray[messageId]=new char[messageLengthList[messageId]*2];
							for (j=0;j<messageLengthList[messageId]*2;j++)
							{

								messageArray[messageId][j]=(char)messageBytes[i+j+2];
							}	
							i+=2+(messageLengthList[messageId]*2);
						}


						BufferedWriter buf = new BufferedWriter(new FileWriter(outfile, true));
						buf.append(""+systemTime+",");  
						dataArray[0]=""+systemTime;
						for(i=1;i<messageLengthList.length;i++)
						{
							if (messageArray[i]!=null)
							{
								for(j=0;j<messageLengthList[i]*2;j++)
								{

									buf.append(messageArray[i][j]);

								}
								dataArray[i]= new String(messageArray[i]);
								     
							}
							buf.append(",");
						}
						doInTrip();

						buf.newLine();
						buf.flush();
						buf.close();
					}
					else
					{
						Toast.makeText(getBaseContext(), "Checksum Failure",
								Toast.LENGTH_SHORT).show();
					}

				}
				else if (serviceCode==0x7F)
				{
					Toast.makeText(getBaseContext(), "Please Start Engine",
							Toast.LENGTH_SHORT).show();
					send_start_command();
				}
				else if (serviceCode==0x60)
				{
					Toast.makeText(getBaseContext(), "STOP ACK Received",
							Toast.LENGTH_SHORT).show();
					//Move to posttrip screen only after receiving stop acknowledgement
					
				/*	  calculatePosttrip();
					  mStartButton.setText("Start");
					  mStartButton.setTag(1); //start
				 */					 
								
				}
				else
				{
					Toast.makeText(getBaseContext(), "Wrong service code",
							Toast.LENGTH_SHORT).show();
				}
			}
			catch (Exception e)
			{
				Toast.makeText(getBaseContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}


		}
		else
		{
			Toast.makeText(getBaseContext(), "External Storage Not Mounted",
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Reads the 2 parameters as  ascii values. 
	 * Decodes them and combines the nibbles into a 1 byte data and returns it. 
	 * @param a
	 * @param b
	 */
	byte getCharValueOf(byte a, byte b)
	{
		byte c;
		if(a>=0x41 && a<=0x46){
			c=(byte)((a-0x41+10)<<4);
		}else{
			c=(byte)((a-0x30)<<4);		
		}
		if(b>=0x41 && b<=0x46){
			c|=(b-0x41+10);
		}else{
			c|=(b-0x30);		
		}
		return c; 
	}


	/**
	 * Method that does the following intrip actions:
	 * - calculate the realtime ECO score every 10 seconds and update 
	 * 	 the intrip screen.
	 * - Update  the RPM meter every 100 ms.
	 * - Display the Idling warning and Gear Shift warnings 
	 */
	private void doInTrip(){
		new Thread(new Runnable(){
			@Override	
			public void run(){



				tenSecondArray[scoreSecondCounter]=dataArray;
				/*if (firstTimeInTrip==true)  	{
					tripFirstDistance=(float)(Integer.parseInt(tenSecondArray[scoreSecondCounter][DISTANCE_TOTALIZER],16)*bitRate[DISTANCE_TOTALIZER]);
					firstTimeInTrip=false;
				}
				tripLastDistance=(float)(Integer.parseInt(tenSecondArray[scoreSecondCounter][DISTANCE_TOTALIZER],16)*bitRate[DISTANCE_TOTALIZER]);*/
				final float speed_value = (float)(Integer.parseInt(tenSecondArray[scoreSecondCounter][VEHICLE_SPEED],16)*bitRate[VEHICLE_SPEED]);
				tripTotalDistance+= speed_value/36000; //calculate distance traveled every 100 ms
				float rpm_value_temp;
				if (!(tenSecondArray[scoreSecondCounter][ENGINE_RPM_180].equals(""))){
					rpm_value_temp=(float)(Integer.parseInt(tenSecondArray[scoreSecondCounter][ENGINE_RPM_180],16)*bitRate[ENGINE_RPM_180]);
				}
				else{
					rpm_value_temp=(float)(Integer.parseInt(tenSecondArray[scoreSecondCounter][ENGINE_RPM_181],16)*bitRate[ENGINE_RPM_181]);
				}			
				final float rpm_value= rpm_value_temp;
				final float fuel_value= (float)(Integer.parseInt(tenSecondArray[scoreSecondCounter][FUEL_CONSUMPTION_FINE],16)*bitRate[FUEL_CONSUMPTION_FINE]);
				final int ignitionStatus= (int)(Integer.parseInt(tenSecondArray[scoreSecondCounter][IGNITION_SUPPLY],16)*bitRate[IGNITION_SUPPLY]);
				scoreSecondCounter++;

				if (scoreSecondCounter>=100){
					currentEcoScore=calculateEcoScore(idlingSecondCounter,tenSecondArray);
					scoreSecondCounter=0;
					averageEcoScore=((ecoScoreCounter*historyEcoScore)+currentEcoScore)/(ecoScoreCounter+1);
					ecoScoreCounter++;
					

					historyEcoScore=averageEcoScore;
					
					mScore.post(new Runnable(){
						@Override
						public void run(){
							mScore.setText(""+averageEcoScore);
						}
					});
					image1.post(new Runnable(){
						@Override
						public void run(){
							int score_index=(int)averageEcoScore/10;
							image1.setImageResource(scoreMeterArray[score_index]);
						}

					});
				}
				if(ignitionStatus==1){
					if((int)speed_value==0)
					{
						idlingSecondCounter++;
						if (idlingSecondCounter>50){
						image1_visibility=true;
						}
						
					}
					else
					{
						idlingSecondCounter=0;
						image1_visibility=false;
						
					}
					
					image1.post(new Runnable(){
	        			@Override
	        			public void run(){
	        				if (image1_visibility==true){
	        					image1.setVisibility(View.VISIBLE);// Visible
	        				}
	        				else
	        				{
	        					image1.setVisibility(View.GONE);// Invisible
	        				}
	        				}
	        				
	        			});
					if ( idlingSecondCounter>0){

						image2.post(new Runnable(){
							public void run(){
								greaterThan2000Counter=0;	
								int rpm_index= (int)((rpm_value)/3000*36);
								if (rpm_index<0){
									rpm_index=0;
								}
								if (rpm_index>36){
									rpm_index=36;
								}
								
								SharedPreferences pref= getSharedPreferences(PREFNAME_IDLING,MODE_PRIVATE);
								int idlingTime= pref.getInt("idlingTime", 20);
								
								if (idlingSecondCounter<=(idlingTime*10)){
									image2.setImageResource(rpmMeterArray1[rpm_index]);

								}
								else
								{
									if (idlingSecondCounter%10==0){
										playAudio(R.raw.beep_10);
									}
									if((idlingSecondCounter/10)%2==1){
										image2.setImageResource(idlingArray[rpm_index]);

									}
									else
									{
										image2.setImageResource(rpmMeterArray1[rpm_index]);
									}
								}

							}
						});

					}

					else{
						image2.post(new Runnable(){
							public void run(){
								int rpm_index= (int)((rpm_value)/3000*36);
								if (rpm_index<0){
									rpm_index=0;
								}
								if (rpm_index>36){
									rpm_index=36;
								}
								if (rpm_index<=24){
									greaterThan2000Counter=0;
									image2.setImageResource(rpmMeterArray2[rpm_index]);
								}
								


								else if((rpm_index>24)){
									if (fuel_value==0)
									{
										image2.setImageResource(rpmMeterArray3[rpm_index]);
										greaterThan2000Counter=0;
									}
									else{
										greaterThan2000Counter++;
										if (greaterThan2000Counter<30)
										{
											image2.setImageResource(rpmMeterArray2[rpm_index]);
										}
										else if (greaterThan2000Counter>=30 && greaterThan2000Counter<60 )
										{

											if (greaterThan2000Counter%10==0){
												playAudio(R.raw.beep_10);
												
											}
											image2.setImageResource(rpmMeterArray2[rpm_index]);

										}
										else if(greaterThan2000Counter>=60 )
										{
											if (greaterThan2000Counter%5==0 && greaterThan2000Counter<150){
												playAudio(R.raw.beep_10);
											}
											if (rpm_index_blink==1){
												image2.setImageResource(rpmMeterArray2[rpm_index]);
												rpm_index_blink=0;
											}
											else if (rpm_index_blink==0){
												image2.setImageResource(rpmMeterArray1[rpm_index]);
												rpm_index_blink=1;
											}
											

										}

									}
								}
							}   				
						});							
					} 
				}


				else{
					image2.post(new Runnable(){
						public void run(){
							image2.setImageResource(R.drawable.rpm0_start);
						}
					});

				}

			}



		}).start();
	}



	/**
	 * Method to calculate the ECO score every ten seconds
	 * @param idlingSecondCounter
	 * @param valueArray
	 * @return
	 */
	private int calculateEcoScore(int idlingSecondCounter,String[][] valueArray){
		float fuelWastageGS=0;
		float fuelWastageIdling=0;
		float totalFuelConsumed=0;
		float fuelWastageTotal=0;
		fuelConsumptionWrongGear_GS=0;
		fuelConsumptionRightGear_GS=0;
		distanceCoveredWrongGear_GS=0;
		distanceCoveredRightGear_GS=0;
		distanceCoveredWrongGearinKiloMeters_GS=0;
		distanceCoveredRightGearinKiloMeters_GS=0;
		avgFuelConsumptionWrongGear_GS=0;
		avgFuelConsumptionRightGear_GS=0;
		fuelWastedPerKiloMeter_GS=0;
		totalFuelWasted_GS=0;
		totalDistanceCoveredRightGearinKiloMeters_GS =0;
		totalDistanceCoveredWrongGearinKiloMeters_GS =0;
		totalFuelConsumptionRightGear_GS =0;
		totalFuelConsumptionWrongGear_GS=0;
		fuelWastedTillIgnOff_GS=0;			
		for (int i=0;i<100;i++){
			calculateGearShiftFuelWastage(i,valueArray[i]);

			if (((float)(Integer.parseInt(valueArray[i][VEHICLE_SPEED],16)*bitRate[VEHICLE_SPEED])== (float)0.0) && (idlingSecondCounter>200))
			{
				fuelWastageIdling+= (float)(Integer.parseInt(valueArray[i][FUEL_CONSUMPTION_FINE],16)*bitRate[FUEL_CONSUMPTION_FINE]);
			}
			totalFuelConsumed+=(float)(Integer.parseInt(valueArray[i][FUEL_CONSUMPTION_FINE],16)*bitRate[FUEL_CONSUMPTION_FINE]);

		}


		fuelWastageIdling/=1000000;
		totalFuelConsumed/=1000000;
		tripFuelWastageIdling+=fuelWastageIdling;
		tripTotalFuelConsumed+=totalFuelConsumed;
		tripFuelWastageGS+=totalFuelWasted_GS;
		fuelWastageGS=totalFuelWasted_GS;
		fuelWastageTotal= fuelWastageGS+fuelWastageIdling;
		tripTotalFuelWastage+=fuelWastageTotal;
		int ecoScore= (int)((1- ((GAMMA*fuelWastageTotal)/totalFuelConsumed))*100);
		return ecoScore;
	}



	/**
	 * Method to calculate the Intrip Gear Shift Fuel Wastage
	 * @param i
	 * @param rowArray
	 */
	private void calculateGearShiftFuelWastage(int i,String[] rowArray){

		// variables used for GearShift_Post-trip

		float fuelValue_GS = (float)(Integer.parseInt(rowArray[FUEL_CONSUMPTION_FINE],16)*bitRate[FUEL_CONSUMPTION_FINE]);
		float speedValue_GS = (float)(Integer.parseInt(rowArray[VEHICLE_SPEED],16)*bitRate[VEHICLE_SPEED]);
		float rpmValue_GS_temp;
		if (!(rowArray[ENGINE_RPM_180].equals(""))){
			rpmValue_GS_temp=(float)(Integer.parseInt(rowArray[ENGINE_RPM_180],16)*bitRate[ENGINE_RPM_180]);
		}
		else{
			rpmValue_GS_temp=(float)(Integer.parseInt(rowArray[ENGINE_RPM_181],16)*bitRate[ENGINE_RPM_181]);
		}		
		float rpmValue_GS = rpmValue_GS_temp;
		int ignitionSupply_GS= (int)(Integer.parseInt(rowArray[IGNITION_SUPPLY],16)*bitRate[IGNITION_SUPPLY]);
		speedMps_GS = speedValue_GS * 5 / 18;
		if(ignitionSupply_GS==1)
		{
			if(rpmValue_GS <= 2000)
			{
				/*//for speed 0 ~ 20
				if(speedValue_GS >= 0 && speedValue_GS <= 20)
				{
					minRpm_GS = MIN_RPM_GEAR1;
					maxRpm_GS = MAX_RPM_GEAR1;
				}
				//for speed 20 ~ 40
				else if(speedValue_GS > 20 && speedValue_GS <= 40)
				{
					minRpm_GS = MIN_RPM_GEAR2;
					maxRpm_GS = MAX_RPM_GEAR2;
				}
				//for speed 40 ~ 50
				else if(speedValue_GS > 40 && speedValue_GS <= 50)
				{
					minRpm_GS = MIN_RPM_GEAR3;
					maxRpm_GS = MAX_RPM_GEAR3;
				}
				//for speed 50 ~ 60
				else if(speedValue_GS > 50 && speedValue_GS <= 60)
				{
					minRpm_GS = MIN_RPM_GEAR4;
					maxRpm_GS = MAX_RPM_GEAR4;
				}
				//for speed > 60
				else if(speedValue_GS > 60)
				{
					minRpm_GS = MIN_RPM_GEAR5;
					maxRpm_GS = MAX_RPM_GEAR5;
				}
				*/
				// if current RPM is in band, assume right gear
				
					
				fuelConsumptionRightGear_GS = fuelConsumptionRightGear_GS + fuelValue_GS;
				distanceCoveredRightGear_GS = distanceCoveredRightGear_GS + (float)(speedMps_GS * 0.1); // distance = speed * time; time = 100ms
				
			}
			// if current RPM is out of band, assume wrong gear
			else
			{
				
				fuelConsumptionWrongGear_GS = fuelConsumptionWrongGear_GS + fuelValue_GS;
				distanceCoveredWrongGear_GS = distanceCoveredWrongGear_GS + (float)(speedMps_GS * 0.1); // distance = speed * time; time = 100ms
			}
		}
		if (i==99)
		{
			distanceCoveredRightGearinKiloMeters_GS = distanceCoveredRightGear_GS / 1000;
			distanceCoveredWrongGearinKiloMeters_GS = distanceCoveredWrongGear_GS / 1000;
			fuelConsumptionRightGear_GS = fuelConsumptionRightGear_GS / 1000000;
			fuelConsumptionWrongGear_GS = fuelConsumptionWrongGear_GS / 1000000;
			if(distanceCoveredRightGearinKiloMeters_GS !=0)
			{
				avgFuelConsumptionRightGear_GS = fuelConsumptionRightGear_GS / distanceCoveredRightGearinKiloMeters_GS;
			}
			if(distanceCoveredWrongGearinKiloMeters_GS !=0)
			{
				avgFuelConsumptionWrongGear_GS = fuelConsumptionWrongGear_GS / distanceCoveredWrongGearinKiloMeters_GS;
			}

			if(avgFuelConsumptionWrongGear_GS >= avgFuelConsumptionRightGear_GS)
			{
				fuelWastedPerKiloMeter_GS = avgFuelConsumptionWrongGear_GS - avgFuelConsumptionRightGear_GS;
				fuelWastedTillIgnOff_GS = distanceCoveredWrongGearinKiloMeters_GS * fuelWastedPerKiloMeter_GS;
			}
			totalDistanceCoveredRightGearinKiloMeters_GS = totalDistanceCoveredRightGearinKiloMeters_GS + distanceCoveredRightGearinKiloMeters_GS;
			totalDistanceCoveredWrongGearinKiloMeters_GS = totalDistanceCoveredWrongGearinKiloMeters_GS + distanceCoveredWrongGearinKiloMeters_GS;
			totalFuelConsumptionRightGear_GS = totalFuelConsumptionRightGear_GS + fuelConsumptionRightGear_GS;
			totalFuelConsumptionWrongGear_GS = totalFuelConsumptionWrongGear_GS + fuelConsumptionWrongGear_GS;
			totalFuelWasted_GS = totalFuelWasted_GS + fuelWastedTillIgnOff_GS;
			fuelConsumptionWrongGear_GS = 0;
			fuelConsumptionRightGear_GS = 0;
			distanceCoveredWrongGear_GS = 0;
			distanceCoveredRightGear_GS = 0;
			distanceCoveredWrongGearinKiloMeters_GS = 0;
			distanceCoveredRightGearinKiloMeters_GS = 0;
		}	



	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		SharedPreferences pref= getSharedPreferences(PREFNAME,MODE_PRIVATE);
		SharedPreferences.Editor editor= pref.edit();
		editor.putString("deviceAddress", address);
		editor.commit();		
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		case R.id.insecure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent,
					REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

}
