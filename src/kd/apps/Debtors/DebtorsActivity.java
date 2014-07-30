package kd.apps.Debtors;

/*
 import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
 import group.pals.android.lib.ui.lockpattern.widget.LockPatternUtils;
 import group.pals.android.lib.ui.lockpattern.widget.LockPatternView;
 import group.pals.android.lib.ui.lockpattern.widget.LockPatternView.Cell;
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kd.apps.Debtors.adapters.DebtorListAdapter;
import kd.apps.Debtors.db.DB;
import kd.apps.Debtors.db.Debtor;
import kd.apps.Debtors.db.Entry;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//@SuppressLint("SimpleDateFormat")
public class DebtorsActivity extends Activity implements LocationListener
{
	public final static String DEBTOR_ID = "kd.apps.DEBTOR_ID";
	public final static String ENTRY_ID = "kd.apps.ENTRY_ID";

	private final int RESULT_CONTACT_PICK = 1;
	private final int RESULT_FILE_CHOOSER = 2;

	private DB db;
	//private boolean authorized;
	private LocationManager locationManager;
	private Location currentLocation;
	private Date locationTime;

	private DebtorListAdapter debtorsListAdapter;
	private ArrayAdapter<String> debtorsNamesAdapter;

	private EditText tbValue;
	private TextView tbStatus;
	private ListView lvDebtors;
	private Button bLend, bPayoff;
	private AutoCompleteTextView tbPerson;

	private String[] files;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i("KD", "onCreate");
		setContentView(R.layout.activity_main);

		db = new DB(this);
		db.open();

		//authorized = false;

		tbValue = (EditText)findViewById(R.id.valuepad_value);
		tbStatus = (TextView)findViewById(R.id.status);
		bLend = (Button)findViewById(R.id.btn_lend);
		bPayoff = (Button)findViewById(R.id.btn_payoff);
		lvDebtors = (ListView)findViewById(R.id.main_lvDebtors);
		tbPerson = (AutoCompleteTextView)findViewById(R.id.valuepad_person);

		TextWatcher tw = new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				updateStatus();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void afterTextChanged(Editable s)
			{
			}
		};
		tbPerson.addTextChangedListener(tw);
		tbValue.addTextChangedListener(tw);

		lvDebtors.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> view, View v, int position, long id)
			{
				Debtor d = (Debtor)debtorsListAdapter.getItem(position);
				tbPerson.setText(d.getName());
				tbPerson.clearFocus();
			}
		});
		lvDebtors.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> view, View v, int position,
					long id)
			{
				Debtor d = (Debtor)debtorsListAdapter.getItem(position);

				Intent intent = new Intent(view.getContext(), DebtorHistoryActivity.class);
				intent.putExtra(DEBTOR_ID, d.getId());
				startActivity(intent);
				return true;
			}
		});

		updateStatus();

		tbPerson.clearFocus();

		debtorsListAdapter = new DebtorListAdapter(this, R.layout.list_item_main, db);
		debtorsListAdapter.setOnlyNonbalanced(true);
		debtorsListAdapter.setOnlyNonHidden(true);
		lvDebtors.setAdapter(debtorsListAdapter);

		debtorsNamesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
		tbPerson.setAdapter(debtorsNamesAdapter);

		if (Utils.isUseLocationEnabled(this))
		{
			locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
			//locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER, 0, 0, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
		else
		{
			locationManager = null;
		}

		currentLocation = null;

		Timer myTimer = new Timer();
		myTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						updateStatus();
					}
				});
			}
		}, 0, 1000);

	}
	@Override
	protected void onPause()
	{
		super.onPause();
		Utils.activities--;
		Log.i("KD", "onPause " + Utils.activities);

		if (Utils.isUseLocationEnabled(this))
		{
			locationManager.removeUpdates(this);
		}
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		Utils.activities++;
		Log.i("KD", "onResume " + Utils.activities);

		/*
		 * if (!authorized) { String pattern = createPattern ("15963");
		 * 
		 * Intent intent = new Intent (this, LockPatternActivity.class);
		 * intent.putExtra (LockPatternActivity._Mode,
		 * LockPatternActivity.LPMode.ComparePattern); intent.putExtra
		 * (LockPatternActivity._Pattern, pattern); startActivityForResult (intent,
		 * 0); }
		 */

		if (Utils.isUseLocationEnabled(this))
		{
			locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
		debtorsListAdapter.refresh();
		updateDebtorsNames();
	}
	@Override
	protected void onStop()
	{
		super.onStop();
		Log.i("KD", "onStop " + Utils.activities);

		if (Utils.activities <= 0)
		{
			//authorized = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		switch (requestCode)
		{
		case 0:
			if (resultCode == RESULT_OK)
			{
				//authorized = true;
			}
			else if (resultCode == RESULT_CANCELED)
			{
			}
			break;
		case RESULT_CONTACT_PICK:
			if (resultCode == Activity.RESULT_OK)
			{
				Cursor cursor = managedQuery(intent.getData(), null, null, null, null);
				if (cursor.moveToFirst())
				{
					String name = cursor.getString(cursor
							.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					tbPerson.setText(name);
				}
			}
			break;
		case RESULT_FILE_CHOOSER:
			if (resultCode == Activity.RESULT_OK)
			{
				Toast.makeText(this, getString(R.string.imported), Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;
		switch (item.getItemId())
		{
		case R.id.settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.doexport:
			doExport();
			break;
		case R.id.doimport:
			doImport();
			break;
		case R.id.history:
			intent = new Intent(this, DebtorsListActivity.class);
			startActivity(intent);
			return true;
		case R.id.exit:
			moveTaskToBack(true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);
		final Context ctx = this;
		switch (id)
		{
		case 0:
			builder.setTitle(getString(R.string.choose_db_file));
			builder.setItems(files, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					File file = new File(Environment.getExternalStorageDirectory() + File.separator + "Debtors" + File.separator + files[which]);
					try
					{
						FileInputStream is = new FileInputStream(file);
						db.importDB(is);
						is.close();
						cleanControls();
						Toast.makeText(ctx, getString(R.string.imported), Toast.LENGTH_SHORT).show();
					}
					catch (IOException e)
					{
						e.printStackTrace();
						Toast.makeText(ctx, getString(R.string.unable_import), Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		}
		dialog = builder.show();
		return dialog;
	}

	public void on_b10p_clicked(View v)
	{
		double val = Utils.getEditTextValue(tbValue);
		val += 10;
		setValue(val);
	}
	public void on_b1p_clicked(View v)
	{
		double val = Utils.getEditTextValue(tbValue);
		val += 1;
		setValue(val);
	}
	public void on_b01p_clicked(View v)
	{
		double val = Utils.getEditTextValue(tbValue);
		val += 0.1;
		setValue(val);
	}
	public void on_b10m_clicked(View v)
	{
		double val = Utils.getEditTextValue(tbValue);
		val -= 10;
		setValue(val);
	}
	public void on_b1m_clicked(View v)
	{
		double val = Utils.getEditTextValue(tbValue);
		val -= 1;
		setValue(val);
	}
	public void on_b01m_clicked(View v)
	{
		double val = Utils.getEditTextValue(tbValue);
		val -= 0.1;
		setValue(val);
	}

	public void on_bLend_clicked(View v)
	{
		performTransactionAskDesc(true);
	}
	public void on_bPayoff_clicked(View v)
	{
		performTransactionAskDesc(false);
	}
	public void on_bSelectContact_clicked(View v)
	{
		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, RESULT_CONTACT_PICK);
	}

	public void performTransactionAskDesc(final Boolean lend)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Boolean showDescDialog = sharedPrefs.getBoolean("pref_showDescDialog", true);
		if (showDescDialog)
		{
			final EditText input = new EditText(this);

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(getString(R.string.desc));
			alert.setMessage(getString(R.string.enter_desc));
			alert.setView(input);
			alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					String desc = input.getText().toString().trim();
					performTransaction(desc, lend);
				}
			});
			final AlertDialog dialog = alert.create();
			input.setOnFocusChangeListener(new View.OnFocusChangeListener()
			{
				@Override
				public void onFocusChange(View v, boolean hasFocus)
				{
					if (hasFocus)
					{
						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					}
				}
			});
			dialog.show();
		}
		else
		{
			performTransaction("", lend);
		}
	}
	private void performTransaction(String desc, Boolean lend)
	{
		String person = tbPerson.getText().toString();
		Debtor debtor = getDebtorByName(person);
		Entry entry = new Entry();
		entry.setDebtorId(debtor.getId());
		entry.setDate(new Date());
		if (lend)
			entry.setValue(-Utils.getEditTextValue(tbValue));
		else
			entry.setValue(Utils.getEditTextValue(tbValue));
		entry.setDesc(desc);

		if (Utils.isUseLocationEnabled(this))
		{
			if (currentLocation != null)
			{
				entry.setLocation(currentLocation);
			}
			else
			{
				if (Utils.isUseRealLocationEnabled(this))
				{
					return;
				}
				else
				{
					entry.setLocation(locationManager.getLastKnownLocation("network"));
				}
			}
			entry.setIsLastLocation(!hasRealLocation());
		}

		db.insertEntry(entry);
		cleanControls();
	}

	public void doImport()
	{
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			Toast.makeText(this, getString(R.string.external_not_avail), Toast.LENGTH_SHORT).show();
			return;
		}
		File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Debtors");
		if (!dir.exists())
		{
			Toast.makeText(this, getString(R.string.no_files), Toast.LENGTH_SHORT).show();
			return;
		}

		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String filename)
			{
				File sel = new File(dir, filename);
				return filename.contains(".dat") && sel.isFile();
			}
		};
		files = dir.list(filter);
		Arrays.sort(files);
		if (files.length == 0)
		{
			Toast.makeText(this, getString(R.string.no_files), Toast.LENGTH_SHORT).show();
			return;
		}

		showDialog(0);
	}
	public void doExport()
	{
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			Toast.makeText(this, getString(R.string.external_not_avail), Toast.LENGTH_SHORT).show();
			return;
		}
		String dir = Environment.getExternalStorageDirectory() + File.separator + "Debtors";
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
		String path = dir + File.separator + df.format(new Date()) + ".dat";
		File dirFile = new File(dir);
		if (!dirFile.exists())
			dirFile.mkdir();

		File file = new File(path);
		FileOutputStream os;
		try
		{
			os = new FileOutputStream(file);
			db.exportDB(os);
			os.close();
			Toast.makeText(this, String.format(getString(R.string.exported), path), Toast.LENGTH_LONG).show();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			Toast.makeText(this, String.format(getString(R.string.unable_export), path), Toast.LENGTH_SHORT).show();
		}
		catch (IOException e)
		{
			Toast.makeText(this, String.format(getString(R.string.unable_export), path), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	private Debtor getDebtorByName(String name)
	{
		name = name.trim();
		Debtor debtor = db.getDebtorByName(name);
		if (debtor == null)
		{
			debtor = new Debtor();
			debtor.setName(name);
			db.insertDebtor(debtor);
		}
		return debtor;
	}
	private void setValue(double val)
	{
		if (val < 0)
			val = 0;
		tbValue.setText(String.format("%1$01.2f", val));
	}
	private void cleanControls()
	{
		debtorsListAdapter.refresh();
		updateDebtorsNames();
		tbPerson.setText("");
		setValue(0);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(tbPerson.getWindowToken(), 0);
	}

	/*
	 * private String createPattern (String nums) { List<LockPatternView.Cell> pat
	 * = new ArrayList<LockPatternView.Cell> (); for (int i = 0; i < nums.length
	 * (); i++) { int num = nums.charAt (i) - '0' - 1; pat.add (Cell.of (num / 3,
	 * num % 3)); }
	 * 
	 * return LockPatternUtils.patternToSha1 (pat); }
	 */

	public SpannableString formatImage(String str, int... res)
	{
		SpannableString text = new SpannableString(str);

		int resIdx = 0;
		for (int i = 0; i < text.length(); i++)
		{
			if (text.charAt(i) == '%')
			{
				ImageSpan is = new ImageSpan(this, res[resIdx++]);
				text.setSpan(is, i, i + 1, 0);
			}
		}

		return text;
	}

	private void updateDebtorsNames()
	{
		debtorsNamesAdapter.clear();
		for (Debtor d : db.getDebtors(false, false))
			debtorsNamesAdapter.add(d.getName());
		debtorsNamesAdapter.notifyDataSetChanged();
	}
	private void updateStatus()
	{
		if (canAddTransaction())
		{
			bLend.setEnabled(true);
			bPayoff.setEnabled(true);
		}
		else
		{
			bLend.setEnabled(false);
			bPayoff.setEnabled(false);
		}

		if (Utils.isUseLocationEnabled(this))
		{
			if (currentLocation != null)
			{
				Date curDate = new Date();
				long span = (curDate.getTime() - locationTime.getTime()) / 1000;

				String orig = this.getResources().getText(R.string.has_location_status).toString();
				tbStatus.setText(formatImage(String.format("%s %d", orig, span), R.drawable.maps24));
			}
			else
			{
				if (Utils.isUseRealLocationEnabled(this))
				{
					String orig = this.getResources().getText(R.string.waiting_for_location_status).toString();
					tbStatus.setText(orig);
				}
				else
				{
					String orig = this.getResources().getText(R.string.using_last_location_status).toString();
					tbStatus.setText(formatImage(orig, R.drawable.maps24));
				}
			}
			tbStatus.setVisibility(View.VISIBLE);
		}
		else
		{
			tbStatus.setVisibility(View.GONE);
		}
	}

	private Boolean hasRealLocation()
	{
		return currentLocation != null;
	}
	private Boolean canAddTransaction()
	{
		if (tbPerson.length() == 0)
			return false;
		if (Utils.getEditTextValue(tbValue) == 0)
			return false;

		if (Utils.isUseLocationEnabled(this))
		{
			if (Utils.isUseRealLocationEnabled(this))
			{
				if (!hasRealLocation())
					return false;
			}
		}

		return true;
	}

	// LocationListener
	public void onLocationChanged(Location location)
	{
		currentLocation = location;
		locationTime = new Date();
		updateStatus();

		locationManager.removeUpdates(this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
	}
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		Log.i("KD", "stat ch" + status);
	}
	public void onProviderEnabled(String provider)
	{
		/*currentLocation = null;
		Log.i ("KD", "onProviderEnabled");
		updateEnables ();*/
	}
	public void onProviderDisabled(String provider)
	{
		/*currentLocation = null;
		Log.i ("KD", "onProviderDisabled");
		updateEnables ();*/
	}
}