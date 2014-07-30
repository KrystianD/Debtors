package kd.apps.Debtors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.widget.EditText;

public class Utils
{
	public static int activities = 0;

	public static String resReadFile(Context ctx, int id) throws IOException
	{
		InputStream file = ctx.getResources().openRawResource(R.raw.sql);
		BufferedReader read = new BufferedReader(new InputStreamReader(file));

		String str = "";
		String line;
		while ((line = read.readLine()) != null)
		{
			str += line + "\n";
		}

		return str;
	}

	public static double getEditTextValue(EditText edit)
	{
		String str = edit.getText().toString();
		str = str.replace(",", ".");

		double val = 0;
		try
		{
			val = Double.parseDouble(str);
		}
		catch (NumberFormatException e)
		{
			val = 0;
		}
		return val;
	}
	public static String getNumber(Context ctx, double val)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String curr = sharedPrefs.getString("pref_currency", "PLN");
		if (curr.equals("USD"))
			return String.format(Locale.US, "$%.2f", val);
		else if (curr.equals("EUR"))
			return String.format(Locale.US, "%.2f €", val);
		else if (curr.equals("PLN"))
			return String.format(Locale.US, "%.2f zł", val);
		return String.format(Locale.US, "%.2f", val);
	}

	@SuppressLint("DefaultLocale")
	public static String locationToString(Location location)
	{
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		float acc = location.getAccuracy();
		return String.format("%f,%f,%f", lat, lon, acc);
	}
	public static Location locationFromString(String str)
	{
		if (str.length() == 0)
			return null;
		String[] parts = str.split(",");
		double lat = Double.valueOf(parts[0]);
		double lon = Double.valueOf(parts[1]);
		float acc = Float.valueOf(parts[2]);
		Location loc = new Location("network");
		loc.setLatitude(lat);
		loc.setLongitude(lon);
		loc.setAccuracy(acc);
		return loc;
	}

	public static Boolean isUseLocationEnabled(Context ctx)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sharedPrefs.getBoolean("pref_useLocation", false);
	}
	public static Boolean isUseRealLocationEnabled(Context ctx)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sharedPrefs.getBoolean("pref_requireValidLocation", false);
	}
}
