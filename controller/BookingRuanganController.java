package com.example.proyek.controller;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.proyek.model.Ruangan;
import com.example.proyek.model.BookingRuangan;
import com.example.proyek.model.RequestSurat;
import com.example.proyek.service.BookingRuanganService;
import com.example.proyek.service.RuanganService;

@RestController
@RequestMapping("/bookingRuangan")
public class BookingRuanganController {
	
	@Autowired
    private RuanganService ruanganService;
	
	@Autowired
    private BookingRuanganService ibService;
    @GetMapping("")
    public List<Ruangan> list() {
        return ruanganService.listAllRuangan();
    }
    
    @GetMapping("/admin/list")
    public List<BookingRuangan> listRS() {
        return ibService.listAllBookingRuangan();
    }

    @GetMapping("/mahasiswa")
    public ResponseEntity<List<BookingRuangan>> mahasiswalist(HttpSession session) {
    	Integer userId = (Integer) session.getAttribute("idUser");
        try {
            List<BookingRuangan> ibList = ibService.getBookingRuanganByUserId(userId);

            if (!ibList.isEmpty()) {
                return new ResponseEntity<>(ibList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/mahasiswa/book")
    public ResponseEntity<String> add(@RequestBody BookingRuangan ruangan) {
        List<BookingRuangan> listruangan = ibService.listAllBookingRuangan();

        for (BookingRuangan existingBooking : listruangan) {
            if (existingBooking.getIdRuangan() == ruangan.getIdRuangan()) {
                if (existingBooking.getTanggal().equals(ruangan.getTanggal())) {
                    // Calculate end time by adding the duration (waktu) to the start time (jam)
                	LocalTime existingEndTime = existingBooking.getJam().toLocalTime().plusHours(existingBooking.getWaktu());
                    LocalTime newBookingEndTime = ruangan.getJam().toLocalTime().plusHours(ruangan.getWaktu());

                    // Check for overlap in time ranges
                    if (ruangan.getJam().toLocalTime().isBefore(existingEndTime) && newBookingEndTime.isAfter(existingBooking.getJam().toLocalTime())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Ruangan ini sudah di-booking pada waktu tersebut.");
                    }
                }
            }
        }

        // If no conflict, proceed with booking
        ibService.saveBookingRuangan(ruangan);
        return ResponseEntity.status(HttpStatus.OK).body("Booking Ruangan berhasil.");
    }




    @PostMapping("/mahasiswa/update/{id}")
    public RedirectView update(@ModelAttribute BookingRuangan ib, @PathVariable Integer id, HttpSession session) {
    		BookingRuangan editib = ibService.getBookingRuangan(id);
            
    		editib.setIdRuangan(ib.getIdRuangan());
            editib.setKeperluan(ib.getKeperluan());
            editib.setTanggal(ib.getTanggal());
            editib.setJam(ib.getJam());
            editib.setWaktu(ib.getWaktu());
            
            if (editib.getCreatedAt() == null) {
            	editib.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            }
            ibService.saveBookingRuangan(editib);
            return new RedirectView("/mahasiswa/bookingRuangan/indexBooking");   
    }
    

    @GetMapping("/mahasiswa/delete/{id}")
    public RedirectView delete(@PathVariable Integer id, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("idUser");
        if (userId != null) {
            ibService.deleteBookingRuangan(id);
            return new RedirectView("/mahasiswa/bookingRuangan/indexBooking");
        } else {
            // Handle the case where userId is not found in the session
            return new RedirectView("/view-login"); // Redirect to login page or handle it accordingly
        }
    }
    
    @PostMapping("/admin/detail/{id}")
    public RedirectView handleAction(@PathVariable Integer id, @RequestParam String action) {
    	BookingRuangan editib = ibService.getBookingRuangan(id);
        if ("approve".equals(action)) {
            editib.setStatus(1);
        } else if ("reject".equals(action)) {
            editib.setStatus(2);
        }
        // Save the updated IB entity
        ibService.saveBookingRuangan(editib);
        // Redirect to a suitable URL after handling the action
        return new RedirectView("/admin/bookingRuangan/index"); // Replace with the actual URL
    }
    
    @GetMapping("/admin/delete/{id}")
    public RedirectView deleteAdmin(@PathVariable Integer id, HttpSession session) {
            ibService.deleteBookingRuangan(id);
            return new RedirectView("/admin/booking_ruangan/index");
    }
    
//    @GetMapping("/search")
//    public ResponseEntity<List<Ruangan>> searchBarang(@RequestParam("keyword") String keyword) {
//        List<Ruangan> hasilPencarian = (List<Ruangan>) ruanganService.getRuanganByKeyWord(keyword);
//        return new ResponseEntity<>(hasilPencarian, HttpStatus.OK);
//    }
}
