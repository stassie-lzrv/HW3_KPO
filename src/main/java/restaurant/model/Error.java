package restaurant.model;

public class Error extends Exception {
    public String err_type;
    public String err_entity;
    public String err_field;

    public Error(String err_type, String err_entity, String err_field) {
        this.err_type = err_type;
        this.err_entity = err_entity;
        this.err_field = err_field;
    }
}

