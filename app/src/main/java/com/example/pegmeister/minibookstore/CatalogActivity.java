package com.example.pegmeister.minibookstore;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.pegmeister.minibookstore.data.BookContract.BookEntry;
import com.example.pegmeister.minibookstore.data.BookDbHelper;

/**
 * Displays list of products that were entered and stored in the app.
 */

public class CatalogActivity extends AppCompatActivity {

    /**
     * Database helper that will provide us access to the database
     */
    private BookDbHelper mDbHelper;

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

        // To access our database, we instantiate our subclass of SQLiteOpenHelper and pass
        // the context, which is current activity.
        mDbHelper = new BookDbHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen about the database
     */
    private void displayDatabaseInfo() {
        // Create and/or open database to read from it, like .open command used in Terminal
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Create a string projection with table name fields
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER,
                BookEntry.COLUMN_SUPPLIER_PHONE,
        };

        // Perform a query on the books table
        Cursor cursor = db.query(
                BookEntry.TABLE_NAME,               // the table to query
                projection,                         // the columns to return
                null,                      // the columns for the WHERE clause
                null,                   // the values for the WHERE clause
                null,                       // don't group the rows
                null,                        // don't filter by row groups
                null);                       // the sort order

        TextView displayView = findViewById(R.id.text_view_product);

        try {
            displayView.setText("The books table contains " + cursor.getCount() + " products.\n\n");
            displayView.append(BookEntry._ID + " | " +
                    BookEntry.COLUMN_PRODUCT_NAME + " | " +
                    BookEntry.COLUMN_PRICE + " | " +
                    BookEntry.COLUMN_QUANTITY + " | " +
                    BookEntry.COLUMN_SUPPLIER + " | " +
                    BookEntry.COLUMN_SUPPLIER_PHONE + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);

            // Iterate through all the returned rows in the cursor abd display values onscreen
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of word at the current row
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentPrice = cursor.getString(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplier = cursor.getString(supplierColumnIndex);
                String currentPhone = cursor.getString(phoneColumnIndex);

                displayView.append(("\n" + currentID + " | " +
                        currentName + " | " +
                        currentPrice + " | " +
                        currentQuantity + " | " +
                        currentSupplier + " | " +
                        currentPhone));
            }
        } finally {
            // Always close the cursor when finished reading from it. This releases all it's resources
            // and makes it invalid.
            cursor.close();
        }

    }
}
