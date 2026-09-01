package com.myfarmproduce.application.service;

import java.io.InputStream;

public interface FileStorage {
    /** Saves an uploaded image under {@code folder} and returns a relative URL. */
    String saveImage(InputStream content, String originalFileName, String folder);
}
