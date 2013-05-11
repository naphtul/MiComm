package il.co.gilead.micomm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.acra.ACRA;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.github.espiandev.showcaseview.ShowcaseView.OnShowcaseEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenVideo;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.kbeanie.imagechooser.api.VideoChooserListener;
import com.kbeanie.imagechooser.api.VideoChooserManager;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.svenkapudija.imageresizer.ImageResizer;
import com.svenkapudija.imageresizer.operations.ImageRotate;
import com.svenkapudija.imageresizer.operations.ImageRotation;

public class GridFragment extends SherlockFragment implements ImageChooserListener, VideoChooserListener{
	private static final String TAG = "MiComm";
	SQLiteDatabase myDB;
	static File file;
	static Bitmap myBitmap;
	public GridView gv;
	private String videoName;
	private static final int ACTION_TAKE_PHOTO = 1;
	private static final int ACTION_TAKE_VIDEO = 2;
	private ImageChooserManager imageChooserManager;
	private VideoChooserManager videoChooserManager;
	private int chooserType;
	private Integer intTileSize = 144;
	private Integer intNumOfTiles = 12;
	private Integer intPageNum = 1;
	View v;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	String state = Environment.getExternalStorageState();
	public static String zipFileToDecompress = null;
	Bundle args;
	private boolean firstLaunch = true;
    private static String mFileName = null;
    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;
    private PlayButton mPlayButton = null;
    private MediaPlayer mPlayer = null;
    private String audioFileName = null;
	private boolean replaceImage = false;
	private boolean isCopyright = false;
	private String filePath;
	// TODO: Must change this soon!
	// https://github.com/coomar2841/image-chooser-library/issues/50
	private Integer globalImageIDMustStopUsingSoon = 0;
	private boolean hasCamera;
	private AdView mAdView;
    
    /**
     * Empty constructor as per the Fragment documentation
     */
	public GridFragment () {}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
		if (savedInstanceState != null) {
			globalImageIDMustStopUsingSoon = savedInstanceState.getInt("imageid");
	        String tempFilePath = savedInstanceState.getString("file");
	        if (tempFilePath != null)
	        	file = new File(tempFilePath);
	        replaceImage = savedInstanceState.getBoolean("replaceImage", false);
        }
        args = getArguments();
        if (args != null){
        	intPageNum = args.getInt("intPageNum");
        	isCopyright = args.getBoolean("copyright");
        }else{
        	intPageNum = 1;
        	isCopyright = args.getBoolean("copyright");
        }

		hasCamera = getSherlockActivity().getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
        firstLaunch = sharedPrefs.getBoolean("display_tutorial", true);
		intTileSize = sharedPrefs.getInt("tile_size", 144);
		intNumOfTiles = sharedPrefs.getInt("num_of_tiles", 12);

        args.putInt("intTileSize", intTileSize);
        args.putInt("intNumOfTiles", intNumOfTiles);
    }

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
		
	    v = inflater.inflate(R.layout.activity_grid_fragment, container, false);
        if (!isCopyright){
        	TextView copyright = (TextView) v.findViewById(R.id.copyright);
        	copyright.setText("");
        }
		gv = (GridView) v.findViewById(R.id.gridView);
		gv.setColumnWidth(intTileSize);
		gv.setAdapter(new ImageAdapter(v.getContext(), args));
		registerForContextMenu(gv);
		gv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parentView, View iv, int position, long id) {
				ImageThumb it = (ImageThumb) parentView.getAdapter().getItem(position);
				Integer imageID = it.getImageId();
				String ImageDescription = it.getImageDescription(); 
				if (imageID != 9999){
					videoName = it.getVideoName();
					String VideoType = it.getVideoType();
					if (LocalService.processingMedia.contains(imageID)){
						Boast.makeText(v.getContext(), getString(R.string.media_processing)).show(true);
					}else{
						if (videoName != null){
							if (VideoType.equals("1") || VideoType.equals("2")){
								Intent i = new Intent(v.getContext(), VideoPlayback.class);
								i.putExtra("VideoPath", prependFilePath(videoName));
								i.putExtra("VideoType", VideoType);
								if (ImageDescription != null && ImageDescription.length() > 0)
									Boast.makeText(v.getContext(), ImageDescription).show(true);
								startActivity(i);
							}else if (VideoType.equals("3") && MainActivity.mBound && MainActivity.mService.finishedLoading){
								if (ImageDescription != null && ImageDescription.length() > 0){
									Boast.makeText(v.getContext(), ImageDescription).show(true);
								}
								MainActivity.mService.audioPlay(videoName);
							}else
								Boast.makeText(v.getContext(), R.string.audio_not_ready).show(true);
						}else{
							Boast.makeText(v.getContext(), R.string.not_associated).show(true);
						}
					}
				}else{
					Boast.makeText(v.getContext(), R.string.showcase_tile).show(true);
				}
			}
		});
