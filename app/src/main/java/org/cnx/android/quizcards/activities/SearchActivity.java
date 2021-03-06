/**
 * Copyright (c) 2012 Rice University
 *
 * This software is subject to the provisions of the GNU Lesser General
 * Public License Version 2.1 (LGPL).  See LICENSE.txt for details.
 */

package org.cnx.android.quizcards.activities;

import static org.cnx.android.quizcards.Constants.MODULE_ID;
import static org.cnx.android.quizcards.Constants.SEARCH_TERM;

import java.util.ArrayList;

import org.cnx.android.quizcards.R;
import org.cnx.android.quizcards.ModuleToDatabaseParser;
import org.cnx.android.quizcards.SearchResult;
import org.cnx.android.quizcards.SearchResultsAdapter;
import org.cnx.android.quizcards.SearchResultsParser;
import org.cnx.android.quizcards.ModuleToDatabaseParser.ParseResult;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.app.ActionBar;
import android.app.Activity;
import android.view.MenuItem;




public class SearchActivity extends Activity {
    
    public enum SearchDirection {
        NEXT,
        PREVIOUS
    }
    
    String searchTerm;
    ListView resultsListView;
    SearchResultsAdapter resultsAdapter;
    ArrayList<SearchResult> results; 
    SearchResultsParser resultsParser;
    SearchResultsTask searchResultsTask;
    
    Button nextButton;
    Button prevButton;
    Button searchButton;
    TextView pageText;
    EditText searchInput;
    
    DownloadDeckTask downloadTask;
    
