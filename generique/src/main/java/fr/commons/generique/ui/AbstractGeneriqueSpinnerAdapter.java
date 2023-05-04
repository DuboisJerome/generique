package fr.commons.generique.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fr.commons.generique.R;
import fr.commons.generique.model.db.AbstractObjetBddAvecId;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractGeneriqueSpinnerAdapter<T extends AbstractObjetBddAvecId> extends ArrayAdapter<T> {

	@Getter
	@Setter
	private List<T> list = new ArrayList<>();

	public AbstractGeneriqueSpinnerAdapter(@NonNull Context context) {
		super(context, R.layout.item_spinner);
		setDropDownViewResource(R.layout.dropdown_item_spinner);
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public T getItem(int position) {
		return this.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView text = (TextView) super.getView(position, convertView, parent);
		text.setText(getLibelle(getItem(position)));
		return text;
	}

	@Override
	public View getDropDownView(int position, @androidx.annotation.Nullable View convertView, @NonNull ViewGroup parent) {
		TextView text = (TextView) super.getDropDownView(position, convertView, parent);
		text.setText(getLibelle(getItem(position)));
		return text;
	}

	protected abstract String getLibelle(T t);

	public int getPosition(long id) {
		T item = this.list.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
		return this.list.indexOf(item);
	}
}