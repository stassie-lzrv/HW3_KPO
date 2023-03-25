package restaurant.model.cooking;

import java.util.ArrayList;
import java.util.Date;

public class CookingLog {
    public int proc_id;
    public int ord_dish;
    public Date proc_started;
    public Date proc_ended;
    public boolean proc_active;
    public ArrayList<CookingOperation> proc_operations;
}