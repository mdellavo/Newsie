package org.quuux.newsie;


import com.squareup.otto.Bus;

public class EventBus {

    private static Bus instance = new Bus();

    public static Bus getInstance() {
        return instance;
    }
}
