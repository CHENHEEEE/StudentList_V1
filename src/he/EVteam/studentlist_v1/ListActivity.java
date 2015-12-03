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
	// WSDL�ĵ��е������ռ�
	private static final String targetNameSpace = "http://tempuri.org/";
	// WSDL�ĵ��е�URL
	private static final String WSDL = "http://370381b0.nat123.net:18506/WebService1.asmx";

	// ��Ҫ���õķ�����
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
				
				//��ȡ��ǰ�ĵ�¼�û�
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
	        pd.setMessage("�����С�");  
	        pd.setIndeterminate(false);// �����ֵ��Сֵ���ƶ�  
	        pd.setCancelable(true);// ����ȡ��  
	        pd.show();
		}

		@Override
		protected void onPostExecute(String result) {
			pd.dismiss();
			if (result.equals("success")) {
				//�б�������
				SimpleAdapter simpleAdapter = new SimpleAdapter(ListActivity.this, listItems, R.layout.name_item_new, 
						new String[] {"name"}, new int[]{R.id.name});
				mListView.setAdapter(simpleAdapter);
			}else {
				Toast.makeText(ListActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}

		@Override
		protected String doInBackground(Object... params) {
			// ���������ռ�ͷ����õ�SoapObject����
			SoapObject soapObject = new SoapObject(targetNameSpace,
					getStudentList);
			// ͨ��SOAP1.1Э��õ�envelop����
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// ��soapObject��������Ϊenvelop���󣬴�����Ϣ

			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			// ����envelop.bodyOut = soapObject;
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// ��ʼ����Զ�̷���
			try {
				httpSE.call(targetNameSpace + getStudentList, envelop);
				// �õ�Զ�̷������ص�SOAP����
				SoapObject resultObj = (SoapObject) envelop.getResponse();
				// �õ����������ص�����
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