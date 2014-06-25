package org.redpin.android.ui;

import org.example.touch.WrapMotionEvent;
import org.redpin.android.R;
import org.redpin.android.core.Map;
import org.redpin.android.db.EntityHomeFactory;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MainMapViewActivity extends ActionBarActivity implements OnTouchListener {
	
	private static final String TAG = MainMapViewActivity.class.getSimpleName();
	
	ImageView imgMap;
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.map_view_layout);
		
		imgMap = (ImageView) findViewById(R.id.imgMap);
		imgMap.setVisibility(View.GONE);
		
		show();
	}
	
	private void show() {
		Uri uri = getIntent().getData();
		if(uri == null) {
			finish();
		}
		
		long id = ContentUris.parseId(uri);
		Map m = EntityHomeFactory.getMapHome().getById(id);
		
		if(m == null) {
			finish();
		}
	
		imgMap.setOnTouchListener(this);
		matrix.setTranslate(1f, 1f);
		imgMap.setImageMatrix(matrix);
		
		Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() 
				+ "/indoormaps/" + m.getMapURL());
		
		imgMap.setImageBitmap(bm);
		imgMap.setVisibility(View.VISIBLE);
	}
	
	public boolean onTouch(View v, MotionEvent rawEvent) {
		WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
		ImageView view = (ImageView) v;

		float[] values = new float[9];
		matrix.getValues(values);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			
			r.x = (event.getX() - values[2]) / values[0];
			r.y = (event.getY() - values[5]) / values[4];
			
			handler.postDelayed(r, 500);
		}
		
		if ((event.getAction() == MotionEvent.ACTION_MOVE) || (event.getAction() == MotionEvent.ACTION_UP))
			handler.removeCallbacks(r);

		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			Log.d(TAG, "mode=DRAG");
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				Log.d(TAG, "mode=ZOOM");
			}
			break;
		case MotionEvent.ACTION_UP:
			

			break;

		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			Log.d(TAG, "mode=NONE");
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				// ...
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY()
						- start.y);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				Log.d(TAG, "newDist=" + newDist);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}

		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}

	/** Determine the space between the first two fingers */
	private float spacing(WrapMotionEvent event) {
		// ...
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, WrapMotionEvent event) {
		// ...
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
	class LongPressedRunner implements Runnable {

		public float x, y;

		public LongPressedRunner(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public void run() {			

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainMapViewActivity.this, "X: " + x + ", Y: " + y, Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	final Handler handler = new Handler();
	LongPressedRunner r = new LongPressedRunner(0, 0);
}
