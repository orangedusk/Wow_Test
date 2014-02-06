package com.example.android.FUECAPP;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PostTrip extends Activity implements OnClickListener{
	//constants
	private final float FUELCOST= 72;
	private final String PREFNAME = "history_pref";
	//Variables
	private Button exit,back,history;
	private TextView titleTextView;
	private TextView idlingFuelTextView,idlingCostTextView,gearShiftFuelTextView,gearShiftCostTextView,totalFuelTextView,totalCostTextView;
	private TextView totalWastageTextView,totalWastageCostTextView,ecoScoreTextView,totalDistanceTextView,tripKMPLTextView;
	private float idlingFuelValue=0;
	private float idlingFuelCost=0;
	private float gearShiftFuelValue=0;
	private float gearShiftFuelCost=0;
	private float totalFuelValue=0;
	private float totalFuelCost=0;
	private float totalFuelWastageValue=0;
	private float totalFuelWastageCost=0;
	private int ecoScore=0;
	private float totalDistance=0;
	private float tripKMPL=0;
	private float historyIdlingFuelValue=0;
	private float historyIdlingFuelCost=0;
	private float historyGearShiftFuelValue=0;
	private float historyGearShiftFuelCost=0;
	private float historyTotalFuelValue=0;
	private float historyTotalFuelCost=0;
	private float historyTotalFuelWastageValue=0;
	private float historyTotalFuelWastageCost=0;
	private long historyEcoScore=0;
	private float historyTotalDistance=0;
	private float historyTripKMPL=0;
	private long historyTripCount=0;
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_trip);
		 exit=(Button) findViewById(R.id.exitButton);
		 exit.setOnClickListener(this);
		 back=(Button) findViewById(R.id.backButton);
		 back.setOnClickListener(this);
		 history=(Button)findViewById(R.id.historyButton);
		 history.setOnClickListener(this);
		 titleTextView=(TextView) findViewById(R.id.textView5); 
		 totalFuelTextView=(TextView) findViewById(R.id.textView8);
		 totalDistanceTextView=(TextView) findViewById(R.id.TextView08);
		 tripKMPLTextView=(TextView) findViewById(R.id.TextView13);
		 totalCostTextView=(TextView) findViewById(R.id.TextView03);
		 idlingFuelTextView= (TextView) findViewById(R.id.TextView05);
		 idlingCostTextView=(TextView) findViewById(R.id.TextView09);
		 gearShiftFuelTextView= (TextView) findViewById(R.id.TextView07);
		 gearShiftCostTextView=(TextView) findViewById(R.id.TextView10);
		 totalWastageTextView=(TextView) findViewById(R.id.TextView06);
		 totalWastageCostTextView=(TextView) findViewById(R.id.TextView11);
		 ecoScoreTextView=(TextView) findViewById(R.id.textView9);
		 
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
			
			idlingFuelValue = extras.getFloat("IdlingFuelValue");
			gearShiftFuelValue=extras.getFloat("GearShiftFuel");
			totalFuelValue=extras.getFloat("TotalFuelConsumed");
			totalDistance=extras.getFloat("TotalDistance");;
			totalFuelWastageValue=extras.getFloat("TotalFuelWasted");
			ecoScore=extras.getInt("TripEcoScore");
			
			totalFuelCost=totalFuelValue*FUELCOST;
			idlingFuelCost=idlingFuelValue*FUELCOST;
			gearShiftFuelCost=gearShiftFuelValue*FUELCOST;
			totalFuelWastageCost=totalFuelWastageValue*FUELCOST;
			if (totalDistance==0 && totalFuelValue==0){
			tripKMPL=0;
			}
			else{
			tripKMPL=(float)totalDistance/totalFuelValue;
			}
			
			}
			
			saveHistory();
			totalDistanceTextView.setText(String.format("%.2f", totalDistance));
			tripKMPLTextView.setText(String.format("%.2f", tripKMPL));			
			totalFuelTextView.setText(String.format("%.2f", totalFuelValue));
			totalCostTextView.setText(String.format("%.2f", totalFuelCost));
			idlingFuelTextView.setText(String.format("%.2f", idlingFuelValue));
			idlingCostTextView.setText(String.format("%.2f", idlingFuelCost));
			gearShiftFuelTextView.setText(String.format("%.2f", gearShiftFuelValue));
			gearShiftCostTextView.setText(String.format("%.2f", gearShiftFuelCost));
			totalWastageTextView.setText(String.format("%.2f", totalFuelWastageValue));
			totalWastageCostTextView.setText(String.format("%.2f", totalFuelWastageCost));
			
			ecoScoreTextView.setText(""+ecoScore);
	}	
	
	private void saveHistory(){
		//get our shared preferences object copy the values
		SharedPreferences pref= getSharedPreferences(PREFNAME,MODE_PRIVATE);
		historyIdlingFuelValue=pref.getFloat("IdlingFuelValue",0);
		historyIdlingFuelCost=pref.getFloat("IdlingFuelCost",0);
		historyGearShiftFuelValue=pref.getFloat("GearShiftFuelValue",0);
		historyGearShiftFuelCost=pref.getFloat("GearShiftFuelCost",0);
		historyTotalFuelValue=pref.getFloat("TotalFuelValue",0);
		historyTotalFuelCost=pref.getFloat("TotalFuelCost",0);
		historyTotalFuelWastageValue=pref.getFloat("TotalFuelWastageValue",0);
		historyTotalFuelWastageCost=pref.getFloat("TotalFuelWastageCost",0);
		historyEcoScore=pref.getLong("EcoScore", 0);
		historyTotalDistance=pref.getFloat("TotalDistance",0);
		historyTripKMPL=pref.getFloat("TripKMPL",0);
		historyTripCount= pref.getLong("TripCount",0);
		
		//update the values
		historyIdlingFuelValue+= idlingFuelValue;
		historyIdlingFuelCost= historyIdlingFuelValue* FUELCOST;
		historyGearShiftFuelValue+=gearShiftFuelValue;
		historyGearShiftFuelCost= historyGearShiftFuelValue* FUELCOST;
		historyTotalFuelValue+=totalFuelValue;
		historyTotalFuelCost=totalFuelValue* FUELCOST;
		historyTotalFuelWastageValue+= totalFuelWastageValue;
		historyTotalFuelWastageCost= historyTotalFuelWastageValue*FUELCOST;
		historyEcoScore= ((historyEcoScore*historyTripCount)+ ecoScore)/(historyTripCount+1);
		historyTotalDistance+=totalDistance;
		if (historyTotalDistance==0 && historyTotalFuelValue==0){
			historyTripKMPL=0;
			}
		else{
			historyTripKMPL=(float)historyTotalDistance/historyTotalFuelValue;
			}
		historyTripCount++;
		
		//create an editor and Save the preferences
		SharedPreferences.Editor edit= pref.edit();
		edit.putFloat("IdlingFuelValue", historyIdlingFuelValue);
		edit.putFloat("IdlingFuelCost", historyIdlingFuelCost);
		edit.putFloat("GearShiftFuelValue", historyGearShiftFuelValue);
		edit.putFloat("GearShiftFuelCost", historyGearShiftFuelCost);
		edit.putFloat("TotalFuelValue", historyTotalFuelValue);
		edit.putFloat("TotalFuelCost", historyTotalFuelCost);
		edit.putFloat("TotalFuelWastageValue", historyTotalFuelWastageValue);
		edit.putFloat("TotalFuelWastageCost", historyTotalFuelWastageCost);
		edit.putLong("EcoScore", historyEcoScore);
		edit.putFloat("TotalDistance", historyTotalDistance);
		edit.putFloat("TripKMPL", historyTripKMPL);
		edit.putLong("TripCount", historyTripCount);
		
		edit.commit();
		
	}
	
		
		
		
	
	public void onClick(View v)	{
		if(v.getId()==R.id.exitButton){		
			moveTaskToBack(true);
			
		}
		else if (v.getId()==R.id.backButton){		
			finish();
		}
		else if (v.getId()==R.id.historyButton){
			history.setVisibility(View.GONE);
			titleTextView.setText(R.string.history_title);
			totalDistanceTextView.setText(String.format("%.2f", historyTotalDistance));
			tripKMPLTextView.setText(String.format("%.2f", historyTripKMPL));			
			totalFuelTextView.setText(String.format("%.2f", historyTotalFuelValue));
			totalCostTextView.setText(String.format("%.2f", historyTotalFuelCost));
			idlingFuelTextView.setText(String.format("%.2f", historyIdlingFuelValue));
			idlingCostTextView.setText(String.format("%.2f", historyIdlingFuelCost));
			gearShiftFuelTextView.setText(String.format("%.2f", historyGearShiftFuelValue));
			gearShiftCostTextView.setText(String.format("%.2f", historyGearShiftFuelCost));
			totalWastageTextView.setText(String.format("%.2f", historyTotalFuelWastageValue));
			totalWastageCostTextView.setText(String.format("%.2f", historyTotalFuelWastageCost));
			ecoScoreTextView.setText(""+historyEcoScore);
			
		}
	}
}
