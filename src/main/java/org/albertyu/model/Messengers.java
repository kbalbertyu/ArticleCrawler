package org.albertyu.model;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 2019/8/4 7:16
 */
public class Messengers {
    private static List<Messenger> messengerList = new ArrayList<>();

    public void add(Messenger messenger) {
        messengerList.add(messenger);
    }

    public boolean isNotEmpty() {
        return CollectionUtils.isNotEmpty(messengerList);
    }

    public List<Messenger> getList() {
        return messengerList;
    }

    public void clear() {
        messengerList.clear();
    }
}
