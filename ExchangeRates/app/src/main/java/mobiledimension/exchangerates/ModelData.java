package mobiledimension.exchangerates;


import java.util.Comparator;

/**
 * Created by Турал on 30.11.2017.
 */

class ModelData {

    private String name;
    private Double value;


    ModelData(String name, Double value) {
        this.name = name;
        this.value = value;
    }

    String getName() {
        return this.name;
    }

    Double getValue() {
        return this.value;
    }

    static Comparator<ModelData> COMPARATOR_NAME = new Comparator<ModelData>() {
        @Override
        public int compare(ModelData o1, ModelData o2) {
            return o1.name.compareTo(o2.name);
        }
    };

    static Comparator<ModelData> COMPARATOR_VALUE_DESCENDING = new Comparator<ModelData>() {
        @Override
        public int compare(ModelData o1, ModelData o2) {
            return o1.value.compareTo(o2.value);
        }
    };

    static Comparator<ModelData> COMPARATOR_VALUE_ASCENDING = new Comparator<ModelData>() {
        @Override
        public int compare(ModelData o1, ModelData o2) {
            return o2.value.compareTo(o1.value);
        }

    };

    public boolean equals(Object other) {
        if (this == other) return true;
        if (getClass() != other.getClass()) return false;
        ModelData otherData = (ModelData) other;
        if (name.equals(otherData.name) && value == otherData.value) return true;
        return false;
    }
}
