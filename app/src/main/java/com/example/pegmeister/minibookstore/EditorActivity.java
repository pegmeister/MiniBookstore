package com.example.pegmeister.minibookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pegmeister.minibookstore.data.BookContract.BookEntry;

/**
 * Allow user to create a new product or edit an existing product.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the book data loader */
    private static final int EXISTING_BOOK_LOADER = 0;

    /** Content URI for the existing book (null if it's a new book) */
    public static Uri mCurrentBookUri;

    /** EditText fields to enter book's name, price, quantity and supplier's name and phone number */
    private EditText nameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierEditText;
    private EditText supplierPhoneEditText;

    /** Boolean flag that keeps track of whether the book has been edited (true) or not (false) */
    private boolean bookHasChanged = false;

    /** boolean flag that checks if all required fields are entered or valid. */
    private boolean requiredFields = false;

    String supplierNumber;
    EditText numberText;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are
     * modifying the view, and we change the bookHasChange boolean to true.
     */

    private View.OnTouchListener touchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent){
            bookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was unused to launch this activity, in order to figure out
        // if we're creating a new book or editing an existing book.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // if the intent DOES NOT contain a book content URI, then it is creating a new book
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar to say "Add a book"
            setTitle(getString(R.string.editor_add_a_book));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing book, so change app to say "Edit Book"
            setTitle(getString(R.string.editor_edit_a_book));

            // Initialize a loader to read the book data from the database and display the current
            //values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER,null,this);
        }

        // Find all relevant views that we will need to read user input from
        nameEditText = findViewById(R.id.add_product_name);
        priceEditText = findViewById(R.id.add_price);
        quantityEditText = findViewById(R.id.add_quantity);
        supplierEditText = findViewById(R.id.add_supplier);
        supplierPhoneEditText = findViewById(R.id.add_supplier_phone);

        // Decrement button setup
        Button decrement = findViewById(R.id.decrement);

        // decrease quantity when user clicks the button
        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.valueOf(quantityEditText.getText().toString());

                if (quantity >= 1) {
                    quantity = quantity - 1;
                }
                quantityEditText.setText(Integer.toString(quantity));
            }
        });

        // Increment button setup
        Button increment = findViewById(R.id.increment);

        // increase quantity when user clicks the button
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(quantityEditText.getText().toString());

                if (quantity >=0){
                    quantity = quantity + 1;
                }
                quantityEditText.setText(Integer.toString(quantity));
            }
        });

        // setup intent to ACTION_DIAL when user press "CONTACT SUPPLIER" button
        Button contactButton = findViewById(R.id.contact_supplier);
        numberText = findViewById(R.id.add_supplier_phone);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supplierNumber = numberText.getText().toString().trim();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel: "+ supplierNumber));
                if (callIntent.resolveActivity(getPackageManager()) != null){
                startActivity(callIntent);
            }
        }
        });

        // Setup On TouchListener on all the input fields, so we can determine if the user has touched
        // or modified them. This will let us know if there are unsaved changes or not, if the user
        // tries to leave the editor without saving
        nameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierEditText.setOnTouchListener(touchListener);
        supplierPhoneEditText.setOnTouchListener(touchListener);
    }

    /**
     * Get user input from editor and save book into database
     */
    private boolean saveBook() {
        // Read from input field, get all data from the EditText fields
        // use trim to eliminate leading or trailing white space
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplierString = supplierEditText.getText().toString().trim();
        String phoneString = supplierPhoneEditText.getText().toString().trim();

        // Verify if this is a new book and all required fields are blank.
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(phoneString)) {
            // Since no fields were modified, we can return early without creating a new book,
            // No need to create ContentValues and perform any ContentValues operations.
            return requiredFields;
        }

        // Create a ContentValues object where column names are the keys and product attributes from
        // the editor are the values; then save them in a ContentValues object
        ContentValues values = new ContentValues();

        // If name field isn't populated by the user, toast will display prompting user to include before leaving the editor.
        if(TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.enter_name_req), Toast.LENGTH_SHORT).show();
            return requiredFields;
        } else {
            values.put(BookEntry.COLUMN_PRODUCT_NAME, nameString);
            }

        // If price field isn't populated by the user, toast will display prompting user to include before leaving the editor.
            if (TextUtils.isEmpty(priceString)) {
                Toast.makeText(this, getString(R.string.enter_price_req), Toast.LENGTH_SHORT).show();
                return requiredFields;
            } else {
                values.put(BookEntry.COLUMN_PRICE, priceString);
            }

            // If supplier name isn't populated by the user, toast will display prompting user to include before leaving the editor.
            if (TextUtils.isEmpty(supplierString)) {
                Toast.makeText(this, getString(R.string.enter_supplier_req), Toast.LENGTH_SHORT).show();
                return requiredFields;
            } else {
                values.put(BookEntry.COLUMN_SUPPLIER, supplierString);
            }

        // If supplier phone number isn't populated by the user, toast will display prompting user to include
        // and must be a valid 10-digit number format.
            if (TextUtils.isEmpty(phoneString) || phoneString.length() < 10) {
                Toast.makeText(this, getString(R.string.enter_phone_req), Toast.LENGTH_SHORT).show();
                return requiredFields;
            } else {
                values.put(BookEntry.COLUMN_SUPPLIER_PHONE, phoneString);
            }

        // Create a ContentValues object where column names are the keys and product attributes from
        // the editor are the values; then save them in a ContentValues object
            values.put(BookEntry.COLUMN_QUANTITY, quantityString);

        // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not.
        if (mCurrentBookUri == null) {
            // This is a new book, so insert a new book into the provider, returning the content URI
            // for the new book.
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast message.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
            // and pass in the new ContentValues. Passin null for the selection and selection args
            // because mCurrentBookUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0){
                // If no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this,getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        requiredFields = true;
        return requiredFields;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file
        // This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some Menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hid the "Delete" menu item.
        if (mCurrentBookUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on an menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // save product to database, trigger when the Save button is pressed
                saveBook();
                if (requiredFields == true){
                //Exit activity
                finish();}
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}
                if (!bookHasChanged){
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn user.
        // Create a click listener to handle the user confirming that changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                    // User clicked "Discard" button, navigate to parent activity.
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                }
                };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed(){
        // If the book hasn't changed, continue with handling back button press
        if(!bookHasChanged){
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn user.
        // Create a click listener to handle the user confirming that changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there were unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a project that contains
        // all columns from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER,
                BookEntry.COLUMN_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a backgound thread
        return new CursorLoader(this,           // Parent activity context
                mCurrentBookUri,                        // Query the content URI for the current book
                projection,                             // columns to unclude in the resulting Cursor
                null,                          // No selection clause
                null,                      // No selection arguments
                null);                        // default sort order
        }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor.
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it.
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);

            // Extract out the value from the Cursor fro the given column index
            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database.
            nameEditText.setText(name);
            priceEditText.setText(Double.toString(price));
            quantityEditText.setText(Integer.toString(quantity));
            supplierEditText.setText(supplier);
            supplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidate, clear out all the data from the input field.
        nameEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        supplierEditText.setText("");
        supplierPhoneEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners for the
        // positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User click the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog and continue editing
                if (dialog !=null){
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database
     */
    private void deleteBook(){
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null){
            // Call the ContentResolver to delete the book at the given content URI
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri,null, null);

            // show a toast message depending on whether or not the deletion was successful
            if (rowsDeleted == 0){
                Toast.makeText(this,getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, deletion was successful and we can display a toast.
                Toast.makeText(this,getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}