//		Code to display Ads
	    if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO){
	    	mAdView = (AdView) v.findViewById(R.id.ad);
	    	mAdView.loadAd(new AdRequest.Builder().build());
	    }

		if (firstLaunch){
	        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putBoolean("display_tutorial", false);
			editor.commit();
			
			showShowcase();
		}
		
		ImageButton menuButton;
		menuButton = (ImageButton) v.findViewById(R.id.menuButton);
		menuButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getSherlockActivity().openOptionsMenu();
			}
		});
		decideIfToShowMenu(menuButton);
	    return v;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void decideIfToShowMenu(ImageButton menuButton){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			if (ViewConfiguration.get(v.getContext()).hasPermanentMenuKey() == false)
				menuButton.setVisibility(View.VISIBLE);

	}
	
	private void showShowcase() {
		ShowcaseView sv;
		ShowcaseView.ConfigOptions co;

		co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = false;
		sv = ShowcaseView.insertShowcaseView(75, 200, getSherlockActivity(), R.string.showcase_tile_title,
				R.string.showcase_tile, co);
		sv.setTextColors(Color.parseColor("#FF040A"), Color.parseColor("#FFFFFF"));
		
		sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {
			
			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
			}
			
			@SuppressWarnings("deprecation")
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				int width = 480;
				int height = 800;
				try {
					Display display = getSherlockActivity().getWindowManager().getDefaultDisplay(); 
					width = display.getWidth();
					height = display.getHeight();
				} catch (NullPointerException e) {
		            ACRA.getErrorReporter().handleSilentException(e);
					e.printStackTrace();
				}
				float middleOfScreenX = (width / 2);
				float middleOfScreenY = (height / 2);
				ShowcaseView sv2;
				ShowcaseView.ConfigOptions co2 = new ShowcaseView.ConfigOptions();
				co2.hideOnClickOutside = true;
				co2.noButton = true;
				try {
					sv2 = ShowcaseView.insertShowcaseView(middleOfScreenX, middleOfScreenY, getSherlockActivity(),
							R.string.showcase_swipe_title, R.string.showcase_swipe, co2);
					sv2.setTextColors(Color.parseColor("#FF040A"), Color.parseColor("#000000"));
					sv2.animateGesture(100, 0, -100, 0);
				} catch (NullPointerException e) {
		            ACRA.getErrorReporter().handleSilentException(e);
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onPause() {
		if (mAdView != null)
			mAdView.pause();
		super.onPause();
	}
	
	@Override
	public void onResume() {
        super.onResume();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getSherlockActivity());
        intTileSize = sharedPrefs.getInt("tile_size", 144);
		intNumOfTiles = sharedPrefs.getInt("num_of_tiles", 12);
		gv = (GridView) v.findViewById(R.id.gridView);
		gv.setColumnWidth(intTileSize);
        args.putInt("intTileSize", intTileSize);
        args.putInt("intNumOfTiles", intNumOfTiles);
		gv.setAdapter(new ImageAdapter(v.getContext(), args));
		if (mAdView != null)
			mAdView.resume();
    }

	@Override
	public void onDestroy() {
		if (mAdView != null)
			mAdView.destroy();
		super.onDestroy();
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(R.string.options_name);
		AdapterContextMenuInfo cmi = (AdapterContextMenuInfo) menuInfo;
		ImageThumb it = (ImageThumb) gv.getItemAtPosition(cmi.position);
		String VideoType = it.getVideoType();
		if (it.getImageId() == 9999){
			if (hasCamera)
				menu.add(intPageNum, cmi.position, 1, R.string.take_photo);
			menu.add(intPageNum, cmi.position, 2, R.string.browse_for_image);
		}else{
			if (hasCamera)
				menu.add(intPageNum, cmi.position, 3, R.string.take_and_replace);
			menu.add(intPageNum, cmi.position, 4, R.string.browse_and_replace);
			menu.add(intPageNum, cmi.position, 8, R.string.modify_description);
			menu.add(intPageNum, cmi.position, 9, R.string.delete_image_from_database);
			if (VideoType != null){
				if (hasCamera)
					menu.add(intPageNum, cmi.position, 5, R.string.take_video_and_replace);
				menu.add(intPageNum, cmi.position, 6, R.string.link_existing_video_and_replace);
				menu.add(intPageNum, cmi.position, 7, R.string.record_audio_and_replace);
				if (VideoType.equals("3"))
					menu.add(intPageNum, cmi.position, 10, R.string.delete_audio_link);
				else if (VideoType.equals("1") || VideoType.equals("2"))
					menu.add(intPageNum, cmi.position, 10, R.string.delete_video_link);
			}else{
				if (hasCamera)
					menu.add(intPageNum, cmi.position, 5, R.string.take_video);
				menu.add(intPageNum, cmi.position, 6, R.string.link_existing_video);
				menu.add(intPageNum, cmi.position, 7, R.string.record_audio);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		gv = (GridView) v.findViewById(R.id.gridView);
		ImageThumb it = (ImageThumb) gv.getItemAtPosition(item.getItemId());

		if (item.getGroupId() == intPageNum){		
			Integer imageID = it.getImageId();
			globalImageIDMustStopUsingSoon = imageID;
			switch (item.getOrder()) {
			case 1:
				replaceImage = false;
				takePhoto(imageID);
				return true;
			case 2:
				replaceImage = false;
				getImage(ChooserType.REQUEST_PICK_PICTURE, imageID);
				return true;
			case 3:
				replaceImage = true;
				takePhoto(imageID);
				return true;
			case 4:
				replaceImage = true;
				getImage(ChooserType.REQUEST_PICK_PICTURE, imageID);
				return true;
			case 5:
				takeVideo(imageID);
				return true;
			case 6:
				getVideo(ChooserType.REQUEST_PICK_VIDEO, imageID);
				return true;
			case 7:
				takeAudio(imageID);
				return true;
			case 8:
				renameImageDescription(it.getImageDescription(), imageID, it.getVideoName());
				return true;
			case 9:
				deleteImageFromDB(imageID);
				gv.setAdapter(new ImageAdapter(v.getContext(), args));
				return true;
			case 10:
				deleteVideoLink(imageID);
				gv.setAdapter(new ImageAdapter(v.getContext(), args));
				return true;
			}
		    return super.onContextItemSelected(item);
		}
		return false;
	}

	private void takeAudio(final Integer imageID) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getSherlockActivity());
//		Add the Buttons
		audioFileName = "micomm_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + 
				".3gp";
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/MiComm/" + 
				audioFileName;

        LinearLayout ll = new LinearLayout(this.getSherlockActivity());
        mRecordButton = new RecordButton(this.getSherlockActivity());
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        mPlayButton = new PlayButton(this.getSherlockActivity());
        ll.addView(mPlayButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        builder.setView(ll);
		builder.setTitle(getString(R.string.record_audio));

//		Add the buttons
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//	            User clicked OK button
				if (!mRecordButton.mStartRecording)
					onRecord(false);
				insertOrUpdateVideoToDB(audioFileName, 3, imageID);
				MainActivity.mService.addAudio(audioFileName);
				gv = (GridView) v.findViewById(R.id.gridView);
				gv.setAdapter(new ImageAdapter(v.getContext(), args));
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//				User cancelled the dialog
			}
		});
		
		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}

	private void insertOrUpdateVideoToDB (String videoName, int videoType, long imageIdToLinkTo){
		Context context = AcraCrashReports.getApp();
		Log.d(TAG, "AsyncTask debugging - insertOrUpdateVideoToDB - videoName: "+videoName+" videoType: "+videoType+" imageIdToLinkTo: "+imageIdToLinkTo);
	    myDB = context.openOrCreateDatabase(context.getString(R.string.db_name), Context.MODE_PRIVATE, null);
	    String sql = "INSERT INTO [tblVideos] (VideoName, VideoType) VALUES ('" + videoName + "', "+videoType+")";
	    long newVideoID = myDB.compileStatement(sql).executeInsert();
		Cursor c = myDB.rawQuery("SELECT LinkID, VideoID FROM tblImageVideoLink WHERE ImageID="+imageIdToLinkTo, null);
		String LinkID = null;
		String oldVideoID = null;
        if (c.moveToFirst()) {
        	LinkID = c.getString(0);
        	oldVideoID = c.getString(1);
        }
        if (LinkID == null)
    		myDB.execSQL("INSERT INTO [tblImageVideoLink] (ImageID, VideoID) VALUES ("+imageIdToLinkTo+", "+newVideoID+")");
        else{
    		myDB.execSQL("DELETE FROM tblVideos WHERE ID="+oldVideoID);
    		myDB.execSQL("UPDATE tblImageVideoLink SET VideoID="+newVideoID+" WHERE LinkID="+LinkID);
        }
        c.close();
        myDB.close();
	}

	private void getImage(int choice, Integer imageID) {
		chooserType = choice;
		imageChooserManager = new ImageChooserManager(this, choice, false);
		Bundle extras = new Bundle();
		extras.putInt("imageID", imageID);
		imageChooserManager.setExtras(extras);
		imageChooserManager.setImageChooserListener(this);
		globalImageIDMustStopUsingSoon = imageID;
		try {
			imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	private void getVideo(int choice, Integer imageID) {
		chooserType = choice;
		videoChooserManager = new VideoChooserManager(this, choice, false);
		Bundle extras = new Bundle();
		extras.putInt("imageID", imageID);
		videoChooserManager.setExtras(extras);
		videoChooserManager.setVideoChooserListener(this);
		globalImageIDMustStopUsingSoon = imageID;
		try {
			videoChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private void addImageToDB(String fileToAdd) {
		myDB = getSherlockActivity().openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
		myDB.execSQL("INSERT INTO [tblImages] (ImageName, Page, Copyright) VALUES ('" + fileToAdd + "', " + intPageNum + ", 0)");
		myDB.close();
	}

    private void replaceImageInDB(String fileToAdd, Integer imageID) {
		myDB = getSherlockActivity().openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
		myDB.execSQL("UPDATE tblImages SET ImageName='" + fileToAdd + "' WHERE ID=" + imageID);
		myDB.close();
	}

	private void renameImageDescription(String descBefore, final Integer imageID, String vidName){
		char[] descBeforeCharArray;
		videoName = vidName;
		if (descBefore == null)
			descBefore = "";
		descBeforeCharArray = descBefore.toCharArray();
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getSherlockActivity());
//		Add the Edit Text
		final EditText input = new EditText(this.getSherlockActivity());
		input.setText(descBeforeCharArray, 0, descBefore.length());
		input.selectAll();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
		        LinearLayout.LayoutParams.WRAP_CONTENT);
		input.setLayoutParams(lp);
		builder.setView(input);
		builder.setIcon(android.R.drawable.ic_menu_edit);
		builder.setTitle(getString(R.string.modify_description));
		builder.setMessage(R.string.modify_description_explanation);

//		Add the buttons
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//	            User clicked OK button
				String ImageDescription = input.getText().toString().trim();
				if (ImageDescription.contains("'"))
					ImageDescription = ImageDescription.replace("'", "");
				if (ImageDescription.contains(","))
					ImageDescription = ImageDescription.replace(",", "");
				myDB = getSherlockActivity().openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
				ContentValues values = new ContentValues();
				values.put("ImageDescription", ImageDescription);
				myDB.update("tblImages", values, "ID=" + imageID, null);
//				String sql = "UPDATE tblImages SET ImageDescription='" + ImageDescription
//						+ "' WHERE ID=" + imageID;
//				myDB.execSQL(sql);
				myDB.close();
				if (videoName == null && ImageDescription.length() > 0){
					try {
						getMP3(ImageDescription, imageID);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				gv = (GridView) v.findViewById(R.id.gridView);
				gv.setAdapter(new ImageAdapter(v.getContext(), args));
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//				User cancelled the dialog
			}
		});
		
		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}

	private String prependFilePath(String fileName){
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/Pictures/" + getString(R.string.album_name) + "/" + fileName;
		return filePath;
	}

	private void takePhoto(Integer imageID) {
		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePhotoIntent.putExtra("imageID", imageID);
		if (takePhotoIntent.resolveActivity(getSherlockActivity().getPackageManager()) != null)
			startActivityForResult(takePhotoIntent, ACTION_TAKE_PHOTO);
	}

	private void takeVideo(Integer imageID) {
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//		this isn't working... :-(
//		takeVideoIntent.putExtra("imageID", imageID);
		if (takeVideoIntent.resolveActivity(getSherlockActivity().getPackageManager()) != null)
			startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != MainActivity.RESULT_OK) return;
		Integer imageID = 0;
		if (data != null)
			imageID = data.getIntExtra("imageID", 0);
		Log.d(TAG, "onActivityResult - imageID: "+imageID+" requestCode: "+requestCode);
		switch (requestCode) {
		case ACTION_TAKE_PHOTO:
			handleCameraPhoto(data);
			break;
		case ACTION_TAKE_VIDEO:
		    Log.d(TAG, "AsyncTask debugging - onActivityResult");
		    if (!LocalService.processingMedia.contains(globalImageIDMustStopUsingSoon))
		    	LocalService.processingMedia.add(globalImageIDMustStopUsingSoon);
			new HandleCamVid(globalImageIDMustStopUsingSoon).execute(data);
			break;
		case ChooserType.REQUEST_CAPTURE_PICTURE: case ChooserType.REQUEST_PICK_PICTURE:
			if (imageChooserManager == null) {
				reinitializeImageChooser();
			}
			imageChooserManager.submit(requestCode, data);
			break;
		case ChooserType.REQUEST_PICK_VIDEO:
			if (videoChooserManager == null) {
				reinitializeVideoChooser();
			}
		    if (!LocalService.processingMedia.contains(imageID))
		    	LocalService.processingMedia.add(imageID);
			videoChooserManager.submit(requestCode, data);
			break;
		}
	}

	private void handleSelectedVideo(String videoPath, Integer imageID) {
//    	InputStream in = null;
//        try {
//			in = new FileInputStream(new File(videoPath));
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		String extension = getFileExtension(videoPath);
		String videoFileName = "micomm_" + timeStamp + extension;
		file = new File(prependFilePath(videoFileName));
		File from = new File(videoPath);
		from.renameTo(file);
    	insertOrUpdateVideoToDB(file.getName(), 1, imageID);
		gv = (GridView) v.findViewById(R.id.gridView);
		gv.setAdapter(new ImageAdapter(v.getContext(), args));
//		videoProcessing = false;
		LocalService.processingMedia.remove(imageID);
	}

	private void handleSelectedImage(String imagePath, Integer imageID) {
//        InputStream in = null;
//		try {
//			in = new FileInputStream(new File(filePath));
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		} catch (NullPointerException e2) {
//			e2.printStackTrace();
//			Boast.makeText(v.getContext(), getString(R.string.problem_loading_image)).show(true);
//			ACRA.getErrorReporter().putCustomData("filePath", filePath);
//			ACRA.getErrorReporter().handleException(e2);
//			return;
//		}
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		String extension = getFileExtension(imagePath);
		String imageFileName = "micomm_" + timeStamp + extension;
		file = new File(prependFilePath(imageFileName));
		File from = new File(imagePath);
		from.renameTo(file);

//        try {
//			copy(in, file);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        handleCameraPhoto(file, imageID);
	}

	private void handleCameraPhoto(Intent data) {
		Bundle extras = data.getExtras();
//		Integer imageID = data.getIntExtra("imageID", 0);
		Integer imageID = globalImageIDMustStopUsingSoon;
		String imageFileName = "";
		Bitmap mImageBitmap;

		if (extras == null || extras.get("data") == null){
			// When arriving from Gallery selection.
			if (file != null)
				imageFileName = file.getAbsolutePath();
			else{
				Boast.makeText(v.getContext(), getString(R.string.problem_loading_image)).show(true);
			}
		}else{
			// When arriving from the camera capture.
			mImageBitmap = (Bitmap) extras.get("data");
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
			imageFileName = "micomm_" + timeStamp + ".jpg";
			file = new File(prependFilePath(imageFileName));
			ImageResizer.saveToFile(mImageBitmap, file);
		}
		mImageBitmap = ImageResizer.crop(file, 400, 400);
		
		ExifInterface exif;
		try {
			exif = new ExifInterface(prependFilePath(imageFileName));
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch(orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				mImageBitmap = ImageRotate.rotate(mImageBitmap, ImageRotation.CW_90);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				mImageBitmap = ImageRotate.rotate(mImageBitmap, ImageRotation.CW_270);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				mImageBitmap = ImageRotate.rotate(mImageBitmap, ImageRotation.CW_180);
				break;
			}
			ImageResizer.saveToFile(mImageBitmap, file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (replaceImage ){
			replaceImageInDB(file.getName(), imageID);
			replaceImage = false;
		}else{
			addImageToDB(file.getName());
		}
		gv = (GridView) v.findViewById(R.id.gridView);
		gv.setAdapter(new ImageAdapter(v.getContext(), args));
		if (mImageBitmap != null)
			mImageBitmap.recycle();
	}

	private void handleCameraPhoto(File file, Integer imageID) {
		String imageFileName = "";
		Bitmap mImageBitmap;

		if (file != null)
			imageFileName = file.getAbsolutePath();
		else{
			Boast.makeText(v.getContext(), getString(R.string.problem_loading_image)).show(true);
		}
		mImageBitmap = ImageResizer.crop(file, 400, 400);
		
		ExifInterface exif;
		try {
			exif = new ExifInterface(imageFileName);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch(orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				mImageBitmap = ImageRotate.rotate(mImageBitmap, ImageRotation.CW_90);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				mImageBitmap = ImageRotate.rotate(mImageBitmap, ImageRotation.CW_270);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				mImageBitmap = ImageRotate.rotate(mImageBitmap, ImageRotation.CW_180);
				break;
			}
			ImageResizer.saveToFile(mImageBitmap, file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (replaceImage ){
			replaceImageInDB(file.getName(), imageID);
			replaceImage = false;
		}else{
			addImageToDB(file.getName());
		}
		gv = (GridView) v.findViewById(R.id.gridView);
		gv.setAdapter(new ImageAdapter(v.getContext(), args));
		if (mImageBitmap != null)
			mImageBitmap.recycle();
	}

    public void checkStorage(){
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    	    mExternalStorageAvailable = mExternalStorageWriteable = true;
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	    mExternalStorageAvailable = true;
    	    mExternalStorageWriteable = false;
    	} else {
    	    // Something else is wrong. It may be one of many other states, but all we need
    	    //  to know is we can neither read nor write
    	    mExternalStorageAvailable = mExternalStorageWriteable = false;
    	}    	
    }

	private void deleteVideoLink(int ImageID) {
	    myDB = getSherlockActivity().openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
    	String sql = "DELETE FROM tblVideos WHERE ID IN "
    			+ "(SELECT ID FROM tblVideos LEFT JOIN tblImageVideoLink ON ID=VideoID WHERE ImageID="
    			+ ImageID + ")";
    	myDB.execSQL(sql);
    	sql = "DELETE FROM tblImageVideoLink WHERE ImageID="+ImageID;
    	myDB.execSQL(sql);
    	myDB.close();
	}

	private void deleteImageFromDB(int ImageID) {
	    myDB = getSherlockActivity().openOrCreateDatabase(getString(R.string.db_name), Context.MODE_PRIVATE, null);
    	String sql = "DELETE FROM tblVideos WHERE ID IN "
    			+ "(SELECT ID FROM tblVideos LEFT JOIN tblImageVideoLink ON ID=VideoID WHERE ImageID="
    			+ ImageID + ")";
    	myDB.execSQL(sql);
    	sql = "DELETE FROM tblImageVideoLink WHERE ImageID="+ImageID;
    	myDB.execSQL(sql);
    	sql = "DELETE FROM tblImages WHERE ID="+ImageID;
    	myDB.execSQL(sql);
    	myDB.close();
	}

	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("imageid", globalImageIDMustStopUsingSoon);
//		outState.putLong("imageid", ImageID);
		if (file != null)
			outState.putString("file", file.getAbsolutePath());
		outState.putBoolean("replaceImage", replaceImage);
//		outState.putBoolean("videoProcessing", videoProcessing);
		super.onSaveInstanceState(outState);
	}
/*
    private void copy(InputStream in, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[10240];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
*/
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
    	mRecordButton.setClickable(false);
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("AudioRecord", "prepare() failed");
            ACRA.getErrorReporter().handleSilentException(e);
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
        mRecordButton.setClickable(true);
    }

    private void startRecording() {
    	mPlayButton.setClickable(false);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("AudioRecord", "prepare() failed");
            ACRA.getErrorReporter().handleSilentException(e);
        }
        try {
        	mRecorder.start();
        } catch (IllegalStateException ise) {
        	Log.e("mRecorder", "start() failed");
        	ACRA.getErrorReporter().handleSilentException(ise);
        	Boast.makeText(v.getContext(), R.string.unable_to_record_audio).show(true);
        }
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        mPlayButton.setClickable(true);
    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                	setBackgroundResource(R.drawable.btn_stop);
                } else {
                	setBackgroundResource(R.drawable.btn_record);
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
        	setBackgroundResource(R.drawable.btn_record);
//            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                	setBackgroundResource(R.drawable.btn_stop);
//                    setText("Stop playing");
                } else {
                	setBackgroundResource(R.drawable.btn_play);
//                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
        	setBackgroundResource(R.drawable.btn_play);
//            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

	@Override
	public void onImageChosen(final ChosenImage image) {
		getSherlockActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (image != null) {
					filePath = image.getFilePathOriginal();
					handleSelectedImage(filePath, globalImageIDMustStopUsingSoon);
				}
			}
		});

	}

	@Override
	public void onVideoChosen(final ChosenVideo video) {
		getSherlockActivity().runOnUiThread(new Runnable() {

	        @Override
	        public void run() {
	            if (video != null) {
	            	filePath = video.getVideoFilePath();
	                handleSelectedVideo(filePath, globalImageIDMustStopUsingSoon);
	            }
	        }
	    });
	}

	@Override
	public void onError(String reason) {
		ACRA.getErrorReporter().putCustomData("reason", reason);
		ACRA.getErrorReporter().handleException(null);
	}
	
	// Should be called if for some reason the ImageChooserManager is null (Due
	// to destroying of activity for low memory situations)
	private void reinitializeImageChooser() {
		imageChooserManager = new ImageChooserManager(this, chooserType, false);
		imageChooserManager.setImageChooserListener(this);
		imageChooserManager.reinitialize(filePath);
	}

	private void reinitializeVideoChooser() {
		videoChooserManager = new VideoChooserManager(this, chooserType, false);
		videoChooserManager.setVideoChooserListener(this);
		videoChooserManager.reinitialize(filePath);
	}

	private String getFileExtension(String file) {
	    int lastIndexOf = file.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return file.substring(lastIndexOf);
	}

	private void getMP3(String text, final Integer imageID) throws Exception {
		String[] voice_id;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
        String autoDetect = "";
        if (sharedPrefs.getString("voices", null) == null)
        	autoDetect = "&DETECT=1";
		voice_id = sharedPrefs.getString("voices", "2_1_1").split("_");
		OkHttpClient client = new OkHttpClient();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		audioFileName = "micomm_" + timeStamp + ".mp3";
		String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + 
				"/Pictures/"+getSherlockActivity().getString(R.string.album_name)+"/" + audioFileName;
		file = new File(fileName);

		String body = "EID=" + voice_id[0] +
			"&LID=" + voice_id[1] +
			"&VID=" + voice_id[2] +
			"&TXT=" + URLEncoder.encode(text, "utf-8") +
			autoDetect;
//			Encrypt
		MCrypt mcrypt = new MCrypt();
		String encrypted = MCrypt.bytesToHex( mcrypt.encrypt(body));
		Request request = new Request.Builder()
			.url("http://pmu-naphtul.rhcloud.com/get_tts.php")
//				.url("http://naphtul.no-ip.org:10080/get_tts.php")
			.post(RequestBody.create(MediaType.parse("text/plain"), encrypted))
			.addHeader("User-Agent", System.getProperty("http.agent"))
			.build();
	    if (!LocalService.processingMedia.contains(imageID))
	    	LocalService.processingMedia.add(imageID);
		final Activity a = getSherlockActivity();
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Response response) throws IOException, RuntimeException {
				if (response.code() != 200){
					backgroundThreadToast(a.getApplicationContext(),
							getString(R.string.failure_connecting_to_server));
					return;
				}
			    InputStream is = response.body().byteStream();
			    BufferedInputStream input = new BufferedInputStream(is);
			    OutputStream output = new FileOutputStream(file);

			    byte[] data = new byte[1024];

			    @SuppressWarnings("unused")
				long total = 0;
			    int count;

			    while ((count = input.read(data)) != -1) {
			        total += count;
			        output.write(data, 0, count);
			    }

			    output.flush();
			    output.close();
			    input.close();		
				MainActivity.mService.addAudio(audioFileName);
				insertOrUpdateVideoToDB(audioFileName, 3, imageID);
				LocalService.processingMedia.remove(imageID);
				a.runOnUiThread(new Runnable() {
					public void run() {
						gv = (GridView) v.findViewById(R.id.gridView);
						gv.setAdapter(new ImageAdapter(v.getContext(), args));
					}
				});
			}
			
			@Override
			public void onFailure(Request arg0, IOException arg1) {
				backgroundThreadToast(a.getApplicationContext(),
						getString(R.string.failure_connecting_to_server));
				LocalService.processingMedia.remove(imageID);
				arg1.printStackTrace();
			}
		});
	}
	
	public static void backgroundThreadToast(final Context context,
	        final String msg) {
	    if (context != null && msg != null) {
	        new Handler(Looper.getMainLooper()).post(new Runnable() {

	            @Override
	            public void run() {
	                Boast.makeText(context, msg).show();
	            }
	        });
	    }
	}
