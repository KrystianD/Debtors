package kd.apps.Debtors;

import kd.apps.Debtors.R;
import kd.apps.Debtors.adapters.DebtorListAdapter;
import kd.apps.Debtors.db.DB;
import kd.apps.Debtors.db.Debtor;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class DebtorsListActivity extends ListActivity // implements OnClickListener
{
	public final static String DEBTOR_ID = "kd.apps.DEBTOR_ID";

	private DB db;

	private DebtorListAdapter debtorsListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		db = new DB(this);
		db.open();

		debtorsListAdapter = new DebtorListAdapter(this, R.layout.list_item_history, db);
		debtorsListAdapter.setOnlyNonbalanced(false);
		debtorsListAdapter.refresh();
		setListAdapter(debtorsListAdapter);
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		Utils.activities++;
		debtorsListAdapter.refresh();
	}
	@Override
	protected void onPause()
	{
		super.onPause();
		Utils.activities--;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		Debtor d = (Debtor)debtorsListAdapter.getItem(position);

		Intent intent = new Intent(this, DebtorHistoryActivity.class);
		intent.putExtra(DEBTOR_ID, d.getId());
		startActivity(intent);
	}
}