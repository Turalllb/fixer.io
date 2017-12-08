package mobiledimension.exchangerates;

/**
 * Created by Турал on 07.12.2017.
 */

class StaticParser extends Parser {


    StaticParser(String answer) {
        super(answer);
        rootObject = GSON.fromJson(answer, RootObject.class);
    }


}
