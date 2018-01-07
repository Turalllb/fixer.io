package mobiledimension.exchangerates;

import java.util.List;

/**
 * Created by Турал on 07.12.2017.
 */

class IncomeData {

    private String base;
    private String strDate;
    private List<ModelData> rates;
    private List<String> currencies;

    IncomeData(String base, String strDate, List<ModelData> rates, List<String> currencies) {
        this.base = base;
        this.strDate = strDate;
        this.rates = rates;
        this.currencies = currencies;
    }

    String getBase() {
        return base;
    }

    String getDate() {
        return strDate;
    }

    List<ModelData> getRates() {
        return rates;
    }

    List<String> getCurrencies() {
        return currencies;
    }
}
