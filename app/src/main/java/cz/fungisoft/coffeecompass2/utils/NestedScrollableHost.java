package cz.fungisoft.coffeecompass2.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Layout wrapper to allow a child ViewPager2 nested inside a parent ViewPager2
 * to handle horizontal swipe gestures correctly.
 * <p>
 * Based on the official Google sample for nested ViewPager2:
 * https://github.com/android/views-widgets-samples/blob/master/ViewPager2/app/src/main/java/androidx/viewpager2/integration/testapp/NestedScrollableHost.kt
 * <p>
 * Wrap the inner ViewPager2 with this layout in XML so that the inner pager
 * can intercept horizontal touch events that belong to it, preventing the
 * outer ViewPager2 from stealing them.
 */
public class NestedScrollableHost extends FrameLayout {

    private int touchSlop = 0;
    private float initialX = 0f;
    private float initialY = 0f;

    public NestedScrollableHost(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        touchSlop = android.view.ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * Finds the first child ViewPager2 (the inner pager).
     */
    @Nullable
    private ViewPager2 getChildViewPager2() {
        View child = (getChildCount() > 0) ? getChildAt(0) : null;
        return (child instanceof ViewPager2) ? (ViewPager2) child : null;
    }

    /**
     * Walks up the view tree to find the parent ViewPager2 (the outer pager).
     */
    @Nullable
    private ViewPager2 getParentViewPager2() {
        ViewParent parent = getParent();
        while (parent != null) {
            // ViewPager2 uses an internal RecyclerView; its parent's parent is the ViewPager2
            if (parent instanceof ViewPager2) {
                return (ViewPager2) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private boolean canChildScroll(int direction) {
        ViewPager2 child = getChildViewPager2();
        if (child == null) {
            return false;
        }
        // direction < 0: scroll left, direction > 0: scroll right
        int currentItem = child.getCurrentItem();
        int itemCount = child.getAdapter() != null ? child.getAdapter().getItemCount() : 0;
        if (direction < 0) {
            return currentItem > 0;
        } else {
            return currentItem < itemCount - 1;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleInterceptTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private void handleInterceptTouchEvent(MotionEvent ev) {
        ViewPager2 parentViewPager = getParentViewPager2();
        if (parentViewPager == null) {
            return;
        }

        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            initialX = ev.getX();
            initialY = ev.getY();
            // Allow child to handle touch initially
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (action == MotionEvent.ACTION_MOVE) {
            float dx = ev.getX() - initialX;
            float dy = ev.getY() - initialY;
            boolean isVpHorizontal = parentViewPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL;

            // Determine scroll direction based on parent ViewPager2 orientation
            float scaledDx = Math.abs(dx) * (isVpHorizontal ? 0.5f : 1f);
            float scaledDy = Math.abs(dy) * (isVpHorizontal ? 1f : 0.5f);

            if (scaledDx > touchSlop || scaledDy > touchSlop) {
                if (isVpHorizontal == (scaledDy > scaledDx)) {
                    // Gesture is perpendicular to parent pager; let parent handle it
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    // Gesture is parallel to parent pager; check if child can scroll
                    int direction = (isVpHorizontal) ? (dx > 0 ? -1 : 1) : (dy > 0 ? -1 : 1);
                    if (canChildScroll(direction)) {
                        // Child can scroll in this direction; keep intercepting
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        // Child cannot scroll further; let parent handle it
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
            }
        }
    }
}
