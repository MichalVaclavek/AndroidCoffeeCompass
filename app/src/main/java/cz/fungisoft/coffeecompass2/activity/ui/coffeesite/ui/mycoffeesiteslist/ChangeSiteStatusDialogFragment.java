package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;

/**
 * Dialog to select CoffeeSite operational status and date from which it is valid.
 */
public class ChangeSiteStatusDialogFragment extends DialogFragment {

    private static final String ARG_STATUSES = "statuses";
    private static final String ARG_CURRENT_STATUS = "currentStatus";
    private static final String SERVER_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DISPLAY_DATE_PATTERN = "dd.MM. yyyy";

    private ArrayList<CoffeeSiteStatus> statuses = new ArrayList<>();
    private CoffeeSiteStatus selectedStatus;
    private String validFrom;

    public interface ChangeSiteStatusDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    private ChangeSiteStatusDialogListener listener;

    public static ChangeSiteStatusDialogFragment newInstance(ArrayList<CoffeeSiteStatus> statuses,
                                                             String currentStatus) {
        ChangeSiteStatusDialogFragment fragment = new ChangeSiteStatusDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_STATUSES, statuses);
        args.putString(ARG_CURRENT_STATUS, currentStatus);
        fragment.setArguments(args);
        return fragment;
    }

    public CoffeeSiteStatus getSelectedStatus() {
        return selectedStatus;
    }

    public String getValidFrom() {
        return validFrom;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ChangeSiteStatusDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement ChangeSiteStatusDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ArrayList<CoffeeSiteStatus> argumentStatuses = getArguments().getParcelableArrayList(ARG_STATUSES);
            if (argumentStatuses != null) {
                statuses = argumentStatuses;
            }
        }
        validFrom = new SimpleDateFormat(SERVER_DATE_PATTERN, Locale.US).format(new Date());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.change_site_status_dialog, null);

        Spinner statusSpinner = dialogView.findViewById(R.id.site_status_spinner);
        TextView validFromText = dialogView.findViewById(R.id.site_status_valid_from_text);

        setupStatusSpinner(statusSpinner);
        validFromText.setText(formatDisplayDate(validFrom));
        validFromText.setOnClickListener(v -> showDatePicker(validFromText));

        builder.setView(dialogView)
                .setTitle(getDialogTitle())
                .setPositiveButton(R.string.change_site_status_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(ChangeSiteStatusDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(ChangeSiteStatusDialogFragment.this);
                    }
                });

        return builder.create();
    }

    private String getDialogTitle() {
        String currentStatusLabel = getCurrentStatusLabel();
        if (currentStatusLabel.isEmpty()) {
            return getString(R.string.change_site_status);
        }
        return getString(R.string.change_site_status_with_current, currentStatusLabel);
    }

    private void setupStatusSpinner(Spinner statusSpinner) {
        ArrayList<String> statusLabels = new ArrayList<>();
        for (CoffeeSiteStatus status : statuses) {
            statusLabels.add(status.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statusLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        int selectedPosition = getCurrentStatusPosition();
        if (!statuses.isEmpty()) {
            selectedStatus = statuses.get(selectedPosition);
            statusSpinner.setSelection(selectedPosition);
        }

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = statuses.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private int getCurrentStatusPosition() {
        String currentStatus = getArguments() != null ? getArguments().getString(ARG_CURRENT_STATUS, "") : "";
        for (int i = 0; i < statuses.size(); i++) {
            CoffeeSiteStatus status = statuses.get(i);
            if (isCurrentStatus(status, currentStatus)) {
                return i;
            }
        }
        return 0;
    }

    private String getCurrentStatusLabel() {
        String currentStatus = getArguments() != null ? getArguments().getString(ARG_CURRENT_STATUS, "") : "";
        for (CoffeeSiteStatus status : statuses) {
            if (isCurrentStatus(status, currentStatus)) {
                return status.toString();
            }
        }
        return currentStatus;
    }

    private boolean isCurrentStatus(CoffeeSiteStatus status, String currentStatus) {
        return status != null
                && currentStatus != null
                && (currentStatus.equals(status.getStatus())
                || currentStatus.equals(status.getId())
                || currentStatus.equals(status.toString()));
    }

    private void showDatePicker(TextView validFromText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    validFrom = new SimpleDateFormat(SERVER_DATE_PATTERN, Locale.US)
                            .format(selectedDate.getTime());
                    validFromText.setText(formatDisplayDate(validFrom));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private String formatDisplayDate(String serverDate) {
        try {
            Date date = new SimpleDateFormat(SERVER_DATE_PATTERN, Locale.US).parse(serverDate);
            if (date != null) {
                return new SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.getDefault()).format(date);
            }
        } catch (java.text.ParseException ignored) {
        }
        return serverDate;
    }
}
