package info.smart_tools.smartactors.core.iobject;

import java.util.regex.Pattern;

/**
 * A {@code FieldName} passed to {@code IObject} methods
 * as name of needed field. This class checks the validity of the name.
 */
public class FieldName implements IFieldName {

    /**
     * Pattern with valid symbols for {@code FieldName}
     */
    private static final Pattern VALID_SYMBOLS = Pattern.compile("[\\wа-яА-ЯёЁ\\-\\+=\\|!@#\\$%\\^&\\*:/\\., \\{\\}\\(\\)\\[\\]]+");

    private String name;

    /**
     * Base constructor for {@code FieldName}
     * @param name is name of field, it must not be {@code null} and
     *             must contain at least one of the symbols from {@literal 0-9a-zA-Z_-+=|!@#$%^&*:/., {}()[]}
     * @throws IllegalArgumentException if name is not valid
     */
    public FieldName(final String name) {
        initialize(name);
    }

    @Override
    public int hashCode() {
        return 23 + name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return (this == obj) ||
                ((obj instanceof FieldName)
                        && this.name.equals(((FieldName) obj).name)
                );
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Initialize class property {@code FieldName}
     * @param nameValue pretender name for {@code FieldName}
     */
    protected void initialize(final String nameValue) {
        if (nameValue == null) {
            throw new IllegalArgumentException("Name parameter must not be null");
        }
        if (nameValue.isEmpty()) {
            throw new IllegalArgumentException("Name parameter must not be empty");
        }
        if (!VALID_SYMBOLS.matcher(nameValue).matches()) {
            throw new IllegalArgumentException("Name parameter contains illegal symbols");
        }
        this.name = nameValue;
    }
}
