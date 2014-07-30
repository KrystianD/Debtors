package kd.apps.Debtors.adapters;

import java.util.ArrayList;

import kd.apps.Debtors.R;
import kd.apps.Debtors.Utils;
import kd.apps.Debtors.db.DB;
import kd.apps.Debtors.db.Debtor;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DebtorListAdapter extends BaseAdapter
{
	private final Context context;
	private DB db;
	private int layoutId;

	private ArrayList<Debtor> debtors;

	// options
	private boolean onlyNonbalanced = false;
	private boolean onlyNonHidden = false;

	public DebtorListAdapter(Context context, int layoutId, DB db)
	{
		this.context = context;
		this.db = db;
		this.layoutId = layoutId;
		debtors = new ArrayList<Debtor>();
	}

	public boolean isOnlyNonbalanced()
	{
		return onlyNonbalanced;
	}
	public void setOnlyNonbalanced(boolean onlyNonbalanced)
	{
		this.onlyNonbalanced = onlyNonbalanced;
	}

	public boolean isOnlyNonHidden()
	{
		return onlyNonHidden;
	}
	public void setOnlyNonHidden(boolean onlyNonHidden)
	{
		this.onlyNonHidden = onlyNonHidden;
	}

	public void refresh()
	{
		debtors = db.getDebtors(true, onlyNonbalanced);
		if (isOnlyNonHidden())
		{
			ArrayList<Debtor> debtors2 = new ArrayList<Debtor>();
			for (Debtor d : debtors)
			{
				if (!d.isHidden())
					debtors2.add(d);
			}
			debtors = debtors2;
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return debtors.size();
	}
	@Override
	public Object getItem(int position)
	{
		return debtors.get(position);
	}
	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(layoutId, parent, false);

		TextView lbName = (TextView)rowView.findViewById(R.id.name);
		TextView lbBalance = (TextView)rowView.findViewById(R.id.balance);
		ImageView imageView = (ImageView)rowView.findViewById(R.id.logo);

		Debtor debtor = debtors.get(position);

		lbName.setText(debtor.getName());
		if (debtor.haveToBePaidoff()) // to payoff
		{
			imageView.setImageResource(R.drawable.edit_redo);
			lbBalance.setTextColor(0xffff0000);
			lbBalance.setText(Utils.getNumber(context, debtor.getBalance()));
		}
		else if (debtor.haveToPayoff())
		{
			imageView.setImageResource(R.drawable.dollar48);
			lbBalance.setTextColor(0xffffffff);
			lbBalance.setText(Utils.getNumber(context, debtor.getBalance()));
		}
		else
		{
			imageView.setImageResource(R.drawable.dollar48);
			lbBalance.setText("");
		}

		return rowView;
	}
}