    AlertDialog downloadingDialog;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.search_results);
        
        // Allow going back with ActionBar
        ActionBar actionBar = getActionBar();

        // Hide the keyboard at launch (as EditText will be focused automatically)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        //Get UI elements
        resultsListView = (ListView)findViewById(R.id.resultsList);
        pageText = (TextView)findViewById(R.id.pageText);
        nextButton = (Button)findViewById(R.id.nextPageButton);
        prevButton = (Button)findViewById(R.id.prevPageButton);
        searchInput = (EditText)findViewById(R.id.searchInput);
        searchButton = (Button)findViewById(R.id.searchButton);
        
        // Get a parser for the search term
        searchTerm = getIntent().getStringExtra(SEARCH_TERM);
        searchInput.setText(searchTerm);
        
        resultsParser = new SearchResultsParser(this, searchTerm);
        
        // Tie the ListView to the results
        results = new ArrayList<>();
        resultsAdapter = new SearchResultsAdapter(this, results);
        resultsListView.setAdapter(resultsAdapter);
        
        // Get the first page of search results
        search(SearchDirection.NEXT);
        
        // When an item's clicked, try to download that module.
        resultsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long resource_id) {
                String id = ((SearchResult)resultsListView.getItemAtPosition(position)).getId();
                setProgressBarIndeterminateVisibility(Boolean.TRUE);
                
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setMessage("Downloading chapter...");
                builder.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setProgressBarIndeterminateVisibility(false);
                        downloadTask.cancel(true);
                        Toast cancelledToast = Toast.makeText(SearchActivity.this, "Download cancelled.", Toast.LENGTH_SHORT);
                        cancelledToast.show();
                    }
                });
                downloadingDialog = builder.create();
               
                downloadTask = new DownloadDeckTask();
                downloadTask.execute(id);
                
                downloadingDialog.show();
            }
        });
        
        // Loads the next page when clicked
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                search(SearchDirection.NEXT);
                nextButton.setEnabled(false);
                prevButton.setEnabled(false);
            }
        });
        
        // Loads the previous page when clicked
        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                search(SearchDirection.PREVIOUS);
                nextButton.setEnabled(false);
                prevButton.setEnabled(false);
            }
        });
        
        // Search if the user hits enter while typing a search term
        searchInput.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchTerm = searchInput.getText().toString();
                resultsParser = new SearchResultsParser(SearchActivity.this, searchTerm);
                search(SearchDirection.NEXT);
                nextButton.setEnabled(false);
                prevButton.setEnabled(false);
                pageText.setText("");
                return true;
            }
        });
        
        // Search if the user hits the search button (identical to hitting enter, TODO: combine them)
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTerm = searchInput.getText().toString();
                resultsParser = new SearchResultsParser(SearchActivity.this, searchTerm);
                search(SearchDirection.NEXT);
                nextButton.setEnabled(false);
                prevButton.setEnabled(false);
                pageText.setText("");  
                
                // Hide the keyboard after search
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        
        searchInput.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals(""))
                    searchButton.setEnabled(false);
                else
                    searchButton.setEnabled(true);
            }
        });
        //actionBar.setDisplayHomeAsUpEnabled(true);
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    @Override
    public void finish() {
        if(searchResultsTask != null)
            searchResultsTask.cancel(true);
        
        super.finish();
    }
    
    
    private void search(SearchDirection direction) {
        results.clear();
        resultsAdapter.notifyDataSetChanged();
        
        if(searchResultsTask != null)
            searchResultsTask.cancel(true);
        searchResultsTask = new SearchResultsTask();
        searchResultsTask.execute(direction);
        
        Toast searchingToast = Toast.makeText(
                SearchActivity.this, "Searching for '" + searchTerm + "'...",
                Toast.LENGTH_SHORT);
        searchingToast.show();
        
        searchInput.setText(searchTerm);
        
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
    }

    
    private class SearchResultsTask extends AsyncTask<SearchDirection, Void, ArrayList<SearchResult>> {

        @Override
        protected ArrayList<SearchResult> doInBackground(SearchDirection... direction) {
            ArrayList<SearchResult> resultList;
            
            if(direction[0] == SearchDirection.NEXT)
                resultList = resultsParser.getNextPage();
            else
                resultList = resultsParser.getPrevPage();
            
            return resultList;
        }
        
        @Override
        protected void onPostExecute(ArrayList<SearchResult> resultList) {
            super.onPostExecute(resultList);

            setProgressBarIndeterminateVisibility(Boolean.FALSE);
            
            if(isCancelled())
                return;
            
            //TODO: Handle a null result better (repeat search?)
            if(resultList != null && resultList.size()!=0) {
                
                if(resultList.size() > resultsParser.resultsPerPage) {
                    nextButton.setEnabled(true);
                    resultList.remove(resultList.size()-1);
                }
                
                results.addAll(resultList);
                resultsAdapter.notifyDataSetChanged();
                
                pageText.setText(Integer.toString(resultsParser.currentPage+1));
                if(resultsParser.currentPage == 0)
                    prevButton.setEnabled(false);
                else
                    prevButton.setEnabled(true);
            }
            else if(resultList == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setTitle("Unable to search");
                builder.setMessage("Couldn't reach Connexions to search.");
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
            else if(resultList.size() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setTitle("No results");
                builder.setMessage("No results were found for the search term \"" + searchTerm + "\".");
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }
    }


    public class DownloadDeckTask extends AsyncTask<String, Void, ParseResult> {

        String id;

        @Override
        protected ParseResult doInBackground(String... idParam) {
            this.id = idParam[0];
            
            ModuleToDatabaseParser parser = new ModuleToDatabaseParser(SearchActivity.this, id);
            
            // Horrible number of if statements to avoid downloading after being cancelled
            
            // Check if module is already downloaded
            if(parser.isDuplicate())
                return ParseResult.DUPLICATE;

            // Download the module and metadata, if task isn't cancelled
            if(!isCancelled())  parser.retrieveModuleXML();
            if(!isCancelled())  parser.retrieveMetadataXML();
            
            // Check that module and metadata downloaded successfully
            if(!isCancelled() && parser.gotXML())
                parser.parseXML();
            else
                return ParseResult.NO_XML;
            
            // Check that module has definitions, get them if it does
            if(!isCancelled() && parser.hasDefinitions())
                parser.extractDefinitions();
            else
                return ParseResult.NO_NODES;
               
            // Finally, add definitions to the database
            if(!isCancelled())
                parser.addValuesToDatabase();

            // Return value is ignored if task cancelled, so just return SUCCESS
            return ParseResult.SUCCESS;
        }

        @Override
        protected void onPostExecute(ParseResult result) {
            super.onPostExecute(result);

            setProgressBarIndeterminateVisibility(Boolean.FALSE);

            String errorText = null;
            boolean launch = false;

            switch (result) {
            case SUCCESS:
                launch = true;
                break;

            case DUPLICATE:
                Toast resultsToast = Toast.makeText(SearchActivity.this, 
                        "You've already downloaded this deck.", Toast.LENGTH_SHORT);
                resultsToast.show();
                
                launch = true;
                break;

            case NO_NODES:
                errorText = "That chapter has no glossary and can't be made into flash cards.";
                break;
                
            case NO_XML:
                errorText = "Unable to download chapter from Connexions. Try again shortly.";
                break;
            }
            
            downloadingDialog.dismiss();
            
            if(launch) {
                Intent cardIntent = new Intent(getApplicationContext(), DeckDetailsActivity.class);
                cardIntent.putExtra(MODULE_ID, id);
                startActivity(cardIntent);
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setMessage(errorText);
                builder.create().show();
            }
        }
    }
}
