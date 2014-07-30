package kd.apps.Debtors;

import kd.apps.Debtors.R;
import kd.apps.Debtors.db.DB;
import kd.apps.Debtors.db.Debtor;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class EditDebtorActivity extends Activity
{
	private DB db;
	private Debtor debtor;

	private EditText tbName;
	private CheckBox cbHidden;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_debtor);

		tbName = (EditText)findViewById(R.id.name);
		cbHidden = (CheckBox)findViewById(R.id.hidden);

		db = new DB(this);
		db.open();

		Intent intent = getIntent();
		long debtorId = intent.getLongExtra(DebtorsActivity.DEBTOR_ID, -1);

		debtor = db.getDebtorById(debtorId);

		tbName.setText(debtor.getName());
		cbHidden.setChecked(debtor.isHidden());
	}
	protected void onResume()
	{
		super.onResume();
		Utils.activities++;
	}
	@Override
	protected void onPause()
	{
		super.onPause();
		Utils.activities--;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		}
		return super.onOptionsItemSelected(item);
	}

	public void on_bSave_clicked(View v)
	{
		debtor.setName(tbName.getText().toString());
		debtor.setHidden(cbHidden.isChecked());

		db.updateDebtor(debtor);

		finish();
	}
	public void on_bCancel_clicked(View v)
	{
		finish();
	}
}
