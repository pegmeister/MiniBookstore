package com.example.pegmeister.minibookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pegmeister.minibookstore.data.BookContract.BookEntry;

import java.text.DecimalFormat;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /*flags */);
    }

    /**
     * Makes a new blank list item view, no data is set to the view yet.
     * @param context app context
     * @param cursor  The cursor from which to get data, it is already moved to the correct position
     * @param parent  The parent to which the new view is attached to
     * @return        The newly created list item view
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    /**
     * it binds the book data to the given list item layout
     * @param view      Existing view, returned earlier by newView() methold
     * @param context   app context
     * @param cursor    the cursor from which to get the data, it is already moved to the correct row
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        final TextView quantityTextView = view.findViewById(R.id.quantity);

        // Find the columns of product attributes that we are interested in.
        final int bookIdColumnIndex = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        // Read the current product attributes from the Cursor
        String productName = cursor.getString(nameColumnIndex);
        Double productPrice = cursor.getDouble(priceColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);

        // Update Textviews with attributes for the current product.
        nameTextView.setText(productName);
        DecimalFormat bkp = new DecimalFormat("#.00");
        priceTextView.setText("Price: $" + bkp.format(productPrice).toString());
        quantityTextView.setText(Integer.toString(productQuantity));

       final Button saleButton = view.findViewById(R.id.sale);

       int currentId = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));

       final Uri contentUri = Uri.withAppendedPath(BookEntry.CONTENT_URI, Integer.toString(currentId));

       saleButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               int quantity = Integer.valueOf(quantityTextView.getText().toString());

               if (quantity > 0){
                   quantity = quantity - 1;
               } else {
                   Toast.makeText(context, R.string.book_sale_failed, Toast.LENGTH_SHORT).show();
               }
               ContentValues values = new ContentValues();
               values.put(BookEntry.COLUMN_QUANTITY, quantity);

               context.getContentResolver().update(contentUri, values, null, null);
           }
       });

    }

}
