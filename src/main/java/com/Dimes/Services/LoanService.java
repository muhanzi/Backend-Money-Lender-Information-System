package com.Dimes.Services;

import com.Dimes.Models.Loan;
import com.Dimes.Repositories.LoanRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class LoanService {

    private LoanRepository loanRepository;

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public boolean createLoan(Loan loan) {

        Objects.requireNonNull(loan, "Loan can not be null");

        Loan loan1 = loanRepository.save(loan);
        if (loan1 != null) {
            return true;
        }
        return false;
    }

    public void deleteLoan(int id) {
        // Objects.requireNonNull(id,"Id can not be null");
        loanRepository.deleteById(id);
    }

    public Loan getLoanById(int id) {
        // Objects.requireNonNull(id,"Id can not be null");

        return loanRepository.findById(id).orElse(null);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    // lebon added this
    public LocalDateTime ConvertToLocalDateTime(Date issued_date) {
        //
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
        String startdate = date.format(issued_date);
        //
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss a");
        LocalDateTime time = LocalDateTime.parse(startdate, inputFormat);
        return time;
    }

    // lebon added this
    public void checkLoan(LocalDateTime date_issued, Loan loan) {
        //
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime date_due = date_issued;
        //
        if (loan.getPeriodType().equals("days")) {
            date_due = date_issued.plusDays(loan.getLoanPeriod());
        } else if (loan.getPeriodType().equals("weeks")) {
            date_due = date_issued.plusWeeks(loan.getLoanPeriod());
        } else if (loan.getPeriodType().equals("months")) {
            date_due = date_issued.plusMonths(loan.getLoanPeriod());
        }
        // after adding the lon period to the date_issued
        if (currentDateTime.isAfter(date_due)) {
            // only for loans with status --> onGoing
            if (loan.getStatus().equals("onGoing")) {
                loan.setStatus("failed to pay");
                loanRepository.save(loan); // update the status
            }
        }
    }

    // lebon added this
    public List<Loan> getLenderLoans(int LenderId) {
        loanRepository.findAllByLender(LenderId).forEach(loan -> {
            checkLoan(ConvertToLocalDateTime(loan.getDate_issued()), loan);
        });
        return loanRepository.findAllByLender(LenderId);
    }

}
