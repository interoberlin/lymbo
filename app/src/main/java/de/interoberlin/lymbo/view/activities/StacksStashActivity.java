package de.interoberlin.lymbo.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mrengineer13.snackbar.SnackBar;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.controller.StacksController;
import de.interoberlin.lymbo.model.card.Stack;
import de.interoberlin.lymbo.view.adapters.StacksStashListAdapter;
import de.interoberlin.mate.lib.view.AboutActivity;
import de.interoberlin.mate.lib.view.LogActivity;
import de.interoberlin.swipelistview.view.SwipeListView;

public class StacksStashActivity extends SwipeRefreshBaseActivity implements SwipeRefreshLayout.OnRefreshListener, SnackBar.OnMessageClickListener {
    // Controllers
    private StacksController stacksController;

    // Model
    private StacksStashListAdapter lymbosStashAdapter;

    private Stack recentStack = null;

    private static int REFRESH_DELAY;

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stacksController = StacksController.getInstance(this);

        if (savedInstanceState != null) {
            stacksController.load();
        }

        setActionBarIcon(R.drawable.ic_ab_drawer);
        setDisplayHomeAsUpEnabled(true);

        REFRESH_DELAY = getResources().getInteger(R.integer.refresh_delay_lymbos);
    }

    public void onResume() {
        super.onResume();
        lymbosStashAdapter = new StacksStashListAdapter(this, this, R.layout.stack_stash, stacksController.getLymbosStashed());

        // Load layout
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.dl);
        final LinearLayout toolbarWrapper = (LinearLayout) findViewById(R.id.toolbar_wrapper);
        final TextView toolbarTitleView = (TextView) findViewById(R.id.toolbar_title);
        final SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        final SwipeListView slv = (SwipeListView) findViewById(R.id.slv);

        drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        toolbarTitleView.setText(R.string.lymbos_stash);

        srl.setOnRefreshListener(this);
        srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);

        slv.setAdapter(lymbosStashAdapter);
        slv.setSwipeMode(SwipeListView.SWIPE_MODE_NONE);

        updateSwipeRefreshProgressBarTop(srl);
        registerHideableHeaderView(toolbarWrapper);
        enableActionBarAutoHide(slv);

        updateListView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_lymbos_stash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_log: {
                Intent i = new Intent(StacksStashActivity.this, LogActivity.class);
                startActivity(i);
                break;
            }
            case R.id.menu_about: {
                Intent i = new Intent(StacksStashActivity.this, AboutActivity.class);
                Bundle b = new Bundle();
                b.putString("flavor", "interoberlin");
                i.putExtras(b);
                startActivity(i);
                break;
            }
            case R.id.menu_settings: {
                Intent i = new Intent(StacksStashActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

        return true;
    }

    // --------------------
    // Methods - Callbacks
    // --------------------

    @Override
    public void onMessageClick(Parcelable token) {
        final SwipeListView slv = (SwipeListView) findViewById(R.id.slv);

        stacksController.stash(recentStack);
        lymbosStashAdapter.notifyDataSetChanged();
        slv.invalidateViews();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
                final SwipeListView slv = (SwipeListView) findViewById(R.id.slv);

                srl.setRefreshing(false);

                stacksController.scan();
                stacksController.load();

                lymbosStashAdapter.notifyDataSetChanged();
                slv.invalidateViews();
            }
        }, REFRESH_DELAY);
    }

    // --------------------
    // Methods - Actions
    // --------------------

    /**
     * Restores a lymbo
     *
     * @param stack lymbo to be restored
     */
    public void restore(Stack stack) {
        updateListView();

        recentStack = stack;

        new SnackBar.Builder(this)
                .withOnClickListener(this)
                .withMessageId(R.string.stack_restored)
                .withActionMessageId(R.string.undo)
                .withStyle(SnackBar.Style.INFO)
                .withDuration(SnackBar.MED_SNACK)
                .show();
    }

    // --------------------
    // Methods
    // --------------------

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_lymbos_stash;
    }

    /**
     * Updates the list view
     */
    private void updateListView() {
        final SwipeListView slv = (SwipeListView) findViewById(R.id.slv);


        lymbosStashAdapter.filter();
        slv.closeOpenedItems();
        slv.invalidateViews();
    }
}