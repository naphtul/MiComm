package il.co.gilead.micomm;

import android.content.Context;

public class PageObject{
	
	private Integer pageId;
	private String pageName;
	private boolean copyright;
	
	PageObject(Context c, Integer id, String name, boolean copyRight)
	{
		pageId = id;
		pageName = name;
		copyright = copyRight;
	}
	
	Integer getPageId()
	{
		return pageId;
	}
	
	String getPageName()
	{
		return pageName;
	}

	boolean isCopyrightProtected()
	{
		return copyright;
	}
}
