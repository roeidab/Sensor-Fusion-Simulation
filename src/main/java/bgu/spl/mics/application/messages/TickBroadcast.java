package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    private int currentTick;

    public TickBroadcast(int tick)
    {
        this.currentTick = tick;
    }

    public int getCurrentTick() {
        return currentTick;
    }
}
