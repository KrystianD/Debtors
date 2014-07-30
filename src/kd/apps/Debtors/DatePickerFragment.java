package kd.apps.Debtors;

import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DatePickerFragment extends DialogFragment
{
	private Date date;
	private DatePickerDialog.OnDateSetListener listener;

	@SuppressLint("ValidFragment")
	public DatePickerFragment(Date date, DatePickerDialog.OnDateSetListener listener)
	{
		this.date = date;
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final Calendar c = Calendar.getInstance();
		c.setTime(date);
		return new DatePickerDialog(getActivity(), listener,
				c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
	}
}