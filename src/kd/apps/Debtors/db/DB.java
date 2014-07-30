package kd.apps.Debtors.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import kd.apps.Debtors.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DB
{
	private SQLiteDatabase db;
	private MySQLiteHelper dbHelper;
	private static final int DBFILE_VERSION = 3;

	public DB(Context context)
	{
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException
	{
		db = dbHelper.getWritableDatabase();
	}

	public void close()
	{
		db.close();
	}

	public void use(SQLiteDatabase db)
	{
		this.db = db;
	}

	// Debtors
	public void insertDebtor(Debtor debtor)
	{
		ContentValues vals = new ContentValues();
		vals.put("name", debtor.getName());
		vals.put("flags", debtor.getFlags());
		long id = db.insert("debtors", null, vals);
		debtor.setId(id);
		Log.i("DB", "Debtor inserted - " + debtor);
	}
	public void updateDebtor(Debtor debtor)
	{
		ContentValues vals = new ContentValues();
		vals.put("name", debtor.getName());
		vals.put("flags", debtor.getFlags());
		db.update("debtors", vals, "id=?", new String[] { String.valueOf(debtor.getId()) });
		Log.i("DB", "Debtor updated - " + debtor);
	}
	public void deleteDebtorById(long debtorId)
	{
		beginTransaction();
		db.delete("debtors", "id=?", new String[] { String.valueOf(debtorId) });
		int entriesDeleted = db.delete("entries", "debtorId=?", new String[] { String.valueOf(debtorId) });
		commitTransaction();
		Log.i("DB", "Debtor of id " + debtorId + " and " + entriesDeleted + " entries deleted");
	}

	public ArrayList<Debtor> getDebtors(boolean withBalance, boolean onlyNonbalanced)
	{
		ArrayList<Debtor> debtors = new ArrayList<Debtor>();

		Cursor c = db.query("debtors", new String[]
		{
				"id", "name", "flags"
		},
				null, null, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast())
		{
			Debtor debtor = new Debtor();
			debtor.setId(c.getLong(0));
			debtor.setName(c.getString(1));
			debtor.setFlags(c.getInt(2));
			debtors.add(debtor);
			// Log.i ("KD", "Found debtor - " + debtor);
			c.moveToNext();
		}
		c.close();

		if (withBalance)
		{
			c = db.query("entries", new String[]
			{
					"debtorId", "SUM(value) AS balance"
			},
					null, null, "debtorId", null, null);

			c.moveToFirst();
			while (!c.isAfterLast())
			{
				long debtorId = c.getLong(0);
				double value = c.getDouble(1);
				for (Debtor d : debtors)
				{
					if (d.getId() == debtorId)
					{
						d.setBalance(value);
						break;
					}
				}
				c.moveToNext();
			}
			c.close();

			if (onlyNonbalanced)
			{
				ArrayList<Debtor> newDebtors = new ArrayList<Debtor>();
				for (Debtor d : debtors)
				{
					if (!d.isBalanced())
						newDebtors.add(d);
				}
				debtors = newDebtors;
			}
		}

		Collections.sort(debtors, new Comparator<Debtor>()
		{
			@Override
			public int compare(Debtor lhs, Debtor rhs)
			{
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});

		return debtors;
	}

	public Debtor getDebtorByName(String name)
	{
		Cursor c = db.query("debtors", new String[] { "id", "name", "flags" }, "name=?",
				new String[] { name },
				null, null, null);

		if (!c.moveToFirst())
			return null;

		Debtor debtor = new Debtor();
		debtor.setId(c.getLong(0));
		debtor.setName(c.getString(1));
		debtor.setFlags(c.getInt(2));
		return debtor;
	}
	public Debtor getDebtorById(long id)
	{
		Cursor c = db.query("debtors", new String[]
		{
				"id", "name", "flags"
		}, "id=?",
				new String[]
				{
				String.valueOf(id)
				},
				null, null, null);

		if (!c.moveToFirst())
			return null;

		Debtor debtor = new Debtor();
		debtor.setId(c.getLong(0));
		debtor.setName(c.getString(1));
		debtor.setFlags(c.getInt(2));
		return debtor;
	}

	// Entries
	public void insertEntry(Entry entry)
	{
		ContentValues vals = new ContentValues();
		vals.put("debtorId", entry.getDebtorId());
		vals.put("date", entry.getDate().getTime() / 1000);
		vals.put("value", entry.getValue());
		vals.put("desc", entry.getDesc());
		if (entry.getLocation() != null)
			vals.put("location", Utils.locationToString(entry.getLocation()) + "%" + (entry.getIsLastLocation() ? "1" : "0"));
		long id = db.insert("entries", null, vals);
		entry.setId(id);
		Log.i("DB", "Entry inserted - " + entry);
	}
	public void updateEntry(Entry entry)
	{
		ContentValues vals = new ContentValues();
		vals.put("debtorId", entry.getDebtorId());
		vals.put("date", entry.getDate().getTime() / 1000);
		vals.put("value", entry.getValue());
		vals.put("desc", entry.getDesc());
		if (entry.getLocation() != null)
			vals.put("location", Utils.locationToString(entry.getLocation()) + "%" + (entry.getIsLastLocation() ? "1" : "0"));
		db.update("entries", vals, "id=?", new String[] { String.valueOf(entry.getId()) });
		Log.i("DB", "Entry updated - " + entry);
	}
	public void deleteEntryById(long id)
	{
		db.delete("entries", "id=?", new String[] { String.valueOf(id) });
		Log.i("DB", "Entry with id " + id + " deleted");
	}

	private Entry entryFromCursor(Cursor c)
	{
		Entry entry = new Entry();
		entry.setId(c.getLong(0));
		entry.setDebtorId(c.getLong(1));
		entry.setDate(new Date(c.getLong(2) * 1000));
		entry.setValue(c.getDouble(3));
		entry.setDesc(c.getString(4));
		if (c.getString(5) != null)
		{
			String locStr = c.getString(5);

			String[] parts = locStr.split("%");

			if (parts.length == 2)
			{
				locStr = parts[0];
				Boolean last = parts[1].equals("1") ? true : false;
				entry.setIsLastLocation(last);
			}
			else if (parts.length == 1)
			{
				locStr = parts[0];
				entry.setIsLastLocation(false);
			}

			entry.setLocation(Utils.locationFromString(locStr));
		}
		return entry;
	}
	public ArrayList<Entry> getEntries()
	{
		ArrayList<Entry> entries = new ArrayList<Entry>();

		Cursor c = db.query("entries",
				new String[] { "id", "debtorId", "date", "value", "desc", "location" }, null, null,
				null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast())
		{
			Entry entry = entryFromCursor(c);
			c.moveToNext();
			entries.add(entry);
			// Log.i ("DB", String.valueOf (entry.getValue ()));
		}
		c.close();

		return entries;
	}
	public ArrayList<Entry> getEntriesByDebtorId(long debtorId)
	{
		ArrayList<Entry> entries = new ArrayList<Entry>();

		Cursor c = db.query("entries",
				new String[] { "id", "debtorId", "date", "value", "desc", "location" }, "debtorId=?",
				new String[] { String.valueOf(debtorId) },
				null, null, "date DESC");

		c.moveToFirst();
		while (!c.isAfterLast())
		{
			Entry entry = entryFromCursor(c);
			c.moveToNext();
			entries.add(entry);
		}
		c.close();

		return entries;
	}
	public Entry getEntryById(long entryId)
	{
		Cursor c = db.query("entries",
				new String[] { "id", "debtorId", "date", "value", "desc", "location" }, "id=?",
				new String[] { String.valueOf(entryId) },
				null, null, null);

		if (!c.moveToFirst())
			return null;

		Entry entry = entryFromCursor(c);
		return entry;
	}

	// DB
	public void revert()
	{
		dbHelper.revert(db);
	}
	public void beginTransaction()
	{
		db.beginTransaction();
	}
	public void commitTransaction()
	{
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void exportDB(OutputStream strm) throws IOException
	{
		ArrayList<Debtor> debtors = getDebtors(false, false);
		ArrayList<Entry> entries = getEntries();

		DataOutputStream data = new DataOutputStream(strm);
		data.writeByte(DBFILE_VERSION);
		data.writeInt(debtors.size());
		for (int i = 0; i < debtors.size(); i++)
		{
			Debtor debtor = debtors.get(i);
			debtor.saveToStream(data);

			int cnt = 0;
			for (int j = 0; j < entries.size(); j++)
			{
				Entry entry = entries.get(j);
				if (entry.getDebtorId() == debtor.getId())
					cnt++;
			}

			data.writeInt(cnt);

			for (int j = 0; j < entries.size(); j++)
			{
				Entry entry = entries.get(j);
				if (entry.getDebtorId() == debtor.getId())
					entry.saveToStream(data);
			}
		}
	}
	public void importDB(InputStream strm) throws IOException
	{
		revert();
		beginTransaction();

		DataInputStream data = new DataInputStream(strm);
		int ver = data.readByte();
		Log.i("KD", String.format("Importing DB of version %d", ver));
		int cnt1, cnt2;
		switch (ver)
		{
		case 1:
			cnt1 = data.readInt();
			for (int i = 0; i < cnt1; i++)
			{
				Debtor debtor = Debtor.fromStream1(data);
				insertDebtor(debtor);

				cnt2 = data.readInt();
				for (int j = 0; j < cnt2; j++)
				{
					Entry entry = Entry.fromStream1(data);
					entry.setDebtorId(debtor.getId());
					insertEntry(entry);
				}
			}
			break;
		case 2:
			cnt1 = data.readInt();
			for (int i = 0; i < cnt1; i++)
			{
				Debtor debtor = Debtor.fromStream2(data);
				insertDebtor(debtor);

				cnt2 = data.readInt();
				for (int j = 0; j < cnt2; j++)
				{
					Entry entry = Entry.fromStream2(data);
					entry.setDebtorId(debtor.getId());
					insertEntry(entry);
				}
			}
			break;
		case 3:
			cnt1 = data.readInt();
			for (int i = 0; i < cnt1; i++)
			{
				Debtor debtor = Debtor.fromStream3(data);
				insertDebtor(debtor);

				cnt2 = data.readInt();
				for (int j = 0; j < cnt2; j++)
				{
					Entry entry = Entry.fromStream3(data);
					entry.setDebtorId(debtor.getId());
					insertEntry(entry);
				}
			}
			break;
		}

		commitTransaction();
	}
}
