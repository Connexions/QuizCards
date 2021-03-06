/**
 * Copyright (c) 2012 Rice University
 *
 * This software is subject to the provisions of the GNU Lesser General
 * Public License Version 2.1 (LGPL).  See LICENSE.txt for details.
 */

package org.cnx.android.quizcards.database;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static org.cnx.android.quizcards.Constants.*;

public class DeckProvider extends ContentProvider {

    private CardDatabaseOpenHelper helper;
    public static final Uri CONTENT_URI = Uri
            .parse("content://org.cnx.android.quizcards.DeckProvider");

    @Override
    public boolean onCreate() {
        helper = new CardDatabaseOpenHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    	SQLiteDatabase cardsdb = helper.getWritableDatabase();
        return cardsdb.delete(DECKS_TABLE, selection, null);
        
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Inserts values into the cards table. TODO: Modify to return the right
     * URI, handle duplicates.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // Check to see if this module is already a deck
        String newID = values.getAsString(MODULE_ID);
        Cursor c = db.query(DECKS_TABLE, new String[] { MODULE_ID }, MODULE_ID
                + "='" + newID + "'", null, null, null, null);
        if (c.getCount() > 0)
            return null;
        c.close();
        
        // Always have a default high score of 0
        values.put(HIGH_SCORE, 0);

        // If not, insert it in.
        long rowNum = db.insertOrThrow(DECKS_TABLE, null, values);

        if (rowNum > 0) {
            Uri deckUri = ContentUris.withAppendedId(CONTENT_URI, rowNum);
            getContext().getContentResolver().notifyChange(deckUri, null);
            return deckUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SQLiteDatabase cardsdb = helper.getReadableDatabase();

        return cardsdb.query(DECKS_TABLE, projection, selection,
                selectionArgs, null, null, sortOrder);

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        
        SQLiteDatabase cardsdb = helper.getWritableDatabase();
        return cardsdb.update(DECKS_TABLE, values, selection, selectionArgs);
        
    }

}
