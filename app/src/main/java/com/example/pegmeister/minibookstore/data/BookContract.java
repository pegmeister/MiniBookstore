package com.example.pegmeister.minibookstore.data;

import android.provider.BaseColumns;

// Create a separate Contract class to store all constant values to use throughout the app
public class BookContract {

    // To prevent anyone from accidentally instantiating the contract class,
    // give it an empty constructor
    private BookContract() {
    }

    /**
     * Inner class that defines constant values for the product database table
     * Each entry in the table represents a single product
     */
    public static final class BookEntry implements BaseColumns {

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
