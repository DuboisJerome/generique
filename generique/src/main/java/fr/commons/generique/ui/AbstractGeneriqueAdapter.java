package fr.commons.generique.ui;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import fr.commons.generique.controller.dao.AbstractObjetBddAvecIdDAO;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractGeneriqueAdapter<T, VH extends AbstractGeneriqueViewHolder<T>> extends RecyclerView.Adapter<VH> implements Filterable, ListAdapter {

	protected List<T> currentList;
	protected List<T> originalList;
	private final Binder binder = new Binder();
	private Set<T> itemsSelected = new HashSet<>();
	@Getter
	@Setter
	protected Comparator<? super T> comparator;
	@Getter
	@Setter
	protected boolean isNotify = true;
	private CharSequence lastFilter = "";

	public AbstractGeneriqueAdapter() {
		this(new ArrayList<>(), null);
	}

	public AbstractGeneriqueAdapter(List<T> l) {
		this(l, null);
	}

	public AbstractGeneriqueAdapter(List<T> l, Comparator<T> c) {
		this.isNotify = false;
		setList(l);
		this.isNotify = true;
		this.comparator= c;
	}

	public void setList(List<T> l){
		this.originalList = l;
		if(this.comparator != null){
			originalList.sort(comparator);
		}
		this.currentList = new ArrayList<>(originalList);
		if(isNotify) {
			notifyDataSetChanged();
		}
	}

	public void clear(){
		setList(new ArrayList<>());
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
			if(isNotify) notifyItemChanged(idxItemExistantCurrent);
		} else {
			this.currentList.add(t);
			if(comparator!=null){
				this.currentList.sort(comparator);
				idxItemExistantCurrent = this.currentList.indexOf(t);
			} else {
				idxItemExistantCurrent = this.currentList.size()-1;
			}
			if(isNotify) notifyItemInserted(idxItemExistantCurrent);
		}
		return isOk;
	}

	public boolean removeItem(T t) {
		int position = this.originalList.indexOf(t);
		if(position != -1){
			boolean isOk = this.originalList.remove(t);
			this.currentList.remove(t);
			if(isNotify) notifyItemRemoved(position);
			return isOk;
		}
		return false;
	}

	@Deprecated
	public void addBinderView(BiConsumer<View,T> binder) {
		addBinderView(binder.toString(), binder);
	}
	public void addBinderView(String tag, BiConsumer<View,T> binder) {
		this.binder.set(tag, binder);
	}

	@Override
	public int getItemCount() {
		return this.currentList.size();
	}

	public boolean isSelected(T t) {
		return t != null && itemsSelected.contains(t);
	}

	public boolean select(T t) {
		return itemsSelected.add(t);
	}
	public boolean deselect(T t) {
		return itemsSelected.remove(t);
	}

	public Set<T> getLstItemSelected() {
		return this.itemsSelected;
	}

	public T getItemAt(int position) {
		return this.currentList.get(position);
	}

	@Override
	public void onBindViewHolder(@NonNull VH viewHolder, int i) {
		viewHolder.bind(this.binder.merge(), getItemAt(viewHolder.getBindingAdapterPosition()));
	}

	public void filter() {
		filter("");
	}

	public void filter(CharSequence json) {
		if(!isAlreadyFilter(json)){
			getFilter().filter(json);
			lastFilter = json;
		}
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

	/**
	 * Quand on veut filtrer avant la création de la view
	 * @param json
	 */
	public void filterSyncNoNotif(CharSequence json){
		if(!isAlreadyFilter(json)) {
			CustomFilterResult filteredResults = performFilteringCustom(json);
			boolean isNotifyOld = isNotify;
			isNotify = false;
			publishResultsCustom((List<T>) filteredResults.values);
			isNotify = isNotifyOld;
			lastFilter = json;
		}
	}

	private boolean isAlreadyFilter(CharSequence json){
		return json == null || lastFilter.equals(json);
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				publishResultsCustom((List<T>) results.values);
			}

			@Override
			protected FilterResults performFiltering(CharSequence json) {
				CustomFilterResult filteredResults = performFilteringCustom(json);
				FilterResults results = new FilterResults();
				results.values = filteredResults.getValues();
				results.count = filteredResults.getCount();
				return results;
			}
		};
	}

	private CustomFilterResult performFilteringCustom(CharSequence json){
		List<T> filteredResults = null;
		if (json == null || json.length() <= 0) {
			filteredResults = AbstractGeneriqueAdapter.this.originalList;
		} else {
			filteredResults = getFilteredResults(json.toString());
		}
		return new CustomFilterResult(filteredResults);
	}

	private void publishResultsCustom(List<T> resultFiltering){
		List<T> previousList = AbstractGeneriqueAdapter.this.currentList;
		List<T> tmpList = new ArrayList<>(AbstractGeneriqueAdapter.this.currentList);
		AbstractGeneriqueAdapter.this.currentList = (List<T>) resultFiltering;
		if (AbstractGeneriqueAdapter.this.currentList == null) {
			AbstractGeneriqueAdapter.this.currentList = new ArrayList<>();
		}

		int lastIdxValid = -1;
		for (int i = 0; i < AbstractGeneriqueAdapter.this.originalList.size(); i++) {
			final T t = AbstractGeneriqueAdapter.this.originalList.get(i);
			boolean isPresentBefore = previousList.contains(t);
			boolean isPresentAfter = AbstractGeneriqueAdapter.this.currentList.contains(t);
			if (isPresentBefore) {
				int idx = tmpList.indexOf(t);
				if (!isPresentAfter) {
					tmpList.remove(idx);
					if(isNotify) notifyItemRemoved(idx);
				} else {
					lastIdxValid = idx;
				}
			} else {
				int idx = lastIdxValid+1;
				if (isPresentAfter) {
					tmpList.add(idx, t);
					if(isNotify) notifyItemInserted(idx);
					lastIdxValid = idx;
				}
			}
		}
	}

	protected List<T> getFilteredResults(String json) {
		List<T> lst = new ArrayList<>(this.originalList);
		Predicate<T> filter = getPredicateFilter(json);
		if (filter != null) {
			Predicate<T> removeFilter = filter.negate();
			lst.removeIf(removeFilter);
		}
		return lst;
	}

	@Deprecated
	public T getItem(int position){ return getItemAt(position);}

	protected abstract Predicate<T> getPredicateFilter(String json);


	/**
	 * Sort
	 *
	 * @param c
	 */
	public void sort(final Comparator<? super T> c) {
		List<T> tmpLst = new ArrayList<>(this.currentList);
		originalList.sort(c);
		currentList.sort(c);
		comparator = c;

		for (int i = 0; i < this.currentList.size(); i++) {
			T t = this.currentList.get(i);
			int idxBefore = tmpLst.indexOf(t);
			if (idxBefore != i) {
				boolean addBefore = i < idxBefore;
				tmpLst.add(i, t);
				tmpLst.remove(addBefore ? (idxBefore + 1) : idxBefore);
				if(isNotify) notifyItemMoved(idxBefore, i);
			}
		}
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// Nothing
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// Nothing
	}

	@Override
	public int getCount() {
		return getItemCount();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		VH b = this.onCreateViewHolder(parent, 0);
		b.bind(this.binder.merge(), getItemAt(position));
		return b.itemView;
	}



	@Override
	public int getViewTypeCount() {
		return 1;
	}

	public boolean isEmpty(){
		return getItemCount() <= 0;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	public void setOnClickItemListener(Consumer<T> c){
		this.binder.set("ON_CLICK_ITEM_LISTENER", (v,t) -> {
			v.setOnClickListener(v1 -> {
				c.accept(t);
			});
		});
	}

	private class Binder {
		private final Map<String, BiConsumer<View,T>> binders=  new HashMap<>();
		private void set(String tag,BiConsumer<View,T> binder) {
			binders.put(tag, binder);
		}
		private BiConsumer<View, T> merge(){
			return (view, t) -> {
				binders.forEach((tag,biConsumer) -> {
					biConsumer.accept(view, t);
				});
			};
		}
	}

	private class CustomFilterResult {
		@Getter
		List<T> values = new ArrayList<>();
		CustomFilterResult(List<T> values){
			if(values != null){
				this.values = values;
			}
		}
		int getCount(){
			return values.size();
		}
	}
}