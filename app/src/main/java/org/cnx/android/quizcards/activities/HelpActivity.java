/**
 * Copyright (c) 2012 Rice University
 *
 * This software is subject to the provisions of the GNU Lesser General
 * Public License Version 2.1 (LGPL).  See LICENSE.txt for details.
 */

package org.cnx.android.quizcards.activities;

import org.cnx.android.quizcards.R;

import android.os.Bundle;
import android.webkit.WebView;
import android.app.ActionBar;
import android.app.Activity;
import android.view.MenuItem;


public class HelpActivity extends Activity {
    
    WebView helpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        
        // Allow going back with ActionBar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        helpView = (WebView)findViewById(R.id.helpWeb);
        helpView.loadUrl("file:///android_asset/Help.html");
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
    
}
