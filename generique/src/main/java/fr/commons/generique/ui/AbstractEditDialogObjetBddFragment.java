package fr.commons.generique.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.commons.generique.controller.db.TableDAO;
import fr.commons.generique.model.db.IObjetBdd;

public abstract class AbstractEditDialogObjetBddFragment<T extends IObjetBdd> extends AbstractItemDialogFragment<T> {

	private List<Consumer<T>> lstOnCreateListener =  new ArrayList<>();
	private List<Consumer<T>> lstOnUpdateListener =  new ArrayList<>();

	public AbstractEditDialogObjetBddFragment(@NonNull T item) {
		super(item);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		addOnItemValideListener(t -> {
			TableDAO<T> dao = getTableDAO();
			if (t.isNew()) {
				dao.creer(t);
				lstOnCreateListener.forEach(c -> c.accept(t));
			} else {
				dao.update(t);
				lstOnUpdateListener.forEach(c -> c.accept(t));
			}
		});
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	protected abstract TableDAO<T> getTableDAO();

	public void addOnCreateListener(Consumer<T> c){
		this.lstOnCreateListener.add(c);
	}

	public void addOnUpdateListener(Consumer<T> c){
		this.lstOnUpdateListener.add(c);
	}

}