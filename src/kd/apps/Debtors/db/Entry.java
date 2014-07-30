package kd.apps.Debtors.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Location;

public class Entry
{
	private long id, debtorId;
	private Date date;
	private double value;
	private String desc;
	private Location location;
	private Boolean isLastLocation;

	public Entry()
	{
		date = new Date();
		value = 0;
		desc = "";
		location = null;
		isLastLocation = false;
	}

	public long getId()
	{
		return id;
	}
	public void setId(long id)
	{
		this.id = id;
	}

	public long getDebtorId()
	{
		return debtorId;
	}
	public void setDebtorId(long debtorId)
	{
		this.debtorId = debtorId;
	}

	public Date getDate()
	{
		return date;
	}
	public String getFormattedDate()
	{
		SimpleDateFormat df = new SimpleDateFormat("y MMMM dd  HH:mm");
		return df.format(getDate());
	}
	public void setDate(Date date)
	{
		this.date = date;
	}

	public double getValue()
	{
		return value;
	}
	public void setValue(double value)
	{
		this.value = value;
	}

	public String getDesc()
	{
		return desc;
	}
	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	public Location getLocation()
	{
		return location;
	}
	public void setLocation(Location location)
	{
		this.location = location;
	}
	public Boolean hasLocation()
	{
		return this.location != null;
	}

	public Boolean getIsLastLocation()
	{
		return isLastLocation;
	}
	public void setIsLastLocation(Boolean isLastLocation)
	{
		this.isLastLocation = isLastLocation;
	}

	@Override
	public String toString()
	{
		return String.format(Locale.US, "#%d debtorId: %d date: %s value: %f desc: %s", getId(), getDebtorId(), getDate().toString(),
				getValue(), getDesc());
	}

	public void saveToStream(OutputStream strm) throws IOException
	{
		DataOutputStream data = new DataOutputStream(strm);
		data.writeLong(date.getTime());
		data.writeDouble(value);
		data.writeUTF(desc);
		if (location != null)
		{
			data.writeByte(1);
			data.writeDouble(location.getLatitude());
			data.writeDouble(location.getLongitude());
			data.writeFloat(location.getAccuracy());
			data.writeBoolean(isLastLocation);
		}
		else
		{
			data.writeByte(0);
		}
		data.flush();
	}
	public static Entry fromStream1(InputStream strm) throws IOException
	{
		DataInputStream data = new DataInputStream(strm);
		Entry e = new Entry();
		int ver = data.readByte();
		switch (ver)
		{
		case 1:
			e.setDate(new Date(data.readLong()));
			e.setValue(data.readDouble());
			e.setDesc(data.readUTF());
			break;
		}
		return e;
	}
	public static Entry fromStream2(InputStream strm) throws IOException
	{
		DataInputStream data = new DataInputStream(strm);
		Entry e = new Entry();
		e.setDate(new Date(data.readLong()));
		e.setValue(data.readDouble());
		e.setDesc(data.readUTF());
		return e;
	}
	public static Entry fromStream3(InputStream strm) throws IOException
	{
		DataInputStream data = new DataInputStream(strm);
		Entry e = new Entry();
		e.setDate(new Date(data.readLong()));
		e.setValue(data.readDouble());
		e.setDesc(data.readUTF());
		int tmp = data.readByte();
		if (tmp == 1)
		{
			double lat = data.readDouble();
			double lon = data.readDouble();
			float acc = data.readFloat();
			e.setIsLastLocation(data.readBoolean());
			Location loc = new Location("network");
			loc.setLatitude(lat);
			loc.setLongitude(lon);
			loc.setAccuracy(acc);
			e.setLocation(loc);
		}
		return e;
	}
}
