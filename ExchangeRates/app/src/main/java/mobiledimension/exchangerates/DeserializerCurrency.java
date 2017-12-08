package mobiledimension.exchangerates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Турал on 07.12.2017.
 */

public class DeserializerCurrency implements JsonDeserializer<IncomeData> {

    @Override
    public IncomeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {


        List<ModelData> ListModelData = new ArrayList<>();
        List<String> currencies = new ArrayList<>();
        if (json.isJsonObject()) {
            Set<Map.Entry<String, JsonElement>> entrySet = json.getAsJsonObject().entrySet();
            if (entrySet.size() > 0) {
                Iterator<Map.Entry<String, JsonElement>> entries = entrySet.iterator();
                while (entries.hasNext()) {
                    Map.Entry<String, JsonElement> entry = entries.next();
                    ModelData md = new ModelData(entry.getKey(), entry.getValue().getAsDouble());
                    ListModelData.add(md);
                    currencies.add(md.getName());
                }
            }
        }

        return new IncomeData(ListModelData);
    }
}
