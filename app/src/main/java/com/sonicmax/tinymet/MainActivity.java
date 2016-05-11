package com.sonicmax.tinymet;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.sonicmax.tinymet.adapters.DictionaryAdapter;
import com.sonicmax.tinymet.dialogs.LanguageDialog;
import com.sonicmax.tinymet.dialogs.SortDialog;
import com.sonicmax.tinymet.dialogs.TempoRangeDialog;
import com.sonicmax.tinymet.utilities.Tempo;
import com.sonicmax.tinymet.utilities.TempoDictionary;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Default values
    private final String DEFAULT_LANG = "Italian";

    // Fragment tags
    private final String LANGUAGE_SELECTOR_TAG = "language_selector";
    private final String TEMPO_RANGE_TAG = "tempo_selector";
    private final String SORT_TAG = "sort_selector";

    // sqlite stuff
    private final String NAME_COLUMN = "NAME";
    private final String MIN_COLUMN = "MIN";
    private final String MAX_COLUMN = "MAX";

    // Objects, primitives and views that we might want to reference later
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerDictionary;
    private TextView mDrawerLanguage;
    private ImageButton mDrawerSort;
    private DictionaryAdapter mDrawerAdapter;
    private TempoDictionary mTempoDictionary;
    private String mCurrentLanguage;

    ///////////////////////////////////////////////////////////////////////////
    // Activity lifecycle stuff
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(4);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        getViewsFromDrawer();
        setDrawerLanguage(DEFAULT_LANG);

        // Init TempoDictionary and populate navigation drawer with data
        mTempoDictionary = new TempoDictionary(this) {
            @Override
            public void onLoad(Cursor data) {
                addDrawerToggleListener();
                populateDrawer(data);
            }
        };

        addListeners();
    }

    private void addListeners() {
        // Add listener which allows user to change language of dictionary
        mDrawerLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LanguageDialog selector = new LanguageDialog() {

                    @Override
                    public void onChooseLanguage(String language) {
                        mDrawerDictionary.clearChoices();
                        mCurrentLanguage = language;
                        mDrawerLanguage.setText(language);
                        mTempoDictionary.loadDatabase(language);
                        this.dismiss();
                    }
                };

                selector.show(getSupportFragmentManager(), LANGUAGE_SELECTOR_TAG);
            }
        });

        // Add listener which allows user to choose sort type of dictionary
        mDrawerSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SortDialog selector = new SortDialog() {

                    @Override
                    public void onChooseSort(String sortType) {
                        mDrawerDictionary.clearChoices();
                        mTempoDictionary.sortDatabase(mCurrentLanguage, sortType);
                        this.dismiss();
                    }
                };

                selector.show(getSupportFragmentManager(), SORT_TAG);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Navigation drawer helper methods
    ///////////////////////////////////////////////////////////////////////////

    private void getViewsFromDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLanguage = (TextView) findViewById(R.id.drawer_lang);
        mDrawerDictionary = (ListView) findViewById(R.id.drawer_dictionary);
        mDrawerSort = (ImageButton) findViewById(R.id.sort_dictionary);
    }

    private void populateDrawer(Cursor cursor) {
        // First, do some basic UI stuff
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Init arrays used to store dictionary search results
        ArrayList<String> nameArray = new ArrayList<>();
        final ArrayList<Tempo> tempoArray = new ArrayList<>();

        int nameColumn = cursor.getColumnIndex(NAME_COLUMN);
        int minColumn = cursor.getColumnIndex(MIN_COLUMN);
        int maxColumn = cursor.getColumnIndex(MAX_COLUMN);

        while (cursor.moveToNext()) {
            // Search database for tempo indications which fit user intent
            nameArray.add(cursor.getString(nameColumn));
            tempoArray.add(new Tempo(cursor.getInt(minColumn), cursor.getInt(maxColumn)));
        }

        // Create adapter to display this data and add necessary listeners
        mDrawerAdapter = new DictionaryAdapter(getBaseContext());
        mDrawerDictionary.setAdapter(mDrawerAdapter);
        mDrawerAdapter.updateDictionary(nameArray, tempoArray);

        mDrawerDictionary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TempoRangeDialog rangeDialog = new TempoRangeDialog(tempoArray.get(position)) {

                    @Override
                    public void onChooseTempo(int tempo) {
                        MainFragment fragment = (MainFragment)
                                getSupportFragmentManager().findFragmentById(R.id.main);
                        fragment.setTempo(tempo);
                        this.dismiss();
                        mDrawerLayout.closeDrawers();
                    }

                    @Override
                    public void onCancel() {
                        this.dismiss();
                        mDrawerLayout.closeDrawers();
                    }
                };

                rangeDialog.show(getSupportFragmentManager(), TEMPO_RANGE_TAG);
            }
        });
    }

    private void addDrawerToggleListener() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };
    }

    private void setDrawerLanguage(String language) {
        mDrawerLanguage.setText(language);
        mTempoDictionary.loadDatabase(language);
        mCurrentLanguage = language;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Options menu methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
