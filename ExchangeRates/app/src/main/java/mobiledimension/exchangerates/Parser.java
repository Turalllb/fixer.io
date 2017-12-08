package mobiledimension.exchangerates;

import com.google.gson.Gson;

/**
 * Created by Турал on 05.12.2017.
 */

abstract class Parser {

    RootObject rootObject;
    Gson GSON = new Gson();
    IncomeData incomeData;

    Parser(String json) {
    }


    IncomeData getIncomeData() {
        return incomeData;
    }

    RootObject getRootObject() {
        return rootObject;
    }


/* Можно использовать, если нужен дессериализатор. В данный момент он возвращает только массив курсов
    void GetData() {
        Object obj = new Object();
        try {
            // Считываем json
            obj = new JSONParser().parse(answer);
        } catch (Exception e) {
        }
        JSONObject jo = (JSONObject) obj;
        String Currency = (String) jo.get("base");
        String Date = (String) jo.get("date");
        JSONObject rates = (JSONObject) jo.get("rates");

        Type type = new TypeToken<IncomeData>(){}.getType();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(type, new DeserializerCurrency())
                .create();


        IncomeData modelData = gson.fromJson(rates.toJSONString(), type);

    }
*/



}