// TODO See if anyone answers me on StackOverflow:
// http://stackoverflow.com/questions/28730058/how-to-use-volley-to-post-a-string-in-the-body-and-to-save-the-binary-response-t

/*
	private void getMP3(String text, final Long ImageID) {
		String[] voice_id;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
        String autoDetect = "";
        if (sharedPrefs.getString("voices", null) == null)
        	autoDetect = "&DETECT=1";
		voice_id = sharedPrefs.getString("voices", "2_1_1").split("_");
		RequestQueue mRequestQueue;
		Network network = new BasicNetwork(new HurlStack());
		mRequestQueue = new RequestQueue(null, network);
		mRequestQueue.start();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		audioFileName = "micomm_" + timeStamp + ".mp3";
		String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + 
				"/Pictures/"+getSherlockActivity().getString(R.string.album_name)+"/" + audioFileName;
		file = new File(fileName);

		String body = "EID=" + voice_id[0] +
			"&LID=" + voice_id[1] +
			"&VID=" + voice_id[2] +
			"&TXT=" + URLEncoder.encode(text, "utf-8") +
			autoDetect;
//			Encrypt
		MCrypt mcrypt = new MCrypt();
		String encrypted = MCrypt.bytesToHex( mcrypt.encrypt(body));
		StringRequest request = new StringRequest(Request.Method.POST,
				"http://pmu-naphtul.rhcloud.com/get_tts.php",
				encrypted,
				new Response.Listener<String>(){

					@Override
					public void onResponse(String response) {
					    InputStream is = new ByteArrayInputStream(response.getBytes());
					    BufferedInputStream input = new BufferedInputStream(is);
					    OutputStream output = new FileOutputStream(file);

					    byte[] data = new byte[1024];

					    @SuppressWarnings("unused")
						long total = 0;
					    int count;

					    while ((count = input.read(data)) != -1) {
					        total += count;
					        output.write(data, 0, count);
					    }

					    output.flush();
					    output.close();
					    input.close();		
						MainActivity.mService.addAudio(audioFileName);
						insertOrUpdateVideoToDB(audioFileName, 3, ImageID);
					}
			
				}, 
				new Response.ErrorListener(){

					@Override
					public void onErrorResponse(VolleyError error) {
						
					}
					
				});
		mRequestQueue.add(request);
	}
*/

	private class HandleCamVid extends AsyncTask<Intent, Void, String>{
		private static final String TAG = "MiComm";
		private Integer imageID;
		
		public HandleCamVid (Integer imageID){
			this.imageID = imageID;
		}
		
		@Override
		protected String doInBackground(Intent... data) {
			String fileName = "";
			try {
				AssetFileDescriptor fileAsset = getSherlockActivity().getContentResolver()
						.openAssetFileDescriptor(data[0].getData(), "r");
			    FileInputStream fis = fileAsset.createInputStream();
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
				fileName = "micomm_" + timeStamp + ".3gp";
			    FileOutputStream fos = new FileOutputStream(new File(prependFilePath(fileName)));
			    Log.d(TAG, "AsyncTask debugging - Started Copying");

			    byte[] buf = new byte[10240];
			    int len;
			    while ((len = fis.read(buf)) > 0) {
			        fos.write(buf, 0, len);
			    }
			    fis.close();
			    fos.close();
			    Log.d(TAG, "AsyncTask debugging - Done Copying");
			} catch (IOException io_e) {
				io_e.printStackTrace();
			}
			return fileName;
		}

		@Override
		protected void onPostExecute(String fileName){
		    Log.d(TAG, "AsyncTask debugging - onPostExecute");
			LocalService.processingMedia.remove(imageID);
		    insertOrUpdateVideoToDB(fileName, 1, imageID);
		    Log.d(TAG, "AsyncTask debugging - Done inserting to DB");			    
			gv = (GridView) v.findViewById(R.id.gridView);
			gv.setAdapter(new ImageAdapter(v.getContext(), args));
			super.onPostExecute(fileName);
		}
	}
}