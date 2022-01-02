package fr.commons.generique.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import fr.commons.generique.R;

public abstract class AbstractDialogFragment<T> extends DialogFragment {

	protected T item;

	protected List<Predicate<T>> lstValidationItemListener = new ArrayList<>();
	protected List<Consumer<T>> lstItemValideListener = new ArrayList<>();
	protected List<Runnable> lstAnnulerListener = new ArrayList<>();

	public AbstractDialogFragment(@NonNull T item) {
		this.item = item;
	}

	@NonNull
	@Override
	public final Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		AlertDialog d = creerDialogBuilder().setPositiveButton(R.string.btn_valider, null).setNegativeButton(R.string.btn_annuler, getAnnulerListener()).create();
		d.setOnShowListener((arg0) -> {

			Button posBtn = d.getButton(AlertDialog.BUTTON_POSITIVE);
			LinearLayout.LayoutParams posParams = (LinearLayout.LayoutParams) posBtn.getLayoutParams();
			posParams.weight = 10;
			posParams.setMargins(0, 0, 0, 0);
			posBtn.setLayoutParams(posParams);
			posBtn.setTextColor(Color.GREEN);
			posBtn.invalidate();
			posBtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					boolean isAllValid = lstValidationItemListener.stream().allMatch(l -> l.test(item));
					if(isAllValid){
						lstItemValideListener.forEach(l -> l.accept(item));
						d.dismiss();
					}
				}
			});

			Button negBtn = d.getButton(AlertDialog.BUTTON_NEGATIVE);
			LinearLayout.LayoutParams negParams = (LinearLayout.LayoutParams) negBtn.getLayoutParams();
			negParams.weight = 10;
			negParams.setMargins(0, 0, 0, 0);
			negBtn.setLayoutParams(negParams);
			negBtn.setTextColor(Color.RED);
			negBtn.invalidate();
		});

		return d;
	}

	protected abstract AlertDialog.Builder creerDialogBuilder();

	public void addValidationItemListener(Predicate<T> c) {
		this.lstValidationItemListener.add(c);
	}

	public void addOnItemValideListener(Consumer<T> c) {
		this.lstItemValideListener.add(c);
	}
	protected DialogInterface.OnClickListener getAnnulerListener() {
		return (dialog, which) -> {
			this.lstAnnulerListener.forEach(Runnable::run);
		};
	}

	public void addAnnulerListener(Runnable r) {
		this.lstAnnulerListener.add(r);
	}
}