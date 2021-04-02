package org.yuldashev.s3server.util;

import org.springframework.core.io.InputStreamResource;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class Util {
    private Util() {
    }

    @NotNull
    public static InputStreamResource asOctetStream(@NotNull final byte[] content) {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            return new InputStreamResource(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
