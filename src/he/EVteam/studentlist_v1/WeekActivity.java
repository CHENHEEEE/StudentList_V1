package he.EVteam.studentlist_v1;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.R.string;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class WeekActivity extends Activity {

	// WSDL�ĵ��е������ռ�
	private static final String targetNameSpace = "http://tempuri.org/";
	// WSDL�ĵ��е�URL
	private static final String WSDL = "http://370381b0.nat123.net:18506/WebService1.asmx";

	// ��Ҫ���õķ�����
	private static final String selectStudentInfo="selectStudentInfo";
	private static final String updateStudentInfo="updateStudentInfo";
	private static final String clearInfo="clear";
	private List<Map<String,String>> listItems,listItems1,listItemsD;
	private ListView mListView,mListView1,mlistviewD;
	private TextView mTextView;
	private List<ListView> viewlist = new ArrayList<ListView>();
	private String user,sname;
	private ProgressDialog pd;
	private boolean pdflag=false;
	private String week[] = {
			"һ.a","һ.p",
			"��.a","��.p",
			"��.a","��.p",
			"��.a","��.p",
			"��.a","��.p",
			"��.a","��.p",
			"��.a","��.p",
	};
	private String week_f[] = {
			"��һ.����","��һ.����",
			"�ܶ�.����","�ܶ�.����",
			"����.����","����.����",
			"����.����","����.����",
			"����.����","����.����",
			"����.����","����.����",
			"����.����","����.����",
	};
	
	final List<Integer> fauCodeList = new ArrayList<Integer>(){{
		add(0);
		add(1);
		add(4);
		add(5);
		add(8);
		add(9);
		add(12);
		add(13);
	}};
	
	String arrangeList[] = new String[14];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.arrangement);
		
		//���б�
		mlistviewD = (ListView)findViewById(R.id.listView3);
		listItemsD = new ArrayList<Map<String,String>>();
		listItemsD.clear();
		for (int i = 0; i < 14; i++) {
			Map<String,String> listItem = new HashMap<String, String>();
			listItem.put("week", "  "+week[i]);
			listItem.put("ID", String.valueOf(i+1));
			listItemsD.add(listItem);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(WeekActivity.this, listItemsD, R.layout.name_item, 
				new String[] {"week"}, new int[]{R.id.name})
		{  
			@Override  
			public View getView(int position, View convertView, ViewGroup parent) {  
				View view = super.getView(position, convertView, parent);  
				//      view.setBackgroundResource(colors[position % 10]);
				if (fauCodeList.contains(position)==true )  
				{  
					//���ע�͵���䣬����������cell������215228241  
					//���������ֻ������  
					view.setBackgroundColor(Color.WHITE);  
				}  
				else  
				{  
					//          view.setBackgroundResource(215228241);  
					view.setBackgroundColor(Color.parseColor("#D7E4F1"));  
				}  
				return view;  
			}  
		};
		mlistviewD.setAdapter(simpleAdapter);
		
		//ʱ�䰲���б�
		listItems = new ArrayList<Map<String,String>>();
		mListView = (ListView) findViewById(R.id.listView1);
		//����ʱ���б�
		listItems1 = new ArrayList<Map<String,String>>();
		mListView1 = (ListView) findViewById(R.id.listView2);
		
		user = getIntent().getExtras().getString("user");
		sname = getIntent().getExtras().getString("Sname");
		user=user.trim();
		sname=sname.trim();
		
		new NetAsyncTask().execute();
		Button btnclear=(Button)findViewById(R.id.button1);
		Button btnrefresh=(Button)findViewById(R.id.button2);
		if(!user.equals(sname)){
			btnclear.setText(sname);
		}
		//ͬ������
		viewlist.add(mListView);
		viewlist.add(mListView1);
		viewlist.add(mlistviewD);
		MyScrollListener mlistener = new MyScrollListener();
		for(ListView item : viewlist){
			item.setOnScrollListener(mlistener);
		}
		
		
		//��ť����
		btnclear.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(WeekActivity.this, "�����������", Toast.LENGTH_SHORT);
				new NetAsyncTaskC().execute();
				pdshow();
			}
		});
		btnrefresh.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				new NetAsyncTask().execute();
				pdshow();
			}
		});
		
		//�б����¼�����
		mListView.setOnItemClickListener(new ItemListener());
		
		mListView1.setOnItemClickListener(new ItemListener());

		mlistviewD.setOnItemClickListener(new ItemListener());


	}

	//�б����
	class ItemListener implements AdapterView.OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			int day = Integer.parseInt(listItems.get(position).get("ID"));
			final String ID = "a"+listItems.get(position).get("ID");
			Log.d("ID", ID);

			if(user.equals(sname)){
				final Dialog dialog = new Dialog(WeekActivity.this);
				dialog.setContentView(R.layout.dialog_update);
				mTextView = (TextView)dialog.findViewById(R.id.arrangetext);
				dialog.setTitle(week_f[day-1]);
				mTextView.setText(arrangeList[day-1]);
				Window dialogWindow = dialog.getWindow();
				WindowManager.LayoutParams lp = dialogWindow.getAttributes();
				dialogWindow.setGravity(Gravity.CENTER);
				dialogWindow.setAttributes(lp);

				Button btnConfirm = (Button) dialog.findViewById(R.id.button1);
				Button btnCancel = (Button) dialog.findViewById(R.id.button2);

				btnConfirm.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//�ճ̸���
						EditText cNoEditText = (EditText) dialog.findViewById(R.id.editText1);
						final String text=cNoEditText.getText().toString();
						Log.d("text",text);
						dialog.dismiss();

						//ʱ����Ϣ
						SimpleDateFormat    formatter    =   new    SimpleDateFormat    ("MM��dd�� HH:mm     ");       
						Date    curDate    =   new    Date(System.currentTimeMillis());//��ȡ��ǰʱ��       
						String    time    =    formatter.format(curDate);

						new NetAsyncTask1().execute(ID,text,time);

						Toast.makeText(WeekActivity.this, "������", Toast.LENGTH_SHORT).show();
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
			else{
				final Dialog dialog1 = new Dialog(WeekActivity.this);
				dialog1.setContentView(R.layout.dialog_view);
				mTextView = (TextView)dialog1.findViewById(R.id.arrangetext);
				dialog1.setTitle(week_f[day-1]);
				mTextView.setText(arrangeList[day-1]);
				Window dialogWindow = dialog1.getWindow();
				WindowManager.LayoutParams lp = dialogWindow.getAttributes();
				dialogWindow.setGravity(Gravity.CENTER);
				dialogWindow.setAttributes(lp);

				Button btnCancel = (Button) dialog1.findViewById(R.id.button2);



				btnCancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog1.dismiss();
					}
				});
				dialog1.show();
			}
		}
	}
			
	//ListViewͬ������
	class MyScrollListener implements OnScrollListener {  

		@Override  
		public void onScrollStateChanged(AbsListView view, int scrollState) {  
			// �ؼ�����  
			if (scrollState == SCROLL_STATE_IDLE  
					|| scrollState == SCROLL_STATE_TOUCH_SCROLL) {  
				View subView = view.getChildAt(0);  
				if (subView != null) {  
					final int top = subView.getTop();  
					final int position = view.getFirstVisiblePosition();  
					for (ListView item : viewlist) {  
						item.setSelectionFromTop(position, top);  
					}  
				}  
			}  
		}  

		@Override  
		public void onScroll(AbsListView view, int firstVisibleItem,  
				int visibleItemCount, int totalItemCount) {  
			// �ؼ�����  
			View subView = view.getChildAt(0);  
			if (subView != null) {  
				final int top = subView.getTop();  
				for (ListView item : viewlist) {  
					item.setSelectionFromTop(firstVisibleItem, top);  
				}  
			}  
		}  
	}  

	//�����߳�
	class NetAsyncTask extends AsyncTask<Object, Object, String> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pdshow();
		}

		@Override
		protected void onPostExecute(String result) {
			pd.dismiss();
			pdflag=false;
			if (result.equals("success")) {
				//�б�������
				SimpleAdapter simpleAdapter = new SimpleAdapter(WeekActivity.this, listItems, R.layout.name_item, 
						new String[] {"arrangement"}, new int[]{R.id.name})
				{  
					@Override  
					public View getView(int position, View convertView, ViewGroup parent) {  
						View view = super.getView(position, convertView, parent);  
						//      view.setBackgroundResource(colors[position % 10]);
						if (fauCodeList.contains(position)==true )  
						{  
							//���ע�͵���䣬����������cell������
							//���������ֻ������  
							view.setBackgroundColor(Color.WHITE);  
						}  
						else  
						{  
							
							view.setBackgroundColor(Color.parseColor("#D7E4F1"));  
						}  
						return view;  
					}  
				};
				mListView.setAdapter(simpleAdapter);
				SimpleAdapter simpleAdapter1 = new SimpleAdapter(WeekActivity.this, listItems1, R.layout.name_item, 
						new String[] {"remark"}, new int[]{R.id.name})
				{  
					@Override  
					public View getView(int position, View convertView, ViewGroup parent) {  
						View view = super.getView(position, convertView, parent);  
						//      view.setBackgroundResource(colors[position % 10]);  
						view.setBackgroundColor(Color.parseColor("#D7E4F1")); 
						if (fauCodeList.contains(position)==true )  
						{  
							//���ע�͵���䣬����������cell������
							//���������ֻ������  
							view.setBackgroundColor(Color.WHITE);  
						}  
						else  
						{   
							view.setBackgroundColor(Color.parseColor("#D7E4F1"));  
						}  
						return view;  
					}  
				};
				mListView1.setAdapter(simpleAdapter1);
			}else{
				Toast.makeText(WeekActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}

		@Override
		protected String doInBackground(Object... params) {
			//��ȡʱ�䰲����Ϣ
			// ���������ռ�ͷ����õ�SoapObject����
			SoapObject soapObject = new SoapObject(targetNameSpace,selectStudentInfo);
			//��������
			String name = getIntent().getExtras().getString("Sname");
			Log.d("Sname", name);
			soapObject.addProperty("Sname", name);
			// ͨ��SOAP1.1Э��õ�envelop����
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// ��soapObject��������Ϊenvelop���󣬴�����Ϣ
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// ��ʼ����Զ�̷���
			try {
				httpSE.call(targetNameSpace + selectStudentInfo, envelop);
				// �õ�Զ�̷������ص�SOAP����
				SoapObject resultObj = (SoapObject) envelop.getResponse();
				// �õ����������ص�����
				int count = resultObj.getPropertyCount();
				listItems.clear();
				for (int i = 0; i < count; i++) {
					Map<String,String> listItem = new HashMap<String, String>();
					listItem.put("arrangement",resultObj.getProperty(i).toString());
					arrangeList[i]="    "+resultObj.getProperty(i).toString();
					listItem.put("ID", String.valueOf(i+1));
					listItems.add(listItem);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return "IOException";
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				return "XmlPullParserException";
			}

			//��ȡ��ע��Ϣ
			// ���������ռ�ͷ����õ�SoapObject����
			SoapObject soapObject1 = new SoapObject(targetNameSpace,selectStudentInfo);
			//��������
			String name1 = "1"+getIntent().getExtras().getString("Sname");
			Log.d("Sname1", name1);
			soapObject1.addProperty("Sname", name1);
			// ͨ��SOAP1.1Э��õ�envelop����
			SoapSerializationEnvelope envelop1 = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// ��soapObject��������Ϊenvelop���󣬴�����Ϣ
			envelop1.dotNet = true;
			envelop1.setOutputSoapObject(soapObject1);
			HttpTransportSE httpSE1 = new HttpTransportSE(WSDL);
			// ��ʼ����Զ�̷���
			try {
				httpSE1.call(targetNameSpace + selectStudentInfo, envelop1);
				// �õ�Զ�̷������ص�SOAP����
				SoapObject resultObj1 = (SoapObject) envelop1.getResponse();
				// �õ����������ص�����
				int count = resultObj1.getPropertyCount();
				listItems1.clear();
				for (int i = 0; i < count; i++) {
					Map<String,String> listItem1 = new HashMap<String, String>();
					listItem1.put("remark", " "+resultObj1.getProperty(i).toString());
					listItem1.put("ID", String.valueOf(i+1));
					listItems1.add(listItem1);
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

	//�����߳�
	class NetAsyncTask1 extends AsyncTask<String,Void,Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				new NetAsyncTask().execute();
				Toast.makeText(WeekActivity.this, "���³ɹ�", Toast.LENGTH_SHORT).show();
			}
			else Toast.makeText(WeekActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// ���������ռ�ͷ����õ�SoapObject����
			SoapObject soapObject = new SoapObject(targetNameSpace,updateStudentInfo);
			//��������
			String name = getIntent().getExtras().getString("Sname");
			Log.d("Sname", name);
			soapObject.addProperty("Sname", name);	
			soapObject.addProperty("when", params[0]);
			Log.d("when", params[0]);
			soapObject.addProperty("what", params[1]);
			Log.d("what", params[1]);
			soapObject.addProperty("time", params[2]);
			Log.d("time", params[2]);
			// ͨ��SOAP1.1Э��õ�envelop����
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// ��soapObject��������Ϊenvelop���󣬴�����Ϣ
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// ��ʼ����Զ�̷���
			try {
				httpSE.call(targetNameSpace + updateStudentInfo, envelop);
				// �õ����������ص�����
				String response=envelop.getResponse().toString();
				if(response.equals("true")){
					return true;
				}
				else return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	//�����߳�
	class NetAsyncTaskC extends AsyncTask<Object,Void,Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				new NetAsyncTask().execute();
				Toast.makeText(WeekActivity.this, "����ɹ�", Toast.LENGTH_SHORT).show();
			}
			else Toast.makeText(WeekActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			// ���������ռ�ͷ����õ�SoapObject����
			SoapObject soapObject = new SoapObject(targetNameSpace,clearInfo);
			//��������
			String name = getIntent().getExtras().getString("Sname");
			Log.d("SnameC", name);
			soapObject.addProperty("Sname", name);	
			// ͨ��SOAP1.1Э��õ�envelop����
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// ��soapObject��������Ϊenvelop���󣬴�����Ϣ
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// ��ʼ����Զ�̷���
			try {
				httpSE.call(targetNameSpace + clearInfo, envelop);
				// �õ�Զ�̷������ص�SOAP����
				String response=envelop.getResponse().toString();
				if(response.equals("true")){
					return true;
				}
				else return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	private void pdshow(){
		if(pdflag==false){
			pd = new ProgressDialog(WeekActivity.this);  
	        pd.setMessage("�����С�");  
	        pd.setIndeterminate(false);// �����ֵ��Сֵ���ƶ�  
	        pd.setCancelable(true);// ����ȡ��  
	        pd.show();
	        pdflag=true;
		}
	}
}
