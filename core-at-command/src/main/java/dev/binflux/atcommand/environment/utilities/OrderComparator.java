package dev.binflux.atcommand.environment.utilities;

import dev.binflux.atcommand.environment.meta.MethodMeta;

import java.util.Comparator;

public class OrderComparator implements Comparator<MethodMeta> {

    @Override
    public int compare(MethodMeta o1, MethodMeta o2) {
        if (o1 != null && o2 != null) {
            return o2.getOrder() - o1.getOrder();
        } else if (o1 != null) {
            return -1;
        } else if (o2 != null) {
            return 1;
        }
        return 0;
    }
}