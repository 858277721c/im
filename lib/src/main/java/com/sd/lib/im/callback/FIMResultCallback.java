package com.sd.lib.im.callback;

/**
 * IM通用的结果回调
 *
 * @param <T> 结果数据类型
 */
public abstract class FIMResultCallback<T>
{
    private static final String KEY = "$";

    /**
     * 返回callback对应的tag，可用于ui销毁的时候移除callback
     *
     * @return
     */
    public String getTag()
    {
        String className = getClass().getName();
        final int keyIndex = className.indexOf(KEY);
        if (keyIndex > 0)
            className = className.substring(0, keyIndex);
        return className;
    }

    /**
     * 成功回调
     *
     * @param result
     */
    public abstract void onSuccess(T result);

    /**
     * 失败回调
     *
     * @param code 错误码
     * @param desc 失败描述
     */
    public abstract void onError(int code, String desc);
}
