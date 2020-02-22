package cz.fungisoft.coffeecompass2.activity.support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.utils.Utils;

@SuppressLint("AppCompatCustomView")
/**
 * Special version of TextView capable listening of CoffeeSite's distance property change.
 */
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

    /**
     * Enter the new distance value as this TextView text property.
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (coffeeSite != null) {
            setText(Utils.getDistanceInBetterReadableForm(coffeeSite.getDistance()));
        }
    }

}
