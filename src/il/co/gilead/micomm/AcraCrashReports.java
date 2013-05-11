package il.co.gilead.micomm;

import android.app.Application;
import android.content.Context;

import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(
        formKey = "",
        formUri = "http://naphtul.iriscouch.com/acra-micomm/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="reporter",
        formUriBasicAuthPassword="v0n.58976wj09SDT&W4")
public class AcraCrashReports extends Application {
	public static Context app;
	
	@Override
	public void onCreate() {
	//  public final void onCreate() {
		super.onCreate();
		
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		app = this;
	}
	
	public static Context getApp(){
		return app;
	}
}