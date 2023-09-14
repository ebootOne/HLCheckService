package com.xbase.tool;

import android.content.Context;

public class UtilsBridge {


    /**
     * SPUtils
     * */
    static SPUtils getSpUtils4Utils(Context context) {
        return SPUtils.getInstance(context,"Utils");
    }

}
