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

	// WSDL文档中的命名空间
	private static final String targetNameSpace = "http://tempuri.org/";
	// WSDL文档中的URL
	private static final String WSDL = "http://370381b0.nat123.net:18506/WebService1.asmx";

	// 需要调用的方法名
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
			"一.a","一.p",
			"二.a","二.p",
			"三.a","三.p",
			"四.a","四.p",
			"五.a","五.p",
			"六.a","六.p",
			"日.a","日.p",
	};
	private String week_f[] = {
			"周一.上午","周一.下午",
			"周二.上午","周二.下午",
			"周三.上午","周三.下午",
			"周四.上午","周四.下午",
			"周五.上午","周五.下午",
			"周六.上午","周六.下午",
			"周日.上午","周日.下午",
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
		
		//周列表
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
					//如果注释掉这句，滑动后，所有cell都会变成215228241  
					//可能是有种缓存机制  
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
		
		//时间安排列表
		listItems = new ArrayList<Map<String,String>>();
		mListView = (ListView) findViewById(R.id.listView1);
		//更新时间列表
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
		//同步滑动
		viewlist.add(mListView);
		viewlist.add(mListView1);
		viewlist.add(mlistviewD);
		MyScrollListener mlistener = new MyScrollListener();
		for(ListView item : viewlist){
			item.setOnScrollListener(mlistener);
		}
		
		
		//按钮监听
		btnclear.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(WeekActivity.this, "正在清除数据", Toast.LENGTH_SHORT);
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
		
		//列表单击事件监听
		mListView.setOnItemClickListener(new ItemListener());
		
		mListView1.setOnItemClickListener(new ItemListener());

		mlistviewD.setOnItemClickListener(new ItemListener());


	}

	//列表监听
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
						//日程更新
						EditText cNoEditText = (EditText) dialog.findViewById(R.id.editText1);
						final String text=cNoEditText.getText().toString();
						Log.d("text",text);
						dialog.dismiss();

						//时间信息
						SimpleDateFormat    formatter    =   new    SimpleDateFormat    ("MM月dd日 HH:mm     ");       
						Date    curDate    =   new    Date(System.currentTimeMillis());//获取当前时间       
						String    time    =    formatter.format(curDate);

						new NetAsyncTask1().execute(ID,text,time);

						Toast.makeText(WeekActivity.this, "更新中", Toast.LENGTH_SHORT).show();
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
			
	//ListView同步滚动
	class MyScrollListener implements OnScrollListener {  

		@Override  
		public void onScrollStateChanged(AbsListView view, int scrollState) {  
			// 关键代码  
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
			// 关键代码  
			View subView = view.getChildAt(0);  
			if (subView != null) {  
				final int top = subView.getTop();  
				for (ListView item : viewlist) {  
					item.setSelectionFromTop(firstVisibleItem, top);  
				}  
			}  
		}  
	}  

	//载入线程
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
				//列表适配器
				SimpleAdapter simpleAdapter = new SimpleAdapter(WeekActivity.this, listItems, R.layout.name_item, 
						new String[] {"arrangement"}, new int[]{R.id.name})
				{  
					@Override  
					public View getView(int position, View convertView, ViewGroup parent) {  
						View view = super.getView(position, convertView, parent);  
						//      view.setBackgroundResource(colors[position % 10]);
						if (fauCodeList.contains(position)==true )  
						{  
							//如果注释掉这句，滑动后，所有cell都会变成
							//可能是有种缓存机制  
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
							//如果注释掉这句，滑动后，所有cell都会变成
							//可能是有种缓存机制  
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
				Toast.makeText(WeekActivity.this, "载入失败", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}

		@Override
		protected String doInBackground(Object... params) {
			//获取时间安排信息
			// 根据命名空间和方法得到SoapObject对象
			SoapObject soapObject = new SoapObject(targetNameSpace,selectStudentInfo);
			//参数输入
			String name = getIntent().getExtras().getString("Sname");
			Log.d("Sname", name);
			soapObject.addProperty("Sname", name);
			// 通过SOAP1.1协议得到envelop对象
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// 将soapObject对象设置为envelop对象，传出消息
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// 开始调用远程方法
			try {
				httpSE.call(targetNameSpace + selectStudentInfo, envelop);
				// 得到远程方法返回的SOAP对象
				SoapObject resultObj = (SoapObject) envelop.getResponse();
				// 得到服务器传回的数据
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

			//获取备注信息
			// 根据命名空间和方法得到SoapObject对象
			SoapObject soapObject1 = new SoapObject(targetNameSpace,selectStudentInfo);
			//参数输入
			String name1 = "1"+getIntent().getExtras().getString("Sname");
			Log.d("Sname1", name1);
			soapObject1.addProperty("Sname", name1);
			// 通过SOAP1.1协议得到envelop对象
			SoapSerializationEnvelope envelop1 = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// 将soapObject对象设置为envelop对象，传出消息
			envelop1.dotNet = true;
			envelop1.setOutputSoapObject(soapObject1);
			HttpTransportSE httpSE1 = new HttpTransportSE(WSDL);
			// 开始调用远程方法
			try {
				httpSE1.call(targetNameSpace + selectStudentInfo, envelop1);
				// 得到远程方法返回的SOAP对象
				SoapObject resultObj1 = (SoapObject) envelop1.getResponse();
				// 得到服务器传回的数据
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

	//更新线程
	class NetAsyncTask1 extends AsyncTask<String,Void,Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				new NetAsyncTask().execute();
				Toast.makeText(WeekActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
			}
			else Toast.makeText(WeekActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// 根据命名空间和方法得到SoapObject对象
			SoapObject soapObject = new SoapObject(targetNameSpace,updateStudentInfo);
			//参数输入
			String name = getIntent().getExtras().getString("Sname");
			Log.d("Sname", name);
			soapObject.addProperty("Sname", name);	
			soapObject.addProperty("when", params[0]);
			Log.d("when", params[0]);
			soapObject.addProperty("what", params[1]);
			Log.d("what", params[1]);
			soapObject.addProperty("time", params[2]);
			Log.d("time", params[2]);
			// 通过SOAP1.1协议得到envelop对象
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// 将soapObject对象设置为envelop对象，传出消息
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// 开始调用远程方法
			try {
				httpSE.call(targetNameSpace + updateStudentInfo, envelop);
				// 得到服务器传回的数据
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

	//清理线程
	class NetAsyncTaskC extends AsyncTask<Object,Void,Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				new NetAsyncTask().execute();
				Toast.makeText(WeekActivity.this, "清理成功", Toast.LENGTH_SHORT).show();
			}
			else Toast.makeText(WeekActivity.this, "清理失败", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			// 根据命名空间和方法得到SoapObject对象
			SoapObject soapObject = new SoapObject(targetNameSpace,clearInfo);
			//参数输入
			String name = getIntent().getExtras().getString("Sname");
			Log.d("SnameC", name);
			soapObject.addProperty("Sname", name);	
			// 通过SOAP1.1协议得到envelop对象
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			// 将soapObject对象设置为envelop对象，传出消息
			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			// 开始调用远程方法
			try {
				httpSE.call(targetNameSpace + clearInfo, envelop);
				// 得到远程方法返回的SOAP对象
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
	        pd.setMessage("载入中…");  
	        pd.setIndeterminate(false);// 在最大值最小值中移动  
	        pd.setCancelable(true);// 可以取消  
	        pd.show();
	        pdflag=true;
		}
	}
}
