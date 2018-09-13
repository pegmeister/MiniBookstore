package com.example.pegmeister.minibookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.pegmeister.minibookstore.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {

    /**
     * Tag for the log message
     */
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the books table
     */
    private static final int BOOKS = 100;

    /**
     * URI matcher code for the content URI for a single book in the books table
     */
    private static final int BOOK_ID = 101;

    /**
     * URI matcher object to match a content URI to a corresponding code. The input passed
     * into the constructor represents the code to return for the root URI
     * It's common to use NO_MATCH as the input for this case
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer, this is run the first time anything is called from this class.
    static {
        // This calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return when
        // a match is found.

        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    /**
     * Database helper object
     */
    private BookDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments
     * and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        // Call UriMatcher.match(Uri) and pass in a Uri, which will return the corresponding integer code
        // (if it matched a valid pattern) or will indicate there is no match.
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor so we now what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert a new product into the database with the given content values.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not support for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        // Check that name is not null
        String name = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        // Check that the price is valid
        Double price = values.getAsDouble(BookEntry.COLUMN_PRICE);
        if (price != null && price < 0.00) {
            throw new IllegalArgumentException("Product requires a valid price");
        }
        // Check that the quantity is valid
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }
        // Check that the supplier name is not null
        String supplierName = values.getAsString(BookEntry.COLUMN_SUPPLIER);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product requires a valid supplier name");
        }
        // Check that the supplier phone number is not null
        String supplierNumber = values.getAsString(BookEntry.COLUMN_SUPPLIER_PHONE);
        if (supplierNumber == null || supplierNumber.length() < 10) {
            throw new IllegalArgumentException("Product requires a valid 10-digit number");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new book with the given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the book content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row, return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentVales.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            // For the BOOK_ID code, extract out the ID from the URI, so we know which row to update
            // selection will be "_=?" and selectionArgs will be a String array containing the actual ID
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update books in the database with the given content values. Apply changes to rows specified in
     * the selection and selectionArgs (which could be 0 or 1 0r more books). Return the number of rows
     * that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link BookEntry#COLUMN} keys are present, check that the given value is
        // not null.
        if (values.containsKey(BookEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_PRICE)) {
            Double price = values.getAsDouble(BookEntry.COLUMN_PRICE);
            if (price != null && price < 0.00) {
                throw new IllegalArgumentException("Product requires a valid price");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires a valid quantity");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER)) {
            String supplier = values.getAsString(BookEntry.COLUMN_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Product requires a valid supplier name");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_PHONE)) {
            String supplierNumber = values.getAsString(BookEntry.COLUMN_SUPPLIER_PHONE);
            if (supplierNumber == null || supplierNumber.length() < 10) {
                throw new IllegalArgumentException("Product requires a valid 10-digit number");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the given
        // URI has changed.
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection Args
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection Args
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URL
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not support for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given
        // URI has deleted.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
