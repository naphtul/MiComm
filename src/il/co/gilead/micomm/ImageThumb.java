package il.co.gilead.micomm;

import android.content.Context;

public class ImageThumb{
	private Integer imageId;
	private String imageName;
	private String imageDescription;
	private String videoName;
	private String videoType;
	private Boolean isProcessing;
	
	ImageThumb(Context c, Integer imageId, String imageName, String imageDescription, String videoName,
			String videoType, Boolean isProcessing){
		this.imageId = imageId;
		this.imageName = imageName;
		this.imageDescription = imageDescription;
		this.videoName = videoName;
		this.videoType = videoType;
		this.isProcessing = isProcessing;
	}

	public Integer getImageId(){
		return imageId;
	}

	public String getImageName(){
		return imageName;
	}

	public String getImageDescription(){
		return imageDescription;
	}

	public String getVideoName(){
		return videoName;
	}

	public String getVideoType(){
		return videoType;
	}

	public Boolean isProcessing(){
		return isProcessing;
	}
	
	public void setProcessingFlag(Boolean isProcessing) {
		this.isProcessing = isProcessing;
	}
}
