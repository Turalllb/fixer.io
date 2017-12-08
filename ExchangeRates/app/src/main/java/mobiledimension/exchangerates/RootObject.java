package mobiledimension.exchangerates;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Турал on 29.11.2017.
 */

class RootObject {

    private String base;
    private String date;
    private Rates rates;
    //private ArrayList<ModelData> rates;

    String getBase() {
        return this.base;
    }

    String getDate() {
        return this.date;
    }

    Rates getRates() {
        return this.rates;
    }


}
