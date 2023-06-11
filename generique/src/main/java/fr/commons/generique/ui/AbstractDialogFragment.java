package fr.commons.generique.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import fr.commons.generique.R;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractDialogFragment<T> extends DialogFragment {

	protected T item;

	@Getter
	@Setter
	private boolean isWithBtnValider = true;
	@Getter
	@Setter
	private boolean isWithBtnAnnuler = true;

	protected List<DialogInterface.OnShowListener> lstOnShowListener = new ArrayList<>();
	protected List<Predicate<T>> lstValidationItemListener = new ArrayList<>();
	protected List<Consumer<T>> lstItemValideListener = new ArrayList<>();
	protected List<Runnable> lstAnnulerListener = new ArrayList<>();

	protected Button btnValider;

	protected Button btnAnnuler;

	public AbstractDialogFragment(@NonNull T item) {
		this.item = item;
	}

	@NonNull
	@Override
	public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		AlertDialog.Builder b = creerDialogBuilder();
		if(isWithBtnValider){
			b.setPositiveButton(R.string.btn_valider, null);
		}
		if(isWithBtnAnnuler){
			b.setNegativeButton(R.string.btn_annuler, null);
		}

		AlertDialog d = b.create();
		d.setOnShowListener((di) -> {
			if(isWithBtnValider){
				applyStylePositiveButton(d);
				btnValider = d.getButton(AlertDialog.BUTTON_POSITIVE);
				// On redéfini le onClick au lieu d'utiliser celui en 2eme paramètre de setPositiveButton
				// sinon le click sur valider va dismiss meme si l'on ne veut pas
				btnValider.setOnClickListener(this::onClickValider);
			}
			if(isWithBtnAnnuler){
				applyStyleNegativeButton(d);
				btnAnnuler = d.getButton(AlertDialog.BUTTON_NEGATIVE);
				// cf comme sur le btnValider
				btnAnnuler.setOnClickListener(this::onClickAnnuler);
			}
			this.lstOnShowListener.forEach(l -> l.onShow(di));
		});

		return d;
	}

	protected void onClickValider(View _v) {
		boolean isAllValid = lstValidationItemListener.stream().allMatch(l -> l.test(item));
		if(isAllValid){
			lstItemValideListener.forEach(l -> l.accept(item));
			if(getDialog() != null){
				getDialog().dismiss();
			}
		}
	}

	protected void onClickAnnuler(View _v) {
		this.lstAnnulerListener.forEach(Runnable::run);
		if(getDialog() != null){
			getDialog().dismiss();
		}
	}

	private void applyStylePositiveButton(AlertDialog d){
		Button posBtn = d.getButton(AlertDialog.BUTTON_POSITIVE);
		LinearLayout.LayoutParams posParams = (LinearLayout.LayoutParams) posBtn.getLayoutParams();
		posParams.weight = 10;
		posParams.setMargins(0, 0, 0, 0);
		posBtn.setLayoutParams(posParams);
		posBtn.setTextColor(Color.GREEN);
		posBtn.invalidate();
	}

	private void applyStyleNegativeButton(AlertDialog d){
		Button negBtn = d.getButton(AlertDialog.BUTTON_NEGATIVE);
		LinearLayout.LayoutParams negParams = (LinearLayout.LayoutParams) negBtn.getLayoutParams();
		negParams.weight = 10;
		negParams.setMargins(0, 0, 0, 0);
		negBtn.setLayoutParams(negParams);
		negBtn.setTextColor(Color.RED);
		negBtn.invalidate();
	}

	protected abstract AlertDialog.Builder creerDialogBuilder();

	public void addValidationItemListener(Predicate<T> c) {
		this.lstValidationItemListener.add(c);
	}

	public void addOnItemValideListener(Consumer<T> c) {
		this.lstItemValideListener.add(c);
	}

	public void addAnnulerListener(Runnable r) {
		this.lstAnnulerListener.add(r);
	}

	public void addOnShowListener(DialogInterface.OnShowListener l){
		this.lstOnShowListener.add(l);
	}
}