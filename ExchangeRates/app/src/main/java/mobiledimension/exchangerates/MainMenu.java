package mobiledimension.exchangerates;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lamerman.FileDialog;
import com.lamerman.FileDialogOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.lamerman.FileDialogOptions.OPTION_CURRENT_PATH_IN_TITLEBAR;
import static mobiledimension.exchangerates.ModelData.COMPARATOR_NAME;
import static mobiledimension.exchangerates.ModelData.COMPARATOR_VALUE_ASCENDING;
import static mobiledimension.exchangerates.ModelData.COMPARATOR_VALUE_DESCENDING;

public class MainMenu extends AppCompatActivity implements DatePickerFragment.DialogFragmentListener, AsyncUploadingData.AsyncResult {
    static final String LOG_TAG = "myLogs";
    private final int REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 234;
    DatabaseHelper databaseHelper;
    DatabaseManagement databaseManagement;
    private String currentDate;
    private String answer;
    private String CurrentCurrency = "EUR";
    private List<String> currencies = new ArrayList<>(Arrays.asList("EUR"));
    private TextView Current_Date;
    private List<ModelData> modelDataArrayList = new ArrayList<>();
    private ArrayAdapter<String> SpinnerAdapter;
    private AdapterModelData adapterModelData;
    private SaveLoadDate saveLoadDate;
    private Fragment fragmentAct;
    private DatePickerFragment datePickerFragment;
    private Spinner SpinnerOfCurrencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //постоянно портретная ориентация
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //приложение на полный экран
        datePickerFragment = new DatePickerFragment();
        saveLoadDate = new SaveLoadDate(this);
        fragmentAct = new Fragment();
        ListView listView;
        //region база данных
        databaseHelper = new DatabaseHelper(this);  // создаем объект для создания и управления версиями БД
        SQLiteDatabase exchangeRatesDatabase = databaseHelper.getWritableDatabase();
        databaseManagement = new DatabaseManagement(exchangeRatesDatabase, databaseHelper);
        //endregion


        //region findViewById
        Current_Date = (TextView) findViewById(R.id.Current_Date);
        SpinnerOfCurrencies = (Spinner) findViewById(R.id.spinnerCurrency);
        listView = (ListView) findViewById(R.id.ListView);
        //endregion

        //region предварительная установка текущей даты
        currentDate = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        Current_Date.setText(currentDate);
        //endregion

        adapterModelData = new AdapterModelData(this, R.layout.rates, modelDataArrayList); //Адаптер списка с курсом валют
        listView.setAdapter(adapterModelData);

        SpinnerAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, currencies); //Адаптер для спиннера
        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerOfCurrencies.setAdapter(SpinnerAdapter);

        //Обработчик нажатия на спиннер
        SpinnerOfCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                CurrentCurrency(position);
                UploadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }


    public void CurrentCurrency(int position) {
        CurrentCurrency = currencies.get(position); //сохраняю в переменную выбранную позицию в спинере
    }

    public void onClick(View view) {
        datePickerFragment.show(getSupportFragmentManager(), "DataPicker");
    }

    public void onClickSortName(View view) {
        Collections.sort(modelDataArrayList, COMPARATOR_NAME);
        adapterModelData.Refresh();
    }

    public void onClickSortAscending(View view) {
        Collections.sort(modelDataArrayList, COMPARATOR_VALUE_ASCENDING);
        adapterModelData.Refresh();
    }

    public void onClickSortDescending(View view) {
        Collections.sort(modelDataArrayList, COMPARATOR_VALUE_DESCENDING);
        adapterModelData.Refresh();
    }

    public void onClickSave(View view) {
        if (answer != null) { //Если не проверить, answer = null запишется
            saveLoadDate.SaveDate(answer, currentDate, CurrentCurrency);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Нет загруженных данных по последнему запросу", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onClickLoad(View view) {
        //Запрашиваю разрешение на чтение данных с SD
        // Проверка на версию api не требуется, т.к. используется библиотека поддержки
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
    }

    private void Load() {
        //Для файлового менеджера используется библиотека
        Intent intent = new Intent(getBaseContext(), FileDialog.class);
        // Устанавливаю директорию, в которой откроется загрузчик файлов
        intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory() + "/ExchangeRates");
        intent.putExtra(OPTION_CURRENT_PATH_IN_TITLEBAR, false);
        startActivityForResult(intent, FileDialog.REQUEST_LOAD);
    }


    // Метод интерфейса для связи с Активити из Фрагмента
    public void getDate(String date) {
        Current_Date.setText(date);
        currentDate = date;
        UploadData();
    }

    private void UploadData() {
        answer = databaseManagement.getAnswer(currentDate, CurrentCurrency);
        if (answer != null) {
            System.out.println("из выборки: " + answer);
            SetData();
        } else {
            String url = "https://api.fixer.io/" + currentDate + "?base=" + CurrentCurrency;
            AsyncUploadingData asyncUploadingData = new AsyncUploadingData(this);
            asyncUploadingData.execute(url);
        }

    }

    //Метод интерфейса для обратного вызова из АсинкТаск
    public void getResult(String answer) {
        this.answer = answer; //сделать переменную локальной после того как сохранение станет автоматическим
        SetData();
    }

    void SetData() {
        modelDataArrayList.clear();
        if (answer != null) {
            Parser parser = new Parser(answer);
            //класс, который хранит дату, установленную валюту и т.д. для удобной передачи из парсера,
            // а если нужно и для возврата из дессериализатора
            IncomeData incomeData = parser.getIncomeData();
            if (incomeData.getDate().equals(currentDate)) {
                modelDataArrayList.addAll(incomeData.getRates());
                currencies.clear();
                currencies.addAll(incomeData.getCurrencies());
                Collections.sort(currencies);
                SpinnerAdapter.notifyDataSetChanged();
                //Не всегда в спиннере после обновления будет стоять валюта по которой сделан запрос,
                //так как список спиннера тоже всегда обновляется, поэтому вручную устанавливаю текущую валюту
                SpinnerOfCurrencies.setSelection(currencies.indexOf(CurrentCurrency));
                adapterModelData.Refresh();
                save(incomeData.getDate(), incomeData.getBase(), answer);
            } else {
                adapterModelData.Refresh();
                Toast.makeText(getApplicationContext(),
                        "Курсы на выходные дни отсутствуют", Toast.LENGTH_SHORT).show();
            }
        } else {
            adapterModelData.Refresh();
            Toast.makeText(getApplicationContext(),
                    "Курсы на текущую дату по выбранной валюте отсутствуют", Toast.LENGTH_SHORT).show();
        }
    }

    void save(String date, String currency, String answer) {
        databaseManagement.setDatabase(date, currency, answer);
    }


    @Override
    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == FileDialog.REQUEST_LOAD) {
                String FilePAth = data.getStringExtra(FileDialogOptions.RESULT_FILE);
                answer = saveLoadDate.LoadDate(FilePAth);
                SetData();
            }
        }
    }

    @Override
    public synchronized void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Load();
                }
            }
        }
    }
}
