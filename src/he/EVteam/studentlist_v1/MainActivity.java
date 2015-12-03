package he.EVteam.studentlist_v1;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	Button btnl,btns;
	String username,password;
	CheckBox login_check;
	EditText editText1,editText2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		//实例化
		btnl=(Button)findViewById(R.id.button1);
		btns=(Button)findViewById(R.id.button2);
		login_check=(CheckBox)findViewById(R.id.checkBox1);
		editText1=(EditText)findViewById(R.id.editText1);
		editText2=(EditText)findViewById(R.id.editText2);

		SharedPreferences sp = getSharedPreferences("box", MODE_PRIVATE);
		editText1.setText(sp.getString("user", ""));
		editText2.setText(sp.getString("pss", ""));

		btnl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				username=editText1.getText().toString();
				password=editText2.getText().toString();
				SharedPreferences sp = getSharedPreferences("box", MODE_PRIVATE);
				//记住密码
				if(login_check.isChecked()){
					SharedPreferences.Editor editor = sp.edit();
					editor.putString("user", username);
					editor.putString("pss", password);
					editor.commit();
				}
				new LoginNetAsyncTask().execute(username,password);
			}
		});

		btns.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final Dialog dialog = new Dialog(MainActivity.this);
				dialog.setContentView(R.layout.signup);
				dialog.setTitle("注册");
				Window dialogWindow = dialog.getWindow();
				WindowManager.LayoutParams lp = dialogWindow.getAttributes();
				dialogWindow.setGravity(Gravity.CENTER);
				dialogWindow.setAttributes(lp);

				Button btnConfirm = (Button) dialog.findViewById(R.id.button1);
				Button btnCancel = (Button) dialog.findViewById(R.id.button2);

				btnConfirm.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						EditText snEditText = (EditText) dialog.findViewById(R.id.editText1);
						String Sname_L=snEditText.getText().toString();
						Log.d("Sname-L",Sname_L);

						EditText psEditText = (EditText) dialog.findViewById(R.id.editText2);
						String ps_L=psEditText.getText().toString();
						Log.d("psc_L",ps_L);

						EditText pscEditText = (EditText) dialog.findViewById(R.id.editText3);
						String psc_L=pscEditText.getText().toString();
						Log.d("psc_L",psc_L);
						if(Sname_L.length() == 0){
							Toast.makeText(MainActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
						}
						if(!psc_L.equals(ps_L)){
							Toast.makeText(MainActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
						}
						if(Sname_L.length() != 0 && psc_L.equals(ps_L)){
							dialog.dismiss();
							new SignupNetAsyncTask().execute(Sname_L,ps_L);
							dialog.dismiss();
						}
					}
				});

				btnCancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}

	class LoginNetAsyncTask extends AsyncTask<String, Object, String> {

		// WSDL文档中的命名空间
		private static final String targetNameSpace = "http://tempuri.org/";
		// WSDL文档中的URL
		private static final String WSDL = "http://370381b0.nat123.net:18506/WebService1.asmx";
		// 需要调用的方法名
		private static final String method  = "Login";
		//进度条
		ProgressDialog pd;

		/** 
		 * 执行任务之前调用 
		 */  
		@Override  
		protected void onPreExecute() {  
			// TODO Auto-generated method stub  
			super.onPreExecute();  
			pd = new ProgressDialog(MainActivity.this);  
			pd.setMessage("正在验证，请稍后……");  
			pd.setIndeterminate(false);// 在最大值最小值中移动  
			pd.setCancelable(true);// 可以取消  
			pd.show();  	        
		}  

		/**
		 * 执行任务后调用
		 */
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//进度条消失
			pd.dismiss();
			switch (result) {
			case "ok":
				//Toast.makeText(MainActivity.this, "登录成功，正在跳转...", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.putExtra("Sname",username);
				intent.setClass(MainActivity.this,ListActivity.class);
				startActivity(intent);
				MainActivity.this.finish();
				break;

			case "incorrect":
				editText2.setText("");
				Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
				break;

			case "neterror":
				Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
				break;

			default:
				Toast.makeText(MainActivity.this, "无用户", Toast.LENGTH_SHORT).show();
			}
		}


		/**
		 * 执行任务
		 */
		@Override
		protected String doInBackground(String... params) {
			// 根据命名空间和方法得到SoapObject对象
			SoapObject soapObject = new SoapObject(targetNameSpace,method);
			//参数输入
			soapObject.addProperty("Sname", params[0]);
			Log.d("Sname", params[0]);
			soapObject.addProperty("pss", params[1]);
			Log.d("pss", params[1]);
			// 通过SOAP1.1协议得到envelop对象
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// 将soapObject对象设置为envelop对象，传出消息
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// 开始调用远程方法
			String response = "false";
			try {
				httpSE.call(targetNameSpace + method, envelop);
				// 得到服务器传回的数据
				response=envelop.getResponse().toString();
				Log.d("response_L", response);
			} catch (IOException e) {
				e.printStackTrace();
				response="neterror";
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				//return "false";
			}
			return response;
		}
	}


	class SignupNetAsyncTask extends AsyncTask<String, Object, String> {

		// WSDL文档中的命名空间
		private static final String targetNameSpace = "http://tempuri.org/";
		// WSDL文档中的URL
		private static final String WSDL = "http://370381b0.nat123.net:18506/WebService1.asmx";
		// 需要调用的方法名
		private static final String method  = "Signup";
		//进度条
		ProgressDialog pd;

		/** 
		 * 执行任务之前调用 
		 */  
		@Override  
		protected void onPreExecute() {  
			// TODO Auto-generated method stub  
			super.onPreExecute();  
			pd = new ProgressDialog(MainActivity.this);  
			pd.setMessage("正在注册...");  
			pd.setIndeterminate(false);// 在最大值最小值中移动  
			pd.setCancelable(true);// 可以取消  
			pd.show();  	        
		}  

		/**
		 * 执行任务后调用
		 */
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//进度条消失
			pd.dismiss();
			switch (result) {
			case "true":
				//记住密码
				if(login_check.isChecked()){
				}
				Toast.makeText(MainActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
				break;

			default:
				Toast.makeText(MainActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
			}
		}


		/**
		 * 执行任务
		 */
		@Override
		protected String doInBackground(String... params) {
			// 根据命名空间和方法得到SoapObject对象
			SoapObject soapObject = new SoapObject(targetNameSpace,method);
			//参数输入
			soapObject.addProperty("Sname", params[0]);
			Log.d("Sname_L", params[0]);
			soapObject.addProperty("pss", params[1]);
			Log.d("pss_L", params[1]);
			// 通过SOAP1.1协议得到envelop对象
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// 将soapObject对象设置为envelop对象，传出消息
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// 开始调用远程方法
			String response = "false";
			try {
				httpSE.call(targetNameSpace + method, envelop);
				// 得到服务器传回的数据
				response=envelop.getResponse().toString();
				Log.d("response_S", response);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
			return response;
		}
	}
}
