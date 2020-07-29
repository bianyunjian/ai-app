package com.hankutech.ax.appdemo.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelGroups {

    private static ConcurrentHashMap<ChannelId, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void add(Channel channel) {
        remove(channel.id());
        CHANNEL_MAP.put(channel.id(), channel);
    }

    public static boolean remove(ChannelId channelId) {
        if (contains(channelId)) {
            CHANNEL_MAP.remove(channelId);
        }
        return true;
    }

    public static boolean contains(ChannelId channelId) {
        return CHANNEL_MAP.contains(channelId);
    }

    public static void broadcast(Object msg, ChannelId... channelIds) {
        if (channelIds == null || channelIds.length == 0) {
            channelIds = CHANNEL_MAP.keySet().toArray(new ChannelId[0]);
        }

        for (ChannelId id : channelIds) {
            try {
                if (contains(id) == false) continue;
                Channel channel = CHANNEL_MAP.get(id);

                if (channel.isWritable()) {
                    channel.write(msg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void broadcast(Object msg) {
        for (ChannelId id : CHANNEL_MAP.keySet()) {
            try {
                Channel channel = CHANNEL_MAP.get(id);
                if (channel.isWritable()) {
                    channel.write(msg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void clear() {
        CHANNEL_MAP.clear();
    }


    public static int size() {
        return CHANNEL_MAP.size();
    }
}
