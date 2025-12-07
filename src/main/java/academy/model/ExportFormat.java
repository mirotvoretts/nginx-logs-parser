package academy.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExportFormat {
    JSON("json", ".json"),
    ADOC("adoc", ".ad"),
    MARKDOWN("markdown", ".md");

    private final String name;
    private final String extension;
}
