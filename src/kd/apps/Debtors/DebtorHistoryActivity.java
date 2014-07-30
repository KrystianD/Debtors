package kd.apps.Debtors;

import kd.apps.Debtors.adapters.DebtorEntriesListAdapter;
import kd.apps.Debtors.db.DB;
import kd.apps.Debtors.db.Debtor;
import kd.apps.Debtors.db.Entry;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

public class DebtorHistoryActivity extends ListActivity
{
	private DB db;
	private Debtor debtor;
	private int selPosition;

	private DebtorEntriesListAdapter debtorsEntriesListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		db = new DB(this);
		db.open();

		Intent intent = getIntent();
		long debtorId = intent.getLongExtra(DebtorsActivity.DEBTOR_ID, -1);

		debtorsEntriesListAdapter = new DebtorEntriesListAdapter(this, R.layout.list_item_entry, db, debtorId);
		debtorsEntriesListAdapter.refresh();
		setListAdapter(debtorsEntriesListAdapter);

		registerForContextMenu(getListView());
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		Utils.activities++;

		Intent intent = getIntent();
		long debtorId = intent.getLongExtra(DebtorsActivity.DEBTOR_ID, -1);

		debtor = db.getDebtorById(debtorId);

		debtorsEntriesListAdapter.refresh();

		String orig = this.getResources().getText(R.string.title_activity_debtor_history).toString();
		String title = String.format(orig, debtor.getName());
		setTitle(title);
	}
	@Override
	protected void onPause()
	{
		super.onPause();
		Utils.activities--;
	}

	/*@Override
	protected void onListItemClick (ListView l, View v, int position, long id)
	{
		super.onListItemClick (l, v, position, id);
		selPosition = position;
		//openContextMenu (l);
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_history, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final DebtorHistoryActivity obj = this;
		Intent intent;

		switch (item.getItemId())
		{
		case R.id.edit:
			intent = new Intent(this, EditDebtorActivity.class);
			intent.putExtra(DebtorsActivity.DEBTOR_ID, debtor.getId());
			startActivity(intent);
			return true;
		case R.id.delete:
			String orig = this.getResources().getText(R.string.delete_debtor_confirmation).toString();
			String msg = String.format(orig, debtor.getName());

			new AlertDialog.Builder(this)
					.setTitle(R.string.debtor_deletion)
					.setMessage(msg)
					.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							db.deleteDebtorById(debtor.getId());
							obj.finish();
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
						}
					}).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		AdapterView.AdapterContextMenuInfo info;

		info = (AdapterView.AdapterContextMenuInfo)menuInfo;

		final Entry entry;
		if (menuInfo != null)
			entry = (Entry)debtorsEntriesListAdapter.getItem(info.position);
		else
			entry = (Entry)debtorsEntriesListAdapter.getItem(selPosition);

		menu.setHeaderTitle(R.string.entry);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_history_entry_ctx, menu);

		MenuItem showOnMapItem = menu.getItem(2);

		showOnMapItem.setVisible(entry.getLocation() != null || Utils.isUseLocationEnabled(this));
		showOnMapItem.setEnabled(entry.getLocation() != null);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info;
		Intent intent;

		info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		final Entry entry;
		if (info != null)
			entry = (Entry)debtorsEntriesListAdapter.getItem(info.position);
		else
			entry = (Entry)debtorsEntriesListAdapter.getItem(selPosition);

		switch (item.getItemId())
		{
		case R.id.edit:
			intent = new Intent(this, EditEntryActivity.class);
			intent.putExtra(DebtorsActivity.ENTRY_ID, entry.getId());
			startActivity(intent);
			return true;
		case R.id.delete:
			new AlertDialog.Builder(this)
					.setTitle(R.string.entry_deletion)
					.setMessage(R.string.delete_entry_confirmation)
					.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							db.deleteEntryById(entry.getId());
							debtorsEntriesListAdapter.refresh();
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
						}
					}).show();
			return true;
		case R.id.showMap:
			intent = new Intent(this, EntryMapActivity.class);
			intent.putExtra(DebtorsActivity.ENTRY_ID, entry.getId());
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
