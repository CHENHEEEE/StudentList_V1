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

		//ʵ����
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
				//��ס����
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
				dialog.setTitle("ע��");
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
							Toast.makeText(MainActivity.this, "�������û���", Toast.LENGTH_SHORT).show();
						}
						if(!psc_L.equals(ps_L)){
							Toast.makeText(MainActivity.this, "�������벻һ��", Toast.LENGTH_SHORT).show();
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

		// WSDL�ĵ��е������ռ�
		private static final String targetNameSpace = "http://tempuri.org/";
		// WSDL�ĵ��е�URL
		private static final String WSDL = "http://370381b0.nat123.net:18506/WebService1.asmx";
		// ��Ҫ���õķ�����
		private static final String method  = "Login";
		//������
		ProgressDialog pd;

		/** 
		 * ִ������֮ǰ���� 
		 */  
		@Override  
		protected void onPreExecute() {  
			// TODO Auto-generated method stub  
			super.onPreExecute();  
			pd = new ProgressDialog(MainActivity.this);  
			pd.setMessage("������֤�����Ժ󡭡�");  
			pd.setIndeterminate(false);// �����ֵ��Сֵ���ƶ�  
			pd.setCancelable(true);// ����ȡ��  
			pd.show();  	        
		}  

		/**
		 * ִ����������
		 */
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//��������ʧ
			pd.dismiss();
			switch (result) {
			case "ok":
				//Toast.makeText(MainActivity.this, "��¼�ɹ���������ת...", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.putExtra("Sname",username);
				intent.setClass(MainActivity.this,ListActivity.class);
				startActivity(intent);
				MainActivity.this.finish();
				break;

			case "incorrect":
				editText2.setText("");
				Toast.makeText(MainActivity.this, "�������", Toast.LENGTH_SHORT).show();
				break;

			case "neterror":
				Toast.makeText(MainActivity.this, "�����쳣", Toast.LENGTH_SHORT).show();
				break;

			default:
				Toast.makeText(MainActivity.this, "���û�", Toast.LENGTH_SHORT).show();
			}
		}


		/**
		 * ִ������
		 */
		@Override
		protected String doInBackground(String... params) {
			// ���������ռ�ͷ����õ�SoapObject����
			SoapObject soapObject = new SoapObject(targetNameSpace,method);
			//��������
			soapObject.addProperty("Sname", params[0]);
			Log.d("Sname", params[0]);
			soapObject.addProperty("pss", params[1]);
			Log.d("pss", params[1]);
			// ͨ��SOAP1.1Э��õ�envelop����
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// ��soapObject��������Ϊenvelop���󣬴�����Ϣ
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// ��ʼ����Զ�̷���
			String response = "false";
			try {
				httpSE.call(targetNameSpace + method, envelop);
				// �õ����������ص�����
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

		// WSDL�ĵ��е������ռ�
		private static final String targetNameSpace = "http://tempuri.org/";
		// WSDL�ĵ��е�URL
		private static final String WSDL = "http://370381b0.nat123.net:18506/WebService1.asmx";
		// ��Ҫ���õķ�����
		private static final String method  = "Signup";
		//������
		ProgressDialog pd;

		/** 
		 * ִ������֮ǰ���� 
		 */  
		@Override  
		protected void onPreExecute() {  
			// TODO Auto-generated method stub  
			super.onPreExecute();  
			pd = new ProgressDialog(MainActivity.this);  
			pd.setMessage("����ע��...");  
			pd.setIndeterminate(false);// �����ֵ��Сֵ���ƶ�  
			pd.setCancelable(true);// ����ȡ��  
			pd.show();  	        
		}  

		/**
		 * ִ����������
		 */
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//��������ʧ
			pd.dismiss();
			switch (result) {
			case "true":
				//��ס����
				if(login_check.isChecked()){
				}
				Toast.makeText(MainActivity.this, "ע��ɹ�", Toast.LENGTH_SHORT).show();
				break;

			default:
				Toast.makeText(MainActivity.this, "ע��ʧ��", Toast.LENGTH_SHORT).show();
			}
		}


		/**
		 * ִ������
		 */
		@Override
		protected String doInBackground(String... params) {
			// ���������ռ�ͷ����õ�SoapObject����
			SoapObject soapObject = new SoapObject(targetNameSpace,method);
			//��������
			soapObject.addProperty("Sname", params[0]);
			Log.d("Sname_L", params[0]);
			soapObject.addProperty("pss", params[1]);
			Log.d("pss_L", params[1]);
			// ͨ��SOAP1.1Э��õ�envelop����
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// ��soapObject��������Ϊenvelop���󣬴�����Ϣ
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// ��ʼ����Զ�̷���
			String response = "false";
			try {
				httpSE.call(targetNameSpace + method, envelop);
				// �õ����������ص�����
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
