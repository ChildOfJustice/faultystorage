package org.yuldashev.s3server.validation;

import javax.validation.constraints.NotNull;

public final class Constants {
    @NotNull
    public static final String FILENAME_REGEX = "^[\\w,\\s-]+\\.[A-Za-z]{3}$";

    private Constants() {
    }
}
