package com.home.dvr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class DVRActivity extends Activity implements SurfaceHolder.Callback,
		View.OnClickListener, Camera.PreviewCallback {

	private Button saveBtn;
	private Button recBtn;
	private Boolean record = false;
	
	private Camera camera;
	private SurfaceHolder surHold;
	private SurfaceView preView;
	private VideoRecAndSetings recorder;
	private long recBeginTime;

	Thread myThread;
	boolean running;
	private Handler h;

	
	private  String FILENAME = "";
	private  String TEXT = "";
	private int tick;
	private Boolean ticked = false;
	
	MyLoc loc;
	
	private static final String TAG = "DVR";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.main);
		preView = (SurfaceView) findViewById(R.id.SurfaceView);

		surHold = preView.getHolder();
		surHold.addCallback(this);
		surHold.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

		saveBtn = (Button) findViewById(R.id.Save);
		recBtn = (Button) findViewById(R.id.Record);
		saveBtn.setOnClickListener(this);
		recBtn.setOnClickListener(this);
		saveBtn.setEnabled(false);

		recorder = new VideoRecAndSetings();
		Log.v(TAG, "Inicial complete!");

		File saveDir = new File("/sdcard/DVRRamil/");

		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}
		tick = 0;
		h = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if(record && msg.what == 1)
				{
					Log.v(TAG, "new semple");
					record = !record;
					stopCamera();
					motor();
					record = !record;
				}
				if(record && msg.what == 2)
				{
					tick++;
					Calendar c = Calendar.getInstance();
					String mount = leftPad(Integer.toString(c.get(Calendar.MONTH)), 2) + ".";
					String day = leftPad(Integer.toString(c.get(Calendar.DAY_OF_MONTH)), 2) + ".";
					String min = leftPad(Integer.toString(c.get(Calendar.MINUTE)), 2) + ":";
					String hour = leftPad(Integer.toString(c.get(Calendar.HOUR_OF_DAY)), 2) + ":";
					String sec = leftPad(Integer.toString(c.get(Calendar.SECOND)), 2);
					TEXT = TEXT + "\n\n" +Integer.toString(tick)+ "\n" +"00:00:"+Integer.toString(tick)+ ",000 --> 00:00:" + Integer.toString(tick +1)+ ",000";
					TEXT = TEXT + "\nTime: " +mount+day+ c.get(Calendar.YEAR)+ " " + hour + min + sec;
					TEXT = TEXT + "\nPosition: " + Double.toString(loc.latitude) + ", " + Double.toString(loc.longitud);
					ticked = false;
				}

			};
		};
		
		
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		loc = new MyLoc();
		LocationListener mlocListener = loc;
		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

	}

	@Override
	protected void onResume() {
		Log.v(TAG, "ONRESUME!");
		super.onResume();
		camera = Camera.open();
		recorder.newV();

		startThread();
		Log.v(TAG, "ONRESUME complete!");
	}

	@Override
	protected void onPause() {
		Log.v(TAG, "Pause!");
		super.onPause();
		if (recorder != null)
			recorder.died();

		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}

		stopThread();
		Log.v(TAG, "Pause complete!");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (camera != null) {
			try {

				camera.setPreviewDisplay(holder);
				camera.setPreviewCallback(this);

			} catch (IOException e) {
				e.printStackTrace();
			}

			Size size = camera.getParameters().getPreviewSize();

			LayoutParams param = preView.getLayoutParams();

			camera.setDisplayOrientation(0);
			param.width = preView.getWidth();
			;
			param.height = (int) (preView.getWidth() / (size.width / size.height));

			preView.setLayoutParams(param);
			camera.startPreview();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public void onClick(View v) {
		if (v == saveBtn) {
			Log.v(TAG, "SAve!");
			stopCamera();
			motor();
		} else if (v == recBtn) {
			if (record) {
				stopCamera();
			} else {

				motor();
			}

			record = !record;
		}

	}

	@Override
	public void onPreviewFrame(byte[] paramArrayOfByte, Camera paramCamera) {
	}

	private void stopCamera() {
		recorder.stop();

		try {
			camera.reconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}

		camera.startPreview();
		saveBtn.setEnabled(false);
		recBtn.setText("Record");
		
		saveFile(FILENAME);
	}

	private void motor() {
		tick = 0;
		saveBtn.setEnabled(true);
		camera.stopPreview();
		camera.unlock();
		recorder.setCamera(camera);
		recorder.setRecorderParams(100, 100, 100, 1, 15, 640, 480, 0, 0);
		recorder.setPreview(surHold.getSurface());
		
		Calendar c = Calendar.getInstance();
		String mount = leftPad(Integer.toString(c.get(Calendar.MONTH)), 2);
		String day = leftPad(Integer.toString(c.get(Calendar.DAY_OF_MONTH)), 2);
		String min = leftPad(Integer.toString(c.get(Calendar.MINUTE)), 2);
		String hour = leftPad(Integer.toString(c.get(Calendar.HOUR_OF_DAY)), 2);
		String sec = leftPad(Integer.toString(c.get(Calendar.SECOND)), 2);
		FILENAME = "/sdcard/DVRRamil/" + Integer.toString(c.get(Calendar.YEAR)) + mount + day + min + hour + sec;
		recorder.start(FILENAME + ".avi");
		FILENAME = "/mnt" + FILENAME + ".srt";


		TEXT = "";
		recBtn.setText("Stop");

		recBeginTime = System.currentTimeMillis();

	}



	public void startThread() {
		running = true;
		myThread = new Thread(new Runnable() {
			public void run() {
				while (running) {
					Calendar c = Calendar.getInstance();
					Log.v(TAG, "delta = " + Integer.toString((int) (System.currentTimeMillis() - recBeginTime)));
					if (record && System.currentTimeMillis() - recBeginTime > 60000)
					{
						h.sendEmptyMessage(1);
					}
					if (!ticked && record && c.getTimeInMillis() - recBeginTime > tick*1000)
					{
						ticked = true;
						Log.v(TAG,"tick");
						h.sendEmptyMessage(2);
					}
					
				}
			}
		});
		myThread.setPriority(1);
		myThread.start();
	}

	public void stopThread() {
		running = false;
		boolean retry = true;
		while (retry) {
			try {
				myThread.join();
				retry = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveFile(String FileName) {
		try {	
			File resolveMeSDCard = new File(FILENAME);
			resolveMeSDCard.createNewFile();
			FileOutputStream fos = new FileOutputStream(resolveMeSDCard);
			fos.write(TEXT.getBytes());
			fos.close();
		} catch (Throwable t) {
			Toast.makeText(getApplicationContext(),
					"Exception: " + t.toString(), Toast.LENGTH_LONG).show();
		}
	}
	
	private String leftPad(String in, int needLength)
	{
		while(in.length() < needLength)
		{
			in = "0" + in; 
		}
		return in;
	}

}
