package kd.apps.Debtors.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import kd.apps.Debtors.R;
import kd.apps.Debtors.Utils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper
{
	private Context ctx;
	private static final String DB_NAME = "debtors";
	private static final int DB_VERSION = 6;

	public MySQLiteHelper(Context context)
	{
		//context.getExternalFilesDir (null).getAbsolutePath () + "/" + DB_NAME
		super(context, DB_NAME, null, DB_VERSION);
		Log.d("KD", DB_NAME);
		ctx = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.beginTransaction();
		try
		{
			String sql = Utils.resReadFile(ctx, R.raw.sql);
			for (String st : sql.split(";"))
			{
				String st2 = st.trim();
				if (st2.length() == 0)
					break;
				db.execSQL(st2);
			}
			db.setTransactionSuccessful();
			Log.i("KD", "Database 'debtors' created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			db.endTransaction();
		}
	}

	public void revert(SQLiteDatabase db)
	{
		db.execSQL("DROP TABLE IF EXISTS debtors");
		db.execSQL("DROP TABLE IF EXISTS entries");
		Log.i("DB", "Database 'debtors' dropped");
		onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2)
	{
		if (arg1 == arg2)
			return;

		Log.i("KD", String.format("onUpgrade from %d to %d", arg1, arg2));

		File outputDir = ctx.getCacheDir();
		File outputFile;
		try
		{
			outputFile = File.createTempFile(String.format(Locale.US, "bck%d", arg1), "db", outputDir);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
			return;
		}

		try
		{
			Log.i("KD", String.format("Database 'debtors' dumping to %s...", outputFile.getPath()));

			DB db2 = new DB(ctx);
			db2.use(db);

			FileOutputStream os = new FileOutputStream(outputFile);
			db2.exportDB(os);
			os.close();

			Log.i("KD", String.format("Database 'debtors' dumped to %s", outputFile.getPath()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		revert(db);

		try
		{
			Log.i("KD", String.format("Database 'debtors' restoring from %s...", outputFile.getPath()));

			DB db2 = new DB(ctx);
			db2.use(db);

			FileInputStream is = new FileInputStream(outputFile);
			db2.importDB(is);
			is.close();

			Log.i("KD", String.format("Database 'debtors' restored from %s, new version: %d", outputFile.getPath(), arg2));
			outputFile.delete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
