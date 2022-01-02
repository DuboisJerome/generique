package fr.commons.generique.ui;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

public abstract class AbstractItemDialogFragment<T> extends AbstractDialogFragment<T> {

	public AbstractItemDialogFragment(@NonNull T item) {
		super(item);
	}

	protected abstract ViewDataBinding createBinding();

	@Override
	protected AlertDialog.Builder creerDialogBuilder() {
		Context ctx = requireContext();
		AlertDialog.Builder a = new AlertDialog.Builder(ctx);
		ViewDataBinding binding = createBinding();
		return a.setView(binding.getRoot());
	}
}