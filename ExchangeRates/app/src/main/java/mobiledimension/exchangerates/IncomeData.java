package mobiledimension.exchangerates;

import java.util.List;

/**
 * Created by Турал on 07.12.2017.
 */

class IncomeData {

    private String base;
    private String date;
    private List<ModelData> rates;
    private List<String> currencies;

    IncomeData(String base, String date, List<ModelData> rates, List<String> currencies) {
        this.base = base;
        this.date = date;
        this.rates = rates;
        this.currencies = currencies;
    }

    IncomeData(List<ModelData> rates) {
        this.rates = rates;
    }


    String getBase() {
        return base;
    }

    String getDate() {
        return date;
    }

    List<ModelData> getRates() {
        return rates;
    }

    List<String> getCurrencies() {
        return currencies;
    }
}
