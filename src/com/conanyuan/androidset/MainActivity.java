package com.conanyuan.androidset;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GridView grid = (GridView) findViewById(R.id.card_grid);
        grid.setAdapter(new ImageAdapter(this));

        grid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	//Toast.makeText(HelloGridView.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public class ImageAdapter extends BaseAdapter {
    	private Context mContext;
    	private int[] mShownCards;
    	private int mToShow = 12;

        public ImageAdapter(Context c) {
            mContext = c;
            mShownCards = new int[20];
        }

        @Override
        public int getCount() {
            return mShownCards.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mImageIds[position+1]);
            return imageView;
        }

        // references to our images
        private Integer[] mImageIds = {
        	null,
        	R.drawable.card_1,  R.drawable.card_2,  R.drawable.card_3,
        	R.drawable.card_4,  R.drawable.card_5,  R.drawable.card_6,
        	R.drawable.card_7,  R.drawable.card_8,  R.drawable.card_9,
        	R.drawable.card_10, R.drawable.card_11, R.drawable.card_12,
        	R.drawable.card_13, R.drawable.card_14, R.drawable.card_15,
        	R.drawable.card_16, R.drawable.card_17, R.drawable.card_18,
        	R.drawable.card_19, R.drawable.card_20, R.drawable.card_21,
        	R.drawable.card_22, R.drawable.card_23, R.drawable.card_24,
        	R.drawable.card_25, R.drawable.card_26, R.drawable.card_27,
        	R.drawable.card_28, R.drawable.card_29, R.drawable.card_30,
        	R.drawable.card_31, R.drawable.card_32, R.drawable.card_33,
        	R.drawable.card_34, R.drawable.card_35, R.drawable.card_36,
        	R.drawable.card_37, R.drawable.card_38, R.drawable.card_39,
        	R.drawable.card_40, R.drawable.card_41, R.drawable.card_42,
        	R.drawable.card_43, R.drawable.card_44, R.drawable.card_45,
        	R.drawable.card_46, R.drawable.card_47, R.drawable.card_48,
        	R.drawable.card_49, R.drawable.card_50, R.drawable.card_51,
        	R.drawable.card_52, R.drawable.card_53, R.drawable.card_54,
        	R.drawable.card_55, R.drawable.card_56, R.drawable.card_57,
        	R.drawable.card_58, R.drawable.card_59, R.drawable.card_60,
        	R.drawable.card_61, R.drawable.card_62, R.drawable.card_63,
        	R.drawable.card_64, R.drawable.card_65, R.drawable.card_66,
        	R.drawable.card_67, R.drawable.card_68, R.drawable.card_69,
        	R.drawable.card_70, R.drawable.card_71, R.drawable.card_72,
        	R.drawable.card_73, R.drawable.card_74, R.drawable.card_75,
        	R.drawable.card_76, R.drawable.card_77, R.drawable.card_78,
        	R.drawable.card_79, R.drawable.card_80, R.drawable.card_81,
        };
    }
}
