package sianis.org.goeurotest.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import sianis.org.goeurotest.PlacePickerActivity;
import sianis.org.goeurotest.R;

public class SearchFragment extends Fragment {

    private static final String FROM_KEY = "FROM";
    private static final String TO_KEY = "TO";
    private static final String DATE_KEY = "DATE";
    private static final int FROM_REQUEST_CODE = 1000;
    private static final int TO_REQUEST_CODE = 1001;

    @InjectView(R.id.from)
    TextView fromTextView;

    @InjectView(R.id.to)
    TextView toTextView;

    @InjectView(R.id.date)
    TextView dateTextView;

    @InjectViews({R.id.from, R.id.to, R.id.date})
    List<TextView> inputFields;

    @InjectView(R.id.searchButton)
    Button searchButton;

    //Store selected date
    private Calendar currentDate;
    //Store today with cleared fields
    private Calendar minimumDate;
    //OnDateSetListener instance for DatePickerDialog
    private DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            //Create a Calendar with picked date
            Calendar pickedDate = Calendar.getInstance();
            pickedDate.set(Calendar.YEAR, year);
            pickedDate.set(Calendar.MONTH, monthOfYear);
            pickedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            //Set this date as current date
            setDateTextView(pickedDate);
            validateForm();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, view);
        minimumDate = generateMinimumDate();
        handleSavedInstance(savedInstanceState);
        validateForm();
        return view;
    }

    private static Calendar generateMinimumDate() {
        Calendar ret = Calendar.getInstance();
        ret.clear(Calendar.HOUR_OF_DAY);
        ret.clear(Calendar.MINUTE);
        ret.clear(Calendar.SECOND);
        ret.clear(Calendar.MILLISECOND);
        return ret;
    }

    private void handleSavedInstance(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (currentDate != null) {
                setDateTextView(currentDate);
            } else {
                setDateTextView(minimumDate);
            }
        } else {
            //Existing instance restore
            fromTextView.setText(savedInstanceState.getCharSequence(FROM_KEY));
            toTextView.setText(savedInstanceState.getCharSequence(TO_KEY));
            setDateTextView((Calendar) savedInstanceState.getSerializable(DATE_KEY));
        }
    }

    private void setDateTextView(Calendar date) {
        currentDate = date;
        dateTextView.setText(DateUtils.formatDateTime(dateTextView.getContext(), currentDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    @OnClick({R.id.date, R.id.dateButton})
    void openDatePickerDialog(View v) {
        //Create date picker with selected date
        DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), mOnDateSetListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
        //Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(minimumDate.getTimeInMillis());
        datePickerDialog.show();
    }

    @OnClick({R.id.from, R.id.to})
    void placeSelectorClickedChanged(View view) {
        Intent intent = new Intent(fromTextView.getContext(), PlacePickerActivity.class);
        intent.putExtra(PlacePickerActivity.EXTRA_SELECTED_PLACE, ((TextView) view).getText().toString());
        intent.putExtra(PlacePickerActivity.EXTRA_SELECTED_DIRECTION, view.getId() == R.id.from ? PlacePickerActivity.EXTRA_DIRECTION_FROM : PlacePickerActivity.EXTRA_DIRECTION_TO);
        startActivityForResult(intent, view.getId() == R.id.from ? FROM_REQUEST_CODE : TO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Handle place selection
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case (FROM_REQUEST_CODE):
                    fromTextView.setText(data.getStringExtra(PlacePickerActivity.EXTRA_SELECTED_PLACE));
                    break;
                case (TO_REQUEST_CODE):
                    toTextView.setText(data.getStringExtra(PlacePickerActivity.EXTRA_SELECTED_PLACE));
                    break;
            }
        }
        validateForm();
    }

    private void validateForm() {
        boolean valid = true;
        for (TextView textView : inputFields) {
            valid &= !textView.getText().toString().trim().isEmpty();
        }
        searchButton.setEnabled(valid);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save selected values
        outState.putCharSequence(FROM_KEY, fromTextView.getText());
        outState.putCharSequence(TO_KEY, toTextView.getText());
        outState.putSerializable(DATE_KEY, currentDate);
    }

    @OnClick(R.id.searchButton)
    void searchButtonClicked() {
        //Just show unimplemented dialog
        new AlertDialog.Builder(searchButton.getContext())
                .setTitle(R.string.warning)
                .setMessage(R.string.search_not_implemented)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing, just close
                    }
                }).setIcon(R.drawable.ic_info)
                .show();
    }
}
