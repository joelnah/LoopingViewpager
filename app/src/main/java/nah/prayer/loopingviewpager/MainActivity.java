package nah.prayer.loopingviewpager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import nah.prayer.loopingviewpagerlib.LoopViewPager2;
import nah.prayer.loopingviewpagerlib.NahPagerAdapter;

public class MainActivity extends AppCompatActivity {

    LoopViewPager2 viewPager;
    NahPagerAdapter pagerAdapter;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ArrayList<Object> list = new ArrayList<>();
        list.add(R.drawable.if_number_0_blue_1553042);
        list.add(R.drawable.if_number_1_blue_1553030);
        list.add(R.drawable.if_number_2_blue_1553043);
        list.add(R.drawable.if_number_3_blue_1553079);
        list.add(R.drawable.if_number_4_blue_1553097);
        list.add(R.drawable.if_number_5_blue_1553045);
        list.add(R.drawable.if_number_6_blue_1553053);
        list.add(R.drawable.if_number_7_blue_1553034);
        list.add(R.drawable.if_number_8_blue_1553070);
        list.add(R.drawable.if_number_9_blue_1553064);

        final ArrayList<String> list2 = new ArrayList<>();
        list2.add("0");
        list2.add("1");
        list2.add("2");
        list2.add("3");
        list2.add("4");
        list2.add("5");
        list2.add("6");
        list2.add("7");
        list2.add("8");
        list2.add("9");


        viewPager = findViewById(R.id.viewpager);
        tv = findViewById(R.id.tv);




        pagerAdapter = new NahPagerAdapter(this, ImageView.ScaleType.CENTER_CROP, list);
        //pagerAdapter = new NahPagerAdapter(this, ImageView.ScaleType.FIT_CENTER, 1600, list);
        //pagerAdapter = new NahPagerAdapter(this, ImageView.ScaleType.CENTER, 1024, 768, list);
        viewPager.setAdapter(pagerAdapter);
        viewPager.startAutoScroll();
        viewPager.setInterval(3000);
        viewPager.setCycle(true);
        viewPager.setBorderAnimation(false);
        viewPager.setStopScrollWhenTouch(true);

        viewPager.setOnReceivedEvent(new LoopViewPager2.PositionListerner() {
            @Override
            public void onReceivedEvent(int position) {
                tv.setText("position : "+list2.get(position));
            }
        });
    }
}
