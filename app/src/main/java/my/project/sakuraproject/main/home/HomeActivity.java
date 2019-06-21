package my.project.sakuraproject.main.home;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.WeekAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.custom.VpSwipeRefreshLayout;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.about.AboutActivity;
import my.project.sakuraproject.main.animeList.AnimeListActivity;
import my.project.sakuraproject.main.animeTopic.AnimeTopicActivity;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.favorite.FavoriteActivity;
import my.project.sakuraproject.main.search.SearchActivity;
import my.project.sakuraproject.main.setting.SettingActivity;
import my.project.sakuraproject.main.tag.TagActivity;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;

public class HomeActivity extends BaseActivity<HomeContract.View, HomePresenter> implements NavigationView.OnNavigationItemSelectedListener, HomeContract.View {
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    VpSwipeRefreshLayout mSwipe;
    private ImageView imageView;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    private int week;
    private SearchView mSearchView;
    private String[] tabs = Utils.getArray(R.array.week_array);

    @Override
    protected HomePresenter createPresenter() {
        return new HomePresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_home;
    }

    @Override
    protected void init() {
        initToolbar();
        initDrawer();
        initSwipe();
        initFragment();
    }

    @Override
    protected void initBeforeView() {
    }

    public void initToolbar() {
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setSubtitle(getResources().getString(R.string.app_sub_name));
        setSupportActionBar(toolbar);
    }

    public void initDrawer() {
        StatusBarUtil.setColorForDrawerLayout(this, drawer, getResources().getColor(R.color.night), 0);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        };
        int[] colors = new int[]{getResources().getColor(R.color.grey50),
                getResources().getColor(R.color.pinka200)
        };
        ColorStateList csl = new ColorStateList(states, colors);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl);
        View view = navigationView.getHeaderView(0);
        imageView = view.findViewById(R.id.imageView);
        imageView.setOnClickListener(view1 -> {
            Utils.showSnackbar(imageView, Utils.getString(R.string.huaji));
            final ObjectAnimator animator = Utils.tada(imageView);
            animator.setRepeatCount(0);
            animator.setDuration(1000);
            animator.start();
        });
        navigationView.getBackground().mutate().setAlpha(150);//0~255透明度值
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            viewpager.removeAllViews();
            mPresenter.loadData(true);
        });
    }

    public void initFragment() {
        week = Utils.getWeekOfDate(new Date());
        for (String title : tabs) {
            tab.addTab(tab.newTab());
        }
        tab.setupWithViewPager(viewpager);
        //手动 添加标题必须在 setupwidthViewPager后
        for (int i = 0; i < tabs.length; i++) {
            tab.getTabAt(i).setText(tabs[i]);
        }
        tab.setSelectedTabIndicatorColor(getResources().getColor(R.color.pinka200));
        if (Boolean.parseBoolean(SharedPreferencesUtils.getParam(Sakura.getInstance(), "show_x5_info", true).toString()))
            Utils.showX5Info(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        final MenuItem item = menu.findItem(R.id.search);
        mSearchView = (SearchView) item.getActionView();
        mSearchView.setQueryHint(Utils.getString(R.string.search_hint));
        mSearchView.setMaxWidth(1000);
        SearchView.SearchAutoComplete textView = mSearchView.findViewById(R.id.search_src_text);
        textView.setTextColor(getResources().getColor(R.color.grey50));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.replaceAll(" ", "").isEmpty()) {
                    Utils.hideKeyboard(mSearchView);
                    mSearchView.clearFocus();
                    startActivity(new Intent(HomeActivity.this, SearchActivity.class).putExtra("title", query));
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else application.showSnackbarMsg(toolbar,
                Utils.getString(R.string.exit_app),
                Utils.getString(R.string.exit),
                v -> application.removeALLActivity());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (!Utils.isFastClick()) return false;
        switch (item.getItemId()) {
            case R.id.find_anime:
                startActivity(new Intent(this, TagActivity.class));
                break;
            case R.id.anime_movie:
                openAnimeListActivity("动漫电影", Sakura.MOVIE_API, true);
                break;
            case R.id.anime_zt:
                Bundle bundle = new Bundle();
                bundle.putString("title", "动漫专题");
                bundle.putString("url", Sakura.ZT_API);
                startActivity(new Intent(this, AnimeTopicActivity.class).putExtras(bundle));
                break;
            case R.id.anime_jcb:
                openAnimeListActivity("剧场版动画", Sakura.JCB_API, false);
                break;
            case R.id.favorite:
                startActivity(new Intent(this, FavoriteActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.setting:
                startActivityForResult(new Intent(this, SettingActivity.class), 0x10);
                break;
        }
        return true;
    }

    private void openAnimeListActivity(String title, String url, boolean isMovie) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        bundle.putBoolean("isMovie", isMovie);
        startActivity(new Intent(this, AnimeListActivity.class).putExtras(bundle));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x10 && resultCode == 0x20) {
            viewpager.removeAllViews();
            mPresenter.loadData(true);
        }
    }

    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
        application.error = "";
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            mSwipe.setRefreshing(false);
            application.showToastMsg(msg);
            application.error = msg;
            application.week = new JSONObject();
            setWeekAdapter();
        });
    }

    @Override
    public void showEmptyVIew() {
    }

    @Override
    public void showLoadSuccess(LinkedHashMap map) {
        runOnUiThread(() -> {
            mSwipe.setRefreshing(false);
            application.error = "";
            application.week = map.get("week") == null ? new JSONObject() : (JSONObject) map.get("week");
            setWeekAdapter();
        });
    }

    public void setWeekAdapter() {
        WeekAdapter adapter = new WeekAdapter(getSupportFragmentManager(), tab.getTabCount());
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(week);
        for (int i = 0; i < tabs.length; i++) {
            tab.getTabAt(i).setText(tabs[i]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtil.closeDB();
    }
}