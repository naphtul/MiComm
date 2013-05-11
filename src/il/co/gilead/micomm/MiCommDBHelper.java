package il.co.gilead.micomm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MiCommDBHelper extends MainActivity {
	
	public MiCommDBHelper() {}
	
	public static String exportDBtoInsertStatements (Context ctx, SQLiteDatabase myDB, int pageID){
		String output = "";
		String sql;
		String eol = System.getProperty("line.separator");
		Cursor c;
		
		myDB = ctx.openOrCreateDatabase(ctx.getString(R.string.db_name), Context.MODE_PRIVATE, null);
		sql = "SELECT ID, ImageName, ImageDescription, Copyright FROM tblImages WHERE Page=" + pageID +
				" ORDER BY ID";
		c = myDB.rawQuery(sql, null);
        if(c.moveToFirst()){
            do{
            	output = output + "INSERT INTO tblImages (ID, ImageName, ImageDescription, Copyright) VALUES (" +
            			c.getString(0) + ", '" + c.getString(1) + "', '" +
            			c.getString(2) + "', " + c.getString(3) +");" + eol;
            }
            while(c.moveToNext());
        }
		c.close();
		sql = "SELECT tblVideos.ID, tblVideos.VideoName, tblVideos.VideoDescription, tblVideos.VideoType " +
				"FROM tblVideos " +
				"LEFT JOIN tblImageVideoLink ON tblImageVideoLink.VideoID=tblVideos.ID " +
				"LEFT JOIN tblImages ON tblImageVideoLink.ImageID=tblImages.ID " +
				"WHERE tblImages.Page="+pageID;
		c = myDB.rawQuery(sql, null);
        if(c.moveToFirst()){
            do{
            	output = output + "INSERT INTO tblVideos (ID, VideoName, VideoDescription, VideoType) " +
            			"VALUES (" +
            			c.getString(0) + ", '" + c.getString(1) + "', '" +
            			c.getString(2) + "', " + c.getString(3) + ");" + eol;
            }
            while(c.moveToNext());
        }
        c.close();
		sql = "SELECT LinkID, ImageID, VideoID " +
				"FROM tblImageVideoLink " +
				"LEFT JOIN tblImages ON tblImageVideoLink.ImageID=tblImages.ID " +
				"WHERE tblImages.Page="+pageID;
		c = myDB.rawQuery(sql, null);
        if(c.moveToFirst()){
            do{
            	output = output + "INSERT INTO tblImageVideoLink (LinkID, ImageID, VideoID) " +
            			"VALUES (" +
            			c.getString(0) + ", " + c.getString(1) + ", " + 
            			c.getString(2) + ");" + eol;
            }
            while(c.moveToNext());
        }
        c.close();
		sql = "SELECT ID, PageName " +
				"FROM tblPages " +
				"WHERE ID=" + pageID + 
				" ORDER BY ID";
		c = myDB.rawQuery(sql, null);
        if(c.moveToFirst()){
            do{
            	output = output + "INSERT INTO tblPages (ID, PageName) " +
            			"VALUES (" + c.getString(0) + ", '" + c.getString(1) + "');" + eol;
            }
            while(c.moveToNext());
        }
        c.close();
        myDB.close();
		return output;
	}

	public static String exportDBtoCSV (Context ctx, SQLiteDatabase myDB, int pageID){
		String output = "";
		String sql;
		String eol = System.getProperty("line.separator");
		Cursor c;
		
		myDB = ctx.openOrCreateDatabase(ctx.getString(R.string.db_name), Context.MODE_PRIVATE, null);
		sql = "SELECT PageName FROM tblPages WHERE ID="+pageID;
		c = myDB.rawQuery(sql, null);
        if(c.moveToFirst()){
            do{
            	output = output + "INSERT INTO tblPages (PageName) " +
            			"VALUES ('" + c.getString(0) + "');" + eol;
            }
            while(c.moveToNext());
        }
        c.close();
		sql = "SELECT DISTINCT I.ImageName, I.ImageDescription, I.Copyright, V.VideoName, " +
				"V.VideoDescription, V.VideoType " +
				"FROM tblImages I " +
				"LEFT JOIN tblImageVideoLink L ON L.ImageID=I.ID " +
				"LEFT JOIN tblVideos V ON L.VideoID=V.ID " +
				"WHERE I.Page =" + pageID +
				" ORDER BY I.ID";
		c = myDB.rawQuery(sql, null);
        if(c.moveToFirst()){
            do{
            	String ImageName = c.getString(0);
            	String ImageDescription = c.getString(1);
            	String Copyright = c.getString(2);
            	String VideoName = c.getString(3);
            	String VideoDescription = c.getString(4);
            	String VideoType = c.getString(5);
            	if (ImageName == null) ImageName = "";
            	if (ImageDescription == null) ImageDescription = "";
            	if (Copyright == null) Copyright = "";
            	if (VideoName == null) VideoName = "";
            	if (VideoDescription == null) VideoDescription = "";
            	if (VideoType == null) VideoType = "";
            	output = output + ImageName + "," + ImageDescription + "," + Copyright + "," + VideoName +
            			"," + VideoDescription + "," + VideoType + eol;
            }
            while(c.moveToNext());
        }
        c.close();
        myDB.close();
		return output;
	}

	public static void importPage (){
		
	}
}
