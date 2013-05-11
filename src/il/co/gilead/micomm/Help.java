package il.co.gilead.micomm;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.app.Activity;

public class Help extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		TextView tv = (TextView) findViewById(R.id.textViewHelp);
		tv.setMovementMethod(new ScrollingMovementMethod());
	}

}
