package com.masonsoft.imsdk.uikit;

import java.io.Closeable;

public interface DataLoader extends Closeable {
    void requestLoadData();
}
