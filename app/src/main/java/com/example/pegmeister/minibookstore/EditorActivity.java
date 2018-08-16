package com.example.pegmeister.minibookstore;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pegmeister.minibookstore.data.BookContract;
import com.example.pegmeister.minibookstore.data.BookDbHelper;

/**
 * Allow user to create a new product or edit an existing product.
 */
public class EditorActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierEditText;
    private EditText supplierPhoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        nameEditText = findViewById(R.id.add_product_name);
        priceEditText = findViewById(R.id.add_price);
        quantityEditText = findViewById(R.id.add_quantity);
        supplierEditText = findViewById(R.id.add_supplier);
        supplierPhoneEditText = findViewById(R.id.add_supplier_phone);
    }

    /**
     * Get user input from editor and save new product into database
     */
    private void insertProduct() {
        // Read from input field, get all data from the EditText fields
        // use trim to eliminate leading or trailing white space
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String supplierString = supplierEditText.getText().toString().trim();
        String phoneString = supplierPhoneEditText.getText().toString().trim();

        // Create database helper
        BookDbHelper mDbHelper = new BookDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContenValues object where column names are the keys and product attributes from
        // the editor are the values; then save them in a ContentValues object
        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(BookContract.BookEntry.COLUMN_PRICE, priceString);
        values.put(BookContract.BookEntry.COLUMN_QUANTITY, quantity);
        values.put(BookContract.BookEntry.COLUMN_SUPPLIER, supplierString);
        values.put(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE, phoneString);

        // Insert a new row for product in the database, returing the ID of that new row
        // (insert the ContentValues object into the books table)
        long newRowId = db.insert(BookContract.BookEntry.TABLE_NAME, null, values);

        // Show a toast message if product was insert successfully
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion
            Toast.makeText(this, "Error with saving product", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise the insertion was successful and display a toast with the row ID.
            Toast.makeText(this, "Product saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file
        // This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on an menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // save product to database, trigger when the Save button is pressed
                insertProduct();
                //Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
