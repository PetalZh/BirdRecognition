package xiaoyu.recorder;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import java.io.File;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener{
    private ViewPager pager;
    private ScreenSlidePagerAdapter adapter;
    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkState();// check login

//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
//
//            ActivityCompat.requestPermissions( this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                    LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION );
//        }

//        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)!= true)
//        {
//            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(callGPSSettingIntent);
//        }

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.viewpaper);
        pager.setAdapter(adapter);

        //pager.setCurrentItem(1);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }
            @Override
            public void onPageSelected(int pos) {
                actionBar.setSelectedNavigationItem(pos);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        for(int i = 0; i< adapter.getCount(); i++)
        {
            actionBar.addTab(actionBar.newTab().setText(adapter.getPageTitle(i)).setTabListener(this));
        }

    }

    public void checkState()
    {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/birdRec/log/userLog.txt";
        File file = new File(dir);
        // if user log file does not exit, go to login activity
        if(!file.exists())
        {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos)
        {
            Fragment f = null;
            if(pos == 0)
            {
                f = new RecorderFragment();


            }else if(pos == 1){
                f = new MapFragment();

            }else if(pos ==2)
            {
                f = new UserInfoFragment();

            }
            return f;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int pos) {
            if(pos ==0)
            {
                return "Record";
            }else if(pos == 1)
            {
                return "Map";
            }else if(pos == 2)
            {
                return "User Info";
            }
            return super.getPageTitle(pos);
        }
    }

}