package il.co.gilead.micomm;

import il.co.gilead.micomm.LocalService.LocalBinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends SherlockFragmentActivity {
	private static final String TAG = "MiComm";
	public static boolean free = false;
	SQLiteDatabase myDB;
    TabHost mTabHost;
    ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;
    HorizontalScrollView mHorizontalScroll;
	Integer intNumOfPages;
	ProgressDialog mProgressDialog;
	List<PageObject> pages;
	Integer pgNum = null;
	Integer tabPosition = null;
	String pgName = null;
    public static LocalService mService;
    public static boolean mBound = false;
    private InterstitialAd mInterstitialAd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		setTheme(R.style.SmallerActionBar);
//		setTheme(R.style.Theme_Styled);
		setTheme(R.style.Theme_Sherlock_Light_NoActionBar);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setLocale();

		try {
			new File(prependFilePath(".nomedia")).createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        pages = getPageNames();
        intNumOfPages = pages.size();

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        mHorizontalScroll = (HorizontalScrollView) findViewById(R.id.scrollView);
        
        mViewPager = (ViewPager)findViewById(R.id.pager);

        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager, mHorizontalScroll);

        addTabs();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tabTag = sharedPrefs.getString("tab", null);
        if (tabTag != null)
        	mTabHost.setCurrentTabByTag(tabTag);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

    @Override
    protected void onStart() {
    	super.onStart();
//    	Bind to LocalService
    	Intent intent = new Intent(this, LocalService.class);
    	startService(intent);
    	bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//    	if (mService.isTicking)
//    		mService.timer.cancel();
    	loadInterstitial();
    }

	@Override
	protected void onStop() {
		super.onStop();
//		Unbind from the service
		if (mBound) {
			mService.timer.start();
			unbindService(mConnection);
			mBound = false;
		}
	}
	
	@Override
	protected void onDestroy(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("tab", mTabHost.getCurrentTabTag());
		editor.commit();
		super.onDestroy();
	}
    
	@Override
	public void onBackPressed(){
		showInterstitial();
		super.onBackPressed();
	}

	private void loadInterstitial() {
        // Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(this);
        // Defined in values/setup.xml
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_unit_id));
        // Load the ad.
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
	}

	private void showInterstitial() {
        // Show the ad if it's ready. 
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d(TAG, "Interstitial Ad did not load");
        }
	}

	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

	private List<PageObject> getPageNames() {
		SQLiteDatabase myDB;
		List<PageObject> pages = new ArrayList<PageObject>();
        myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
    	String sql = "SELECT ID, PageName FROM tblPages ORDER BY ID";
        Cursor c = myDB.rawQuery(sql, null);
        if(c.moveToFirst()){
            do{
            	int pgID = c.getInt(0);
            	String pgName = c.getString(1);
            	sql = "SELECT Copyright FROM tblImages WHERE Page="+pgID+" GROUP BY Copyright";
        		boolean isCopyright = false;
                Cursor c2 = myDB.rawQuery(sql, null);
                if(c2.moveToFirst()){
                    do{
                    	if (c2.getString(0) != null){
        	                if (c2.getString(0).equals("1"))
        	                	isCopyright = true;
                    	}
                    }
                    while(c2.moveToNext());
                }
                c2.close();
            	PageObject po = new PageObject(getApplicationContext(), pgID, pgName, isCopyright);
            	pages.add(po);
             }
            while(c.moveToNext());
        }
        c.close();
        myDB.close();
    	
    	return pages;
	}

	private void addNewPage() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			Add the Edit Text
		final EditText input = new EditText(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
		        LinearLayout.LayoutParams.WRAP_CONTENT);
		input.setLayoutParams(lp);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.setFocusable(true);
		builder.setView(input);
		builder.setTitle(getString(R.string.add_page));

//			Add the buttons
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//		            User clicked OK button
				String PageName = input.getText().toString().trim();
				if (PageName.contains("'"))
					PageName = PageName.replace("'", "");
				myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
				ContentValues values = new ContentValues();
				values.put("PageName", PageName);
				myDB.insert("tblPages", null, values);
