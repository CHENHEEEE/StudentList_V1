package he.EVteam.studentlist_v1;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ListActivity extends ActionBarActivity {
	// WSDL文档中的命名空间
	private static final String targetNameSpace = "http://tempuri.org/";
	// WSDL文档中的URL
	private static final String WSDL = "http://370381b0.nat123.net:18506/WebService1.asmx";

	// 需要调用的方法名
	private static final String getStudentList  = "getStudentList ";
	private List<Map<String,String>> listItems;
	private ListView mListView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_list);
		listItems = new ArrayList<Map<String,String>>();
		mListView = (ListView) findViewById(R.id.listView1);
		new NetAsyncTask().execute();

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String mSname = listItems.get(position).get("name");
				Log.d("StudentName", mSname);
				
				//获取当前的登录用户
				String user = getIntent().getExtras().getString("Sname");
				Log.d("user", user);
				Intent intent = new Intent();
				intent.putExtra("Sname", mSname);
				intent.putExtra("user", user);
				intent.setClass(ListActivity.this, WeekActivity.class);
				startActivity(intent);
			}
		});
	}

	class NetAsyncTask extends AsyncTask<Object, Object, String> {
        
		ProgressDialog pd;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(ListActivity.this);  
	        pd.setMessage("载入中…");  
	        pd.setIndeterminate(false);// 在最大值最小值中移动  
	        pd.setCancelable(true);// 可以取消  
	        pd.show();
		}

		@Override
		protected void onPostExecute(String result) {
			pd.dismiss();
			if (result.equals("success")) {
				//列表适配器
				SimpleAdapter simpleAdapter = new SimpleAdapter(ListActivity.this, listItems, R.layout.name_item_new, 
						new String[] {"name"}, new int[]{R.id.name});
				mListView.setAdapter(simpleAdapter);
			}else {
				Toast.makeText(ListActivity.this, "载入失败", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}

		@Override
		protected String doInBackground(Object... params) {
			// 根据命名空间和方法得到SoapObject对象
			SoapObject soapObject = new SoapObject(targetNameSpace,
					getStudentList);
			// 通过SOAP1.1协议得到envelop对象
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// 将soapObject对象设置为envelop对象，传出消息

			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			// 或者envelop.bodyOut = soapObject;
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// 开始调用远程方法
			try {
				httpSE.call(targetNameSpace + getStudentList, envelop);
				// 得到远程方法返回的SOAP对象
				SoapObject resultObj = (SoapObject) envelop.getResponse();
				// 得到服务器传回的数据
				int count = resultObj.getPropertyCount();
				for (int i = 0; i < count; i++) {
					Map<String,String> listItem = new HashMap<String, String>();
					listItem.put("name", resultObj.getProperty(i).toString());
					listItems.add(listItem);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return "IOException";
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				return "XmlPullParserException";
			}
			return "success";
		}
	}
}