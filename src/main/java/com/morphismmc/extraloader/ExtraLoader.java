package com.morphismmc.extraloader;

import com.mojang.logging.LogUtils;
import com.morphismmc.extraloader.core.Common;
import org.slf4j.Logger;

public interface ExtraLoader {

    String ID = "extraloader";
    String NAME = "Extra Loader";
    Logger LOGGER = LogUtils.getLogger();

    static ExtraLoader instance() {
        return Common.instance;
    }

}
