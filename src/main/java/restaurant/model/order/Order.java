package restaurant.model.order;

import java.util.ArrayList;
import java.util.Date;

public class Order {
    public String vis_name;
    public Date vis_ord_started;
    public Date vis_ord_ended;
    public int vis_ord_total;
    public ArrayList<OrderDish> vis_ord_dishes;
}
