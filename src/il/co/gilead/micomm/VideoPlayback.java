package il.co.gilead.micomm;

import java.io.File;

import org.acra.ACRA;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.MenuItem;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;
import android.support.v4.app.NavUtils;
import android.content.Intent;

public class VideoPlayback extends Activity {
	private VideoView mVideoView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar
//		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);    // Removes notification bar

		setContentView(R.layout.activity_video_playback);
		
		int position = 0;
	    if (savedInstanceState != null) {
	        position = savedInstanceState.getInt("pos");
	    }
	    
		String VideoPath = "", VideoType = "";
		Intent i = getIntent();
		Bundle b = i.getExtras();
		if (b != null && b.get("VideoPath") != null)
			VideoPath = (String) b.get("VideoPath");
		if (b != null && b.get("VideoType") != null)
			VideoType = (String) b.get("VideoType");
		if (VideoType.equals("") || VideoPath.equals("")){
			Boast.makeText(getApplicationContext(), R.string.video_not_ready).show(true);
			ACRA.getErrorReporter().handleSilentException(null);
		}else{
			playVideo(VideoPath, VideoType, position);
		}
	}

	private void playVideo(String VideoPath, String VideoType, int position) {
		mVideoView = (VideoView) findViewById(R.id.videoView);
		mVideoView.setMediaController(new MediaController(this));
		if (VideoType.equals("1")){
			if (!isFileReady(VideoPath))
				finish();
			mVideoView.setVideoPath(VideoPath);
		}else if (VideoType.equals("2")){
			Uri videoLink = Uri.parse(VideoPath);
			mVideoView.setVideoURI(videoLink);
		}
		mVideoView.seekTo(position);
		mVideoView.start();
		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				finish();
			}
		});
	}

	private Boolean isFileReady(String fileName){
		if (isExternalStorageReadable()) {
			File tempFile = new File(fileName);
			if (tempFile.exists()){
				if (tempFile.canRead()){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	/* Checks if external storage is available to at least read */
	private static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		        	return true;
		        }
		        return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    if (mVideoView.isPlaying())
	    	outState.putInt("pos", mVideoView.getCurrentPosition());
	}
}
