package mobiledimension.exchangerates;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.lamerman.FileDialogOptions.OPTION_CURRENT_PATH_IN_TITLEBAR;
import static mobiledimension.exchangerates.ModelData.COMPARATOR_NAME;
import static mobiledimension.exchangerates.ModelData.COMPARATOR_VALUE_ASCENDING;
import static mobiledimension.exchangerates.ModelData.COMPARATOR_VALUE_DESCENDING;

//АПИ сайта  по запросу на второе и третье декабря 2017 года , возвращает результат на первое декабря.

public class MainMenu extends AppCompatActivity implements DatePickerFragment.DialogFragmentListener, AsyncUploadingData.AsyncResult {

    Spinner SpinnerOfCurrencies;
    TextView Current_Date;
    String currentDate;
    public DatePickerFragment datePickerFragment;
    Rates rates;
    String CurrentCurrency = "EUR";
    List<String> currencies = new ArrayList<>();
    /*List<String> currencies = new ArrayList<>(Arrays.asList("AUD", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP", "HKD", "HRK", "HUF", "IDR", "ILS",
            "INR", "JPY", "KRW", "MXN", "MYR", "NOK", "NZD", "PHP", "PLN", "RON", "RUB", "SEK", "SGD", "THB", "TRY", "USD", "ZAR"));*/


    ListView listView;
    private List<ModelData> modelDataArrayList = new ArrayList<>();
    AdapterModelData adapterModelData;
    SaveLoadDate saveLoadDate;
    String answer;
    Fragment fragmentAct;


    ArrayAdapter<String> SpinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //постоянно портретная ориентация
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //приложение на полный экран

        datePickerFragment = new DatePickerFragment();
        rates = new Rates();
        saveLoadDate = new SaveLoadDate(this);
        fragmentAct = new Fragment();

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

        SpinnerAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, currencies); //Адаптер для спиннера
        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerOfCurrencies.setAdapter(SpinnerAdapter);
        //SpinnerOfCurrencies.setSelection(currencies.indexOf("EUR")); //EUR установлена валютой по умолчанию в спиннере

        //Обработчик нажатия на спиннер
        SpinnerOfCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //Установленный цвет и размер не сохраняются после обновления спиннера, поэтому создан кастомный xml
               /* ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#ffff8800")); //Цвет текста выбранной позиции в спинере
                ((TextView) parent.getChildAt(0)).setTextSize(20);*/
                CurrentCurrency(position);
                UploadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        UploadData();
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
        saveLoadDate.SaveDate(answer, currentDate, CurrentCurrency);
    }

    public void onClickLoad(View view) {
        //Для файлового менеджера используется библиотека
        Intent intent = new Intent(getBaseContext(), FileDialog.class);
        // Устанавливаю директорию, в которой откроется загрузчик файлов
        intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory() + "/ExchangeRates");
        intent.putExtra(OPTION_CURRENT_PATH_IN_TITLEBAR, false);
        startActivityForResult(intent, FileDialog.REQUEST_LOAD);
    }


    // Метод интерфейса для связи с Активити из Фрагмента
    public void getDate(String date) {
        currentDate = date;
        UploadData();
    }


    private void UploadData() {
        String url = "https://api.fixer.io/" + currentDate + "?base=" + CurrentCurrency;
        AsyncUploadingData asyncUploadingData = new AsyncUploadingData(this);
        asyncUploadingData.execute(url);
        /*try {
            answer = asyncUploadingData.get(); //Вынести ожиданние ответа в калбек, во избежание ANR
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        */
    }

    //Метод интерфейса для обратного вызова из АсинкТаск
    public void getResult(String answer) {
        this.answer = answer;
        SetModelDataArrayList();
    }

    void SetModelDataArrayList() {

        if (answer != null) {
            Parser parser = new DynamicParser(answer);
            //класс, который хранит дату, установленную валюту и т.д. для удобной передачи из парсера,
            // а если нужно и для возврата из дессериализатора
            IncomeData incomeData = parser.getIncomeData();
            modelDataArrayList.clear();
            modelDataArrayList.addAll(incomeData.getRates()); //
            currencies.clear();
            currencies.addAll(incomeData.getCurrencies());
            Collections.sort(currencies);
            Refresh(incomeData.getDate(), incomeData.getBase());
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Курсы по данной валюте на выбранную дату отсутствуют", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    //region Этот метод был создан, думая , что мне нужен статический парсер.
    //Не увидел сразу, что список  валют меняется в зависимости от года
 /*
    void SetModelDataArrayList() {
        if (answer != null) {

            Parser parser = new StaticParser(answer);
            RootObject rootObject = parser.getRootObject();
            modelDataArrayList.clear();
            modelDataArrayList.addAll(rootObject.getRates().GetArrayListModelData());
            Refresh(date, CurrentCurrency);
            //modelDataArrayList.addAll(rootObject.getRates().GetArrayListModelData());
            // Refresh(rootObject.getDate(), rootObject.getBase());
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Курсы на установленную дату отсутствуют", Toast.LENGTH_SHORT);
            toast.show();
        }
    }*/
    //endregion


    void Refresh(String date, String currency) {
        Current_Date.setText(date);
        currentDate = date;
        SpinnerOfCurrencies.setSelection(currencies.indexOf(currency));
        CurrentCurrency = currency;
        SpinnerAdapter.notifyDataSetChanged();
        adapterModelData.Refresh();
    }

    @Override
    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {

        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == FileDialog.REQUEST_SAVE) {
                System.out.println("Saving...");
            } else if (requestCode == FileDialog.REQUEST_LOAD) {
                String FilePAth = data.getStringExtra(FileDialogOptions.RESULT_FILE);
                answer = saveLoadDate.LoadDate(FilePAth);
                if (answer != null) {
                    SetModelDataArrayList();
                }

                System.out.println("Loading...");
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {

        }

    }

}
