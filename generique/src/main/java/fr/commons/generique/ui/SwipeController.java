package fr.commons.generique.ui;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import fr.commons.generique.R;
import lombok.Getter;
import lombok.Setter;


public class SwipeController extends ItemTouchHelper.Callback {
	public final static int HORIZONTAL = 0;
	public final static int VERTICAL = 1;

	enum ButtonsState {
		GONE,
		RIGHT_VISIBLE
	}

	@Getter
	@Setter
	public static class SwipeControllerParams {
		SwipeControllerActions actions;
		@ColorRes int idColorSwipeBg = R.color.design_default_color_background;
		@DrawableRes int idEditDrawable = R.drawable.ic_baseline_edit_24;
		@DrawableRes int idDeleteDrawable = R.drawable.ic_baseline_delete_24;
		int orientation;
	}

	public interface SwipeControllerActions {

		void onEditClicked(int position);

		void onDeleteClicked(int position);

	}


	private boolean swipeBack = false;

	private ButtonsState buttonShowedState = ButtonsState.GONE;

	private RectF buttonEdit = null;
	private RectF buttonDelete = null;
	public static final int btnMinWidth = 100;

	private RecyclerView.ViewHolder currentItemViewHolder = null;

	private final SwipeControllerParams params;

	public SwipeController(@NonNull SwipeControllerParams params) {
		this.params = params;
	}

