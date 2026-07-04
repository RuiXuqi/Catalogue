package {{ package }};

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CatalogueConstants {
    private CatalogueConstants() {
    }

    public static final String MOD_ID = "{{ mod_id }}";
    public static final String MOD_NAME = "{{ mod_name }}";
    public static final String VERSION = "{{ mod_version }}";
}
