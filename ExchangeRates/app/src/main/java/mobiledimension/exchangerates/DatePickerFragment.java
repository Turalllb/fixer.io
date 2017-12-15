package mobiledimension.exchangerates;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Турал on 28.11.2017.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    DialogFragmentListener mCallback;
    Boolean timesCalled = true; // Используется как костыль.   при нажатии на DataPiker он вызывается дважды.

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        final Calendar calendarMin = Calendar.getInstance(); // Дата для установки нижнего порога в DataPiker
        calendarMin.set(1999, 0, 1); // http://fixer.io/ хранит курсы валют начиная с 1999 года . Отсчет месяцев идет с нуля.


        //region получаем дату из TextView и парсим в DataPiker, чтобы при открытии диалога, он был наведен на установленную дату
        TextView CurrentDate = (TextView) getActivity().findViewById(R.id.Current_Date);
        String Date = CurrentDate.getText().toString();
        if (!Date.isEmpty()) {
            String[] arrdate = Date.split("-");
            year = Integer.parseInt(arrdate[0]);
            month = Integer.parseInt(arrdate[1]) - 1;
            day = Integer.parseInt(arrdate[2]);
        }
        //endregion

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        DatePicker dp = datePickerDialog.getDatePicker();
        dp.setMinDate(calendarMin.getTimeInMillis()); // ставим нижний диапазон в DatePiker
        dp.setMaxDate(System.currentTimeMillis()); // верхний диапазон   в DatePiker устанавливаем сегодняшним днем

        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {

        if (timesCalled) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(calendar.getTime());
            mCallback.getDate(formattedDate);

            timesCalled = false;
        }

    }

    interface DialogFragmentListener {
        void getDate(String date);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (DialogFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        timesCalled = true;   // возвращаем исходное значение
        mCallback = null;
    }
}
