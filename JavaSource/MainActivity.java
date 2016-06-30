package com.camshouse;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Switch;

public class MainActivity extends Activity {

	private char light_state = (char)0;
	private TCPClient mTcpClient;
	Switch sw1, sw2, sw3, sw4, sw5, sw6, sw7, sw8;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		sw1 = (Switch) findViewById(R.id.switch1);
		sw2 = (Switch) findViewById(R.id.switch2);
		sw3 = (Switch) findViewById(R.id.switch3);
		sw4 = (Switch) findViewById(R.id.switch4);
		sw5 = (Switch) findViewById(R.id.switch5);
		sw6 = (Switch) findViewById(R.id.switch6);
		sw7 = (Switch) findViewById(R.id.switch7);
		sw8 = (Switch) findViewById(R.id.switch8);
		
		switchListener(sw1,(char)0x01);
		switchListener(sw2,(char)0x02);
		switchListener(sw3,(char)0x04);
		switchListener(sw4,(char)0x08);
		switchListener(sw5,(char)0x10);
		switchListener(sw6,(char)0x20);
		switchListener(sw7,(char)0x40);
		switchListener(sw8,(char)0x80);			
		
		// starts the asynchronous thing that starts the TCP message receive server
		new connectTask().execute("");	
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mTcpClient.stopClient();		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//mTcpClient.stopClient();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/*void switchListener(Switch sw, final char sw_num)
	{
		sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	light_state = (char)(light_state | sw_num);
		        } else {
		        	light_state = (char)(light_state & ~sw_num);
		        }
		        //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(Character.toString(light_state));
                }
		        System.out.println(light_state);
		    }
		});
	}*/
	
	void switchListener(final Switch sw, final char sw_num)
	{
		sw.setOnClickListener(new View.OnClickListener() {
			@Override
            public void onClick(View view) {
		        if (sw.isChecked()) {
		        	light_state = (char)(light_state | sw_num);
		        } else {
		        	light_state = (char)(light_state & ~sw_num);
		        }
		        //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(Character.toString(light_state));
                }
		        System.out.println(light_state);
		    }
		});
	}
	
    public class connectTask extends AsyncTask<String,String,TCPClient> {
    	 
        @Override
        protected TCPClient doInBackground(String... message) {
 
            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();
 
            return null;
        }
 
        @Override
        protected void onProgressUpdate(String... value) {
            super.onProgressUpdate(value);
 
            light_state = (char)value[0].charAt(0);
            
            sw1.setChecked(!((light_state & 0x01) == 0));
            sw2.setChecked(!((light_state & 0x02) == 0));
            sw3.setChecked(!((light_state & 0x04) == 0));
            sw4.setChecked(!((light_state & 0x08) == 0));
            sw5.setChecked(!((light_state & 0x10) == 0));
            sw6.setChecked(!((light_state & 0x20) == 0));
            sw7.setChecked(!((light_state & 0x40) == 0));
            sw8.setChecked(!((light_state & 0x80) == 0));
        }
    }
}
