package mobiledimension.exchangerates;

import java.util.List;

/**
 * Created by Турал on 07.12.2017.
 */

class IncomeData {
    private String base;
    private String strDate;
    private List<ModelData> rates;
    private List<String> currenciesList;

    IncomeData(String base, String strDate, List<ModelData> rates, List<String> currenciesList) {
        this.base = base;
        this.strDate = strDate;
        this.rates = rates;
        this.currenciesList = currenciesList;
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
        return currenciesList;
    }
}
