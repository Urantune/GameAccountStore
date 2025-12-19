package webBackEnd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import webBackEnd.successfullyDat.SendMailTest;

import java.math.BigDecimal;

@Service
public class MailAsyncService {

    @Autowired private SendMailTest sendMailTest;

    @Async
    public void sendRejectEmail(String to, String orderId, BigDecimal refund) {
        if (to == null || to.trim().isEmpty()) return;

        String body =
                "<b>Kính chào quý khách,</b><br><br>" +
                        "Đơn hàng của bạn đã bị <b>TỪ CHỐI</b>.<br><br>" +
                        "<b>MÃ ĐƠN HÀNG:</b> " + orderId + "<br>" +
                        "<b>SỐ TIỀN HOÀN:</b> " + (refund != null ? refund.toPlainString() : "0") + " đ<br><br>" +
                        "Nếu bạn có sử dụng voucher, hệ thống đã mở lại để bạn dùng lần sau.<br><br>" +
                        "Xin cảm ơn.";

        sendMailTest.testSend(to, "ĐƠN HÀNG BỊ TỪ CHỐI", body);
    }

    @Async
    public void sendAcceptEmail(String to, String accountHtml) {
        if (to == null || to.trim().isEmpty()) return;

        String body =
                "<b>Kính chào quý khách,</b><br><br>" +
                        "Đơn hàng của bạn đã được <b>XÁC NHẬN THÀNH CÔNG</b>.<br>" +
                        "Dưới đây là thông tin tài khoản game bạn đã mua / thuê:<br><br>" +
                        accountHtml +
                        "<hr>" +
                        "<b>LƯU Ý QUAN TRỌNG:</b><br>" +
                        "- Vui lòng <b>ĐỔI MẬT KHẨU NGAY</b> sau khi đăng nhập.<br>" +
                        "- Không chia sẻ thông tin tài khoản cho người khác.<br>" +
                        "- Với tài khoản thuê, hệ thống sẽ <b>TỰ ĐỘNG THU HỒI</b> khi hết hạn.<br>" +
                        "- Khiếu nại trong vòng <b>24 GIỜ</b>.<br><br>" +
                        "Xin cảm ơn.";

        sendMailTest.testSend(to, "THÔNG TIN TÀI KHOẢN GAME", body);
    }
}
