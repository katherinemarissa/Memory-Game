package iss.workshop.ca;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

    //import context class
    private Context context;

    //create constructor for ImageAdapter
    public ImageAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        //we have 12 images in the gridview, so return 12
        return 12;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //initialise image view
        ImageView imageView;

        if (convertView == null) {
            //create image view
            imageView = new ImageView(this.context);
            //setting parameters for image view
            imageView.setLayoutParams(new ViewGroup.LayoutParams(350, 350));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); //can also try fit_xy
        }
        else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(R.drawable.ic_back);
        return imageView;
    }
}
