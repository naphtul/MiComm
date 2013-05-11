package il.co.gilead.micomm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class LocalService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
	private static final String TAG = "MiComm";
    private SoundPool sp;
    private HashMap<String, Integer> audiosMap;
    public boolean finishedLoading;
    public boolean isTicking = false;
    public CountDownTimer timer;
    public static List<Integer> processingMedia;
    private static Context context;
    
    @Override
    public void onCreate() {
        super.onCreate();
        context = AcraCrashReports.getApp();
        processingMedia = new ArrayList<Integer>();
        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        new LoadAudiosInBackground().execute();

        timer = new CountDownTimer(15*60*1000, 10000){
        	@Override
    		public void onTick(long msUntilFinished){
    			isTicking = true;
    			Log.d(TAG, "Time left until the service is supposed to stop: "+msUntilFinished);
    		}
    		
        	@Override
    		public void onFinish(){
    			isTicking = false;
    			stopSelf();
    		}
    	};
    }
    
    public void playAudio(String audio, float fSpeed) {
        AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = streamVolumeCurrent / streamVolumeMax;

        if (audiosMap.get(audio) != null)
        	sp.play(audiosMap.get(audio), volume, volume, 1, 0, fSpeed);
    }

    public void addAudio(String audio){
    	audiosMap.put(audio, sp.load(prependFilePath(audio), 1));
    }
    
	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
	    LocalService getService() {
	        // Return this instance of LocalService so clients can call public methods
	        return LocalService.this;
	    }
	}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public void audioAdd(String audio) {
    	addAudio(audio);
    }

    public void audioPlay(String audio){
        playAudio(audio, 1.0f);
    }

	private void loadAudios() {
    	SQLiteDatabase myDB;
        myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
		String sql = "SELECT DISTINCT V.VideoName " +
				"FROM tblVideos V " +
				"WHERE V.VideoType = '3'";
		Cursor c = myDB.rawQuery(sql, null);

		audiosMap = new HashMap<String, Integer>();
        if(c.moveToFirst()){
            do{
                audiosMap.put(c.getString(0), sp.load(prependFilePath(c.getString(0)), 1));
            }
            while(c.moveToNext());
        }
        c.close();
        myDB.close();
	}
	
	private static String prependFilePath(String fileName){
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/Pictures/" + context.getString(R.string.album_name) + "/" + fileName;
		return filePath;
	}

    private class LoadAudiosInBackground extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            finishedLoading = false ;
        }

        @Override
        protected Void doInBackground(Void... params) {
        	loadAudios();
			return null;
        }

       @Override
       protected void onPostExecute(Void result) {
    	   finishedLoading = true ;
       }       
   }

}