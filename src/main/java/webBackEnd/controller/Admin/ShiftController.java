package webBackEnd.controller.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webBackEnd.entity.Shift;
import webBackEnd.entity.Staff;
import webBackEnd.repository.ShiftRepository;
import webBackEnd.service.AdministratorService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Controller
@RequestMapping("/adminHome")
public class ShiftController {

    @Autowired
    private AdministratorService administratorService;

    @Autowired
    private ShiftRepository shiftRepository;

    @GetMapping("/shift")
    public String shift(
            @RequestParam(value = "week", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            @RequestParam(value = "month", required = false) String month,
            Model model
    ) {
        LocalDate weekStart;
        if (month != null && !month.isBlank()) {
            YearMonth ym = YearMonth.parse(month);
            LocalDate firstDay = ym.atDay(1);
            weekStart = firstDay.with(DayOfWeek.MONDAY);
        } else {
            LocalDate base = (week != null) ? week : LocalDate.now();
            weekStart = base.with(DayOfWeek.MONDAY);
        }

        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate prevWeekStart = weekStart.minusWeeks(1);
        LocalDate nextWeekStart = weekStart.plusWeeks(1);

        List<LocalDate> days = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) days.add(weekStart.plusDays(i));

        List<Staff> staffList = administratorService.getAll();
        staffList.removeIf(s -> s == null || s.getRole() == null || !s.getRole().equalsIgnoreCase("STAFF"));
        staffList.sort(Comparator.comparing(Staff::getUsername, String.CASE_INSENSITIVE_ORDER));

        Map<String, UUID> shiftMap = buildShiftMap(weekStart);

        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("prevWeekStart", prevWeekStart);
        model.addAttribute("nextWeekStart", nextWeekStart);

        model.addAttribute("days", days);
        model.addAttribute("staffList", staffList);
        model.addAttribute("shiftMap", shiftMap);

        model.addAttribute("monthValue", YearMonth.from(weekStart).toString());

        return "admin/Shift";
    }

    @PostMapping("/shift/weekly-save")
    @Transactional
    public String saveWeek(
            @RequestParam("weekStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> dates,
            @RequestParam(value = "morningStaffId", required = false) List<UUID> morningIds,
            @RequestParam(value = "nightStaffId", required = false) List<UUID> nightIds,
            RedirectAttributes ra
    ) {
        int n = dates == null ? 0 : dates.size();

        for (int i = 0; i < n; i++) {
            LocalDate d = dates.get(i);

            UUID mId = (morningIds != null && morningIds.size() > i) ? morningIds.get(i) : null;
            UUID nId = (nightIds != null && nightIds.size() > i) ? nightIds.get(i) : null;

            upsertShift(d.atTime(0, 0), d.atTime(12, 0), "MORNING", mId);
            upsertShift(d.atTime(12, 0), d.plusDays(1).atTime(0, 0), "NIGHT", nId);
        }

        ra.addFlashAttribute("success", "Saved weekly shifts!");
        return "redirect:/adminHome/shift?week=" + weekStart;
    }

    private void upsertShift(LocalDateTime start, LocalDateTime end, String type, UUID staffId) {
        Optional<Shift> opt = shiftRepository.findByDateStartAndTimekeeping(start, type);

        if (staffId == null) {
            opt.ifPresent(shiftRepository::delete);
            return;
        }

        Staff staff = administratorService.getStaffByID(staffId);
        if (staff == null) return;

        Shift s = opt.orElseGet(() -> {
            Shift x = new Shift();
            x.setShiftId(UUID.randomUUID());
            x.setDateStart(start);
            x.setDateEnd(end);
            x.setTimekeeping(type);
            return x;
        });

        s.setStaff(staff);
        s.setDateStart(start);
        s.setDateEnd(end);
        s.setTimekeeping(type);

        shiftRepository.save(s);
    }

    private Map<String, UUID> buildShiftMap(LocalDate weekStart) {
        LocalDateTime from = weekStart.atStartOfDay();
        LocalDateTime to = weekStart.plusDays(7).atStartOfDay();

        List<Shift> shifts = shiftRepository.findAllByDateStartGreaterThanEqualAndDateStartLessThan(from, to);
        Map<String, UUID> map = new HashMap<>();

        for (Shift s : shifts) {
            if (s == null || s.getStaff() == null) continue;
            String day = s.getDateStart().toLocalDate().toString();
            map.put(day + "|" + s.getTimekeeping(), s.getStaff().getId());
        }
        return map;
    }
}
