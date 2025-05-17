package com.example.demo.service.impl;

import com.example.demo.constant.Status;
import com.example.demo.dto.request.BookingRequest;
import com.example.demo.entity.Booking;
import com.example.demo.entity.Hour;
import com.example.demo.entity.Schedule;
import com.example.demo.entity.User;
import com.example.demo.repository.BookingRepo;
import com.example.demo.repository.HourRepo;
import com.example.demo.repository.ScheduleRepo;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

@Service
public class BookingService {

    @Autowired
    private ScheduleRepo scheduleRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private HourRepo hourRepo;

    @Autowired
    private JavaMailSender mailSender;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    // Lưu trữ mã OTP tạm thời: email -> {otp, expirationTime}
    private final Map<String, OtpData> otpStore = new HashMap<>();

    // Class để lưu trữ mã OTP và thời gian hết hạn
    private static class OtpData {
        String otp;
        long expirationTime;

        OtpData(String otp, long expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }
    }

    // Tạo mã OTP ngẫu nhiên (6 chữ số)
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Tạo số ngẫu nhiên từ 100000 đến 999999
        return String.valueOf(otp);
    }

    // Gửi mã OTP qua email
    public void sendOtpToEmail(String email) {
        String otp = generateOtp();
        long expirationTime = System.currentTimeMillis() + 3 * 60 * 1000; // Hết hạn sau 5 phút

        // Lưu mã OTP và thời gian hết hạn
        otpStore.put(email, new OtpData(otp, expirationTime));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Mã OTP Xác Minh Email - Bệnh Viện Đa Khoa Diệp Sinh");
            helper.setText(
                    "<h3>Mã OTP Xác Minh Email</h3>" +
                            "<p>Chào bạn,</p>" +
                            "<p>Mã OTP của bạn là: <strong>" + otp + "</strong></p>" +
                            "<p>Mã này có hiệu lực trong 3 phút. Vui lòng nhập mã OTP để xác minh email của bạn.</p>" +
                            "<p>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>" +
                            "<p>Trân trọng,</p>" +
                            "<p>Bệnh Viện Đa Khoa Diệp Sinh</p>",
                    true
            );
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi mã OTP qua email: " + e.getMessage());
        }
    }

    // Xác minh mã OTP
    public boolean verifyOtp(String email, String otp) {
        OtpData otpData = otpStore.get(email);
        if (otpData == null) {
            return false; // Không tìm thấy mã OTP
        }

        // Kiểm tra thời gian hết hạn
        if (System.currentTimeMillis() > otpData.expirationTime) {
            otpStore.remove(email); // Xóa mã OTP đã hết hạn
            return false;
        }

        // Kiểm tra mã OTP
        boolean isValid = otpData.otp.equals(otp);
        if (isValid) {
            otpStore.remove(email); // Xóa mã OTP sau khi xác minh thành công
        }
        return isValid;
    }

    private String generateMeaningfulToken(BookingRequest request) {
        String phone = request.getPhone();
        String lastFourDigits = phone.substring(phone.length() - 4);

        String fullName = request.getFullName()
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();

        String baseToken = "MKH" + lastFourDigits + fullName;

        String token = baseToken;
        int suffix = 0;
        Random random = new Random();

        while (bookingRepo.existsByToken(token)) {
            suffix = random.nextInt(100);
            token = baseToken + String.format("%02d", suffix);
        }

        return token;
    }

    private void sendConfirmationEmailToPatient(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(booking.getEmail());
            helper.setSubject("Xác Nhận Lịch Hẹn Khám Bệnh - Bệnh viện Đa Khoa Diệp Sinh");

            // Định dạng ngày cho đẹp hơn
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = dateFormat.format(booking.getDate());

            // Nội dung email với HTML và inline CSS
            String emailContent = "<!DOCTYPE html>" +
                    "<html lang='vi'>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;'>" +
                    "<table cellpadding='0' cellspacing='0' width='100%' style='max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                    "<tr>" +
                    "<td style='background-color: #007bff; text-align: center; padding: 20px; border-top-left-radius: 8px; border-top-right-radius: 8px;'>" +
                    "<h1 style='color: #ffffff; margin: 0; font-size: 24px;'>Xác Nhận Lịch Hẹn Khám Bệnh</h1>" +
                    "<p style='color: #e6f0ff; margin: 5px 0 0;'>Bệnh Viện Đa Khoa Diệp Sinh</p>" +
                    "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 30px;'>" +
                    "<h2 style='color: #333333; font-size: 20px; margin-bottom: 20px;'>Chào " + booking.getFullName() + ",</h2>" +
                    "<p style='color: #555555; font-size: 16px; line-height: 1.5; margin: 0 0 15px;'>Cảm ơn bạn đã đặt lịch hẹn khám bệnh tại Bệnh Viện Đa Khoa Diệp Sinh. Dưới đây là thông tin chi tiết về lịch hẹn của bạn:</p>" +
                    "<table cellpadding='8' cellspacing='0' style='width: 100%; border: 1px solid #e0e0e0; border-radius: 4px; background-color: #f9f9f9;'>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333; width: 150px;'>Tên bệnh nhân:</td>" +
                    "<td style='color: #555555;'>" + booking.getFullName() + "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333; width: 150px;'>Mã bệnh nhân:</td>" +
                    "<td style='color: #555555;'>" + booking.getToken() + "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333;'>Bác sĩ:</td>" +
                    "<td style='color: #555555;'>" + booking.getUser().getFullname() + "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333;'>Ngày khám:</td>" +
                    "<td style='color: #555555;'>" + formattedDate + "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333;'>Khung giờ:</td>" +
                    "<td style='color: #555555;'>" + hourRepo.findById(booking.getIdHour()).get().getName() + "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333;'>Phòng ban:</td>" +
                    "<td style='color: #555555;'>" + booking.getUser().getMajor().getName() + " , Phòng 302</td>" +
                    "</tr>" +
                    "</table>" +
                    "<h3 style='color: #333333; font-size: 18px; margin: 25px 0 15px;'>Hướng Dẫn Trước Khi Đến Khám</h3>" +
                    "<ul style='color: #555555; font-size: 16px; line-height: 1.5; padding-left: 20px; margin: 0 0 20px;'>" +
                    "<li>Vui lòng đến sớm trước giờ hẹn 15 phút để làm thủ tục đăng ký</li>" +
                    "<li>Sau khi làm thủ tục, quý khách sẽ nhận được số thứ tự khám bệnh, qúy khách vui lòng chờ trong giây lát để chờ đến khi gọi đến số và tên của mình.</li>" +
                    "<li>Mang theo giấy tờ tùy thân (CMND/CCCD) và thẻ BHYT (nếu có).</li>" +
                    "<li>Nếu bạn cần thay đổi hoặc hủy lịch hẹn, vui lòng quay lại ứng dụng phần: <strong>Tra cứu lịch khám</strong>. Tại đây quý khách vui lòng tìm lịch khám bằng việc sử dụng mã khách hàng trên màn hình để thực hiện thay đổi lịch khám bệnh.</li>" +
                    "<li style='color: red;'>Lưu ý: Trong trường hợp quý khách muốn đổi lịch khám bệnh khác, vui lòng chọn hủy lịch cũ ban dầu và đặt lại lịch mới.</li>" +
                    "<li >Chúc quý khách có một trải nghiệm tốt nhất ở bệnh viện đa khoa Diệp Sinh.</li>" +
                    "</ul>" +
                    "<p style='text-align: center; margin: 30px 0;'>" +
                    "</p>" +
                    "<p style='color: #555555; font-size: 14px; line-height: 1.5; text-align: center;'>Nếu bạn không phải là người đặt lịch hẹn này, vui lòng bỏ qua email này.</p>" +
                    "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='background-color: #f1f1f1; text-align: center; padding: 15px; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px;'>" +
                    "<p style='color: #777777; font-size: 14px; margin: 0;'>Bệnh Viện Đa Khoa Diệp Sinh</p>" +
                    "<p style='color: #777777; font-size: 14px; margin: 5px 0;'>Địa chỉ: 106 Đường Hoàng Quốc Việt, Hà Nội</p>" +
                    "<p style='color: #777777; font-size: 14px; margin: 0;'>Hotline: 035 994 789 | Email: support@benhviendiepsinh.com</p>" +
                    "</td>" +
                    "</tr>" +
                    "</table>" +
                    "</body>" +
                    "</html>";

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email xác nhận cho bệnh nhân: " + e.getMessage());
        }
    }

    public Booking bookSchedule(BookingRequest request) {
        // Kiểm tra lịch có tồn tại không
        Schedule schedule = scheduleRepo.findById(request.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch!"));

        // Kiểm tra khung giờ có khớp không
        if (!schedule.getHour().getId().equals(request.getHourId())) {
            throw new RuntimeException("Khung giờ không khớp với lịch này!");
        }

        // Kiểm tra lịch đã được đặt chưa
        if (schedule.getIsBooked()) {
            throw new RuntimeException("Khung giờ " + schedule.getHour().getName() + " đã được đặt!");
        }

        // Lấy thông tin bệnh nhân và bác sĩ
        User patient = userRepo.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân!"));
        User doctor = schedule.getDoctor();

        // Tạo token
        String token = generateMeaningfulToken(request);

        // Tạo đối tượng Booking
        Booking booking = new Booking();
        booking.setFullName(request.getFullName());
        booking.setDob(request.getDob());
        booking.setPhone(request.getPhone());
        booking.setEmail(request.getEmail());
        booking.setGender(request.getGender().name());
        booking.setAddress(request.getAddress());
        booking.setDate(schedule.getDate());
        booking.setIdHour(request.getHourId());
        booking.setStatus(Status.PENDING);
        booking.setNote(request.getNote());
        if (request.getToken() == null || request.getToken().isEmpty()) {
            booking.setToken(token);
        } else {
            booking.setToken(request.getToken());
        }
        booking.setUser(doctor);
        booking.setSchedule(schedule);

        // Lưu lịch hẹn
        booking = bookingRepo.save(booking);

        // Cập nhật trạng thái lịch
        schedule.setIsBooked(true);
        scheduleRepo.save(schedule);

        return booking;
    }



    public List<Booking> getPendingBookingsByDoctor(Long doctorId) {
        return bookingRepo.findByUserIdAndStatus(doctorId, Status.PENDING);
    }

    public void approveBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn!"));

        if (!booking.getStatus().equals(Status.PENDING)) {
            throw new RuntimeException("Lịch hẹn không ở trạng thái PENDING!");
        }

        booking.setStatus(Status.CONFIRMING);
        bookingRepo.save(booking);

        sendConfirmationEmailToPatient(booking);
    }

    public void rejectBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn!"));

        if (!booking.getStatus().equals(Status.PENDING)) {
            throw new RuntimeException("Lịch hẹn không ở trạng thái PENDING!");
        }

        booking.setStatus(Status.FAILURE);
        bookingRepo.save(booking);

        Schedule schedule = booking.getSchedule();
        if (schedule != null) {
            schedule.setIsBooked(false);
            scheduleRepo.save(schedule);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(booking.getEmail());
            helper.setSubject("Thông báo: Lịch hẹn của bạn đã bị từ chối");

            // Định dạng ngày cho đẹp hơn
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = dateFormat.format(booking.getDate());

            String emailContent = "<!DOCTYPE html>" +
                    "<html lang='vi'>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;'>" +
                    "<table cellpadding='0' cellspacing='0' width='100%' style='max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                    "<tr>" +
                    "<td style='background-color: #dc3545; text-align: center; padding: 20px; border-top-left-radius: 8px; border-top-right-radius: 8px;'>" +
                    "<h1 style='color: #ffffff; margin: 0; font-size: 24px;'>Thông Báo Từ Chối Lịch Hẹn</h1>" +
                    "<p style='color: #ffe6e6; margin: 5px 0 0;'>Bệnh Viện Đa Khoa Diệp Sinh</p>" +
                    "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 30px;'>" +
                    "<h2 style='color: #333333; font-size: 20px; margin-bottom: 20px;'>Chào " + booking.getFullName() + ",</h2>" +
                    "<p style='color: #555555; font-size: 16px; line-height: 1.5; margin: 0 0 15px;'>Rất tiếc, lịch hẹn của bạn đã bị từ chối bởi bác sĩ. Dưới đây là thông tin chi tiết:</p>" +
                    "<table cellpadding='8' cellspacing='0' style='width: 100%; border: 1px solid #e0e0e0; border-radius: 4px; background-color: #f9f9f9;'>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333; width: 150px;'>Tên bệnh nhân:</td>" +
                    "<td style='color: #555555;'>" + booking.getFullName() + "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333;'>Bác sĩ:</td>" +
                    "<td style='color: #555555;'>" + booking.getUser().getFullname() + "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333;'>Ngày khám:</td>" +
                    "<td style='color: #555555;'>" + formattedDate + "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-weight: bold; color: #333333;'>Khung giờ:</td>" +
                    "<td style='color: #555555;'>" + hourRepo.findById(booking.getIdHour()).get().getName() + "</td>" +
                    "</tr>" +
                    "</table>" +
                    "<p style='color: #555555; font-size: 16px; line-height: 1.5; margin: 20px 0;'>Nếu bạn muốn đặt lịch hẹn khác, vui lòng truy cập hệ thống của chúng tôi hoặc liên hệ qua hotline: <strong>0123 456 789</strong>.</p>" +
                    "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='background-color: #f1f1f1; text-align: center; padding: 15px; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px;'>" +
                    "<p style='color: #777777; font-size: 14px; margin: 0;'>Bệnh Viện Đa Khoa XYZ</p>" +
                    "<p style='color: #777777; font-size: 14px; margin: 5px 0;'>Địa chỉ: 123 Đường Sức Khỏe, Quận 1, TP. Hồ Chí Minh</p>" +
                    "<p style='color: #777777; font-size: 14px; margin: 0;'>Hotline: 0123 456 789 | Email: support@benhvienxyz.com</p>" +
                    "</td>" +
                    "</tr>" +
                    "</table>" +
                    "</body>" +
                    "</html>";

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email thông báo cho bệnh nhân: " + e.getMessage());
        }
    }

    public void confirmByPatient(String token) {
        Optional<Object> bookingOpt = bookingRepo.findByToken(token); // Sửa lỗi ép kiểu
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy lịch hẹn với mã này!");
        }

        Booking booking = (Booking) bookingOpt.get();
        if (!booking.getStatus().equals(Status.CONFIRMING)) {
            throw new RuntimeException("Lịch hẹn không ở trạng thái CONFIRMING!");
        }

        booking.setStatus(Status.SUCCESS);
        bookingRepo.save(booking);

        Schedule schedule = booking.getSchedule();
        if (schedule != null) {
            schedule.setIsBooked(true);
            scheduleRepo.save(schedule);
        }
    }
    public List<Booking> findBookingsByTokenFiltered(String token, Status status, Date startDate, Date endDate) {
        return bookingRepo.findBookingsByTokenFiltered(token, status, startDate, endDate);
    }

    public List<Booking> findBookingsByDoctorWithFilters(
            Long doctorId,
            Status status,
            Date startDate,
            Date endDate,
            String searchTerm) {
        return bookingRepo.findBookingsByDoctorWithFilters(
                doctorId, status, startDate, endDate, searchTerm != null ? searchTerm.trim() : null);
    }

    public void sendAppointmentReminderEmail(String to, String patientName, String doctorName, String date, String timeSlot) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Nhắc nhở lịch hẹn khám bệnh");
        String htmlBody = "<h2 style=\"color: #2c3e50; font-family: Arial, sans-serif;\">Nhắc Nhở Lịch Hẹn Khám Bệnh</h2>" +
                "<p style=\"font-family: Arial, sans-serif; color: #333;\">Kính gửi: <strong style=\"color: #2c3e50;\">" + patientName + "</strong>,</p>" +
                "<p style=\"font-family: Arial, sans-serif; color: #333;\">Phòng khám chúng tôi trân trọng nhắc nhở quý khách về lịch hẹn khám bệnh sắp tới với các thông tin chi tiết như sau:</p>" +
                "<table style=\"font-family: Arial, sans-serif; color: #333; margin: 15px 0; border-collapse: collapse; width: 100%; max-width: 500px;\">" +
                "<tr style=\"background-color: #f4f7f9;\">" +
                "<td style=\"padding: 10px; border: 1px solid #e0e0e0;\"><strong>Bác sĩ phụ trách:</strong></td>" +
                "<td style=\"padding: 10px; border: 1px solid #e0e0e0;\">" + doctorName + "</td>" + // Tạm hardcode, có thể lấy động từ bảng Doctor
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 10px; border: 1px solid #e0e0e0;\"><strong>Ngày khám:</strong></td>" +
                "<td style=\"padding: 10px; border: 1px solid #e0e0e0;\">" + date + "</td>" +
                "</tr>" +
                "<tr style=\"background-color: #f4f7f9;\">" +
                "<td style=\"padding: 10px; border: 1px solid #e0e0e0;\"><strong>Khung giờ:</strong></td>" +
                "<td style=\"padding: 10px; border: 1px solid #e0e0e0;\">" + timeSlot + "</td>" +
                "</tr>" +
                "</table>" +
                "<p style=\"font-family: Arial, sans-serif; color: #333;\">Quý khách vui lòng đến đúng giờ để đảm bảo lịch khám diễn ra thuận lợi. Nếu có nhu cầu thay đổi lịch hẹn, xin vui lòng liên hệ với chúng tôi qua:</p>" +
                "<ul style=\"font-family: Arial, sans-serif; color: #333;\">" +
                "<li>Email: <a href=\"mailto:support@phongkham.com\" style=\"color: #007bff;\">support@phongkham.com</a></li>" +
                "<li>Số điện thoại: <a href=\"tel:0123456789\" style=\"color: #007bff;\">0123 456 789</a></li>" +
                "</ul>" +
                "<p style=\"font-family: Arial, sans-serif; color: #333;\">Chúng tôi rất mong được phục vụ quý khách.</p>" +
                "<p style=\"font-family: Arial, sans-serif; color: #333;\">Trân trọng,</p>" +
                "<p style=\"font-family: Arial, sans-serif; color: #333; font-weight: bold;\">Phòng Khám Sức Khỏe Toàn Diện</p>" +
                "<p style=\"font-family: Arial, sans-serif; font-size: 12px; color: #777; border-top: 1px solid #e0e0e0; padding-top: 10px;\">" +
                "Đây là email tự động, vui lòng không trả lời trực tiếp. Nếu có thắc mắc, xin liên hệ qua thông tin trên." +
                "</p>";
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    // Chạy hàng ngày lúc 8h sáng
    public void sendAppointmentReminders() {
        // Lấy ngày hôm nay và ngày mai
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Thêm 1 ngày để lấy ngày mai
        Date tomorrow = calendar.getTime();

        // Đặt giờ, phút, giây, mili giây về 0 để so sánh chính xác ngày
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        tomorrow = calendar.getTime();

        // Truy vấn danh sách lịch hẹn của ngày mai với trạng thái SUCCESS
        List<Booking> appointments = bookingRepo.findByDateAndStatusSuccess(tomorrow);

        if (appointments.isEmpty()) {
            throw new RuntimeException("Không có lịch hẹn nào cho ngày mai với trạng thái SUCCESS.");
        }

        // Gửi email nhắc nhở cho từng bệnh nhân
        for (Booking booking : appointments) {
            try {
                // Lấy thông tin khung giờ từ bảng Hour
                Optional<Hour> hourOptional = hourRepo.findById(booking.getIdHour());
                String timeSlot = hourOptional.isPresent() ? hourOptional.get().getName() : "N/A";

                // Lấy thông tin bệnh nhân và bác sĩ
                String patientEmail = booking.getEmail();
                String patientName = booking.getFullName();
                String doctorName = booking.getUser().getFullname(); // Giả định User có trường fullName
                String date = dateFormatter.format(booking.getDate());

                // Gửi email
                sendAppointmentReminderEmail(
                        patientEmail,
                        patientName,
                        doctorName,
                        date,
                        timeSlot
                );
                System.out.println("Đã gửi email nhắc nhở cho: " + patientEmail);
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi email cho " + booking.getEmail() + ": " + e.getMessage());
                // Tiếp tục với lịch hẹn tiếp theo, không dừng lại
            }
        }
    }
}