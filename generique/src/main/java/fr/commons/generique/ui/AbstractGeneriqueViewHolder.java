package fr.commons.generique.ui;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.function.BiConsumer;

public abstract class AbstractGeneriqueViewHolder<T> extends RecyclerView.ViewHolder {

	public AbstractGeneriqueViewHolder(View view) {
		super(view);
	}

	protected abstract void bind(T t);

	public void bind(BiConsumer<View,Integer> binder, T t, int position) {
		bind(t);
		if (binder != null) {
			binder.accept(this.itemView, position);
		}
	}

}