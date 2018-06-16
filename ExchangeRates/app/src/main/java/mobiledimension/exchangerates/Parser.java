package mobiledimension.exchangerates;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Турал on 05.12.2017.
 */

class Parser {
    private IncomeData incomeData;

    Parser(String answer) throws JSONException {
        deserialize(answer);
    }

    private void deserialize(String json) throws JSONException {
        //парсим строку в json
        JSONObject jsonObject = new JSONObject(json);
        //забираем base и date
        String base = jsonObject.get("base").toString();
        String strDate = jsonObject.get("date").toString();
        //заберем объект rates и пройдемся по нему итератором
        //все валюты сложим в свои ModelData и все сложим в лист
        JSONObject rates = jsonObject.getJSONObject("rates");
        Iterator keys = rates.keys();
        List<ModelData> ratesList = new ArrayList<>();
        List<String> currenciesList = new ArrayList<String>();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            System.out.println();
            currenciesList.add(key);
            //Number потому, что по ключу "IDR" прилетает Integer
            Number value = (Number) rates.get(key);
            ratesList.add(new ModelData(key, value.doubleValue()));
        }
        //собираем IncomeData из полученных данных
        incomeData = new IncomeData(base, strDate, ratesList, currenciesList);
    }

    IncomeData getIncomeData() {
        return incomeData;
    }
}
