package fr.commons.generique.ui;

import android.view.View;
import android.widget.AdapterView;
import android.widget.SpinnerAdapter;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import fr.commons.generique.model.db.AbstractObjetBddAvecId;
public class SpinnerBinding {

	@BindingAdapter("selectedSpinner")
	public static void setValue(AppCompatSpinner pAppCompatSpinner, long newSelectedId) {
		SpinnerAdapter adapter = pAppCompatSpinner.getAdapter();
		if (adapter instanceof AbstractGeneriqueSpinnerAdapter) {
			AbstractGeneriqueSpinnerAdapter<?> customAdapter = (AbstractGeneriqueSpinnerAdapter<?>) adapter;
			long currentId = pAppCompatSpinner.getSelectedItemId();
			if (currentId != newSelectedId) {
				pAppCompatSpinner.setSelection(customAdapter.getPosition(newSelectedId));
			}
			return;
		}
		throw new UnsupportedOperationException("L'adapter doit être une instance de AbstractGeneriqueSpinnerAdapter");
	}

	@BindingAdapter("selectedSpinnerAttrChanged")
	public static void valueChanged(AppCompatSpinner pAppCompatSpinner, final InverseBindingListener spinnerValueChanged) {
		AdapterView.OnItemSelectedListener existingListener = pAppCompatSpinner.getOnItemSelectedListener();
		AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				spinnerValueChanged.onChange();
				if (existingListener != null) {
					existingListener.onItemSelected(parent, view, position, id);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				spinnerValueChanged.onChange();
				if (existingListener != null) {
					existingListener.onNothingSelected(parent);
				}
			}
		};
		pAppCompatSpinner.setOnItemSelectedListener(listener);
	}

	@InverseBindingAdapter(attribute = "selectedSpinner")
	public static long getValue(AppCompatSpinner pAppCompatSpinner) {
		return ((AbstractObjetBddAvecId) pAppCompatSpinner.getSelectedItem()).getId();
	}
}