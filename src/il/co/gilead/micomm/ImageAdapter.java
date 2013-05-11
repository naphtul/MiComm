package il.co.gilead.micomm;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	Bundle args;
	private List<ImageThumb> images;
	private Context context;
//	private Bitmap bm;
	private Integer tileSize = 144;
	private Integer numOfTiles = 12;
	private List<Integer> colors;
	private int index = 0;
	private int colorsSize;
	private int pgNum;
//	private GridView.LayoutParams mImageViewLayoutParams;
//    private int mItemHeight = 0;

	public ImageAdapter(Context applicationContext, Bundle arguments) {
		args = arguments;
		context = applicationContext;
		pgNum = args.getInt("intPageNum");
		numOfTiles = args.getInt("intNumOfTiles");
		tileSize = args.getInt("intTileSize");
		images = populateImagesArrayFromDatabase(numOfTiles, pgNum);
//		numOfTiles = intNumOfTiles;
        colors = initListOfColors();
        colorsSize = colors.size();
//        mImageViewLayoutParams = new GridView.LayoutParams(
//                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	@Override
	public int getCount() {
		return images.size();
//		return numOfTiles;
	}

	@Override
	public ImageThumb getItem(int position) {
		if (position < images.size())
			return images.get(position);
		else
			return null;
	}

	@Override
	public long getItemId(int position) {
		return images.get(position).getImageId();
	}
	
	public String getItemDescription(int position) {
		return (String) images.get(position).getImageDescription();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView iv;

		if (convertView != null) {
			iv = (ImageView) convertView;
		} else {
			iv = new ImageView(context);
//			iv = new ImageViewRounded(context);
			iv.setLayoutParams(new GridView.LayoutParams(tileSize, tileSize));
//			iv.setScaleType(ScaleType.CENTER_CROP);
			iv.setPadding(5, 5, 5, 5);
		}
		index++;
		if (index >= colorsSize)
			index = 0;
		Integer imgID = null;
		imgID = images.get(position).getImageId();
		if (imgID == 9999){
			iv.setImageResource(R.drawable.camera);
		}else{
			if (imgID != null){
				String imagePath = prependFilePath(context, images.get(position)
						.getImageName());
//				File imageFile = new File(imagePath);
//				Bitmap bm;
//				bm = ImageResizer.decodeSampledBitmapFromFile(imagePath, tileSize, tileSize);
//				bm = com.svenkapudija.imageresizer.ImageResizer.resize(imageFile, tileSize, tileSize);
//				bm = com.svenkapudija.imageresizer.utils.ImageDecoder.decodeFile(imageFile);
//				bm = BitmapFactory.decodeFile(imagePath);
				
//				Log.d("MiComm", "Difference between Width and Height: "+Math.abs(bm.getWidth() - bm.getHeight()));
//				if (bm == null){
//					iv.setImageResource(android.R.drawable.ic_menu_gallery);
//					return iv;
//				}
//				if (Math.abs(bm.getWidth() - bm.getHeight()) < 10)
//					bm = Bitmap.createScaledBitmap(bm, tileSize, tileSize, false);
//	        	int x = Math.round((bm.getWidth() - tileSize) / 2);
//	        	int y = Math.round((bm.getHeight() - tileSize) / 2);
//	        	Log.d("MiComm", "x: "+x);
//	        	Log.d("MiComm", "y: "+y);
//	        	int tileSizeX = tileSize;
//	        	int tileSizeY = tileSize;
//	        	if (x<=0){
//	        		x = 0;
//	        		tileSizeX = bm.getWidth();
//	        		if (tileSizeY > tileSizeX)
//	        			tileSizeY = tileSizeX;
//	        	}
//	        	if (y<=0){
//	        		y = 0;
//	        		tileSizeY = bm.getHeight();
//	        		if (tileSizeX > tileSizeY)
//		        		tileSizeX = tileSizeY;
//	        	}
//				if (bm.getWidth() > tileSize || bm.getHeight() > tileSize) {
//		        	Log.d("MiComm", "bm.getWidth(): "+bm.getWidth());
//		        	Log.d("MiComm", "bm.getHeight(): "+bm.getHeight());
//		        	Log.d("MiComm", "tileSize: "+tileSize);
//		        	Log.d("MiComm", "tileSizeX: "+tileSizeX);
//		        	Log.d("MiComm", "tileSizeY: "+tileSizeY);
//		        	Log.d("MiComm", "x: "+x);
//		        	Log.d("MiComm", "y: "+y);

//		        	int x = Math.round((bm.getWidth() - tileSize) / 2);
//		        	int y = Math.round((bm.getHeight() - tileSize) / 2);
//		            bm = Bitmap.createBitmap(bm, x, y, tileSizeX, tileSizeY);
//				}
//				bm = getRoundedCornerBitmap(bm, 20);
//				bm = GridFragment.getRoundedCornerBitmap(context, bm, 30, tileSize, tileSize, true, true, true, true);

//				bm = BitmapFactory.decodeFile(imagePath);
				iv.setBackgroundColor(colors.get(index));
//				iv.setImageBitmap(bm);
//				Picasso.with(context).load(imagePath).into(iv);
				Glide.with(context)
						.load(imagePath)
						.centerCrop()
						.into(iv);
//				// Check the height matches our calculated column width
//	            if (iv.getLayoutParams().height != mItemHeight) {
//	                iv.setLayoutParams(mImageViewLayoutParams);
//	            }
			}
		}
		return iv;
	}

	private List<ImageThumb> populateImagesArrayFromDatabase(Integer numOfTiles, Integer pageNum){
		List<ImageThumb> imageThumbs = new ArrayList<ImageThumb>();
		SQLiteDatabase myDB;

		myDB = context.openOrCreateDatabase(context.getString(R.string.db_name), Context.MODE_PRIVATE, null);
		String sql = "SELECT DISTINCT I.ID, I.ImageName, I.ImageDescription, V.VideoName, V.VideoType " +
				"FROM tblImages I " +
				"LEFT JOIN tblImageVideoLink L ON L.ImageID=I.ID " +
				"LEFT JOIN tblVideos V ON L.VideoID=V.ID " +
				"WHERE I.Page =" + pageNum + " ORDER BY I.ID";

	    Cursor c = myDB.rawQuery(sql, null);

	    if(c.moveToFirst()){
	        do{
	        	String ImageID = c.getString(0);
	        	String ImageName = c.getString(1);
	        	String ImageDescription = c.getString(2);
	        	String VideoName = c.getString(3);
	        	String VideoType = c.getString(4);
	        	ImageThumb it = new ImageThumb(context, Integer.parseInt(ImageID), ImageName, 
	        			ImageDescription, VideoName, VideoType, false);
	    		imageThumbs.add(it);
	    	}
	        while(c.moveToNext());
	    }
	    c.close();
	    myDB.close();
	    if (imageThumbs.size() < numOfTiles){
	    	ImageThumb it = new ImageThumb(context, 9999, "PlaceHolder", "", "", "", false);
	    	imageThumbs.add(it);
	    }

	   	return imageThumbs;
	}

	private List<Integer> initListOfColors(){
		List<Integer> colors = new ArrayList<Integer>();
		colors.add(Color.BLUE);
//		colors.add(Color.CYAN);
//		colors.add(Color.GREEN);
//		colors.add(Color.RED);
		colors.add(Color.MAGENTA);
		colors.add(Color.YELLOW);
		colors.add(Color.LTGRAY);
		colors.add(Color.rgb(250, 119, 5));
		colors.add(Color.rgb(51, 181, 229));
		colors.add(Color.rgb(153, 204, 0));
		colors.add(Color.rgb(255, 68, 68));
		return colors;
	}

	private String prependFilePath(Context context, String fileName){
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/Pictures/" + context.getString(R.string.album_name) + "/" + fileName;
		return filePath;
	}
	
}
