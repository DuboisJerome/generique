package fr.commons.generique.ui;

import android.app.AlertDialog;
import android.content.Context;
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

public class DeleteDialogObjetBddFragment<T extends IObjetBdd> extends AbstractDialogFragment<T> {

	protected TableDAO<T> dao;
	private final List<Consumer<T>> lstOnDeleteListener=  new ArrayList<>();

	public DeleteDialogObjetBddFragment(@NonNull T item, @NonNull TableDAO<T> dao) {
		super(item);
		this.dao = dao;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		addOnItemValideListener(t -> {
			if (!t.isNew()) {
				this.dao.delete(t);
				lstOnDeleteListener.forEach(c -> c.accept(t));
			}
		});
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected AlertDialog.Builder creerDialogBuilder() {
		Context ctx = requireContext();
		AlertDialog.Builder a = new AlertDialog.Builder(ctx);
		return a.setMessage("Êtes-vous sûr de vouloir supprimer cet élément ?");
	}

	public void addOnDeleteListener(Consumer<T> c){
		if(c==null)return;
		this.lstOnDeleteListener.add(c);
	}

}