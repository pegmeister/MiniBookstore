package com.example.pegmeister.minibookstore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.pegmeister.minibookstore.data.BookContract.BookEntry;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the book data loader
     */
    private static final int BOOK_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    BookCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the book data
        ListView bookListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        //Setup an Adapter to create a list item for each row of book data in the Cursor
        cursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(cursorAdapter);

        // Setup the item click listener
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create a new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific book that was clicked on, by
                // appending the ID onto the {@link BookEntry#CONTENT_URI)
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                // set the URI on the data field of the intent
                intent.setData(currentBookUri);

                // Launch the {@link EditorActivity} to display the data for the current book.
                startActivity(intent);
            }
        });
        // Kick off the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded bood data into the database. For debugging purposes only
     */
    private void insertBook() {
        // Restock populate item
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, getString(R.string.restock_book_name));
        values.put(BookEntry.COLUMN_PRICE, getString(R.string.restock_book_price));
        values.put(BookEntry.COLUMN_QUANTITY, getString(R.string.restock_book_quantity));
        values.put(BookEntry.COLUMN_SUPPLIER, getString(R.string.restock_book_supplier));
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, getString(R.string.restock_book_phone));

        getContentResolver().insert(BookEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all entries in the database
     */
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.v("CatalogActvity", rowsDeleted + " rows deleted from book database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the menu_catalog.xml file, this adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Restock product" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a project that specifies the columns from the table we care about.
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,               // Parent activity context
                BookEntry.CONTENT_URI,                      // Provider content URI to query
                projection,                                 // Columns to include in the resulting Cursor
                null,                              // No selection clause
                null,                          // No selection arguments
                null);                            // default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link BookCursorAdapter} with this new cursor containing updated book data
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        cursorAdapter.swapCursor(null);
    }
}
