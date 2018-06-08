package mobiledimension.exchangerates;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static mobiledimension.exchangerates.ModelData.COMPARATOR_NAME;
import static mobiledimension.exchangerates.ModelData.COMPARATOR_VALUE_ASCENDING;
import static mobiledimension.exchangerates.ModelData.COMPARATOR_VALUE_DESCENDING;

public class MainMenu extends AppCompatActivity implements DatePickerFragment.DialogFragmentListener, AsyncTaskUploadingData.AsyncTaskResult {
    static final String LOG_TAG = "myLogs";
    private final String ACCESS_KEY = "0cd4416cd335bb08486b95e597b8c6b3"; //Для доступа к апи сайта. Есть ограничения в бесплатной версии.
    DatabaseHelper databaseHelper;
    DatabaseManagement databaseManagement;
    NetworkChangeReceiver networkChangeReceiver;
    private String currentDate;
    private String answer;
    private String currentCurrency = "EUR";
    private List<String> currenciesArrayList = new ArrayList<>(Arrays.asList("EUR")); //Список валют для спиннера
    private TextView currentDateTextView;
    private List<ModelData> modelDataArrayList = new ArrayList<>(); //список из моделей (валюта курс)
    private ArrayAdapter<String> spinnerAdapter;
    private AdapterModelData adapterModelData;
    private DatePickerFragment datePickerFragment = new DatePickerFragment();
    private Spinner spinnerOfCurrencies;
    private RadioGroup sortRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //постоянно портретная ориентация
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //приложение на полный экран

        networkChangeReceiver = new NetworkChangeReceiver();

        databaseHelper = new DatabaseHelper(this, getPath());  // объект для создания и управления версиями БД
        SQLiteDatabase exchangeRatesDatabase = databaseHelper.getWritableDatabase();
        databaseManagement = new DatabaseManagement(exchangeRatesDatabase);

        //region findViewById
        currentDateTextView = (TextView) findViewById(R.id.currentDateTextView);
        spinnerOfCurrencies = (Spinner) findViewById(R.id.spinnerCurrency);
        ListView listView = (ListView) findViewById(R.id.ListView);
        sortRadioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        //endregion

        //region предварительная установка текущей даты
        currentDate = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        currentDateTextView.setText(currentDate);
        //endregion

        adapterModelData = new AdapterModelData(this, R.layout.rates, modelDataArrayList); //Адаптер списка с курсом валют
        listView.setAdapter(adapterModelData);

        spinnerAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, currenciesArrayList); //Адаптер для спиннера
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOfCurrencies.setAdapter(spinnerAdapter);

        //Обработчик нажатия на спиннер
        spinnerOfCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                currentCurrency(position);
                uploadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        sortRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int id) {
                sorting();
                adapterModelData.refresh();
            }
        });
    }

    protected void onResume() {
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onResume();

    }

    protected void onPause() {
        unregisterReceiver(networkChangeReceiver);
        super.onPause();
    }

    private void sorting() {
        switch (sortRadioGroup.getCheckedRadioButtonId()) {
            case R.id.radioButton1:
                Collections.sort(modelDataArrayList, COMPARATOR_NAME);
                break;
            case R.id.radioButton2:
                Collections.sort(modelDataArrayList, COMPARATOR_VALUE_ASCENDING);
                break;
            case R.id.radioButton3:
                Collections.sort(modelDataArrayList, COMPARATOR_VALUE_DESCENDING);
                break;
        }
    }

    private String getPath() {
        //Проверяем наличие внешней памяти и возращаем путь для создания БД
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return getExternalFilesDir(null).getAbsolutePath();
        } else {
            return getFilesDir().getAbsolutePath();
        }
    }

    private void currentCurrency(int position) {
        //сохраняю в переменную выбранную позицию в спинере
        currentCurrency = currenciesArrayList.get(position);
    }

    public void onClickDate(View view) {
        datePickerFragment.show(getSupportFragmentManager(), "DataPicker");
    }

    // Метод обратного вызова, сработает при выборе даты в DataPickerFragment
    public void getDate(String date) {
        currentDateTextView.setText(date);
        currentDate = date;
        uploadData();
    }

    private void uploadData() {
        answer = databaseManagement.getAnswer(currentDate, currentCurrency);
        if (answer != null) {
            //Если в БД уже есть результат
            setData();
        } else {
            String url = "http://data.fixer.io/api/" + currentDate + "?access_key=" + ACCESS_KEY + "&base=" + currentCurrency;
            AsyncTaskUploadingData asyncTaskUploadingData = new AsyncTaskUploadingData(this);
            asyncTaskUploadingData.execute(url);
        }
    }

    //Метод интерфейса для обратного вызова из АсинкТаск
    public void getResult(String answer) {
        this.answer = answer;
        setData();

    }

    private void setData() {
        modelDataArrayList.clear();
        if (answer != null) {
            Parser parser = new Parser(answer);
            /*класс, который хранит дату, установленную валюту и т.д. для удобной передачи из парсера,
             а если нужно и для возврата из дессериализатора*/
            IncomeData incomeData = parser.getIncomeData();
            if (incomeData.getDate().equals(currentDate)) { //Проверяю дату, т.к. апи может вернуть результат на другую дату, если по текущей курсов нет.
                modelDataArrayList.addAll(incomeData.getRates());
                currenciesArrayList.clear();
                currenciesArrayList.addAll(incomeData.getCurrencies());
                Collections.sort(currenciesArrayList);
                spinnerAdapter.notifyDataSetChanged();
               /* Не всегда в спиннере после обновления будет стоять валюта по которой сделан запрос,
                так как список спиннера тоже всегда обновляется, поэтому вручную устанавливаю текущую валюту*/
                spinnerOfCurrencies.setSelection(currenciesArrayList.indexOf(currentCurrency));
                sorting();
                adapterModelData.refresh();
                databaseManagement.setDataBase(incomeData.getDate(), incomeData.getBase(), answer);
            } else {
                adapterModelData.refresh(); //обновляю, чтобы показать List без результатов
                Toast.makeText(getApplicationContext(),
                        "Курсы обновляются в рабочие дни после 16.00 по msk", Toast.LENGTH_SHORT).show();
            }
        } else {
            adapterModelData.refresh();
            Toast.makeText(getApplicationContext(),
                    "Курсы на текущую дату по выбранной валюте отсутствуют", Toast.LENGTH_SHORT).show();
        }
    }

}
