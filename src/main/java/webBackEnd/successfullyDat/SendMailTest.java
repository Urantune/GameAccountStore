package webBackEnd.successfullyDat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import webBackEnd.service.very.*;

@RestController
public class SendMailTest {

    @Autowired
    MailService mailService;

    @GetMapping("/veryEmail")
    public String testSend(@RequestParam(defaultValue = "nguyenduykhang.pnt.11c1@gmail.com") String to,String title,String content) {
        try {
            mailService.sendHtml(to, title, content);
            return "Khang đẹp trai";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi gửi mail: " + e.getMessage();
        }
    }
}
