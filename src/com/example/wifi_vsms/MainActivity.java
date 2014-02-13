package com.example.wifi_vsms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {
	
	WifiManager wifiManager;
	List<ScanResult> wifiList;
	String received_msg;
	TextView tv;
	WifiManager w;
	String FILENAME = "messages"; //the file where received messages will be stored
	String FILENAME_s = "sent"; //the file where sent messages are stored
	FileOutputStream fos;
	FileInputStream fis;
	String string = "this\n";
	String[]  arr = new String[100];
	LaunchpadSectionFragment LSF;
	LaunchpadSectionSent LSS;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//initialising layouts
		LSF = new LaunchpadSectionFragment();
		LSS = new LaunchpadSectionSent();
		
		//Wifimanager initialisation
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	
    	//starting the message receive process
    	recMessagethread();
		
    	//reading from file to display previous messages
    	read_from_file();
		
		setContentView(R.layout.activity_main);
		
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		
		
		//setContentView(R.layout.fragment_section_launchpad);
		//View inflatedView = getLayoutInflater().inflate(R.layout.fragment_section_launchpad, null);
		//TextView text = (TextView) inflatedView.findViewById(R.id.text_view);
		//ListView l=(ListView) inflatedView.findViewById(R.id.listView1);
		//ListView l=(ListView) findViewById(R.id.listView1);
		//adapter = new ArrayAdapter<String>(inflatedView.getContext() , android.R.layout.simple_list_item_1, list_arr);
		//adapter = new ArrayAdapter<String>(this , android.R.layout.simple_list_item_1, list_arr);
		//adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list_arr);
        //l.setAdapter(adapter);
        //setContentView(R.layout.activity_main);
		

	}
	
	//This is called when the application goes in the background
	protected void onPause()  {
    	super.onPause();
    	//Log.i("pause", "onpause called");
    	if(LSF!=null&&LSS!=null){
    		print_to_file();
    	}
    }
    
	//This is called when the application is resumed
    protected void onResume()  {
    	super.onResume();
    	//Log.i("pause", "onresume called");
    	if(LSF!=null&&LSS!=null){
    		LSF.list_arr1.clear();
    		LSS.list_sent.clear();
    		read_from_file();
    	}
    }
    
    void print_to_file(){
    	int l = LSF.list_arr1.size() ;
    	
    	try{
			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
			
        } 
        catch(FileNotFoundException e) {
			e.printStackTrace();
		}
        
        for(int i=0;i<l;i++){
        	String string = LSF.list_arr1.get(i)+"@";
        	try {
        		//writing to file
				fos.write(string.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
        l = LSS.list_sent.size() ;
    	
        try{
			fos = openFileOutput(FILENAME_s, Context.MODE_PRIVATE);
			
        } 
        catch(FileNotFoundException e) {
			e.printStackTrace();
		}
        
        for(int i=0;i<l;i++){
        	String string = LSS.list_sent.get(i)+"@";
        	try {
				fos.write(string.getBytes());
				Log.i("pause " , "printed on file");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    void read_from_file(){
    	int in = 2;
    	String out="";
    	char ch;
    	
    	try {
			fis =  openFileInput(FILENAME);
		} catch (FileNotFoundException e) {
			Log.i("rff", "couldnt open file");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	while(true){
    		try {
    			in = fis.read();
    		} catch (IOException e) {
    			Log.i("Read From File", "File read error");
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		//check if file reading completed
    		if(in==-1){
    			break;
    		}
    		ch = (char)in;
    		//checking if the string is ended and displaying it
    		if(ch=='@'){
    			if(LSF!=null){
	    			LSF.list_arr1.add(out);
    			}
    			out="";
    		}
    		//if string not ended then append char to out
    		else{
    			out+=ch;
    		}
    	}
    	
    	try {
			fis =  openFileInput(FILENAME_s);
		} catch (FileNotFoundException e) {
			Log.i("rff", "couldnt open file");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	while(true){
    		
    		try {
    			//read from file
    			in = fis.read();
    		} catch (IOException e) {
    			Log.i("Read from File", "File read error");
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		//checking if file read is done
    		if(in==-1){
    			Log.i("Read from File", "File read done");
    			break;
    		}
    		
    		ch = (char)in;
    		
    		//checking if string completed, demarcated strings with @
    		if(ch=='@'){    			
    			if(LSS!=null){
    				//appending the string to the list
	    			LSS.list_sent.add(out);
    			}
    			out="";
    		}
    		//string not complete, append to the string built now
    		else{
    			out+=ch;
    		}
    	}
    }
    
    private void receive_message(){
    	Calendar cal = Calendar.getInstance();
		int mseconds = cal.get(Calendar.MILLISECOND);
		int seconds = cal.get(Calendar.SECOND);
		int minutes = cal.get(Calendar.MINUTE);
		
		Log.i("Receive Message", "Intent registered at  "+minutes+":"+seconds+":"+mseconds);
    	//Log.i("Receive Message", "Receive message process strated");
    	IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    	
    	registerReceiver(new BroadcastReceiver(){
    		public void onReceive(Context c, Intent i) {
    			Calendar cal = Calendar.getInstance();
    			int mseconds = cal.get(Calendar.MILLISECOND);
    			int seconds = cal.get(Calendar.SECOND);
    			int minutes = cal.get(Calendar.MINUTE);
    			
    			Log.i("Receive Message", "On receive called at "+minutes+":"+seconds+":"+mseconds);
                wifiList = w.getScanResults();
                
                Log.i("Receive Message" , "No of Access point "+wifiList.size());
                int m;
                for(m = 0; m < wifiList.size(); m++)
                {
                       received_msg = wifiList.get(m).SSID;
                       Log.i("AP " , received_msg);
                       if(true){//received_msg.charAt(0)=='@'&&received_msg.charAt(1)=='!'){
                    	   received_msg=received_msg.substring(2);
                    	   Log.i("message " , received_msg);
                    	   break;
                       }
                }
                
                if(m==wifiList.size()){
                	received_msg="";
                }
                else{
                	LSF.list_arr1.add(received_msg+"\n");
            		LSF.count++;
                }
                LSF.adapter1.notifyDataSetChanged();
                LSS.adaptersent.notifyDataSetChanged();
    			//mseconds = cal.get(Calendar.MILLISECOND);
    			//seconds = cal.get(Calendar.SECOND);
    			//minutes = cal.get(Calendar.MINUTE);
    			//Log.i("Receive Message", "Receive message being recalled at "+minutes+":"+seconds+":"+mseconds);
                receive_message();
    		}
    	}, i);
    }
    
    class Task implements Runnable {
		@Override
		public void run() {
				//running the receive message thread
				Log.i("Receive Task " , "scan started");
	    		receive_message();
		}
	}
    
    class sendThread implements Runnable {
		
		String ssid;
		public sendThread(String ssid_in){
			ssid = ssid_in;
		}
		
		@Override
		public void run() {
				//running the receive message thread
				Log.i("Sending Task " , "send started");
	    		createWifiAccessPoint(ssid);
		}
	}
    
    private void recMessagethread(){
    	//See if the WiFi is enabled and enable it
    	if(!w.isWifiEnabled())
        {
            w.setWifiEnabled(true);          
        }
    	new Thread(new Task()).start();
    }
    
    public void sendMessage(View view){
    	//the text field in which message is put
    	EditText mEdit   = (EditText)findViewById(R.id.EditText);
    	String message = mEdit.getText().toString();

    	if(message!=""){
    		//showing in the list of sent messages
    		LSS.list_sent.add(message+"\n");
    		LSS.adaptersent.notifyDataSetChanged();
    		new Thread(new sendThread(message)).start();
    		//createWifiAccessPoint("@!"+message);
    		mEdit.setText ("");
    	}
    }
    
    private void createWifiAccessPoint(String ssid) {
    	//see if WiFi is enabled if not enable it
    	if(wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(false);          
        }
    	
    	//first we want to get the creation of WiFi access point function which is not directly exposed in the API, below code finds it and runs it
        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();   
        boolean methodFound=false;
        
        for(Method method: wmMethods){
            if(method.getName().equals("setWifiApEnabled")){
                methodFound=true;
                WifiConfiguration netConfig = new WifiConfiguration();
                netConfig.SSID = ssid; 
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                Log.i("here", "here3");
                try {
                	boolean apstatus=(Boolean) method.invoke(wifiManager, netConfig, false);
                    apstatus=(Boolean) method.invoke(wifiManager, netConfig,true);
                    //apstatus=(Boolean) method.invoke(wifiManager, netConfig,false);
                    for (Method isWifiApEnabledmethod: wmMethods)
                    {
                        if(isWifiApEnabledmethod.getName().equals("isWifiApEnabled")){
                            while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){
                            };
                            for(Method method1: wmMethods){
                                if(method1.getName().equals("getWifiApState")){
                                    int apstate;
                                    apstate=(Integer)method1.invoke(wifiManager);
                                }
                            }
                        }
                    }
                    
                    if(apstatus)
                    {
                        Log.d("Splash Activity", "Access Point created");   
                    }else
                    {
                        Log.d("Splash Activity", "Access Point creation failed");   
                    }
                    SystemClock.sleep(10000);
                    apstatus=(Boolean) method.invoke(wifiManager, netConfig,false);
                    if(!w.isWifiEnabled())
                    {
                        w.setWifiEnabled(true);          
                    }
                    
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }      
        }
        if(!methodFound){
            Log.d("Splash Activity", "cannot configure an access point");
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		if(tab.getPosition()==0){
			//LSF.adapter1.notifyDataSetChanged();
		}
		else if(tab.getPosition()==1){
			//LSS.adaptersent.notifyDataSetChanged();
		}
		mViewPager.setCurrentItem(tab.getPosition());
	}
	
	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
	            case 0:
	                // The first section of the app is the most interesting -- it offers
	                // a launchpad into the other demonstrations in this example application.
	            	//LSF = new LaunchpadSectionFragment();
	            	return LSF;
	            case 1:
	            	return LSS;
	            case 2:	
	            	return new LaunchpadSectionSettings();
	            default:    
	            	return new LaunchpadSectionSettings();
				// getItem is called to instantiate the fragment for the given page.
				// Return a DummySectionFragment (defined as a static inner class
				// below) with the page number as its lone argument.
				/*Fragment fragment = new DummySectionFragment();
				Bundle args = new Bundle();
				//args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;*/
			}
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return "Messages";
			case 1:
				return "Sent";
			case 2:
				return "Settings";//getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}
	
	public static class LaunchpadSectionFragment extends Fragment {
		
		List<String> list_arr1 = new ArrayList<String>();
		int count;
		ArrayAdapter<String> adapter1;
		ListView l;
		
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);
            //View inflatedView = inflater.inflate(R.layout.event_log, container, false);
            //View inflatedView = getLayoutInflater().inflate(R.layout.fragment_section_launchpad, null);
            //List<String> list_arr = new ArrayList<String>();
            //list_arr1.add("dh"); list_arr1.add("dh1"); list_arr1.add("dh2");
            count=3;
            l=(ListView) rootView.findViewById(R.id.listView1);
            adapter1 = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, list_arr1);
            l.setAdapter(adapter1);
            return rootView;
        }
        public void OnResume(){
        	//adapter1.notifyDataSetChanged();
        }
        public void notify_adapter(){
        	l.getAdapter().notifyAll();
        } 
	}
public static class LaunchpadSectionSent extends Fragment {
		
		List<String> list_sent = new ArrayList<String>();
		ArrayAdapter<String> adaptersent;
		
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sent_section_launchpad, container, false);
            //View inflatedView = inflater.inflate(R.layout.event_log, container, false);
            //View inflatedView = getLayoutInflater().inflate(R.layout.fragment_section_launchpad, null);
            //List<String> list_arr = new ArrayList<String>();
            //list_sent.add("dh"); list_arr1.add("dh1"); list_arr1.add("dh2");
            ListView l=(ListView) rootView.findViewById(R.id.listView1);
            adaptersent = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, list_sent);
            l.setAdapter(adaptersent);
            return rootView;
        }
	}

	public static class LaunchpadSectionSettings extends Fragment {
		
		List<String> list_sent = new ArrayList<String>();
		
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View rootView = inflater.inflate(R.layout.settings_section_launchpad, container, false);
	        /*Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner1);
	     // Create an ArrayAdapter using the string array and a default spinner layout
	     ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(rootView.getContext(),
	    		 list_sent, android.R.layout.simple_spinner_item);
	     // Specify the layout to use when the list of choices appears
	     adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	     // Apply the adapter to the spinner
	     spinner.setAdapter(adapter);*/
	        return rootView;
	    }
	}
}
