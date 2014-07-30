package kd.apps.Debtors.adapters;

import java.util.ArrayList;
import java.util.List;

import kd.apps.Debtors.R;
import kd.apps.Debtors.Utils;
import kd.apps.Debtors.db.DB;
import kd.apps.Debtors.db.Entry;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DebtorEntriesListAdapter extends BaseAdapter
{
	private final Context context;
	private DB db;
	private int layoutId;
	private long debtorId;

	private List<Entry> entries;
	private ArrayList<Double> entriesBalance;

	public DebtorEntriesListAdapter(Context context, int layoutId, DB db, long debtorId)
	{
		this.context = context;
		this.db = db;
		this.layoutId = layoutId;
		this.debtorId = debtorId;
	}

	public void refresh()
	{
		entries = db.getEntriesByDebtorId(debtorId);
		entriesBalance = new ArrayList<Double>();
		double sum = 0;
		for (int i = entries.size() - 1; i >= 0; i--)
		{
			sum += entries.get(i).getValue();
			entriesBalance.add(sum);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return entries.size();
	}
	@Override
	public Object getItem(int position)
	{
		return entries.get(position);
	}
	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View rowView;
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(layoutId, parent, false);
		}
		else
		{
			rowView = convertView;
		}

		TextView lbDate = (TextView)rowView.findViewById(R.id.date);
		TextView lbValue = (TextView)rowView.findViewById(R.id.value);
		TextView lbDesc = (TextView)rowView.findViewById(R.id.desc);
		TextView lbBalance = (TextView)rowView.findViewById(R.id.balance);
		ImageView ivHasLocation = (ImageView)rowView.findViewById(R.id.hasLocation);

		Entry entry = entries.get(position);

		lbDate.setText(entry.getFormattedDate());
		lbValue.setText(Utils.getNumber(context, entry.getValue()));
		lbDesc.setText(entry.getDesc());

		if (entry.getValue() < 0)
			lbValue.setTextColor(0xffff0000);
		else
			lbValue.setTextColor(0xff00ff00);

		lbBalance.setText(Utils.getNumber(context, entriesBalance.get(getCount() - position - 1)));

		ivHasLocation.setVisibility(entry.hasLocation() ? View.VISIBLE : View.GONE);

		return rowView;
	}
}