package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;


public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    public interface EnterTimeDialogListener {

        void onEnterTimeDialogPositiveClick(TextInputEditText callingInputEditText, int hourOfDay, int minute);
    }

    // Use this instance of the interface to deliver action events
    TimePickerFragment.EnterTimeDialogListener listener;

    private TextInputEditText callingInputEditText;

    public void setCallingInputEditText(TextInputEditText callingInputEditText) {
        this.callingInputEditText = callingInputEditText;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DeleteUserAccountDialogListener so we can send events to the host
            listener = (TimePickerFragment.EnterTimeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(listener.toString()
                    + " must implement TimePickerFragment.EnterTimeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        //Dialog timePickerDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar, this, hour, minute,
        Dialog timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));

        // Create a new instance of TimePickerDialog and return it
        return timePickerDialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        listener.onEnterTimeDialogPositiveClick(callingInputEditText, hourOfDay, minute);
    }

}
