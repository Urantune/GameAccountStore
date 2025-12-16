//import java.security.MessageDigest;
//
//String input = "wait" + user.getId();
//String fi;
//        try {
//MessageDigest md = MessageDigest.getInstance("MD5");
//byte[] digest = md.digest(input.getBytes());
//StringBuilder sb = new StringBuilder();
//            for (byte b : digest) sb.append(String.format("%02x", b));
//fi = sb.toString();
//        } catch (Exception e) {
//        throw new RuntimeException(e);
//        }
//
//String title = "Xác nhận tài khoản của bạn";
//String link = "http://localhost:8080/veryAccount/done/" + user.getId() + "/" + fi;
//String content =
//        "<p>Hãy nhấp vào liên kết dưới đây để kích hoạt tài khoản của bạn (hạn 2 phút):</p>"
//                + "<p><a href=\"" + link + "\">Nhấn vào đây để kích hoạt</a></p>"
//                + "<p>Nếu không bấm được, copy link sau dán vào trình duyệt:<br>" + link + "</p>";
//        sendMailTest.testSend(user.getEmail(), title, content);