package academy.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImportFormat {
    TEXT(".txt"),
    LOG(".log");

    private final String extension;
}
