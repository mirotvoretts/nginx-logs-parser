package academy.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExitCode {
    OK(0),
    UNEXPECTED_ERROR(1),
    INVALID_ARGUMENTS(2);

    private final int code;
}
