package club.crestmc.queue.util;

import java.util.Collection;
import java.util.Map;

public class Collections {

    public static <K, V> int getPositionOfKey(Map<K, V> map, Object key) {
        Object[] keys = map.keySet().toArray(new Object[0]);

        for (int i = 0; i < map.keySet().size(); i++) {
            if (keys[i].equals(key)) {
                return i;
            }
        }

        return 0;
    }

    public static <V> int getPositionOfObject(Collection<V> collection, Object key) {
        Object[] keys = collection.toArray(new Object[0]);

        for (int i = 0; i < collection.size(); i++) {
            if (keys[i].equals(key)) {
                return i;
            }
        }

        return 0;
    }

}
