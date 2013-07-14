package com.physisjr.ipostal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraActivity extends Activity {


	private Camera mCamera;
	private CameraPreview mPreview;
	protected final String TAG = "CameraActivity";
	private Handler mHandler = new Handler();
	

	public static Camera getCameraInstance(){
		Camera c = null;
		try 
		{
			c = Camera.open(); // attempt to get a Camera instance
		}
		catch (Exception e)
		{
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camerapreview);
		mHandler.postDelayed(mandarImagem, 5000);

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
	}

	final private Runnable mandarImagem = new Runnable() 
	{
		public void run() {
			mCamera.takePicture(null, null, null, mPicture);
			Log.d(TAG, "Tirou foto");
			mHandler.postDelayed(mandarImagem, 5000);
		}
	};

	private String sendToServer(File file, String url)
	{
		try {
			HttpClient httpclient = new DefaultHttpClient();

			HttpPost httppost = new HttpPost(url);

			InputStreamEntity reqEntity = new InputStreamEntity(
					new FileInputStream(file), -1);
			reqEntity.setContentType("binary/octet-stream");
			reqEntity.setChunked(true); // Send in multiple parts if needed
			httppost.setEntity(reqEntity);
			ResponseHandler<String> responseHandler=new BasicResponseHandler();
			String responseBody = httpclient.execute(httppost, responseHandler);
			return responseBody;

		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
	}

	private PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			if(Environment.getExternalStorageState() == "MEDIA_MOUNTED")
			{
				String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
				File mediaFile;
				mediaFile = new File("IMG_"+ timeStamp + ".jpg");

				try {
					FileOutputStream fos = new FileOutputStream(mediaFile);
					fos.write(data);
					fos.close();
				} catch (FileNotFoundException e) {
					Log.d(TAG, "File not found: " + e.getMessage());
				} catch (IOException e) {
					Log.d(TAG, "Error accessing file: " + e.getMessage());
				}
				
				try
				{
					String server = sendToServer(mediaFile,"url");
					Toast.makeText(getApplicationContext(), server, Toast.LENGTH_LONG).show();
				}
				catch(Exception e)
				{
					Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				}
			}
			else
			{
				Log.d(TAG, "Error creating media file, check storage permissions");
				return;
			}
		}
	};
}