	@Override
	public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
		return makeMovementFlags(0, ItemTouchHelper.LEFT);
	}

	@Override
	public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
		return false;
	}

	@Override
	public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

	}

	@Override
	public int convertToAbsoluteDirection(int flags, int layoutDirection) {
		if (this.swipeBack) {
			this.swipeBack = this.buttonShowedState != ButtonsState.GONE;
			return 0;
		}
		return super.convertToAbsoluteDirection(flags, layoutDirection);
	}

	@Override
	public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
			if (this.buttonShowedState != ButtonsState.GONE) {
				float limit = params.orientation == HORIZONTAL ? getBtnWidth()*2 : getBtnWidth();
				if (this.buttonShowedState == ButtonsState.RIGHT_VISIBLE)
					dX = Math.min(dX, -limit);
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			} else {
				setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			}
		}

		if (this.buttonShowedState == ButtonsState.GONE) {
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
		}
		this.currentItemViewHolder = viewHolder;
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setTouchListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
		recyclerView.setOnTouchListener((v, event) -> {
			SwipeController.this.swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
			if (SwipeController.this.swipeBack) {
				float limit = params.orientation == LinearLayoutCompat.HORIZONTAL ? getBtnWidth()*2 : getBtnWidth();
				if (dX < -limit){
					SwipeController.this.buttonShowedState = ButtonsState.RIGHT_VISIBLE;
				}

				if (SwipeController.this.buttonShowedState != ButtonsState.GONE) {
					setTouchDownListener(c, recyclerView, viewHolder, dY, actionState, isCurrentlyActive);
					setItemsClickable(recyclerView, false);
				}
			}
			return false;
		});
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dY, final int actionState, final boolean isCurrentlyActive) {
		recyclerView.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				setTouchUpListener(c, recyclerView, viewHolder, dY, actionState, isCurrentlyActive);
			}
			return false;
		});
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dY, final int actionState, final boolean isCurrentlyActive) {
		recyclerView.setOnTouchListener((v, ev) -> {
			if (ev.getAction() == MotionEvent.ACTION_UP) {
				SwipeController.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
				recyclerView.setOnTouchListener((v1, event1) -> false);
				setItemsClickable(recyclerView, true);
				SwipeController.this.swipeBack = false;

				if (SwipeController.this.params.actions != null && SwipeController.this.buttonShowedState == ButtonsState.RIGHT_VISIBLE) {

					if (isActionTouch(this.buttonEdit, ev)) {
						SwipeController.this.params.actions.onEditClicked(viewHolder.getAdapterPosition());
					}
					if (isActionTouch(this.buttonDelete, ev)) {
						SwipeController.this.params.actions.onDeleteClicked(viewHolder.getAdapterPosition());
					}
				}
				SwipeController.this.buttonShowedState = ButtonsState.GONE;
				SwipeController.this.currentItemViewHolder = null;
			}
			return false;
		});
	}

	private boolean isActionTouch(RectF rectAction, MotionEvent event) {
		return rectAction != null && rectAction.contains(event.getX(), event.getY());
	}

	private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
		for (int i = 0; i < recyclerView.getChildCount(); ++i) {
			recyclerView.getChildAt(i).setClickable(isClickable);
		}
	}

	private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
		float corners = 0;

		View iv = viewHolder.itemView;
		Paint p = new Paint();

		Resources res = iv.getResources();
		Resources.Theme theme = iv.getContext().getTheme();

		RectF bg = new RectF(iv.getLeft(), iv.getTop(), iv.getRight(), iv.getBottom());
		p.setColor(res.getColor(params.idColorSwipeBg, theme));
		c.drawRoundRect(bg, corners, corners, p);

		drawEditButton(c, p, iv);
		drawDeleteButton(c, p, iv);
	}

	private float getBtnWidth(){
		return this.buttonEdit != null ? this.buttonEdit.width() : 0f;
	}

	private Rect toRectIconWithPadding(RectF r) {
		float size = Math.min(r.width(), r.height());
		int padding = 5;
		size -= padding*2;
		float halfSize = size/2f;
		int left = (int) (r.centerX() - halfSize);
		int right = (int) (r.centerX() + halfSize);
		int top = (int) (r.centerY() - halfSize);
		int bottom = (int) (r.centerY() + halfSize);
		return new Rect(left, top, right, bottom);
	}

	private RectF creerRectBtn(View iv, int idx, int nbBtn){
		float l,t,r,b;
		if(params.orientation == HORIZONTAL){
			float tailleBtn = Math.max(Math.abs( iv.getBottom() - iv.getTop()), btnMinWidth);
			// From right to left
			l = iv.getRight() - ((nbBtn-idx)*tailleBtn);
			r = iv.getRight() - ((nbBtn-idx-1) * tailleBtn);
			t = iv.getTop();
			b = iv.getBottom();
		} else {
			int tailleBtn = Math.abs( iv.getBottom() - iv.getTop()) /nbBtn;
			tailleBtn = Math.max(tailleBtn, btnMinWidth);
			// From top to bottom
			l = iv.getRight() - tailleBtn;
			r = iv.getRight();
			t = iv.getTop() + (idx * tailleBtn);
			b = iv.getTop() + ((idx+1) * tailleBtn);
		}
		return new RectF(l,t,r,b);
	}

	private RectF drawButton(Canvas c, Paint p, View iv, int idx, @DrawableRes int idDrawable, @ColorInt int bgColor, String altText, int nbBtn){
		RectF selectableRectBtn = creerRectBtn(iv, idx, nbBtn);
		Resources res = iv.getResources();
		Resources.Theme theme = iv.getContext().getTheme();
		Drawable d = ResourcesCompat.getDrawable(res, idDrawable, theme);
		p.setColor(bgColor);
		c.drawRect(selectableRectBtn, p);
		if (d != null) {
			Rect rectIcon = toRectIconWithPadding(selectableRectBtn);
			d.setBounds(rectIcon);
			d.draw(c);
		} else {
			drawText(altText, c, selectableRectBtn, p);
		}
		return selectableRectBtn;
	}

	private void drawEditButton(Canvas c, Paint p, View iv) {
		RectF selectableRectBtn = drawButton(c, p, iv, 0, params.idEditDrawable, Color.BLACK,"EDIT", 2);
		this.buttonEdit = null;
		if (this.buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
			this.buttonEdit = selectableRectBtn;
		}
	}

	private void drawDeleteButton(Canvas c, Paint p, View iv) {
		RectF selectableRectBtn = drawButton(c, p, iv, 1, params.idDeleteDrawable, Color.RED, "DELETE", 2);
		this.buttonDelete = null;
		if (this.buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
			this.buttonDelete = selectableRectBtn;
		}
	}

	private void drawText(String text, Canvas c, RectF button, Paint p) {
		float textSize = 60;
		p.setColor(Color.WHITE);
		p.setAntiAlias(true);
		p.setTextSize(textSize);

		float textWidth = p.measureText(text);
		c.drawText(text, button.centerX() - (textWidth / 2), button.centerY() + (textSize / 2), p);
	}

	public void onDraw(Canvas c) {
		if (this.currentItemViewHolder != null) {
			drawButtons(c, this.currentItemViewHolder);
		}
	}

}