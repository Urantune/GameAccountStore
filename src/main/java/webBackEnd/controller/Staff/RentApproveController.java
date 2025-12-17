package webBackEnd.controller.Staff;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.entity.RentAccountGame;
import webBackEnd.service.OrderDetailService;
import webBackEnd.service.OrdersService;
import webBackEnd.service.RentAccountGameService;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/staffHome")
public class RentApproveController {

    @Autowired private OrdersService ordersService;
    @Autowired private OrderDetailService orderDetailService;
    @Autowired private RentAccountGameService rentAccountGameService;

    @GetMapping("/rentApproveList")
    public String rentApproveList(Model model) {

        List<Orders> list = ordersService.findAllByStatus("WAITRENT");
        list.sort(Comparator.comparing(Orders::getCreatedDate).reversed());

        Map<UUID, OrderDetail> firstDetailMap = new HashMap<>();
        for (Orders o : list) {
            List<OrderDetail> details = orderDetailService.findAllByOrderId(o.getId());
            if (details != null && !details.isEmpty()) {
                firstDetailMap.put(o.getId(), details.get(0));
            }
        }

        model.addAttribute("orderList", list);
        model.addAttribute("firstDetailMap", firstDetailMap);

        return "staff/RentApproveList";
    }

    @GetMapping("/rentApprove/{orderId}")
    public String viewOrderDetail(@PathVariable UUID orderId, Model model) {
        Orders order = ordersService.findById(orderId);
        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);

        model.addAttribute("orderId", orderId);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);

        return "staff/RentOrderDetail";
    }

    @PostMapping("/rentApprove/decision")
    @Transactional
    public String decision(@RequestParam("orderId") UUID orderId,
                           @RequestParam("decision") String decision,
                           RedirectAttributes ra) {

        Orders order = ordersService.findById(orderId);
        if (order == null) {
            ra.addFlashAttribute("errorMessage", "Order not found.");
            return "redirect:/staffHome/rentApproveList";
        }

        if (!"WAITRENT".equalsIgnoreCase(order.getStatus())) {
            ra.addFlashAttribute("errorMessage", "Order is not WAITRENT.");
            return "redirect:/staffHome/rentApprove/" + orderId;
        }

        if ("REJECT".equalsIgnoreCase(decision)) {
            order.setStatus("REJECT");
            ordersService.save(order);
            ra.addFlashAttribute("successMessage", "Rejected rent order.");
            return "redirect:/staffHome/rentApproveList";
        }

        if (!"ACCEPT".equalsIgnoreCase(decision)) {
            ra.addFlashAttribute("errorMessage", "Invalid decision.");
            return "redirect:/staffHome/rentApprove/" + orderId;
        }

        List<OrderDetail> details = orderDetailService.findAllByOrderId(orderId);
        if (details == null || details.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "OrderDetail empty.");
            return "redirect:/staffHome/rentApprove/" + orderId;
        }

        UUID customerId = (order.getCustomer() != null) ? order.getCustomer().getCustomerId() : null;
        if (customerId == null) {
            ra.addFlashAttribute("errorMessage", "Order missing customer.");
            return "redirect:/staffHome/rentApprove/" + orderId;
        }

        LocalDateTime now = LocalDateTime.now();

        for (OrderDetail od : details) {
            if (od.getGameAccount() == null || od.getGameAccount().getGameAccountId() == null) {
                ra.addFlashAttribute("errorMessage", "OrderDetail missing gameAccount.");
                return "redirect:/staffHome/rentApprove/" + orderId;
            }

            UUID gaId = od.getGameAccount().getGameAccountId();
            int months = (od.getDuration() != null ? od.getDuration() : 1);
            if (months <= 0) months = 1;

            Optional<RentAccountGame> optRent =
                    rentAccountGameService.findByCustomerIdAndGameAccountId(customerId, gaId);

            if (optRent.isEmpty()) {
                ra.addFlashAttribute("errorMessage", "Rent record not found for GA: " + gaId);
                return "redirect:/staffHome/rentApprove/" + orderId;
            }

            RentAccountGame rent = optRent.get();

            LocalDateTime end = rent.getDateEnd();
            if (end == null || end.isBefore(now)) end = now;

            rent.setDateEnd(end.plusMonths(months));
            rent.setStatus("STILL VAILD");
            rentAccountGameService.save(rent);
        }

        order.setStatus("COMPLETED");
        ordersService.save(order);

        ra.addFlashAttribute("successMessage", "Accepted. Rent extended and order COMPLETED.");
        return "redirect:/staffHome/rentApproveList";
    }
}
