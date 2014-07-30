package kd.apps.Debtors.db;

//import android.annotation.SuppressLint;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class Debtor
{
	private long id;
	private String name;
	private int flags;

	// not permanent
	private double balance;

	public long getId()
	{
		return id;
	}
	public void setId(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public int getFlags()
	{
		return flags;
	}
	public void setFlags(int flags)
	{
		this.flags = flags;
	}

	public boolean isHidden()
	{
		return (flags & 0x00000001) != 0;
	}
	public void setHidden(boolean val)
	{
		flags = flags & ~0x00000001 | (val ? 0x00000001 : 0);
	}

	public double getBalance()
	{
		return balance;
	}
	public void setBalance(double balance)
	{
		this.balance = balance;
	}

	public boolean isBalanced()
	{
		return balance > -0.0001 && balance < 0.0001;
	}
	public boolean haveToPayoff()
	{
		return balance < -0.0001;
	}
	public boolean haveToBePaidoff()
	{
		return balance > 0.0001;
	}

	//@SuppressLint("DefaultLocale")
	@Override
	public String toString()
	{
		return String.format(Locale.US, "#%d name: %s balance: %f", getId(), getName(), getBalance());
	}

	public void saveToStream(OutputStream strm) throws IOException
	{
		DataOutputStream data = new DataOutputStream(strm);
		data.writeUTF(name);
		data.writeInt(flags);
		data.flush();
	}
	public static Debtor fromStream1(InputStream strm) throws IOException
	{
		DataInputStream data = new DataInputStream(strm);
		Debtor d = new Debtor();
		int ver = data.readByte();
		switch (ver)
		{
		case 1:
			d.setName(data.readUTF());
			d.setFlags(0);
			break;
		case 2:
			d.setName(data.readUTF());
			d.setFlags(data.readInt());
			break;
		}
		return d;
	}
	public static Debtor fromStream2(InputStream strm) throws IOException
	{
		DataInputStream data = new DataInputStream(strm);
		Debtor d = new Debtor();
		d.setName(data.readUTF());
		d.setFlags(data.readInt());
		return d;
	}
	public static Debtor fromStream3(InputStream strm) throws IOException
	{
		return fromStream2(strm);
	}
}
