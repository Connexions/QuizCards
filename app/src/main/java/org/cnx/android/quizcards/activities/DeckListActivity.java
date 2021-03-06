/**
 * Copyright (c) 2012 Rice University
 *
 * This software is subject to the provisions of the GNU Lesser General
 * Public License Version 2.1 (LGPL).  See LICENSE.txt for details.
 */

package org.cnx.android.quizcards.activities;

import static org.cnx.android.quizcards.Constants.ABSTRACT;
import static org.cnx.android.quizcards.Constants.AUTHOR;
import static org.cnx.android.quizcards.Constants.DECK_ID;
import static org.cnx.android.quizcards.Constants.NEW_DECK;
import static org.cnx.android.quizcards.Constants.TITLE;

import org.cnx.android.quizcards.R;
import org.cnx.android.quizcards.database.DeckProvider;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class DeckListActivity extends Activity {
    
    ListView deckListView;
    Cursor titlesCursor;
    
    static int DECK_INFO_REQUEST = 0;
    static int NEW_DECK_REQUEST = 1;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deck_list);
        
        // Allow going back with ActionBar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        // Get UI elements
        deckListView = (ListView)findViewById(R.id.deckListView);
        
        // Retrieve decks from the database, show in list
        getDecks();
        
        deckListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long resource_id) {
                // Get the id of the selected deck
                titlesCursor.moveToPosition(position);
                String id = titlesCursor.getString(titlesCursor.getColumnIndex(BaseColumns._ID));
                
                // Launch the deck
                Intent cardIntent = new Intent(getApplicationContext(), DeckDetailsActivity.class);
                cardIntent.putExtra(DECK_ID, id);
                startActivityForResult(cardIntent, DECK_INFO_REQUEST);
            }
        });
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deck_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;
            
        case R.id.newDeckActionItem:
            Intent editIntent = new Intent(DeckListActivity.this, DeckEditorActivity.class);
            editIntent.putExtra(NEW_DECK, true);
            startActivityForResult(editIntent, NEW_DECK_REQUEST);
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    /**Extracts decks from the database, shows them in the ListView**/
    private void getDecks() {
        String[] projection = {BaseColumns._ID, TITLE, AUTHOR, ABSTRACT };
        String order = "LOWER(" + TITLE + ")";
        titlesCursor = getContentResolver().query(
                DeckProvider.CONTENT_URI, projection, null, null, order);
        titlesCursor.moveToFirst();
        
        int[] to = {R.id.title, R.id.authors, R.id.summary};
        String[] from = {TITLE, AUTHOR, ABSTRACT };
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.deck_list_row, titlesCursor, from, to, CursorAdapter.NO_SELECTION);

        deckListView.setAdapter(cursorAdapter);
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == DECK_INFO_REQUEST || requestCode == NEW_DECK_REQUEST) {
    		getDecks();
    	}

    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    @Override
    public void finish() {
        titlesCursor.close();
        super.finish();
    }
}
