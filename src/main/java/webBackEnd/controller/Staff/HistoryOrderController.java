package webBackEnd.controller.Staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.service.OrderDetailService;
import webBackEnd.service.OrdersService;
import webBackEnd.successfullyDat.GetQuantity;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/staffHome")
public class HistoryOrderController {

    @Autowired private OrdersService ordersService;
    @Autowired private OrderDetailService orderDetailService;
    @Autowired private GetQuantity getQuantity;

    @GetMapping("/historyOrders")
    public String historyOrders(Model model) {

        List<Orders> complete = ordersService.findHistoryOrders("COMPLETED");
        List<Orders> reject = ordersService.findHistoryOrders("REJECTED");

        List<Orders> orderList = new ArrayList<>();
        if (complete != null) orderList.addAll(complete);
        if (reject != null) orderList.addAll(reject);

        orderList.sort((a, b) -> {
            LocalDateTime da = a != null ? a.getCreatedDate() : null;
            LocalDateTime db = b != null ? b.getCreatedDate() : null;
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.compareTo(da);
        });

        Map<UUID, String> orderTypeMap = new HashMap<>();
        for (Orders o : orderList) {
            List<OrderDetail> details = orderDetailService.findByOrder(o);

            String type = "N/A";
            if (details != null && !details.isEmpty()) {
                boolean anyRent = details.stream().anyMatch(d -> d.getDuration() != null && d.getDuration() > 0);
                type = anyRent ? "RENT" : "BUY";
            }
            orderTypeMap.put(o.getId(), type);
        }

        model.addAttribute("orderList", orderList);
        model.addAttribute("orderTypeMap", orderTypeMap);
        model.addAttribute("getQuantity", getQuantity);

        return "staff/HistoryOrder";
    }

    @GetMapping("/historyOrders/detail/{orderId}")
    public String historyOrderDetail(@PathVariable UUID orderId, Model model) {

        Orders order = ordersService.findOrderById(orderId);
        if (order == null) {
            model.addAttribute("order", null);
            model.addAttribute("orderDetails", Collections.emptyList());
            model.addAttribute("errorMessage", "ORDER NOT FOUND.");
            return "staff/HistoryOrderDetail";
        }

        List<OrderDetail> orderDetails = orderDetailService.findByOrder(order);
        if (orderDetails == null) orderDetails = new ArrayList<>();

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);

        return "staff/HistoryOrderDetail";
    }
}
