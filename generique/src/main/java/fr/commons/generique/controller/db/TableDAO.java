package fr.commons.generique.controller.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
		creerAll(List.of(bo), SQLiteDatabase.CONFLICT_IGNORE);
	}
	public void creer(B bo, int conflictAlgo) {
		creerAll(List.of(bo), conflictAlgo);
	}

	protected void creerAll(final List<B> bos, int conflictAlgo) {
		if (bos != null && bos.size() > 0) {
			SQLiteDatabase db = this.openWrite();
			db.beginTransaction();
			for (B bo : bos) {
				if (bo != null) {
					db.insertWithOnConflict(getNomTable(), null,
							getContentValues(bo), conflictAlgo);
				}
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}


	public B get(B bo) {
		if (bo == null) return null;
		B result;
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
		updateAll(List.of(new Pair<>((B)null, bo)));
	}

	public void update(B boOld, B boNew){
		updateAll(List.of(new Pair<>(boOld, boNew)));
	}

	protected void updateAll(final List<Pair<B,B>> bos) {
		if (bos != null && bos.size() > 0) {
			SQLiteDatabase db = this.openWrite();
			db.beginTransaction();
			for (Pair<B,B> pair : bos) {
				if (pair != null) {

					ContentValues cv;
					if(pair.first != null){
						// Update only modified fields
						cv = getContentValuesToUpdate(pair.first, pair.second);
					} else {
						// Update all fields
						cv = getContentValues(pair.second);
					}
					ContentValues keys = getKeyValues(pair.second);
					String where = buildPartReqArgs(keys, e -> e.getKey() +"=?", " AND ");
					String[] whereArgs =  toArgs(keys);
					db.update(getNomTable(), cv, where, whereArgs);
				}
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}

	public void delete(B bo) {
		SQLiteDatabase db = this.openWrite();
		db.beginTransaction();
		db.delete(getNomTable(), getWhereClause(bo), null);
		db.setTransactionSuccessful();
		db.endTransaction();
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
		ContentValues cv = getKeyValues(bo);
		return buildPartReqArgs(cv, e -> e.getKey() +"="+e.getValue(), " AND ");
	}

	protected String getWhereClauseWithArgs(B bo){
		ContentValues cv = getKeyValues(bo);
		return cv.valueSet().stream()
				.map(Map.Entry::getKey)
				.map(k -> k +"=?")
				.collect(Collectors.joining(" AND "));
	}

	protected String[] getWhereArgs(B bo){
		ContentValues cv = getKeyValues(bo);
		return toArgs(cv);
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
		String values = buildPartReqArgs(cv, Map.Entry::getValue,",");
		var sb = new StringBuilder();
		sb.append("INSERT INTO ").append(getNomTable()).append(" (");
		sb.append(String.join(",",cv.keySet()));
		sb.append(") VALUES (");
		sb.append(values);
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

	public boolean isNeedUpdate(B oldBo, B newBo){
		return getContentValuesToUpdate(oldBo, newBo).size() > 0;
	}

	public String buildReqUpdate(B newBo){
		ContentValues cvNew = getContentValues(newBo);
		// on supprime les clés des valeurs à update
		getKeyValues(newBo).keySet().forEach(cvNew::remove);

		return buildReqUpdate(newBo, cvNew);
	}

	private ContentValues getContentValuesToUpdate(B oldBo, B newBo){

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
		return cvNew;
	}

	public String buildReqUpdate(B oldBo, B newBo) {
		return buildReqUpdate(newBo, getContentValuesToUpdate(oldBo, newBo));
	}

	public String buildReqUpdate(B newBo, ContentValues cvNew){
		if(cvNew.size() <= 0){
			return "";
		}
		var sb = new StringBuilder();
		sb.append("UPDATE ").append(getNomTable()).append(" SET ");
		String values = buildPartReqArgs(cvNew, e -> e.getKey() + "="+ e.getValue(),",");
		sb.append(values);
		sb.append(" WHERE ");
		sb.append(getWhereClause(newBo));
		sb.append(";");
		return sb.toString();
	}

	public static String[] toArgs(ContentValues cv){
		List<String> lstVals = new ArrayList<>();
		for (String k : cv.keySet()) {
			lstVals.add(cv.getAsString(k));
		}
		return lstVals.toArray(new String[0]);
	}

	public static String buildPartReqArg(ContentValues cv, String k){
		Object vObj = cv.get(k);
		String v = cv.getAsString(k);
		if (vObj instanceof String) {
			v = DatabaseUtils.toStringWithQuotes(v);
		} else if (vObj instanceof Enum<?> en){
			v = DatabaseUtils.toStringWithQuotes(en.name());
		}
		return v;
	}

	public static String buildPartReqArgs(ContentValues cv, Function<Map.Entry<String, String>, String> valToArg, String strJoin){
		return cv.valueSet().stream()
				.map(e -> Map.entry(e.getKey(), buildPartReqArg(cv, e.getKey())))
				.map(valToArg)
				.collect(Collectors.joining(strJoin));
	}

}