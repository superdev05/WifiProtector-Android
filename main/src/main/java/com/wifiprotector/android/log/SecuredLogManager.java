package com.wifiprotector.android.log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.wifiprotector.android.Preferences;
import com.wifiprotector.android.activities.MainActivity;
import com.wifiprotector.android.model.LicenceType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import de.blinkt.openvpn.BuildConfig;
import timber.log.Timber;
public class SecuredLogManager {

	public static SecuredLogManager gLogManager;
	public static String LOGDIR_PATH = "/sdcard/wplog";
	public static String LOGFILE_PREFIX = "gosecured";
	public static String LOGFILE_SUFFIX = ".log";
	public static String SENDLOG_URL = "https://services.wifiprotector.com/wifiprotwebservice.asmx/SendAllUserLogs";
	public String logFilePath;
	
	private File fp;
	private FileOutputStream fOut;

	private static MainActivity gMainActivigty;
	public static SecuredLogManager getLogManager(){
		
		if(gLogManager == null)
		{
			gLogManager = new SecuredLogManager();
		}
		return gLogManager;
	}
	
	public SecuredLogManager()
	{
		Calendar c = Calendar.getInstance(); 
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int date = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		
		logFilePath = String.format("%s%d-%d-%d-%d-%d-%d%s", LOGFILE_PREFIX, year, month, date, hour, minute, second, LOGFILE_SUFFIX);
		
		File dir = new File(LOGDIR_PATH);
		if(!dir.exists()){
			dir.mkdirs();
		}
		fp = new File(dir, logFilePath);
		try {
			fOut = new FileOutputStream(fp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//writing version info
		String strVersion = String.format("WIFI-PROTECTOR VERSION:%s\n\n", BuildConfig.VERSION_NAME);

		try {
			fOut.write(strVersion.getBytes());
			fOut.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setMainActivity(MainActivity gActivity){

		gMainActivigty = gActivity;
	}

	public void writeLog(int logLevel, String name, String message, Throwable th)
	{
		String strLogLevel = null;
		switch(logLevel)
		{
		case 7:
			strLogLevel = "ASSERT";
			Log.e(name, message);
			break;
		case 6:
			strLogLevel = "ERROR";
			Log.e(name, message);
			break;
		case 5:
			strLogLevel = "WARN";
			Log.w(name, message);
			break;
		case 4:
			strLogLevel = "INFO";
			Log.i(name, message);
			break;
		case 3:
			strLogLevel = "DEBUG";
			Log.d(name, message);
			break;
		case 2:
			strLogLevel = "VERBOSE";
			Log.v(name, message);
			break;
		default:
			strLogLevel = "DEFAULT";
			break;
		}
		
		String strErrMsg;
		strErrMsg = String.format("LOGLEVEL : %s : From - %s : Msg - %s\n", strLogLevel, name, message);
		try {
			fOut.write(strErrMsg.getBytes());
			fOut.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void writeLog(int logLevel, String name, String message, Exception e2)
	{
		String strLogLevel = null;
		switch(logLevel)
		{
		case 7:
			strLogLevel = "ASSERT";
			Log.e(name, message);
			break;
		case 6:
			strLogLevel = "ERROR";
			Log.e(name, message);
			break;
		case 5:
			strLogLevel = "WARN";
			Log.w(name, message);
			break;
		case 4:
			strLogLevel = "INFO";
			Log.i(name, message);
			break;
		case 3:
			strLogLevel = "DEBUG";
			Log.d(name, message);
			break;
		case 2:
			strLogLevel = "VERBOSE";
			Log.v(name, message);
			break;
		default:
			strLogLevel = "DEFAULT";
			break;
		}
		
		String strErrMsg;
		strErrMsg = String.format("LOGLEVEL : %s : From - %s : Msg - %s\n", strLogLevel, name, message);
		try {
			fOut.write(strErrMsg.getBytes());
			fOut.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void writeLog(int logLevel, String name, String message)
	{
		String strLogLevel = null;
		switch(logLevel)
		{
		case 7:
			strLogLevel = "ASSERT";
			Log.e(name, message);
			break;
		case 6:
			strLogLevel = "ERROR";
			Log.e(name, message);
			break;
		case 5:
			strLogLevel = "WARN";
			Log.w(name, message);
			break;
		case 4:
			strLogLevel = "INFO";
			Log.i(name, message);
			break;
		case 3:
			strLogLevel = "DEBUG";
			Log.d(name, message);
			break;
		case 2:
			strLogLevel = "VERBOSE";
			Log.v(name, message);
			break;
		default:
			strLogLevel = "DEFAULT";
			break;
		}
		
		String strErrMsg;
		strErrMsg = String.format("LOGLEVEL : %s : From - %s : Msg - %s\n", strLogLevel, name, message);
		try {
			fOut.write(strErrMsg.getBytes());
			fOut.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private String strEmail = "";
	private String strComment = "";


	public void sendLog(String email, String comment ){
		strEmail = email;
		strComment = comment;
		new UploadingTask().execute();
	}

	private static class fileuploadResponseHandler implements ResponseHandler<Object> {

		@Override
		public Object handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {

			HttpEntity r_entity = response.getEntity();
			String responseString = EntityUtils.toString(r_entity);
			Log.d("UPLOAD", responseString);

			if(responseString.toUpperCase().contains("OK")){
				gMainActivigty.m_handler.sendEmptyMessage(0);
			}else {
				gMainActivigty.m_handler.sendEmptyMessage(1);
			}
			return null;
		}

	}

	private class UploadingTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Object doInBackground(Object... paramss) {

			String fileOutName = String.format("/sdcard/wplog_mobile_from_%s.zip", strEmail) ;

			zipFolder(LOGDIR_PATH, fileOutName);

			HttpPost httppost = new HttpPost(SENDLOG_URL);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

			final File file = new File(fileOutName);
			FileBody fb = new FileBody(file);

			builder.addPart("attachedZip", fb);
			builder.addTextBody("email", strEmail);
			builder.addTextBody("dev", "");
			builder.addTextBody("productVersion", BuildConfig.VERSION_NAME);
			builder.addTextBody("comment", strComment);
			builder.addTextBody("uniqueId", "");
			builder.addTextBody("productId", "");

			LicenceType licType = Preferences.WifiProtector.licenceType();
			String strLicense = null;
			if(licType == LicenceType.FREE)
				strLicense = "FREE";
			else
				strLicense = "PAID";

			builder.addTextBody("licenseType", strLicense);

			String strfileName =String.format("wplog_mobile_from_%s.zip", strEmail);
			builder.addTextBody("attachedZipName", strfileName);

			final HttpEntity httpEntity = builder.build();

			httppost.setEntity(httpEntity);

			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			DefaultHttpClient mHttpClient = new DefaultHttpClient(params);

			try {
				mHttpClient.execute(httppost, new fileuploadResponseHandler());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object params){

		}
	}
	private static void zipFolder(String inputFolderPath, String outZipPath) {
		try {
			FileOutputStream fos = new FileOutputStream(outZipPath);
			ZipOutputStream zos = new ZipOutputStream(fos);
			File srcFile = new File(inputFolderPath);
			File[] files = srcFile.listFiles();
			Log.d("", "Zip directory: " + srcFile.getName());
			for (int i = 0; i < files.length; i++) {
				Log.d("", "Adding file: " + files[i].getName());
				byte[] buffer = new byte[1024];
				FileInputStream fis = new FileInputStream(files[i]);
				zos.putNextEntry(new ZipEntry(files[i].getName()));
				int length;
				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
				fis.close();
			}
			zos.close();
		} catch (IOException ioe) {
			Log.e("", ioe.getMessage());
		}
	}
}
