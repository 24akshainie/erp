package domain;

public class Settings {

    private String key;
    private String value;

    // Constructor
    public Settings(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Getters 
    public String getKey() { return key;}
    public String getValue() { return value;}

    // Setters 
    public void setKey(String key) {this.key = key;} 
    public void setValue(String value) {this.value = value;}


    public boolean asBoolean() {
        return "true".equalsIgnoreCase(value);
    }

    public Integer asInt() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Settings{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}