package kd.apps.Debtors;

import java.util.List;

import kd.apps.Debtors.db.DB;
import kd.apps.Debtors.db.Debtor;
import kd.apps.Debtors.db.Entry;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class EntryMapActivity extends MapActivity
{
	private DB db;
	private Debtor debtor;
	private Entry entry;

	private GeoPoint p;
	private Location loc;

	private TextView tbDebtor, tbDate, tbValue, tbDesc;

	class MapOverlay extends com.google.android.maps.Overlay
	{
		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
		{
			super.draw(canvas, mapView, shadow);

			Projection proj = mapView.getProjection();

			Point screenPts = proj.toPixels(p, null);
			float radius = proj.metersToEquatorPixels(loc.getAccuracy());

			float scale = 0.3f;

			int size = 64;

			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inDensity = DisplayMetrics.DENSITY_DEFAULT;
			Bitmap bmp;
			if (entry.getIsLastLocation())
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.circle_red16, opt);
			else
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.circle_green16, opt);

			Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
			RectF dst = new RectF(
					screenPts.x - (size * scale) / 2,
					screenPts.y - (size * scale) / 2,
					screenPts.x - (size * scale) / 2 + size * scale,
					screenPts.y - (size * scale) / 2 + size * scale);

			Paint paint;

			paint = new Paint();
			paint.setARGB(120, 200, 200, 255);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(screenPts.x, screenPts.y, radius, paint);

			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setARGB(255, 0, 0, 255);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(0.5f);
			canvas.drawCircle(screenPts.x, screenPts.y, radius, paint);

			paint = new Paint();
			canvas.drawBitmap(bmp, src, dst, paint);

			return true;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry_map);

		db = new DB(this);
		db.open();

		Intent intent = getIntent();
		long entryId = intent.getLongExtra(DebtorsActivity.ENTRY_ID, -1);

		entry = db.getEntryById(entryId);
		debtor = db.getDebtorById(entry.getDebtorId());
		loc = entry.getLocation();

		tbDebtor = (TextView)findViewById(R.id.debtor);
		tbDate = (TextView)findViewById(R.id.date);
		tbValue = (TextView)findViewById(R.id.value);
		tbDesc = (TextView)findViewById(R.id.desc);

		tbDebtor.setText(debtor.getName());
		tbDesc.setText(entry.getDesc());
		tbDesc.setVisibility(entry.getDesc().length() == 0 ? View.GONE : View.VISIBLE);
		tbDate.setText(entry.getFormattedDate());
		tbValue.setText(Utils.getNumber(this, entry.getValue()));

		MapView mapView = (MapView)findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);

		int latitudeE6 = (int)(loc.getLatitude() * 1E6);
		int longitudeE6 = (int)(loc.getLongitude() * 1E6);

		p = new GeoPoint(latitudeE6, longitudeE6);

		MapController mc = mapView.getController();
		mc.setCenter(p);

		double zoomOut = 3;
		int zoom = (int)Math.round(Math.log10(40000000.0 / (loc.getAccuracy() * zoomOut)) / Math.log10(2) + 1);

		mc.setZoom(zoom);

		MapOverlay mapOverlay = new MapOverlay();
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(mapOverlay);

		mapView.invalidate();
	}
	@Override
	protected void onPause()
	{
		super.onPause();
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		/*String orig = this.getResources().getText(R.string.title_activity_entry_map).toString();
		String title = String.format(orig, debtor.getName());
		setTitle(title);*/
	}
	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.entry_map, menu);
		return true;
	}
	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
}
