package nah.prayer.loopingviewpagerlib;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class ImageAdapter extends PagerAdapter {
	private Context context;
	private ArrayList<Object> list;
	private ScaleType type;
    private int customScaleWidth = 1600;
    private int customScaleHeight = 1600;



    public ImageAdapter(Context context, ScaleType type){
        this.context=context;
        this.type=type;
    }
    public ImageAdapter(Context context, ScaleType type, ArrayList<Object> list){
        this.context=context;
        this.type=type;
        this.list=list;
    }
    public ImageAdapter(Context context, ScaleType type, int customScale, ArrayList<Object> list){
        this.context=context;
        this.type=type;
        this.list=list;
        this.customScaleWidth=customScale;
        this.customScaleHeight=customScale;
    }
    public ImageAdapter(Context context, ScaleType type, int customScaleWidth, int customScaleHeight, ArrayList<Object> list){
        this.context=context;
        this.type=type;
        this.list=list;
        this.customScaleWidth=customScaleWidth;
        this.customScaleHeight=customScaleHeight;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(type);
        Glide.with(context)
                .load(list.get(position))
                .apply(new RequestOptions()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(customScaleWidth, customScaleHeight)
                    .fitCenter())
                .into(imageView);

        container.addView(imageView, 0);

        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
  }
