package fr.commons.generique.controller.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.commons.generique.controller.utils.Log;
import fr.commons.generique.model.db.IObjetBdd;

/**
 * @author Zapagon
 */
public abstract class TableDAO<B extends IObjetBdd> {

	/**
	 * Open database
	 *
	 * @return SQLiteDatabase
	 */
	private SQLiteDatabase openRead() throws SQLiteException {
		return DBHelper.getInstance().getReadableDatabase();
	}

	/**
	 * Open database
	 *
	 * @return SQLiteDatabase
	 */
	private SQLiteDatabase openWrite() throws SQLiteException {
		return DBHelper.getInstance().getWritableDatabase();
	}

	/**
	 * Close database
	 */
	private void close() {
		DBHelper.getInstance().close();
	}

	/**
	 * @return the table name
	 */
	public abstract String getNomTable();

	public void creer(B bo) {
		insertOrReplaceImpl(bo);
	}

	public B get(B bo) {
		if (bo == null) return bo;
		B result = bo;
		ContentValues cv = getKeyValues(bo);
		try (SQLiteDatabase db = this.openRead()) {
			db.beginTransaction();
			String query = getSelectAllQuery(getWhereClause(bo));
			Cursor c = db.rawQuery(query, null);
			result = convert(c);
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		return result;
	}

	public void update(B bo) {
		insertOrReplaceImpl(bo);
	}

	public void delete(B bo) {
		try (SQLiteDatabase db = this.openWrite()) {
			db.beginTransaction();
			db.delete(getNomTable(), getWhereClause(bo), null);
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}

	private void insertOrReplaceImpl(final B bo) {
		insertOrReplaceImpl(Collections.singletonList(bo));
	}

	/**
	 * Add business objects to database
	 *
	 * @param bos business objects
	 */
	protected void insertOrReplaceImpl(final List<B> bos) {
		if (bos != null && bos.size() > 0) {
			try (SQLiteDatabase db = this.openWrite()) {
				db.beginTransaction();
				for (B bo : bos) {
					if (bo != null) {
						long id = replaceOrThrow(db, bo);
						if (id == -1) {
							throw new SQLiteException(
									"Error inserting following object : " + bo);
						}
					}
				}
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		}
	}

	protected long replaceOrThrow(SQLiteDatabase db, B bo){
		long id = db.replaceOrThrow(getNomTable(), null,
				getContentValues(bo));
		return id;
	}

	/**
	 * getContentValues from given bo. Use by default to insert or replace
	 * values
	 *
	 * @return ContentValues
	 */
	protected abstract ContentValues getContentValues(B bo);

	protected abstract ContentValues getKeyValues(B bo);

	protected String getWhereClause(B bo) {
		List<String> lstStr = new ArrayList<>();
		ContentValues cv = getKeyValues(bo);
		for (String k : cv.keySet()) {
			String v = cv.getAsString(k);
			lstStr.add(k + "=" + v);
		}
		return String.join(" AND ", lstStr);
	}

	@NonNull
	public List<B> selectAll() {
		return selectAll(getSelectAllQuery(null));
	}

	@NonNull
	public List<B> selectAll(final String query) {
		// call database
		Cursor c = openRead().rawQuery(query, null);

		List<B> results = new ArrayList<>();

		// foreach entries
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			B bo = convert(c);
			if (bo != null) {
				results.add(bo);
			}
		}

		c.close();
		close();

		return results;
	}

	protected String getSelectAllQuery(final String whereClause) {
		final String formattedQuery;
		if (whereClause != null && !whereClause.isEmpty()) {
			formattedQuery = String.format("SELECT * FROM %s WHERE %s",
					getNomTable(), whereClause);
		} else {
			formattedQuery = String.format("SELECT * FROM %s", getNomTable());
		}

		Log.debug(formattedQuery);
		return formattedQuery;
	}

	/**
	 * Convert an entry at position of the given cursor to object
	 *
	 * @param c Cursor
	 * @return model object convert from entry at cursor position
	 */
	protected abstract B convert(Cursor c);

}