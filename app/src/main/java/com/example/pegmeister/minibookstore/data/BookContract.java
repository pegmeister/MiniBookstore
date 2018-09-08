package com.example.pegmeister.minibookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

// Create a separate Contract class to store all constant values to use throughout the app
public class BookContract {

    // To prevent anyone from accidentally instantiating the contract class,
    // give it an empty constructor
    private BookContract() {
    }

    /**
     * The Content Authority is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for
     * the content authority is the package name for the app, which is guaranteed to be unique on device
     */
    public static final String CONTENT_AUTHORITY = "com.example.pegmeister.minibookstore";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Stores the path for each of the tables which will be appended to the base content URI */
    public static final String PATH_BOOKS = "books";

    /**
     * Inner class that defines constant values for the product database table
     * Each entry in the table represents a single product
     */
    public static final class BookEntry implements BaseColumns {

        /** The content URI to access the book data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of books
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * The MIME Type of the {@link @CONTENT_URI} for a single book
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + '/' + PATH_BOOKS;

        // Name of the database table
        public final static String TABLE_NAME = "books";

        // Unique ID for each product, Type: INTEGER
        public final static String _ID = BaseColumns._ID;

        // Name of the product, Type TEXT
        public final static String COLUMN_PRODUCT_NAME = "name";

        // Price of the product, Type: TEXT
        public final static String COLUMN_PRICE = "price";

        // Quantity of the product, Type: INTEGER
        public final static String COLUMN_QUANTITY = "quantity";

        // Supplier of the product, Type: TEXT
        public final static String COLUMN_SUPPLIER = "supplier";

        // Supplier phone number, Type: TEXT
        public final static String COLUMN_SUPPLIER_PHONE = "supplier_phone_number";

    }
}
