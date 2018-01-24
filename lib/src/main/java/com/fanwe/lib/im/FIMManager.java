/*
 * Copyright (C) 2017 zhengjun, fanwe (http://www.fanwe.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fanwe.lib.im;

import android.text.TextUtils;
import android.util.Log;

import com.fanwe.lib.im.callback.FIMMsgCallback;
import com.fanwe.lib.im.callback.FIMResultCallback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * IM管理基类
 */
public class FIMManager
{
    public static final String TAG = "FIM";

    private static FIMManager sInstance;

    private FIMHandler mIMHandler;

    private final Map<String, FIMResultCallbackInfo> mMapResultCallback = new HashMap<>();
    private final List<FIMMsgCallback> mListMsgCallback = new CopyOnWriteArrayList<>();

    private boolean mIsDebug;

    private FIMManager()
    {
    }

    public static FIMManager getInstance()
    {
        if (sInstance == null)
        {
            synchronized (FIMManager.class)
            {
                if (sInstance == null)
                {
                    sInstance = new FIMManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 设置是否调试模式
     *
     * @param debug
     */
    public void setDebug(boolean debug)
    {
        mIsDebug = debug;
    }

    /**
     * 是否调试模式
     *
     * @return
     */
    public boolean isDebug()
    {
        return mIsDebug;
    }

    /**
     * 设置IM处理对象
     *
     * @param handler
     */
    public void setIMHandler(FIMHandler handler)
    {
        mIMHandler = handler;
    }

    /**
     * 返回IM处理类
     *
     * @return
     */
    private FIMHandler getIMHandler()
    {
        if (mIMHandler == null)
        {
            throw new NullPointerException("you must set a FIMHandler to FIMManager before this");
        }
        return mIMHandler;
    }

    /**
     * 添加消息回调
     *
     * @param callback
     */
    public synchronized void addMsgCallback(FIMMsgCallback callback)
    {
        if (callback == null || mListMsgCallback.contains(callback))
        {
            return;
        }
        mListMsgCallback.add(callback);

        if (mIsDebug)
        {
            Log.i(TAG, "FIMMsgCallback add size " + mListMsgCallback.size() + " " + callback);
        }
    }

    /**
     * 移除消息回调
     *
     * @param callback
     */
    public synchronized void removeMsgCallback(FIMMsgCallback callback)
    {
        if (mListMsgCallback.remove(callback))
        {
            if (mIsDebug)
            {
                Log.e(TAG, "FIMMsgCallback remove size " + mListMsgCallback.size() + " " + callback);
            }
        }
    }

    FIMMsgCallback mInternalMsgCallback = new FIMMsgCallback()
    {

        @Override
        public boolean ignoreMsg(FIMMsg fimMsg)
        {
            return false;
        }

        @Override
        public void onReceiveMsg(FIMMsg fimMsg)
        {
            synchronized (FIMManager.this)
            {
                Iterator<FIMMsgCallback> it = mListMsgCallback.iterator();
                while (it.hasNext())
                {
                    FIMMsgCallback item = it.next();

                    if (item.ignoreMsg(fimMsg))
                    {
                        // 忽略当前消息
                    } else
                    {
                        item.onReceiveMsg(fimMsg);
                    }
                }
            }
        }
    };

    /**
     * 返回新创建的第三方IM消息接收对象
     *
     * @return
     */
    public FIMMsgReceiver newMsgReceiver()
    {
        return getIMHandler().newMsgReceiver();
    }

    /**
     * 发送C2C消息
     *
     * @param peer 对方id
     * @param data 要发送的数据
     * @return
     */
    public FIMMsg sendMsgC2C(String peer, FIMMsgData data, FIMResultCallback<FIMMsg> callback)
    {
        return getIMHandler().sendMsg(peer, data, FIMConversationType.C2C, generateCallbackId(callback));
    }

    /**
     * 发送Group消息
     *
     * @param peer group id
     * @param data 要发送的数据
     * @return
     */
    public FIMMsg sendMsgGroup(String peer, FIMMsgData data, FIMResultCallback<FIMMsg> callback)
    {
        return getIMHandler().sendMsg(peer, data, FIMConversationType.Group, generateCallbackId(callback));
    }

    /**
     * 加入群组
     *
     * @param groupId  群组id
     * @param callback
     */
    public void joinGroup(String groupId, FIMResultCallback callback)
    {
        getIMHandler().joinGroup(groupId, generateCallbackId(callback));
    }

    /**
     * 退出群组
     *
     * @param groupId  群组id
     * @param callback
     */
    public void quitGroup(String groupId, FIMResultCallback callback)
    {
        getIMHandler().quitGroup(groupId, generateCallbackId(callback));
    }

    /**
     * 移除并返回结果回调
     *
     * @param callbackId 回调对应的id
     * @return
     */
    synchronized FIMResultCallback removeResultCallback(String callbackId)
    {
        FIMResultCallbackInfo info = mMapResultCallback.remove(callbackId);
        if (info != null)
        {
            return info.callback;
        } else
        {
            return null;
        }
    }

    /**
     * 根据tag移除结果回调
     *
     * @param tag
     * @return 移除的数量
     */
    public synchronized int removeResultCallbackByTag(String tag)
    {
        int count = 0;
        if (TextUtils.isEmpty(tag) || mMapResultCallback.isEmpty())
        {
            return count;
        }

        Iterator<Map.Entry<String, FIMResultCallbackInfo>> it = mMapResultCallback.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, FIMResultCallbackInfo> item = it.next();
            FIMResultCallbackInfo info = item.getValue();

            if (tag.equals(info.tag))
            {
                it.remove();
                count++;
            }
        }

        return count;
    }

    /**
     * 移除过期的回调对象
     *
     * @param expireTime 过期时间（毫秒）
     */
    public synchronized void removeExpiredResultCallback(long expireTime)
    {
        if (expireTime <= 0 || mMapResultCallback.isEmpty())
        {
            return;
        }

        final long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, FIMResultCallbackInfo>> it = mMapResultCallback.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, FIMResultCallbackInfo> item = it.next();
            FIMResultCallbackInfo info = item.getValue();

            if (currentTime - info.createTime >= expireTime)
            {
                it.remove();
                if (mIsDebug)
                {
                    Log.e(TAG, "removeExpiredResultCallback:" + info.callback);
                }
            }
        }
    }

    /**
     * 生成callback的标识
     *
     * @param callback
     * @return
     */
    private synchronized String generateCallbackId(FIMResultCallback callback)
    {
        if (callback == null)
        {
            return null;
        }

        final String callbackId = String.valueOf(UUID.randomUUID());
        FIMResultCallbackInfo info = new FIMResultCallbackInfo(callback, callback.getTag());

        mMapResultCallback.put(callbackId, info);
        return callbackId;
    }
}
