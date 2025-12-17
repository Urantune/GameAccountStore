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


        StringBuilder accountHtml = new StringBuilder();
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


            accountHtml.append("<b>game:</b> ").append(a.getGameAccount().getGame().getGameName()).append("<br>")
                    .append("<b>username:</b> ").append(a.getGameAccount().getGameAccount()).append("<br>")
                    .append("<b>password:</b> ").append(a.getGameAccount().getGamePassword()).append("<br>")
                    .append("<br>"); // cách dòng giữa các account


        }


        order.setStatus("COMPLETED");
        order.setStaff(
                administratorService.getStaffByID(UUID.fromString("88A7A905-CB27-431C-BFED-1D16BEA9B91B")));
        ordersService.save(order);

        String title = "Xác nhận tài khoản của bạn";

        String content =
                "Xin chào <b>" + order.getCustomer().getUsername() + "</b>,<br><br>"
                        + "Cảm ơn bạn đã tin tưởng và mua hàng tại <b>ACCOUNT GAME STORE</b> của chúng tôi.<br>"
                        + "Đơn hàng của bạn đã được xử lý thành công.<br><br>"
                        + "Dưới đây là thông tin tài khoản game mà bạn đã mua:<br><br>"
                        + "<div style='padding:12px;border:1px solid #ddd;border-radius:8px;'>"
                        + accountHtml
                        + "</div><br>"
                        + "<b>LƯU Ý QUAN TRỌNG:</b><br>"
                        + "- Không chia sẻ thông tin tài khoản cho người khác.<br>"
                        + "- Nếu phát sinh lỗi đăng nhập hoặc tài khoản không đúng mô tả, hãy liên hệ với chúng tôi trong vòng 24h để được hỗ trợ.<br><br>"
                        + "<b>HỖ TRỢ KHÁCH HÀNG:</b><br>"
                        + "Hotline: 0923 445 566<br>"
                        + "Email: support@shopgame.vn<br>"
                        + "Hỗ trợ 24/7 – Phản hồi nhanh<br><br>"
                        + "Chúc bạn có những giây phút trải nghiệm game vui vẻ!<br>"
                        + "Trân trọng,<br><br>"
                        + "<b>ACCOUNT GAME STORE</b><br>"
                        + "Uy tín – Giá tốt – Giao dịch tự động 24/7";



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
