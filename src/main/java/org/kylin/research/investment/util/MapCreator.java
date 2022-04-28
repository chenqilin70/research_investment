package org.kylin.research.investment.util;

import java.util.HashMap;
import java.util.Map;


public class MapCreator<K, V> {
    public final static MapCreator<String, String> SS = new MapCreator<>();
    public final static MapCreator<String, Integer> SI = new MapCreator<>();
    public final static MapCreator<Integer, String> IS = new MapCreator<>();
    public final static MapCreator<String, Object> SO = new MapCreator<>();
    public final static MapCreator<String, Boolean> SB = new MapCreator<>();

    /**
     * 偶数下标的参数为key，奇数下标的参数为value
     *
     * @param args
     * @return
     */
    public Map<K, V> create(Object... args) {
        Map<K, V> map = new HashMap();
        append(map, args);
        return map;
    }

    public void append(Map<K, V> map, Object... args) {
        checkArgs(args);
        put(map, args);
    }

    /**
     * 如果传入的map为空，则创建并返回，如果传入的map不为空，则添加元素
     *
     * @param map
     * @param args
     * @return
     */
    public Map<K, V> createOrAppend(Map<K, V> map, Object... args) {
        if (map == null) {
            map = create(args);
        } else {
            append(map, args);
        }
        return map;
    }

    private void put(Map<K, V> map, Object... args) {
        K key = null;
        V val = null;
        for (int i = 0; i < args.length; i++) {
            if (i % 2 == 0) {
                key = (K) args[i];
            } else {
                val = (V) args[i];
                map.put((K) key, (V) val);
            }
        }
    }

    private void checkArgs(Object... args) {
        if (args.length % 2 != 0) {
            throw new RuntimeException("MapUtil.append参数必须为偶数！");
        }
    }

}
