package il.co.gilead.micomm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class SplashActivity extends Activity {
	private static String TAG = SplashActivity.class.getName();
	private static long SLEEP_TIME = 2;    // Sleep time in seconds
	SQLiteDatabase myDB;
	TextView status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);    // Removes notification bar

		setContentView(R.layout.activity_splash);

//		Start timer and launch main activity
		IntentLauncher launcher = new IntentLauncher();
		launcher.start();
	}

	private class IntentLauncher extends Thread {
		
		/**
		* Sleep for some time and then start the main activity.
		*/
		@Override
		public void run() {
			File dbFile = new File(getApplicationInfo().dataDir+"/databases/"+getString(R.string.db_name));
	        if (!dbFile.exists()) {
//	            myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
	        	prepareDB();
	        	populateDBWithInitialData();
	            copyAssets();
	        }else{
//	            myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
				try {
//					Sleeping
					Thread.sleep(SLEEP_TIME*1000);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
//			Start main activity
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(intent);
			finish();
		}

		private void prepareDB() {
			myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
	        myDB.beginTransaction();

	        myDB.compileStatement("CREATE TABLE IF NOT EXISTS [tblImageVideoLink] (" +
					"[LinkID] INTEGER  NOT NULL PRIMARY KEY," +
	    			"[ImageID] INTEGER  UNIQUE NOT NULL," +
	    			"[VideoID] INTEGER  NULL);").execute();

			myDB.compileStatement("CREATE TABLE IF NOT EXISTS [tblImages] (" +
	    			"[ID] INTEGER  PRIMARY KEY NOT NULL," +
	    			"[ImageName] VARCHAR(50)  NOT NULL," +
	    			"[ImageDescription] VARCHAR(100)  NULL," +
	    			"[Page] INTEGER  NULL," +
	    			"[Copyright] INTEGER  NULL);").execute();

			myDB.compileStatement("CREATE TABLE IF NOT EXISTS [tblPages] (" +
	    			"[ID] INTEGER  NOT NULL PRIMARY KEY," +
	    			"[PageName] VARCHAR(30)  NULL);").execute();

			myDB.compileStatement("CREATE TABLE IF NOT EXISTS [tblVideos] (" +
	    			"[ID] INTEGER  NOT NULL PRIMARY KEY," +
	    			"[VideoName] VARCHAR(150)  NOT NULL," +
	    			"[VideoDescription] VARCHAR(100)  NULL," +
	    			"[VideoType] VARCHAR(10)  NULL);").execute();

			myDB.compileStatement("CREATE INDEX IF NOT EXISTS [IDX_TBLIMAGEVIDEOLINK_IMAGEID] " + 
					"ON [tblImageVideoLink](" +
	    			"[ImageID]  DESC," +
	    			"[VideoID]  DESC);").execute();

			myDB.compileStatement("CREATE INDEX IF NOT EXISTS [IDX_TBLIMAGES_PAGE] ON [tblImages](" +
	    			"[Page]  DESC);").execute();

			myDB.setTransactionSuccessful();
			myDB.endTransaction();
			myDB.close();
	    }

	    private void populateDBWithInitialData(){
			myDB = openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
//	        DELETE
	        myDB.beginTransaction();
			myDB.compileStatement("DELETE FROM tblImageVideoLink").execute();
			myDB.compileStatement("DELETE FROM tblImages").execute();
			myDB.compileStatement("DELETE FROM tblVideos").execute();
			myDB.compileStatement("DELETE FROM tblPages").execute();
			myDB.setTransactionSuccessful();
			myDB.endTransaction();
//			INSERT
			myDB.beginTransaction();
			myDB.compileStatement("INSERT INTO tblPages (PageName) VALUES ('"+getString(R.string.sign_language)+"')").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('right.png', '"+getString(R.string.right)+"', 1, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('right.mp3', '"+getString(R.string.right)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('i_want.png', '"+getString(R.string.i_want)+"', 1, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('i_want.mp3', '"+getString(R.string.i_want)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('more.png', '"+getString(R.string.more)+"', 1, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('more.mp3', '"+getString(R.string.more)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('wrong.png', '"+getString(R.string.wrong)+"', 1, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('wrong.mp3', '"+getString(R.string.wrong)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('i_dont_want.png', '"+getString(R.string.i_dont_want)+"', 1, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('i_dont_want.mp3', '"+getString(R.string.i_dont_want)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('done.png', '"+getString(R.string.done)+"', 1, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('done.mp3', '"+getString(R.string.done)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblPages (PageName) VALUES ('"+getString(R.string.daily_usage)+"')").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('wash_face.png', '"+getString(R.string.wash_face)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('wash_face.mp3', '"+getString(R.string.wash_face)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('brush_teeth.png', '"+getString(R.string.brush_teeth)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('brush_teeth.mp3', '"+getString(R.string.brush_teeth)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('get_dressed.png', '"+getString(R.string.get_dressed)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('get_dressed.mp3', '"+getString(R.string.get_dressed)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('wash_hands.png', '"+getString(R.string.wash_hands)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('wash_hands.mp3', '"+getString(R.string.wash_hands)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('eat.png', '"+getString(R.string.eat)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('eat.mp3', '"+getString(R.string.eat)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('drink.png', '"+getString(R.string.drink)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('drink.mp3', '"+getString(R.string.drink)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('toilet.png', '"+getString(R.string.toilet)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('toilet.mp3', '"+getString(R.string.toilet)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('poop.png', '"+getString(R.string.poop)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('poop.mp3', '"+getString(R.string.poop)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('bath.png', '"+getString(R.string.bath)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('bath.mp3', '"+getString(R.string.bath)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('wash_hair.png', '"+getString(R.string.wash_hair)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('wash_hair.mp3', '"+getString(R.string.wash_hair)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('bedtime.png', '"+getString(R.string.bedtime)+"', 2, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('bedtime.mp3', '"+getString(R.string.bedtime)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblPages (PageName) VALUES ('"+getString(R.string.speech_therapist)+"')").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('a.jpg', '"+getString(R.string.a)+"', 3, 0)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('a.3gp', '"+getString(R.string.a)+"', 1)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('e.jpg', '"+getString(R.string.e)+"', 3, 0)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('e.3gp', '"+getString(R.string.e)+"', 1)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('i.jpg', '"+getString(R.string.i)+"', 3, 0)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('i.3gp', '"+getString(R.string.i)+"', 1)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('o.jpg', '"+getString(R.string.o)+"', 3, 0)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('o.3gp', '"+getString(R.string.o)+"', 1)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('u.jpg', '"+getString(R.string.u)+"', 3, 0)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('u.3gp', '"+getString(R.string.u)+"', 1)").execute();
			myDB.compileStatement("INSERT INTO tblPages (PageName) VALUES ('"+getString(R.string.calendar_events)+"')").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('holiday1.png', '"+getString(R.string.holiday1)+"', 4, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('holiday1.mp3', '"+getString(R.string.holiday1)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('holiday2.png', '"+getString(R.string.holiday2)+"', 4, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('holiday2.mp3', '"+getString(R.string.holiday2)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('holiday3.png', '"+getString(R.string.holiday3)+"', 4, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('holiday3.mp3', '"+getString(R.string.holiday3)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('holiday4.png', '"+getString(R.string.holiday4)+"', 4, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('holiday4.mp3', '"+getString(R.string.holiday4)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblPages (PageName) VALUES ('"+getString(R.string.weekly_schedule)+"')").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('playground.png', '"+getString(R.string.playground)+"', 5, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('playground.mp3', '"+getString(R.string.playground)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('swimming_class.png', '"+getString(R.string.swimming_class)+"', 5, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('swimming_class.mp3', '"+getString(R.string.swimming_class)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('music_class.png', '"+getString(R.string.music_class)+"', 5, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('music_class.mp3', '"+getString(R.string.music_class)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblPages (PageName) VALUES ('"+getString(R.string.songs)+"')").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('happy.png', '"+getString(R.string.happy)+"', 6, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('happy.mp3', '"+getString(R.string.happy)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('clap.png', '"+getString(R.string.clap)+"', 6, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('clap.mp3', '"+getString(R.string.clap)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('feet.png', '"+getString(R.string.feet)+"', 6, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('feet.mp3', '"+getString(R.string.feet)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('call.png', '"+getString(R.string.call)+"', 6, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('call.mp3', '"+getString(R.string.call)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('laugh.png', '"+getString(R.string.laugh)+"', 6, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('laugh.mp3', '"+getString(R.string.laugh)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('hug.png', '"+getString(R.string.hug)+"', 6, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('hug.mp3', '"+getString(R.string.hug)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('kiss.png', '"+getString(R.string.kiss)+"', 6, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('kiss.mp3', '"+getString(R.string.kiss)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblPages (PageName) VALUES ('"+getString(R.string.breakfast)+"')").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('drink.png', '"+getString(R.string.drink)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('drink.mp3', '"+getString(R.string.drink)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('water.png', '"+getString(R.string.water)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('water.mp3', '"+getString(R.string.water)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('eat.png', '"+getString(R.string.eat)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('eat.mp3', '"+getString(R.string.eat)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('fork.png', '"+getString(R.string.fork)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('fork.mp3', '"+getString(R.string.fork)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('plate.png', '"+getString(R.string.plate)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('plate.mp3', '"+getString(R.string.plate)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('spoon.png', '"+getString(R.string.spoon)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('spoon.mp3', '"+getString(R.string.spoon)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('bread.png', '"+getString(R.string.bread)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('bread.mp3', '"+getString(R.string.bread)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('cheese.png', '"+getString(R.string.cheese)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('cheese.mp3', '"+getString(R.string.cheese)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('cucumber.png', '"+getString(R.string.cucumber)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('cucumber.mp3', '"+getString(R.string.cucumber)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('omlet.png', '"+getString(R.string.omlet)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('omlet.mp3', '"+getString(R.string.omlet)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('sandwich.png', '"+getString(R.string.sandwich)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('sandwich.mp3', '"+getString(R.string.sandwich)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImages (ImageName, ImageDescription, Page, Copyright) VALUES ('tomato.png', '"+getString(R.string.tomato)+"', 7, 1)").execute();
			myDB.compileStatement("INSERT INTO tblVideos (VideoName, VideoDescription, VideoType) VALUES ('tomato.mp3', '"+getString(R.string.tomato)+"', 3)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (1, 1)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (2, 2)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (3, 3)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (4, 4)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (5, 5)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (6, 6)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (7, 7)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (8, 8)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (9, 9)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (10, 10)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (11, 11)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (12, 12)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (13, 13)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (14, 14)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (15, 15)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (16, 16)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (17, 17)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (18, 18)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (19, 19)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (20, 20)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (21, 21)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (22, 22)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (23, 23)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (24, 24)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (25, 25)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (26, 26)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (27, 27)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (28, 28)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (29, 29)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (30, 30)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (31, 31)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (32, 32)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (33, 33)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (34, 34)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (35, 35)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (36, 36)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (37, 37)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (38, 38)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (39, 39)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (40, 40)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (41, 41)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (42, 42)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (43, 43)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (44, 44)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (45, 45)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (46, 46)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (47, 47)").execute();
			myDB.compileStatement("INSERT INTO tblImageVideoLink (ImageID, VideoID) VALUES (48, 48)").execute();
			myDB.setTransactionSuccessful();
			myDB.endTransaction();
			myDB.close();
		}

		private void copyAssets(){
	    	getAlbumDir();
	    	try {
				String[] files = getAssets().list("general");
				
				for (String value : files) {
					InputStream in = getAssets().open("general/" + value);
					File dst = new File(prependFilePath("") + value);
					copy(in, dst);
				}
				
				Locale loc = Locale.getDefault();
				String folderLanguage = loc.getLanguage();
				// Special case for Hebrew, and probably in the future for other languages
				if (folderLanguage.equalsIgnoreCase("iw")) folderLanguage = "he";
				
				files = getAssets().list(folderLanguage);
				if (files.length == 0)
					files = getAssets().list("en");
				else
					files = getAssets().list(folderLanguage);

				for (String value : files) {
					InputStream in = getAssets().open(folderLanguage+"/" + value);
					File dst = new File(prependFilePath("") + value);
					copy(in, dst);
				}
	    	} catch (IOException e) {
				e.printStackTrace();
			}
	    }

	    public void copy(InputStream in, File dst) throws IOException {
	        OutputStream out = new FileOutputStream(dst);

	        // Transfer bytes from in to out
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        in.close();
	        out.close();
	    }

		private File getAlbumDir() {
			File storageDir = null;
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				storageDir = new File(prependFilePath(""));
				if (storageDir != null) {
					if (! storageDir.mkdirs()) {
						if (! storageDir.exists()){
							Log.d(getString(R.string.app_name), "failed to create directory");
							return getFilesDir();
						}
					}
				}
			} else {
				Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
			}
			return storageDir;
		}

		private String prependFilePath(String fileName){
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/Pictures/" + getString(R.string.album_name) + "/" + fileName;
			return filePath;
		}
	}
}
