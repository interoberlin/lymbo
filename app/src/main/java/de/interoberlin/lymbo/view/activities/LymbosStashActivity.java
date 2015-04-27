package de.interoberlin.lymbo.view.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.github.mrengineer13.snackbar.SnackBar;

import java.util.List;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.controller.CardsController;
import de.interoberlin.lymbo.controller.LymbosController;
import de.interoberlin.lymbo.model.card.Lymbo;
import de.interoberlin.lymbo.view.adapters.LymbosStashListAdapter;
import de.interoberlin.lymbo.view.dialogfragments.DisplayDialogFragment;
import de.interoberlin.mate.lib.util.Toaster;
import de.interoberlin.mate.lib.view.AboutActivity;
import de.interoberlin.mate.lib.view.LogActivity;

public class LymbosStashActivity extends BaseActivity implements SnackBar.OnMessageClickListener, DisplayDialogFragment.OnCompleteListener {
    // Controllers
    LymbosController lymbosController = LymbosController.getInstance();
    CardsController cardsController = CardsController.getInstance();

    // Context and Activity
    private static Context context;
    private static Activity activity;

    // Views
    private SwipeListView slv;

    // Model
    private List<Lymbo> lymbos;
    private LymbosStashListAdapter lymbosStashedAdapter;

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            lymbosController.load();
        }

        setTitle(R.string.stash);
        setActionBarIcon(R.drawable.ic_ab_drawer);
        setDisplayHomeAsUpEnabled(true);

        // Register on toaster
        Toaster.register(this, context);

        // Get activity and context for further use
        activity = this;
        context = getApplicationContext();
    }

    public void onResume() {
        super.onResume();
        lymbos = lymbosController.getLymbosStashed();
        lymbosStashedAdapter = new LymbosStashListAdapter(activity, context, R.layout.stack_stash, lymbos);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.dl);
        drawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        slv = (SwipeListView) findViewById(R.id.slv);
        slv.setAdapter(lymbosStashedAdapter);
        slv.setSwipeMode(SwipeListView.SWIPE_MODE_NONE);
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
                Intent i = new Intent(LymbosStashActivity.this, LogActivity.class);
                startActivity(i);
                break;
            }
            case R.id.menu_about: {
                Intent i = new Intent(LymbosStashActivity.this, AboutActivity.class);
                Bundle b = new Bundle();
                b.putString("flavor", "interoberlin");
                i.putExtras(b);
                startActivity(i);
                break;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

        return true;
    }

    public void restore() {
        cardsController.restore();
        lymbosStashedAdapter.notifyDataSetChanged();
        slv.invalidateViews();

        new SnackBar.Builder(this)
                .withOnClickListener(this)
                .withMessageId(R.string.stack_restored)
                .withActionMessageId(R.string.undo)
                .withStyle(SnackBar.Style.INFO)
                .withDuration(SnackBar.MED_SNACK)
                .show();
    }

    @Override
    public void onMessageClick(Parcelable token) {
        cardsController.stash();
        lymbosStashedAdapter.notifyDataSetChanged();
        slv.invalidateViews();
    }

    // --------------------
    // Methods - Callbacks
    // --------------------

    @Override
    public void onHintDialogComplete() {
    }

    @Override
    public void onDiscardCardDialogComplete() {
    }

    // --------------------
    // Methods
    // --------------------

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_lymbos_stash;
    }
}