package fr.commons.generique.ui;

import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import fr.commons.generique.controller.dao.AbstractObjetBddAvecIdDAO;

public abstract class AbstractGeneriqueAdapter<T, VH extends AbstractGeneriqueViewHolder<T>> extends RecyclerView.Adapter<VH> implements Filterable {

	protected List<T> currentList;
	protected List<T> originalList;
	private BiConsumer<View,Integer> binder;
	private T seletecItem = null;
	protected Comparator<T> comparator;

	public AbstractGeneriqueAdapter(List<T> l) {
		this(l, null);
	}

	public AbstractGeneriqueAdapter(List<T> l, Comparator<T> c) {
		setList(l);
		this.comparator= c;
	}

	public void setList(List<T> l){
		this.originalList = l;
		this.currentList = new ArrayList<>(l);
	}

	public boolean addItem(T t) {
		int idxItemExistant = this.originalList.indexOf(t);
		boolean isOk;
		if(idxItemExistant != -1){
			this.originalList.set(idxItemExistant, t);
			isOk = true;
		} else {
			isOk = this.originalList.add(t);
		}
		if(comparator!=null){
			this.originalList.sort(comparator);
		}

		int idxItemExistantCurrent = this.currentList.indexOf(t);
		if(idxItemExistantCurrent != -1){
			this.currentList.set(idxItemExistantCurrent, t);
			if(comparator!=null){
				this.currentList.sort(comparator);
				idxItemExistantCurrent = this.currentList.indexOf(t);
			}
			notifyItemChanged(idxItemExistantCurrent);
		} else {
			this.currentList.add(t);
			if(comparator!=null){
				this.currentList.sort(comparator);
				idxItemExistantCurrent = this.currentList.indexOf(t);
			} else {
				idxItemExistantCurrent = this.currentList.size()-1;
			}
			notifyItemInserted(idxItemExistantCurrent);
		}
		return isOk;
	}

	public boolean removeItem(T t) {
		int position = this.originalList.indexOf(t);
		if(position != -1){
			boolean isOk = this.originalList.remove(t);
			this.currentList.remove(t);
			notifyItemRemoved(position);
			return isOk;
		}
		return false;
	}

	public void setBinderView(BiConsumer<View,Integer> binder) {
		this.binder = binder;
	}

	@Override
	public int getItemCount() {
		return this.currentList.size();
	}

	public boolean isSelected(T t) {
		return t != null && t.equals(this.seletecItem);
	}

	public void setSelected(T t) {
		this.seletecItem = t;
	}

	public T getSelected() {
		return this.seletecItem;
	}

	public T getItemAt(int position) {
		return this.currentList.get(position);
	}

	@Override
	public void onBindViewHolder(@NonNull VH viewHolder, int i) {
		viewHolder.bind(this.binder, getItemAt(viewHolder.getBindingAdapterPosition()), viewHolder.getBindingAdapterPosition());
	}

	public void filter(CharSequence json) {
		getFilter().filter(json);
	}

	public void filterById(long id) {
		JSONObject j = new JSONObject();
		try {
			j.put(AbstractObjetBddAvecIdDAO.COL_ID, id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		filter(j.toString());
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				List<T> previousList = AbstractGeneriqueAdapter.this.currentList;
				List<T> tmpList = new ArrayList<>(AbstractGeneriqueAdapter.this.currentList);
				AbstractGeneriqueAdapter.this.currentList = (List<T>) results.values;
				if (AbstractGeneriqueAdapter.this.currentList == null) {
					AbstractGeneriqueAdapter.this.currentList = new ArrayList<>();
				}

				for (int i = 0; i < AbstractGeneriqueAdapter.this.originalList.size(); i++) {
					final T t = AbstractGeneriqueAdapter.this.originalList.get(i);
					boolean isPresentBefore = previousList.contains(t);
					boolean isPresentAfter = AbstractGeneriqueAdapter.this.currentList.contains(t);
					if (isPresentBefore) {
						if (!isPresentAfter) {
							int idx = tmpList.indexOf(t);
							tmpList.remove(idx);
							notifyItemRemoved(idx);
						}
					} else {
						if (isPresentAfter) {
							tmpList.add(i, t);
							notifyItemInserted(i);
						}
					}
				}
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<T> filteredResults = null;
				if (constraint == null || constraint.length() <= 0) {
					filteredResults = AbstractGeneriqueAdapter.this.originalList;
				} else {
					filteredResults = getFilteredResults(constraint.toString());
				}
				FilterResults results = new FilterResults();
				results.values = filteredResults;
				return results;
			}
		};
	}

	protected List<T> getFilteredResults(String json) {
		List<T> lst = new ArrayList<>(this.originalList);
		Predicate<T> filter = getPredicateFilter(json);
		if (filter != null) {
			lst.removeIf(filter.negate());
		}
		return lst;
	}

	protected abstract Predicate<T> getPredicateFilter(String json);

}