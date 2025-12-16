package webBackEnd.controller.Staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webBackEnd.entity.OrderDetail;
import webBackEnd.entity.Orders;
import webBackEnd.entity.RentAccountGame;
import webBackEnd.entity.Staff;
import webBackEnd.service.AdministratorService;
import webBackEnd.service.OrderDetailService;
import webBackEnd.service.OrdersService;
import webBackEnd.service.RentAccountGameService;
import webBackEnd.successfullyDat.GetQuantity;
import webBackEnd.successfullyDat.SendMailTest;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/staffHome")
public class ApproveController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private GetQuantity getQuantity;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private AdministratorService administratorService;
    @Autowired
    private RentAccountGameService rentAccountGameService;

    @Autowired
    private SendMailTest sendMailTest;


    @GetMapping("/approveList")
    public String approveList(Model model) {

        List<Orders> list = ordersService.findAllByStatus("WAIT");
        list.sort(Comparator.comparing(Orders::getCreatedDate));

        model.addAttribute("orderList", list);
        model.addAttribute("getQuantity", getQuantity);
        return "staff/ApproveList";
    }

    @GetMapping("/approve/{orderId}")
    public String viewOrderDetail(@PathVariable UUID orderId, Model model) {
        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);

        model.addAttribute("orderId", orderId);
        model.addAttribute("orderDetails", orderDetails);

        Orders order = ordersService.findById(orderId);
        model.addAttribute("order", order);
        return "staff/OrderDetail";
    }

    @PostMapping("/approve/accept")
    public String approveOrder(@RequestParam UUID orderId) {

        Orders order = ordersService.findById(orderId);

        List<OrderDetail> orderDetails = orderDetailService.findAllByOrderId(orderId);
        for (OrderDetail a : orderDetails) {
            if (a.getDuration() != 0) {
                RentAccountGame rentAccountGame = new RentAccountGame();

                rentAccountGame.setCustomer(order.getCustomer());
                rentAccountGame.setGameAccount(a.getGameAccount());
                rentAccountGame.setDateStart(order.getCreatedDate());
                rentAccountGame.setDateEnd(order.getCreatedDate().plusMonths(a.getDuration()));
                rentAccountGame.setStatus("Still valid");
                rentAccountGameService.save(rentAccountGame);
            }
            a.getGameAccount().setStatus("IN USE");

        }


        order.setStatus("COMPLETED");
        order.setStaff(
                administratorService.getStaffByID(UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91B")));
        ordersService.save(order);


        String title = "Xác nhận tài khoản của bạn";

        String content =
                "<p>Hãy nhấp vào liên kết dưới đây để kích hoạt tài khoản của bạn (hạn 2 phút):</p>"
                        + "<p><a href="

                        + "<p>Nếu không bấm được, copy link sau dán vào trình duyệt:<br></p>";
        sendMailTest.testSend(order.getCustomer().getEmail(), title, content);


        return "redirect:/staffHome/approveList";
    }


    @PostMapping("/approve/reject")
    public String rejectOrder(@RequestParam UUID orderId) {

        Orders order = ordersService.findById(orderId);

        order.setStatus("REJECTED");
        order.setStaff(
                administratorService.getStaffByID(UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91B")));
        ordersService.save(order);

        ordersService.save(order);

        return "redirect:/staffHome/approveList";
    }
}
