package fr.commons.generique.controller.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.commons.generique.controller.utils.DatabaseUtils;
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
	 * @return the table name
	 */
	public abstract String getNomTable();

	public void creer(B bo) {
		insertOrReplaceImpl(bo);
	}

	public B get(B bo) {
		if (bo == null) return null;
		B result = bo;
		ContentValues cv = getKeyValues(bo);
		SQLiteDatabase db = this.openRead();

		db.beginTransaction();
		String query = getSelectAllQuery(getWhereClause(bo));
		Cursor c = db.rawQuery(query, null);
		if(c.moveToFirst()){
			result = convert(c);
		} else {
			result = null;
		}
		db.setTransactionSuccessful();
		db.endTransaction();

		return result;
	}

	public void update(B bo) {
		insertOrReplaceImpl(bo);
	}

	public void delete(B bo) {
		SQLiteDatabase db = this.openWrite();
		db.beginTransaction();
		db.delete(getNomTable(), getWhereClause(bo), null);
		db.setTransactionSuccessful();
		db.endTransaction();
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
			SQLiteDatabase db = this.openWrite();
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

		return results;
	}

	public long count(){
		SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();

		String sql = "SELECT COUNT(*) as NB FROM "+getNomTable();
		Cursor c = db.rawQuery(sql, null);
		// foreach entries
		long count = 0;
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			count = DatabaseUtils.getLongCheckNullColumn(c, "NB");
		}
		c.close();
		return count;
	}

	public String getSelectAllQuery(final String whereClause) {
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

	public String buildReqInsert(B bo){
		ContentValues cv = getContentValues(bo);
		return buildReqInsert(cv);
	}

	public String buildReqInsert(ContentValues cv){
		Map<String, String> map =new HashMap<>();
		cv.keySet().forEach(k -> {
			String nomCol = k;
			String valCol = cv.getAsString(k);
			map.put(nomCol, valCol);
		});
		return buildReqInsert(map);
	}


	public String buildReqInsert(Map<String, String> map){
		var sb = new StringBuilder();
		sb.append("INSERT INTO ").append(getNomTable()).append(" (");
		sb.append(String.join(",",map.keySet()));
		sb.append(") VALUES (");
		sb.append(String.join(",",map.values()));
		sb.append(");");
		return sb.toString();
	}

	public String buildReqDelete(B bo){
		var sb = new StringBuilder();
		sb.append("DELETE FROM ").append(getNomTable()).append(" WHERE ");
		sb.append(getWhereClause(bo));
		sb.append(";");
		return sb.toString();
	}
	public String buildReqUpdate(B newBo){
		ContentValues cvNew = getContentValues(newBo);
		// on supprime les clés des valeurs à update
		getKeyValues(newBo).keySet().forEach(cvNew::remove);

		return buildReqUpdate(newBo, cvNew);
	}

	public String buildReqUpdate(B oldBo, B newBo){
		ContentValues cvOld = getContentValues(oldBo);
		ContentValues cvNew = getContentValues(newBo);

		List<String> lstColToNotUpdate = new ArrayList<>();
		for(String nomCol : cvNew.keySet()){
			String oldVal = cvOld.getAsString(nomCol);
			String newVal = cvNew.getAsString(nomCol);
			// Si la valeur ne change pas, on la retire
			if((oldVal != null && oldVal.equals(newVal)) || (oldVal == null && newVal == null)){
				lstColToNotUpdate.add(nomCol);
			}
		}
		// Après la boucle pour ne pas supprimer en meme temps que l'on parcours les clés
		lstColToNotUpdate.forEach(cvNew::remove);
		// On supprime les clés des valeurs à update, elles devraient de toute façon être identiques et donc dans la liste précédente
		getKeyValues(newBo).keySet().forEach(cvNew::remove);

		return cvNew.size() > 0 ? buildReqUpdate(newBo, cvNew) : "";
	}

	public String buildReqUpdate(B newBo, ContentValues cvNew){
		var sb = new StringBuilder();
		sb.append("UPDATE ").append(getNomTable()).append(" SET ");
		sb.append(cvNew.keySet().stream().map(k -> k + " = "+cvNew.getAsString(k)).collect(Collectors.joining(",")));
		sb.append(" WHERE ");
		sb.append(getWhereClause(newBo));
		sb.append(";");
		return sb.toString();
	}
}