//		    	String sql = "INSERT INTO tblPages (PageName) VALUES ('" + PageName + "')";
//				myDB.execSQL(sql);
		    	myDB.close();

		    	onRefresh();
				mTabHost.setCurrentTab(mTabHost.getTabWidget().getChildCount()-1);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//					User cancelled the dialog
			}
		});

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}

	private void renamePage(){
		tabPosition = mTabHost.getCurrentTab();
		pgNum = pages.get(tabPosition).getPageId();
		pgName = mTabHost.getCurrentTabTag();

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		Add the Edit Text
		final EditText input = new EditText(this);
		input.setText(pgName);
		input.selectAll();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
		        LinearLayout.LayoutParams.WRAP_CONTENT);
		input.setLayoutParams(lp);
		builder.setView(input);
		builder.setIcon(android.R.drawable.ic_menu_edit);
		builder.setTitle(getString(R.string.rename_page));

//		Add the buttons
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//	            User clicked OK button
				String newPageName = input.getText().toString().trim();
				if (newPageName.contains("'"))
					newPageName = newPageName.replace("'", "");
				if (newPageName.contains(","))
					newPageName = newPageName.replace(",", "");
				myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
				ContentValues values = new ContentValues();
				values.put("PageName", newPageName);
				myDB.update("tblPages", values, "ID=" + pgNum, null);
//				String sql = "UPDATE tblPages SET PageName='" + newPageName + "' WHERE ID=" + pgNum;
//				myDB.execSQL(sql);
				myDB.close();
				onRefresh();
	    		mTabHost.setCurrentTab(tabPosition);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//				User cancelled the dialog
			}
		});

//		 Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}

	private void removePage() {
		tabPosition = mTabHost.getCurrentTab();
		pgNum = pages.get(tabPosition).getPageId();
		pgName = mTabHost.getCurrentTabTag();
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_delete);
		builder.setTitle(getString(R.string.remove_page));
		builder.setMessage(getString(R.string.remove_page_confirmation)+" \""+pgName+"\"?");

//		Add the buttons
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//		            User clicked OK button
			    myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
		    	String sql = "DELETE FROM tblVideos WHERE ID IN " +
		    			"(SELECT V.ID FROM tblImages I " +
		    			"LEFT JOIN tblImageVideoLink L ON L.ImageID=I.ID " +
		    			"LEFT JOIN tblVideos V ON L.VideoID=V.ID " +
		    			"WHERE I.Page="+pgNum+")";
		    	myDB.execSQL(sql);
		    	sql = "DELETE FROM tblImageVideoLink WHERE LinkID IN " +
		    			"(SELECT LinkID FROM tblImageVideoLink " + 
		    			"LEFT JOIN tblImages ON tblImageVideoLink.ImageID=tblImages.ID " +
		    			"WHERE tblImages.Page="+pgNum+")";
		    	myDB.execSQL(sql);
		    	sql = "DELETE FROM tblImages WHERE Page="+pgNum;
		    	myDB.execSQL(sql);
		    	if (pages.size() == 1){
		    		sql = "UPDATE tblPages SET PageName='"+getString(R.string.page)+"' WHERE ID="+pgNum;
		    	}else{
			    	sql = "DELETE FROM tblPages WHERE ID="+pgNum;
		    	}
		    	myDB.execSQL(sql);
		    	myDB.close();
		    	onRefresh();
		    	if (tabPosition>0)
		    		mTabHost.setCurrentTab(tabPosition-1);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//					User cancelled the dialog
			}
		});
	
