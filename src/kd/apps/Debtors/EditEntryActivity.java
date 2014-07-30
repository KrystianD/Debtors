package kd.apps.Debtors;

import java.util.Calendar;

import kd.apps.Debtors.db.DB;
import kd.apps.Debtors.db.Debtor;
import kd.apps.Debtors.db.Entry;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

public class EditEntryActivity extends FragmentActivity
{
	private DB db;
	private Debtor debtor;
	private Entry entry;

	private TextView lbDebtor, lbDate;
	private EditText tbValue, tbDesc;
	private RadioButton rbLoan, rbPayoff;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_entry);

		db = new DB(this);
		db.open();

		Intent intent = getIntent();
		long entryId = intent.getLongExtra(DebtorsActivity.ENTRY_ID, -1);

		entry = db.getEntryById(entryId);
		debtor = db.getDebtorById(entry.getDebtorId());

		lbDebtor = (TextView)findViewById(R.id.debtor);
		lbDate = (TextView)findViewById(R.id.date);
		tbValue = (EditText)findViewById(R.id.value);
		tbDesc = (EditText)findViewById(R.id.desc);
		rbLoan = (RadioButton)findViewById(R.id.option_loan);
		rbPayoff = (RadioButton)findViewById(R.id.option_payoff);
	}
	protected void onResume()
	{
		super.onResume();
		Utils.activities++;

		reload();
	}
	@Override
	protected void onPause()
	{
		super.onPause();
		Utils.activities--;
	}

	private void reload()
	{
		lbDebtor.setText(debtor.getName());
		tbDesc.setText(entry.getDesc());
		lbDate.setText(entry.getFormattedDate());
		tbValue.setText(String.format("%.2f", Math.abs(entry.getValue())));

		if (entry.getValue() < 0)
			rbLoan.setChecked(true);
		else
			rbPayoff.setChecked(true);
	}

	public void on_bSetDate_clicked(View v)
	{
		DatePickerDialog.OnDateSetListener listener = new OnDateSetListener()
		{
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			{
				final Calendar c = Calendar.getInstance();
				c.setTime(entry.getDate());
				c.set(year, monthOfYear, dayOfMonth);
				entry.setDate(c.getTime());
				reload();
			}
		};

		DialogFragment newFragment = new DatePickerFragment(entry.getDate(), listener);
		FragmentManager fm = getSupportFragmentManager();
		newFragment.show(fm, "datePicker");
	}
	public void on_bSetTime_clicked(View v)
	{
		TimePickerDialog.OnTimeSetListener listener = new OnTimeSetListener()
		{
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute)
			{
				final Calendar c = Calendar.getInstance();
				c.setTime(entry.getDate());
				c.set(Calendar.HOUR_OF_DAY, hourOfDay);
				c.set(Calendar.MINUTE, minute);
				entry.setDate(c.getTime());
				reload();
			}
		};

		DialogFragment newFragment = new TimePickerFragment(entry.getDate(), listener);
		FragmentManager fm = getSupportFragmentManager();
		newFragment.show(fm, "timePicker");
	}
	public void on_bSave_clicked(View v)
	{
		double val = Utils.getEditTextValue(tbValue);

		entry.setDesc(tbDesc.getText().toString());

		if (rbLoan.isChecked())
			entry.setValue(-val);
		else
			entry.setValue(val);

		db.updateEntry(entry);

		finish();
	}
	public void on_bCancel_clicked(View v)
	{
		finish();
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener =
			new DatePickerDialog.OnDateSetListener()
			{
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth)
				{

				}
			};

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
		case 0:
			DatePickerDialog dtp = new DatePickerDialog(this, mDateSetListener, 2012, 01, 01);
			return dtp;
		}
		return null;
	}
}
