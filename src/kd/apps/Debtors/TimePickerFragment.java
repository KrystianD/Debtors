package kd.apps.Debtors;

import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

public class TimePickerFragment extends DialogFragment
{
	private Date date;
	private TimePickerDialog.OnTimeSetListener listener;

	public TimePickerFragment(Date date, TimePickerDialog.OnTimeSetListener listener)
	{
		this.date = date;
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final Calendar c = Calendar.getInstance();
		c.setTime(date);

		return new TimePickerDialog(getActivity(), listener, c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE), DateFormat.is24HourFormat(getActivity()));
	}
}