//		 Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void exportPage() {
		tabPosition = mTabHost.getCurrentTab();
		pgNum = pages.get(tabPosition).getPageId();
		pgName = pages.get(tabPosition).getPageName();

		String csvContent = MiCommDBHelper.exportDBtoCSV(getApplicationContext(), myDB, pgNum);
		String csvFileName = prependFilePath("page_info.csv");
		writeStringToTextFile(csvContent, csvFileName);

		List<String> listFilesToZip = new ArrayList<String>();
		listFilesToZip.add(csvFileName);
        myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
    	String sql =  "SELECT DISTINCT I.ImageName FROM tblImages I WHERE I.Page="+ pgNum +
    			" UNION SELECT DISTINCT V.VideoName FROM tblImages I " + 
    			"LEFT JOIN tblImageVideoLink L ON L.ImageID=I.ID LEFT JOIN tblVideos V ON L.VideoID=V.ID " +
    			"WHERE I.Page=" + pgNum;
    	
        Cursor c = myDB.rawQuery(sql, null);

        if(c.moveToFirst()){
            do{
            	String fileName = c.getString(0);
	    		if (fileName != null)
	    			listFilesToZip.add(prependFilePath(fileName));
	    	}
            while(c.moveToNext());
        }
        c.close();
        myDB.close();
		String[] filesToZip = listFilesToZip.toArray(new String[0]);
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		String imageFileName = "micomm_pack_" + timeStamp + "_" + pgName + ".zip";
		new Compress(filesToZip, prependFilePath(imageFileName)).zip();
		Boast.makeText(getApplicationContext(), R.string.export_completed).show(true);
	}

    private void importPage() {
        File mPath = new File(prependFilePath(""));
        FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".zip");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
			public void fileSelected(File file) {
				new Decompress(file.getAbsolutePath(), prependFilePath("")).unzip();
				processImportToDB();
			}

			private void processImportToDB() {
				File pageInfo = new File(prependFilePath("page_info.csv"));
				if (pageInfo.exists()){
					List<String> processedImportInfo = readRawTextFile(pageInfo);
		        	Iterator<String> iterator = processedImportInfo.iterator();
				    myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
				    long pageIDToAdd = myDB.compileStatement(processedImportInfo.get(0)).executeInsert();

				    while (iterator.hasNext()){
				    	String lineToParse = iterator.next();
				    	if (lineToParse.substring(0, 6).equals("INSERT"))
				    		lineToParse = iterator.next();
				    	String[] lineData = lineToParse.split(",");
				    	String ImageName = "";
				    	String ImageDescription = "";
				    	String Copyright = "";
				    	String VideoName = "";
				    	String VideoDescription = "";
				    	String VideoType = "";
				    	if (lineData[0] != null)
				    		ImageName = lineData[0];
				    	if (lineData.length > 1)
				    		ImageDescription = lineData[1];
				    	if (lineData.length > 2)
				    		Copyright = lineData[2];
			    		if (Copyright.equals(""))
			    			Copyright = "0";
				    	if (lineData.length > 3)
				    		VideoName = lineData[3];
				    	if (lineData.length > 4)
				    		VideoDescription = lineData[5];
				    	if (lineData.length > 5)
				    		VideoType = lineData[5];
		            	String sql = "INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) " + 
				    		"VALUES ('" + ImageName + "', '" + ImageDescription + "', " + pageIDToAdd + 
				    		", " + Copyright + ")";
					    long imageIDToLink = myDB.compileStatement(sql).executeInsert();

		                if (VideoName != ""){
			            	sql = "INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('" + 
					    		VideoName + "', '" + VideoDescription + "', " + VideoType + ")";
						    long videoIDToLink = myDB.compileStatement(sql).executeInsert();
			            	sql = "INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (" + 
					    		imageIDToLink + ", " + videoIDToLink + ")";
			            	myDB.execSQL(sql);
		                }
		        	}
//	                c.close();
	                myDB.close();
	                onRefresh();
	                mService.onCreate();
		    		mTabHost.setCurrentTab(mTabHost.getTabWidget().getChildCount()-1);
				}
			}
        });
        fileDialog.showDialog();
    }

    private void writeStringToTextFile(String s, String f){
    	File file = new File(f);
    	try{
    		FileOutputStream fos = new FileOutputStream(file, false); //True = Append to file, false = Overwrite
    		PrintStream p = new PrintStream(fos);
    		p.print(s);
    		p.close();
    		fos.close();
    	} catch (FileNotFoundException e) {
    	} catch (IOException e) {
    	}
    }

	private static List<String> readRawTextFile(File fileToProcess)
	{
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(fileToProcess);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		InputStreamReader inputreader = new InputStreamReader(inputStream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;
		List<String> arrayList = new ArrayList<String>();

		try
		{
			while (( line = buffreader.readLine()) != null)
			{
				arrayList.add(line);
			}
			buffreader.close();
		}
		catch (IOException e)
		{
			return null;
		}
		return arrayList;
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
//		menu.add(1, R.id.main_app_screen, 50, getString(R.id.main_screen));
		menu.add(1, R.id.add_page, 200, getString(R.string.add_page));
		menu.add(1, R.id.rename_page, 210, getString(R.string.rename_page));
		menu.add(1, R.id.remove_page, 220, getString(R.string.remove_page));
		menu.add(1, R.id.export_page, 270, getString(R.string.export_page));
		menu.add(1, R.id.import_page, 280, getString(R.string.import_page));
		menu.add(1, R.id.action_settings, 350, getString(R.string.action_settings));
		menu.add(1, R.id.help, 380, getString(R.string.help_caption));
		menu.add(1, R.id.exit, 400, getString(R.string.exit));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
//    	case R.id.main_app_screen:
//    		Boast.makeText(getApplicationContext(), "MainAppScreen Clicked").show(true);
//    	TODO Implement "Home"/"Main" page 
//    		return true;
    	case R.id.add_page:
    		addNewPage();
    		return true;
    	case R.id.rename_page:
    		renamePage();
    		return true;
    	case R.id.remove_page:
    		removePage();
    		return true;
    	case R.id.export_page:
    		exportPage();
    		return true;
    	case R.id.import_page:
    		importPage();
    		return true;
    	case R.id.help:
    		startActivity(new Intent(getApplicationContext(), Help.class));
    		return true;
    	case R.id.action_settings:
    		startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    		return true;
    	case R.id.exit:
    		stopService(new Intent(MainActivity.this, LocalService.class));
    		finish();
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

	private void onRefresh(){
		mTabHost.setCurrentTab(0);
		mTabHost.clearAllTabs();

        pages = getPageNames();
        intNumOfPages = pages.size();

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager, mHorizontalScroll);

        addTabs();
    }

	private void addTabs() {
		for (int i=0; i<intNumOfPages; i++)
		{
	        Bundle args = new Bundle();
	        args.putInt("intPageNum", pages.get(i).getPageId());
	        args.putBoolean("copyright", pages.get(i).isCopyrightProtected());
	        mTabsAdapter.addTab(mTabHost.newTabSpec(pages.get(i).getPageName()).setIndicator(pages.get(i).getPageName()),
	        		GridFragment.class, args);
	        final TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);        
	        tv.setPadding(5, 2, 5, 2);
	        tv.setTextColor(getResources().getColorStateList(R.color.text_tab_indicator));
	        tv.setBackgroundColor(getResources().getColor(R.color.text_tab_unselected_background));
	        tv.setTextSize(18);
	        mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.text_tab_unselected_background));
		}
	}

	private String prependFilePath(String fileName){
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/Pictures/" + getString(R.string.album_name) + "/" + fileName;
		return filePath;
	}

    // Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("tab", mTabHost.getCurrentTabTag());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	public static void setLocale(){
		Context c = AcraCrashReports.getApp();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        Locale locale;
        if (sharedPrefs.getString("languages", null) == null){
        	locale = Locale.getDefault();
        }else{
        	String languageToLoad = sharedPrefs.getString("languages", null);
        	locale = new Locale(languageToLoad);
        	Locale.setDefault(locale);
        	Configuration config = new Configuration();
        	config.locale = locale;
        	c.getResources().updateConfiguration(config, c.getResources().getDisplayMetrics());
        }
	}

}