package com.example.demo.controller;
import com.example.demo.constant.Status;
import com.example.demo.dto.dto.BookingDTO;
import com.example.demo.dto.request.BookingRequest;
import com.example.demo.dto.response.StatsResponse;
import com.example.demo.entity.Booking;
import com.example.demo.repository.BookingRepo;
import com.example.demo.service.impl.BookingService;
import com.example.demo.service.impl.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private StatsService statsService;

    @GetMapping("/by-token-filtered")
    public ResponseEntity<List<Booking>> getBookingsByTokenFiltered(
            @RequestParam("token") String token,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Chuyển đổi status từ String sang enum Status
        Status status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Status.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value: " + statusStr);
            }
        }

        // Chuyển đổi LocalDateTime sang Date
        Date startDateConverted = null;
        Date endDateConverted = null;

        if (startDate != null) {
            startDateConverted = Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant());
        }
        if (endDate != null) {
            endDateConverted = Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant());
        }

        List<Booking> bookings = bookingService.findBookingsByTokenFiltered(token, status, startDateConverted, endDateConverted);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/by-token/{token}")
    public ResponseEntity<BookingDTO> getBookingByToken(@PathVariable String token) {
        // Tìm booking theo token
       List<Booking> bookings = bookingRepo.findByTokennAndStatus(token);

        if (bookings.size() == 0) {
            throw new RuntimeException("Booking not found with token: " + token);
        }

        Booking booking = bookings.get(0);

        // Chuyển đổi Booking thành BookingDTO
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setFullName(booking.getFullName());
        bookingDTO.setDob(booking.getDob());
        bookingDTO.setPhone(booking.getPhone());
        bookingDTO.setEmail(booking.getEmail());
        bookingDTO.setGender(booking.getGender());
        bookingDTO.setAddress(booking.getAddress());
        bookingDTO.setDate(booking.getDate());
        bookingDTO.setIdHour(booking.getIdHour());
        bookingDTO.setStatus(booking.getStatus());
        bookingDTO.setNote(booking.getNote());
        bookingDTO.setToken(booking.getToken());

        return ResponseEntity.ok(bookingDTO);
    }

    @GetMapping("/by-token-all/{token}")
    public ResponseEntity<List<Booking>> getBookingAllByToken(@PathVariable String token) {
        // Tìm booking theo token
        List<Booking> bookings = bookingRepo.findByTokennAndStatus(token);

        if (bookings.size() == 0) {
            throw new RuntimeException("Booking not found with token: " + token);
        }



        return ResponseEntity.ok(bookings);
    }
    @PostMapping("/otp/send")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        try {
            bookingService.sendOtpToEmail(email);
            return ResponseEntity.ok("Mã OTP đã được gửi đến email của bạn!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể gửi mã OTP: " + e.getMessage());
        }
    }
    @PostMapping
    public ResponseEntity<?> bookSchedule(@RequestBody BookingRequest request) {
        try {
            Booking booking = bookingService.bookSchedule(request);
            return ResponseEntity.ok(new BookingResponse(
                    "Đặt lịch thành công! Vui lòng chờ bác sĩ xác nhận.",
                    booking.getToken()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Có lỗi xảy ra khi đặt lịch!");
        }
    }

    // API để bác sĩ lấy danh sách lịch hẹn PENDING
    @GetMapping("/pending/{doctorId}")
    public ResponseEntity<List<Booking>> getBookingsByDoctorWithFilters(
            @PathVariable Long doctorId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate,
            @RequestParam(required = false) String searchTerm) {
        List<Booking> bookings = bookingService.findBookingsByDoctorWithFilters(
                doctorId, status, startDate, endDate, searchTerm);
        return ResponseEntity.ok(bookings);
    }

    // API để bác sĩ đồng ý lịch hẹn
    @PostMapping("/approve/{bookingId}")
    public ResponseEntity<?> approveBooking(@PathVariable Long bookingId) {
        try {
            bookingService.approveBooking(bookingId);
            return ResponseEntity.ok("Đã gửi email xác nhận cho bệnh nhân!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Có lỗi xảy ra khi xác nhận!");
        }
    }

    @PostMapping("/reminders/send")
    public ResponseEntity<String> sendAppointmentReminders() {
        try {
            bookingService.sendAppointmentReminders();
            return ResponseEntity.ok("Email nhắc lịch hẹn đã được gửi đến bệnh nhân.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi gửi email nhắc nhở: " + e.getMessage());
        }
    }

    // API để bác sĩ từ chối lịch hẹn
    @PostMapping("/reject/{bookingId}")
    public ResponseEntity<?> rejectBooking(@PathVariable Long bookingId) {
        try {
            bookingService.rejectBooking(bookingId);
            return ResponseEntity.ok("Đã từ chối lịch hẹn!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Có lỗi xảy ra khi từ chối lịch hẹn!");
        }
    }

    // API để bệnh nhân xác nhận lịch hẹn qua email
    @GetMapping("/confirm-patient/{token}")
    public ResponseEntity<?> confirmByPatient(@PathVariable String token) {
        try {
            bookingService.confirmByPatient(token);
            return ResponseEntity.ok("Xác nhận lịch hẹn thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Có lỗi xảy ra khi xác nhận!");
        }
    }



    @PostMapping("/otp/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = bookingService.verifyOtp(email, otp);
        if (isValid) {
            return ResponseEntity.ok("Xác minh email thành công!");
        } else {
            return ResponseEntity.badRequest().body("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }
    }

    @DeleteMapping("/cancel/{token}")
    public ResponseEntity<String> cancelBooking(@PathVariable String token) {
        // Tìm lịch hẹn theo token
        List<Booking> bookings = bookingService.findBookingsByTokenFiltered(token, null, null, null);
        if (bookings.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy lịch hẹn với mã: " + token);
        }

        Booking booking = bookings.get(bookings.size() - 1);

        // Kiểm tra thời gian
        Date createdAt = booking.getCreatedAt();
        Date currentTime = new Date();
        long timeDifference = currentTime.getTime() - createdAt.getTime();
        long eightHoursInMs = TimeUnit.HOURS.toMillis(8); // 8 tiếng

        // Nếu đã quá 5 phút, tự động chuyển trạng thái sang SUCCESS
        if (timeDifference > eightHoursInMs) {
            if (!booking.getStatus().equals(Status.SUCCESS)) {
                booking.setStatus(Status.SUCCESS);
                bookingRepo.save(booking);
            }
            return ResponseEntity.ok("Lịch hẹn đã được xác nhận thành công do đã quá 5 phút!");
        }

        // Nếu chưa quá 5 phút, kiểm tra trạng thái để hủy
        if (!booking.getStatus().equals(Status.CONFIRMING)) {
            return ResponseEntity.badRequest().body("Chỉ có thể hủy lịch hẹn ở trạng thái đang xác nhận!");
        }

        // Thực hiện hủy lịch
        booking.setStatus(Status.FAILURE);
        booking.getSchedule().setIsBooked(false);
        booking.setSchedule(null);
        bookingRepo.save(booking);

        return ResponseEntity.ok("Hủy lịch hẹn thành công!");
    }

    @PutMapping("/set-status-success/{token}")
    public ResponseEntity<String> setStatusSuccess(@PathVariable String token) {
        // Tìm lịch hẹn theo token
        List<Booking> bookings = bookingService.findBookingsByTokenFiltered(token, null, null, null);
        if (bookings.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy lịch hẹn với mã: " + token);
        }

        Booking booking = bookings.get(bookings.size() - 1);

        // Chỉ cho phép cập nhật nếu trạng thái hiện tại là CONFIRMING
        if (!booking.getStatus().equals(Status.CONFIRMING)) {
            return ResponseEntity.badRequest().body("Lịch hẹn không ở trạng thái đang xác nhận, không thể cập nhật!");
        }

        // Kiểm tra thời gian (dự phòng)
        Date createdAt = booking.getCreatedAt();
        Date currentTime = new Date();
        long timeDifference = currentTime.getTime() - createdAt.getTime();
        long eightHoursInMs = TimeUnit.HOURS.toMillis(8); // 8 tiếng

        if (timeDifference <= eightHoursInMs) {
            return ResponseEntity.badRequest().body("Chưa quá 8 tiếng, không thể chuyển trạng thái sang thành công!");
        }

        // Cập nhật trạng thái sang SUCCESS
        booking.setStatus(Status.SUCCESS);
        bookingRepo.save(booking);

        return ResponseEntity.ok("Cập nhật trạng thái thành công!");
    }
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = statsService.getStats();
        return ResponseEntity.ok(stats);
    }

    private static class BookingResponse {
        private String message;
        private String token;

        public BookingResponse(String message, String token) {
            this.message = message;
            this.token = token;
        }

        public String getMessage() {
            return message;
        }

        public String getToken() {
            return token;
        }
    }
}
