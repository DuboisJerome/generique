package fr.commons.generique.controller.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import fr.commons.generique.controller.db.TableDAO;
import fr.commons.generique.controller.utils.DatabaseUtils;
import fr.commons.generique.model.db.AbstractObjetBddAvecId;


public abstract class AbstractObjetBddAvecIdDAO<T extends AbstractObjetBddAvecId> extends TableDAO<T> {

	public static final String COL_ID = "ID";

	public String getNomColId(){
		return COL_ID;
	}

	protected long getId(Cursor c) {
		return DatabaseUtils.getLongCheckNullColumn(c, COL_ID);
	}

	@Override
	protected ContentValues getContentValues(T bo) {
		ContentValues cv = new ContentValues();
		if (!bo.isNew()) {
			cv.put(getNomColId(), bo.getId());
		}
		return cv;
	}

	@Override
	protected ContentValues getKeyValues(T bo) {
		ContentValues cv = new ContentValues();
		cv.put(getNomColId(), bo.getId());
		return cv;
	}

	public T getById(long id) {
		List<T> results = selectAll(getSelectAllQuery(getNomColId() + "=" + id));
		return results.isEmpty() || results.get(0) == null ? null : results.get(0);
	}

}