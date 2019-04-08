package cz.fungisoft.coffeecompass.activity.support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteMovable;

@SuppressLint("AppCompatCustomView")
public class DistanceChangeTextView extends TextView implements PropertyChangeListener
{
    public DistanceChangeTextView(Context context) {
        super(context);
    }

    public DistanceChangeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DistanceChangeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCoffeeSite(CoffeeSiteMovable coffeeSite) {
        this.coffeeSite = coffeeSite;
    }

    private CoffeeSiteMovable coffeeSite;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (coffeeSite != null) {
            setText(String.valueOf(coffeeSite.getDistance()) + " m");
        }
    }

}
